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
 * Created on May 25, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.preferencedata;

import java.util.List;

/**
 * This class holds the list of criteria for a project.
 * 
 * @author Ganeshan
 */
public class CriteriaData {
    /**
     * The list of criteria.
     */
    private List criterias;

    /**
     * Sets the criteria.
     * 
     * @param criteria
     *            The criterias to set.
     */
    public void setCriteria(final List criteria) {
        this.criterias = criteria;
    }

    /**
     * Returns the list of criteria. Postcondition: result.oclIsKindOf(List) and
     * result.elements.oclIsKindOf(Criteria)
     * 
     * @return List The list of criteria.
     */
    public List getCriterias() {
        return criterias;
    }
}
