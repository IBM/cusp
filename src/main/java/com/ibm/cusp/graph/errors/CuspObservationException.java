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
