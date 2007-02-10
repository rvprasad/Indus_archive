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
