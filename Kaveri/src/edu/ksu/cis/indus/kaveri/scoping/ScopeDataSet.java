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
     * @return Returns the classScopeSet.
     */
    public Set getClassScopeSet() {
        return classScopeSet;
    }
    /**
     * Adds the given xml class specification to the class set.
     * @param strClassScope The classScopeSet to set.
     */
    public void addClassScope(String strClassSpec) {
        classScopeSet.add(strClassSpec);
    }
    /**
     * Returns the set of field scope specifications.
     * @return Returns the fieldScopeSet.
     */
    public Set getFieldScopeSet() {
        return fieldScopeSet;
    }
    /**
     * Adds the given field scope specification to the field set.
     * @param fieldScopeSet The fieldScopeSet to set.
     */
    public void addFieldScope(String strFieldScopeSet) {
        fieldScopeSet.add(strFieldScopeSet);
    }
    /**
     * Returns the set of method specifications.
     * @return Returns the methodScopeSet.
     */
    public Set getMethodScopeSet() {
        return methodScopeSet;
    }
    /**
     * Adds the given method scope to the method scope set.
     * @param methodScopeSet The methodScopeSet to set.
     */
    public void addMethodScope(String strMethodScope) {
        methodScopeSet.add(strMethodScope);
    }
}

