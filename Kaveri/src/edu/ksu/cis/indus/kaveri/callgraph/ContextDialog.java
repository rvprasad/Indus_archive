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

package edu.ksu.cis.indus.kaveri.callgraph;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * @author ganeshan
 * 
 * Display the context dialog.
 */
public class ContextDialog extends Dialog {
    
    /**
     * The viewer instance.
     */
    private CheckboxTableViewer viewer;

    /**
     * The set of MethodContexts.
     */
    private Collection callStrings;

    
    /** The Java project */
    private IJavaProject jProject;
    /**
     * Constructor.
     * 
     * @param parentShell The parent shell.
     */
    public ContextDialog(final Shell parentShell, final IJavaProject project) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        callStrings = new HashSet();
        this.jProject = project;
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(final Shell newShell) {
        newShell.setText("Configure the contexts");
        super.configureShell(newShell);        
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(final Composite parent) {
        
        final Composite _comp = new Composite(parent, SWT.NONE);        
        GridLayout _layout = new GridLayout(1, true);
		_layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		_layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		_layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		_layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		_comp.setLayout(_layout);
		_comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
        final Group _grp = new Group(_comp, SWT.NONE);
        _grp.setText("Select the call string contexts for the slice");
        final GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        _gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _grp.setLayoutData(_gd);
        _grp.setLayout(new FillLayout());

        viewer = CheckboxTableViewer.newCheckList(_grp, SWT.SINGLE
                | SWT.FULL_SELECTION);
        final Table _table = viewer.getTable();
        _table.setHeaderVisible(true);
        _table.setLinesVisible(true);

        final String[] _colNames = { "!", "Call String Source", "Call String End"};
        for (int _i = 0; _i < _colNames.length; _i++) {
            final TableColumn _col = new TableColumn(_table, SWT.NONE);
            _col.setText(_colNames[_i]);
        }
        viewer.setContentProvider(new ContextContentProvider(jProject));
        viewer.setLabelProvider(new ContextLabelProvider());
        viewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getCtxRepository());
        for (int _i = 0; _i < _colNames.length; _i++) {
            _table.getColumn(_i).pack();
        }
        hookContextMenu();
        return _comp;
    }

    /**
     * Add a right click menu to view the call string.
     */
    private void hookContextMenu() {
       final MenuManager _mnuManger = new MenuManager("#PopupMenu");
       _mnuManger.setRemoveAllWhenShown(true);
       _mnuManger.addMenuListener(new IMenuListener() {             
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);               
            }
       	}
               );
       final Menu _mnu = _mnuManger.createContextMenu(viewer.getControl());
       viewer.getControl().setMenu(_mnu);
    }

    /**
     * Create the context menu to view the call string.
     * @param manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        final IAction _action = new Action() {
          public void run() {
              final ISelection _selection = viewer.getSelection();
              if (_selection != null && !_selection.isEmpty() &&
                      _selection instanceof IStructuredSelection) {
                  final IStructuredSelection _ssl = (IStructuredSelection) _selection;
                  final Object _obj = _ssl.getFirstElement();
                  if (_obj instanceof MethodCallContext) {
                      final MethodCallContext _ctx = (MethodCallContext) _obj;
                      CallStringDisplayDialog _csdd = new CallStringDisplayDialog
                      (Display.getCurrent().getActiveShell(), _ctx);
                      _csdd.open();
                  }
              }
          }
        };
        _action.setText("View Callstring");        
        manager.add(_action);
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        final Object[] _elems = viewer.getCheckedElements();
        if (_elems != null && _elems.length > 0) {
            for (int _i = 0; _i < _elems.length; _i++) {
                callStrings.add(_elems[_i]);
            }
        }
        super.okPressed();
    }

    /**
     * Returns the set of selected call strings.
     * 
     * @return Returns the callStrings.
     */
    public Collection getCallStrings() {
        return callStrings;
    }
}