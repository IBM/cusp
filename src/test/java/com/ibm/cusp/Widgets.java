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


public class Widgets {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String results;

    public Widgets(String results) {
        logger.debug("Created Widgets object: {}", results);
        this.results = results;
    }

    @Override
    public String toString() {
        return "procured " + results;
    }
}
