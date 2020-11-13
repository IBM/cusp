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

import static com.ibm.cusp.graph.errors.CuspErrorCode.UNKNOWN;

public class UnknownExecutionError extends CuspExecutionError {
    public UnknownExecutionError(Throwable t) {
        super(t, UNKNOWN,
                "Unknown exception encountered: {0}",
                t.getMessage());
    }
}
