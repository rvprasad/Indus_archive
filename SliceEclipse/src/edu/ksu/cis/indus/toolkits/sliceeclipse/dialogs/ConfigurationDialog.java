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

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * This dialog is responsible for the configuration editing.
 * @author Ganeshan
 *
 * 
 */
public class ConfigurationDialog extends Dialog {

	/**
	 * The slice tool instance.
	 */
	private SlicerTool sTool;
	
	/**
	 * Constructor.
	 * @param shell The parent shell.
	 * @param tool The slicer tool.
	 */
	public ConfigurationDialog(final Shell shell, final SlicerTool tool) {
		super(shell);
		sTool = tool;
	}


//	/** Ok button pressed.
//	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
//	 */
//	protected void okPressed() {
//		super.okPressed();
//	}
	
	/** Sets the title.
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(final Shell arg0) {		
		super.configureShell(arg0);
		arg0.setText(Messages.getString("ConfigurationDialog.0")); //$NON-NLS-1$
	}
	
	
	/** Creates the dialog area.
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite _composite = new Composite(parent, SWT.NONE);
		sTool.getConfigurator().initialize(_composite);
		return _composite;
	}
}
