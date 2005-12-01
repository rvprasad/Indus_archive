/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import soot.ArrayType;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import edu.ksu.cis.indus.interfaces.IPrototype;

/**
 * It "creates" various compoments required to setup and run the analysis. Other components of the framework use this class to
 * obtain components when assembling the analysis and the flow graph. An implementation of <i>Abstract Factory</i> pattern
 * given in "Gang of Four" book.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <ARI> DOCUMENT ME!
 * @param <AI> DOCUMENT ME!
 * @param <IFI> DOCUMENT ME!
 * @param <LE> DOCUMENT ME!
 * @param <MI> DOCUMENT ME!
 * @param <N> DOCUMENT ME!
 * @param <RE> DOCUMENT ME!
 * @param <SS> DOCUMENT ME!
 * @param <SFI> DOCUMENT ME!
 */
public class ModeFactory<ARI extends IIndexManager<? extends IIndex<?>, ArrayType>, AI extends IIndexManager<? extends IIndex<?>, Value>, IFI extends IIndexManager<? extends IIndex<?>, SootField>, LE extends IExprSwitch<N>, MI extends IIndexManager<? extends IIndex<?>, SootMethod>, N extends IFGNode<N, ?>, RE extends IExprSwitch<N>, SS extends IStmtSwitch, SFI extends IIndexManager<? extends IIndex<?>, SootField>> {

	/**
	 * The prototype of index managers that manager indices related to arrays.
	 */
	private ARI arrayIdxMgr;

	/**
	 * The prototype of index managers that manage indices related to AST nodes.
	 */
	private IPrototype<AI> astIdxMgrPrototype;

	/**
	 * The prototype of index managers that manage indices related to instance field variables.
	 */
	private IFI instanceFieldIdxMgr;

	/**
	 * The prototype of LHS expression visitor to be used in the analysis.
	 */
	private IPrototype<LE> lhsExprPrototype;

	/**
	 * The prototype of index managers that manage indices related to methods.
	 */
	private MI methodIdxMgr;

	/**
	 * The prototype of RHS expression visitor to be used in the analysis.
	 */
	private IPrototype<RE> rhsExprPrototype;

	/**
	 * A prototype of index managers to manage indices related to static field variables.
	 */
	private SFI staticFieldIdxMgr;

	/**
	 * The prototype of statement visitor to be used in the analysis.
	 */
	private IPrototype<SS> stmtPrototype;

	/**
	 * The prototype of the flow graph node to be used in constructing the flow graph during the analysis.
	 */
	private IPrototype<N> theNodePrototype;

	/**
	 * Returns an index manager to manage indices related arrays.
	 *
	 * @return an index manager related to arrays.
	 * @post result != null
	 */
	public final ARI getArrayIndexManager() {
		return arrayIdxMgr;
	}

	/**
	 * Returns the prototype of the index manager that can manage indices related to AST nodes.
	 *
	 * @return an index manager related to AST nodes.
	 */
	public final IPrototype<AI> getASTIndexManagerPrototype() {
		return astIdxMgrPrototype;
	}

	/**
	 * Returns an flow graph node object that can be plugged into the flow graph.
	 *
	 * @param o parameter to be used to create the flow graph node.
	 * @return a flow graph node parameterized by <code>o</code>.
	 */
	public final N getFGNode(final Object o) {
		return theNodePrototype.getClone(o);
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 *
	 * @return an index manager related to instance field variables.
	 */
	public final IFI getInstanceFieldIndexManager() {
		return instanceFieldIdxMgr;
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 *
	 * @param s parameter to be used to create the expression visitor.
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final LE getLHSExprVisitor(final SS s) {
		return lhsExprPrototype.getClone(s);
	}

	/**
	 * Returns an index manager to manage indices related to methods.
	 *
	 * @return an index manager related to methods.
	 */
	public final MI getMethodIndexManager() {
		return methodIdxMgr;
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 *
	 * @param s parameter to be used to create the expression visitor.
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final RE getRHSExprVisitor(final SS s) {
		return rhsExprPrototype.getClone(s);
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 *
	 * @return an index manager related to static field variables.
	 */
	public final SFI getStaticFieldIndexManager() {
		return staticFieldIdxMgr;
	}

	/**
	 * Returns a statement visitor parameterized by <code>m</code>.
	 *
	 * @param m parameter to be used to create the statement visitor.
	 * @return a LHS expression visitor parameterizec by <code>m</code>.
	 */
	public final SS getStmtVisitor(final IMethodVariant<N, LE, RE, SS> m) {
		return stmtPrototype.getClone(m);
	}

	/**
	 * Set the array index manager.
	 *
	 * @param arrayIndexManager the manager generates arrays related index manager objects.
	 * @pre arrayIndexManager != null
	 */
	public void setArrayIndexManager(final ARI arrayIndexManager) {
		this.arrayIdxMgr = arrayIndexManager;
	}

	/**
	 * Set the prototype from which new instances of corresponding type should be created.
	 *
	 * @param astIndexManagerPrototype the prototype to generate AST node related index manager objects.
	 * @pre astIndexManagerPrototype != null
	 */
	public void setASTIndexManagerPrototype(final IPrototype<AI> astIndexManagerPrototype) {
		this.astIdxMgrPrototype = astIndexManagerPrototype;
	}

	/**
	 * Set the instance field index manager.
	 *
	 * @param instanceFieldIndexManager the manager generates instance field variables related index manager objects.
	 * @pre instanceFieldManager != null
	 */
	public void setInstanceFieldIndexManager(final IFI instanceFieldIndexManager) {
		this.instanceFieldIdxMgr = instanceFieldIndexManager;
	}

	/**
	 * Set the prototype from which new instances of LHS expression visitors should be created.
	 *
	 * @param lhsExprVisitorPrototype the prototype to generate LHS expression vistor to be used in constructing the flow
	 *            graph. This implementation should support <code>getClone(o)</code>.
	 * @pre lhsExprVisitorPrototype != null
	 */
	public void setLHSExprVisitorPrototype(final IPrototype<LE> lhsExprVisitorPrototype) {
		this.lhsExprPrototype = lhsExprVisitorPrototype;
	}

	/**
	 * Set the method index manager.
	 *
	 * @param methodIndexManager the manager generates method related index manager objects.
	 * @pre methodIndexManager != null
	 */
	public final void setMethodIndexManager(final MI methodIndexManager) {
		this.methodIdxMgr = methodIndexManager;
	}

	/**
	 * Set the prototype from which new instances of nodes should be created.
	 *
	 * @param nodePrototype the prototype to generate flow graph nodes to be used in constructing the flow graph. This
	 *            implementation should support <code>getClone(o)</code>.
	 * @pre nodePrototype != null
	 */
	public final void setNodePrototype(final IPrototype<N> nodePrototype) {
		this.theNodePrototype = nodePrototype;
	}

	/**
	 * Set the prototype from which new instances of RHS expression visitors should be created.
	 *
	 * @param rhsExprVisitorPrototype the prototype to generate RHS expression vistor to be used in constructing the flow
	 *            graph. This implementation should support <code>getClone(o)</code>.
	 */
	public final void setRHSExprVisitorPrototype(final IPrototype<RE> rhsExprVisitorPrototype) {
		this.rhsExprPrototype = rhsExprVisitorPrototype;
	}

	/**
	 * Set the static field index manager.
	 *
	 * @param staticFieldIndexManager the manager static variables related index manager objects.
	 * @pre staticFieldManager != null
	 */
	public final void setStaticFieldIndexManager(final SFI staticFieldIndexManager) {
		this.staticFieldIdxMgr = staticFieldIndexManager;
	}

	/**
	 * Set the prototype from which new instances of statement visitors should be created.
	 *
	 * @param stmtVisitorPrototype the prototype to generate statement vistor to be used in constructing the flow graph. This
	 *            implementation should support <code>getClone(o)</code>.
	 * @pre stmtVisitorPrototype != null
	 */
	public final void setStmtVisitorPrototype(final IPrototype<SS> stmtVisitorPrototype) {
		this.stmtPrototype = stmtVisitorPrototype;
	}
}

// End of File
