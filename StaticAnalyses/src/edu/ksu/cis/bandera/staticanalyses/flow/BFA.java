
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

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;

import edu.ksu.cis.bandera.staticanalyses.interfaces.IEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * The instance of the framework which controls and manages the analysis on execution.  It acts the central repository for
 * information pertaining to various components of the framework when the analysis is in progress.  It also serves as the
 * central repository for various instances of the framework at a given time.
 * </p>
 *
 * <p>
 * Created: Tue Jan 22 00:45:10 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class BFA
  implements IEnvironment {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(BFA.class);

	/**
	 * <p>
	 * A map of name to <code>BFA</code> instances.  This facilitates the retrival of analysis via name.
	 * </p>
	 */
	private static final Map INSTANCES = new HashMap();

	/**
	 * <p>
	 * The analyzer associated with this instance of the framework.
	 * </p>
	 */
	public final AbstractAnalyzer _ANALYZER;

	/**
	 * <p>
	 * The worklist associated with this instance of the framework.
	 * </p>
	 */
	final WorkList worklist;

	/**
	 * <p>
	 * The manager of array variants.
	 * </p>
	 */
	ArrayVariantManager arrayManager;

	/**
	 * <p>
	 * The manager of class related primitive information and processing.
	 * </p>
	 */
	ClassManager classManager;

	/**
	 * <p>
	 * The manager of instance field variants.
	 * </p>
	 */
	FieldVariantManager instanceFieldManager;

	/**
	 * <p>
	 * The manager of static field variants.
	 * </p>
	 */
	FieldVariantManager staticFieldManager;

	/**
	 * <p>
	 * The manager of method variants.
	 * </p>
	 */
	MethodVariantManager methodManager;

	/**
	 * <p>
	 * The factory that provides the components during the analysis performed by this instance of the framework.
	 * </p>
	 */
	private final ModeFactory modeFactory;

	/**
	 * <p>
	 * The <code>SootClassManager</code> which provides the set of class to be analyzed.
	 * </p>
	 */
	private SootClassManager scm;

	/**
	 * <p>
	 * Creates a new <code>BFA</code> instance.
	 * </p>
	 *
	 * @param name the name of the instance of the framework.
	 * @param analyzer the analyzer associated with this instance of the framework.
	 * @param mf the factory object to be used in the instrumentation of the analysis by this instance of the framework.
	 */
	BFA(String name, AbstractAnalyzer analyzer, ModeFactory mf) {
		worklist = new WorkList();
		modeFactory = mf;
		this._ANALYZER = analyzer;
		BFA.INSTANCES.put(name, this);
		this.classManager = modeFactory.getClassManager(this);
		arrayManager = new ArrayVariantManager(this, modeFactory.getArrayIndexManager());
		instanceFieldManager = new FieldVariantManager(this, modeFactory.getInstanceFieldIndexManager());
		methodManager = new MethodVariantManager(this, modeFactory.getMethodIndexManager(), modeFactory.getASTIndexManager());
		staticFieldManager = new FieldVariantManager(this, modeFactory.getStaticFieldIndexManager());
	}

	/**
	 * <p>
	 * Returns the instance of the framework associated with the given name.
	 * </p>
	 *
	 * @param name the name of the framework that is to be returned.
	 *
	 * @return the instance of the framework with given name.
	 */
	public static final BFA getBFA(String name) {
		BFA temp = null;

		if (INSTANCES.containsKey(name)) {
			temp = (BFA) INSTANCES.get(name);
		}

		return temp;
	}

	/**
	 * <p>
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.  If none
	 * exists, a new variant is greated
	 * </p>
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 */
	public final ArrayVariant getArrayVariant(ArrayType a) {
		return getArrayVariant(a, _ANALYZER.context);
	}

	/**
	 * <p>
	 * Returns the variant associated with the given array type in the given context.  If none exists, a new variant is
	 * created.
	 * </p>
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
	 * <p>
	 * Returns the Jimple representation of the given class.
	 * </p>
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 */
	public final SootClass getClass(String className) {
		return scm.getClass(className);
	}

	/**
	 * <p>
	 * Returns the classes of the system that were analyzed/accessed by this analysis.
	 * </p>
	 *
	 * @return a collection of classes.
	 *
	 * @post result->forall(o | o.oclType = ca.mcgill.sable.soot.SootClass)
	 */
	public Collection getClasses() {
		return Collections.unmodifiableCollection(classManager.classes);
	}

	/**
	 * <p>
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.  If none exists,
	 * a new variant is created.
	 * </p>
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.
	 */
	public final FieldVariant getFieldVariant(SootField sf) {
		return getFieldVariant(sf, _ANALYZER.context);
	}

	/**
	 * <p>
	 * Returns the variant associated with the given field in the given context.  If none exists, a new variant is created.
	 * </p>
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
	 * <p>
	 * Returns an LHS expression visitor as created by the factory.
	 * </p>
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new LHS expression visitor.
	 */
	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getLHSExpr(e);
	}

	/**
	 * <p>
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.  If none
	 * exists, a new variant is created.
	 * </p>
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>.
	 */
	public final MethodVariant getMethodVariant(SootMethod sm) {
		return getMethodVariant(sm, _ANALYZER.context);
	}

	/**
	 * <p>
	 * Returns a method variant corresponding to the given method in the given context.  If none exists, a new variant is
	 * created.
	 * </p>
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
	 * <p>
	 * Returns the flow graph node as created by the factory.
	 * </p>
	 *
	 * @return a new flow graph node.
	 */
	public final IFGNode getNewFGNode() {
		return modeFactory.getFGNode(worklist);
	}

	/**
	 * <p>
	 * Returns a RHS expression visitor as created by the factory.
	 * </p>
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new RHS expression visitor.
	 */
	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getRHSExpr(e);
	}

	/**
	 * <p>
	 * Returns the associated <code>SootClassManager</code>.
	 * </p>
	 *
	 * @return the associated <code>SootClassManager</code>.
	 */
	public final SootClassManager getSootClassManager() {
		return scm;
	}

	/**
	 * <p>
	 * Returns a statement visitor as created by the factory.
	 * </p>
	 *
	 * @param e the method variant which parameterizes the statement visitor.
	 *
	 * @return a new statement visitor.
	 */
	public final AbstractStmtSwitch getStmt(MethodVariant e) {
		return modeFactory.getStmt(e);
	}

	/**
	 * <p>
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.
	 * </p>
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 *            <code>null</code> if none exist.
	 */
	public final ArrayVariant queryArrayVariant(ArrayType a) {
		return queryArrayVariant(a, _ANALYZER.context);
	}

	/**
	 * <p>
	 * Returns the variant associated with the given array type in the given context.
	 * </p>
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
	 * <p>
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.
	 * </p>
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.<code>null</code>
	 *            if none exists.
	 */
	public final FieldVariant queryFieldVariant(SootField sf) {
		return queryFieldVariant(sf, _ANALYZER.context);
	}

	/**
	 * <p>
	 * Returns the variant associated with the given field in the given context.
	 * </p>
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
	 * <p>
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.
	 * </p>
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>. <code>null</code> if none exist.
	 */
	public final MethodVariant queryMethodVariant(SootMethod sm) {
		return queryMethodVariant(sm, _ANALYZER.context);
	}

	/**
	 * <p>
	 * Returns a method variant corresponding to the given method in the given context.
	 * </p>
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
	 * <p>
	 * Resets the framework.  The framework forgets all information allowing for a new session of analysis to executed.
	 * </p>
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
	 * <p>
	 * Analyzes the given classes starting with <code>root</code> method.
	 * </p>
	 *
	 * @param scm <code>SootClassManager</code> object which contains the classes to be analyzed.
	 * @param root the method to start the analysis from.
	 */
	void analyze(SootClassManager scm, SootMethod root) {
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
			classManager.process(getClass(((RefType) type).className));
		} else if (type instanceof ArrayType && ((ArrayType) type).baseType instanceof RefType) {
			classManager.process(getClass(((RefType) ((ArrayType) type).baseType).className));
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
