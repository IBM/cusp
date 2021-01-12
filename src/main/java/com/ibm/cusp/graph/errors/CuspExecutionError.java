/**
 * Copyright (c) 2020 International Business Machines
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

    public CuspExecutionError(CuspErrorCode code, String description) {
        super(code + ": " + description);

        this.code = code;
        this.description = description;
    }

    public CuspExecutionError(CuspErrorCode code, String description, Object... args) {
        super(code + ": " + MessageFormat.format(description, args));

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
