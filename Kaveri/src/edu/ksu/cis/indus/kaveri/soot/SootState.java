/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2006 SAnToS Laboratory, Kansas State University
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
