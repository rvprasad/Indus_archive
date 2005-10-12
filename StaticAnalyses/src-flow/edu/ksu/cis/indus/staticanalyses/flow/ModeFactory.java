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
 * An implementation of <i>Abstract Factory</i> pattern given in "Gang of Four" book. It "creates" various compoments
 * required to setup and run the analysis. Other components of the framework use this class to obtain components when
 * assembling the analysis and the flow graph.
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
public class ModeFactory<ARI extends IIndexManager<? extends IIndex, ArrayType>, AI extends IIndexManager<? extends IIndex, Value>, IFI extends IIndexManager<? extends IIndex, SootField>, LE extends IExprSwitch<LE, N>, MI extends IIndexManager<? extends IIndex, SootMethod>, N extends IFGNode<N, ?>, RE extends IExprSwitch<RE, N>, SS extends IStmtSwitch<SS>, SFI extends IIndexManager<? extends IIndex, SootField>> {

	/**
	 * The prototype of index managers that manager indices related to arrays.
	 */
	private IPrototype<ARI> arrayIdxMgrPrototype;

	/**
	 * The prototype of index managers that manage indices related to AST nodes.
	 */
	private IPrototype<AI> astIdxMgrPrototype;

	/**
	 * The prototype of index managers that manage indices related to instance field variables.
	 */
	private IPrototype<IFI> instanceFieldIdxMgrPrototype;

	/**
	 * The prototype of LHS expression visitor to be used in the analysis.
	 */
	private IPrototype<LE> lhsExprPrototype;

	/**
	 * The prototype of index managers that manage indices related to methods.
	 */
	private IPrototype<MI> methodIdxMgrPrototype;

	/**
	 * The prototype of RHS expression visitor to be used in the analysis.
	 */
	private IPrototype<RE> rhsExprPrototype;

	/**
	 * A prototype of index managers to manage indices related to static field variables.
	 */
	private IPrototype<SFI> staticFieldIdxMgrPrototype;

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
		return arrayIdxMgrPrototype.getClone();
	}

	/**
	 * Returns an index manager to manage indices related to AST nodes.
	 * 
	 * @return an index manager related to AST nodes.
	 */
	public final IPrototype<AI> getASTIndexManagerPrototypeCreator() {
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
		return instanceFieldIdxMgrPrototype.getClone();
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 * 
	 * @param s parameter to be used to create the expression visitor.
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final LE getLHSExprVisitor(final IStmtSwitch s) {
		return lhsExprPrototype.getClone(s);
	}

	/**
	 * Returns an index manager to manage indices related to methods.
	 * 
	 * @return an index manager related to methods.
	 */
	public final MI getMethodIndexManager() {
		return methodIdxMgrPrototype.getClone();
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 * 
	 * @param s parameter to be used to create the expression visitor.
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final RE getRHSExprVisitor(final IStmtSwitch s) {
		return rhsExprPrototype.getClone(s);
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 * 
	 * @return an index manager related to static field variables.
	 */
	public final SFI getStaticFieldIndexManager() {
		return staticFieldIdxMgrPrototype.getClone();
	}

	/**
	 * Returns a statement visitor parameterized by <code>m</code>.
	 * 
	 * @param m parameter to be used to create the statement visitor.
	 * @return a LHS expression visitor parameterizec by <code>m</code>.
	 */
	public final SS getStmtVisitor(final IMethodVariant m) {
		return stmtPrototype.getClone(m);
	}

	/**
	 * Set the prototype from which new instances of array index managers should be created.
	 * 
	 * @param arrayIndexManagerPrototype the prototype to generate arrays related index manager objects. This implementation
	 *            should support <code>getClone()</code>.
	 * @pre arrayIndexManagerPrototype != null
	 */
	public void setArrayIndexManagerPrototype(final IPrototype<ARI> arrayIndexManagerPrototype) {
		this.arrayIdxMgrPrototype = arrayIndexManagerPrototype;
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
	 * Set the prototype from which new instances of instance field index managers should be created.
	 * 
	 * @param instanceFieldIndexManagerPrototype the prototype to generate instance field variables related index manager
	 *            objects. This implementation should support <code>getClone()</code>.
	 * @pre instanceFieldManagerPrototype != null
	 */
	public void setInstanceFieldIndexManagerPrototype(final IPrototype<IFI> instanceFieldIndexManagerPrototype) {
		this.instanceFieldIdxMgrPrototype = instanceFieldIndexManagerPrototype;
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
	 * Set the prototype from which new instances of method index managers should be created.
	 * 
	 * @param methodIndexManagerPrototype the prototype to generate method related index manager objects. This implementation
	 *            should support <code>getClone()</code>.
	 * @pre methodIndexManagerPrototype != null
	 */
	public final void setMethodIndexManagerPrototype(final IPrototype<MI> methodIndexManagerPrototype) {
		this.methodIdxMgrPrototype = methodIndexManagerPrototype;
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
	 * Set the prototype from which new instances of static field index managers should be created.
	 * 
	 * @param staticFieldIndexManagerPrototype the prototype to generate static variables related index manager objects. This
	 *            implementation should support <code>getClone()</code>.
	 * @pre staticFieldManagerPrototype != null
	 */
	public final void setStaticFieldIndexManagerPrototype(final IPrototype<SFI> staticFieldIndexManagerPrototype) {
		this.staticFieldIdxMgrPrototype = staticFieldIndexManagerPrototype;
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
