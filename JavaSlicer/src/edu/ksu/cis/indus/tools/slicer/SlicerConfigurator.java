
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.slicer.SlicingEngine;

import edu.ksu.cis.indus.tools.AbstractToolConfigurator;
import edu.ksu.cis.indus.tools.IToolConfiguration;

import org.eclipse.swt.SWT;

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
	 * Creates a new SlicerConfigurator object.
	 */
	SlicerConfigurator() {
	}

	/**
	 * Checks if <code>toolConfiguration</code> can be handled by this configurator.
	 *
	 * @param toolConfiguration is the configuration to be check.
	 *
	 * @throws RuntimeException when <code>toolConfiguration</code> is an unhandled type of exception.
	 *
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfigurator#checkConfiguration(AbstractToolConfiguration)
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

		// Slice-for-deadlock button
		GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData.horizontalSpan = 1;

		Button _button = new Button(parent, SWT.CHECK);
		_button.setText("Slice for Deadlock");
		_button.setLayoutData(_gridData);
		_button.setSelection(_cfg.sliceForDeadlock);

		SelectionListener _sl = new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, _button, _cfg);
		_button.addSelectionListener(_sl);

		_gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData.horizontalSpan = 1;

		_button = new Button(parent, SWT.CHECK);
		_button.setText("Executable slice");
		_button.setLayoutData(_gridData);
		_button.setSelection(_cfg.sliceForDeadlock);
		_sl = new BooleanPropertySelectionListener(SlicerConfiguration.EXECUTABLE_SLICE, _button, _cfg);
		_button.addSelectionListener(_sl);

		setupRow2();
		setupRow3();
		setupRow4();
		parent.pack();
	}

	/**
	 * Sets up row 2 corresponding to Slice type and Interference DA in the configurator composite.
	 */
	private void setupRow2() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Slice type related group
		Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Slice Type");

		GridData _gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_gridData.horizontalSpan = 1;
		_group.setLayoutData(_gridData);

		RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_group.setLayout(_rowLayout);

		final Button _backwardSlice = new Button(_group, SWT.RADIO);
		_backwardSlice.setText("Backward slice");

		final Button _forwardSlice = new Button(_group, SWT.RADIO);
		_forwardSlice.setText("Forward slice");

		final Button _completeSlice = new Button(_group, SWT.RADIO);
		_completeSlice.setText("Complete slice");

		SelectionListener _sl =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object _value = null;

					if (evt.widget == _backwardSlice) {
						_value = SlicingEngine.BACKWARD_SLICE;
					} else if (evt.widget == _completeSlice) {
						_value = SlicingEngine.COMPLETE_SLICE;
					} else if (evt.widget == _forwardSlice) {
						_value = SlicingEngine.FORWARD_SLICE;
					}

					if (_value != null) {
						_cfg.setProperty(SlicerConfiguration.SLICE_TYPE, _value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		_backwardSlice.addSelectionListener(_sl);
		_completeSlice.addSelectionListener(_sl);
		_forwardSlice.addSelectionListener(_sl);

		Object _temp = _cfg.getSliceType();

		if (_temp.equals(SlicingEngine.BACKWARD_SLICE)) {
			_backwardSlice.setSelection(true);
		} else if (_temp.equals(SlicingEngine.COMPLETE_SLICE)) {
			_completeSlice.setSelection(true);
		} else if (_temp.equals(SlicingEngine.FORWARD_SLICE)) {
			_forwardSlice.setSelection(true);
		}

		// Interference dependence related group
		_gridData = new GridData(GridData.FILL_HORIZONTAL);
		_gridData.horizontalSpan = 1;
		_group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Precision of Interference dependence");
		_group.setLayoutData(_gridData);
		_rowLayout = new RowLayout();
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
		_sl = new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, _useOFAForInterference, _cfg);
		_useOFAForInterference.addSelectionListener(_sl);

		_sl = new SelectionListener() {
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
		_equivalenceClassEscapeAnalysisBasedIDA.addSelectionListener(_sl);
		_symbolBasedEscapeAnalysisBasedIDA.addSelectionListener(_sl);
		_typedIDA.addSelectionListener(_sl);

		_temp = _cfg.getProperty(SlicerConfiguration.NATURE_OF_INTERFERENCE_DA);

		if (_temp == null || _temp.equals(SlicerConfiguration.SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			_symbolBasedEscapeAnalysisBasedIDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INFO)) {
			_equivalenceClassEscapeAnalysisBasedIDA.setSelection(true);
		} else if (_temp.equals(SlicerConfiguration.TYPE_BASED_INFO)) {
			_typedIDA.setSelection(true);
		}

		final Boolean _bool = (Boolean) _cfg.getProperty(SlicerConfiguration.USE_RULE4_IN_READYDA);
		_useOFAForInterference.setSelection(_bool.booleanValue());
	}

	/**
	 * Sets up row 3 corresponding to Divergence DA in the configurator composite.
	 */
	private void setupRow3() {
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
			_interProceduralDivergenceDAButton.setSelection(((Boolean) _cfg.getProperty(
					SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA)).booleanValue());
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
	 * Sets up row 4 corresponding to Ready DA in the configurator composite.
	 */
	private void setupRow4() {
		final SlicerConfiguration _cfg = (SlicerConfiguration) configuration;

		// Ready dependence related group
		final Group _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		_group.setText("Ready dependence");

		GridData _twoSpanHorzFill = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_group.setLayoutData(_twoSpanHorzFill);

		GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 2;
		_group.setLayout(_gridLayout);

		GridData _gridData = new GridData(GridData.FILL_HORIZONTAL);
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
		_sl = new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, _useOFAForReady, _cfg);
		_useOFAForReady.addSelectionListener(_sl);

		final Composite _readyComposite = new Composite(_group, SWT.NONE);
		_twoSpanHorzFill = new GridData(GridData.FILL_HORIZONTAL);
		_twoSpanHorzFill.horizontalSpan = 2;
		_readyComposite.setLayoutData(_twoSpanHorzFill);
		_gridLayout = new GridLayout();
		_gridLayout.numColumns = 2;
		_readyComposite.setLayout(_gridLayout);
		_gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		_gridData.horizontalSpan = 2;
		_readyComposite.setLayoutData(_gridData);

		final Button _useRDAButton = new Button(_readyComposite, SWT.CHECK);
		_useRDAButton.setText("use ready dependence");
		_useRDAButton.setSelection(((Boolean) _cfg.getProperty(SlicerConfiguration.USE_READYDA)).booleanValue());
		_useRDAButton.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_READYDA,
				_useRDAButton, _cfg));

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
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.20  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.19  2003/12/02 11:32:01  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.18  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.17  2003/11/06 05:21:49  venku
   - documentation.
   Revision 1.16  2003/11/05 08:26:42  venku
   - changed the xml schema for the slicer configuration.
   - The configruator, driver, and the configuration handle
     these changes.
   Revision 1.15  2003/11/03 08:05:34  venku
   - lots of changes
     - changes to get the configuration working with JiBX
     - changes to make configuration amenable to CompositeConfigurator
     - added EquivalenceClassBasedAnalysis
     - added fix for Thread's start method
   Revision 1.14  2003/10/21 06:07:01  venku
   - added support for executable slice.
   Revision 1.13  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.12  2003/10/20 13:55:25  venku
   - Added a factory to create new configurations.
   - Simplified AbstractToolConfigurator methods.
   - The driver manages the shell.
   - Got all the gui parts running EXCEPT for changing
     the name of the configuration.
   Revision 1.11  2003/10/14 05:36:12  venku
   - implemented checkConfiguration().
   Revision 1.10  2003/10/14 02:58:21  venku
   - ripple effect of changes to AbstractToolConfigurator.
   Revision 1.9  2003/10/13 01:01:45  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.8  2003/09/29 04:20:30  venku
   - coding convention.
   Revision 1.7  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.6  2003/09/27 01:09:35  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.5  2003/09/26 15:30:39  venku
   - removed PropertyIdentifier class.
   - ripple effect of the above change.
   - formatting
   Revision 1.4  2003/09/26 15:08:02  venku
   - completed support for exposing slicer as a tool
     and configuring it both in Bandera and outside it.
   Revision 1.3  2003/09/26 07:33:29  venku
   - checkpoint commit.
   Revision 1.2  2003/09/26 05:55:28  venku
   - a checkpoint commit. Also a cvs fix commit.
   Revision 1.1  2003/09/24 07:32:23  venku
   - Created an implementation of indus tool api specific to Slicer.
     The GUI needs to be setup and bandera adapter needs to be fixed.
 */
