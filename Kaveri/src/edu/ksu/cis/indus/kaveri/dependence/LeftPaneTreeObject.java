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


/**
 * 
 * @author ganeshan
 *
 * 
 */
public class LeftPaneTreeObject implements IAdaptable {    

    private LeftPaneTreeParent parent;

    private String statement;

    private IFile file;

    private int lineNumber;

    /*
     * The index into the set of jimple statements represented by 
     * this object. -1 for the root.
     */
    private int jimpleIndex;
    

    public LeftPaneTreeObject(String stmt) {
        this.statement = stmt;        
    }

    public void setParent(LeftPaneTreeParent parent) {
        this.parent = parent;
    }

    public LeftPaneTreeParent getParent() {
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
     * @return Returns the jimpleIndex.
     */
    public int getJimpleIndex() {
        return jimpleIndex;
    }
    /**
     * @param jimpleIndex The jimpleIndex to set.
     */
    public void setJimpleIndex(int jimpleIndex) {
        this.jimpleIndex = jimpleIndex;
    }
}