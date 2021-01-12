/**
 * BEGIN_COPYRIGHT
 *
 * IBM Confidential
 * OCO Source Materials
 *
 * 5727-I17
 * (C) Copyright IBM Corp. 2021 All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 * END_COPYRIGHT
 */
package com.ibm.cusp.execution;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.ibm.cusp.graph.Cusp;
import com.ibm.cusp.graph.errors.*;
import com.ibm.cusp.graph.stages.Stage;
import com.ibm.cusp.graph.stages.StageOutcomes;
import com.linkedin.parseq.*;
import com.linkedin.parseq.function.Consumer1;
import com.linkedin.parseq.promise.PromiseException;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CuspExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cusp cusp;
    private final Map<String, Stopwatch> stopwatches = Maps.newConcurrentMap();
    private StageOutcomeListener stageOutcomeListener = new NoOpStageOutcomeListener();
    private Engine engine;
    private Task<?> pipeline;
    private String initialStageName;
    private Executor taskExecutor;
    private ScheduledExecutorService timerScheduler;

    public CuspExecutor(Cusp cusp) {
        this.cusp = cusp;
    }

    public CuspExecutor(Cusp cusp, Executor taskExecutor, ScheduledExecutorService timerScheduler) {
        this.cusp = cusp;
        useExecutors(taskExecutor, timerScheduler);
    }

    public void useStageOutcomeListener(StageOutcomeListener stageOutcomeListener) {
        this.stageOutcomeListener = stageOutcomeListener;
    }

    public void useEngine(Engine engine) {
        if(this.engine != null) {
            engine.shutdown();
        }

        this.engine = engine;
    }

    public void useExecutors(Executor taskExecutor, ScheduledExecutorService timerScheduler) {
        createEngine(taskExecutor, timerScheduler);
    }

    public void constructPipeline(String initialStageName, Object input) {
        this.cusp.validateGraph();
        this.initialStageName = initialStageName;
        this.pipeline = toTask(cusp.getStage(initialStageName), input);
    }

    public Task<?> getPipeline() {
        return this.pipeline;
    }

    public Executor getTaskExecutor() {
        return taskExecutor;
    }

    public Executor getTimerScheduler() {
        return timerScheduler;
    }

    void constructPipeline(Task<?> pipeline) {
        this.pipeline = pipeline;
    }

    // TODO: find a way to avoid the case of assigning the output of this to a variable of the wrong type,
    // which will compile but will fail at runtime with a ClassCashException
    public <T> T execute() throws InterruptedException, UnknownExecutionError, StageFailedException {
        if (engine == null) {
            createEngine();
        }

        if (pipeline == null) {
            throw new StageNotFoundException(this.initialStageName);
        }

        logger.debug("PIPELINE: {}", pipeline);

        logger.debug("Running pipeline");
        engine.run(pipeline);

        logger.debug("Awaiting pipeline");
        pipeline.await();

        try {
            logger.debug("Getting pipeline output");
            T output = (T) pipeline.get();

            logger.debug("Done running pipeline");
            logger.debug("FINAL OUTPUT: {}", output);

            return output;
        } catch (PromiseException e) {
            Throwable cause = e.getCause();
            if (cause instanceof StageFailedException) {
                StageFailedException thrownException = (StageFailedException) cause;
                throw thrownException;
            } else if (cause instanceof MultiException) {
                MultiException thrownException = (MultiException) cause;
                for (Throwable subcause : thrownException.getCauses()) {
                    if (subcause instanceof StageFailedException) {
                        StageFailedException subthrownException = (StageFailedException) subcause;
                        throw subthrownException;
                    }
                }

                throw new UnknownExecutionError(cause);
            } else {
                throw new UnknownExecutionError(cause);
            }
        }
    }

    public String generateTrace() {
        return pipeline.getTrace().toString();
    }

    private Task<?> toTask(Stage initialStage, Object input) {
        return toTask(Task.value("initialization", input), initialStage);
    }

    @SuppressWarnings("unchecked") // higher kinded types would be nice
    private Task<?> toTask(Task<?> previousTask, Stage... stages) {
        logger.debug("Recursing from task " + previousTask + " to construct task subgraph based at " + stages[0].name());

        Task<?> currentTask = createTask(previousTask, stages);
        currentTask = attachRecoveryStages(previousTask, currentTask, stages[0]);
        currentTask = attachSuccessStages(currentTask, stages[0]);
        // TODO: flatmap, etc?

        return currentTask;
    }

    private <S,T> Task<T> createTask(Task<S> previousTask, S previousTaskOutput, Stage<S,T> stage) {
        // The reason for using Tasks#blocking below is that it is required for concurrency
        // see: https://github.com/linkedin/parseq/issues/63
        Task<T> task = Task.blocking(stage.name(), () -> {
            logger.debug("Executing stage {} from output of task {}, which was {}", stage.name(), previousTask, previousTaskOutput);

            T currentTaskOutput = executeStageWithTimer(stage, previousTaskOutput);

            stageOutcomeListener.success(stage, currentTaskOutput, getElapsed(stage.name()));
            logger.debug("Stage {} resulted in {}", stage.name(), currentTaskOutput);
            return currentTaskOutput;
        }, this.taskExecutor);

        return task.onFailure("failure handler for " + stage.name(), observeFailureOf(task, stage));
    }

    private <S, T> Task<T> createTask(Task<S> previousTask, Stage<S, T>... stages) {
        if(stages.length == 1) {
            Task<Task<T>> task = previousTask.map("use output of " + previousTask.getName(), previousTaskOutput -> createTask(previousTask, previousTaskOutput, stages[0]));
            return Task.flatten("flattened " + stages[0].name(), task);
        } else {
            String combinedStageName = Arrays.stream(stages)
                    .map(Stage::name)
                    .collect(Collectors.joining(" and "));

            Task<Task<T>> task = previousTask.map("use output of " + previousTask.getName(), previousTaskOutput -> {
                List<Task<T>> taskList = Arrays.stream(stages)
                        .map(stage -> createTask(previousTask, previousTaskOutput, stage))
                        .collect(Collectors.toList());


                ParTask<T> parallelTask = Task.par(taskList);
                return parallelTask.map(combinedStageName, parallelTaskOutput -> parallelTaskOutput.get(0));
            });

            return Task.flatten("flattened " + combinedStageName, task);
        }
    }

    private Consumer1<Throwable> observeFailureOf(Task task, Stage stage) {
        return thrown -> {
            if (task.isFailed()) {
                logger.info("Stage {} failed: {}", stage.name(), thrown.getMessage());
                if(!hasRecoveryStage(stage)) {
                    stageOutcomeListener.failure(stage, getActualCause(thrown), getElapsed(stage.name()));
                }
                return;
            }
        };
    }

    private boolean hasRecoveryStage(Stage stage) {
        return !cusp.getNextStages(stage.name(), StageOutcomes.RECOVERABLE_FAILURE).isEmpty();
    }

    private Throwable getActualCause(Throwable throwable) {
        Throwable actualStageFailureCause = throwable;
        if (actualStageFailureCause instanceof MultiException) {
            // ParTask wraps the failures in a MultiException
            actualStageFailureCause = actualStageFailureCause.getCause();
        }
        if (actualStageFailureCause instanceof StageFailedException) {
            // Get what exception caused the stage failure
            actualStageFailureCause = actualStageFailureCause.getCause();
        }
        return actualStageFailureCause;
    }

    @SuppressWarnings("unchecked")
    private Task<?> attachRecoveryStages(Task<?> previousTask, Task<?> currentTask, Stage currentStage) {
        Optional<Stage> maybeRecoverableStage = cusp.getNextStage(currentStage.name(), StageOutcomes.RECOVERABLE_FAILURE);
        logger.debug("maybeRecoverableStage for {}: {}", currentStage.name(), maybeRecoverableStage);
        if(maybeRecoverableStage.isPresent()) {
            Stage recoverableStage = maybeRecoverableStage.get();
            logger.debug("Attaching recoverable stage to {}: {}", currentStage.name(), recoverableStage.name());
            return currentTask.recoverWith(currentStage.name() + " recovering with " + recoverableStage.name(),
                    throwable -> {
                        Throwable actualStageFailureCause = getActualCause(throwable);
                        stageOutcomeListener.recover(currentStage, recoverableStage, actualStageFailureCause, getElapsed(currentStage.name()));
                        logger.debug("Recovering from {} with {}: {}", currentStage.name(), recoverableStage.name(),throwable);

                        return (Task) toTask(previousTask, recoverableStage);
                    });
        } else {
            return currentTask;
        }
    }

    private Task<?> attachSuccessStages(Task<?> currentTask, Stage currentStage) {
        Set<Stage> successStages = cusp.getNextStages(currentStage.name(), StageOutcomes.SUCCESS);

        logger.debug("maybeSuccessStage for {}: {}", currentStage.name(), successStages);
        if(!successStages.isEmpty()) {
            Set<Stage> leafStages = successStages.stream().filter(stage -> cusp.isTerminal(stage.name())).collect(Collectors.toSet());
            Set<Stage> internalStages = successStages;
            internalStages.removeAll(leafStages);

            if(internalStages.size() > 1) {
                throw new NondeterministicExecutionException(
                        CuspErrorCode.NONDETERMINISTIC_PIPELINE,
                        "Stage {0}'s output was defined as being used by multiple downstream stages, which is not supported; those downstream stages were: {1}",
                        currentStage.name(),
                        new ArrayList<>(internalStages).toString());
            }

            List<Stage> stages = new ArrayList<>();
            stages.addAll(internalStages);
            stages.addAll(leafStages);

            return toTask(currentTask, stages.toArray(new Stage[0]));

        } else {
            return currentTask;
        }
    }

    private void createEngine() {
        createEngine(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2),
                Executors.newScheduledThreadPool(1));
    }

    private void createEngine(Executor taskExecutor, ScheduledExecutorService timerScheduler) {
        this.taskExecutor = taskExecutor;
        this.timerScheduler = timerScheduler;

        if(engine == null) {
            synchronized (this) {
                if(engine == null) {
                    this.engine = new EngineBuilder()
                            .setTaskExecutor(taskExecutor)
                            .setTimerScheduler(timerScheduler)
                            .build();
                }
            }
        }
    }

    private <S, T> T executeStageWithTimer(Stage<S, T> stage, S previousTaskOutput) throws StageFailedException {
        T currentTaskOutput;
        try {
            startTimerForStage(stage.name());

            currentTaskOutput = stage.execute(previousTaskOutput);

            stopTimerForStage(stage.name());
        } catch (Exception e) {
            stopTimerForStage(stage.name());

            throw new StageFailedException(stage.name(), e);
        }

        return currentTaskOutput;
    }

    private void startTimerForStage(String name) {
        if (stopwatches.containsKey(name)) {
            logger.error("stage already found: {}", name);
        }
        stopwatches.putIfAbsent(name, Stopwatch.createStarted());
    }

    private void stopTimerForStage(String stageName) {
        Stopwatch stopwatch = stopwatches.get(stageName);
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
    }

    private long getElapsed(String stageName) {
        long elapsed = stopwatches.getOrDefault(stageName, Stopwatch.createUnstarted()).elapsed(TimeUnit.MILLISECONDS);
        stopwatches.remove(stageName);
        return elapsed;
    }

    private static class NoOpStageOutcomeListener implements StageOutcomeListener {
        @Override
        public void success(Stage currentStage, Object currentStageOutput, long elapsedMs) { }

        @Override
        public void failure(Stage failureStage, Throwable throwable, long elapsedMs) { }

        @Override
        public void recover(Stage currentStage, Stage recoverStage, Throwable throwable, long elapsedMs) { }
    }
}
