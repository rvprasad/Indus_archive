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
 * Created on Aug 2, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.views;

/**
 * This interface is implemented by any viewers which want to listen to changes
 * in PartialStmtData model.
 * 
 * @author ganeshan
 */
public interface IDeltaListener {
    /**
     * The property has changed. Update the stuff.
     */
    void propertyChanged();

    /**
     * The received is ready to receive the data
     *  
     */
    boolean isReady();
}
