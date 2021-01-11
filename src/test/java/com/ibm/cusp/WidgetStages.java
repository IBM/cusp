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

import com.ibm.cusp.graph.stages.Stages;

public interface WidgetStages extends Stages {
    String RECEIVE_REQUEST = "receiveRequest";
    String PARSE_REQUEST = "parseRequest";
    String PLACE_ORDER = "placeOrder";
    String SEND_EMAIL = "sendEmail";
    String LOG_REQUEST = "logRequest";
    String QUERY_INVENTORY = "queryInventory";
    String QUERY_BACKUP_SYSTEM = "queryBackupSystem";
    String MANUFACTURE_WIDGETS = "manufactureWidgets";
}
