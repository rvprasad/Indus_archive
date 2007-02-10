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
     * The index into the set of jimple statements represented by this object.
     * -1 for the root.
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
     * @param jimpleIndex
     *            The jimpleIndex to set.
     */
    public void setJimpleIndex(int jimpleIndex) {
        this.jimpleIndex = jimpleIndex;
    }
}
