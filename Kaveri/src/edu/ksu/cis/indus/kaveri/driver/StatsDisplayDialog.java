/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
package edu.ksu.cis.indus.kaveri.driver;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

//import edu.ksu.cis.indus.common.soot.MetricsProcessor;

/**
 * @author ganeshan
 *
 * Displays the statistics for the dialog.
 */
public class StatsDisplayDialog extends Dialog {

    private List statsDisplay;
    
    private Map statValues;
    /**
     * @param parentShell
     */
    protected StatsDisplayDialog(Shell parentShell, final Map entry) {
        super(parentShell);
        this.statValues = entry;
    }

   
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Slice Statistics");
        super.configureShell(newShell);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        _comp.setLayout(new GridLayout(1, false));
        
        statsDisplay = new List(_comp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        _gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        statsDisplay.setLayoutData(_gd);
        initializeList();
        return _comp;
    }



    /**
     * Initialize the list.
     */
    private void initializeList() {
        final Set _entries = statValues.entrySet();
        for (Iterator iter = _entries.iterator(); iter.hasNext();) {
            final Map.Entry _entry = (Entry) iter.next();            
            statsDisplay.add(_entry.getKey().toString());
            statsDisplay.add("-----------------------");
            final Map _resultMap = (Map) _entry.getValue();
            final Set _resultEntries = _resultMap.entrySet();
            for (Iterator iterator = _resultEntries.iterator(); iterator
                    .hasNext();) {
                final Map.Entry _rEntry = (Entry) iterator.next();
                statsDisplay.add(_rEntry.getKey() + " : " + _rEntry.getValue());
                
            }
            statsDisplay.add(" ");
        }
        
    }
}
