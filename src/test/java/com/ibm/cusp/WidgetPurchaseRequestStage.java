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

public class WidgetPurchaseRequestStage extends AbstractStage<WidgetRequest, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String name() {
        return WidgetStages.RECEIVE_REQUEST;
    }

    @Override
    public String execute(WidgetRequest input) throws Exception {
        logger.info("Receiving widget purchase request");
        return "received " + input;
    }
}
