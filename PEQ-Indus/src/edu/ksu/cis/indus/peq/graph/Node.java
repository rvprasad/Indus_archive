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
 
package edu.ksu.cis.indus.peq.graph;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;

import soot.SootMethod;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.peq.graph.interfaces.INode;
/**
 * @author ganeshan
 *
 * This represent a node in the Indus Program Dependency Graph.
 */
public class Node implements INode {

    /**
     * The set of entering edges.
     */
    private Set enteringEdges;
    
    /**
     * The program point corresponding to this node.
     * @inv informationStmt.oclIsKindOf(Pair(Stmt, SootMethod))
     */
    private Pair informationStmt;
        
    
    /**
     * The set of exiting edges.
     */
    private Set exitingEdges;
    
    
    public Node() {
        enteringEdges = new HashSet();
        exitingEdges = new HashSet();
    }
    
    /** 
     * Get the set of entering edges.
     * @see edu.ksu.cis.peq.graph.interfaces.INode#getEnteringEdges()
     */
    public Set getEnteringEdges() {        
       return enteringEdges;
    }

    /** 
     * Get the set of exiting edges.
     * @see edu.ksu.cis.peq.graph.interfaces.INode#getExitingEdges()
     */
    public Set getExitingEdges() {
        return exitingEdges;        
    }
    

    /** 
     * Returns the information associated with the object.
     * @see edu.ksu.cis.peq.graph.interfaces.INode#getInformation()
     */
    public Object getInformation() {
        return informationStmt;
    }    
    
    /**
     * Set the information Jimple statement.
     * @param stmt The statement to set.
     * @param sm The soot method.
     */
    public void setInformation(final Stmt stmt, final SootMethod sm) {
        this.informationStmt = new Pair(stmt, sm);        
    }

    /**
     * Add an entering edge.
     * @param e The edge.
     */
    public void addEnteringEdge(final Edge e) {
        this.enteringEdges.add(e);
    }
    /**
     * Add an exiting edge.
     * @param e The edge.
     */
    public void addExitingEdge(final Edge e) {
        this.exitingEdges.add(e);
    }
    
    /**
     * Set the information.
     * @param pair The pair of Jimple Stmt, SootMethod
     */
    public void setInformation(Pair pair) {
       this.informationStmt = pair;
        
    }
    public String toString() {
        return this.informationStmt.toString();
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof Node)) {
            return false;
        }
        Node rhs = (Node) object;
        return new EqualsBuilder().appendSuper(super.equals(object)).append(
                this.exitingEdges, rhs.exitingEdges).append(
                this.informationStmt, rhs.informationStmt).append(
                this.enteringEdges, rhs.enteringEdges).isEquals();
    }
   
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int _result = 17;
        if (informationStmt != null) {
            _result = 37 * _result + informationStmt.hashCode();
        }
        return _result;
    }
}
