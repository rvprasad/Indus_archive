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

import java.util.Iterator;

import edu.ksu.cis.indus.kaveri.views.IDeltaListener;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

/**
 * @author ganeshan
 *
 * This class add dependence related functionality to PartialStmtData
 */
public class DependenceStmtData extends PartialStmtData {

    /**
     * The jimple index with which to calculate the dependence.
     * A value of -1 indicates that all the statements are to be used.
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
     * Returns the index of the chosen Jimple statement.
     * A value of -1 indicates use all the statements.
     * @return Returns the jimpleIndex.
     */
    public int getJimpleIndex() {
        return jimpleIndex;
    }
}
