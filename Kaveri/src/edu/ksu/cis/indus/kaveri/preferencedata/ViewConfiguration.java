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

import java.util.List;

/**
 * This class holds the list of available views. The view are encapsulated by
 * the ViewData class.
 * 
 * @author Ganeshan
 */
public class ViewConfiguration {
    /**
     * The list of views. list.elements.oclIsKindOf(ViewData)
     */
    private List list;

    /**
     * Sets the list of views.
     * 
     * @param thelist
     *            The list to set.
     */
    public void setList(final List thelist) {
        this.list = thelist;
    }

    /**
     * Get the set of views.
     * 
     * @return List The views.
     */
    public List getList() {
        return list;
    }
}
