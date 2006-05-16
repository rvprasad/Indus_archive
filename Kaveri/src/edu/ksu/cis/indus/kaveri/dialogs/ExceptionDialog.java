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

package edu.ksu.cis.indus.kaveri.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This dialog is responsible for showing the exception trace.
 * 
 * @author Ganeshan
 */
public class ExceptionDialog
		extends Dialog {

	/**
	 * The exception trace to show.
	 */
	private String exceptionTrace;

	/**
	 * Constructor.
	 * 
	 * @param parentShell The parent shell.
	 * @param expTrace The exception trace.
	 */
	public ExceptionDialog(final Shell parentShell, final String expTrace) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);
		exceptionTrace = expTrace;
	}

	/**
	 * Configures the shell.
	 * 
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
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * Creates the dialog area.
	 * 
	 * @param parent The parent control
	 * @return Control The new control
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite _composite = (Composite) super.createDialogArea(parent);
		final Group _g = new Group(_composite, SWT.SHADOW_NONE);
		_g.setLayout(new GridLayout());
		_g.setLayoutData(new GridData(GridData.FILL_BOTH));
		_g.setText(Messages.getString("ExceptionDialog.1")); //$NON-NLS-1$
		final Text _expList = new Text(_g, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		_expList.setLayoutData(new GridData(GridData.FILL_BOTH));
		_expList.setEditable(false);
		_expList.append(exceptionTrace);
		_expList.getVerticalBar().setSelection(_expList.getVerticalBar().getIncrement());
		return _composite;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override protected Point getInitialSize() {
		// TODO: Auto-generated method stub
		return new Point(600, 400);
	}

}
