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

import java.util.Collection;

import soot.SootMethod;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * @author ganeshan
 *
 * Manage the dependee translations.
 */
public class DependeeTranslator extends AbstractDependenceAnalyser {

    /**
     * ConstructorAST.
     * @param sTool
     */
    protected DependeeTranslator() {        
    }
    
    protected void setSlicerTool(final SlicerTool sTool) {
        super.setSlicerTool(sTool);
    }
    /**
     * @param informationStmt
     * @return
     */
    public Collection getControlInfo(Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.CONTROL_DA);
    }

    /**
     * @param informationStmt
     * @return
     */
    public Collection getIDataInfo(Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA);
    }

    /**
     * Get reference dependee information.
     * @param informationStmt
     * @return
     */
    public Collection getRefDataInfo(final Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA);
    }
    
    /**
     * @param informationStmt
     * @return
     */
    public Collection getDvgInfo(Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.DIVERGENCE_DA);
    }

    /**
     * @param informationStmt
     * @return
     */
    public Collection getIntfInfo(Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.INTERFERENCE_DA);
    }

    /**
     * @param informationStmt
     * @return
     */
    public Collection getSyncInfo(Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA);
    }

    /**
     * @param informationStmt
     * @return
     */
    public Collection getReadyInfo(Pair informationStmt) {
        final Stmt _stmt = (Stmt) informationStmt.getFirst();
        final SootMethod _sm = (SootMethod) informationStmt.getSecond();
        return super.getDependeeInfo(_stmt, _sm, IDependencyAnalysis.DependenceSort.READY_DA);
    }
}
