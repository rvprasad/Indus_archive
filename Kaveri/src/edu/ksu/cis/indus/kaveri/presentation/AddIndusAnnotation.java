
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
 * Created on Apr 12, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.presentation;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IFileEditorInput;


/**
 * The manager for Indus annotations.
 *
 * @author Ganeshan
 */
public class AddIndusAnnotation {
	/** 
	 * <p>
	 * The java editor.
	 * </p>
	 */
	CompilationUnitEditor editor;

	/** 
	 * Map of class names to source positions methodMap.key.oclIsKindOf(String : classname) and
	 * methodMap.values.oclIsKindOf(Position).
	 */
	private Map classMap;

	/** 
	 * <p>
	 * Map of Java files to line numbers. locationMap.key.oclIsKindOf(IFile) and
	 * locationMap.values.oclIsKindof(AnnotationData)
	 * </p>
	 */
	private Map locationMap;

	/** 
	 * Map of method names to source positions. methodMap.key.oclIsKindOf(String : methodname) and
	 * methodMap.values.oclIsKindOf(Position)
	 */
	private Map methodMap;

	/** 
	 * <p>
	 * Id of the normal highlight annotation.
	 * </p>
	 */
	private final String normalHighlightString = "indus.slice.highlightAnnotation";

	/** 
	 * <p>
	 * Id of the partial highlight annotation.
	 * </p>
	 */
	private final String partialHighlightString = "indus.slice.partialhighlightAnnotation";

	/**
	 * Constructor.
	 */
	public AddIndusAnnotation() {
		locationMap = new HashMap();
		methodMap = new HashMap();
		classMap = new HashMap();
	}

	/**
	 * Toggles the annotation on the supplied editor.
	 *
	 * @param theeditor The editor to set.
	 * @param showAnnotation Show or remove annotations.
	 */
	public final void setEditor(final CompilationUnitEditor theeditor, final boolean showAnnotation) {
		this.editor = theeditor;

		final IFile _file = ((IFileEditorInput) theeditor.getEditorInput()).getFile();

		if (showAnnotation) {
			removeAnnotations();
			showAnnotations(_file);
		} else {
			removeAnnotations();
		}
	}

	//	public final void addAnnotationToElement(final ASTNode node) {
	//		if (editor == null || locationMap == null) {
	//			return;
	//		}
	//
	//		final Position _pos = new Position(node.getStartPosition(), node.getLength());
	//		locationMap.add(_pos);
	//
	//		final Annotation _annotation = new Annotation("indus.slice.highlightAnnotation", true, null);
	//		final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
	//		_model.addAnnotation(_annotation, _pos);
	//	}

	/**
	 * Returns whether annotations are present in the editor.
	 *
	 * @param jeditor The Java editor currently open.
	 *
	 * @return Returns the areAnnotationsPresent.
	 */
	public boolean isAreAnnotationsPresent(final CompilationUnitEditor jeditor) {
		final IAnnotationModel _model = jeditor.getDocumentProvider().getAnnotationModel(jeditor.getEditorInput());
		boolean _found = false;
		final Iterator _it = _model.getAnnotationIterator();

		while (_it.hasNext()) {
			final Annotation _annotation = (Annotation) _it.next();

			if (_annotation.getType().equals(normalHighlightString) || _annotation.getType().equals(partialHighlightString)) {
				_found = true;
				break;
			}
		}
		return _found;
	}

	/**
	 * Shows the annotations in the editor. Precondition: lineMap.keys.oclIsKindOf(String: classname) and
	 * lineMap.values.oclIsKindOf(AnnotationData).
	 *
	 * @param theeditor The currently open Java editor
	 * @param lineMap The map of classnames to line numbers
	 */
	public void setEditor(final CompilationUnitEditor theeditor, final Map lineMap) {
		this.editor = theeditor;

		final IFile _file = ((IFileEditorInput) theeditor.getEditorInput()).getFile();

		addAnnotationToLines(_file);

		//setEditor(theeditor, true);
	}

	/**
	 * Indicates if there are any annotations associated with the given file.
	 *
	 * @param file The input file to the editor.
	 *
	 * @return True If annotations are present in the editor.
	 */
	public boolean annotationPresent(final IFile file) {
		final List _lst = (List) locationMap.get(file);
		boolean _result = false;

		if (_lst != null && _lst.size() > 0) {
			_result = true;
		}
		return _result;
	}

	/**
	 * Resets the line numbers.
	 */
	public void reset() {
		locationMap.clear();
		methodMap.clear();
		classMap.clear();
	}

	/**
	 * Adds the annotations to the given lines.
	 *
	 * @param javaFile The input java file.
	 * 
	 */
	private void addAnnotationToLines(final IFile javaFile) {
		final IProject _project = javaFile.getProject();
		if (_project != KaveriPlugin.getDefault().getIndusConfiguration().getSliceProject()) {
			return;
		}
		
		final List _cllist = SECommons.getClassesInFile(javaFile);
		final Map _newMap = KaveriPlugin.getDefault().getCacheMap();
		Map lineMap = new HashMap();
		boolean _atleastSomePresent = false;
		for (int _i = 0; _i < _cllist.size(); _i++) {
			final String _classname = (String) _cllist.get(_i);
			 if (_newMap.get(_classname) != null) {
			 	lineMap.put(_classname, _newMap.get(_classname));
			 	_atleastSomePresent = true;
			 } 
			 			 
		}
		
		if (!_atleastSomePresent) {
			lineMap = TagToAnnotationMapper.getAnnotationLinesForFile(javaFile);
		}
		if (lineMap != null && lineMap.size() > 0) {
			//			IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
			//			ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
			//			if(unit !=null)
			//			{
			//				CompilationUnit cu = AST.parseCompilationUnit(unit, false);
			//				cu.accept(new AnnotationTraverser(lineVector, editor));					
			//			}
			final List _lst = new LinkedList();
			final List _pruneList = filterLines(lineMap);

			final List _oldlst = (List) locationMap.get(javaFile);

			if (_oldlst != null) {
				mergeLists(_oldlst, _pruneList);
			}

			for (int _nCtr = 0; _pruneList != null && _nCtr < _pruneList.size(); _nCtr++) {
				final AnnotationData _data = ((AnnotationData) _pruneList.get(_nCtr));

				final int _line = _data.getNLineNumber();

				try {
					final IRegion _region =
						editor.getDocumentProvider().getDocument(editor.getEditorInput()).getLineInformation(_line - 1);
					final String _text =
						editor.getDocumentProvider().getDocument(editor.getEditorInput()).get(_region.getOffset(),
							_region.getLength());
					final String _trimmedString = _text.trim();
					final int _index = _text.indexOf(_trimmedString);

					if (_trimmedString.equals("}") || _trimmedString.equals("{")) {
						continue;
					}

					if (_region.getLength() > 0) {
						final Position _pos = new Position(_region.getOffset() + _index, _region.getLength() - _index);
						Annotation _annotation;

						if (_data.isComplete()) {
							_annotation = new Annotation(normalHighlightString, false, "complete slice element");
						} else {
							_annotation = new Annotation(partialHighlightString, false, "partial slice element");
						}

						final IAnnotationModel _model =
							editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
						_model.addAnnotation(_annotation, _pos);
						_data.setPosition(_pos);
						_lst.add(_data);
					}
				} catch (BadLocationException _e) {
					SECommons.handleException(_e);
					continue;
				}
			}
			processForMethods(javaFile, lineMap);
			locationMap.put(javaFile, _lst);
		}
	}

	/**
	 * Filters the map of line numbers to the classes present in the currently open file.
	 *
	 *
	 * @param lineMap The mapping of classnames to line numbers.
	 *
	 * @return List The list of filtered line numbers
	 */
	private List filterLines(final Map lineMap) {
		final List _lst = new LinkedList();
		final Iterator _it = lineMap.keySet().iterator();
		while (_it.hasNext()) {
			final String _className = (String) _it.next();
			final Map _methodMap = (Map) lineMap.get(_className);
			final Map _mMap = (Map) lineMap.get(_className);
			final Iterator _mIt = _mMap.values().iterator();
			while (_mIt.hasNext()) {
				final List _annonList = (List) _mIt.next();
				_lst.addAll(_annonList);
			}
		}		
		return _lst;
	}

	/**
	 * Merges the old and the new annotation.  In case of clash, new ones take precedence. The new list gets the final set of
	 * line numbers.
	 *
	 * @param oldlst The old list of line numbers
	 * @param list The new list of line numbers
	 */
	private void mergeLists(final List oldlst, final List list) {
		for (int _i = 0; _i < list.size(); _i++) {
			final AnnotationData _data = (AnnotationData) list.get(_i);

			if (oldlst.contains(_data)) {
				final int _index = oldlst.indexOf(_data);
				oldlst.remove(_index);  // remove the common stuff from 
				// the old list.				
			}
		}
		list.addAll(oldlst);  // Add the non-common line numbers.
	}

	/**
	 * Adds annotations to method declarations.
	 *
	 * @param file The currently open file
	 * @param lineMap The Map of linenumbers.
	 */
	private void processForMethods(final IFile file, final Map lineMap) {
		final ICompilationUnit _unit = JavaCore.createCompilationUnitFrom(file);
		final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());

		if (_unit != null) {
			try {
				final IType[] _types = _unit.getAllTypes();

				for (int _i = 0; _types != null && _i < _types.length; _i++) {
					final String _className = _types[_i].getFullyQualifiedName();
					final Map _methodMap = (Map) lineMap.get(_className);

					if (_methodMap == null || _methodMap.size() == 0) {
						continue;
					}

					Position _p = null;

					if (classMap.containsKey(_className)) {
						_p = (Position) classMap.get(_className);
					} else {
						final ISourceRange _crange = ((IMember) _types[_i]).getNameRange();

						_p = new Position(_crange.getOffset(), _crange.getLength());
						classMap.put(_className, _p);
					}

					final Annotation _annot = new Annotation(normalHighlightString, false, null);
					_model.addAnnotation(_annot, _p);
					
					final IMethod[] _methods = _types[_i].getMethods();

					for (int _j = 0; _j < _methods.length; _j++) {
						final IMethod _method = _methods[_j];
						final String _methodsig = SECommons.getProperMethodName(_method);

						if (_methodMap.containsKey(_methodsig)) {
							Position _pos = null;
							final String _completeMethodName = PrettySignature.getMethodSignature(_method);

							if (methodMap.containsKey(_completeMethodName)) {
								_pos = (Position) methodMap.get(_completeMethodName);
							} else {
								final ISourceRange _range = ((IMember) _method).getNameRange();
								_pos = new Position(_range.getOffset(), _range.getLength());
								methodMap.put(_completeMethodName, _pos);
							}

							final Annotation _annotation = new Annotation(normalHighlightString, false, null);

							_model.addAnnotation(_annotation, _pos);
						}
					}
				}
			} catch (JavaModelException _e) {
				SECommons.handleException(_e);
			}
		}
	}

	/**
	 * Removes the annotations.
	 */
	private void removeAnnotations() {
		if (editor == null) {
			return;
		}

		//locationMap.clear();
		final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		final Iterator _it = _model.getAnnotationIterator();

		while (_it.hasNext()) {
			final Annotation _annotation = (Annotation) _it.next();

			if (_annotation.getType().equals(normalHighlightString) || _annotation.getType().equals(partialHighlightString)) {
				_model.removeAnnotation(_annotation);
			}
		}
	}

	/**
	 * Shows the annotations.
	 *
	 * @param file The java file on which the annotations are to be shown.
	 */
	private void showAnnotations(final IFile file) {
		if (locationMap != null) {
			final List _lst = (List) locationMap.get(file);

			if (_lst != null) {
				for (int _nCtr = 0; _nCtr < _lst.size(); _nCtr++) {
					final AnnotationData _data = (AnnotationData) _lst.get(_nCtr);
					final Position _pos = _data.getPosition();
					Annotation _annotation;

					if (_data.isComplete()) {
						_annotation = new Annotation(normalHighlightString, false, null);
						_annotation.setText("complete slice element");
					} else {
						_annotation = new Annotation(partialHighlightString, false, null);
						_annotation.setText("partial slice element");
					}

					final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
					_model.addAnnotation(_annotation, _pos);
				}
			}
		}
	}
}

//class AnnotationTraverser extends ASTVisitor
//{Vector lineList;String methodName; CompilationUnitEditor editor;
//	IDocument document;
//	AnnotationTraverser(Vector lineList, CompilationUnitEditor editor)
//	{
//		this.editor = editor;
//		this.lineList = (Vector) lineList.clone();
//		document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
//	}
//	public void preVisit(ASTNode node)
//	{
//		if (node instanceof MethodDeclaration) {
//			MethodDeclaration md = (MethodDeclaration) node;										
//			Block body;
//			body = md.getBody();			
//			List stmts = body.statements();
//			Iterator it = stmts.iterator();			
//			while (it.hasNext()) {
//				Statement stmt = (Statement) it.next();
//				handleStatement(stmt, lineList);								
//			}
//		}
//		
//	}
//	/**
//	 * @param stmt
//	 * @param lineList2
//	 */
//	private void handleStatement(Statement stmt, Vector lineList2) {
//		if(stmt==null) return;
//		if(stmt instanceof AssertStatement){					
//			try {
//				int pos =document.getLineOfOffset(stmt.getStartPosition());
//				Integer itg = new Integer(pos);
//				if(lineList2.contains(itg))
//				{
//					addAnnotation(stmt);
//					lineList2.remove(itg);
//				}
//				return;
//			} catch (BadLocationException e) {
//				return;
//			}
//		}
//		else if(stmt instanceof Block) {
//			Block blk = (Block) stmt;
//			List list =blk.statements();
//			for(int i=0;i<list.size();i++)
//				handleStatement((Statement) list.get(i), lineList2);
//			return;
//		}
//		else if(stmt instanceof BreakStatement) {
//			try {
//				int pos =document.getLineOfOffset(stmt.getStartPosition());
//				Integer itg = new Integer(pos);
//				if(lineList2.contains(itg))
//				{
//					addAnnotation(stmt);
//					lineList2.remove(itg);
//				}
//				return;
//			} catch (BadLocationException e) {
//				return;
//			}			
//		}
//		else if(stmt instanceof ConstructorInvocation) {
//			try {
//				int pos =document.getLineOfOffset(stmt.getStartPosition());
//				Integer itg = new Integer(pos);
//				if(lineList2.contains(itg))
//				{
//					addAnnotation(stmt);
//					lineList2.remove(itg);
//				}
//				return;
//			} catch (BadLocationException e) {
//				return;
//			}
//			
//		}
//		else if(stmt instanceof ContinueStatement) {
//			try {
//				int pos =document.getLineOfOffset(stmt.getStartPosition());
//				Integer itg = new Integer(pos);
//				if(lineList2.contains(itg))
//				{
//					addAnnotation(stmt);
//					lineList2.remove(itg);
//				}
//				return;
//			} catch (BadLocationException e) {
//				return;
//			}
//			
//		}
//		else if(stmt instanceof DoStatement) {
//			DoStatement dostmt = (DoStatement) stmt;
//			handleStatement(dostmt.getBody(), lineList2);
//			return;
//		}
//		else if(stmt instanceof LabeledStatement) {
//			LabeledStatement ls = (LabeledStatement) stmt;
//			handleStatement(ls.getBody(), lineList2);
//			return;
//		}
//		else if(stmt instanceof EmptyStatement) {
//			return;
//		}
//		else if(stmt instanceof EnhancedForStatement) {
//			EnhancedForStatement efs = (EnhancedForStatement) stmt;
//			handleStatement(efs.getBody(), lineList2);
//			return;
//		}
//		else if(stmt instanceof ExpressionStatement) {
//			ExpressionStatement est = (ExpressionStatement) stmt;
//			Expression xp = est.getExpression();
//			try {
//				int pos =document.getLineOfOffset(stmt.getStartPosition());
//				Integer itg = new Integer(pos);
//				if(lineList2.contains(itg))
//				{
//					addAnnotation(stmt);
//					lineList2.remove(itg);
//				}
//				return;
//			} catch (BadLocationException e) {
//				return;
//			}
//		}
//		else if(stmt instanceof ForStatement) {
//			ForStatement efs = (ForStatement) stmt;
//			handleStatement(efs.getBody(), lineList2);
//			return;
//		}
//		else if(stmt instanceof IfStatement) {
//			IfStatement ifstmt = (IfStatement) stmt;
//			handleStatement(ifstmt.getThenStatement(), lineList2);
//			handleStatement(ifstmt.getElseStatement(), lineList2);
//			return;
//		}
//		else if(stmt instanceof ReturnStatement) {
//			return;
//		}
//		else if(stmt instanceof SuperConstructorInvocation) {
//			return;
//		}
//		
//		else if(stmt instanceof SwitchStatement) {
//			SwitchStatement ss = (SwitchStatement) stmt;
//			List lst = ss.statements();
//			for(int n=0;n<lst.size();n++)
//			{
//				handleStatement((Statement) lst.get(n), lineList2);
//			}
//			return;
//		}
//		else if(stmt instanceof SynchronizedStatement) {
//			SynchronizedStatement st = (SynchronizedStatement) stmt;
//			handleStatement(st.getBody(), lineList2);
//			return;
//		}
//		else if(stmt instanceof ThrowStatement ) {
//			return;
//		}
//		else if(stmt instanceof TryStatement) {
//			TryStatement trys = (TryStatement) stmt;
//			handleStatement(trys.getBody(), lineList2);
//			handleStatement(trys.getFinally(), lineList2);			
//			return;
//		}
//		else if(stmt instanceof TypeDeclarationStatement) {
//			return;
//		}
//		else if(stmt instanceof VariableDeclarationStatement) {
//			return;
//		}
//		else if(stmt instanceof WhileStatement) {
//			WhileStatement wstmt = (WhileStatement) stmt;
//			handleStatement(wstmt.getBody(), lineList2);
//			return;
//		}
//	}
//	
//	private void addAnnotation(ASTNode node)
//	{
//		Position pos = new Position(node.getStartPosition(), node.getLength());		
//
//		Annotation annotation = new Annotation("indus.slice.highlightAnnotation", true, null);
//		IAnnotationModel model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
//		model.addAnnotation(annotation, pos);	
//	}
//}
