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
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import edu.ksu.cis.indus.common.datastructures.Triple;

/**
 * @author ganeshan
 *
 * Displays the call string.
 */
public class CallStringDisplayDialog extends Dialog {

    private MethodCallContext ctx;
    
    /**
     * @param parentShell
     */
    public CallStringDisplayDialog(Shell parentShell, final MethodCallContext context) {
        super(parentShell);        
       ctx = context;
    }

    /**
     * configure the shell.
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Call String");
        super.configureShell(newShell);
    }
    
    /**
     * Create the dialog area.
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        GridLayout _layout = new GridLayout(1, true);
		//_layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		//_layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		//_layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		//_layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		_comp.setLayout(_layout);
		_comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Call String between " + ctx.getCallRoot().getElementName()
                + " and " + ctx.getCallSource().getElementName());
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _grp.setLayoutData(_gd);
        _grp.setLayout(new GridLayout(1, true));
        
        final Table _table = new Table(_grp, SWT.SINGLE | SWT.FULL_SELECTION |
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        _table.setLinesVisible(true);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _grp.setLayoutData(_gd);
        _table.setLayoutData(_gd);

        setupTable(_table);
        setupTableEntries(_table);
        
        for (int i = 0; i < _table.getColumnCount(); i++) {
            _table.getColumn(i).pack();
        }
        return _comp;
    }

    /**
     * Setup the table.
     * @param table
     */
    private void setupTable(Table table) {
        table.setHeaderVisible(true);
        final TableColumn _col0 = new TableColumn(table, SWT.NONE);
        _col0.setText("Caller");
        
        final TableColumn _col1 = new TableColumn(table, SWT.NONE);
        _col1.setText("Callee");
        table.setHeaderVisible(true);
    }

    /**
     * Adds the call strings to the table.
     * @param table
     */
    private void setupTableEntries(Table table) {
        final Collection _c = ctx.getContextStacks();
        if (_c.size() > 0) {
            final Stack _stk = (Stack) _c.iterator().next();
            for (Iterator iter = _stk.iterator(); iter.hasNext();) {
                final Triple _triple = (Triple) iter.next();
                final TableItem _item = new TableItem(table, SWT.NONE);
                final MethodWrapper _mw1 = (MethodWrapper) _triple.getFirst();
                final MethodWrapper _mw2 = (MethodWrapper) _triple.getSecond();                
                _item.setText(0, _mw1.getName());
                _item.setText(1, _mw2.getName());
            }
        }
        
    }
}
