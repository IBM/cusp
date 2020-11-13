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
package com.ibm.cusp.execution;

import com.ibm.cusp.graph.stages.Stage;

/**
 * This interface allows the caller of an {@link CuspExecutor} pipeline execution to gather detailed data about
 * each stage of the pipeline execution.
 *
 * A pipeline execution completes with either a successful output object or a thrown error or exception
 * describing an unrecoverable stage failure. This does not allow the caller the ability to know if there were
 * recovered failures in a successful outcome, or exactly which failures occurred during an execution that resulted
 * in a final thrown error or exception result.
 *
 * By providing an implementation of this interface, the user can respond to the details of every stage outcome
 * in the pipeline as they require. For example, they may store in their implementation the elapsed time of each
 * stage to report for metrics or logging analysis. Alternatively, they may record when stages are successfully
 * recovered to ensure that there are no legitimate errors being hidden by successful fallback responses. In
 * addition, they could record all failures that occur in the pipeline stages to more accurately diagnose problems
 * when the end result of a pipeline execution is failure.
 */
public interface StageOutcomeListener {
    /**
     * This method will be called when a stage completes successfully, providing the stage that completed, the output
     * of the stage, and the elapsed time in milliseconds it took the stage to execute.
     *
     * @param currentStage the stage that completed executing with a successful result
     * @param currentStageOutput the output of the completed stage
     * @param elapsedMs time in milliseconds that elapsed during execution of the stage
     */
    void success(Stage currentStage, Object currentStageOutput, long elapsedMs);

    /**
     * When a stage fails, an exception will be thrown for analysis or reporting.
     *
     * This method will be called on the provided listener in this case, providing which stage failed, the cause of the
     * failure, and the elapsed time in milliseconds since the failing stage began executing.
     *
     * @param failureStage the stage that will consume the exception for failure analysis
     * @param throwable the exception or error that was thrown by the current stage
     * @param elapsedMs time in milliseconds that elapsed during execution of the stage until the pipeline processed the failure
     */
    void failure(Stage failureStage, Throwable throwable, long elapsedMs);

    /**
     * When a stage is routed to another with a {@link com.ibm.cusp.graph.stages.StageOutcomes#RECOVERABLE_FAILURE},
     * if that stage fails, the following stage will be run to attempt to recover from the failure and continue the
     * pipeline successfully.
     *
     * This method will be called on the provided listener in this case, providing which stage failed, which stage
     * will attempt the recovery, the cause of the failure, and the elapsed time in milliseconds since the failing stage
     * began executing.
     *
     * @param currentStage the stage that failed with a thrown exception or error
     * @param recoverStage the stage that will be executed in an attempt to recover from the thrown exception or error
     * @param throwable the exception or error that was thrown by the current stage
     * @param elapsedMs time in milliseconds that elapsed during execution of the stage until the pipeline processed the failure
     */
    void recover(Stage currentStage, Stage recoverStage, Throwable throwable, long elapsedMs);
}
