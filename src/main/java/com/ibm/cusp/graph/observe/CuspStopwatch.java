package com.ibm.cusp.graph.observe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.util.Observable;

public class CuspStopwatch extends Observable implements Closeable {
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
        duration = Duration.ofMillis(System.currentTimeMillis() - startTimeMillis);

        if(countObservers() > 0) {
            logger.trace("Stopped stopwatch {} at {}ns after {}ns", identifier, startTimeMillis, duration.toMillis());

            setChanged();

            logger.trace("Notifying observer");
            notifyObservers();
        } else {
            logger.warn("Stopwatch completed but there are no observers to report to");
            logger.trace("Stopwatch {} stopped after {}ns", identifier, duration.toMillis());
        }
    }
}
