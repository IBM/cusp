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

import com.ibm.cusp.graph.stages.AbstractStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


class LogRequestStage extends AbstractStage<String, Void> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<String> logSink;

    public LogRequestStage(List<String> logSink) {
        this.logSink = logSink;
    }

    @Override
    public String name() {
        return WidgetStages.LOG_REQUEST;
    }

    @Override
    public Void execute(String input) throws Exception {
        logger.info("logging request");
        Thread.sleep(50);
        logSink.add(input);
        return null;
    }
}
