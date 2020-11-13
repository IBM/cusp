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
package com.ibm.cusp.graph.errors;

import static com.ibm.cusp.graph.errors.CuspErrorCode.NONDETERMINISTIC_PIPELINE;

public class NondeterministicExecutionException extends CuspConstructionError {
    public NondeterministicExecutionException(CuspErrorCode code, String description, Object... args) {
        super(NONDETERMINISTIC_PIPELINE,
                "Multiple nonterminal intermediate stages are not supported");
    }
}
