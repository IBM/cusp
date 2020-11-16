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
