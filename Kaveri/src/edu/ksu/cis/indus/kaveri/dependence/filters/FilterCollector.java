/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
package edu.ksu.cis.indus.kaveri.dependence.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ganeshan
 *
 * Holds the current filter name and the list of all instances.
 */
public class FilterCollector {

    /**
     * The current filter name.
     */
    private String currentFilter;
    
    /**
     * The list of all the filters.
     */
    private List filterList;
    
    /**
     * Constructor.
     *
     */
    public FilterCollector() {
        currentFilter = "";
        filterList = new ArrayList();
    }
    /**
     * @return Returns the currentFilter.
     */
    public String getCurrentFilter() {
        return currentFilter;
    }
    /**
     * @param currentFilter The currentFilter to set.
     */
    public void setCurrentFilter(String currentFilter) {
        this.currentFilter = currentFilter;
    }
    
    /**
     * Adds an element to the filter list.
     * @param instance The filter Instance.
     * 
     */
    public void add(FilterInstance inst) {
        filterList.add(inst);
    }
    /**
     * @return Returns the filterList.
     */
    public List getFilterList() {
        return filterList;
    }
}
