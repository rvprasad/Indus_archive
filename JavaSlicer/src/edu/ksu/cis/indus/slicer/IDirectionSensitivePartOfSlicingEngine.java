package edu.ksu.cis.indus.slicer;

import java.util.Collection;

import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Stmt;


interface IDirectionSensitivePartOfSlicingEngine {

    /**
     * DOCUMENT ME!
     * 
     * @param the_crit
     */
    void initializeCriterion(ISliceCriterion the_crit);
    
    void generateCriteriaToIncludeCallees(final Collection callees, final Stmt stmt, final SootMethod method, final boolean considerReturnValue);

    /**
     * DOCUMENT ME!
     * 
     * @param theBox
     * @param theStmt
     * @param theMethod
     */
    void generatedNewCriteriaForLocal(ValueBox theBox, Stmt theStmt, SootMethod theMethod);

    /**
     * DOCUMENT ME!
     * 
     * @param theBox
     * @param theMethod
     */
    void generateNewCriteriaForParam(ValueBox theBox, SootMethod theMethod);

    /**
     * DOCUMENT ME!
     * 
     * 
     */
    void reset();

}


/*
ChangeLog:

$Log$
*/