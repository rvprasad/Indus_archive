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
 
package edu.ksu.cis.indus.peq.indusinterface;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * @author ganeshan
 *
 * The interface to Indus.
 */
public class IndusInterface {

    /**
     * The slicer tool instance.
     */
    private SlicerTool sTool;
    
    /**
     * The class instance.
     */
    private static IndusInterface iinterface;
    
    private DependeeTranslator dependeeTranslator;
    private DependentTranslator dependentTranslator;
    
    private IndusInterface() {
        dependeeTranslator = new DependeeTranslator();
        dependentTranslator = new DependentTranslator();
    }
    
    
    public void setSlicer(final SlicerTool sTool) {
        this.sTool = sTool;
        dependeeTranslator.setSlicerTool(sTool);
        dependentTranslator.setSlicerTool(sTool);
    }
    
    /**
     * Get a singleton instance.
     * @return IndusInterface The class instance.
     */
    public static IndusInterface getInstance() {
        if (iinterface == null) {
            iinterface = new IndusInterface();
        }
        return iinterface;
    }
    
    /**
     * @return Returns the dependeeTranslator.
     */
    public DependeeTranslator getDependeeTranslator() {
        return dependeeTranslator;
    }
    /**
     * @return Returns the dependentTranslator.
     */
    public DependentTranslator getDependentTranslator() {
        return dependentTranslator;
    }
}
