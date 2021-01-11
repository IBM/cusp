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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Observable;

public class CuspStopwatch extends Observable implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final long startTimeMillis;
    private final String identifier;
    private Duration duration;

    public CuspStopwatch(CuspObserver observer, String stopwatchIdentifier) {
        identifier = stopwatchIdentifier;
        startTimeMillis = System.currentTimeMillis();

        if(observer != null) {
            addObserver(observer);
        }

        logger.trace("Started stopwatch {} at {}ns", stopwatchIdentifier, startTimeMillis);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Duration getDuration() {
        return duration;
    }

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
