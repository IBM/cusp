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

import com.ibm.cusp.graph.errors.CuspObservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

/**
 * {@code CuspObserver} extends {@link java.util.Observer} for use with {@link com.ibm.cusp.graph.observe.CuspStopwatch}.
 * This is an abstract base class so that the consumer can define what action to take with the reported measurement.
 */
public abstract class CuspObserver implements Observer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Implement this function to handle the measurement reported by {@link com.ibm.cusp.graph.observe.CuspStopwatch}.
     * @param stopwatch
     */
    public abstract void receiveDuration(CuspStopwatch stopwatch);

    /**
     * Attempts to resolve the observable that was notified.
     * @param observable Observable that notified
     * @param arg Currently unused.
     */
    @Override
    public final void update(Observable observable, Object arg) {
        logger.trace("Observable {} signalled", observable.getClass());

        if (observable instanceof CuspStopwatch) {
            CuspStopwatch stopwatch = (CuspStopwatch) observable;
            logger.debug("{} had duration {}", stopwatch.getIdentifier(), stopwatch.getDuration());
            this.receiveDuration(stopwatch);
        } else {
            throw new CuspObservationException("Observed unexpected observable of type {0}", observable.getClass());
        }
    }
}
