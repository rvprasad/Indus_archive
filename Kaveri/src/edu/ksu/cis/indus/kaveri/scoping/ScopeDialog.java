/*
 * Created on Jan 25, 2005
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.scoping;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @author ganeshan
 * Displayes the scope selection dialog. The set of enabled
 * scope elements is maintained by the plugin preference. The
 * purpose of this dialog is to enable the user to pick the 
 * relevant scope elements for an instance of the slice.
 *
 */
public class ScopeDialog extends Dialog {

	/**
	 * The checkbox viewer
	 */
	private CheckboxTableViewer tv;
	
	/**
	 * Constructor.
	 * @param parentShell
	 */
	public ScopeDialog(Shell parentShell) {
		super(parentShell);		
	}
	
	

	/** Set the title.
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Scope Selection");
	}
	
	
	/*
	_comp.setLayout(new GridLayout(1, true));
	final Group _grp = new Group(_comp, SWT.BORDER);
	_grp.setText("Pick the elements to be included in the scope");
	_grp.setLayout(new GridLayout(1, true));		
	
	GridData _gd = new GridData();
	_gd.grabExcessHorizontalSpace = true;
	_gd.horizontalSpan = 1;
	_gd.horizontalAlignment = GridData.FILL;
	_gd.grabExcessVerticalSpace = true;
	_gd.verticalAlignment = GridData.FILL;
	_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
	_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
	_grp.setLayoutData(_gd);
	
	
	final Table _table = new Table(_grp, SWT.SINGLE | SWT.V_SCROLL | SWT.CHECK);
	_table.setLinesVisible(true);
	_table.setHeaderVisible(true);
	
	_gd = new GridData();
	_gd.horizontalSpan =1;
	_gd.grabExcessHorizontalSpace = true;
	_gd.grabExcessVerticalSpace = true;
	_gd.horizontalAlignment = GridData.FILL;
	_gd.verticalAlignment = GridData.FILL;
	
	
	
	_table.setLayoutData(_gd);
	
	final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
	_col1.setText("!");
	
	final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
	_col2.setText("Type");
	
	final TableColumn _col3 = new TableColumn(_table, SWT.NONE);
	_col3.setText("Scope Name");
	
	final TableColumn _col4 = new TableColumn(_table, SWT.NONE);
	_col4.setText("Element Name");
	
	
	final TableItem _tt = new TableItem(_table, SWT.NONE);
	_tt.setText(new String[]{"", "Class", "myScope", "myPackage.Readers"});
	
	final TableColumn _tc[] = _table.getColumns();
	for (int i = 0; i < _tc.length; i++) {
		_tc[i].pack();
	}
	
	_comp.addControlListener(
			new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					final TableColumn _cols[] = _table.getColumns();
					for (int i = 0; i < _cols.length; i++) {
						_cols[i].pack();
					}
				}
			}
			);
	
	*/
	
	/** Create the Dialog parts.
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		final Composite _comp = (Composite) super.createDialogArea(parent);
		_comp.setLayout(new GridLayout(1, true));
		
		final Group _grp = new Group(_comp, SWT.BORDER);
		_grp.setText("Pick the elements to be included in the scope");
		_grp.setLayout(new GridLayout(1, true));		
		
		GridData _gd = new GridData();
		_gd.grabExcessHorizontalSpace = true;
		_gd.horizontalSpan = 1;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.grabExcessVerticalSpace = true;
		_gd.verticalAlignment = GridData.FILL;
		_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
		_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
		_grp.setLayoutData(_gd);
		
		tv = CheckboxTableViewer.newCheckList(_grp, SWT.NONE);		
		final Table _table = tv.getTable();
		_table.setLinesVisible(true);
		_table.setHeaderVisible(true);		
		setupTable(_table);
		setupEditors(tv);
		
		_gd = new GridData();
		_gd.horizontalSpan =1;
		_gd.grabExcessHorizontalSpace = true;
		_gd.grabExcessVerticalSpace = true;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.verticalAlignment = GridData.FILL;
		_table.setLayoutData(_gd);
		tv.setContentProvider(new ViewContentProvider());
		tv.setLabelProvider(new ViewLabelProvider());
		tv.setInput("Input");
		
		_comp.addControlListener(
				new ControlAdapter() {
					public void controlResized(ControlEvent e) {
						final TableColumn _cols[] = _table.getColumns();
						for (int i = 0; i < _cols.length; i++) {
							_cols[i].pack();
						}
					}
				}
				);
		
		return _comp;
	}



	/**
	 * Setup the cell editors
	 * @param viewer
	 */
	private void setupEditors(TableViewer viewer) {		
		final Table _table = viewer.getTable();
		final CellEditor _ces[] = new CellEditor[_table.getColumnCount()];
		_ces[0] = new CheckboxCellEditor(_table);
		_ces[1] = new TextCellEditor(_table);
		((Text) _ces[1].getControl()).setEditable(false);
		_ces[2] = new TextCellEditor(_table);
		((Text) _ces[2].getControl()).setEditable(false);
		_ces[3] = new TextCellEditor(_table);
		((Text) _ces[3].getControl()).setEditable(false);
		viewer.setCellEditors(_ces);
	}



	/**
	 * Setup the table with proper columns
	 * @param table
	 */
	private void setupTable(Table table) {
		final TableColumn _col1 = new TableColumn(table, SWT.CENTER);
		_col1.setText("!");		
		
		final TableColumn _col2 = new TableColumn(table, SWT.NONE);
		_col2.setText("Type");
		
		final TableColumn _col3 = new TableColumn(table, SWT.NONE);
		_col3.setText("Scope Name");
		
		final TableColumn _col4 = new TableColumn(table, SWT.NONE);
		_col4.setText("Element Name");
				
	}
}

class ViewContentProvider implements IStructuredContentProvider {
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	public void dispose() {
	}
	public Object[] getElements(Object parent) {
		return new String[] { "One", "Two", "Three" };
	}
}
class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	public String getColumnText(Object obj, int index) {
		if (index == 0) {
			return "";
		}
		return getText(obj);
	}
	public Image getColumnImage(Object obj, int index) {
		return null;
	}
	public Image getImage(Object obj) {
		return null;
	}
}