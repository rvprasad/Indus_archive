
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
package edu.ksu.cis.indus.toolkits.sliceeclipse.presentation;

import edu.ksu.cis.indus.toolkits.sliceeclipse.common.SECommons;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
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
	 * The java editor
	 * </p>
	 * .
	 */
	CompilationUnitEditor editor;

	/**
	 * <p>
	 * Vector to hold the annotations.
	 * </p>
	 */
	private HashMap locationMap;

	/**
	 * Are annotations present?
	 */
	private boolean areAnnotationsPresent;

	/**
	 * 
	 */
	public AddIndusAnnotation() {
		locationMap = new HashMap();
	}
	/**
	 * Sets the java editor.
	 *
	 * @param theeditor The editor to set.
	 * @param showAnnotation Toggle annotation.
	 */
	public final void setEditor(final CompilationUnitEditor theeditor, final boolean showAnnotation) {
		this.editor = theeditor;
		final IFile _file = (IFile) ((IFileEditorInput) theeditor.getEditorInput()).getFile();
		if (showAnnotation) {
			removeAnnotations();
			showAnnotations(_file);
		} else {
			removeAnnotations();
		}			
	}
	
	/**
	 * Resets the map.
	 */
	public void reset() {
		locationMap.clear();
	}
	/**
	 *  Sets the editor and shows the annotation.
	 * 
	 * @param theeditor The currently open Java editor
	 * @param lineMap The map of classnames to line numbers
	 */
	public void setEditor(final CompilationUnitEditor theeditor, final HashMap lineMap) {
		this.editor = theeditor;
		final IFile _file = (IFile) ((IFileEditorInput) theeditor.getEditorInput()).getFile();		
		
		addAnnotationToLines(_file, lineMap);

		setEditor(theeditor, true);
	}
	
	/**
	 * <p>
	 * Adds annotation to the given java ASTNode.
	 * </p>
	 *
	 * @param node The node to which the annotaion has to be added
	 */
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
	 * Returns if the annotation are present in the editor.
	 * @param jeditor The Java editor currently open.
	 * @return Returns the areAnnotationsPresent.
	 */
	public boolean isAreAnnotationsPresent(final CompilationUnitEditor jeditor) {
		final IAnnotationModel _model = jeditor.getDocumentProvider().getAnnotationModel(jeditor.getEditorInput());
		boolean _found = false;
		final Iterator _it = _model.getAnnotationIterator();
		while (_it.hasNext()) {
			final Annotation _annotation = (Annotation) _it.next();			
			if (_annotation.getType().equals("indus.slice.highlightAnnotation")) {
				_found = true; break;
			}
		}
		return _found;
	}

	

	/**
	 * Adds annotations to the given lines.
	 *
	 * @param javaFile The input java file.
	 * @param lineMap The set of slice line numbers.
	 */
	public void addAnnotationToLines(final IFile javaFile, final HashMap lineMap) {
		if (lineMap != null) {
			//			IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
			//			ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
			//			if(unit !=null)
			//			{
			//				CompilationUnit cu = AST.parseCompilationUnit(unit, false);
			//				cu.accept(new AnnotationTraverser(lineVector, editor));					
			//			}
			final List _lst = new LinkedList();
			final List _pruneList = filterLines(javaFile, lineMap); 
			
			for (int _nCtr = 0; _pruneList != null && _nCtr < _pruneList.size(); _nCtr++) {
				areAnnotationsPresent = true;

				final int _line = ((Integer) _pruneList.get(_nCtr)).intValue();

				try {
					final IRegion _region =
						editor.getDocumentProvider().getDocument(editor.getEditorInput()).getLineInformation(_line - 1);
					final String _text =
						editor.getDocumentProvider().getDocument(editor.getEditorInput()).get(_region.getOffset(),
							_region.getLength());
					final String _trimmedString = _text.trim();
					final int _index = _text.indexOf(_trimmedString);

					if (_trimmedString.equals("}")) {
						continue;
					}

					if (_trimmedString.equals("{")) {
						continue;
					}

					if (_region.getLength() > 0) {
						final Position _pos = new Position(_region.getOffset() + _index, _region.getLength() - _index);

						final Annotation _annotation = new Annotation("indus.slice.highlightAnnotation", true, null);
						final IAnnotationModel _model =
							editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
						_model.addAnnotation(_annotation, _pos);
						_lst.add(_pos);
					}
				} catch (BadLocationException _e) {
					SECommons.handleException(_e);
					continue;
				}
			}
			locationMap.put(javaFile, _lst);
		}
	}
	
	/** 
	 * Are annotations present for file.
	 * @param file The input file to the editor.
	 * 	@return True If annotations are present in the editor. 
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
	 * Filters the map of line numbers to the classes present in the currently open file.
	 * @param javaFile The Java file.
	 * @param lineMap The mapping of classnames to line numbers.
	 * @return List The list of filtered line numbers
	 */
	private List filterLines(final IFile javaFile, final HashMap lineMap) {
		final List _lst = new LinkedList();
		final ICompilationUnit _unit = JavaCore.createCompilationUnitFrom(javaFile);
		if (_unit != null) {
			try {
				final IType[] _types = _unit.getAllTypes();
				
				for (int _i = 0; _types != null && _i < _types.length; _i++) {
					final String _className = _types[_i].getFullyQualifiedName();
					final List _lineLst = (List) lineMap.get(_className);
					if (_lineLst != null && _lineLst.size() > 0) {
						_lst.addAll(_lineLst);
					}
				}
			} catch (JavaModelException _e) {				
				SECommons.handleException(_e);
			}
		}
		return _lst;
	}
	/**
	 * Removes the annotations.
	 */
	private void removeAnnotations() {
		areAnnotationsPresent = false;

		if (editor == null) {
			return;
		}

		//locationMap.clear();
		final IAnnotationModel _model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		final Iterator _it = _model.getAnnotationIterator();

		while (_it.hasNext()) {
			final Annotation _annotation = (Annotation) _it.next();

			if (_annotation.getType().equals("indus.slice.highlightAnnotation")) {
				_model.removeAnnotation(_annotation);
			}
		}
	}

	/**
	 * Shows the annotations.
	 * @param file The java file on which the annotations are to be shown.
	 */
	private void showAnnotations(final IFile file) {		
		areAnnotationsPresent = true;
		
		if (locationMap != null) {
			final List _lst = (List) locationMap.get(file);
			if (_lst != null) {
			for (int _nCtr = 0; _nCtr < _lst.size(); _nCtr++) {
				final Position _pos = (Position) _lst.get(_nCtr);
				final Annotation _annotation = new Annotation("indus.slice.highlightAnnotation", true, null);
				_annotation.setText("Indus Slice Element");
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
