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
package com.ibm.cusp.graph.errors;

import com.ibm.cusp.graph.stages.Stage;

import java.util.Arrays;
import java.util.Set;

import static com.ibm.cusp.graph.errors.CuspErrorCode.INFINITE_LOOP;

public class InfiniteLoopException extends CuspConstructionError {
    public InfiniteLoopException() {
        super(INFINITE_LOOP, "Infinite loop detected");
    }

    public InfiniteLoopException(Set<Stage> loop) {
        super(INFINITE_LOOP, "Infinite loop detected: {0}", Arrays.toString(loop.toArray()));
    }
}
