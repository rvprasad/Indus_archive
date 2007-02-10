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

import edu.ksu.cis.indus.kaveri.views.IDeltaListener;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

import java.util.Iterator;

/**
 * @author ganeshan
 * 
 * This class add dependence related functionality to PartialStmtData
 */
public class DependenceStmtData extends PartialStmtData {

    /**
     * The jimple index with which to calculate the dependence. A value of -1
     * indicates that all the statements are to be used.
     */
    private int jimpleIndex = -1;

    public void setupData(final PartialStmtData psd, final int jimpleIndex) {
        this.setClassName(psd.getClassName());
        this.setJavaFile(psd.getJavaFile());
        this.setLineNo(psd.getLineNo());
        this.setMethodName(psd.getMethodName());
        this.setStmtList(psd.getStmtList());
        this.setSelectedStatement(psd.getSelectedStatement());

        this.jimpleIndex = jimpleIndex;

        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            final IDeltaListener _listnr = (IDeltaListener) iter.next();
            _listnr.propertyChanged();

        }

    }

    /**
     * Returns the index of the chosen Jimple statement. A value of -1 indicates
     * use all the statements.
     * 
     * @return Returns the jimpleIndex.
     */
    public int getJimpleIndex() {
        return jimpleIndex;
    }
}
