
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

package edu.ksu.cis.indus.kaveri.views;

import java.util.Stack;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;


/**
 * <p>Partial slice view. This class creates a view that shows 
 * the Jimple statements for a given Java statement and indicates if they have
 * a slice tag or not. This view is only active after slicing has been performed. </p>
 */
public class DependenceHistoryView
  extends ViewPart {
	/** 
	 * <p>
	 * The table viewer for this view.
	 * </p>
	 */
	private TableViewer viewer;
	
	
	
	/**
	 * The constructor.
	 */
	public DependenceHistoryView() {
	}

	/**
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content
	 * 
	 */
	class DependenceHistoryViewContentProvider
	  implements IStructuredContentProvider,
		  IDeltaListener {
		/**
		 * Returns the elements to show in the view.
		 *
		 * @param parent The parent.
		 *
		 * @return Object[] The list of statements if any present.
		 */
		public Object[] getElements(final Object parent) {
			final Stack _stk = KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory().getHistory();
			final Object[] _retObj;
			if (! _stk.isEmpty()) {
				_retObj =  _stk.toArray();
			} else {
				_retObj = new String[] { " ", " ", " ", " ", " ", " ", " ", " ", " " }; 
			}
			return _retObj;
		}

		/**
		 * Dispose any created resources.
		 */
		public void dispose() {
		}

		/**
		 * The input has changed. Register for receiving any changes.
		 *
		 * @param v The current viewer
		 * @param oldInput The old input to the view.
		 * @param newInput The new input to the view.
		 */
		public void inputChanged(final Viewer v, final Object oldInput, 
				final Object newInput) {

			if (oldInput != null) {
				((DependenceHistoryData) oldInput).removeListener(this);
			}

			if (newInput != null) {
				((DependenceHistoryData) newInput).addListener(this);
			}
		}

		/**
		 * The slice statement list has changed. Refresh the view.
		 *
		 * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
		 */
		public void propertyChanged() {
			if (viewer != null) {
				viewer.refresh();				
				final TableColumn _cols[] = viewer.getTable().getColumns();
				for (int _i = 0; _i < _cols.length; _i++) {
					_cols[_i].pack();
				}
			}
		}
	}


	/**
	 * <p>This class provides the labels for the elements shown in the view.</p>
	 * 
	 * 
	 */
	class DependenceHistoryViewLabelProvider
	  extends LabelProvider
	  implements ITableLabelProvider {
		
		/**
		 * Get the image label for the given column.
		 * @param obj The object for which the image is needed
		 * @param index The column
		 * @return Image The image for the given column
		 */
		public Image getColumnImage(final Object obj, final int index) {			
			return getImage(obj);
		}

		/**
		 * Returns the textual representation of the element 
		 * to be shown.
		 *
		 * @param obj The object whose value is to be shown
		 * @param index The column number
		 *
		 * @return String The textual representation of the object
		 */
		public String getColumnText(final Object obj, final int index) {
			String _retString = "";

			if (index == 0) {
				if (obj instanceof Pair) {
				_retString = ((Pair) obj).getFirst().toString();
				}
			} else {
				if (obj instanceof Pair) {
				_retString = ((Pair) obj).getSecond().toString();
				}
			}			
			return _retString;
		}

		/**
		 * Returns the image label for the given object.
		 * @param obj The object for which the image label is needed.
		 * @return Image The image for the given object.
		 */
		public Image getImage(final Object obj) {			
			return null;
		}

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 *
	 * @param parent The parent control
	 */
	public void createPartControl(final Composite parent) {
		final Composite _comp = new Composite(parent, SWT.NONE);
		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 2;		
		_layout.horizontalSpacing = 10;
		_layout.marginWidth = 10;
		_comp.setLayout(_layout);
		
				
		final Table _table = createTable(_comp);
		final GridData _data = new GridData();		
		_data.horizontalSpan = 2;
		_data.horizontalAlignment = GridData.FILL_BOTH;
		_data.grabExcessHorizontalSpace = true;
		//_data.grabExcessVerticalSpace = true;
		_table.setLayoutData(_data);
		
		_comp.addControlListener(
				new ControlAdapter() {
					public void controlResized(ControlEvent e)
					{
						TableColumn _col1 = _table.getColumn(0);
						_col1.setWidth(_comp.getSize().x / 2 );
						_col1 = _table.getColumn(1);
						_col1.setWidth(_comp.getSize().x / 2);						
					}
				}
				);
		
		viewer = new TableViewer(_table);								
								
		
		//viewer = new CheckboxTableViewer(_table);
		viewer.setContentProvider(new DependenceHistoryViewContentProvider());
		viewer.setLabelProvider(new DependenceHistoryViewLabelProvider());
		viewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory());
		 
	}

	/**
	 * @param _table
	 */
	/*
	private void updateTable(Table _table) {
		_table.setLinesVisible(true);
		_table.setHeaderVisible(true);

		final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
		_col1.setText("Statement");
		
		final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
		_col2.setText("Part of Slice");
		_col1.pack();
		_col2.pack();
	}*/

	/**
	 * Creates the table.
	 *
	 * @param parent The parent composite
	 *
	 * @return Table The table
	 */
	private Table createTable(final Composite parent) { 
		final Table _table = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL 
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		_table.setLinesVisible(true);
		_table.setHeaderVisible(true);				
		
		final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
		_col1.setText("Statement");
		
		
		final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
		_col2.setText("Dependence Tracked");		
		
		return _table;
	}
}
