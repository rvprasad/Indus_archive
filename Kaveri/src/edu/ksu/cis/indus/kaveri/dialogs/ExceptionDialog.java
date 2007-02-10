/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

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
