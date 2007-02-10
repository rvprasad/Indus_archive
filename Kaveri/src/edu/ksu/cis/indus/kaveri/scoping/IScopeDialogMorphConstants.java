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

/**
 * @author ganeshan
 * 
 * This interface defines constants that define how the
 * ScopePropertiesSelectionDialog is displayed.
 */
public interface IScopeDialogMorphConstants {
    /**
     * Only the scope name.
     */
    int SCOPE_NAME_ONLY = 1;

    /**
     * Scope name and the regular expression for the entity name
     */
    int SCOPE_NAME_REGEX = 2;
    /**
     * Scope name and class scope properties.
     * 
     * @author ganeshan
     */
    // int SCOPE_NAME_PROP = 3;
}
