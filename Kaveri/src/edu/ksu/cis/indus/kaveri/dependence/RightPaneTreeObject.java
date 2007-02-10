/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.dependence;

import org.eclipse.core.runtime.IAdaptable;

import soot.SootMethod;

/**
 * @author ganeshan
 * 
 * The right pane tree object.
 */
public class RightPaneTreeObject implements IAdaptable {
    private String name;

    private RightPaneTreeParent parent;

    private String statement;

    /*
     * The soot method this object represents.
     */
    private SootMethod sm;

    private int lineNumber;

    public RightPaneTreeObject(String stmt) {
        this.statement = stmt;
    }

    public void setParent(RightPaneTreeParent parent) {
        this.parent = parent;
    }

    public RightPaneTreeParent getParent() {
        return parent;
    }

    public String toString() {
        return getStatement();
    }

    public Object getAdapter(Class key) {
        return null;
    }

    /**
     * @return Returns the lineNumber.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @param lineNumber
     *            The lineNumber to set.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * @return Returns the statement.
     */
    public String getStatement() {
        return statement;
    }

    /**
     * @return Returns the sm.
     */
    public SootMethod getSm() {
        return sm;
    }

    /**
     * @param sm
     *            The sm to set.
     */
    public void setSm(SootMethod sm) {
        this.sm = sm;
    }
}
