/**
 * BEGIN_COPYRIGHT
 *
 * IBM Confidential
 * OCO Source Materials
 *
 * 5727-I17
 * (C) Copyright IBM Corp. 2020 All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 * END_COPYRIGHT
 */
package com.ibm.cusp.graph;

import com.ibm.cusp.graph.conditions.Conditions;
import com.ibm.cusp.graph.errors.*;
import com.ibm.cusp.graph.routes.LabeledEdge;
import com.ibm.cusp.graph.stages.Stage;
import com.ibm.cusp.graph.stages.StageOutcomes;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Cusp {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Stage> stages;
    private final Graph<Stage, LabeledEdge> stageGraph;
    private CycleDetector cycleDetector;

    public Cusp() {
        stages = new HashMap<>();
        stageGraph = new DefaultDirectedGraph<>(LabeledEdge.class);
    }

    /**
     * Add a new stage, identified uniquely by the stage's declared name.
     * @param stage
     * @return
     * @throws StageAlreadyExistsException
     */
    public Stage addStage(Stage stage) throws StageAlreadyExistsException {
        logger.debug("Creating stage: {}", stage.name());

        if (stages.containsKey(stage.name())) {
            throw new StageAlreadyExistsException(stage.name());
        }

        stages.put(stage.name(), stage);
        stageGraph.addVertex(stage);

        logger.debug("Created stage: {}", stage.name());
        return stage;
    }

    public boolean isTerminal(String stageName) {
        Stage stage = getValidatedStage(stageName);
        return isTerminal(stage);
    }

    private boolean isTerminal(Stage stage) {
        return stageGraph.outgoingEdgesOf(stage).isEmpty();
    }

    /**
     * Get stage represented by identifier {@code stageName}.
     *
     * @param stageName stage identifier
     * @return
     */
    public Stage getStage(String stageName) {
        return getValidatedStage(stageName);
    }

    /**
     * Get stage that follows when stage identified by {@code stageName} exits with outcome {@code outcome}.
     *
     * @param stageName stage identifier
     * @param outcome
     * @return
     */
    public Optional<Stage> getNextStage(String stageName, StageOutcomes outcome) {
        Stage stage = getValidatedStage(stageName);
        return getNextStage(stage, outcome);
    }

    private Optional<Stage> getNextStage(Stage stage, StageOutcomes outcome) {
        return stageGraph.outgoingEdgesOf(stage)
                .stream()
                .filter(edge -> edge.getOutcome() == outcome)
                .map(stageGraph::getEdgeTarget)
                .findFirst();
    }

    /**
     * Get all stages that follow when stage identified by {@code stage} exits with outcome {@code outcome}.
     *
     * @param stageName
     * @param outcome
     * @return
     */
    public Set<Stage> getNextStages(String stageName, StageOutcomes outcome) {
        Stage stage = getValidatedStage(stageName);
        return getNextStages(stage, outcome);
    }

    private Set<Stage> getNextStages(Stage stage, StageOutcomes outcome) {
        return stageGraph.outgoingEdgesOf(stage)
                .stream()
                .filter(edge -> edge.getOutcome() == outcome)
                .map(stageGraph::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    /**
     * Indicate that when the stage identified by name {@code sourceName} exits with outcome {@code outcome}, it should be
     * followed by the stage identified by name {@code targetName}.
     *
     * @param sourceName
     * @param outcome
     * @param targetName
     * @param conditions
     * @throws CuspConstructionError
     */
    public void addRoute(String sourceName, StageOutcomes outcome, String targetName, Conditions... conditions) throws CuspConstructionError {
        Stage source = getValidatedStage(sourceName);
        Stage target = getValidatedStage(targetName);

        validateDesiredRoute(source, outcome, target);

        addRoute(source, outcome, target, conditions);
    }

    private void addRoute(Stage source, StageOutcomes outcome, Stage target, Conditions... conditions) throws CuspConstructionError {
        logger.debug("Stage {} with outcome {} maps to {}", source.name(), outcome, target.name());
        stageGraph.addEdge(source, target, new LabeledEdge(source.name(), target.name(), outcome)); // TODO: support conditions
    }

    private Stage getValidatedStage(String stageName) {
        Stage stage = stages.get(stageName);
        validateStage(stageName, stage);

        return stage;
    }

    private void validateStage(String stageName, Stage stage) {
        assertOrThrow(stage != null, constructInvalidStateInputException(stageName));
    }

    private void validateDesiredRoute(Stage source, StageOutcomes outcome, Stage target) throws InvalidStageInputException {
        switch(outcome) {
            case SUCCESS:
                validateSuccessRoute(source, target);
                break;
            case RECOVERABLE_FAILURE:
                validateRecoveryRoute(source, target);
                break;
        }
    }

    private void validateRecoveryRoute(Stage source, Stage target) throws InvalidStageInputException {
        assertOrThrow(source.getInputType().equals(target.getInputType()), constructInvalidStateInputException(source, target));
    }

    private void validateSuccessRoute(Stage source, Stage target) throws InvalidStageInputException {
        assertOrThrow(source.getOutputType().equals(target.getInputType()), constructInvalidStateInputException(source, target));
    }

    public void validateGraph() {
        assertOrThrow(!GraphTests.isEmpty(stageGraph), new EmptyPipelineException());
        assertOrThrow(GraphTests.isConnected(stageGraph), new UnreachableStageException());
        assertOrThrow(!GraphTests.hasSelfLoops(stageGraph), new InfiniteLoopException());
        assertOrThrow(!this.getCycleDetector().detectCycles(), new InfiniteLoopException(this.getCycleDetector().findCycles()));
    }

    private CycleDetector getCycleDetector() {
        if(this.cycleDetector == null) {
            synchronized(this) {
                if(this.cycleDetector == null) {
                    this.cycleDetector = new CycleDetector(this.stageGraph);
                }
            }
        }

        return this.cycleDetector;
    }

    public Graph<Stage, LabeledEdge> getStageGraph() {
        return stageGraph;
    }

    private StageNotFoundException constructInvalidStateInputException(String stageName) {
        return new StageNotFoundException(stageName);
    }

    private InvalidStageInputException constructInvalidStateInputException(Stage source, Stage target) {
        return new InvalidStageInputException(source.name(), target.name(), target.getInputType(), source.getOutputType());
    }

    private void assertOrThrow(boolean condition, CuspConstructionError t) throws InvalidStageInputException {
        assert condition : t.getMessage();
        if(!condition) {
            throw t;
        }
    }
}
