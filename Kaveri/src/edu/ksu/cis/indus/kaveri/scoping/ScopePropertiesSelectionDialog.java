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
 
package edu.ksu.cis.indus.kaveri.scoping;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author ganeshan
 *
 * 
 */
public class ScopePropertiesSelectionDialog extends Dialog {
    
    Button btnExclusiveAncs;
    Button btnExclusiveDcs;
    Button btnInclusiveAncs;
    Button btnInclusiveDcs;
    Button btnIdentity;
    
    Text txtRegex;
    Text txtScopeName;
    
    String strChoice = "";
    String strScopeName = "";
    String strClassRegex = "";
    String strDefaultScopeName = "";
    
    
    int dialogType = IScopeDialogMorphConstants.SCOPE_NAME_ONLY;
    
    /**
     * Constructor
     * @param shell
     * @param dialogProperties The type of the dialog @see IScopeDialogMorphConstants
     * @param defaultScopeName The default scope name.     
     */
    public ScopePropertiesSelectionDialog(final Shell shell, final int dialogProperties, final String defaultScopeName) {
        super(shell);
        switch(dialogProperties) {
        	case IScopeDialogMorphConstants.SCOPE_NAME_ONLY:
        	case IScopeDialogMorphConstants.SCOPE_NAME_PROP:
        	case IScopeDialogMorphConstants.SCOPE_NAME_REGEX:
        	    dialogType = dialogProperties;
        	    break;
        	default: throw new IllegalArgumentException("Invalid parameters to the dialog");   
        }        
        this.strDefaultScopeName = defaultScopeName;
    }
    
    
    
    /** Configure the title
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Scope Properties");        
        super.configureShell(newShell);
    }
    /** Create the Dialog area contents.
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = (Composite) super.createDialogArea(parent);
		_comp.setLayout(new GridLayout(1, true));
		
		switch(dialogType) {
			case IScopeDialogMorphConstants.SCOPE_NAME_ONLY:
			    createScopeNameOnly(_comp);
			    break;
			case IScopeDialogMorphConstants.SCOPE_NAME_PROP:
			    createScopeNameOnly(_comp);
			    createScopeNameAndProperties(_comp);
			    break;
			case IScopeDialogMorphConstants.SCOPE_NAME_REGEX:
			    createScopeNameOnly(_comp);
				createScopeNameAndRegexInput(_comp);
				createScopeNameAndProperties(_comp);			    
			    break;
		}
		return _comp;
    }
    
	/**
	 * Create the scope name and regex input type dialog. 
     * @param comp The parent componenet
     */
    private void createScopeNameAndRegexInput(Composite comp) {
       // createScopeNameOnly(comp);
        // Create the regex part.
        final Group _grp = new Group(comp, SWT.BORDER);
		_grp.setText("Enter the expression for the class(es)");
		_grp.setLayout(new GridLayout(1, true));		
		
		GridData _gd = new GridData();
		_gd.grabExcessHorizontalSpace = true;
		_gd.horizontalSpan = 1;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.grabExcessVerticalSpace = true;
		_gd.verticalAlignment = GridData.FILL;
		_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
		
		_grp.setLayoutData(_gd);
		
         txtRegex = new Text(_grp, SWT.LEFT | SWT.BORDER);
         txtRegex.setTextLimit(100);
         _gd = new GridData();
         _gd.grabExcessHorizontalSpace = true;
 		_gd.horizontalSpan = 1;
 		_gd.horizontalAlignment = GridData.FILL;
 		txtRegex.setLayoutData(_gd); 		
    }



    /**
     * Create the scopename and the properties.
     * @param comp
     */
    private void createScopeNameAndProperties(Composite comp) {
        //createScopeNameOnly(comp);
        
        final Group _grp = new Group(comp, SWT.BORDER);
		_grp.setText("Pick the scope around the specified type");
		_grp.setLayout(new RowLayout(SWT.VERTICAL));		
		
		GridData _gd = new GridData();
		_gd.grabExcessHorizontalSpace = true;
		_gd.horizontalSpan = 1;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.grabExcessVerticalSpace = true;
		_gd.verticalAlignment = GridData.FILL;
		_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
		//_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
		_grp.setLayoutData(_gd);
		
		btnIdentity = new Button(_grp, SWT.RADIO);
		btnIdentity.setText("Just this class");
		btnIdentity.setSelection(true);
		btnExclusiveAncs = new Button(_grp, SWT.RADIO);
		btnExclusiveAncs.setText("Exclusive Ancestors");
		btnExclusiveDcs = new Button(_grp, SWT.RADIO);
		btnExclusiveDcs.setText("Exclusive Descendants");
		btnInclusiveAncs = new Button(_grp, SWT.RADIO);
		btnInclusiveAncs.setText("Inclusive Ancestors");
		btnInclusiveDcs = new Button(_grp, SWT.RADIO);
		btnInclusiveDcs.setText("Inclusive Descendants");	        
    }



    
    /**
     * Create the scope name acceptor.
     * @param comp
     */	
    private void createScopeNameOnly(Composite comp) {        
        final Group _grp = new Group(comp, SWT.BORDER);
		_grp.setText("Enter the name for the scope");
		_grp.setLayout(new GridLayout(1, true));		
		
		GridData _gd = new GridData();
		_gd.grabExcessHorizontalSpace = true;
		_gd.horizontalSpan = 1;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.grabExcessVerticalSpace = true;
		_gd.verticalAlignment = GridData.FILL;
		_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
		
		_grp.setLayoutData(_gd);
		
		txtScopeName = new Text(_grp, SWT.LEFT | SWT.BORDER);		
		txtScopeName.setTextLimit(100);
		_gd = new GridData();
		_gd.grabExcessHorizontalSpace = true;
		_gd.horizontalSpan = 1;
		_gd.horizontalAlignment = GridData.FILL;
		txtScopeName.setLayoutData(_gd);
		txtScopeName.setText(strDefaultScopeName);
    }



    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        switch(dialogType) {
        	case IScopeDialogMorphConstants.SCOPE_NAME_ONLY:
        	    strScopeName = txtScopeName.getText();
        	    break;
        	case IScopeDialogMorphConstants.SCOPE_NAME_PROP:
        	    strScopeName = txtScopeName.getText();
        		if (btnExclusiveAncs.getSelection()) {
        		    strChoice = "EXCLUSIVE_ANCESTORS";
        		} else if (btnExclusiveDcs.getSelection()) {
        		    strChoice = "EXCLUSIVE_DESCENDANTS";
        		} else if (btnInclusiveAncs.getSelection()) {
        		    strChoice = "INCLUSIVE_ANCESTORS";
        		} else if (btnInclusiveDcs.getSelection()) {
        		    strChoice = "INCLUSIVE_DESCENDANTS";
        		} else if (btnIdentity.getSelection()) {
        		    strChoice = "IDENTITY";
        		}
        	    break;
        	case IScopeDialogMorphConstants.SCOPE_NAME_REGEX:
        	    strScopeName = txtScopeName.getText();
        		strClassRegex = txtRegex.getText();
        	    break;
        }
               
        super.okPressed();
    }
    
    /**
     * Returns the choice made by the user.
     * @return Returns the strChoice.
     */
    public String getStrChoice() {
        return strChoice;
    }
    /**
     * Returns the class regular expression.
     * @return Returns the strClassRegex.
     */
    public String getStrClassRegex() {
        return strClassRegex;
    }
    /**
     * Returns the Scope name.
     * @return Returns the strScopeName.
     */
    public String getStrScopeName() {
        return strScopeName;
    }
}
