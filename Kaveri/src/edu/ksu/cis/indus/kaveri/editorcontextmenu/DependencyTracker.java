
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
 * Created on Jun 17, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.editorcontextmenu;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import soot.SootMethod;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.presentation.TagToAnnotationMapper;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;


/**
 * Tracks Dependencies.
 *
 * @author Ganeshan 
 */
public class DependencyTracker extends DependenceBaseClass
  implements IEditorActionDelegate
	{
	private CompilationUnitEditor editor;

	private ITextSelection tSelection;
	
	/** (non-Javadoc)	 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {		
		editor = (CompilationUnitEditor) targetEditor;
	}

	/** (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		
	}

	/** (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if(tSelection != null) {
			final String _text = tSelection.getText();
            final int _nSelLine = tSelection.getEndLine() + 1;
            final IFile _inpfile = ((IFileEditorInput) editor.getEditorInput()).getFile();           
    		final IProject _project = KaveriPlugin.getDefault().getIndusConfiguration()
    		.getSliceProject();
    		final IProject _sliceProject = _inpfile.getProject();
    		if (_project != null && _project == _sliceProject) {
    			final Map decorateMap = KaveriPlugin.getDefault().getCacheMap();
    			final List _lst = SECommons.getClassesInFile(_inpfile);
    			for (int _i = 0; _i < _lst.size(); _i++) {
    				final String _classname = (String) _lst.get(_i);
    				
    				if (decorateMap.get(_classname) != null) {
    					break;
    				}
    			}
    			final Map _map = TagToAnnotationMapper.getAnnotationLinesForFile(_inpfile);
    			if (_map.size() > 0) {
    				final Iterator _it = _map.keySet().iterator();
    				while (_it.hasNext()) {
    					final Object _key = _it.next();
    					final Map _value = (Map) _map.get(_key);
    					if (_value.size() > 0) {
    						decorateMap.put(_key, _value);    						
    					}					
    				}				
    			}
    			if(decorateMap != null && decorateMap.size() > 0) {
                	processDependency(_nSelLine);
                }
    		}
                        
		}
	}

	/**
	 * Processes the dependency.
	 * @param nLine The selected line number.
	 */
	private void processDependency(int nLine) {
        try {
            final IType _type = SelectionConverter.getTypeAtOffset(editor);
            final IJavaElement _element = SelectionConverter
                    .getElementAtOffset(editor);
            if (_element != null && (_element instanceof IMethod)) {
                final String _className = _type.getElementName();                  
                SootConvertor _sc;
                IFile _file = ((IFileEditorInput) editor
                        .getEditorInput()).getFile();
                List _stmtlist = SootConvertor.getStmtForLine(_file,
                        _type, (IMethod) _element, nLine);
                if (_stmtlist != null && _stmtlist.size() >= 3) {
                	final SootMethod _method = (SootMethod) _stmtlist.get(1);
                	final int _noStmts = _stmtlist.size() - 2;
                	                	
                	final Stmt _stmt = (Stmt) _stmtlist.get(1 + _noStmts);
                    	
                    	final List _lst = handleDependees(_method, _stmt, IDependencyAnalysis.CONTROL_DA);
                    	final IAnnotationModel _model =
							editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
                    	for (int _i = 0; _i < _lst.size(); _i++) {
							final Stmt _st = (Stmt) _lst.get(_i);
							int _nLine = SECommons.getLineNumberForStmt(_st);
							if (_nLine != -1) {
								try {
									final IRegion _region =
										editor.getDocumentProvider().getDocument(editor.getEditorInput()).
										getLineInformation(_nLine - 1);
									final Annotation _annot = new Annotation("indus.slice.DependencehighlightAnnotation", false, null);
									final Position _pos = new Position(_region.getOffset(), _region.getLength());
									_model.addAnnotation(_annot, _pos);
								} catch (BadLocationException _e) {
									SECommons.handleException(_e);									
								}
							}							
						}                    
                	
                	
                	
                }                                     
            }
        } catch (JavaModelException _e) {
            _e.printStackTrace();
        }
	}



	/** (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		tSelection = null;
		if(selection instanceof ITextSelection) {
			tSelection = (ITextSelection) selection;
		}
	}
}
