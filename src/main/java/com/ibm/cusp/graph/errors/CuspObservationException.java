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

import java.text.MessageFormat;

import static com.ibm.cusp.graph.errors.CuspErrorCode.OBSERVATION_ERROR;

public class CuspObservationException extends CuspConstructionError {
    public CuspObservationException(String message) {
        super(OBSERVATION_ERROR, "Observation error: {0}", message);
    }

    public CuspObservationException(String message, Object... args) {
        super(OBSERVATION_ERROR, "Observation error: {0}", MessageFormat.format(message, args));
    }
}
