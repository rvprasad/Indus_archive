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
 
package edu.ksu.cis.indus.kaveri.rootmethodtrapper;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

/**
 * @author ganeshan
 *
 * Displays the additonal root methods for the project.
 */
public class RootMethodDialog extends Dialog {
    
    /**
     * Comment for <code>tvLeft</code>
     */
    private CheckboxTableViewer viewer;
    
    /**
     * Comment for <code>rmColl</code>
     */
    private RootMethodCollection rmColl;
    
    /**
     * Comment for <code>jProject</code>
     */
    private IJavaProject jProject;
    
    /**
     * The collection of deleted root methods.
     */
    private Collection deleteCollection;

    /**
     * @param parentShell
     */
    /**
     * @param parentShell
     * @param project
     */
    public RootMethodDialog(final Shell parentShell, final IJavaProject project) {
        super(parentShell);        
        this.jProject = project;
        deleteCollection = new ArrayList();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Root Method Viewer");
        super.configureShell(newShell);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayout(new GridLayout(1, false));
        GridData _gd = new GridData(GridData.FILL_BOTH);        
        _gd.horizontalSpan = 1;
        _comp.setLayoutData(_gd);
        
        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Additional root methods");
        
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;        
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan  = 1;
        _gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        _gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        _grp.setLayoutData(_gd);        
        _grp.setLayout(new GridLayout(1, true));
        
        
        viewer = CheckboxTableViewer.newCheckList(_grp, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        final Table _table = viewer.getTable();
        setupTable(_table);      
        initRootMethods();
        viewer.setContentProvider(new RootMethodContentProvider());
        viewer.setLabelProvider(new RootMethodLabelProvider());
        viewer.setInput(rmColl);
        for (int i = 0; i < _table.getColumnCount(); i++) {
            _table.getColumn(i).pack();
        }
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan  = 1;
        _table.setLayoutData(_gd);
        
        final Composite _rComp = new Composite(_comp, SWT.BORDER);
        
        final RowLayout _rl = new RowLayout();
        _rl.pack = false;
        _rComp.setLayout(_rl);
        
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan =  1;
        _rComp.setLayoutData(_gd);

        final Button _btnDelete =  new Button(_rComp, SWT.PUSH);
        _btnDelete.setText("Delete");
     
        _btnDelete.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {                        

                            final Object[] _chsObjs = viewer.getCheckedElements();

                            for (int i = 0; _chsObjs != null && i < _chsObjs.length; i++) {
                                deleteCollection.add(_chsObjs[i]);
                                rmColl.getRootMethodCollection().remove(_chsObjs[i]);                                
                             }
                            
                            viewer.setInput(rmColl);
                            for (int i = 0; i < _table.getColumnCount(); i++) {
                                _table.getColumn(i).pack();
                            }
                            
                        } 
                                            
                }
                );
        
        return _comp;
    }    


    /**
     * Initialize the root method object.
     */
    private void initRootMethods() {
        if (jProject != null) {
            final IResource _resource;
            try {
                _resource = jProject.getCorrespondingResource();
            
            final QualifiedName _name = new QualifiedName("edu.ksu.cis.indus.kaveri", "rootMethodCollection");
            final String _propVal =   _resource.getPersistentProperty(_name);
            final XStream _xstream = new XStream(new DomDriver());
            _xstream.alias("RootMethodCollection", RootMethodCollection.class);            
            if (_propVal != null) {
               rmColl = (RootMethodCollection) _xstream.fromXML(_propVal);
            } else {
                rmColl = new RootMethodCollection();
            }
            final String _val = _xstream.toXML(rmColl);
           
                _resource.setPersistentProperty(_name, _val);
            } catch (JavaModelException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Java Model Exception", _e);
            } 
            catch (CoreException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Core Exception", _e);            
            } 
        }
        
    }

    /**
     * Setup the table.
     * @param table
     */
    private void setupTable(Table table) {
        final TableColumn _col0 = new TableColumn(table, SWT.CENTER);
        _col0.setText("!");
        
        final TableColumn _col1 = new TableColumn(table, SWT.NONE);
        _col1.setText("Class");
        
        final TableColumn _col2 = new TableColumn(table, SWT.NONE);
        _col2.setText("Method Signature");
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;        
        table.setLayoutData(_gd);
        
    }

    /**
     * Save the results.
     */
    protected void okPressed() {
        if (rmColl != null) {
            final XStream _xstream = new XStream(new DomDriver());
            _xstream.alias("RootMethodCollection", RootMethodCollection.class);        
            final QualifiedName _name = new QualifiedName("edu.ksu.cis.indus.kaveri", "rootMethodCollection");
            final String _val = _xstream.toXML(rmColl);
            try {
                final IResource _rs = jProject.getCorrespondingResource();
                _rs.setPersistentProperty(_name, _val);
            }  
            catch (JavaModelException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Java Model Exception", _e);
            } 
            catch (CoreException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Core Exception", _e);            
            } 
            
        }
        deleteMarkers();
        
        super.okPressed();
    }

    /**
     * Delete any markers corresponding to the deleted elements.
     */
    private void deleteMarkers() {
        final String _markerId = KaveriPlugin.getDefault().getBundle().getSymbolicName() + "." +
    	"rootMethodMarker";                        
        try {
            final IMarker[] _markers = jProject.getProject().findMarkers(_markerId, true, IResource.DEPTH_INFINITE);
            final String _classNameKey = "className";
            final String _methodSigKey = "methodSignature";
            final Collection _markersToDelete = new ArrayList();
            for (Iterator iter = deleteCollection.iterator(); iter.hasNext();) {
                final Pair _pair = (Pair) iter.next();
                for (int j = 0; j < _markers.length; j++) {
                    final IMarker _marker = _markers[j];
                    final String _classname = (String) _marker.getAttribute(_classNameKey);
                    final String _methodNameSig = (String) _marker.getAttribute(_methodSigKey);
                    if (_classname != null && _methodSigKey  != null &&
                            _classname.equals(_pair.getFirst().toString()) &&
                                    _methodNameSig.equals(_pair.getSecond().toString())) {
                        _markersToDelete.add(_marker);
                    }    
            }
            
                for (Iterator iterator = _markersToDelete.iterator(); iterator
                .hasNext();) {
                    final IMarker _marker = (IMarker) iterator.next();
                    _marker.delete();                                
                }

            }
        } catch (CoreException e) {
            SECommons.handleException(e);
            KaveriErrorLog.logException("Unable to find markers", e);
        }
        
        
    }
}




