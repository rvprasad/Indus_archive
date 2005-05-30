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
 * Created on Apr 14, 2004
 *
 *
 *
 */
package edu.ksu.cis.indus.kaveri.presentation;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import soot.Scene;

/**
 * This class implements the slice toggle action.
 * 
 * @author Ganeshan
 */
public class SliceAnnotate implements IEditorActionDelegate {
    /**
     * The java editor instance.
     */
    private CompilationUnitEditor editor;

    /**
     * The previous file chosen
     */
    private IFile prevFile;

    /**
     * The previous line number chosen, used to avoid redundant calls.
     */
    private int nLineno = -1;
    
    private ISelectionChangedListener listener;
    
    
    
    /**
     * Indicates the current java editor.
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(final IAction action,
            final IEditorPart targetEditor) {
        if (listener == null) {
            listener = new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                   if(editor != null) {
                       if (event.getSelection() instanceof ITextSelection) {
                           final ITextSelection _selection = (ITextSelection) event.getSelection();
                           processSelection(_selection);
                       }
                   }
                    
                }
                
            };
            
        }
        
        if (editor != null) {
            if (editor.getSelectionProvider() instanceof IPostSelectionProvider) {
                final IPostSelectionProvider _ipp = (IPostSelectionProvider) editor.getSelectionProvider();
                _ipp.addPostSelectionChangedListener(listener);
            }
        }
        
        if (targetEditor != null) {            
            if (((CompilationUnitEditor) targetEditor).getSelectionProvider() instanceof IPostSelectionProvider) {
                final IPostSelectionProvider _ipp = (IPostSelectionProvider) ((CompilationUnitEditor) targetEditor).getSelectionProvider();
                _ipp.addPostSelectionChangedListener(listener);
            }
        }
        
        this.editor = (CompilationUnitEditor) targetEditor;
        
        if (editor != null) {
            IWorkspaceRunnable _runnable = new IWorkspaceRunnable() {

                public void run(IProgressMonitor monitor) throws CoreException {        
                    final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
                    if (_file != null) {
                        final IProject _prj = _file.getProject();
                        KaveriPlugin.getDefault().getIndusConfiguration().setCurrentProject(_prj, _file);    
                    }
                    
                }
                
            };
            try {
                ResourcesPlugin.getWorkspace().run(_runnable, null);
            } catch (CoreException e) {
                KaveriErrorLog.logException("Unable to update criteria view", e);
            }
        }
        
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                final IFile _file = ((IFileEditorInput) editor.getEditorInput())
                        .getFile();                
                final Map _map = KaveriPlugin.getDefault().getCacheMap();

                if (editor != null && _map != null && _map.size() > 0) {
                    final IDecoratorManager _manager = KaveriPlugin
                            .getDefault().getWorkbench().getDecoratorManager();

                    if (_manager
                            .getEnabled("edu.ksu.cis.indus.kaveri.decorator")) {
                        final IFile _fl = ((IFileEditorInput) editor
                                .getEditorInput()).getFile();
                        boolean _hasNature = false;
                        try {
                            _hasNature = _fl.getProject().hasNature("org.eclipse.jdt.core.javanature");
                        } catch (CoreException e) {
                            return;
                        }
                        if (_hasNature) {
                        final AddIndusAnnotation _indusA = KaveriPlugin
                                .getDefault().getIndusConfiguration()
                                .getIndusAnnotationManager();

                        if (!_indusA.isAreAnnotationsPresent(editor)
                                && classesPresent(_fl, _map)) {
                            _indusA.setEditor(editor, _map);
                        }
                        }
                    }
                }
            }

            /**
             * Indicates if atleast one class in the file has a slice
             * associated.
             * 
             * @param fl
             *            The file in which to check.
             * @param map
             *            The map of class names to line numbers
             * 
             * @return boolean Whether the file is worth annotating
             */
            private boolean classesPresent(final IFile fl, final Map map) {
                final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
                        .create(fl);
                boolean _result = false;

                try {
                    if (_icunit != null) {
                        IType[] _types = null;
                        _types = _icunit.getAllTypes();

                        for (int _nrun = 0; _nrun < _types.length; _nrun++) {
                            final IType _type = _types[_nrun];
                            final String _className = _type
                                    .getFullyQualifiedName();

                            if (map.keySet().contains(_className)) {
                                _result = true;
                                break;
                            }
                        }
                    }
                } catch (JavaModelException _jme) {
                    KaveriErrorLog.logException("Java Model Exception", _jme);
                    SECommons.handleException(_jme);
                }
                return _result;
            }
        });
    }

    /**
     * Process the selection.
     * @param _selection
     */
    protected void processSelection(final ITextSelection _selection) {
        if (KaveriPlugin.getDefault().getIndusConfiguration()
                        .getStmtList().isListenersPresent()) {
            if (KaveriPlugin.getDefault().getIndusConfiguration().getStmtList()
                    .isListenersReady()
                    && editor != null) {
                final IFile _file = ((IFileEditorInput) editor.getEditorInput())
                .getFile();
                if (_file == null) {
                    return;
                }
                if (!hasJavaNature(_file)) {
                    return;
                }
                if (prevFile != null
                        && ((IFileEditorInput) editor.getEditorInput())
                                .getFile().equals(prevFile)) {
                    
                    if (nLineno == _selection.getStartLine()) {
                        return;
                    } else {
                        nLineno = _selection.getStartLine();
                    }

                } else {
                    prevFile = ((IFileEditorInput) editor.getEditorInput())
                            .getFile();
                    nLineno = _selection.getStartLine();
                }
                if (isSootClassAlreadyPresent(_file)) {
                    handleSelectionForSliceView(_selection);    
                } else {
                   
                    
                    final ProgressMonitorDialog _pmd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
                    final IRunnableWithProgress _progress = new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            monitor.beginTask("Loading Soot", IProgressMonitor.UNKNOWN);                            
                            handleSelectionForSliceView(_selection);
                        }
                        
                    };
                    try {
                    _pmd.run(false, false, _progress );
                    } catch (InterruptedException _ie) {
                        SECommons.handleException(_ie);
                        KaveriErrorLog.logException("Interrupted Exception", _ie);
                    } catch (InvocationTargetException _ie) {
                        SECommons.handleException(_ie);
                        KaveriErrorLog.logException("Interrupted Exception", _ie);
                    }
                }                                
            }
        }
        
    }

    /**
     * Determines if the file has already been loaded into soot.
     * @param file
     * @return
     */
    private boolean isSootClassAlreadyPresent(IFile _file) {
        final ICompilationUnit _unit = JavaCore.createCompilationUnitFrom(_file);
        boolean _result = false;
        try {
            final IType[] _types  =_unit.getAllTypes();
            for (int i = 0; i < _types.length; i++) {
                final IType _type = _types[i];
                if (Scene.v().containsClass(_type.getFullyQualifiedName())) {
                    _result = true;
                    break;
                }
            }
        } catch (JavaModelException e) {
            SECommons.handleException(e);
            KaveriErrorLog.logException("Java Model Exception", e);
        }
        
        return _result;
    }

    /**
     * Toggles the higlighting in the editor.
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(final IAction action) {
        final AddIndusAnnotation _indusA = KaveriPlugin.getDefault()
                .getIndusConfiguration().getIndusAnnotationManager();

        if (editor == null) {
            return;
            
        }
        final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
        if (_file != null && hasJavaNature(_file)) {
        if (editor != null && _indusA.isAreAnnotationsPresent(editor)) {
            _indusA.setEditor(editor, false);
        } else {
            final IFile _fl = ((IFileEditorInput) editor.getEditorInput())
                    .getFile();
            final Map _map = KaveriPlugin.getDefault().getIndusConfiguration()
                    .getLineNumbers();
            _indusA.setEditor(editor, _map);
        }
        }
    }

    /**
     * Checks for the java nature of the file.
     * @param file
     * @return
     */
    private boolean hasJavaNature(IFile file) {
        boolean _hasNature = false;
        try {
            _hasNature = file.getProject().hasNature("org.eclipse.jdt.core.javanature");
        } catch (CoreException e) {
            _hasNature = false;
        }
        return _hasNature;
    }

    /**
     * Handles the selection change event.
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(final IAction action,
            final ISelection selection) {
        
        /*if (selection != null
                && !selection.isEmpty()
                && KaveriPlugin.getDefault().getIndusConfiguration()
                        .getStmtList().isListenersPresent()) {
            if (KaveriPlugin.getDefault().getIndusConfiguration().getStmtList()
                    .isListenersReady()
                    && editor != null) {
                final IFile _file = ((IFileEditorInput) editor.getEditorInput())
                .getFile();
                if (_file == null) {
                    return;
                }
                if (!hasJavaNature(_file)) {
                    return;
                }
                if (prevFile != null
                        && ((IFileEditorInput) editor.getEditorInput())
                                .getFile().equals(prevFile)) {
                    final ITextSelection _tsel = (ITextSelection) selection;
                    if (_tsel.getLength() == 0
                            || nLineno == _tsel.getStartLine()) {
                        return;
                    } else {
                        nLineno = _tsel.getStartLine();
                    }

                } else {
                    prevFile = ((IFileEditorInput) editor.getEditorInput())
                            .getFile();
                    nLineno = ((ITextSelection) selection).getStartLine();
                }
                handleSelectionForSliceView(selection);
            }
        } */
    }

    /**
     * Update the slice view with the information from the current selection.
     * 
     * @param selection
     */
    private void handleSelectionForSliceView(ISelection selection) {
        final ITextSelection _tSelect = (ITextSelection) selection;
        if (editor != null) {
            String _text = "";
            final int _nSelLine = _tSelect.getEndLine() + 1;
            try {
                final IRegion _region = editor.getDocumentProvider()
                        .getDocument(editor.getEditorInput())
                        .getLineInformation(_nSelLine - 1);

                _text = editor.getDocumentProvider().getDocument(
                        editor.getEditorInput()).get(_region.getOffset(),
                        _region.getLength());
            } catch (BadLocationException _ble) {
                KaveriErrorLog.logException("Bad Location Exception", _ble);
                SECommons.handleException(_ble);
                return;
            }
            _text = _text.trim();

            try {
                final IType _type = SelectionConverter.getTypeAtOffset(editor);
                final IJavaElement _element = SelectionConverter
                        .getElementAtOffset(editor);
                if (_element != null && _element instanceof IMethod) {
                    final IFile _file = ((IFileEditorInput) editor
                            .getEditorInput()).getFile();
                    
                    final List _stmtlist = SootConvertor.getStmtForLine(_file,
                            _type, (IMethod) _element, _nSelLine);

                    if (_stmtlist != null && _stmtlist.size() >= 3) {
                        final PartialStmtData _psd = KaveriPlugin.getDefault()
                                .getIndusConfiguration().getStmtList();

                        _psd.setJavaFile(_file);
                        _psd.setSelectedStatement(_text);
                        _psd.setClassName(PrettySignature.getSignature(_type));                        
                        _psd.setMethodName(PrettySignature
                                .getSignature((IMethod) _element));
                        _psd.setLineNo(_nSelLine);
                        _psd.setStmtList(_stmtlist);
                    }
                }
            } catch (JavaModelException e) {
                KaveriErrorLog.logException("Java Model Exception", e);
                SECommons.handleException(e);
            }

        }
    }
}