
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
		_gridLayout.numColumns = 2;
		parent.setLayout(_gridLayout);

		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;
		final GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData.horizontalSpan = 2;

		executableSliceButton = new Button(parent, SWT.CHECK);
		executableSliceButton.setText("Executable slice");
		executableSliceButton.setLayoutData(_gridData);
		executableSliceButton.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.EXECUTABLE_SLICE)).booleanValue());

		final SelectionListener _sl =
			new BooleanPropertySelectionListener(SlicerConfiguration.EXECUTABLE_SLICE, executableSliceButton, _cfg);
		executableSliceButton.addSelectionListener(_sl);

		if (_cfg.getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			executableSliceButton.setEnabled(false);
		}

		setupRow2();
		setupRow3();
		setupRow4();
		setupRow5();
		parent.pack();
	}

	/**
	 * Sets up row 2 to configure deadlock preserving slicing  and slice type.
	 */
	private void setupRow2() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;
		final Composite _comp = new Composite(parent, SWT.NONE);
		final GridData _gridData1 = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		_gridData1.horizontalSpan = 1;
		_comp.setLayoutData(_gridData1);

		final RowLayout _rowLayout1 = new RowLayout();
		_rowLayout1.type = SWT.VERTICAL;
		_rowLayout1.fill = true;
		_comp.setLayout(_rowLayout1);

		final Button _button = new Button(_comp, SWT.CHECK);
		_button.setText("Slice for Deadlock");
		_button.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.SLICE_FOR_DEADLOCK)).booleanValue());

		final Group _group1 = new Group(_comp, SWT.SHADOW_ETCHED_IN);
		_group1.setText("Deadlock Criteria Selection Strategy");

		final RowLayout _rowLayout2 = new RowLayout();
		_rowLayout2.type = SWT.VERTICAL;
		_group1.setLayout(_rowLayout2);

		final Button _allSycnStrategy = new Button(_group1, SWT.RADIO);
		_allSycnStrategy.setText("All Synchronization constructs");

		final Button _escapingSyncStrategy = new Button(_group1, SWT.RADIO);
		_escapingSyncStrategy.setText("Escaping Sychronization constructs");

		/*final Button _completeSlice = new Button(_group, SWT.RADIO);
		   _completeSlice.setText("Complete slice");*/
		final SelectionListener _sl2 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _escapingSyncStrategy) {
						_value = SlicerConfiguration.ESCAPING_SYNC_CONSTRUCTS;
					} else if (evt.widget == _allSycnStrategy) {
						_value = SlicerConfiguration.ALL_SYNC_CONSTRUCTS;

						/*} else if (evt.widget == _completeSlice) {
						   _value = SlicingEngine.COMPLETE_SLICE;*/
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
		//_completeSlice.addSelectionListener(_sl);
		_escapingSyncStrategy.addSelectionListener(_sl2);

		final Object _temp = _cfg.getDeadlockCriteriaSelectionStrategy();

		if (_temp.equals(SlicerConfiguration.ALL_SYNC_CONSTRUCTS)) {
			_allSycnStrategy.setSelection(true);

			/*} else if (_temp.equals(SlicingEngine.COMPLETE_SLICE)) {
			   _completeSlice.setSelection(true);*/
		} else if (_temp.equals(SlicerConfiguration.ESCAPING_SYNC_CONSTRUCTS)) {
			_escapingSyncStrategy.setSelection(true);
		}

		final SelectionListener _sl1 =
			new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, _button, _cfg) {
				public void widgetSelected(final SelectionEvent evt) {
					final boolean _value = button.getSelection();
					containingConfiguration.setProperty(id, Boolean.valueOf(_value));
					_group1.setEnabled(_value);
					_allSycnStrategy.setEnabled(_value);
					_escapingSyncStrategy.setEnabled(_value);
				}
			};
		_button.addSelectionListener(_sl1);

		//Slice type related group
		final Group _group2 = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group2.setText("Slice Type");

		final GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData.horizontalSpan = 1;
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

	/**
	 * Sets up row 3 corresponding Interference DA in the configurator composite.
	 */
	private void setupRow3() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Interference dependence related group
		final GridData _gridData = new GridData(GridData.FILL_HORIZONTAL);
		_gridData.horizontalSpan = 2;

		final Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Interference dependence");
		_group.setLayoutData(_gridData);

		final RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_group.setLayout(_rowLayout);

		final Button _typedIDA = new Button(_group, SWT.RADIO);
		_typedIDA.setText("use type-based analysis");

		final Button _equivalenceClassEscapeAnalysisBasedIDA = new Button(_group, SWT.RADIO);
		_equivalenceClassEscapeAnalysisBasedIDA.setText("use equivalence class-based analysis");

		final Button _symbolBasedEscapeAnalysisBasedIDA = new Button(_group, SWT.RADIO);
		_symbolBasedEscapeAnalysisBasedIDA.setText("use sybmol and equivalence class-based analysis");

		final Button _useOFAForInterference = new Button(_group, SWT.CHECK);
		_useOFAForInterference.setText("use object flow analysis information");

		final SelectionListener _sl1 =
			new BooleanPropertySelectionListener(SlicerConfiguration.USE_OFA_FOR_INTERFERENCE_DA, _useOFAForInterference, _cfg);
		_useOFAForInterference.addSelectionListener(_sl1);

		final SelectionListener _sl2 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _equivalenceClassEscapeAnalysisBasedIDA) {
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
		_equivalenceClassEscapeAnalysisBasedIDA.addSelectionListener(_sl2);
		_symbolBasedEscapeAnalysisBasedIDA.addSelectionListener(_sl2);
		_typedIDA.addSelectionListener(_sl2);

		final Object _temp = _cfg.getProperty(SlicerConfiguration.NATURE_OF_INTERFERENCE_DA);

		if (_temp == null || _temp.equals(SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			_symbolBasedEscapeAnalysisBasedIDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO)) {
			_equivalenceClassEscapeAnalysisBasedIDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.TYPE_BASED_INFO)) {
			_typedIDA.setSelection(true);
		}

		final Boolean _bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_OFA_FOR_INTERFERENCE_DA);
		_useOFAForInterference.setSelection(_bool.booleanValue());
	}

	/**
	 * Sets up row 4 corresponding to Divergence DA in the configurator composite.
	 */
	private void setupRow4() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Divergence dependence related group
		final GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		_gridData.horizontalSpan = 2;

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
			final Boolean _interProceduralDDA =
				((Boolean) _cfg.getProperty(SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA));

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
	 * Sets up row 5 corresponding to Ready DA in the configurator composite.
	 */
	private void setupRow5() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Ready dependence related group
		final Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Ready dependence");

		GridData _twoSpanHorzFill = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_group.setLayoutData(_twoSpanHorzFill);

		GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 3;
		_group.setLayout(_gridLayout);

		Composite _readyComposite = new Composite(_group, SWT.NONE);
		_twoSpanHorzFill = new GridData(GridData.FILL_HORIZONTAL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_readyComposite.setLayoutData(_twoSpanHorzFill);
		_gridLayout = new GridLayout();
		_gridLayout.numColumns = 3;
		_readyComposite.setLayout(_gridLayout);

		GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_gridData.horizontalSpan = 2;
		_readyComposite.setLayoutData(_gridData);

		final Button _useRDAButton = new Button(_readyComposite, SWT.CHECK);
		_useRDAButton.setText("use ready dependence");
		_useRDAButton.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_READYDA)).booleanValue());
		_useRDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_READYDA,
				_useRDAButton, _cfg));

		final Button _useSLAForReady = new Button(_readyComposite, SWT.CHECK);
		_useSLAForReady.setText("use safe lock analysis ");
		_useSLAForReady.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_SLA_FOR_READY_DA)).booleanValue());
		_useSLAForReady.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_SLA_FOR_READY_DA,
				_useSLAForReady, _cfg));

		_gridData = new GridData(GridData.FILL_HORIZONTAL);
		_gridData.horizontalSpan = 2;

		final Group _natureOfRDAGroup = new Group(_group, SWT.SHADOW_ETCHED_IN);
		_gridLayout = new GridLayout();
		_gridLayout.numColumns = 1;
		_natureOfRDAGroup.setLayout(_gridLayout);
		_natureOfRDAGroup.setText("Precision of Ready dependence");
		_natureOfRDAGroup.setLayoutData(_gridData);

		final Button _typedRDA = new Button(_natureOfRDAGroup, SWT.RADIO);
		_typedRDA.setText("use type-based analysis");

		final Button _equivalenceClassBasedEscapceAnalysisBasedRDA = new Button(_natureOfRDAGroup, SWT.RADIO);
		_equivalenceClassBasedEscapceAnalysisBasedRDA.setText("use equivalence class-based analysis");

		final Button _symbolBasedEscapeAnalysisBasedRDA = new Button(_natureOfRDAGroup, SWT.RADIO);
		_symbolBasedEscapeAnalysisBasedRDA.setText("use sybmol and equivalence class-based analysis");

		SelectionListener _sl =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _equivalenceClassBasedEscapceAnalysisBasedRDA) {
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
		_equivalenceClassBasedEscapceAnalysisBasedRDA.addSelectionListener(_sl);
		_symbolBasedEscapeAnalysisBasedRDA.addSelectionListener(_sl);
		_typedRDA.addSelectionListener(_sl);

		final Object _temp = _cfg.getProperty(SlicerConfiguration.NATURE_OF_READY_DA);

		if (_temp == null || _temp.equals(SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			_symbolBasedEscapeAnalysisBasedRDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO)) {
			_equivalenceClassBasedEscapceAnalysisBasedRDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.TYPE_BASED_INFO)) {
			_typedRDA.setSelection(true);
		}

		final Button _useOFAForReady = new Button(_natureOfRDAGroup, SWT.CHECK);
		_useOFAForReady.setText("use object flow analysis information");
		_sl = new BooleanPropertySelectionListener(SlicerConfiguration.USE_OFA_FOR_READY_DA, _useOFAForReady, _cfg);
		_useOFAForReady.addSelectionListener(_sl);

		_readyComposite = new Composite(_group, SWT.NONE);
		_twoSpanHorzFill = new GridData(GridData.FILL_HORIZONTAL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_readyComposite.setLayoutData(_twoSpanHorzFill);
		_gridLayout = new GridLayout();
		_gridLayout.numColumns = 2;
		_readyComposite.setLayout(_gridLayout);
		_gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_gridData.horizontalSpan = 2;
		_readyComposite.setLayoutData(_gridData);

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

		_useRDAButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					boolean _val = false;

					if (_useRDAButton.getSelection()) {
						_val = true;
					}
					_natureOfRDAGroup.setEnabled(_val);
					_equivalenceClassBasedEscapceAnalysisBasedRDA.setEnabled(_val);
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
			_natureOfRDAGroup.setEnabled(true);
			_typedRDA.setEnabled(true);
			_equivalenceClassBasedEscapceAnalysisBasedRDA.setEnabled(true);
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
			_natureOfRDAGroup.setEnabled(false);
			_typedRDA.setEnabled(false);
			_equivalenceClassBasedEscapceAnalysisBasedRDA.setEnabled(false);
			_symbolBasedEscapeAnalysisBasedRDA.setEnabled(false);
			_rule1RDAButton.setEnabled(false);
			_rule2RDAButton.setEnabled(false);
			_rule3RDAButton.setEnabled(false);
			_rule4RDAButton.setEnabled(false);
			_useOFAForReady.setEnabled(false);
			_useSLAForReady.setEnabled(false);
		}
	}
}

// End of File
