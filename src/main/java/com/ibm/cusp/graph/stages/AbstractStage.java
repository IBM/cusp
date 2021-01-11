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
