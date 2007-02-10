/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.scoping;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author ganeshan
 * 
 *  
 */
public class ScopePropertiesSelectionDialog extends Dialog {

    private Button btnExclusiveAncs;

    private Button btnExclusiveDcs;

    private Button btnInclusiveAncs;

    private Button btnInclusiveDcs;

    private Button btnIdentity;

    private Label lblWarning;

    private Text txtRegex;

    private Text txtScopeName;

    private String strChoice = "";

    private String strScopeName = "";

    private String strClassRegex = "";

    private String strDefaultScopeName = "";

    private String strDefaultClassName = "";

    private VerifyListener regexVerifier;

    private VerifyListener noRegexVerifier;

    private int dialogType = IScopeDialogMorphConstants.SCOPE_NAME_ONLY;

    /**
     * Constructor
     * 
     * @param shell
     * @param dialogProperties
     *            The type of the dialog
     * @see IScopeDialogMorphConstants
     * @param defaultScopeName
     *            The default scope name.
     */
    public ScopePropertiesSelectionDialog(final Shell shell,
            final int dialogProperties, final String defaultScopeName) {
        super(shell);
        switch (dialogProperties) {
        case IScopeDialogMorphConstants.SCOPE_NAME_ONLY:
        case IScopeDialogMorphConstants.SCOPE_NAME_REGEX:
            dialogType = dialogProperties;
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid parameters to the dialog");
        }
        this.strDefaultScopeName = defaultScopeName;
    }

    /**
     * Configure the title
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Scope Properties");
        super.configureShell(newShell);
    }

    /**
     * Create the Dialog area contents.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = (Composite) super.createDialogArea(parent);
        _comp.setLayout(new GridLayout(1, true));

        switch (dialogType) {
        case IScopeDialogMorphConstants.SCOPE_NAME_ONLY:
            createScopeNameOnly(_comp);
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
     * 
     * @param comp
     *            The parent componenet
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
        regexVerifier = new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                lblWarning.setText("");
                if (!e.text.equals("")) {
                    final String _currString = txtRegex.getText() + e.text;
                    try {
                        Pattern.compile(_currString);
                    } catch (PatternSyntaxException _pe) {
                        lblWarning.setText(e.text
                                + " is not allowed in this context");
                    }

                }
            }
        };

        noRegexVerifier = new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                lblWarning.setText("");
                if (!e.text.equals("")) {
                    final String _curString = txtRegex.getText() + e.text;
                    final Status _status = (Status) JavaConventions
                            .validateJavaTypeName(_curString);
                    final int _sev = _status.getSeverity();
                    if (_sev == IStatus.ERROR || _sev == IStatus.WARNING) {
                        txtRegex.setToolTipText(_status.getMessage());
                        lblWarning.setText(_status.getMessage());
                    } else {
                        e.doit = true;
                    }

                }
            }
        };

        _gd = new GridData();
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan = 1;
        _gd.horizontalAlignment = GridData.FILL;
        txtRegex.setLayoutData(_gd);
        txtRegex.setText(strDefaultClassName);
        lblWarning = new Label(_grp, SWT.LEFT);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan = 1;
        _gd.grabExcessVerticalSpace = true;

        lblWarning.setLayoutData(_gd);
        txtRegex.addVerifyListener(regexVerifier);
    }

    /**
     * Create the scopename and the properties.
     * 
     * @param comp
     */
    private void createScopeNameAndProperties(Composite comp) {
        //createScopeNameOnly(comp);

        final Group _grp = new Group(comp, SWT.BORDER);
        _grp.setText("Scope around the specified type");
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
        btnIdentity.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                lblWarning.setText("");
                txtRegex.removeVerifyListener(noRegexVerifier);
                txtRegex.addVerifyListener(regexVerifier);
            }
        });

        btnExclusiveAncs = new Button(_grp, SWT.RADIO);
        btnExclusiveAncs.setText("Ancestors excluding this class");
        btnExclusiveAncs.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                lblWarning.setText("");
                txtRegex.removeVerifyListener(regexVerifier);
                txtRegex.addVerifyListener(noRegexVerifier);
            }
        });
        btnInclusiveAncs = new Button(_grp, SWT.RADIO);
        btnInclusiveAncs.setText("Ancestors including this class");
        btnInclusiveAncs.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                lblWarning.setText("");
                txtRegex.removeVerifyListener(regexVerifier);
                txtRegex.addVerifyListener(noRegexVerifier);
            }
        });

        btnExclusiveDcs = new Button(_grp, SWT.RADIO);
        btnExclusiveDcs.setText("Descendants excluding this class");
        btnExclusiveDcs.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                lblWarning.setText("");
                txtRegex.removeVerifyListener(regexVerifier);
                txtRegex.addVerifyListener(noRegexVerifier);
            }
        });

        btnInclusiveDcs = new Button(_grp, SWT.RADIO);
        btnInclusiveDcs.setText("Descendants including this class");
        btnInclusiveDcs.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                lblWarning.setText("");
                txtRegex.removeVerifyListener(regexVerifier);
                txtRegex.addVerifyListener(noRegexVerifier);
            }
        });
    }

    /**
     * Create the scope name acceptor.
     * 
     * @param comp
     */
    private void createScopeNameOnly(Composite comp) {
        final Group _grp = new Group(comp, SWT.BORDER);
        _grp.setText("Scope Name");
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        switch (dialogType) {
        case IScopeDialogMorphConstants.SCOPE_NAME_ONLY:
            strScopeName = txtScopeName.getText();
            break;
        case IScopeDialogMorphConstants.SCOPE_NAME_REGEX:
            strScopeName = txtScopeName.getText();
            strClassRegex = txtRegex.getText();
            if (btnExclusiveAncs.getSelection()) {
                final IStatus _status = (Status) JavaConventions
                        .validateJavaTypeName(strClassRegex);
                if (_status.getSeverity() == IStatus.ERROR) {
                    lblWarning.setText(strClassRegex + " is not a FQN");
                    return;
                }
                strChoice = "EXCLUSIVE_ANCESTORS";
            } else if (btnExclusiveDcs.getSelection()) {
                final IStatus _status = JavaConventions
                        .validateJavaTypeName(strClassRegex);
                if (_status.getSeverity() == IStatus.ERROR) {
                    lblWarning.setText(strClassRegex + " is not a FQN");
                    return;
                }
                strChoice = "EXCLUSIVE_DESCENDANTS";
            } else if (btnInclusiveAncs.getSelection()) {
                final IStatus _status = JavaConventions
                        .validateJavaTypeName(strClassRegex);
                if (_status.getSeverity() == IStatus.ERROR) {
                    lblWarning.setText(strClassRegex + " is not a FQN");
                    return;
                }
                strChoice = "INCLUSIVE_ANCESTORS";
            } else if (btnInclusiveDcs.getSelection()) {
                final IStatus _status = JavaConventions
                        .validateJavaTypeName(strClassRegex);
                if (_status.getSeverity() == IStatus.ERROR) {
                    lblWarning.setText(strClassRegex + " is not a FQN");
                    return;
                }
                strChoice = "INCLUSIVE_DESCENDANTS";
            } else if (btnIdentity.getSelection()) {
                try {
                    Pattern.compile(strClassRegex);
                } catch (PatternSyntaxException _pe) {
                    lblWarning
                            .setText(strClassRegex = " is not a valid regular expression");
                    return;
                }
                strChoice = "IDENTITY";
            }
            break;
        }

        super.okPressed();
    }

    /**
     * Returns the choice made by the user.
     * 
     * @return Returns the strChoice.
     */
    public String getStrChoice() {
        return strChoice;
    }

    /**
     * Returns the class regular expression.
     * 
     * @return Returns the strClassRegex.
     */
    public String getStrClassRegex() {
        return strClassRegex;
    }

    /**
     * Returns the Scope name.
     * 
     * @return Returns the strScopeName.
     */
    public String getStrScopeName() {
        return strScopeName;
    }

    /**
     * @param strDefaultClassName
     *            The strDefaultClassName to set.
     */
    public void setStrDefaultClassName(String strDefaultClassName) {
        this.strDefaultClassName = strDefaultClassName;
    }
}
