
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
import edu.ksu.cis.indus.tools.AbstractToolConfiguration;
import edu.ksu.cis.indus.tools.AbstractToolConfigurator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfigurator#validateConfiguration(AbstractToolConfiguration)
	 */
	protected void checkConfiguration(final AbstractToolConfiguration toolConfiguration) {
		if (!(toolConfiguration instanceof SlicerConfiguration)) {
			throw new RuntimeException(
				"The toolConfiguration has to be of type edu.ksu.cis.indus.tools.slicer.SlicerConfiguration.");
		}
	}

	/**
	 * {@inheritDoc} This method should be called after <code>setConfiguration</code> has been invoked on this object.
	 */
	protected void setup() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		final SlicerConfiguration SLICER_CONFIG = (SlicerConfiguration) configuration;

		// Slice-for-deadlock button
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;

		Button button = new Button(parent, SWT.CHECK);
		button.setText("Slice for Deadlock");
		button.setLayoutData(gridData);
		button.setSelection(SLICER_CONFIG.sliceForDeadlock);
		button.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, button,
				SLICER_CONFIG));

         gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;

        button = new Button(parent, SWT.CHECK);
        button.setText("Executable slice");
        button.setLayoutData(gridData);
        button.setSelection(SLICER_CONFIG.sliceForDeadlock);
        button.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.EXECUTABLE_SLICE, button,
                        SLICER_CONFIG));

		// Interference dependence related group
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;

		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Interference dependence");
		group.setLayoutData(gridData);

		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.VERTICAL;
		group.setLayout(rowLayout);

		button = new Button(group, SWT.CHECK);
		button.setText("use equivalence class-based analysis");
		button.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INTERFERENCEDA))
			  .booleanValue());
		button.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INTERFERENCEDA,
				button,
				SLICER_CONFIG));

		// Slice type related group
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Slice Type");
		group.setLayoutData(gridData);
		group.setLayout(rowLayout);

		final Button BW_SLICE = new Button(group, SWT.RADIO);
		BW_SLICE.setText("Backward slice");

        final Button FW_SLICE = new Button(group, SWT.RADIO);
        FW_SLICE.setText("Forward slice");
        
        final Button CMPLT_SLICE = new Button(group, SWT.RADIO);
		CMPLT_SLICE.setText("Complete slice");

		SelectionListener sl =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object value = null;

					if (evt.widget == BW_SLICE) {
						value = SlicingEngine.BACKWARD_SLICE;
					} else if (evt.widget == CMPLT_SLICE) {
						value = SlicingEngine.COMPLETE_SLICE;
					}else if (evt.widget == FW_SLICE) {
                        value = SlicingEngine.FORWARD_SLICE;
                    }                    

					if (value != null) {
						SLICER_CONFIG.setProperty(SlicerConfiguration.SLICE_TYPE, value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		BW_SLICE.addSelectionListener(sl);
		CMPLT_SLICE.addSelectionListener(sl);

		if (SLICER_CONFIG.getSliceType().equals(SlicingEngine.BACKWARD_SLICE)) {
			BW_SLICE.setSelection(true);
		} else if (SLICER_CONFIG.getSliceType().equals(SlicingEngine.COMPLETE_SLICE)) {
			CMPLT_SLICE.setSelection(true);
		}

		// Divergence dependence related group
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Divergence dependence");
		group.setLayoutData(gridData);
		group.setLayout(rowLayout);

		final Button USE_DDA_BUTTON = new Button(group, SWT.CHECK);
		USE_DDA_BUTTON.setText("use divergence dependence");
		USE_DDA_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.USE_DIVERGENCEDA)).booleanValue());
		USE_DDA_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_DIVERGENCEDA,
				USE_DDA_BUTTON, SLICER_CONFIG));

		final Button DIVDA_IP_BUTTON = new Button(group, SWT.CHECK);
		DIVDA_IP_BUTTON.setText("use interprocedural variant");

		if (USE_DDA_BUTTON.getSelection()) {
			DIVDA_IP_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(
					SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA)).booleanValue());
		} else {
			DIVDA_IP_BUTTON.setEnabled(false);
		}
		DIVDA_IP_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA,
				DIVDA_IP_BUTTON,
				SLICER_CONFIG));
		USE_DDA_BUTTON.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					if (USE_DDA_BUTTON.getSelection()) {
						DIVDA_IP_BUTTON.setEnabled(true);
					} else {
						DIVDA_IP_BUTTON.setEnabled(false);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		// Ready dependence related group
		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Ready dependence");

		GridData twoSpanHorzFill = new GridData(GridData.FILL_HORIZONTAL);
		twoSpanHorzFill.horizontalSpan = 2;
		group.setLayoutData(twoSpanHorzFill);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		group.setLayout(gridLayout);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);

		final Button USE_RDA_BUTTON = new Button(group, SWT.CHECK);
		USE_RDA_BUTTON.setText("use ready dependence");
		USE_RDA_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.USE_READYDA)).booleanValue());
		USE_RDA_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_READYDA,
				USE_RDA_BUTTON, SLICER_CONFIG));

		final Button RDA_USE_ECBA_BUTTON = new Button(group, SWT.CHECK);
		RDA_USE_ECBA_BUTTON.setText("use equivalence class-based analysis");
		RDA_USE_ECBA_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(
				SlicerConfiguration.EQUIVALENCE_CLASS_BASED_READYDA,
				RDA_USE_ECBA_BUTTON,
				SLICER_CONFIG));

		final Button RDA_R1_BUTTON = new Button(group, SWT.CHECK);
		RDA_R1_BUTTON.setText("use rule 1 of ready dependence");
		RDA_R1_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				RDA_R1_BUTTON, SLICER_CONFIG));

		final Button RDA_R2_BUTTON = new Button(group, SWT.CHECK);
		RDA_R2_BUTTON.setText("use rule 2 of ready dependence");
		RDA_R2_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				RDA_R2_BUTTON, SLICER_CONFIG));

		final Button RDA_R3_BUTTON = new Button(group, SWT.CHECK);
		RDA_R3_BUTTON.setText("use rule 3 of ready dependence");
		RDA_R3_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				RDA_R3_BUTTON, SLICER_CONFIG));

		final Button RDA_R4_BUTTON = new Button(group, SWT.CHECK);
		RDA_R4_BUTTON.setText("use rule 4 of ready dependence");
		RDA_R4_BUTTON.addSelectionListener(new BooleanPropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA,
				RDA_R4_BUTTON, SLICER_CONFIG));
		USE_RDA_BUTTON.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					boolean val = false;

					if (USE_RDA_BUTTON.getSelection()) {
						val = true;
					}
					RDA_USE_ECBA_BUTTON.setEnabled(val);
					RDA_R1_BUTTON.setEnabled(val);
					RDA_R2_BUTTON.setEnabled(val);
					RDA_R3_BUTTON.setEnabled(val);
					RDA_R4_BUTTON.setEnabled(val);
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		if (USE_RDA_BUTTON.getSelection()) {
			RDA_USE_ECBA_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(
					SlicerConfiguration.EQUIVALENCE_CLASS_BASED_READYDA)).booleanValue());
			RDA_R1_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA))
				  .booleanValue());
			RDA_R2_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA))
				  .booleanValue());
			RDA_R3_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA))
				  .booleanValue());
			RDA_R4_BUTTON.setSelection(((Boolean) SLICER_CONFIG.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA))
				  .booleanValue());
		} else {
			RDA_USE_ECBA_BUTTON.setEnabled(false);
			RDA_R1_BUTTON.setEnabled(false);
			RDA_R2_BUTTON.setEnabled(false);
			RDA_R3_BUTTON.setEnabled(false);
			RDA_R4_BUTTON.setEnabled(false);
		}
	}
}

/*
   ChangeLog:
   $Log$
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
