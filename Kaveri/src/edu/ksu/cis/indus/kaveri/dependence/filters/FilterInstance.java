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

/**
 * @author ganeshan
 *
 * A filter.
 * 
 */
public class FilterInstance {
    // Name of the filter.
    public String filterName;
   
    /*
     * Should just use a interger and bit wise operators
     * but this is easier to read. 
     */
    
    // Dependee 
    public boolean controlDd;
    public boolean dataDd;
    public boolean intfDd;
    public boolean rdyDd;
    public boolean syncDd;
    public boolean dvgDd;
    
    // Dependent
    public boolean controlDt;
    public boolean dataDt;    
    public boolean intfDt;
    public boolean rdyDt;
    public boolean syncDt;
    public boolean dvgDt;
    
    public FilterInstance() {
        controlDd = dataDd = intfDd = rdyDd = syncDd = dvgDd = true;
        controlDt = dataDt = intfDt = rdyDt = syncDt = dvgDt = true;
    }
}
