
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;


/**
 * An implementation of <i>Abstract Factory</i> pattern given in "Gang of Four" book.  It "creates" various compoments
 * required to setup and run the analysis.  Other components of the framework use this class to obtain components when
 * assembling the analysis and the flow graph.
 * 
 * <p>
 * Created: Sun Jan 27 16:31:18 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ModeFactory {
	/** 
	 * The prototype of index managers that manager indices related to arrays.
	 */
	private IPrototype arrayIdxMgrPrototype;

	/** 
	 * The prototype of index managers that manage indices related to AST nodes.
	 */
	private IPrototype astIdxMgrPrototype;

	/** 
	 * The prototype of class managers that manage class related primitive information and processing.  Processing of
	 * &lt;clinit&gt; would be an example of such information.
	 */
	private IPrototype classMgrPrototype;

	/** 
	 * The prototype of index managers that manage indices related to instance field variables.
	 */
	private IPrototype instanceFieldIdxMgrPrototype;

	/** 
	 * The prototype of LHS expression visitor to be used in the analysis.
	 */
	private IPrototype lhsExprPrototype;

	/** 
	 * The prototype of index managers that manage indices related to methods.
	 */
	private IPrototype methodIdxMgrPrototype;

	/** 
	 * The prototype of RHS expression visitor to be used in the analysis.
	 */
	private IPrototype rhsExprPrototype;

	/** 
	 * A prototype of index managers to manage indices related to static field variables.
	 */
	private IPrototype staticFieldIdxMgrPrototype;

	/** 
	 * The prototype of statement visitor to be used in the analysis.
	 */
	private IPrototype stmtPrototype;

	/** 
	 * The prototype of the flow graph node to be used in constructing the flow graph during the analysis.
	 */
	private IPrototype theNodePrototype;

	/**
	 * Returns an index manager to manage indices related to AST nodes.
	 *
	 * @return an index manager related to AST nodes.
	 */
	public final IIndexManager getASTIndexManager() {
		return (IIndexManager) astIdxMgrPrototype.getClone();
	}

	/**
	 * Returns an index manager to manage indices related arrays.
	 *
	 * @return an index manager related to arrays.
	 *
	 * @post result != null
	 */
	public final IIndexManager getArrayIndexManager() {
		return (IIndexManager) arrayIdxMgrPrototype.getClone();
	}

	/**
	 * Set the prototype from which new instances of corresponding type should be created.
	 *
	 * @param astIndexManagerPrototype the prototype to generate AST node related index manager objects.
	 *
	 * @pre astIndexManagerPrototype != null
	 */
	public void setASTIndexManagerPrototype(final IPrototype astIndexManagerPrototype) {
		this.astIdxMgrPrototype = astIndexManagerPrototype;
	}

	/**
	 * Set the prototype from which new instances of array index managers should be created.
	 *
	 * @param arrayIndexManagerPrototype the prototype to generate arrays related index manager objects.  This implementation
	 * 		  should support <code>getClone()</code>.
	 *
	 * @pre arrayIndexManagerPrototype != null
	 */
	public void setArrayIndexManagerPrototype(final IPrototype arrayIndexManagerPrototype) {
		this.arrayIdxMgrPrototype = arrayIndexManagerPrototype;
	}

	/**
	 * Returns an object that manages class related primitive information and processing.  Processing of &lt;clinit&gt; would
	 * be an example of such information.
	 *
	 * @param o parameter to be used to create manager.
	 *
	 * @return a <code>ClassManager</code> object parameterized by <code>o</code>.
	 */
	public final ClassManager getClassManager(final Object o) {
		return (ClassManager) classMgrPrototype.getClone(o);
	}

	/**
	 * Set the prototype from which new instances of class managers should be created.
	 *
	 * @param classManagerPrototype the prototype to generate managers to manage class related primitive information and
	 * 		  processing.  This implementation should support <code>getClone(o)</code>.
	 *
	 * @pre classManagerPrototype != null
	 */
	public void setClassManagerPrototype(final IPrototype classManagerPrototype) {
		this.classMgrPrototype = classManagerPrototype;
	}

	/**
	 * Returns an flow graph node object that can be plugged into the flow graph.
	 *
	 * @param o parameter to be used to create the flow graph node.
	 *
	 * @return a flow graph node parameterized by <code>o</code>.
	 */
	public final IFGNode getFGNode(final Object o) {
		return (IFGNode) theNodePrototype.getClone(o);
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 *
	 * @return an index manager related to instance field variables.
	 */
	public final IIndexManager getInstanceFieldIndexManager() {
		return (IIndexManager) instanceFieldIdxMgrPrototype.getClone();
	}

	/**
	 * Set the prototype from which new instances of instance field index managers should be created.
	 *
	 * @param instanceFieldIndexManagerPrototype the prototype to generate instance field variables related index manager
	 * 		  objects.  This implementation should support <code>getClone()</code>.
	 *
	 * @pre instanceFieldManagerPrototype != null
	 */
	public void setInstanceFieldIndexManagerPrototype(final IPrototype instanceFieldIndexManagerPrototype) {
		this.instanceFieldIdxMgrPrototype = instanceFieldIndexManagerPrototype;
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 *
	 * @param s parameter to be used to create the expression visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final IExprSwitch getLHSExprVisitor(final IStmtSwitch s) {
		return (IExprSwitch) lhsExprPrototype.getClone(s);
	}

	/**
	 * Set the prototype from which new instances of LHS expression visitors should be created.
	 *
	 * @param lhsExprVisitorPrototype the prototype to generate LHS expression vistor to be used in constructing the flow
	 * 		  graph.  This implementation should support <code>getClone(o)</code>.
	 *
	 * @pre lhsExprVisitorPrototype != null
	 */
	public void setLHSExprVisitorPrototype(final IPrototype lhsExprVisitorPrototype) {
		this.lhsExprPrototype = lhsExprVisitorPrototype;
	}

	/**
	 * Returns an index manager to manage indices related to methods.
	 *
	 * @return an index manager related to methods.
	 */
	public final IIndexManager getMethodIndexManager() {
		return (IIndexManager) methodIdxMgrPrototype.getClone();
	}

	/**
	 * Set the prototype from which new instances of method index managers should be created.
	 *
	 * @param methodIndexManagerPrototype the prototype to generate method related index manager objects. This implementation
	 * 		  should support <code>getClone()</code>.
	 *
	 * @pre methodIndexManagerPrototype != null
	 */
	public final void setMethodIndexManagerPrototype(final IPrototype methodIndexManagerPrototype) {
		this.methodIdxMgrPrototype = methodIndexManagerPrototype;
	}

	/**
	 * Set the prototype from which new instances of nodes should be created.
	 *
	 * @param nodePrototype the prototype to generate flow graph nodes to be used in constructing the flow graph. This
	 * 		  implementation should support <code>getClone(o)</code>.
	 *
	 * @pre nodePrototype != null
	 */
	public final void setNodePrototype(final IPrototype nodePrototype) {
		this.theNodePrototype = nodePrototype;
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 *
	 * @param s parameter to be used to create the expression visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final IExprSwitch getRHSExprVisitor(final IStmtSwitch s) {
		return (IExprSwitch) rhsExprPrototype.getClone(s);
	}

	/**
	 * Set the prototype from which new instances of RHS expression visitors should be created.
	 *
	 * @param rhsExprVisitorPrototype the prototype to generate RHS expression vistor to be used in constructing the flow
	 * 		  graph.  This implementation should support <code>getClone(o)</code>.
	 */
	public final void setRHSExprVisitorPrototype(final IPrototype rhsExprVisitorPrototype) {
		this.rhsExprPrototype = rhsExprVisitorPrototype;
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 *
	 * @return an index manager related to static field variables.
	 */
	public final IIndexManager getStaticFieldIndexManager() {
		return (IIndexManager) staticFieldIdxMgrPrototype.getClone();
	}

	/**
	 * Set the prototype from which new instances of static field index managers should be created.
	 *
	 * @param staticFieldIndexManagerPrototype the prototype to generate static variables related index manager objects. This
	 * 		  implementation should support <code>getClone()</code>.
	 *
	 * @pre staticFieldManagerPrototype != null
	 */
	public final void setStaticFieldIndexManagerPrototype(final IPrototype staticFieldIndexManagerPrototype) {
		this.staticFieldIdxMgrPrototype = staticFieldIndexManagerPrototype;
	}

	/**
	 * Returns a statement visitor parameterized by <code>m</code>.
	 *
	 * @param m parameter to be used to create the statement visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>m</code>.
	 */
	public final IStmtSwitch getStmtVisitor(final MethodVariant m) {
		return (IStmtSwitch) stmtPrototype.getClone(m);
	}

	/**
	 * Set the prototype from which new instances of statement visitors should be created.
	 *
	 * @param stmtVisitorPrototype the prototype to generate statement vistor to be used in constructing the flow graph. This
	 * 		  implementation should support <code>getClone(o)</code>.
	 *
	 * @pre stmtVisitorPrototype != null
	 */
	public final void setStmtVisitorPrototype(final IPrototype stmtVisitorPrototype) {
		this.stmtPrototype = stmtVisitorPrototype;
	}
}

// End of File
