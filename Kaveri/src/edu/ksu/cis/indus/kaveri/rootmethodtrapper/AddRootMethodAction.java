/*
 *
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
 
package edu.ksu.cis.indus.kaveri.rootmethodtrapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AddRootMethodAction implements IObjectActionDelegate {

    private IStructuredSelection selection;
    
    /** (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        

    }

    /** (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (selection != null && !selection.isEmpty()) {
            final Object _obj = selection.getFirstElement();
            if (_obj instanceof IMethod) {
                final IMethod _method = (IMethod) _obj;
                final ICompilationUnit _unit = _method.getCompilationUnit();
                if (_unit != null) {
                    final IFile _file;
                    try {
                        _file = (IFile)_unit.getCorrespondingResource();
                        
                    if (_file != null) {
                        final IProject _prj = _file.getProject();                        
                        final IJavaProject _jProject = JavaCore.create(_prj);
                        if (_jProject != null) {
                            final IResource _resource = _jProject.getCorrespondingResource();
                            final QualifiedName _name = new QualifiedName("edu.ksu.cis.indus.kaveri", "rootMethodCollection");
                            final String _propVal =   _resource.getPersistentProperty(_name);
                            final XStream _xstream = new XStream(new DomDriver());
                            _xstream.alias("RootMethodCollection", RootMethodCollection.class);
                            RootMethodCollection _rmc = null;
                            if (_propVal != null) {
                                _rmc = (RootMethodCollection) _xstream.fromXML(_propVal);
                                
                            } else {
                                _rmc = new RootMethodCollection();                                
                            }
                            final String _methodSig = SECommons.getProperMethodName(_method);
                            _rmc.addRootMethod(_method.getDeclaringType().getFullyQualifiedName(),
                                    _methodSig);
                            final String _val = _xstream.toXML(_rmc);
                            addRootMethodMarker(_method, _file, _methodSig);                            
                            _resource.setPersistentProperty(_name, _val);                            
                        }
                    }
                    } catch (JavaModelException _e) {
                        SECommons.handleException(_e);
                        KaveriErrorLog.logException("Java Model Exception", _e);
                    } catch (CoreException _e) {
                        SECommons.handleException(_e);
                        KaveriErrorLog.logException("Core Exception", _e);
                    }
                }
            }
        }

    }

    /**
     * Adds a marker to indicate that the method is a root method.
     * @param method
     * @param file
     * @param methodSig
     */
    private void addRootMethodMarker(IMethod method, IFile file, final String methodSig) {
        final String _markerId = KaveriPlugin.getDefault().getBundle().getSymbolicName() + "." +
        		"rootMethodMarker";
        final Map _map = new HashMap();
        final String _classNameKey = "className";
        final String _methodSigKey = "methodSignature";
        final Document  _d =  SECommons.getDocumentForJavaFile(file);
        if (_d != null) {
            final int _nLineNo;
            try {
                _nLineNo = _d.getLineOfOffset(method.getNameRange().getOffset());            
            _map.put(IMarker.MESSAGE, "Root Method");
            _map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
            _map.put(IMarker.LINE_NUMBER, new Integer(_nLineNo + 1));
            _map.put(_classNameKey, method.getDeclaringType().getFullyQualifiedName());
            _map.put(_methodSigKey, methodSig);
            MarkerUtilities.createMarker(file, _map, _markerId);            
            } catch (JavaModelException e) {
               SECommons.handleException(e);
            } catch (BadLocationException e) {
                SECommons.handleException(e);
            } catch (CoreException e) {
                SECommons.handleException(e);
            }
        }
        
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;    
        }        
    }

}
