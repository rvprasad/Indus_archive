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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import soot.jimple.Stmt;
import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;

/**
 * This presents a dialog allowing the user to pick a Jimple Stmt.
 * @author Ganeshan
 *
 */
public class StatementResolver extends Dialog {
	/**
	 * The list of jimple statements.
	 */
	private ArrayList stmtList;
	
	/** The SWT list. */
	private List jimpleList;
	
	/**
	 * Constructor.
	 * @param parentShell The parent shell
	 * @param stmtlist The jimple stmt list
	 */
	public  StatementResolver(final Shell parentShell, final ArrayList stmtlist) {
		super(parentShell);
		stmtList = stmtlist;
	}
	
	/**
	 * Configures the shell.
	 * @param newShell The parent shell
	 */
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("StatementResolver.0")); //$NON-NLS-1$
	}
	
	/**
	 * Creates the dialog area .
	 *
	 * @param parent The parent control
	 *
	 * @return Control The new control
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite _composite = new Composite(parent, SWT.NONE);
		final RowLayout _rl = new RowLayout(SWT.HORIZONTAL);		
		_composite.setLayout(_rl);
		final Group _group1 = new Group(_composite, SWT.NONE);
		_group1.setText(Messages.getString("StatementResolver.1")); //$NON-NLS-1$
		final FillLayout _fl = new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL);
		_group1.setLayout(_fl);
		jimpleList = new List(_group1, SWT.BORDER | SWT.V_SCROLL);
		final int _wh = 400;
		jimpleList.setBounds(0, 0, _wh, _wh);
		for (int _i = 0; _i < stmtList.size(); _i++) {
			final Stmt _stmt = (Stmt) stmtList.get(_i);
			jimpleList.add(_stmt.toString());
		}
		return _composite;
	}
	
	
	/**
	 * Ok button pressed.
	 */
	protected void okPressed() {
		final int _index = jimpleList.getSelectionIndex();
		if (_index != -1) {
			final IDialogSettings _settings = SliceEclipsePlugin.getDefault()
					.getDialogSettings();
			_settings.put(Messages.getString("StatementResolver.2"), _index); //$NON-NLS-1$
			super.okPressed();
		}		
	}
}