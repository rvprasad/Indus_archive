
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.slicer.SlicingEngine;

import edu.ksu.cis.indus.tools.AbstractToolConfigurator;
import edu.ksu.cis.indus.tools.IToolConfiguration;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


/**
 * This provides the graphical user interface via which the user can configure the slicer.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerConfigurator
  extends AbstractToolConfigurator {
	/** 
	 * This is the button used to toggle executability.
	 */
	Button executableSliceButton;

	/**
	 * Creates a new SlicerConfigurator object.
	 */
	SlicerConfigurator() {
	}

	/**
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(final DisposeEvent evt) {
		super.widgetDisposed(evt);
		executableSliceButton = null;
	}

	/**
	 * Checks if <code>toolConfiguration</code> can be handled by this configurator.
	 *
	 * @param toolConfiguration is the configuration to be check.
	 *
	 * @throws RuntimeException when <code>toolConfiguration</code> is an unhandled type of exception.
	 *
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfigurator#checkConfiguration(IToolConfiguration)
	 */
	protected void checkConfiguration(final IToolConfiguration toolConfiguration) {
		if (!(toolConfiguration instanceof SlicerConfiguration)) {
			throw new RuntimeException(
				"The toolConfiguration has to be of type edu.ksu.cis.indus.tools.slicer.SlicerConfiguration.");
		}
	}

	/**
	 * {@inheritDoc} This method should be called after <code>setConfiguration</code> has been invoked on this object.
	 */
	protected void setup() {
		final GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 3;
		parent.setLayout(_gridLayout);

		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		executableSliceButton = new Button(parent, SWT.CHECK);
		executableSliceButton.setText("Executable slice");

		final GridData _gridData1 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData1.horizontalSpan = 2;
		_gridData1.horizontalAlignment = SWT.LEFT;
		executableSliceButton.setLayoutData(_gridData1);

		final Boolean _executableProperty = (Boolean) _cfg.getProperty(SlicerConfiguration.EXECUTABLE_SLICE);
		executableSliceButton.setSelection(_executableProperty.booleanValue());

		final SelectionListener _sl1 =
			new BooleanPropertySelectionListener(SlicerConfiguration.EXECUTABLE_SLICE, executableSliceButton, _cfg);
		executableSliceButton.addSelectionListener(_sl1);

		if (_cfg.getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			executableSliceButton.setEnabled(false);
		}

		final Button _assertionPreservingSliceButton = new Button(parent, SWT.CHECK);
		_assertionPreservingSliceButton.setText("Preserve assertions");

		final GridData _gridData2 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData2.horizontalSpan = 1;
		_gridData2.horizontalAlignment = SWT.LEFT;
		_assertionPreservingSliceButton.setLayoutData(_gridData2);

		final Boolean _assertionsProperty = (Boolean) _cfg.getProperty(SlicerConfiguration.SLICE_TO_PRESERVE_ASSERTIONS);
		_assertionPreservingSliceButton.setSelection(_assertionsProperty.booleanValue());

		final SelectionListener _sl2 =
			new BooleanPropertySelectionListener(SlicerConfiguration.EXECUTABLE_SLICE, _assertionPreservingSliceButton, _cfg);
		_assertionPreservingSliceButton.addSelectionListener(_sl2);

		setupSliceInfoUI();
		setupInteferenceDepUI();
		setupDivergenceDepUI();
		setupReadyDepUI();
		parent.pack();
	}

	/**
	 * Sets up row corresponding to Divergence DA in the configurator composite.
	 */
	private void setupDivergenceDepUI() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Divergence dependence related group
		final GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		_gridData.horizontalSpan = 3;

		final Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Divergence dependence");
		_group.setLayoutData(_gridData);

		final GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 2;
		_group.setLayout(_gridLayout);

		final Button _useDDAButton = new Button(_group, SWT.CHECK);
		_useDDAButton.setText("use divergence dependence");
		_useDDAButton.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_DIVERGENCEDA)).booleanValue());
		_useDDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_DIVERGENCEDA,
				_useDDAButton, _cfg));

		final Button _interProceduralDivergenceDAButton = new Button(_group, SWT.CHECK);
		_interProceduralDivergenceDAButton.setText("use interprocedural variant");

		if (_useDDAButton.getSelection()) {
			final Boolean _interProceduralDDA = (Boolean) _cfg.getProperty(SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA);

			if (_interProceduralDDA != null) {
				_interProceduralDivergenceDAButton.setSelection(_interProceduralDDA.booleanValue());
			} else {
				_interProceduralDivergenceDAButton.setSelection(false);
			}
		} else {
			_interProceduralDivergenceDAButton.setEnabled(false);
		}
		_interProceduralDivergenceDAButton.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA,
				_interProceduralDivergenceDAButton,
				_cfg));
		_useDDAButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					if (_useDDAButton.getSelection()) {
						_interProceduralDivergenceDAButton.setEnabled(true);
					} else {
						_interProceduralDivergenceDAButton.setEnabled(false);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});
	}

	/**
	 * Sets up row corresponding Interference DA in the configurator composite.
	 */
	private void setupInteferenceDepUI() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Interference dependence related group
		final Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Interference dependence");

		final GridData _gridData = new GridData(GridData.FILL_HORIZONTAL);
		_gridData.horizontalSpan = 3;
		_group.setLayoutData(_gridData);

		final GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 2;
		_group.setLayout(_gridLayout);

		final Button _useIDAButton = new Button(_group, SWT.CHECK);
		_useIDAButton.setText("use interference dependence");
		_useIDAButton.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_INTERFERENCEDA)).booleanValue());
		_useIDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_INTERFERENCEDA,
				_useIDAButton, _cfg));

		//Sets up the composite and buttons pertaining to precision control.        
		final Group _natureOfIDAGroup = new Group(_group, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout3 = new GridLayout();
		_gridLayout3.numColumns = 2;
		_natureOfIDAGroup.setLayout(_gridLayout3);
		_natureOfIDAGroup.setText("Precision of Interference dependence");

		final GridData _gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		_gridData1.horizontalSpan = 2;
		_natureOfIDAGroup.setLayoutData(_gridData1);

		final Group _precisionGroup = new Group(_natureOfIDAGroup, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout4 = new GridLayout();
		_gridLayout4.numColumns = 1;
		_precisionGroup.setLayout(_gridLayout4);
		_precisionGroup.setText("Inteference dependence mode");

		final Button _typedIDA = new Button(_precisionGroup, SWT.RADIO);
		_typedIDA.setText("use type-based analysis");

		final Button _equivalenceClassBasedEscapeAnalysisBasedIDA = new Button(_precisionGroup, SWT.RADIO);
		_equivalenceClassBasedEscapeAnalysisBasedIDA.setText("use equivalence class-based analysis");

		final Button _symbolBasedEscapeAnalysisBasedIDA = new Button(_precisionGroup, SWT.RADIO);
		_symbolBasedEscapeAnalysisBasedIDA.setText("use sybmol and equivalence class-based analysis");

		final SelectionListener _sl2 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _equivalenceClassBasedEscapeAnalysisBasedIDA) {
						_value = SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO;
					} else if (evt.widget == _symbolBasedEscapeAnalysisBasedIDA) {
						_value = SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO;
					} else if (evt.widget == _typedIDA) {
						_value = SlicerConfiguration.TYPE_BASED_INFO;
					}

					if (_value != null) {
						_cfg.setProperty(SlicerConfiguration.NATURE_OF_INTERFERENCE_DA, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_equivalenceClassBasedEscapeAnalysisBasedIDA.addSelectionListener(_sl2);
		_symbolBasedEscapeAnalysisBasedIDA.addSelectionListener(_sl2);
		_typedIDA.addSelectionListener(_sl2);

		final Object _temp = _cfg.getProperty(SlicerConfiguration.NATURE_OF_INTERFERENCE_DA);

		if (_temp == null || _temp.equals(SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			_symbolBasedEscapeAnalysisBasedIDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO)) {
			_equivalenceClassBasedEscapeAnalysisBasedIDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.TYPE_BASED_INFO)) {
			_typedIDA.setSelection(true);
		}

		//Sets up the buttons that control what auxiliary analysis are used improve precision.         
		final Composite _analysisComposite = new Composite(_natureOfIDAGroup, SWT.NONE);
		final RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_analysisComposite.setLayout(_rowLayout);

		final GridData _analysisCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
		_analysisCompositeGridData.verticalAlignment = SWT.TOP;
		_analysisComposite.setLayoutData(_analysisCompositeGridData);

		final Button _useOFAForInterference = new Button(_analysisComposite, SWT.CHECK);
		_useOFAForInterference.setText("use object flow analysis information");

		_useOFAForInterference.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.USE_OFA_FOR_INTERFERENCE_DA,
				_useOFAForInterference,
				_cfg));

		final Boolean _bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_OFA_FOR_INTERFERENCE_DA);
		_useOFAForInterference.setSelection(_bool.booleanValue());

		// Links up the buttons via selection listener to control toggling based on the user's decision 
		// to use interference DA.
		_useIDAButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					boolean _val = false;

					if (_useIDAButton.getSelection()) {
						_val = true;
					}
					_natureOfIDAGroup.setEnabled(_val);
					_equivalenceClassBasedEscapeAnalysisBasedIDA.setEnabled(_val);
					_typedIDA.setEnabled(_val);
					_symbolBasedEscapeAnalysisBasedIDA.setEnabled(_val);
					_useOFAForInterference.setEnabled(_val);
					_precisionGroup.setEnabled(_val);
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		if (_useIDAButton.getSelection()) {
			final Boolean _b = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_OFA_FOR_READY_DA);
			_useOFAForInterference.setSelection(_b.booleanValue());
			_natureOfIDAGroup.setEnabled(true);
			_typedIDA.setEnabled(true);
			_equivalenceClassBasedEscapeAnalysisBasedIDA.setEnabled(true);
			_symbolBasedEscapeAnalysisBasedIDA.setEnabled(true);
			_precisionGroup.setEnabled(true);
		} else {
			_useOFAForInterference.setEnabled(false);
			_natureOfIDAGroup.setEnabled(false);
			_typedIDA.setEnabled(false);
			_equivalenceClassBasedEscapeAnalysisBasedIDA.setEnabled(false);
			_symbolBasedEscapeAnalysisBasedIDA.setEnabled(false);
			_precisionGroup.setEnabled(false);
		}
	}

	/**
	 * Sets up row corresponding to Ready DA in the configurator composite.
	 */
	private void setupReadyDepUI() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Ready dependence related group
		final Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Ready dependence");

		GridData _twoSpanHorzFill = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_twoSpanHorzFill.horizontalSpan = 3;
		_group.setLayoutData(_twoSpanHorzFill);

		final GridLayout _gridLayout1 = new GridLayout();
		_gridLayout1.numColumns = 2;
		_group.setLayout(_gridLayout1);

		Composite _readyComposite = new Composite(_group, SWT.NONE);
		_twoSpanHorzFill = new GridData(GridData.FILL_HORIZONTAL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_readyComposite.setLayoutData(_twoSpanHorzFill);

		final GridLayout _gridLayout2 = new GridLayout();
		_gridLayout2.numColumns = 2;
		_readyComposite.setLayout(_gridLayout2);

		final Button _useRDAButton = new Button(_readyComposite, SWT.CHECK);
		_useRDAButton.setText("use ready dependence");
		_useRDAButton.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_READYDA)).booleanValue());
		_useRDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_READYDA,
				_useRDAButton, _cfg));

		// Sets up the composite and buttons pertaining to precision control.        
		final Group _natureOfRDAGroup = new Group(_group, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout3 = new GridLayout();
		_gridLayout3.numColumns = 2;
		_natureOfRDAGroup.setLayout(_gridLayout3);
		_natureOfRDAGroup.setText("Precision of Ready dependence");

		final GridData _gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		_gridData1.horizontalSpan = 2;
		_natureOfRDAGroup.setLayoutData(_gridData1);

		final Group _precisionGroup = new Group(_natureOfRDAGroup, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout4 = new GridLayout();
		_gridLayout4.numColumns = 1;
		_precisionGroup.setLayout(_gridLayout4);
		_precisionGroup.setText("Ready dependence mode");

		// Sets up the buttons that control the nature of ready dependence.
		final Button _typedRDA = new Button(_precisionGroup, SWT.RADIO);
		_typedRDA.setText("use type-based analysis");

		final Button _equivalenceClassBasedEscapeAnalysisBasedRDA = new Button(_precisionGroup, SWT.RADIO);
		_equivalenceClassBasedEscapeAnalysisBasedRDA.setText("use equivalence class-based analysis");

		final Button _symbolBasedEscapeAnalysisBasedRDA = new Button(_precisionGroup, SWT.RADIO);
		_symbolBasedEscapeAnalysisBasedRDA.setText("use sybmol and equivalence class-based analysis");

		SelectionListener _sl =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _equivalenceClassBasedEscapeAnalysisBasedRDA) {
						_value = SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO;
					} else if (evt.widget == _symbolBasedEscapeAnalysisBasedRDA) {
						_value = SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO;
					} else if (evt.widget == _typedRDA) {
						_value = SlicerConfiguration.TYPE_BASED_INFO;
					}

					if (_value != null) {
						_cfg.setProperty(SlicerConfiguration.NATURE_OF_READY_DA, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_equivalenceClassBasedEscapeAnalysisBasedRDA.addSelectionListener(_sl);
		_symbolBasedEscapeAnalysisBasedRDA.addSelectionListener(_sl);
		_typedRDA.addSelectionListener(_sl);

		final Object _temp = _cfg.getProperty(SlicerConfiguration.NATURE_OF_READY_DA);

		if (_temp == null || _temp.equals(SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			_symbolBasedEscapeAnalysisBasedRDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO)) {
			_equivalenceClassBasedEscapeAnalysisBasedRDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.TYPE_BASED_INFO)) {
			_typedRDA.setSelection(true);
		}

		// Sets up the buttons that control what auxiliary analysis are used improve precision.         
		final Composite _analysisComposite = new Composite(_natureOfRDAGroup, SWT.NONE);
		final RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_analysisComposite.setLayout(_rowLayout);

		final GridData _analysisCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
		_analysisCompositeGridData.verticalAlignment = SWT.TOP;
		_analysisComposite.setLayoutData(_analysisCompositeGridData);

		final Button _useOFAForReady = new Button(_analysisComposite, SWT.CHECK);
		_useOFAForReady.setText("use object flow analysis information");
		_useOFAForReady.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_OFA_FOR_READY_DA)).booleanValue());
		_useOFAForReady.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_OFA_FOR_READY_DA,
				_useOFAForReady, _cfg));

		final Button _useSLAForReady = new Button(_analysisComposite, SWT.CHECK);
		_useSLAForReady.setText("use safe lock analysis ");
		_useSLAForReady.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_SLA_FOR_READY_DA)).booleanValue());
		_useSLAForReady.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_SLA_FOR_READY_DA,
				_useSLAForReady, _cfg));

		// Sets up the buttons that control what rules of ready dependence analysis are used.        
		_readyComposite = new Composite(_group, SWT.NONE);
		_twoSpanHorzFill = new GridData(GridData.FILL_HORIZONTAL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_readyComposite.setLayoutData(_twoSpanHorzFill);

		final GridLayout _gridLayout5 = new GridLayout();
		_gridLayout5.numColumns = 2;
		_readyComposite.setLayout(_gridLayout5);

		final GridData _gridData2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_gridData2.horizontalSpan = 2;
		_readyComposite.setLayoutData(_gridData2);

		final Button _rule1RDAButton = new Button(_readyComposite, SWT.CHECK);
		_rule1RDAButton.setText("use rule 1 of ready dependence");
		_rule1RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule1RDAButton, _cfg));

		final Button _rule2RDAButton = new Button(_readyComposite, SWT.CHECK);
		_rule2RDAButton.setText("use rule 2 of ready dependence");
		_rule2RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule2RDAButton, _cfg));

		final Button _rule3RDAButton = new Button(_readyComposite, SWT.CHECK);
		_rule3RDAButton.setText("use rule 3 of ready dependence");
		_rule3RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule3RDAButton, _cfg));

		final Button _rule4RDAButton = new Button(_readyComposite, SWT.CHECK);
		_rule4RDAButton.setText("use rule 4 of ready dependence");
		_rule4RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule4RDAButton, _cfg));

		// Links up the buttons via selection listener to control toggling based on the user's decision to use ready DA.
		_useRDAButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					boolean _val = false;

					if (_useRDAButton.getSelection()) {
						_val = true;
					}
					_precisionGroup.setEnabled(_val);
					_natureOfRDAGroup.setEnabled(_val);
					_equivalenceClassBasedEscapeAnalysisBasedRDA.setEnabled(_val);
					_typedRDA.setEnabled(_val);
					_useOFAForReady.setEnabled(_val);
					_useSLAForReady.setEnabled(_val);
					_symbolBasedEscapeAnalysisBasedRDA.setEnabled(_val);
					_rule1RDAButton.setEnabled(_val);
					_rule2RDAButton.setEnabled(_val);
					_rule3RDAButton.setEnabled(_val);
					_rule4RDAButton.setEnabled(_val);
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		if (_useRDAButton.getSelection()) {
			_precisionGroup.setEnabled(true);
			_natureOfRDAGroup.setEnabled(true);
			_typedRDA.setEnabled(true);
			_equivalenceClassBasedEscapeAnalysisBasedRDA.setEnabled(true);
			_symbolBasedEscapeAnalysisBasedRDA.setEnabled(true);

			Boolean _bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA);
			_rule1RDAButton.setSelection(_bool.booleanValue());
			_bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_RULE2_IN_READYDA);
			_rule2RDAButton.setSelection(_bool.booleanValue());
			_bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_RULE3_IN_READYDA);
			_rule3RDAButton.setSelection(_bool.booleanValue());
			_bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_RULE4_IN_READYDA);
			_rule4RDAButton.setSelection(_bool.booleanValue());
			_bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_OFA_FOR_READY_DA);
			_useOFAForReady.setSelection(_bool.booleanValue());
			_bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_SLA_FOR_READY_DA);
			_useSLAForReady.setSelection(_bool.booleanValue());
		} else {
			_precisionGroup.setEnabled(false);
			_natureOfRDAGroup.setEnabled(false);
			_typedRDA.setEnabled(false);
			_equivalenceClassBasedEscapeAnalysisBasedRDA.setEnabled(false);
			_symbolBasedEscapeAnalysisBasedRDA.setEnabled(false);
			_rule1RDAButton.setEnabled(false);
			_rule2RDAButton.setEnabled(false);
			_rule3RDAButton.setEnabled(false);
			_rule4RDAButton.setEnabled(false);
			_useOFAForReady.setEnabled(false);
			_useSLAForReady.setEnabled(false);
		}
	}

	/**
	 * Sets up row to configure deadlock preserving slicing  and slice type.
	 */
	private void setupSliceInfoUI() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;
		final Group _deadlockGroup = new Group(parent, SWT.NONE);
		final GridData _gridData1 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		_gridData1.horizontalSpan = 2;
		_deadlockGroup.setLayoutData(_gridData1);

		final RowLayout _rowLayout1 = new RowLayout();
		_rowLayout1.type = SWT.VERTICAL;
		_rowLayout1.fill = true;
		_deadlockGroup.setLayout(_rowLayout1);
		_deadlockGroup.setText("Slice for Deadlock");

		final Button _button = new Button(_deadlockGroup, SWT.CHECK);
		_button.setText("Preserve Deadlock");
		_button.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.SLICE_FOR_DEADLOCK)).booleanValue());

		final Group _group1 = new Group(_deadlockGroup, SWT.SHADOW_ETCHED_IN);
		_group1.setText("Deadlock Criteria Selection Strategy");

		final RowLayout _rowLayout2 = new RowLayout();
		_rowLayout2.type = SWT.VERTICAL;
		_group1.setLayout(_rowLayout2);

		final Button _allSycnStrategy = new Button(_group1, SWT.RADIO);
		_allSycnStrategy.setText("All Synchronization constructs");

		final Button _escapingSyncStrategy = new Button(_group1, SWT.RADIO);
		_escapingSyncStrategy.setText("Escaping Sychronization constructs");

		final Button _ctxtsensEscapingSyncStrategy = new Button(_group1, SWT.RADIO);
		_ctxtsensEscapingSyncStrategy.setText("Escaping Sychronization constructs with their contexts");

		final SelectionListener _sl2 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _ctxtsensEscapingSyncStrategy) {
						_value = SlicerConfiguration.CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS;
					} else if (evt.widget == _escapingSyncStrategy) {
						_value = SlicerConfiguration.ESCAPING_SYNC_CONSTRUCTS;
					} else if (evt.widget == _allSycnStrategy) {
						_value = SlicerConfiguration.ALL_SYNC_CONSTRUCTS;
					}

					if (_value != null) {
						_cfg.setProperty(SlicerConfiguration.DEADLOCK_CRITERIA_SELECTION_STRATEGY, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_allSycnStrategy.addSelectionListener(_sl2);
		_escapingSyncStrategy.addSelectionListener(_sl2);
		_ctxtsensEscapingSyncStrategy.addSelectionListener(_sl2);

		final Object _temp = _cfg.getDeadlockCriteriaSelectionStrategy();

		if (_temp.equals(SlicerConfiguration.ALL_SYNC_CONSTRUCTS)) {
			_allSycnStrategy.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.ESCAPING_SYNC_CONSTRUCTS)) {
			_escapingSyncStrategy.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS)) {
			_ctxtsensEscapingSyncStrategy.setSelection(true);
		}

		final SelectionListener _sl1 =
			new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, _button, _cfg) {
				public void widgetSelected(final SelectionEvent evt) {
					final boolean _value = button.getSelection();
					containingConfiguration.setProperty(id, Boolean.valueOf(_value));
					_group1.setEnabled(_value);
					_allSycnStrategy.setEnabled(_value);
					_escapingSyncStrategy.setEnabled(_value);
					_ctxtsensEscapingSyncStrategy.setEnabled(_value);
				}
			};
		_button.addSelectionListener(_sl1);

		//Slice type related group
		final Group _group2 = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group2.setText("Slice Type");

		final GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData.horizontalSpan = 1;
		_gridData.verticalAlignment = SWT.TOP;
		_gridData.horizontalAlignment = SWT.RIGHT;
		_group2.setLayoutData(_gridData);

		final RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_group2.setLayout(_rowLayout);

		final Button _backwardSlice = new Button(_group2, SWT.RADIO);
		_backwardSlice.setText("Backward slice");

		final Button _forwardSlice = new Button(_group2, SWT.RADIO);
		_forwardSlice.setText("Forward slice");

		final Button _completeSlice = new Button(_group2, SWT.RADIO);
		_completeSlice.setText("Complete slice");

		final SelectionListener _sl3 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _forwardSlice) {
						_value = SlicingEngine.FORWARD_SLICE;
						executableSliceButton.setEnabled(false);
					} else {
						if (evt.widget == _backwardSlice) {
							_value = SlicingEngine.BACKWARD_SLICE;
						} else if (evt.widget == _completeSlice) {
							_value = SlicingEngine.COMPLETE_SLICE;
						}
						executableSliceButton.setEnabled(true);
					}

					if (_value != null) {
						_cfg.setProperty(SlicerConfiguration.SLICE_TYPE, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_backwardSlice.addSelectionListener(_sl3);
		_completeSlice.addSelectionListener(_sl3);
		_forwardSlice.addSelectionListener(_sl3);

		final Object _sliceType = _cfg.getSliceType();

		if (_sliceType.equals(SlicingEngine.BACKWARD_SLICE)) {
			_backwardSlice.setSelection(true);
		} else if (_sliceType.equals(SlicingEngine.COMPLETE_SLICE)) {
			_completeSlice.setSelection(true);
		} else if (_sliceType.equals(SlicingEngine.FORWARD_SLICE)) {
			_forwardSlice.setSelection(true);
		}
	}
}

// End of File
