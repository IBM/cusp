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

import static com.ibm.cusp.graph.errors.CuspErrorCode.STAGE_ALREADY_EXISTS;

public class StageAlreadyExistsException extends CuspConstructionError {
    public StageAlreadyExistsException(String stageName) {
        super(STAGE_ALREADY_EXISTS,
                "Tried to create already existing stage with name: {0}",
                stageName);
    }
}
