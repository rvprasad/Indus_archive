
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

import edu.ksu.cis.indus.tools.ToolConfiguration;
import edu.ksu.cis.indus.tools.ToolConfigurator;
import edu.ksu.cis.indus.transformations.slicer.SlicingEngine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
public class SlicerConfigurator
  extends ToolConfigurator {
	/**
	 * The configuration which can be edited via this editor.
	 */
	SlicerConfiguration configuration;

	/**
	 * Creates a new SlicerConfigurator object.
	 */
	SlicerConfigurator() {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class PropertySelectionListener
	  implements SelectionListener {
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final Button button;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final Object id;

		/**
		 * Creates a new BooleanSelectionListener object.
		 *
		 * @param propID DOCUMENT ME!
		 * @param sender DOCUMENT ME!
		 */
		PropertySelectionListener(final Object propID, final Button sender) {
			id = propID;
			button = sender;
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(final SelectionEvent evt) {
			widgetSelected(evt);
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(final SelectionEvent arg0) {
			configuration.setProperty(id, Boolean.valueOf(button.getSelection()));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.tools.ToolConfigurator#dispose()
	 */
	public void disposeTemplateMethod() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.tools.ToolConfigurator#hide()
	 */
	public void hide() {
		configuration = null;
	}

	/**
	 * @see edu.ksu.cis.indus.tools.ToolConfigurator#displayTemplateMethod(edu.ksu.cis.indus.tools.ToolConfiguration)
	 */
	protected void displayTemplateMethod(final ToolConfiguration config) {
		configuration = (SlicerConfiguration) config;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param composite DOCUMENT ME!
	 */
	protected void initialize(final Composite composite) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);

		// Slice-for-deadlock button
		Button button = new Button(composite, SWT.CHECK);
		button.setText("Slice for Deadlock");

		GridData oneSpanHorzBegin = new GridData();
		oneSpanHorzBegin.horizontalSpan = 1;
		oneSpanHorzBegin.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.SLICE_FOR_DEADLOCK)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.SLICE_FOR_DEADLOCK, button));

		// Slice type related group
		Group group = new Group(composite, SWT.NONE);
		group.setText("Slice Type");

		GridData oneSpanHorzFill = new GridData();
		oneSpanHorzFill.horizontalSpan = 1;
		oneSpanHorzFill.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;
		group.setLayoutData(oneSpanHorzFill);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		group.setLayout(gridLayout);

		final Button BW_SLICE = new Button(group, SWT.RADIO);
		BW_SLICE.setText("Backward slice");

		final Button CMPLT_SLICE = new Button(group, SWT.RADIO);
		CMPLT_SLICE.setText("Complete slice");

		SelectionListener sl =
			new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					Object value = null;

					if (evt.item == BW_SLICE) {
						value = SlicingEngine.BACKWARD_SLICE;
					} else if (evt.item == CMPLT_SLICE) {
						value = SlicingEngine.COMPLETE_SLICE;
					}

					if (value != null) {
						configuration.setProperty(SlicerConfiguration.SLICE_TYPE, value);
					}
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			};
		BW_SLICE.addSelectionListener(sl);
		CMPLT_SLICE.addSelectionListener(sl);

		// Interference dependence related group
		group = new Group(composite, SWT.NONE);
		group.setText("Interference dependence");
		group.setLayoutData(oneSpanHorzFill);

		button = new Button(group, SWT.TOGGLE);
		button.setText("use equivalence class-based analysis");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INTERFERENCEDA))
			  .booleanValue());
		button.addSelectionListener(new PropertySelectionListener(
				SlicerConfiguration.EQUIVALENCE_CLASS_BASED_INTERFERENCEDA,
				button));

		// Divergence dependence related group
		group = new Group(composite, SWT.NONE);
		group.setText("Divergence dependence");
		group.setLayoutData(oneSpanHorzFill);

		button = new Button(group, SWT.TOGGLE);
		button.setText("use divergence dependence");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.USE_DIVERGENCEDA)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.USE_DIVERGENCEDA, button));

		button = new Button(group, SWT.TOGGLE);
		button.setText("use interprocedural variant");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA))
			  .booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.INTERPROCEDURAL_DIVERGENCEDA, button));

		// Ready dependence related group
		group = new Group(composite, SWT.NONE);
		group.setText("Ready dependence");
		group.setLayoutData(oneSpanHorzFill);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		group.setLayout(gridLayout);

		button = new Button(group, SWT.TOGGLE);
		button.setText("use ready dependence");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.USE_READYDA)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.USE_READYDA, button));

		button = new Button(group, SWT.TOGGLE);
		button.setText("use equivalence class-based analysis");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_READYDA))
			  .booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.EQUIVALENCE_CLASS_BASED_READYDA, button));

		button = new Button(group, SWT.TOGGLE);
		button.setText("use rule 1 of ready dependence");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA, button));

		button = new Button(group, SWT.TOGGLE);
		button.setText("use rule 2 of ready dependence");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA, button));

		button = new Button(group, SWT.TOGGLE);
		button.setText("use rule 3 of ready dependence");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA, button));

		button = new Button(group, SWT.TOGGLE);
		button.setText("use rule 4 of ready dependence");
		button.setLayoutData(oneSpanHorzBegin);
		button.setSelection(((Boolean) configuration.getProperty(SlicerConfiguration.USE_RULE1_IN_READYDA)).booleanValue());
		button.addSelectionListener(new PropertySelectionListener(SlicerConfiguration.USE_RULE1_IN_READYDA, button));
	}
}

/*
   ChangeLog:
   $Log$
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
