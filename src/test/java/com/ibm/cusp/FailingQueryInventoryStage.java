/**
 * BEGIN_COPYRIGHT
 *
 * IBM Confidential
 * OCO Source Materials
 *
 * 5727-I17
 * (C) Copyright IBM Corp. 2020 All Rights Reserved.
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

class FailingQueryInventoryStage extends AbstractStage<String, Widgets> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Exception toThrow;

    public FailingQueryInventoryStage() {
        this(new InventoryQueryException("query inventory failed"));
    }

    public FailingQueryInventoryStage(Exception toThrow) {
        this.toThrow = toThrow;
    }

    @Override
    public String name() {
        return WidgetStages.QUERY_INVENTORY;
    }

    @Override
    public Widgets execute(String input) throws Exception {
        logger.info("querying inventory failed");
        Thread.sleep(100);
        throw toThrow;
    }

    static class InventoryQueryException extends Exception {
        public InventoryQueryException(String message) {
            super(message);
        }
    }
}
