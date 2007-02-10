/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
/*
 * Created on Jan 3, 2005
 *
 */
package edu.ksu.cis.indus.kaveri.views;

import org.eclipse.core.resources.IFile;

/**
 * 
 * Holds the information for each action in the stack.
 * 
 * @author ganeshan
 * 
 *  
 */
public class DependenceStackData {

    private String className;
    private String methodName;
    private int lineNo;
    private String selectedStatement;
    private IFile javaFile;                

    private String nextElemDepType;


    public DependenceStackData(final String className, final String methodName, final int nLineno, final String selectedStmt,
            final IFile javaFile) {        
        this.className = className;
        this.methodName = methodName;
        this.javaFile = javaFile;
        this.lineNo = nLineno;
        this.selectedStatement = selectedStmt;        
    }

    public boolean equals(Object obj) {
        if (obj instanceof DependenceStackData) {
            final DependenceStackData _cObj = (DependenceStackData) obj;            

            boolean _result = false;
            if (_cObj.getClassName().equals(className)
                    && _cObj.getFile().equals(javaFile)
                    && _cObj.getMethodName().equals(methodName)
                    && _cObj.getStatement().equals(
                            selectedStatement)
                    && _cObj.getLineNo() == lineNo) {
                _result = true;
            }
            return _result;

        } else {
            return super.equals(obj);
        }
    }

    /**
     * @return Returns the file.
     */
    public IFile getFile() {
        return javaFile;
    }

    /**
     * @return Returns the lineNo.
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * @return Returns the statement.
     */
    public String getStatement() {
        return selectedStatement;
    }

    /**
     * @param string
     */
    public void setNextElemDepType(String string) {
        nextElemDepType = string;
    }

    /**
     * @return Returns the nextElemDepType.
     */
    public String getNextElemDepType() {
        return nextElemDepType;
    }
    public String getClassName() {
        return className;
    }
    public String getMethodName() {
        return methodName;
    }
}