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
package com.ibm.cusp.graph.visualize;

import com.ibm.cusp.graph.Cusp;
import com.ibm.cusp.graph.routes.LabeledEdge;
import com.ibm.cusp.graph.stages.Stage;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.swing.*;

public class CuspVisualizer {
    /**
     * Brings up an interactive GUI that visualizes your stage graph.
     * @param cusp
     */
    public static void visualize(Cusp cusp) {
        JFrame frame = new JFrame("NewGraph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Graph<Stage, LabeledEdge> graph = cusp.getStageGraph();
        JGraphXAdapter<Stage, LabeledEdge> graphAdapter = new JGraphXAdapter<>(graph);

        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        frame.add(new mxGraphComponent(graphAdapter));

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        while(true);
    }
}
