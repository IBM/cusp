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
package com.ibm.cusp.graph.stages;

public interface Stage<S,T> {
    /**
     * A unique identifier for this stage.
     * @return
     */
    String name();

    /**
     * The input data type that this stage accepts. See {@link AbstractStage} for implementation.
     * @return
     */
    Class<S> getInputType();

    /**
     * The output data type that this stage emits. See {@link AbstractStage} for implementation.
     * @return
     */
    Class<T> getOutputType();

    /**
     * The implementation of this stage.
     * @return
     */
    T execute(S input) throws Exception;
}
