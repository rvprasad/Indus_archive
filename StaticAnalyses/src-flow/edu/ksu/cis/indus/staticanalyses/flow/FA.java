
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.ArrayType;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import edu.ksu.cis.indus.staticanalyses.*;
import edu.ksu.cis.indus.staticanalyses.*;
import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;


/**
 * The instance of the framework which controls and manages the analysis on execution.  It acts the central repository for
 * information pertaining to various components of the framework when the analysis is in progress.  It also serves as the
 * central repository for various instances of the framework at a given time.    Created: Tue Jan 22 00:45:10 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class BFA
  implements IEnvironment {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(BFA.class);

	/**
	 * The analyzer associated with this instance of the framework.
	 */
	public final AbstractAnalyzer _ANALYZER;

	/**
	 * The worklist associated with this instance of the framework.
	 */
	final WorkList worklist;

	/**
	 * The manager of array variants.
	 */
	ArrayVariantManager arrayManager;

	/**
	 * The manager of class related primitive information and processing.
	 */
	ClassManager classManager;

	/**
	 * The manager of instance field variants.
	 */
	FieldVariantManager instanceFieldManager;

	/**
	 * The manager of static field variants.
	 */
	FieldVariantManager staticFieldManager;

	/**
	 * The manager of method variants.
	 */
	MethodVariantManager methodManager;

	/**
	 * The factory that provides the components during the analysis performed by this instance of the framework.
	 */
	private ModeFactory modeFactory;

	/**
	 * The <code>Scene</code> which provides the set of class to be analyzed.
	 */
	private Scene scm;

	/**
	 * Creates a new <code>BFA</code> instance.
	 *
	 * @param analyzer the analyzer associated with this instance of the framework.
	 */
	BFA(AbstractAnalyzer analyzer) {
		worklist = new WorkList();
		this._ANALYZER = analyzer;
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.  If none
	 * exists, a new variant is greated
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 */
	public final ArrayVariant getArrayVariant(ArrayType a) {
		return getArrayVariant(a, _ANALYZER.context);
	}

	/**
	 * Returns the variant associated with the given array type in the given context.  If none exists, a new variant is
	 * created.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>.
	 */
	public final ArrayVariant getArrayVariant(ArrayType a, Context context) {
		processType(a.baseType);
		return (ArrayVariant) arrayManager.select(a, context);
	}

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 */
	public final SootClass getClass(String className) {
		return scm.getSootClass(className);
	}

	/**
	 * Returns the classes of the system that were analyzed/accessed by this analysis.
	 *
	 * @return a collection of classes.
	 *
	 * @post result->forall(o | o.oclType = soot.SootClass)
	 */
	public Collection getClasses() {
		return Collections.unmodifiableCollection(classManager.classes);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.  If none exists,
	 * a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.
	 */
	public final FieldVariant getFieldVariant(SootField sf) {
		return getFieldVariant(sf, _ANALYZER.context);
	}

	/**
	 * Returns the variant associated with the given field in the given context.  If none exists, a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant associated with the given field in the given context.
	 */
	public final FieldVariant getFieldVariant(SootField sf, Context context) {
		IVariant temp = null;
		processClass(sf.getDeclaringClass());
		processType(sf.getType());

		if (Modifier.isStatic(sf.getModifiers())) {
			temp = staticFieldManager.select(sf, context);
		} else {
			temp = instanceFieldManager.select(sf, context);
		}
		return (FieldVariant) temp;
	}

	/**
	 * Returns an LHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new LHS expression visitor.
	 */
	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getLHSExprVisitor(e);
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.  If none
	 * exists, a new variant is created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>.
	 */
	public final MethodVariant getMethodVariant(SootMethod sm) {
		return getMethodVariant(sm, _ANALYZER.context);
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.  If none exists, a new variant is
	 * created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 *
	 * @return the variant corresonding to <code>sm</code> in the given context.
	 */
	public final MethodVariant getMethodVariant(SootMethod sm, Context context) {
		return (MethodVariant) methodManager.select(sm, context);
	}

	/**
	 * Returns the flow graph node as created by the factory.
	 *
	 * @return a new flow graph node.
	 */
	public final IFGNode getNewFGNode() {
		return modeFactory.getFGNode(worklist);
	}

	/**
	 * Returns a RHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new RHS expression visitor.
	 */
	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getRHSExprVisitor(e);
	}

	/**
	 * Returns the associated <code>Scene</code>.
	 *
	 * @return the associated <code>Scene</code>.
	 */
	public final Scene getScene() {
		return scm;
	}

	/**
	 * Returns a statement visitor as created by the factory.
	 *
	 * @param e the method variant which parameterizes the statement visitor.
	 *
	 * @return a new statement visitor.
	 */
	public final AbstractStmtSwitch getStmt(MethodVariant e) {
		return modeFactory.getStmtVisitor(e);
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 * 		   <code>null</code> if none exist.
	 */
	public final ArrayVariant queryArrayVariant(ArrayType a) {
		return queryArrayVariant(a, _ANALYZER.context);
	}

	/**
	 * Returns the variant associated with the given array type in the given context.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>. <code>null</code> if none exist.
	 */
	public final ArrayVariant queryArrayVariant(ArrayType a, Context context) {
		return (ArrayVariant) arrayManager.query(a, context);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.<code>null</code>
	 * 		   if none exists.
	 */
	public final FieldVariant queryFieldVariant(SootField sf) {
		return queryFieldVariant(sf, _ANALYZER.context);
	}

	/**
	 * Returns the variant associated with the given field in the given context.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant associated with the given field in the given context.  <code>null</code> if none exists.
	 */
	public final FieldVariant queryFieldVariant(SootField sf, Context context) {
		IVariant temp = null;

		if (Modifier.isStatic(sf.getModifiers())) {
			temp = staticFieldManager.query(sf, context);
		} else {
			temp = instanceFieldManager.query(sf, context);
		}

		// end of else
		return (FieldVariant) temp;
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>. <code>null</code> if none exist.
	 */
	public final MethodVariant queryMethodVariant(SootMethod sm) {
		return queryMethodVariant(sm, _ANALYZER.context);
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 *
	 * @return the variant corresonding to <code>sm</code> in the given context.  <code>null</code> if none exist.
	 */
	public final MethodVariant queryMethodVariant(SootMethod sm, Context context) {
		return (MethodVariant) methodManager.query(sm, context);
	}

	/**
	 * Resets the framework.  The framework forgets all information allowing for a new session of analysis to executed.
	 */
	public void reset() {
		arrayManager.reset();
		instanceFieldManager.reset();
		methodManager.reset();
		staticFieldManager.reset();
		worklist.clear();
		scm = null;
	}

	/**
	 * Sets the mode factory on this framework instance.
	 *
	 * @param mf is the factory object which provides the objects that dictate the mode of analysis.
	 */
	void setModeFactory(ModeFactory mf) {
		modeFactory = mf;
		this.classManager = mf.getClassManager(this);
		arrayManager = new ArrayVariantManager(this, mf.getArrayIndexManager());
		instanceFieldManager = new FieldVariantManager(this, mf.getInstanceFieldIndexManager());
		methodManager = new MethodVariantManager(this, mf.getMethodIndexManager(), modeFactory.getASTIndexManager());
		staticFieldManager = new FieldVariantManager(this, mf.getStaticFieldIndexManager());
	}

	/**
	 * Analyzes the given classes starting with <code>root</code> method.
	 *
	 * @param scm <code>Scene</code> object which contains the classes to be analyzed.
	 * @param root the method to start the analysis from.
	 */
	void analyze(Scene scm, SootMethod root) {
		this.scm = scm;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting system processing...");
		}
		getMethodVariant(root);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting worklist processing...");
		}
		worklist.process();
	}

	/**
	 * Performs type-based processing of the given class.
	 *
	 * @param clazz is the class to be processed.
	 */
	void processClass(SootClass clazz) {
		classManager.process(clazz);
	}

	/**
	 * Performs type-based processing.
	 *
	 * @param type to be processed.
	 */
	void processType(Type type) {
		if (type instanceof RefType) {
			classManager.process(getClass(((RefType) type).getClassName()));
		} else if (type instanceof ArrayType && ((ArrayType) type).baseType instanceof RefType) {
			classManager.process(getClass(((RefType) ((ArrayType) type).baseType).getClassName()));
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
