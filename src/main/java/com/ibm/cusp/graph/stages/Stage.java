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
package com.ibm.cusp.graph.stages;

import com.ibm.cusp.graph.observe.CuspObserver;

public interface Stage<S,T> {
    /**
     * A unique identifier for this stage.
     * @return
     */
    String name();

    /**
     * The input data type that this stage accepts. See {@link AbstractStage} for implementation.
     * @return
     */
    Class<S> getInputType();

    /**
     * The output data type that this stage emits. See {@link AbstractStage} for implementation.
     * @return
     */
    Class<T> getOutputType();

    /**
     * The implementation of this stage.
     * @return
     */
    T execute(S input) throws Exception;


    void registerObserver(CuspObserver observer);
}
