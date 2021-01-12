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
package com.ibm.cusp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cusp.graph.stages.AbstractStage;

class FailedManufactureWidgetsStage extends AbstractStage<String, Widgets> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Exception toThrow;

    public FailedManufactureWidgetsStage() {
        this(new WidgetManufactureFailed("widget manufacture failed!"));
    }

    public FailedManufactureWidgetsStage(Exception toThrow) {
        this.toThrow = toThrow;
    }

    @Override
    public String name() {
        return WidgetStages.MANUFACTURE_WIDGETS;
    }

    @Override
    public Widgets execute(String input) throws Exception {
        logger.info("manufacturing widgets");
        throw toThrow;
    }

    static class WidgetManufactureFailed extends Exception {
        public WidgetManufactureFailed(String message) {
            super(message);
        }
    }
}
