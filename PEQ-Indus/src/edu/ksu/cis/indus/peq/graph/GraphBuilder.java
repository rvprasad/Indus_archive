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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.peq.constructors.ConstructorContainer;
import edu.ksu.cis.indus.peq.constructors.GeneralConstructor;
import edu.ksu.cis.indus.peq.indusinterface.DependeeTranslator;
import edu.ksu.cis.indus.peq.indusinterface.DependentTranslator;
import edu.ksu.cis.indus.peq.indusinterface.IndusInterface;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import edu.ksu.cis.peq.graph.interfaces.INode;


/**
 * @author ganeshan
 *
 * Create the initial graph nodes.
 */
public class GraphBuilder implements IGraphEngine {

    /**
     * The set of initial Jimple program points.
     * @inv initNodeStatements.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
     */
    private Collection initNodeStatements;
    
    private Map object2NodeMap;
    
    
    /**
     * Initialize the graph with the set of initial node contents.
     * @param stmtColl The set of Jimple program points that act as the initial nodes.
     * @pre stmtColl.oclIsKindOf(Collection(Pair(Stmt,SootMethod)))
     */
    public GraphBuilder(final Collection stmtColl) {
        initNodeStatements = new ArrayList();
        this.initNodeStatements.addAll(stmtColl);
        object2NodeMap = new HashMap();
    }
        
    
    /** 
     * Get the initial nodes.
     * @see edu.ksu.cis.peq.graph.interfaces.IGraphEngine#getInitialNodes()
     */
    public Set getInitialNodes() {
        final Set _collNodes = new HashSet();
        for (Iterator iter = initNodeStatements.iterator(); iter.hasNext();) {
            final Pair _pair = (Pair) iter.next();
            final Node _node = new Node();
            _node.setInformation(_pair);
            _collNodes.add(_node);
            object2NodeMap.put(_pair, _node);
        }
        
        return _collNodes;
    }


    /**
     * Process the edges for the node. Doing it here if more memory efficient.
     * @param node
     * @return
     */
    public Set getOutgoingEdges(INode node) {
        Set _retSet = null;
        if (node.getExitingEdges().size() > 0) {
            _retSet = node.getExitingEdges();
        } else {
            _retSet = new HashSet();
            _retSet.addAll(setupDependeeEdges(node));
            _retSet.addAll(setupDependentEdges(node));
        }
        return _retSet;
    }
    
    /**
     * Setup the dependee edges.
     * @param node The source node.
     */
    private Set setupDependeeEdges(final INode node) {
        IndusInterface _ii = IndusInterface.getInstance();
        final Set _depeSet = new HashSet();
        final DependeeTranslator _dt =  _ii.getDependeeTranslator();
        final Pair informationStmt = (Pair) ((Node) node).getInformation();
        final Node _srcNode = (Node) node;
        // Control        
        Collection _depColl = _dt.getControlInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getCDepD()));
        // Identifier 
        
        _depColl = _dt.getIDataInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getIDef()));
        
        // Divergence
        _depColl = _dt.getDvgInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getDvdd()));
        // Interference        
        _depColl = _dt.getIntfInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getIdd()));
        // Synchronization.
        _depColl = _dt.getSyncInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getSdd()));
        // Ready
        _depColl = _dt.getReadyInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getRdd()));
        // Reference
        _depColl = _dt.getRefDataInfo(informationStmt);
        _depeSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getRdef()));
        
        return _depeSet;
    }
    
    /**
     * Setup the dependent edges.
     * @param edgeSet The set to add the edges to.
     */
    private Set setupDependentEdges(final INode node) {
        IndusInterface _ii = IndusInterface.getInstance();
        final DependentTranslator _dt =  _ii.getDependentTranslator();
        final Pair informationStmt = (Pair) ((Node) node).getInformation();
        final Set _deptSet = new HashSet();
        final Node _srcNode = (Node) node;
        // Control
        Collection _depColl = _dt.getControlInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getCDepT()));
        
        // Identifier 
        _depColl = _dt.getIDataInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getIUse()));
        
        // Divergence
        _depColl = _dt.getDvgInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getDvdt()));
        // Interference        
        _depColl = _dt.getIntfInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getIdt()));
        // Synchronization.
        _depColl = _dt.getSyncInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getSdt()));
        // Ready
        _depColl = _dt.getReadyInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getRdt()));
        // Reference
        _depColl = _dt.getRefDataInfo(informationStmt);
        _deptSet.addAll(process(_srcNode, _depColl, ConstructorContainer.getInstance().getRuse()));
        
        return _deptSet;
    }

       
    
    /**
     * Process the collection of statements to create new edges and child nodes.
     * @param coll
     * @param construcor The constructor that is appropriate for the edge.
     */
    private Set process(final Node mainNode, Collection coll, final GeneralConstructor constructor) {
     final Set _retSet = new HashSet();
     	final Pair informationStmt = (Pair) mainNode.getInformation();
        final SootMethod _masterMethod = (SootMethod) informationStmt.getSecond();        
        for (Iterator _iter = coll.iterator(); _iter.hasNext();) {
            final Object _obj = _iter.next();
            Pair _pair = null;
            
            if (_obj instanceof Stmt) {
                _pair  = new Pair(_obj, _masterMethod);
            } 
            if (_obj instanceof Pair) {                
                _pair = (Pair) _obj;
                // Fix for ready dependence.
                if (_pair.getFirst() == null) {
                    continue;
                }                
            }
            Node _node = null;
            if (object2NodeMap.get(_pair) != null) {
                _node = (Node) object2NodeMap.get(_pair);                
            } else {
                _node = new Node();
                _node.setInformation(_pair);
                object2NodeMap.put(_pair, _node); // Update the node map.
            }
            final Edge _edge = new Edge();            
            _edge.setConstructor(constructor);
            _edge.setSrcNode(mainNode);
            _edge.setDstnNode(_node);
            mainNode.addExitingEdge(_edge);          
            _node.addEnteringEdge(_edge);
            _retSet.add(_edge);
        }
        return _retSet;
       }
}
