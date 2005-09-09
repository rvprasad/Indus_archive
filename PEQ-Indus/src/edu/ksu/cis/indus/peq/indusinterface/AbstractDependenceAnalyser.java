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
 
package edu.ksu.cis.indus.peq.indusinterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import soot.SootMethod;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis;
import edu.ksu.cis.indus.tools.slicer.SlicerConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * @author ganeshan
 *
 * Provides basic dependence analysis capabilities.
 */
public class AbstractDependenceAnalyser {
    /** Slicer Tool instance */
    private SlicerTool sTool;

    /**
     * ConstructorAST.
     * @param sTool
     */
    protected AbstractDependenceAnalyser() {
        
    }
    
    protected void setSlicerTool(final SlicerTool sTool) {
        this.sTool = sTool;
    }
    
    /**
     * Get the dependee information.
     * @param stmt
     * @param sm
     * @param depType The dependence type.
     * @return Collection The collection of dependee program points.
     */
    protected Collection getDependeeInfo(final Stmt stmt, final SootMethod sm, final Object depType) {
        if (sTool == null) {
            return Collections.EMPTY_LIST;
        }
        final SlicerConfiguration _sConfig = (SlicerConfiguration) sTool.getCurrentConfiguration();        
        Collection _results = Collections.EMPTY_LIST;
        if (_sConfig != null) {
            final  Collection _depConv =  _sConfig.getDependenceAnalyses(depType);
            if (_depConv != null) {
                _results = new ArrayList();
                for (Iterator _iter = _depConv.iterator(); _iter.hasNext();) {
                    final AbstractDependencyAnalysis _ada = (AbstractDependencyAnalysis) _iter.next();
                    _results.addAll(_ada.getDependees(stmt, sm));                    
                }
            }
        }
        return _results;
    }
    
    
    /**
     * Get the dependent information.
     * @param stmt
     * @param sm
     * @param depType The dependence type.
     * @return Collection The collection of dependee program points.
     */
    protected Collection getDependentInfo(final Stmt stmt, final SootMethod sm, final Object depType) {
        if (sTool == null) {
            return Collections.EMPTY_LIST;
        }
        final SlicerConfiguration _sConfig = (SlicerConfiguration) sTool.getCurrentConfiguration();
        Collection _results = Collections.EMPTY_LIST;
        if (_sConfig != null) {
            final  Collection _depConv =  _sConfig.getDependenceAnalyses(depType);
            if (_depConv != null) {
                _results = new ArrayList();
                for (Iterator _iter = _depConv.iterator(); _iter.hasNext();) {
                    final AbstractDependencyAnalysis _ada = (AbstractDependencyAnalysis) _iter.next();
                    _results.addAll(_ada.getDependents(stmt, sm));                    
                }
            }
        }
        return _results;
    }
}
