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
 * Created on Apr 5, 2004
 *
 *
 *
 */
package edu.ksu.cis.indus.kaveri.driver;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import soot.G;


import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.tools.IToolProgressListener;

/**
 * This does the bulk of the call to the eclipse indus driver. The settings for
 * the slice are currently stored in IndusConfiguration
 * 
 * @author Ganeshan
 */
public class J2BIndusRunner extends AbstractIndusRunner implements IRunnableWithProgress {
 
    /**
     * Creates a new KaveriIndusRunner object.
     * 
     * @param filesList
     *            The file pointing to the java file being sliced
     * @param bar
     *            The slice progress bar to which to report the messages.
     */
    public J2BIndusRunner(final List filesList, SliceProgressBar bar) {
        super(filesList, bar);
    }

    /**
     * Sets the current editor. Used to show the highlighting in case of
     * backward and forward slicing.
     * 
     * @param ceditor
     *            The editor to set.
     */
    public void setEditor(final CompilationUnitEditor ceditor) {
        this.editor = ceditor;
    }

    /**
     * Sets up the slice parameters.
     * 
     * @return boolean True if the slicer was set up properly.
     */
    public boolean doWork() {
        return setUp();
    }

    /**
     * Runs the slice using the driver.
     * 
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        monitor.beginTask("Running J2B slice", 100); //$NON-NLS-1$
        final Collection _ctx = KaveriPlugin.getDefault()
                .getIndusConfiguration().getChosenContext();
        if (_ctx.size() > 0) {
            processContexts(_ctx);
        }
        String _stag = "EclipseIndusTag";
        _stag = _stag + System.currentTimeMillis();
        final String _oldTag = driver.getNameOfSliceTag();
        driver.setNameOfSliceTag(_stag);
        
        driver.getSlicer().addToolProgressListener(new IToolProgressListener() {
            int _ctr = 1;

            long _currTime;

            String _newMsg = null;
           
            public void toolProgess(final ToolProgressEvent arg0) {
                _ctr++;
                if (!monitor.isCanceled()) {
                    monitor.worked(_ctr);
                    if (_newMsg == null) {
                        _newMsg = arg0.getMsg();
                        _currTime = System.currentTimeMillis();
                    } else {
                        final long _newTimeDelta = System.currentTimeMillis()
                                - _currTime;
                        _currTime = _newTimeDelta + _currTime;
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                bar.addSliceMessage(_newMsg + " Time: "
                                        + _newTimeDelta + " ms");
                            }
                        });
                        _newMsg = arg0.getMsg();
                    }
                    //System.out.println(arg0.getMsg());
                } else {                    
                    opCancelled = true;                                       
                }
            }

        });

        
        driver.execute();
        
        // Residualize the scene
        driver.residualize();
        
        // Cancel pressed.
        if (opCancelled) {              
           throw new InterruptedException("Slice was stopped");
        }
       
          
        returnCriteriaToPool();
        monitor.done();                    
        // Change the tag so that the view remain in a consistent state.
        _stag = "EclipseIndusTag";
        _stag = _stag + System.currentTimeMillis();        
        driver.setNameOfSliceTag(_stag);        
        
        resetSoot();
        
    }

    /**
     * Reset the effects of the slice on the system.
     */
    private void resetSoot() {
        G.reset();
        KaveriPlugin.getDefault().getIndusConfiguration().reset();
        KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
    }

}

