
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

import edu.ksu.cis.indus.common.soot.NamedTag;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.preferencedata.IDeltaListener;
import edu.ksu.cis.indus.kaveri.preferencedata.PartialStmtData;

import java.util.List;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.ui.part.ViewPart;

import soot.jimple.Stmt;


/**
 * <p>Partial slice view. This class creates a view that shows 
 * the Jimple statements for a given Java statement and indicates if they have
 * a slice tag or not. This view is only active after slicing has been performed. </p>
 */
public class PartialSliceView
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
	public PartialSliceView() {
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
	class ViewContentProvider
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
			final List _lst = KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().getStmtList();
			final Object[] _retObj;
			if (_lst != null) {
				_retObj =  _lst.toArray();
			} else {
				_retObj = new String[] { "No slice view present" }; 
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
				((PartialStmtData) oldInput).removeListener(this);
			}

			if (newInput != null) {
				((PartialStmtData) newInput).addListener(this);
			}
		}

		/**
		 * The slice statement list has changed. Refresh the view.
		 *
		 * @see edu.ksu.cis.indus.kaveri.preferencedata.IDeltaListener#propertyChanged()
		 */
		public void propertyChanged() {
			if (viewer != null) {
				viewer.refresh();
			}
		}
	}


	/**
	 * <p>This class provides the labels for the elements shown in the view.</p>
	 * 
	 * 
	 */
	class ViewLabelProvider
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
				_retString = getText(obj);
			} else {
				if (obj instanceof Stmt) {					
					_retString = "" + isSliceTagPresent((Stmt) obj);
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

		/**
		 * Indicates if the given Jimple statement has the slice tag.
		 *
		 * @param stmt The jimple statement for which the presence 
		 * of the tag is to be tested
		 *
		 * @return boolean Whether the statement has the tag or not.
		 */
		private boolean isSliceTagPresent(final Stmt stmt) {
			boolean _btagpresent = false;
			final NamedTag _sTag = (NamedTag) stmt.getTag("EclipseIndusTag");

			if (_sTag != null) {
				_btagpresent = true;
			}
			return _btagpresent;
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
		final Table _table = createTable(parent);
		viewer = new TableViewer(_table);

		//viewer = new CheckboxTableViewer(_table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getStmtList());
	}

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
		final int _stmtWidth = 400;
		final int  _infoWidth = 100;
		_col1.setWidth(_stmtWidth);

		final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
		_col2.setText("Part of Slice");
		_col2.setWidth(_infoWidth);
		return _table;
	}
}
