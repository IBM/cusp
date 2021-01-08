package com.ibm.cusp.graph.observe;

import com.ibm.cusp.graph.errors.CuspObservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

public abstract class CuspObserver implements Observer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public abstract void receiveDuration(CuspStopwatch stopwatch);

    @Override
    public void update(Observable observable, Object arg) {
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
