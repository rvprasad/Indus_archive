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
