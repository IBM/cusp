/**
 * Copyright (c) 2020 International Business Machines
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
