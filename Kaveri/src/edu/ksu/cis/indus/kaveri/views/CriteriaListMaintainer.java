/*
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

/*
 * Created on Aug 2, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;

/**
 * This class maintains the set of partial jimple statements for the chosen java
 * statement. This acts as the domain model for the partial slice view.
 * 
 * @author ganeshan
 */
public class CriteriaListMaintainer {
   
    private IProject project;
    
    private IFile javaFile;
    /**
     * The viewers listening to this model.
     */
    protected List listeners;

    /**
     * Constructor.
     *  
     */
    public CriteriaListMaintainer() {
        listeners = new ArrayList();        
    }

    public boolean isListenersReady() {
        boolean _result = false;
        for (int _i = 0; _i < listeners.size(); _i++) {
            if (((IDeltaListener) listeners.get(_i)).isReady()) {
                _result = true;
                break;
            }
        }
        return _result;
    }

    /**
     * @return Returns the stmtList.
     */
    public IProject getProject() {
        return project;
    }

    /**
     * @param stmtsList
     *            The stmtList to set.
     */
    public void setProjectFile(final IProject project, final IFile file) {        
        if (project != null && file != null) {
            this.project = project;
            this.javaFile = file;
            for (int _i = 0; _i < listeners.size(); _i++) {
                final IDeltaListener _listener = (IDeltaListener) listeners
                        .get(_i);
                if (_listener.isReady()) {
                    ((IDeltaListener) listeners.get(_i)).propertyChanged();
                }
            }
        }
    }

    /**
     * Adds the listener to notify in case of change.
     * 
     * @param listener
     *            The objects interested in viewing the data
     */
    public void addListener(final IDeltaListener listener) {
        listeners.add(listener);
    }

    public boolean isListenersPresent() {
        return listeners.size() > 0;
    }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            The listener to remove. the data
     */
    public void removeListener(final IDeltaListener listener) {
        listeners.remove(listener);
    }

    /**
     * Update the listeners.
     */
    public void update() {
        Display.getDefault().asyncExec(
                new Runnable() {
                    public void run() {
                        for (int _i = 0; _i < listeners.size(); _i++) {
                            final IDeltaListener _listener = (IDeltaListener) listeners
                                    .get(_i);
                            if (_listener.isReady()) {
                                ((IDeltaListener) listeners.get(_i)).propertyChanged();
                            }
                        }
                        
                    }
                }
                );
            
    }

    /**
     * @return Returns the javaFile.
     */
    public IFile getJavaFile() {
        return javaFile;
    }
}