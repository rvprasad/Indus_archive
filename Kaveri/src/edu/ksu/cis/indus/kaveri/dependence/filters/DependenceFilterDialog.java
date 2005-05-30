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
 
package edu.ksu.cis.indus.kaveri.dependence.filters;

import com.thoughtworks.xstream.XStream;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


/**
 * @author ganeshan
 *
 * Displays the filter dialog to select/modify filters for 
 * the dependence tracking view.
 */
public class DependenceFilterDialog extends Dialog {

    
    public static final int ADD_FILTER = 1337;
    
    private Combo filterNameCombo;
    
    private FilterCollector fc;
    // Checkboxes
    private Button chkControlDd, chkdataDd,  chkIntDd, chkSynDd, chkRdyDd, chkDvgDd;
    private Button chkControlDt, chkdataDt, chkIntDt, chkSynDt, chkRdyDt, chkDvgDt;
    
    private int selectedIndex;
    /**
     * Constructor.
     * @param parentShell
     */
    public DependenceFilterDialog(Shell parentShell) {
        super(parentShell);        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Filter Configuration");
        super.configureShell(newShell);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		_comp.setLayout(layout);
		_comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(_comp);
		
		// Create combo box.
		final Label _lblName = new Label(_comp, SWT.LEFT);
		_lblName.setText("Current Filter");
		GridData _gd = new GridData();
		_gd.horizontalSpan = 1;
		_lblName.setLayoutData(_gd);
		
		filterNameCombo = new Combo(_comp, SWT.DROP_DOWN);
		_gd = new GridData();
		_gd.horizontalSpan = 1;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.grabExcessHorizontalSpace = true;
		filterNameCombo.setLayoutData(_gd);
		filterNameCombo.setItems(new String[0]);
		
		filterNameCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(final SelectionEvent evt) {
			    saveChanges(selectedIndex);
			    recordSelection();			    
			    updateView();
			}            
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }
		});
		
		filterNameCombo.addFocusListener(new FocusAdapter() {
		    public void focusLost(final FocusEvent evt) {
				updateFilterName();
			}

			public void focusGained(final FocusEvent evt) {
				recordSelection();
			}
			});
		
		// Create the checkboxes
		final Group _grpDd = new Group(_comp, SWT.NONE);
		_grpDd.setText("Dependee");
		_gd = new GridData(GridData.FILL_BOTH);
		_gd.horizontalSpan = 1;
		_gd.grabExcessHorizontalSpace = true;
		_gd.grabExcessVerticalSpace = true;
		_grpDd.setLayoutData(_gd);
		_grpDd.setLayout(new RowLayout(SWT.VERTICAL));
		addDependeeBoxes(_grpDd);
		
		final Group _grpDt = new Group(_comp, SWT.NONE);
		_grpDt.setText("Dependent");
		_gd = new GridData(GridData.FILL_BOTH);
		_gd.horizontalSpan = 1;
		_gd.grabExcessHorizontalSpace = true;
		_gd.grabExcessVerticalSpace = true;
		_grpDt.setLayoutData(_gd);
		_grpDt.setLayout(new RowLayout(SWT.VERTICAL));
		addDependentBoxes(_grpDt);
		initialize();
		if (fc.getFilterList().size() == 0) {
		    setCheckboxStatus(false);
		}
        return _comp;
    }
    

    /**
     * Set the enablement status of the checkboxes.
     * @param enb The enablement state.
     */
    private void setCheckboxStatus(boolean enb) {
        // Dependee
        chkControlDd.setEnabled(enb);
        chkdataDd.setEnabled(enb);        
        chkSynDd.setEnabled(enb);
        chkIntDd.setEnabled(enb);
        chkDvgDd.setEnabled(enb);
        chkRdyDd.setEnabled(enb);
        
        // Dependent
        chkControlDt.setEnabled(enb);
        chkdataDt.setEnabled(enb);        
        chkSynDt.setEnabled(enb);
        chkIntDt.setEnabled(enb);
        chkDvgDt.setEnabled(enb);
        chkRdyDt.setEnabled(enb);
        
    }

    /** 
     * Save all the changes.
     */
    protected void saveChanges(int index) {
        if (index != -1) {
            FilterInstance _inst = (FilterInstance) fc.getFilterList().get(index);
            // Dependee update.
            _inst.controlDd = chkControlDd.getSelection();
            _inst.dataDd = chkdataDd.getSelection();
            _inst.dvgDd = chkDvgDd.getSelection();

            _inst.intfDd = chkIntDd.getSelection();
            _inst.syncDd = chkSynDd.getSelection();
            _inst.rdyDd = chkRdyDd.getSelection();
            // Depedent udpate.
            _inst.controlDt = chkControlDt.getSelection();
            _inst.dataDt = chkdataDt.getSelection();
            _inst.dvgDt = chkDvgDt.getSelection();            
            _inst.intfDt = chkIntDt.getSelection();
            _inst.syncDt = chkSynDt.getSelection();
            _inst.rdyDt = chkRdyDt.getSelection();
        }
        
    }

    /**
     * Update the filter.
     */
    protected void updateFilterName() {
        final int _selIndex = filterNameCombo.getSelectionIndex();

        
		// if text was changed, selection index is -1 when the name is changed.
		if (_selIndex < 0 && selectedIndex >=0 && fc.getFilterList().size() > 0) {
			// retrive the text at previously selected index and the corresponding configuration
			
			final FilterInstance _inst = (FilterInstance) fc.getFilterList().get(selectedIndex);

			// retrieve the new Text in the text box.
			final String _newText = filterNameCombo.getText();
			boolean _noDuplicate = true;

			// check if the new name will lead to duplicate entries.
			final List filterList = fc.getFilterList();
			for (int _i = filterNameCombo.getItemCount() - 1; _i >= 0 && _noDuplicate; _i--) {
				if (((FilterInstance) filterList.get(_i)).filterName.equals(_newText)) {
					final MessageBox _msgBox = new MessageBox(getParentShell(), SWT.OK | SWT.ICON_INFORMATION);
					_msgBox.setMessage("A filter with the name of \"" + _newText
						+ "\" exists.  \nNo changes will be made.");
					_noDuplicate = false;
				}
			}

			// if there will be no duplicate entries, then...
			if (_noDuplicate) {				
				filterNameCombo.remove(selectedIndex);
				filterNameCombo.add(_newText, selectedIndex);
				_inst.filterName = _newText;
			}
		}
        
    }

    /**
     * Update the current view.
     */
    protected void updateView() {
        int _index = filterNameCombo.getSelectionIndex();
        if (_index != -1 && fc.getFilterList().size() > 0) {
            final FilterInstance _inst = (FilterInstance) fc.getFilterList().get(_index);
            updateDialog(_inst);
        }
        
    }

    /**
     * Record the selected event
     */
    protected void recordSelection() {
        selectedIndex = filterNameCombo.getSelectionIndex();
        
    }

    /**
     * Setup the filters from storage. 
     */
    private void initialize() {
        final String _filterKey = "edu.ksu.cis.indus.kaveri.depview.filter.key";
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        final String _val = _ps.getString(_filterKey);
        final XStream _stream = new XStream();
        if (!_val.equals("")) {
         final FilterCollector _fc = (FilterCollector) _stream.fromXML(_val);
         final List _filterList = _fc.getFilterList();
         fc = _fc;
         int _selectIndex = -1;
         final String _currFilter = _fc.getCurrentFilter();
         final List filterList = fc.getFilterList();
         for (int _i = 0; _i < filterList.size(); _i++) {
             final FilterInstance _inst = (FilterInstance) filterList.get(_i);
             if (_inst.filterName.equals(_currFilter)) {
                 _selectIndex = _i;
             }
            filterNameCombo.add(_inst.filterName);            
         }         
         if (_filterList.size() > 0) {             
             if (_fc.getCurrentFilter().equals("")) {
                 filterNameCombo.select(0);
                 updateDialog((FilterInstance)_filterList.get(0));
             } else {
                 filterNameCombo.select(_selectIndex);
                 updateDialog((FilterInstance)_filterList.get(_selectIndex));
             }                          
         }
        } else {
            fc = new FilterCollector();
        }
    }

    /**
     * Update the dialog.
     * @param _init
     */
    private void updateDialog(FilterInstance _init) {
        // Dependee
        chkControlDd.setSelection(_init.controlDd);
        chkdataDd.setSelection(_init.dataDd);
        chkIntDd.setSelection(_init.intfDd);
        chkRdyDd.setSelection(_init.rdyDd);
        chkDvgDd.setSelection(_init.dvgDd);
        chkSynDd.setSelection(_init.syncDd);
        
        // Dependents
        chkControlDt.setSelection(_init.controlDt);
        chkdataDt.setSelection(_init.dataDt);
        chkIntDt.setSelection(_init.intfDt);
        chkRdyDt.setSelection(_init.rdyDt);
        chkDvgDt.setSelection(_init.dvgDt);
        chkSynDt.setSelection(_init.syncDt);
    }

    /**
     * Add the dependee checkboxes.
     * @param dd
     */
    private void addDependeeBoxes(Group dd) {
        // Control
        chkControlDd = new Button(dd, SWT.CHECK);
        chkControlDd.setText("Control");
        
        // Identifier
        chkdataDd = new Button(dd, SWT.CHECK);
        chkdataDd.setText("Data");
        
        
        // Ready
        chkRdyDd = new Button(dd, SWT.CHECK);
        chkRdyDd.setText("Ready");
        
        // Interference
        chkIntDd = new Button(dd, SWT.CHECK);
        chkIntDd.setText("Interference");
        
        // Synchronization
        chkSynDd = new Button(dd, SWT.CHECK);
        chkSynDd.setText("Synchronization");
        
        // Divergence
        chkDvgDd = new Button(dd, SWT.CHECK);
        chkDvgDd.setText("Divergence");
        
    }
    
    /**
     * Add the dependent checkboxes.
     * @param dt
     */
    private void addDependentBoxes(Group dt) {
        // Control
        chkControlDt = new Button(dt, SWT.CHECK);
        chkControlDt.setText("Control");
        
        // Data
        chkdataDt = new Button(dt, SWT.CHECK);
        chkdataDt.setText("Data");
        
        
        // Ready
        chkRdyDt = new Button(dt, SWT.CHECK);
        chkRdyDt.setText("Ready");
        
        // Interference
        chkIntDt = new Button(dt, SWT.CHECK);
        chkIntDt.setText("Interference");
        
        // Synchronization
        chkSynDt = new Button(dt, SWT.CHECK);
        chkSynDt.setText("Synchronization");
        
        // Divergence
        chkDvgDt = new Button(dt, SWT.CHECK);
        chkDvgDt.setText("Divergence");
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, DependenceFilterDialog.ADD_FILTER, "Add Filter", false);
        super.createButtonsForButtonBar(parent);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {        
        super.buttonPressed(buttonId);
        if (buttonId == DependenceFilterDialog.ADD_FILTER) {
            if (!chkControlDd.getEnabled()) {
                setCheckboxStatus(true);
            }
            saveChanges(filterNameCombo.getSelectionIndex());
            final String _filterName = "<NEW FILTER-" + fc.getFilterList().size() + ">";
            filterNameCombo.add(_filterName);            
            FilterInstance _inst = new FilterInstance();
            _inst.filterName = _filterName;
            updateDialog(_inst);
            fc.add(_inst);
            filterNameCombo.select(filterNameCombo.getItemCount()-1);
        }
    }

    /**
     * Set all the checkboxes to true.
     */
    private void setAllBoxes() {
//      Dependee
        chkControlDd.setSelection(true);
        chkdataDd.setSelection(true);
        chkIntDd.setSelection(true);
        chkRdyDd.setSelection(true);
        chkDvgDd.setSelection(true);
        chkSynDd.setSelection(true);
        
        
        // Dependents
        chkControlDt.setSelection(true);
        chkdataDt.setSelection(true);
        chkIntDt.setSelection(true);
        chkRdyDt.setSelection(true);
        chkDvgDt.setSelection(true);
        chkSynDt.setSelection(true);
        
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        saveCurrentSelection();
        final String _filterKey = "edu.ksu.cis.indus.kaveri.depview.filter.key";
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();        
        final XStream _stream = new XStream();
        if (filterNameCombo.getItemCount() > 0) {
        fc.setCurrentFilter(filterNameCombo.getText());
        }
        final String _val = _stream.toXML(fc);
        _ps.setValue(_filterKey, _val);
        KaveriPlugin.getDefault().savePluginPreferences();
        super.okPressed();
    }

    /**
     * Saves the data for the currently selected filter.
     */
    private void saveCurrentSelection() {
        String _fName = filterNameCombo.getText();
        final List filterList = fc.getFilterList();
        for (int _i = 0; _i < filterList.size(); _i++) {
            final FilterInstance _inst = (FilterInstance) filterList.get(_i);
            if (_inst.filterName.equals(_fName)) {
                saveChanges(_i);
                break;
            }
        }                
    }
}
