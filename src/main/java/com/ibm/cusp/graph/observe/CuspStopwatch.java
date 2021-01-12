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
package com.ibm.cusp.graph.observe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Observable;

public class CuspStopwatch extends Observable implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final long startTimeMillis;
    private final String identifier;
    private Duration duration;

    /**
     * {@code CuspStopwatch} provides a mechanism for reporting execution times (in ms) of arbitrary code blocks, meant to be
     * used with try-with-resources.
     *
     * @param observer {@code CuspObserver} to notify
     * @param stopwatchIdentifier
     * @see com.ibm.cusp.graph.observe.CuspObserver
     */
    public CuspStopwatch(CuspObserver observer, String stopwatchIdentifier) {
        identifier = stopwatchIdentifier;
        startTimeMillis = System.currentTimeMillis();

        if(observer != null) {
            addObserver(observer);
        }

        logger.trace("Started stopwatch {} at {}ms", stopwatchIdentifier, startTimeMillis);
    }

    /**
     * Returns user-defined string identifier for indicating what is measured.
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns measured execution time.
     * @return duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Stops timer and reports duration to observer, or logs a warning if no observer has been registered.
     */
    @Override
    public void close() {
        long endTimeMillis = System.currentTimeMillis();
        duration = Duration.ofMillis(endTimeMillis - startTimeMillis);

        if(countObservers() > 0) {
            logger.trace("Stopped stopwatch {} at {}ms after {}ms", identifier, endTimeMillis, duration.toMillis());

            setChanged();

            logger.trace("Notifying observer");
            notifyObservers();
        } else {
            logger.warn("Stopwatch completed but there are no observers to report to");
            logger.trace("Stopwatch {} stopped after {}ms", identifier, duration.toMillis());
        }
    }
}
