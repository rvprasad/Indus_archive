
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

/*
 * Created on May 24, 2004
 *
 * Displays the configuration choose dialog
 *
 */
package edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs;

import com.thoughtworks.xstream.XStream;

import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata.ViewConfiguration;
import edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata.ViewData;


import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;






/**
 * The viewdialog.
 * 
 *
 * @author Ganeshan
 */
public class ViewDialog
  extends Dialog {
	
	/**
	 * Buttons for control, data, interference, data and synchronization.
	 */
	private Button btnControl, btnData, btnInterference, btnReady, btnSync;
	
	/**
	 *  index for editing purposes.
	 */
	private int index;
	
	/**
	 * The constructor.
	 * @param editindex The index for editing the view.
	 * @param parent The parent control
	 */
	public ViewDialog(final Shell parent, final int editindex) {
		super(parent);
		this.index = editindex;
	}

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("ViewDialog.0")); //$NON-NLS-1$
	}

	/**
	 * Creates the dialog area .
	 *
	 * @param parent The parent control
	 *
	 * @return Control The new control
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite _composite = (Composite) super.createDialogArea(parent);
		final GridLayout _gr = new GridLayout(); 
		_gr.numColumns = 2;
		_composite.setLayout(_gr);
		
		
		
		final Composite _sub = new Composite(_composite, SWT.NONE);
		final GridData _lData = new GridData();
		_lData.horizontalSpan = 2;		
		_sub.setLayoutData(_lData);
		final RowLayout _rl = new RowLayout(SWT.VERTICAL);
		_sub.setLayout(_rl);
		
		btnControl = new Button(_sub, SWT.CHECK);
		btnControl.setText(Messages.getString("ViewDialog.1")); //$NON-NLS-1$
		
		btnData = new Button(_sub, SWT.CHECK);
		btnData.setText(Messages.getString("ViewDialog.2")); //$NON-NLS-1$
		
		btnInterference = new Button(_sub, SWT.CHECK);
		btnInterference.setText(Messages.getString("ViewDialog.3")); //$NON-NLS-1$
		
		btnReady = new Button(_sub, SWT.CHECK);
		btnReady.setText(Messages.getString("ViewDialog.4")); //$NON-NLS-1$
		
		btnSync = new Button(_sub, SWT.CHECK);
		btnSync.setText(Messages.getString("ViewDialog.5")); //$NON-NLS-1$
		if (index != -1) {
			final String _viewname = Messages.getString("ViewDialog.6"); //$NON-NLS-1$
			final IPreferenceStore _ps = SliceEclipsePlugin.getDefault().getPreferenceStore();			
			final String _prefval = _ps.getString(_viewname);
			ViewConfiguration _vc = null;
			if (!_prefval.equals("")) { //$NON-NLS-1$
				final XStream _xstream = new XStream();
				_xstream.alias(Messages.getString("ViewDialog.8"), ViewConfiguration.class);				 //$NON-NLS-1$
				_vc = (ViewConfiguration) _xstream.fromXML(_prefval);			
				final ViewData _vd = (ViewData) _vc.getList().get(index);
				btnControl.setSelection(_vd.isControl());
				btnData.setSelection(_vd.isData());
				btnInterference.setSelection(_vd.isInterference());
				btnReady.setSelection(_vd.isReady());
				btnSync.setSelection(_vd.isSynchronization());				
			}
			
		}
		
		return _composite;
	}
	
	/** Ok is pressed.
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		
			final String _viewname = Messages.getString("ViewDialog.9"); //$NON-NLS-1$
			final IPreferenceStore _ps = SliceEclipsePlugin.getDefault().getPreferenceStore();
			final String _prefval = _ps.getString(_viewname);
			final ViewData _vd = new ViewData();
			ViewConfiguration _vc = null;
			final XStream _xstream = new XStream();
			_xstream.alias(Messages.getString("ViewDialog.10"), ViewConfiguration.class); //$NON-NLS-1$
			if (_prefval.equals("")) { //$NON-NLS-1$
			_vc = new ViewConfiguration();
			_vc.setList(new ArrayList());
		} else {
			_vc = (ViewConfiguration) _xstream.fromXML(_prefval);
		}
			
			_vd.setControl(btnControl.getSelection());
			_vd.setData(btnData.getSelection());
			_vd.setInterference(btnInterference.getSelection());
			_vd.setReady(btnReady.getSelection());
			_vd.setSynchronization(btnSync.getSelection());
			if (isDuplicate(_vc, _vd)) {
			MessageDialog
					.openInformation(
							null,
							Messages.getString("ViewDialog.error"),
							Messages.getString("ViewDialog.13")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
			if (index == -1) {
			_vc.getList().add(_vd);
		} else {
			_vc.getList().set(index, _vd);
		}
				
			final String _value = _xstream.toXML(_vc);			
			_ps.setValue(_viewname, _value);
			SliceEclipsePlugin.getDefault().savePluginPreferences();
		super.okPressed();
	}

	/**
	 * Returns true if the view created is duplicate.
	 * @param vc The View Configuration
	 * @param vd The View Data
	 * @return boolean Whether the view is duplicate or not.
	 */
	private boolean isDuplicate(final ViewConfiguration vc, final ViewData vd) {
		boolean _result = false;
		final java.util.List _lst = vc.getList();
		for (int _i = 0; _i < _lst.size(); _i++) {
			final ViewData _data = (ViewData) _lst.get(_i);
			if (vd.equals(_data)) {
				_result = true; break;
			}
		}
		return _result;
	}
}
