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
package com.ibm.cusp.graph.conditions;

public enum Conditions {
    QUERY_PARSE_SUCCEEDED,
    QUERY_PARSE_FAILED,
    SEARCH_SUCCEEDED,
    SEARCH_FAILED,
    RANKER_AVAILABLE,
    NO_RANKER_AVAILABLE,
    RERANKING_FAILED,
    RERANKING_SUCCEEDED,
    NATURAL_LANGUAGE_QUERY_PRESENT,
    NO_NATURAL_LANGUAGE_QUERY_PRESENT,
    SPELLING_CORRECTION_REQUESTED
}
