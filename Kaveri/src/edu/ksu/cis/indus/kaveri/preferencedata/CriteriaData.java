/*
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
    public void setCriterias(final List criteria) {
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