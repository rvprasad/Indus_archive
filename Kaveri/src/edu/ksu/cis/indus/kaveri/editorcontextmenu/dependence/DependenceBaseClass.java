/*
 * Created on Sep 26, 2004
 *
 * Provides all the base level functionalities for the dependence action.
 * Overrride the handleDependence method to handle a particular dependency.
 */
package edu.ksu.cis.indus.kaveri.editorcontextmenu.dependence;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.kaveri.presentation.TagToAnnotationMapper;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis;
import edu.ksu.cis.indus.tools.slicer.SlicerConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * Has the base functions used by the dependence classes.
 * @author ganeshan
 *
 */
abstract public class DependenceBaseClass implements IEditorActionDelegate {	
	
	protected CompilationUnitEditor editor;
	
	private String annotationKey = "indus.slice.DependencehighlightAnnotation";
	
	
	
	private ITextSelection tSelection;
	
	private Set annotSet = new HashSet();
	
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
			//removeDependenceAnnotation(editor);
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
    			final Triple _triple = new Triple(_inpfile.getName(), _text, new Integer(_nSelLine));
    			
    			if(decorateMap != null && decorateMap.size() > 0) {
                	processDependency(_triple);
                }
    		}
                        
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

	
	/** 
	 * Returns the list of statements linked by the dependence.
	 * @param _method
	 * @param _stmt
	 * @param dependenceType The dependecy
	 * @return List The list of dependees
	 */
	public List handleDependees(SootMethod _method, Stmt _stmt, final Object dependenceType) {		
		final EclipseIndusDriver _driver = KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver();
		final SlicerTool _stool = _driver.getSlicer();
		final SlicerConfiguration _config = (SlicerConfiguration) _stool.getActiveConfiguration();
		final List _lst = new LinkedList();
		Collection _coll = _config.getDependenceAnalyses(dependenceType);
		if (_coll != null) {
			Iterator it = _coll.iterator();
			while (it.hasNext()) {
			 AbstractDependencyAnalysis _crt = (AbstractDependencyAnalysis)	 it.next();		 
			 Collection ct =  _crt.getDependees(_stmt, _method);		 
			 Iterator stit = ct.iterator();
			 while(stit.hasNext()) {
			 	_lst.add(stit.next());
			 }
			}	
		}
		 	
		return _lst;
	}

	/**
	 * Returns the List of dependendent statements. 
	 * The actual dependency can thus be specialized by the subclasses.
	 * @param method
	 * @param stmt
	 * @return
	 */
	protected abstract List handleDependence(SootMethod method, Stmt stmt);
	
	protected abstract String getDependenceAnnotationKey();
	/**
	 * Returns the type of the dependence.
	 * 
	 * @return String The dependence type.
	 */
	protected abstract String getDependenceInfo();
	
	/** 
	 * Returns the list of statements linked by the dependence.
	 * @param _method
	 * @param _stmt
	 * @param dependenceType The dependecy
	 * @return List The list of dependent statements.
	 */
	public List handleDependents(SootMethod _method, Stmt _stmt, final Object dependenceType) {		
		final EclipseIndusDriver _driver = KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver();
		final SlicerTool _stool = _driver.getSlicer();
		final SlicerConfiguration _config = (SlicerConfiguration) _stool.getActiveConfiguration();
		final List _lst = new LinkedList();
		// IDependencyAnalysis.CONTROL_DA
		Collection _coll = _config.getDependenceAnalyses(dependenceType);
		if (_coll != null) {
			Iterator it = _coll.iterator();
			while (it.hasNext()) {
			 AbstractDependencyAnalysis _crt = (AbstractDependencyAnalysis)	 it.next();		 
			 Collection ct =  _crt.getDependents(_stmt, _method);		 
			 Iterator stit = ct.iterator();
			 while(stit.hasNext()) {
			 	_lst.add(stit.next());
			 }
			}	
		}
		return _lst;
	}

	/**
	 * Removes the dependence annotation if present previously.
	 * @param ed
	 */
	public void removeDependenceAnnotation(CompilationUnitEditor ed) {
		if (editor == null) {
			return;
		}
		
		final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		final Iterator _it =_model.getAnnotationIterator();
		while (_it.hasNext()) {
			final Annotation _an = (Annotation) _it.next();
			if (_an.getType().equals(annotationKey)) {
				_model.removeAnnotation(_an);
			}
		}
		/*
		final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		if (annotSet.size() > 0) {
			final Iterator _it = annotSet.iterator();
			while (_it.hasNext()) {
				_model.removeAnnotation((Annotation) _it.next());
			}
		}*/
		
}
	
	/**
	 * Processes the dependency.
	 *
	 * @param triple The triple of filename, selected text and line number.
	 * Precondition: 
	 *  triple.getFirst().oclIsType(String) :  Filename
	 *  triple.getMiddle().oclIsType(String) : Selected text
	 *  triple.getFirst().oclIsType(Integer) : Line number
	 */
	protected void processDependency(final Triple triple) {
        try {
            final IType _type = SelectionConverter.getTypeAtOffset(editor);
            final IJavaElement _element = SelectionConverter
                    .getElementAtOffset(editor);
            if (_element != null && (_element instanceof IMethod)) {
                final String _className = _type.getElementName();
                
                SootConvertor _sc;
                IFile _file = ((IFileEditorInput) editor
                        .getEditorInput()).getFile();
                final int nLine = ((Integer) triple.getThird()).intValue();
                List _stmtlist = SootConvertor.getStmtForLine(_file,
                        _type, (IMethod) _element, nLine);
                if (_stmtlist != null && _stmtlist.size() >= 3) {
                	final SootMethod _method = (SootMethod) _stmtlist.get(1);
                	final int _noStmts = _stmtlist.size() - 2;
                	final HashSet _set = new HashSet();
                	
                	for (int _i = 0; _i < _noStmts; _i++) {
                		final Stmt _stmt = (Stmt) _stmtlist.get(2 + _i);
                		_set.addAll(handleDependence(_method, _stmt));
                	}
                	final Integer _nItg = new Integer(nLine);
                	final HashSet _dset = KaveriPlugin.getDefault().getIndusConfiguration().getDepLinkSet();
                	if (! _dset.contains(_nItg) || 
                			KaveriPlugin.getDefault().getIndusConfiguration().
								getDepHistory().getHistory().size() == 0) {
                		KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory().reset();
                		KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory().setElemHistoryLink("");
                	}                	
                	_dset.clear();
                	final String _link = KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory().getElemHistoryLink();
                	Pair _pair = new Pair(triple, _link, false, true);
                	KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory().setElemHistoryLink(getDependenceInfo());
                	KaveriPlugin.getDefault().getIndusConfiguration().setDepHistory(_pair);
                	
                	final IAnnotationModel _model =
							editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
                    	if (_set.size() > 0) {
                    		annotSet.clear();
                    	}
                    	final Iterator _stit = _set.iterator();
                    	while(_stit.hasNext()) {
							final Stmt _st = (Stmt) _stit.next();
							int _nLine = SECommons.getLineNumberForStmt(_st);
							if (_nLine != -1) {								
								try {
									final IRegion _region =
										editor.getDocumentProvider().getDocument(editor.getEditorInput()).
										getLineInformation(_nLine - 1);
									_dset.add(new Integer(_nLine));
									final Annotation _annot = new Annotation(getDependenceAnnotationKey(), false, null);
									final Position _pos = new Position(_region.getOffset(), _region.getLength());
									annotSet.add(_annot);
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
}
