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

package edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This dialog is responsible for showing exceptions.
 * @author Ganeshan
 * 
 */
public class ExceptionDialog extends Dialog {
	/**
	 * The exception trace to show.
	 */
    private String exceptionTrace;

    
    

    /**
     * Constructor.
     * @param parentShell The parent shell.
     * @param expTrace The exception trace.
     */
    public ExceptionDialog(final Shell parentShell, final String expTrace) {
        super(parentShell);
        exceptionTrace = expTrace;
    }

    /**
     * Configures the shell.
     * @param newShell The current shell.
     */
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ExceptionDialog.0")); //$NON-NLS-1$
    }

    /**
     * Creates the OK button.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    /**
     * Creates the dialog area .
     * 
     * @param parent
     *            The parent control
     * 
     * @return Control The new control
     */
    protected Control createDialogArea(final Composite parent) {
        final Composite _composite = new Composite(parent, SWT.NONE);
        final RowLayout _rl = new RowLayout(SWT.HORIZONTAL);
        _composite.setLayout(_rl);
        final Group _group1 = new Group(_composite, SWT.NONE);
        _group1.setText(Messages.getString("ExceptionDialog.1")); //$NON-NLS-1$
        final FillLayout _fl = new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL);
        _group1.setLayout(_fl);
        final Text _expList;
        _expList = new Text(_group1, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        final int _wh = 400;
        _expList.setBounds(0, 0, _wh, _wh);
        _expList.setText(exceptionTrace);
        _expList.setEditable(false);

        return _composite;
    }

}