/*
 * Created on Jan 6, 2005
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.scoping;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.thoughtworks.xstream.XStream;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

/**
 * @author ganeshan
 *  
 */
public class ScopePopupAction implements IObjectActionDelegate {

    private String scopePreferenceId = "edu.ksu.cis.indus.scope";
    
	private ISelection selection;
	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection _ssl = (IStructuredSelection) selection;
			final Object _retObj = _ssl.getFirstElement();
			
			String _scopeSpec = KaveriPlugin.getDefault().
				getPreferenceStore().getString(scopePreferenceId);
			
			if(_scopeSpec.equals("")) {
			    final ScopeDataSet _sd = new ScopeDataSet();
			    final XStream _xstream = new XStream();
			    _xstream.alias("ScopeDataSet", ScopeDataSet.class);
			    final String _scopeXML = _xstream.toXML(_sd);
			    KaveriPlugin.getDefault().getPreferenceStore()
			    	.setValue(scopePreferenceId, _scopeXML);
			    _scopeSpec = _scopeXML;
			}
	
			final XStream _xstream = new XStream();
			_xstream.alias("ScopeDataSet", ScopeDataSet.class);
			final ScopeDataSet _sd = (ScopeDataSet) _xstream.fromXML(_scopeSpec);
			
			if (_retObj instanceof IMethod) {			    
				final IMethod _method = (IMethod) _retObj;
				final String _defaultMessage = "Method_" + _method.getElementName();
				final ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
				        Display.getCurrent().getActiveShell(), IScopeDialogMorphConstants.SCOPE_NAME_ONLY,
				        "Method_" + _method.getElementName());
				if (_spsd.open() == IDialogConstants.OK_ID) {
				    final String _scopeName = _spsd.getStrScopeName();
				    try {
				    final String _methodScopeSpec = ScopeDefinitionHelper.getScopeDefinition(_method, _scopeName);
				    _sd.addMethodScope(_methodScopeSpec);
				    } catch(JavaModelException _jme) {
				        SECommons.handleException(_jme);
				    }
				}
				
				
			
			}
			if  (_retObj instanceof IField) {
			    final IField _field = (IField) _retObj;
			    final ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
				        Display.getCurrent().getActiveShell(), IScopeDialogMorphConstants.SCOPE_NAME_ONLY,
				        "Field_" + _field.getElementName());
			    
				if (_spsd.open() == IDialogConstants.OK_ID) {
				    final String _scopeName = _spsd.getStrScopeName();
				    try {
				    final String _fieldScopeSpec = ScopeDefinitionHelper.getScopeDefinition(_field, _scopeName);
				    _sd.addFieldScope(_fieldScopeSpec);
				    } catch(JavaModelException _jme) {
				        SECommons.handleException(_jme);
				    }
				}
			}
			if (_retObj instanceof IType) {
			    final IType _type = (IType) _retObj;
			    final String _defaultMessage = "Class_" + _type.getElementName();
			    
			    final ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
				        Display.getCurrent().getActiveShell(), IScopeDialogMorphConstants.SCOPE_NAME_PROP,
				        "Class_" + _type.getElementName());
			    
				if (_spsd.open() == IDialogConstants.OK_ID) {
				    final String _scopeName = _spsd.getStrScopeName();
				    final String _classSpec = ScopeDefinitionHelper.getScopeDefinition(_type, _scopeName, _spsd.getStrChoice());
					    _sd.addClassScope(_classSpec);
				    
				    
				}
			}
			KaveriPlugin.getDefault().getPreferenceStore().
			setValue(scopePreferenceId, _xstream.toXML(_sd));
			KaveriPlugin.getDefault().savePluginPreferences();
		}
	

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {		
		this.selection = selection;
	}

}