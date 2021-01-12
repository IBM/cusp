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

public class CuspExecutionError extends Exception implements CuspError<CuspErrorCode> {
    private final String description;
    private final CuspErrorCode code;

    public CuspExecutionError(Throwable t, CuspErrorCode code, String description) {
        super(code + ": " + description, t);

        this.code = code;
        this.description = description;
    }

    public CuspExecutionError(Throwable t, CuspErrorCode code, String description, Object... args) {
        super(code + ": " + MessageFormat.format(description, args), t);

        this.code = code;
        this.description = MessageFormat.format(description, args);
    }

    @Override
    public CuspErrorCode getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Error {0}: {1}", code.name(), description);
    }
}
