/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.callgraph;

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

/**
 * @author ganeshan
 */
public class AddToContext
		implements IViewActionDelegate {

	/**
	 * The current selection.
	 */
	private IStructuredSelection sSel;

	/**
	 * The callgraph view instance.
	 */
	private CallHierarchyViewPart callViewPart;

	/**
	 * (non-Javadoc).
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(final IViewPart view) {
		this.callViewPart = (CallHierarchyViewPart) view;
	}

	/**
	 * (non-Javadoc).
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		if (sSel != null) {
			final List<?> _o = sSel.toList();
			if (_o.size() == 1) {
				final MethodWrapper _callee = callViewPart.getCurrentMethodWrapper();
				final boolean _callerView = _callee.getClass().getName().indexOf("org.eclipse.jdt.internal.corext.callhierarchy.CallerMethodWrapper") == 0;
				if (!_callerView) {
					MessageDialog.openError(this.callViewPart.getSite().getShell(), "Kaveri - Error",
							"This feature is available only in caller mode of call hierarchy view. ");
				} else {
					final MethodWrapper _caller = (MethodWrapper) sSel.getFirstElement();
					MethodCallContext _context = new MethodCallContext((IMethod) _caller.getMember(), (IMethod) _callee
							.getMember());
					generateCallStacks(_callee, _caller, _context,
							new Stack<Triple<MethodWrapper, MethodWrapper, CallLocation>>(), _callerView);
					final IMethod _mStart = _context.getCallRoot();
					final ICompilationUnit _unit = _mStart.getCompilationUnit();
					if (_unit != null) {
						final IResource _res;
						try {
							_res = _unit.getCorrespondingResource();
						} catch (JavaModelException e) {
							SECommons.handleException(e);
							return;
						}
						if (_res != null) {
							final IProject _prj = _res.getProject();
							if (_prj != null && hasJavaNature(_prj)) {
								KaveriPlugin.getDefault().getIndusConfiguration().addContext(JavaCore.create(_prj), _context);
								KaveriPlugin.getDefault().getIndusConfiguration().getInfoBroadcaster().update();
							}
						}
					}
				}
			} else {
				MessageDialog.openError(this.callViewPart.getSite().getShell(), "Kaveri - Error",
						"You should select two methods" + " (not call-sites) in the call hierarchy view to add a context.");
			}
		}
	}

	/**
	 * Check the java nature.
	 * 
	 * @param _prj
	 * @return
	 */
	private boolean hasJavaNature(IProject prj) {
		boolean _hasNature = false;
		try {
			_hasNature = prj.hasNature("org.eclipse.jdt.core.javanature");
		} catch (CoreException e) {
			_hasNature = false;
		}
		return _hasNature;
	}

	/**
	 * Generate all the call stack traces from the given source and destination methods.
	 * 
	 * @param destinationOfCall DOCUMENT ME!
	 * @param sourceOfCall DOCUMENT ME!
	 * @param context DOCUMENT ME!
	 * @param pathStack DOCUMENT ME!
	 * @param callerView DOCUMENT ME!
	 */
	private void generateCallStacks(final MethodWrapper destinationOfCall, final MethodWrapper sourceOfCall,
			final MethodCallContext context, final Stack<Triple<MethodWrapper, MethodWrapper, CallLocation>> pathStack,
			final boolean callerView) {

		if (callerView) {
			final MethodWrapper _calleeOfSrc = sourceOfCall.getParent();
			if (_calleeOfSrc == null) {
				final Stack<?> _stack = (Stack) pathStack.clone();
				context.addContext(_stack);
			} else {
				for (final Iterator<?> _iter = sourceOfCall.getMethodCall().getCallLocations().iterator(); _iter.hasNext();) {
					final CallLocation _callLoc = (CallLocation) _iter.next();
					final Triple<MethodWrapper, MethodWrapper, CallLocation> _triple = new Triple<MethodWrapper, MethodWrapper, CallLocation>(
							sourceOfCall, _calleeOfSrc, _callLoc);
					pathStack.push(_triple);
					generateCallStacks(destinationOfCall, _calleeOfSrc, context, pathStack, callerView);
					pathStack.pop();
				}
			}
		}/*
			 * else { final MethodWrapper _callerOfDest = destinationOfCall.getParent(); if
			 * (sourceOfCall.equals(_callerOfDest)) { final Stack<?> _stack = (Stack) pathStack.clone();
			 * Collections.reverse(_stack); context.addContext(_stack); } else { for (final Iterator<?> _iter =
			 * destinationOfCall.getMethodCall().getCallLocations().iterator(); _iter .hasNext();) { final CallLocation
			 * _callLoc = (CallLocation) _iter.next(); final Triple<MethodWrapper, MethodWrapper, CallLocation> _triple = new
			 * Triple<MethodWrapper, MethodWrapper, CallLocation>( destinationOfCall, _callerOfDest, _callLoc);
			 * pathStack.push(_triple); generateCallStacks(_callerOfDest, sourceOfCall, context, pathStack, callerView);
			 * pathStack.pop(); } } }
			 */
	}

	/**
	 * Get the invoke expression for the given method.
	 * 
	 * @param jimplelist The list of jimple in which the call lies.
	 * @param callee The callee method.
	 * @param caller The caller method.
	 * @return CallTriple The call triple for the method call.
	 */
	private CallTriple fetchTriple(final List jimplelist, final SootMethod callee, final SootMethod caller) {
		CallTriple _triple = null;
		for (final Iterator _iter = jimplelist.iterator(); _iter.hasNext();) {
			final Stmt _stmt = (Stmt) _iter.next();
			if (_stmt.containsInvokeExpr()) {
				final InvokeExpr _expr1 = _stmt.getInvokeExpr();
				if (_expr1.getMethod().equals(callee)) {
					_triple = new CallTriple(caller, _stmt, _expr1);
					break;
				}
			}
		}
		return _triple;
	}

	/**
	 * Checks if the callee view has been activated.
	 * 
	 * @param mw The source method wrapper.
	 * @return boolean If the callee view has been activated.
	 */
	private boolean validateForCalleeView(final MethodWrapper mw) {
		boolean _result = false;
		final CallLocation _loc = mw.getMethodCall().getFirstCallLocation();
		if (_loc != null) {
			final IMethod _methodCall = (IMethod) _loc.getMember();
			final IMethod _callMethod = (IMethod) mw.getParent().getMember();
			if (_methodCall.equals(_callMethod)) {
				_result = true;
			}
		}
		return _result;
	}

	/**
	 * (non-Javadoc).
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection _ssl = (IStructuredSelection) selection;
			this.sSel = _ssl;
		}
	}
}
