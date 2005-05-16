/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
 
package edu.ksu.cis.peq.test.graph;

import java.util.HashSet;
import java.util.Set;


import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import edu.ksu.cis.peq.test.constructors.Constructor1;
import edu.ksu.cis.peq.test.constructors.Constructor2;


/**
 * @author ganeshan
 *
 * Test case where the universal query is not satisfied
 * from node root to node 3 for the pattern "constructor1 constructor2"
 */
public class GraphBuilder$v2 implements IGraphEngine {

    private Set initialNodes;
    
    public GraphBuilder$v2() {
        initialNodes = new HashSet();
    }
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.graph.interfaces.IGraphEngine#getInitialNodes()
     */
    public Set getInitialNodes() {
        createGraph();
        return initialNodes;
    }

    /**          
     * 
     *    Create the graph. 
     */
    private void createGraph() {
        final Node _root = new Node("Root");
        
        Node _node1 = new Node("Node 1");  // *
        Node _node2 = new Node("Node 2");  // *
        						   // *			
        Node _node3 = new Node("Node 3");  // *
       // Node _node4 = new Node("Node 4");  // *
        
        Node _node5 = new Node("Node 5");
        Node _node6 = new Node("Node 6");
        
        
            
        Edge _edge1 = new Edge("Edge 1");
        _edge1.setConstructor(new Constructor1());
        _edge1.setSrcNode(_root);
        _edge1.setDestnNode(_node1);
        
        Edge _edge2 = new Edge("Edge 2");
        _edge2.setConstructor(new Constructor2());
        _edge2.setSrcNode(_root);
        _edge2.setDestnNode(_node2);
        
        _root.addExitingEdge(_edge1);
        _root.addExitingEdge(_edge2);
        
        Edge _edge3 = new Edge("Edge 3");
        _edge3.setConstructor(new Constructor2());
        _edge3.setSrcNode(_node1);
        _edge3.setDestnNode(_node3);
        
        Edge _edge4 = new Edge("Edge 4");
        _edge4.setConstructor(new Constructor1());
        _edge4.setSrcNode(_node1);
        _edge4.setDestnNode(_node3); 
        // rev1: changed from _node4 to _node3
        
        _node1.addEnteringEdge(_edge1);
        _node1.addExitingEdge(_edge3);
        _node1.addExitingEdge(_edge4);
        
        Edge _edge5 = new Edge("Edge 5");
        _edge5.setConstructor(new Constructor2());
        _edge5.setSrcNode(_node2);
        _edge5.setDestnNode(_node5);
        
        Edge _edge6 = new Edge("Edge 6");
        _edge6.setConstructor(new Constructor2());
        _edge6.setSrcNode(_node2);
        _edge6.setDestnNode(_node6);
        
        _node2.addEnteringEdge(_edge2);
        _node2.addExitingEdge(_edge5);
        _node2.addExitingEdge(_edge6);
        
        
        _node3.addEnteringEdge(_edge3);
        _node3.addEnteringEdge(_edge4);
       //_node4.addEnteringEdge(_edge4);
        _node5.addEnteringEdge(_edge5);
        _node6.addEnteringEdge(_edge6);
        
        initialNodes.add(_root);
    }

}
