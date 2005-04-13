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
 
package edu.ksu.cis.indus.kaveri.driver;

import java.util.ArrayList;
import java.util.Collection;
//import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SliceStatistics implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;        

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (window != null) {
            final Shell _parentShell = window.getShell();
            final String _tag = KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().getNameOfSliceTag();
            if (_tag != null && !_tag.equals("")) {
                final SlicerTool _sTool = KaveriPlugin.getDefault().getSlicerTool();
                if (_sTool.getCurrentConfiguration() != null) {
                final ProcessingController  _pc = new ProcessingController();
                final TagBasedProcessingFilter _filter = new TagBasedProcessingFilter(_tag);
                final MetricsProcessor _mp = new MetricsProcessor();                
                final Collection _processors = new ArrayList();
                _processors.add(_mp);
                _pc.setProcessingFilter(_filter);
                _pc.setEnvironment(KaveriPlugin.getDefault().getSlicerTool().getSystem());
                //_pc.setStmtGraphFactory(KaveriPlugin.getDefault().getSlicerTool().getStmtGraphFactory());                
                _pc.driveProcessors(_processors);
                final Map _map = _mp.getStatistics();
                
                final StatsDisplayDialog _sdd = new StatsDisplayDialog(_parentShell, _map);
                _sdd.open();
                }
            }
            
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

}
