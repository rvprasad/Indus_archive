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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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

    /**
     * Constructor.
     * 
     * @param parentShell The parent shell.
     */
    public ContextDialog(final Shell parentShell) {
        super(parentShell);
        callStrings = new HashSet();
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
        _comp.setLayout(new GridLayout(1, true));

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
        viewer.setContentProvider(new ContextContentProvider());
        viewer.setLabelProvider(new ContextLabelProvider());
        viewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getCtxRepository());
        for (int _i = 0; _i < _colNames.length; _i++) {
            _table.getColumn(_i).pack();
        }
        return _comp;
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