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
 
package edu.ksu.cis.indus.kaveri.peq;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.ksu.cis.indus.kaveri.ResourceManager;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * @author ganeshan
 *
 * Dialog that allows the query to be entered.
 */
public class QueryEntryDialog extends Dialog {

    private Text queryEditor;
    private String queryString;
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Query Editor");
        super.configureShell(newShell);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		_comp.setLayout(layout);
		_comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(_comp);
		queryEditor = new Text(_comp, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData _gd = new GridData(GridData.FILL_BOTH);
		_gd.horizontalSpan = 1;
		_gd.grabExcessHorizontalSpace = true;
		_gd.grabExcessVerticalSpace = true;
		_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
		_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
		queryEditor.setLayoutData(_gd);
		final ResourceManager _rm = KaveriPlugin.getDefault().getIndusConfiguration().getRManager();
		queryEditor.setForeground(_rm.getColor(new RGB(255, 0, 0)));
		queryEditor.setText("query default {<> <> }; ");
		return _comp;
    }
    /**
     * @param parentShell
     */
    protected QueryEntryDialog(Shell parentShell) {
        super(parentShell);       
    }

    /**
     * @return Returns the queryString.
     */
    public String getQueryString() {
        return queryString;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        final String _lineDelim = queryEditor.getLineDelimiter();
        queryString = queryEditor.getText().replaceAll(_lineDelim, "");
        super.okPressed();
    }
}
