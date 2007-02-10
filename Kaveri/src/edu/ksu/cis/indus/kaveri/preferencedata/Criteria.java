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
 * Created on May 27, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.preferencedata;

/**
 * Encapsulates the chosen criteria.
 * 
 * @author Ganeshan
 */
public class Criteria {
    /**
     * Consider the exceution.
     */
    private boolean bConsiderValue;

    /**
     * The serialized criteria.
     */
    private String criteriaSpec;

    /**
     * The index of the Jimple Stmt.
     */
    private int nJimpleIndex;

    /**
     * The Java line number.
     */
    private int nLineNo;

    /**
     * The class name.
     */
    private String strClassName;

    /**
     * The method name.
     */
    private String strMethodName;

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Criteria) {
            final Criteria _c = (Criteria) obj;
            return (_c.getStrClassName().equals(strClassName) && _c.getStrMethodName().equals(strMethodName)
                    && _c.getNLineNo() == nLineNo && _c.getNJimpleIndex() == nJimpleIndex);
        }
        
        return super.equals(obj);
    }

    /**
     * Returns the serialized criteria string.
     * 
     * @return Returns the criteriaSpec.
     */
    public String getCriteriaSpec() {
        return criteriaSpec;
    }

    /**
     * Get the chosen Jimple index.
     * 
     * @return int Returns the index.
     */
    public int getNJimpleIndex() {
        return nJimpleIndex;
    }

    /**
     * Returns the line number.
     * 
     * @return int The line number.
     */
    public int getNLineNo() {
        return nLineNo;
    }

    /**
     * Returns the class name in which the criteria is present.
     * 
     * @return String The classname
     */
    public String getStrClassName() {
        return strClassName;
    }

    /**
     * Returns the name of the method in which the criteria is present.
     * 
     * @return String The method name.
     */
    public String getStrMethodName() {
        return strMethodName;
    }

    /**
     * Returns whether consider execution is enabled.
     * 
     * @return Returns the bConsiderValue.
     */
    public boolean isBConsiderValue() {
        return bConsiderValue;
    }

    /**
     * Set consider excecution to considerValue.
     * 
     * @param considerValue The bConsiderValue to set.
     */
    public void setBConsiderValue(final boolean considerValue) {
        bConsiderValue = considerValue;
    }

    /**
     * Sets the serialized form of the criteria.
     * 
     * @param cs The criteriaSpec to set.
     */
    public void setCriteriaSpec(String cs) {
        this.criteriaSpec = cs;
    }

    /**
     * Sets the Jimple index.
     * 
     * @param jimpleIndex The nJimpleIndex to set.
     */
    public void setNJimpleIndex(final int jimpleIndex) {
        nJimpleIndex = jimpleIndex;
    }

    /**
     * Sets the line number.
     * 
     * @param lineNo The line number.
     */
    public void setNLineNo(final int lineNo) {
        nLineNo = lineNo;
    }

    /**
     * Sets the classname to strClass.
     * 
     * @param strClass The class name
     */
    public void setStrClassName(final String strClass) {
        this.strClassName = strClass;
    }

    /**
     * Sets the method name to strMethod.
     * 
     * @param strMethod The method name.
     */
    public void setStrMethodName(final String strMethod) {
        this.strMethodName = strMethod;
    }
}
