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
package com.ibm.cusp.graph.routes;

import com.ibm.cusp.graph.stages.StageOutcomes;
import org.jgrapht.graph.DefaultEdge;

import java.util.Objects;

public class LabeledEdge extends DefaultEdge {
    private final StageOutcomes outcome;
    private final String source;
    private final String target;

    /**
     * An edge whose type is identified by the outcome of its source stage.
     * @param source name of stage whose output to route
     * @param target name of stage to which to route source stage's output
     * @param outcome the type of outcome to require in order to follow this route
     */
    public LabeledEdge(String source, String target, StageOutcomes outcome) {
        this.source = source;
        this.target = target;
        this.outcome = outcome;
    }

    public StageOutcomes getOutcome() {
        return outcome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabeledEdge that = (LabeledEdge) o;
        return outcome == that.outcome &&
                source.equals(that.source) &&
                target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outcome, source, target);
    }
}
