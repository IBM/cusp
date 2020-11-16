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

class FailingQueryBackupSystemStage extends AbstractStage<String, Widgets> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Exception toThrow;

    public FailingQueryBackupSystemStage() {
       this(new QPPStageException("querying backup system failed"));
    }

    public FailingQueryBackupSystemStage(Exception toThrow) {
        this.toThrow = toThrow;
    }

    @Override
    public String name() {
        return WidgetStages.QUERY_BACKUP_SYSTEM;
    }

    @Override
    public Widgets execute(String input) throws Exception {
        logger.info("querying backup system");
        Thread.sleep(100);
        throw toThrow;
    }

    static class QPPStageException extends Exception {
        public QPPStageException(String message) {
            super(message);
        }
    }
}
