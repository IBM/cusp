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
package com.ibm.cusp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cusp.graph.stages.AbstractStage;

class FailingLogPassageStage extends AbstractStage<String, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Exception toThrow;

    public FailingLogPassageStage() {
        this(new FailingLogPassageStage.LogPassageStageException("expected log passage exception"));
    }

    public FailingLogPassageStage(Exception toThrow) {
        this.toThrow = toThrow;
    }

    @Override
    public String name() {
        return WidgetStages.LOG_REQUEST;
    }

    @Override
    public String execute(String input) throws Exception {
        logger.info("logging passage");
        throw toThrow;
    }

    static class LogPassageStageException extends Exception {
        public LogPassageStageException (String message) {
            super(message);
        }
    }
}
