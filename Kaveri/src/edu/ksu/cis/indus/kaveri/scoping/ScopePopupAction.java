/*
 * Created on Jan 6, 2005
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.scoping;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * @author ganeshan
 *  
 */
public class ScopePopupAction implements IObjectActionDelegate {

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
			final IMethod _method = (IMethod) _ssl.getFirstElement();
			KaveriPlugin.getDefault().getIndusConfiguration().addToScopeMap(_method);
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