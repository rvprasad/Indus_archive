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
 
package edu.ksu.cis.indus.kaveri.dependence;

import org.eclipse.core.resources.IFile;
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

    private IFile file;

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
     * @return Returns the file.
     */
    public IFile getFile() {
        return file;
    }

    /**
     * @param file
     *            The file to set.
     */
    public void setFile(IFile file) {
        this.file = file;
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
     * @param sm The sm to set.
     */
    public void setSm(SootMethod sm) {
        this.sm = sm;
    }
}
