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
