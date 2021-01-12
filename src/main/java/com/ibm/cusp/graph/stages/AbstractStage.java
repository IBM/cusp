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
import net.jodah.typetools.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public abstract class AbstractStage<S,T> implements Stage<S,T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<S> inputType;
    private final Class<T> outputType;

    protected CuspObserver observer;

    @SuppressWarnings("unchecked")
    public AbstractStage() {
        Class<?>[] typeArguments = TypeResolver.resolveRawArguments(AbstractStage.class, getClass());

        this.inputType = (Class<S>) typeArguments[0];
        this.outputType = (Class<T>) typeArguments[1];

        logger.debug("Stage {} maps {} to {}", getClass(), inputType.getSimpleName(), outputType.getSimpleName());
    }

    /**
     * Register an observer to report information to during stage execution
     * @param observer
     */
    public void registerObserver(CuspObserver observer) {
        this.observer = observer;
    }

    /**
     * Accomplished using typetools package.
     * @return
     * @see <a href="https://github.com/jhalterman/typetools#common-use-cases">TypeTools package</a>.
     */
    @Override
    public Class<S> getInputType() {
        return inputType;
    }

    /**
     * Accomplished using typetools package.
     * @return
     * @see <a href="https://github.com/jhalterman/typetools#common-use-cases">TypeTools package</a>.
     */
    @Override
    public Class<T> getOutputType() {
        return outputType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractStage<?, ?> that = (AbstractStage<?, ?>) o;
        return Objects.equals(inputType, that.inputType) &&
                Objects.equals(outputType, that.outputType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputType, outputType);
    }

    @Override
    public String toString() {
        return name();
    }
}
