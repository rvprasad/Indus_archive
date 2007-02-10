/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.soot;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;

/**
 * This is a data class tracks the state of Soot in Kaveri plugin.
 * 
 * @author Venkatesh Prasad Ranganath
 *
 */
public class SootState implements Observer, IElementChangedListener {
    private boolean sceneNeedsUpdate;

    /**
     * @return Returns the sceneNeedsUpdate.
     */
    public boolean doesSceneNeedUpdate() {
        return sceneNeedsUpdate;
    }

    /**
     * @param b The sceneNeedsUpdate to set.
     */
    public void setSceneNeedsUpdate(final boolean b) {
        this.sceneNeedsUpdate = b;
    }
    
    /**
     * {@inheritDoc}
     */

    public void update(final Observable o, final Object arg) {
        if (arg == EclipseIndusDriver.SOOT_UPDATED) {
            sceneNeedsUpdate = false;
        }        
    }

    public void elementChanged(final ElementChangedEvent event) {
        final Object _source = event.getSource();
        if (_source instanceof IJavaElementDelta) {
            sceneNeedsUpdate |= ((IJavaElementDelta) _source).getElement() instanceof ICompilationUnit;
        }
        if (sceneNeedsUpdate) {
            KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
        }
    }
}
