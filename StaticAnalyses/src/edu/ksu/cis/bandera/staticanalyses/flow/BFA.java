package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//BFA.java
/**
 * <p>The instance of the framework which controls and manages the analysis on execution.  It acts the central repository for
 * information pertaining to various components of the framework when the analysis is in progress.  It also serves as the
 * central repository for various instances of the framework at a given time.</p>
 *
 * <p>Created: Tue Jan 22 00:45:10 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class BFA {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(BFA.class);

	/**
	 * <p>A map of name to <code>BFA</code> instances.  This facilitates the retrival of analysis via name.</p>
	 *
	 */
	private static final Map instances = new HashMap();

	/**
	 * <p>The <code>SootClassManager</code> which provides the set of class to be analyzed.</p>
	 *
	 */
	private SootClassManager scm;

	/**
	 * <p>The worklist associated with this instance of the framework.</p>
	 *
	 */
	final WorkList worklist;

	/**
	 * <p>The analyzer associated with this instance of the framework.</p>
	 *
	 */
	public final AbstractAnalyzer analyzer;

	/**
	 * <p>The factory that provides the components during the analysis performed by this instance of the framework.</p>
	 *
	 */
	private final ModeFactory modeFactory;

	/**
	 * <p>The manager of array variants.</p>
	 *
	 */
	ArrayVariantManager arrayManager;

	/**
	 * <p>The manager of class related primitive information and processing.</p>
	 *
	 */
	ClassManager classManager;

	/**
	 * <p>The manager of instance field variants.</p>
	 *
	 */
	FieldVariantManager instanceFieldManager;

	/**
	 * <p>The manager of method variants.</p>
	 *
	 */
	MethodVariantManager methodManager;

	/**
	 * <p>The manager of static field variants.</p>
	 *
	 */
	FieldVariantManager staticFieldManager;

	/**
	 * <p>Creates a new <code>BFA</code> instance.</p>
	 *
	 * @param name the name of the instance of the framework.
	 * @param analyzer the analyzer associated with this instance of the framework.
	 * @param mf the factory object to be used in the instrumentation of the analysis by this instance of the framework.
	 */
	BFA (String name, AbstractAnalyzer analyzer, ModeFactory mf) {
		worklist = new WorkList();
		modeFactory = mf;
		this.analyzer = analyzer;
		BFA.instances.put(name, this);
		this.classManager = modeFactory.getClassManager(this);

		arrayManager = new ArrayVariantManager(this, modeFactory.getArrayIndexManager());
		instanceFieldManager = new FieldVariantManager(this, modeFactory.getInstanceFieldIndexManager());
		methodManager = new MethodVariantManager(this, modeFactory.getMethodIndexManager(), modeFactory.getASTIndexManager());
		staticFieldManager = new FieldVariantManager(this, modeFactory.getStaticFieldIndexManager());
	}

	/**
	 * <p>Analyzes the given classes starting with <code>root</code> method.</p>
	 *
	 * @param scm <code>SootClassManager</code> object which contains the classes to be analyzed.
	 * @param root the method to start the analysis from.
	 */
	void analyze(SootClassManager scm, SootMethod root) {
		this.scm = scm;
		getMethodVariant(root);
		worklist.process();
	}

	/**
	 * <p>Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.</p>
	 *
	 * @param a the array type whose variant is to be returned.
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 */
	public final ArrayVariant getArrayVariant(ArrayType a) {
		return getArrayVariant(a, analyzer.context);
	}

	/**
	 * <p>Returns the variant associated with the given array type in the given context.</p>
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>.
	 */
	public final ArrayVariant getArrayVariant(ArrayType a, Context context) {
		return (ArrayVariant)arrayManager.select(a, context);
	}

	/**
	 * <p>Returns the instance of the framework associated with the given name.</p>
	 *
	 * @param name the name of the framework that is to be returned.
	 * @return the instance of the framework with given name.
	 */
	public static final BFA getBFA(String name) {
		BFA temp = null;
		if (instances.containsKey(name)) {
			temp = (BFA)instances.get(name);
		}
		return temp;
	}

	/**
	 * <p>Returns the Jimple representation of the given class.</p>
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 * @return the <code>SootClass</code> containing the Jimple representation of the requested class.
	 */
	public final SootClass getClass(String className) {
		return scm.getClass(className);
	}

	/**
	 * <p>Returns the variant associated with the given field in the context captured by <code>analyzer</code>.</p>
	 *
	 * @param sf the field whose variant is to be returned.
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.
	 */
	public final FieldVariant getFieldVariant(SootField sf) {
		return getFieldVariant(sf, analyzer.context);
	}

	/**
	 * <p>Returns the variant associated with the given field in the given context.</p>
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant associated with the given field in the given context.
	 */
	public final FieldVariant getFieldVariant(SootField sf, Context context) {
		Variant temp = null;
		if (Modifier.isStatic(sf.getModifiers())) {
			temp = staticFieldManager.select(sf, context);
		} else {
			temp = instanceFieldManager.select(sf, context);
		} // end of else
		return (FieldVariant)temp;
	}

	/**
	 * <p>Returns the flow graph node as created by the factory.</p>
	 *
	 * @return a new flow graph node.
	 */
	public final FGNode getFGNode() {
		return modeFactory.getFGNode(worklist);
	}

	/**
	 * <p>Returns an LHS expression visitor as created by the factory.</p>
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 * @return a new LHS expression visitor.
	 */
	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getLHSExpr(e);
	}

	/**
	 * <p>Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.</p>
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>.
	 */
	public final MethodVariant getMethodVariant(SootMethod sm) {
		return getMethodVariant(sm, analyzer.context);
	}

	/**
	 * <p>Returns a method variant corresponding to the given method in the given context.</p>
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 * @return the variant corresonding to <code>sm</code> in the given context.
	 */
	public final MethodVariant getMethodVariant(SootMethod sm, Context context) {
		return (MethodVariant)methodManager.select(sm, context);
	}

	/**
	 * <p>Returns a RHS expression visitor as created by the factory.</p>
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 * @return a new RHS expression visitor.
	 */
	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getRHSExpr(e);
	}

	/**
	 * <p>Returns the associated <code>SootClassManager</code>.</p>
	 *
	 * @return the associated <code>SootClassManager</code>.
	 */
	public final SootClassManager getSootClassManager() {
		return scm;
	}

	/**
	 * <p>Returns a statement visitor as created by the factory.</p>
	 *
	 * @param ethe the method variant which parameterizes the statement visitor.
	 * @return a new statement visitor.
	 */
	public final AbstractStmtSwitch getStmt(MethodVariant e) {
		return modeFactory.getStmt(e);
	}

	/**
	 * <p>Resets the framework.  The framework forgets all information allowing for a new session of analysis to executed.</p>
	 *
	 */
	public void reset() {
		arrayManager.reset();
		instanceFieldManager.reset();
		methodManager.reset();
		staticFieldManager.reset();

		worklist.clear();
		scm = null;
	}

}// BFA
