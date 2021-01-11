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

import static com.ibm.cusp.graph.errors.CuspErrorCode.INVALID_STAGE_INPUT_DATA_TYPE;

public class InvalidStageInputException extends CuspConstructionError {
    public InvalidStageInputException(String source, String target, Class expectedType, Class observedType) {
        super(INVALID_STAGE_INPUT_DATA_TYPE,
                "Invalid input data type for stage {0} from stage {1}: expected {2} but got {3}",
                target,
                source,
                expectedType,
                observedType);
    }
}
