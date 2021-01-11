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

public enum CuspErrorCode {
    INVALID_STAGE_INPUT_DATA_TYPE,
    NONDETERMINISTIC_PIPELINE,
    STAGE_FAILED,
    STAGE_ALREADY_EXISTS,
    STAGE_NOT_FOUND,
    OBSERVATION_ERROR,
    INFINITE_LOOP,
    EMPTY_PIPELINE,
    UNREACHABLE_STAGE,
    UNKNOWN
}
