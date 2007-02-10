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
 * Created on Aug 2, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;

import edu.ksu.cis.indus.kaveri.common.SECommons;

/**
 * This class maintains the set of partial jimple statements for the chosen java
 * statement. This acts as the domain model for the partial slice view.
 * 
 * @author ganeshan
 */
public class PartialStmtData {
    /**
     * The class in which the statement was chosen.
     */
    private String className;

    /**
     * The current Java file.
     * 
     */
    private IFile javaFile;

    /**
     * The line number of the statement.
     */
    private int lineNo;

    /**
     * The viewers listening to this model.
     */
    protected List listeners;

    /**
     * The method in which the statement exists.
     */
    private String methodName;

    /**
     * The selected Java statement
     */
    private String selectedStatement;

    /**
     * The list of Classname, method name and Jimple statements.
     */
    private List stmtList;

    /**
     * Constructor.
     * 
     */
    public PartialStmtData() {
        listeners = new ArrayList();
        stmtList = new ArrayList();
    }

    /**
     * Adds the listener to notify in case of change.
     * 
     * @param listener The objects interested in viewing the data
     */
    public void addListener(final IDeltaListener listener) {
        listeners.add(listener);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        final PartialStmtData _psd = new PartialStmtData();
        _psd.setClassName(className);
        _psd.setJavaFile(javaFile);
        _psd.setLineNo(lineNo);
        _psd.setMethodName(methodName);
        _psd.setSelectedStatement(selectedStatement);
        _psd.setStmtList(stmtList);
        return _psd;
    }

    /**
     * @param _pd
     */
    public void copyContents(PartialStmtData _pd) {
        this.className = _pd.getClassName();
        this.javaFile = _pd.getJavaFile();
        this.lineNo = _pd.getLineNo();
        this.methodName = _pd.getMethodName();
        this.selectedStatement = _pd.getSelectedStatement();
        this.setStmtList(_pd.getStmtList());

    }

    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return Returns the javaFile.
     */
    public IFile getJavaFile() {
        return javaFile;
    }

    /**
     * @return Returns the lineNo.
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * @return Returns the methodName.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return Returns the selectedStatement.
     */
    public String getSelectedStatement() {
        return selectedStatement;
    }

    /**
     * @return Returns the stmtList.
     */
    public List getStmtList() {
        return stmtList;
    }

    public boolean isListenersPresent() {
        return listeners.size() > 0;
    }

    public boolean isListenersReady() {
        boolean _result = false;
        for (int _i = 0; _i < listeners.size(); _i++) {
            if (((IDeltaListener) listeners.get(_i)).isReady()) {
                _result = true;
                break;
            }
        }
        return _result;
    }

    /**
     * Removes the listener.
     * 
     * @param listener The listener to remove. the data
     */
    public void removeListener(final IDeltaListener listener) {
        listeners.remove(listener);
    }

    /**
     * @param cn The className to set.
     */
    public void setClassName(final String cn) {
        this.className = SECommons.normalizeSignature(cn);
    }

    /**
     * @param jf The javaFile to set.
     */
    public void setJavaFile(IFile jf) {
        this.javaFile = jf;
    }

    /**
     * @param ln The lineNo to set.
     */
    public void setLineNo(int ln) {
        this.lineNo = ln;
    }

    /**
     * @param mn The methodName to set.
     */
    public void setMethodName(String mn) {
        this.methodName = SECommons.normalizeSignature(mn);    
    }

    /**
     * @param s The selectedStatement to set.
     */
    public void setSelectedStatement(String s) {
        this.selectedStatement = s;
    }

    /**
     * @param stmtsList The stmtList to set.
     */
    public void setStmtList(final List stmtsList) {
        if (stmtsList != null) {
            this.stmtList = stmtsList;
            for (int _i = 0; _i < listeners.size(); _i++) {
                final IDeltaListener _listener = (IDeltaListener) listeners.get(_i);
                if (_listener.isReady()) {
                    ((IDeltaListener) listeners.get(_i)).propertyChanged();
                }
            }
        }
    }

    /**
     * Update the listeners.
     */
    public void update() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                for (int _i = 0; _i < listeners.size(); _i++) {
                    final IDeltaListener _listener = (IDeltaListener) listeners.get(_i);
                    if (_listener.isReady()) {
                        ((IDeltaListener) listeners.get(_i)).propertyChanged();
                    }
                }

            }
        });

    }

}
