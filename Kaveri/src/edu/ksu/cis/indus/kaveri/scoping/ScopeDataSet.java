/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.scoping;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ganeshan
 * 
 * This is the container class for the scope information.
 */
public class ScopeDataSet {

    /**
     * The set of the class scope definitions.
     */
    private Set classScopeSet;

    /**
     * The set of the method scope definitions.
     */
    private Set methodScopeSet;

    /**
     * The set of the field scope definitions.
     */
    private Set fieldScopeSet;

    /**
     * Constructor.
     *  
     */
    public ScopeDataSet() {
        classScopeSet = new HashSet();
        methodScopeSet = new HashSet();
        fieldScopeSet = new HashSet();
    }

    /**
     * Returns the set of class scope definitions
     * 
     * @return Returns the classScopeSet.
     */
    public Set getClassScopeSet() {
        return classScopeSet;
    }

    /**
     * Adds the given xml class specification to the class set.
     * 
     * @param strClassScope
     *            The classScopeSet to set.
     */
    public void addClassScope(String strClassSpec) {
        classScopeSet.add(strClassSpec);
    }

    /**
     * Returns the set of field scope specifications.
     * 
     * @return Returns the fieldScopeSet.
     */
    public Set getFieldScopeSet() {
        return fieldScopeSet;
    }

    /**
     * Adds the given field scope specification to the field set.
     * 
     * @param fieldScopeSet
     *            The fieldScopeSet to set.
     */
    public void addFieldScope(String strFieldScopeSet) {
        fieldScopeSet.add(strFieldScopeSet);
    }

    /**
     * Returns the set of method specifications.
     * 
     * @return Returns the methodScopeSet.
     */
    public Set getMethodScopeSet() {
        return methodScopeSet;
    }

    /**
     * Adds the given method scope to the method scope set.
     * 
     * @param methodScopeSet
     *            The methodScopeSet to set.
     */
    public void addMethodScope(String strMethodScope) {
        methodScopeSet.add(strMethodScope);
    }
}

