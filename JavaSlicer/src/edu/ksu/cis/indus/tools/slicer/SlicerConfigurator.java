
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


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
		final TabFolder _tabFolder = new TabFolder(parent, SWT.TOP | SWT.BORDER);

		final TabItem _sliceInfoTab = new TabItem(_tabFolder, SWT.NONE);
		final Composite _sliceInfoTabComposite = new Composite(_tabFolder, SWT.NONE);
		setupSliceInfoUI(_sliceInfoTabComposite);
		_sliceInfoTabComposite.pack();
		_sliceInfoTab.setControl(_sliceInfoTabComposite);
		_sliceInfoTab.setText("Slice");
		_sliceInfoTab.setToolTipText("Configure slice properties.");

		final TabItem _dependenceDATab = new TabItem(_tabFolder, SWT.NONE);
		final Composite _dependenceDAComposite = new Composite(_tabFolder, SWT.NONE);
		setupDependenceDepUI(_dependenceDAComposite);
		_dependenceDAComposite.pack();
		_dependenceDATab.setControl(_dependenceDAComposite);
		_dependenceDATab.setText("General Dependence");
		_dependenceDATab.setToolTipText("Configure control and synchronization dependences.");

		final TabItem _divergenceDATab = new TabItem(_tabFolder, SWT.NONE);
		final Composite _divergenceDAComposite = new Composite(_tabFolder, SWT.NONE);
		setupDivergenceDepUI(_divergenceDAComposite);
		_divergenceDAComposite.pack();
		_divergenceDATab.setControl(_divergenceDAComposite);
		_divergenceDATab.setText("Divergence");
		_divergenceDATab.setToolTipText("Configure divergence dependences.");

		final TabItem _interferenceDATab = new TabItem(_tabFolder, SWT.NONE);
		final Composite _interferenceDATabComposite = new Composite(_tabFolder, SWT.NONE);
		setupInteferenceDepUI(_interferenceDATabComposite);
		_interferenceDATabComposite.pack();
		_interferenceDATab.setControl(_interferenceDATabComposite);
		_interferenceDATab.setText("Intereference");
		_interferenceDATab.setToolTipText("Configure interference dependences.");

		final TabItem _readyDATab = new TabItem(_tabFolder, SWT.NONE);
		final Composite _readyDATabComposite = new Composite(_tabFolder, SWT.NONE);
		setupReadyDepUI(_readyDATabComposite);
		_readyDATabComposite.pack();
		_readyDATab.setControl(_readyDATabComposite);
		_readyDATab.setText("Ready");
		_readyDATab.setToolTipText("Configure ready dependences.");

		_tabFolder.pack();
		parent.pack();
	}

	/**
	 * Sets up the assertion preservation part of the UI.
	 *
	 * @param composite on which to install the UI.
	 * @param cfg to be used.
	 *
	 * @pre composite != null and cfg != null
	 */
	private void setupAssertionUI(final Composite composite, final SlicerConfiguration cfg) {
		final Group _assertionGroup = new Group(composite, SWT.NONE);
		final GridData _gridData1 = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		_gridData1.horizontalSpan = 3;
		_gridData1.grabExcessHorizontalSpace = true;
		_assertionGroup.setLayoutData(_gridData1);

		final GridLayout _gl1 = new GridLayout(3, false);
		_assertionGroup.setLayout(_gl1);
		_assertionGroup.setText("Preservation of assertions in the system");

		final Button _assertionPreservingSliceButton = new Button(_assertionGroup, SWT.CHECK);
		_assertionPreservingSliceButton.setText("Preserve assertions");
		_assertionPreservingSliceButton.setEnabled(true);

		final GridData _gd2 = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		_gd2.horizontalSpan = 1;
		_assertionPreservingSliceButton.setLayoutData(_gd2);

		final Button _applclasses = new Button(_assertionGroup, SWT.CHECK);
		_applclasses.setText("Preserve assertions in application classes only");
		_applclasses.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.ASSERTIONS_IN_APPLICATION_CLASSES_ONLY,
				_applclasses,
				cfg));
		_applclasses.setSelection(cfg.areAssertionsOnlyInAppClassesConsidered());

		final GridData _gd3 = new GridData(SWT.END, SWT.BEGINNING, false, false);
		_gd3.horizontalSpan = 2;
		_gd3.grabExcessHorizontalSpace = true;
		_applclasses.setLayoutData(_gd3);

		final SelectionListener _sl1 =
			new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_TO_PRESERVE_ASSERTIONS,
				_assertionPreservingSliceButton, cfg) {
				public void widgetSelected(final SelectionEvent evt) {
					final boolean _value = button.getSelection();
					containingConfiguration.setProperty(id, Boolean.valueOf(_value));
					_applclasses.setEnabled(_value);
				}
			};
		_assertionPreservingSliceButton.addSelectionListener(_sl1);

		_assertionPreservingSliceButton.setSelection(cfg.getSliceToPreserveAssertions());
		_assertionPreservingSliceButton.notifyListeners(SWT.Selection, null);
	}

	/**
	 * Sets up the deadlock preservation part of the UI.
	 *
	 * @param composite on which to install the UI.
	 * @param cfg to be used.
	 *
	 * @pre composite != null and cfg != null
	 */
	private void setupDeadlockUI(final Composite composite, final SlicerConfiguration cfg) {
		final Group _deadlockGroup = new Group(composite, SWT.NONE);
		final GridData _gridData1 = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		_gridData1.horizontalSpan = 2;
		_deadlockGroup.setLayoutData(_gridData1);

		final RowLayout _rowLayout1 = new RowLayout();
		_rowLayout1.type = SWT.VERTICAL;
		_rowLayout1.fill = true;
		_deadlockGroup.setLayout(_rowLayout1);
		_deadlockGroup.setText("Preservation of Deadlocking property");

		final Button _button = new Button(_deadlockGroup, SWT.CHECK);
		_button.setText("Preserve Deadlock");

		final Button _applclasses = new Button(_deadlockGroup, SWT.CHECK);
		_applclasses.setText("Preserve sync constructs in application classes only");
		_applclasses.setSelection(cfg.areSynchronizationsOnlyInAppClassesConsidered());
		_applclasses.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.SYNCS_IN_APPLICATION_CLASSES_ONLY,
				_applclasses,
				cfg));

		final Group _group1 = new Group(_deadlockGroup, SWT.SHADOW_ETCHED_IN);
		_group1.setText("Deadlock Criteria Selection Strategy");

		final RowLayout _rowLayout2 = new RowLayout();
		_rowLayout2.type = SWT.VERTICAL;
		_group1.setLayout(_rowLayout2);

		final Button _allSyncStrategy = new Button(_group1, SWT.RADIO);
		_allSyncStrategy.setText("All Synchronization constructs");
		_allSyncStrategy.setToolTipText("Synchronization constructs are preserved independent of any property");

		final Button _escapingSyncStrategy = new Button(_group1, SWT.RADIO);
		_escapingSyncStrategy.setText("Escaping Sychronization constructs");
		_escapingSyncStrategy.setToolTipText("Only synchronization constructs involving escaping lock object are preserved.");

		final Button _ctxtsensEscapingSyncStrategy = new Button(_group1, SWT.RADIO);
		_ctxtsensEscapingSyncStrategy.setText("Escaping Sychronization constructs with their contexts");
		_ctxtsensEscapingSyncStrategy.setToolTipText("Calling contexs of the preserved synchronization constructs "
			+ "are considered");

		final SelectionListener _sl4 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _ctxtsensEscapingSyncStrategy) {
						_value = SlicerConfiguration.CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS;
					} else if (evt.widget == _escapingSyncStrategy) {
						_value = SlicerConfiguration.ESCAPING_SYNC_CONSTRUCTS;
					} else if (evt.widget == _allSyncStrategy) {
						_value = SlicerConfiguration.ALL_SYNC_CONSTRUCTS;
					}

					if (_value != null) {
						cfg.setProperty(SlicerConfiguration.DEADLOCK_CRITERIA_SELECTION_STRATEGY, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_allSyncStrategy.addSelectionListener(_sl4);
		_escapingSyncStrategy.addSelectionListener(_sl4);
		_ctxtsensEscapingSyncStrategy.addSelectionListener(_sl4);

		final Object _temp = cfg.getDeadlockCriteriaSelectionStrategy();

		if (_temp.equals(SlicerConfiguration.ALL_SYNC_CONSTRUCTS)) {
			_allSyncStrategy.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.ESCAPING_SYNC_CONSTRUCTS)) {
			_escapingSyncStrategy.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS)) {
			_ctxtsensEscapingSyncStrategy.setSelection(true);
		}

		final SelectionListener _sl5 =
			new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, _button, cfg) {
				public void widgetSelected(final SelectionEvent evt) {
					final boolean _value = button.getSelection();
					containingConfiguration.setProperty(id, Boolean.valueOf(_value));
					_group1.setEnabled(_value);
					_applclasses.setEnabled(_value);
					_allSyncStrategy.setEnabled(_value);
					_escapingSyncStrategy.setEnabled(_value);
					_ctxtsensEscapingSyncStrategy.setEnabled(_value);
				}
			};
		_button.addSelectionListener(_sl5);

		_button.setSelection(cfg.getSliceForDeadlock());
		_button.notifyListeners(SWT.Selection, null);
	}

	/**
	 * Sets up tab corresponding to general dependence in the configurator composite.
	 *
	 * @param composite to layout the general dependence configuration widgets.
	 *
	 * @pre composite != null
	 */
	private void setupDependenceDepUI(final Composite composite) {
		final RowLayout _rowLayout = new RowLayout(SWT.VERTICAL);
		composite.setLayout(_rowLayout);

		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		final Button _useNonTerminationSensitiveCDAButton = new Button(composite, SWT.CHECK);
		_useNonTerminationSensitiveCDAButton.setText("use non-termination sensitive control dependence");
		_useNonTerminationSensitiveCDAButton.setToolTipText("Loop behaviors are not preserved");
		_useNonTerminationSensitiveCDAButton.setSelection(((Boolean) _cfg.getProperty(
				SlicerConfiguration.NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE)).booleanValue());
		_useNonTerminationSensitiveCDAButton.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE,
				_useNonTerminationSensitiveCDAButton,
				_cfg));

		final Button _useExplicitExceptionalExitSensitiveCDAButton = new Button(composite, SWT.CHECK);
		_useExplicitExceptionalExitSensitiveCDAButton.setText("consider explicit exceptional exit points");
		_useExplicitExceptionalExitSensitiveCDAButton.setSelection(((Boolean) _cfg.getProperty(
				SlicerConfiguration.EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE)).booleanValue());
		_useExplicitExceptionalExitSensitiveCDAButton.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE,
				_useExplicitExceptionalExitSensitiveCDAButton,
				_cfg));

		final Button _useCommonUncheckedExceptionsCDAButton = new Button(composite, SWT.CHECK);
		_useCommonUncheckedExceptionsCDAButton.setText("consider common unchecked exception based exit points");
		_useCommonUncheckedExceptionsCDAButton.setSelection(((Boolean) _cfg.getProperty(
				SlicerConfiguration.COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD)).booleanValue());
		_useCommonUncheckedExceptionsCDAButton.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD,
				_useCommonUncheckedExceptionsCDAButton,
				_cfg));

		_useExplicitExceptionalExitSensitiveCDAButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent e) {
					final boolean _t = _useExplicitExceptionalExitSensitiveCDAButton.getSelection();
					_useCommonUncheckedExceptionsCDAButton.setEnabled(_t);
				}

				public void widgetDefaultSelected(final SelectionEvent e) {
					widgetSelected(e);
				}
			});

		final Button _useSyncDepButton = new Button(composite, SWT.CHECK);
		_useSyncDepButton.setText("use synchronization dependence");
		_useSyncDepButton.setToolTipText("Use synchronization dependence in calculation of the slice.");
		_useSyncDepButton.setSelection(_cfg.isSynchronizationDepAnalysisUsed());
		_useSyncDepButton.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.USE_SYNCHRONIZATIONDA,
				_useSyncDepButton,
				_cfg));

		final boolean _t = _cfg.isExplicitExceptionalExitSensitiveControlDependenceUsed();
		_useExplicitExceptionalExitSensitiveCDAButton.setSelection(_t);
		_useExplicitExceptionalExitSensitiveCDAButton.notifyListeners(SWT.Selection, null);
	}

	/**
	 * Sets up row corresponding to Divergence DA in the configurator composite.
	 *
	 * @param composite to layout the divergence dependence configuration widgets.
	 *
	 * @pre composite != null
	 */
	private void setupDivergenceDepUI(final Composite composite) {
		final RowLayout _rowLayout = new RowLayout(SWT.VERTICAL);
		composite.setLayout(_rowLayout);

		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		final Button _useDDAButton = new Button(composite, SWT.CHECK);
		_useDDAButton.setText("use divergence dependence");
		_useDDAButton.setToolTipText("Use divergence dependence in calculation of the slice.");
		_useDDAButton.setSelection(_cfg.isDivergenceDepAnalysisUsed());
		_useDDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_DIVERGENCEDA,
				_useDDAButton, _cfg));

		//Sets up the composite and buttons pertaining to precision control.        
		final Group _natureOfDDAGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		final RowLayout _rowLayout3 = new RowLayout(SWT.VERTICAL);
		_natureOfDDAGroup.setLayout(_rowLayout3);
		_natureOfDDAGroup.setText("Nature of Divergence dependence");

		final Button _intraProceduralDDA = new Button(_natureOfDDAGroup, SWT.RADIO);
		_intraProceduralDDA.setText("intra-procedural only");
		_intraProceduralDDA.setToolTipText("This will not capture dependence on call-sites to divergent methods.");

		final Button _interProceduralDDA = new Button(_natureOfDDAGroup, SWT.RADIO);
		_interProceduralDDA.setText("inter-procedural only");
		_interProceduralDDA.setToolTipText("This will not capture dependence on intra-procedual divergence.");

		final Button _intraAndInterProceduralDDA = new Button(_natureOfDDAGroup, SWT.RADIO);
		_intraAndInterProceduralDDA.setText("intra- and inter-procedural");
		_intraAndInterProceduralDDA.setToolTipText("This will capture both intra- and inter-procedual divergence.");

		final SelectionListener _sl2 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _intraProceduralDDA) {
						_value = SlicerConfiguration.INTRA_PROCEDURAL_ONLY;
					} else if (evt.widget == _interProceduralDDA) {
						_value = SlicerConfiguration.INTER_PROCEDURAL_ONLY;
					} else if (evt.widget == _intraAndInterProceduralDDA) {
						_value = SlicerConfiguration.INTRA_AND_INTER_PROCEDURAL;
					}

					if (_value != null) {
						_cfg.setProperty(SlicerConfiguration.NATURE_OF_DIVERGENCE_DA, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_intraAndInterProceduralDDA.addSelectionListener(_sl2);
		_interProceduralDDA.addSelectionListener(_sl2);
		_intraProceduralDDA.addSelectionListener(_sl2);

		final Object _temp = _cfg.getProperty(SlicerConfiguration.NATURE_OF_DIVERGENCE_DA);

		if (_temp == null || _temp.equals(SlicerConfiguration.INTER_PROCEDURAL_ONLY)) {
			_interProceduralDDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.INTRA_AND_INTER_PROCEDURAL)) {
			_intraAndInterProceduralDDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.INTRA_PROCEDURAL_ONLY)) {
			_intraProceduralDDA.setSelection(true);
		}

		//Links up the buttons via selection listener to control toggling based on the user's decision 
		// to use interference DA.
		_useDDAButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					boolean _val = false;

					if (_useDDAButton.getSelection()) {
						_val = true;
					}
					_natureOfDDAGroup.setEnabled(_val);
					_interProceduralDDA.setEnabled(_val);
					_intraProceduralDDA.setEnabled(_val);
					_intraAndInterProceduralDDA.setEnabled(_val);
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		_useDDAButton.setSelection(_cfg.isDivergenceDepAnalysisUsed());
		_useDDAButton.notifyListeners(SWT.Selection, null);
	}

	/**
	 * Sets up row corresponding Interference DA in the configurator composite.
	 *
	 * @param composite to layout the interference dependence configuration widgets.
	 *
	 * @pre composite != null
	 */
	private void setupInteferenceDepUI(final Composite composite) {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		final GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 2;
		composite.setLayout(_gridLayout);

		final Button _useIDAButton = new Button(composite, SWT.CHECK);
		_useIDAButton.setText("use interference dependence");
		_useIDAButton.setToolTipText("Use interference dependence in calculation of the slice.");
		_useIDAButton.setSelection(_cfg.isInterferenceDepAnalysisUsed());
		_useIDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_INTERFERENCEDA,
				_useIDAButton, _cfg));

		//Sets up the composite and buttons pertaining to precision control.        
		final Group _natureOfIDAGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout3 = new GridLayout();
		_gridLayout3.numColumns = 2;
		_natureOfIDAGroup.setLayout(_gridLayout3);
		_natureOfIDAGroup.setText("Precision of Interference dependence");
		_natureOfIDAGroup.setToolTipText("Controls the precision of interference dependence.");

		final GridData _gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		_gridData1.horizontalSpan = 2;
		_natureOfIDAGroup.setLayoutData(_gridData1);

		final Group _precisionGroup = new Group(_natureOfIDAGroup, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout4 = new GridLayout();
		_gridLayout4.numColumns = 1;
		_precisionGroup.setLayout(_gridLayout4);
		_precisionGroup.setText("Sort of analysis to be used.");
		_precisionGroup.setToolTipText("Controls the precision via  properties inherent to interference.");

		final Button _typedIDA = new Button(_precisionGroup, SWT.RADIO);
		_typedIDA.setText("use type-based analysis");
		_typedIDA.setToolTipText("Only primaries of compatible types will be considered");

		final Button _equivalenceClassBasedEscapeAnalysisBasedIDA = new Button(_precisionGroup, SWT.RADIO);
		_equivalenceClassBasedEscapeAnalysisBasedIDA.setText("use equivalence class-based analysis");
		_equivalenceClassBasedEscapeAnalysisBasedIDA.setToolTipText(
			"Only primaries of belonging to the same equivalence class will be considered.");

		final Button _symbolBasedEscapeAnalysisBasedIDA = new Button(_precisionGroup, SWT.RADIO);
		_symbolBasedEscapeAnalysisBasedIDA.setText("use symbol and equivalence class-based analysis");
		_symbolBasedEscapeAnalysisBasedIDA.setToolTipText(
			"Only symbolically equivalent primaries that belong to the same equivalence class will be considered.");

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
		_useOFAForInterference.setToolTipText("Only aliasing primaries will be considered");
		_useOFAForInterference.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.USE_OFA_FOR_INTERFERENCE_DA,
				_useOFAForInterference,
				_cfg));

		_useOFAForInterference.setSelection(_cfg.isOFAUsedForInterference());

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

		_useOFAForInterference.setSelection(_cfg.isOFAUsedForInterference());
		_useIDAButton.setSelection(_cfg.isInterferenceDepAnalysisUsed());
		_useIDAButton.notifyListeners(SWT.Selection, null);
	}

	/**
	 * Sets up row corresponding to Ready DA in the configurator composite.
	 *
	 * @param composite to layout the ready dependence configuration widgets.
	 *
	 * @pre composite != null
	 */
	private void setupReadyDepUI(final Composite composite) {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		final RowLayout _rowLayout1 = new RowLayout(SWT.VERTICAL);
		composite.setLayout(_rowLayout1);

		final Composite _readyComposite1 = new Composite(composite, SWT.NONE);
		final GridLayout _gridLayout2 = new GridLayout();
		_gridLayout2.numColumns = 2;
		_readyComposite1.setLayout(_gridLayout2);

		final Button _useRDAButton = new Button(_readyComposite1, SWT.CHECK);
		_useRDAButton.setText("use ready dependence");
		_useRDAButton.setToolTipText("Use ready dependence in calculation of the slice.");
		_useRDAButton.setSelection(_cfg.isReadyDepAnalysisUsed());
		_useRDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_READYDA,
				_useRDAButton, _cfg));

		// Sets up the composite and buttons pertaining to precision control.        
		final Group _precisionOfRDAGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout3 = new GridLayout();
		_gridLayout3.numColumns = 2;
		_precisionOfRDAGroup.setLayout(_gridLayout3);
		_precisionOfRDAGroup.setText("Precision of Ready dependence");

		final Group _precisionGroup = new Group(_precisionOfRDAGroup, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout4 = new GridLayout();
		_gridLayout4.numColumns = 1;
		_precisionGroup.setLayout(_gridLayout4);
		_precisionGroup.setText("Sort of analysis to be used.");
		_precisionGroup.setToolTipText("Controls the precision via  properties inherent to interference.");

		// Sets up the buttons that control the nature of ready dependence.
		final Button _typedRDA = new Button(_precisionGroup, SWT.RADIO);
		_typedRDA.setText("use type-based analysis");
		_typedRDA.setToolTipText("Only primaries of compatible types will be considered");

		final Button _equivalenceClassBasedEscapeAnalysisBasedRDA = new Button(_precisionGroup, SWT.RADIO);
		_equivalenceClassBasedEscapeAnalysisBasedRDA.setText("use equivalence class-based analysis");
		_equivalenceClassBasedEscapeAnalysisBasedRDA.setToolTipText(
			"Only primaries belonging to the same equivalence class  will be considered.");

		final Button _symbolBasedEscapeAnalysisBasedRDA = new Button(_precisionGroup, SWT.RADIO);
		_symbolBasedEscapeAnalysisBasedRDA.setText("use sybmol and equivalence class-based analysis");
		_symbolBasedEscapeAnalysisBasedRDA.setToolTipText(
			"Only symbolically equivalent primaries that belong to the same equivalence class will be considered.");

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
		final Composite _analysisComposite = new Composite(_precisionOfRDAGroup, SWT.NONE);
		final RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_analysisComposite.setLayout(_rowLayout);

		final GridData _analysisCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
		_analysisCompositeGridData.verticalAlignment = SWT.TOP;
		_analysisComposite.setLayoutData(_analysisCompositeGridData);

		final Button _useOFAForReady = new Button(_analysisComposite, SWT.CHECK);
		_useOFAForReady.setText("use object flow analysis information");
		_useOFAForReady.setToolTipText("Only aliasing primaries will be considered");
		_useOFAForReady.setSelection(_cfg.isOFAUsedForReady());
		_useOFAForReady.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_OFA_FOR_READY_DA,
				_useOFAForReady, _cfg));

		final Button _useSLAForReady = new Button(_analysisComposite, SWT.CHECK);
		_useSLAForReady.setText("use safe lock analysis ");
		_useSLAForReady.setToolTipText("Safe locks will not considered");
		_useSLAForReady.setSelection(_cfg.isSafeLockAnalysisUsedForReady());
		_useSLAForReady.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_SLA_FOR_READY_DA,
				_useSLAForReady, _cfg));

		final Button _useCallSiteSensitiveReady = new Button(_analysisComposite, SWT.CHECK);
		_useCallSiteSensitiveReady.setText("use call-site sensitive");
		_useCallSiteSensitiveReady.setToolTipText("Calls leading to blocking will be considered as blocking as well.");
		_useCallSiteSensitiveReady.setSelection(_cfg.isCallSiteSensitiveReadyUsed());
		_useCallSiteSensitiveReady.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.CALL_SITE_SENSITIVE_READY_DA,
				_useCallSiteSensitiveReady,
				_cfg));

		// Sets up the buttons that control what rules of ready dependence analysis are used.        
		final Group _natureOfRDAGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		final GridLayout _gridLayout5 = new GridLayout();
		_gridLayout5.numColumns = 2;
		_natureOfRDAGroup.setLayout(_gridLayout5);
		_natureOfRDAGroup.setText("Nature of Ready dependence");

		final Button _rule1RDAButton = new Button(_natureOfRDAGroup, SWT.CHECK);
		_rule1RDAButton.setText("use rule 1 of ready dependence");
		_rule1RDAButton.setToolTipText("Intra-thread intra-procedural monitor acquisition based ready dependence");
		_rule1RDAButton.setSelection(_cfg.isReadyRule1Used());
		_rule1RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule1RDAButton, _cfg));

		final Button _rule2RDAButton = new Button(_natureOfRDAGroup, SWT.CHECK);
		_rule2RDAButton.setText("use rule 2 of ready dependence");
		_rule2RDAButton.setToolTipText("Inter-thread monitor acquisition ready dependence");
		_rule2RDAButton.setSelection(_cfg.isReadyRule2Used());
		_rule2RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule2RDAButton, _cfg));

		final Button _rule3RDAButton = new Button(_natureOfRDAGroup, SWT.CHECK);
		_rule3RDAButton.setText("use rule 3 of ready dependence");
		_rule3RDAButton.setToolTipText("Intra-thread intra-procedural Object.wait() based ready dependence");
		_rule3RDAButton.setSelection(_cfg.isReadyRule3Used());
		_rule3RDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				_rule3RDAButton, _cfg));

		final Button _rule4RDAButton = new Button(_natureOfRDAGroup, SWT.CHECK);
		_rule4RDAButton.setText("use rule 4 of ready dependence");
		_rule4RDAButton.setToolTipText("Inter-thread Object.wait() based ready dependence");
		_rule4RDAButton.setSelection(_cfg.isReadyRule4Used());
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
					_precisionOfRDAGroup.setEnabled(_val);
					_equivalenceClassBasedEscapeAnalysisBasedRDA.setEnabled(_val);
					_typedRDA.setEnabled(_val);
					_useOFAForReady.setEnabled(_val);
					_useSLAForReady.setEnabled(_val);
					_useCallSiteSensitiveReady.setEnabled(_val);
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

		_useRDAButton.setSelection(_cfg.isInterferenceDepAnalysisUsed());
		_useRDAButton.notifyListeners(SWT.Selection, null);
	}

	/**
	 * Sets up row to configure deadlock preserving slicing  and slice type.
	 *
	 * @param composite to layout the slice configuration widgets.
	 *
	 * @pre composite != null
	 */
	private void setupSliceInfoUI(final Composite composite) {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		final GridLayout _gridLayout = new GridLayout(3, false);
		composite.setLayout(_gridLayout);

		executableSliceButton = new Button(composite, SWT.CHECK);
		executableSliceButton.setText("Executable slice");

		final GridData _gridData2 = new GridData();
		_gridData2.horizontalSpan = 2;
		executableSliceButton.setLayoutData(_gridData2);

		if (_cfg.getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			executableSliceButton.setEnabled(false);
		}
		executableSliceButton.setSelection(_cfg.getExecutableSlice());

		final Button _propertyAwareSlicingButton = new Button(composite, SWT.CHECK);
		_propertyAwareSlicingButton.setText("Property Aware Slicing");
		_propertyAwareSlicingButton.setToolTipText("Precise but expensive");

		final GridData _gridData4 = new GridData();
		_gridData4.horizontalSpan = 1;
		_propertyAwareSlicingButton.setLayoutData(_gridData4);
		_propertyAwareSlicingButton.setSelection(_cfg.getPropertyAware());

		final SelectionListener _sl3 =
			new BooleanPropertySelectionListener(SlicerConfiguration.PROPERTY_AWARE, _propertyAwareSlicingButton, _cfg);
		_propertyAwareSlicingButton.addSelectionListener(_sl3);

		setupAssertionUI(composite, _cfg);
		setupDeadlockUI(composite, _cfg);

		//Slice type related group
		final Group _group2 = new Group(composite, SWT.SHADOW_ETCHED_IN);
		_group2.setText("Slice Type");

		final GridData _gridData = new GridData(GridData.FILL_HORIZONTAL);
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

		final SelectionListener _sl6 =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _forwardSlice) {
						_value = SlicingEngine.FORWARD_SLICE;
						executableSliceButton.setSelection(false);
						executableSliceButton.notifyListeners(SWT.Selection, null);
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
		_backwardSlice.addSelectionListener(_sl6);
		_completeSlice.addSelectionListener(_sl6);
		_forwardSlice.addSelectionListener(_sl6);

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
