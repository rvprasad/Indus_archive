
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;


/**
 * This class provides data dependence information which considers references. Hence, it considers the effects of aliasing.
 * It is an adapter for an interprocedural use-def analysis which considers the effects of aliasing.  It can be  configured
 * to provide dependence based on static field references.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ReferenceBasedDataDA
  extends AbstractDependencyAnalysis {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceBasedDataDA.class);

	/** 
	 * This provides inter-procedural use-def information which considers the effects of aliasing.
	 */
	private IUseDefInfo aliasedUD;

	/** 
	 * This provides static field use-def information.
	 */
	private IUseDefInfo staticFieldRefUD;

	/**
	 * Return the statements on which field/array access in <code>stmt</code> in <code>method</code> depends on.
	 *
	 * @param stmt in which aliased data is read.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which affect the data being read in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(Stmt) and method.isOclKindOf(SootMethod)
	 * @post result.oclIsKindOf(Pair(AssignStmt, SootMethod))
	 * @post result->forall(o | o.getFirst().getLeftOf().oclIsKindOf(FieldRef) or
	 * 		 o.getFirst().getLeftOf().oclIsKindOf(ArrayRef))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependees(final Object stmt, final Object method) {
		final Collection _result;
		final Stmt _theStmt = (Stmt) stmt;

		if (_theStmt.containsArrayRef() || _theStmt.containsFieldRef()) {
			if (staticFieldRefUD != null) {
				_result =
					CollectionUtils.union(aliasedUD.getDefs(_theStmt, (SootMethod) method),
						staticFieldRefUD.getDefs(_theStmt, (SootMethod) method));
			} else {
				_result = aliasedUD.getDefs(_theStmt, (SootMethod) method);
			}
		} else {
			_result = Collections.EMPTY_LIST;
		}

		return _result;
	}

	/**
	 * Return the statements which depend on the field/array access in <code>stmt</code> in <code>method</code>.
	 *
	 * @param stmt in which aliased data is written.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which are affectted by the data write in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(AssignStmt) and method.isOclKindOf(SootMethod)
	 * @post result.oclIsKindOf(Pair(AssignStmt, SootMethod))
	 * @post result->forall(o | o.getFirst().getRightOp().oclIsKindOf(FieldRef) or
	 * 		 o.getFirst().getRightOp().oclIsKindOf(ArrayRef))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		final Collection _result;
		final Stmt _theStmt = (Stmt) stmt;

		if (stmt instanceof DefinitionStmt && (_theStmt.containsArrayRef() || _theStmt.containsFieldRef())) {
			if (staticFieldRefUD != null) {
				_result =
					CollectionUtils.union(aliasedUD.getUses((DefinitionStmt) _theStmt, (SootMethod) method),
						staticFieldRefUD.getUses((DefinitionStmt) _theStmt, (SootMethod) method));
			} else {
				_result = aliasedUD.getUses((DefinitionStmt) _theStmt, (SootMethod) method);
			}
		} else {
			_result = Collections.EMPTY_LIST;
		}

		return _result;
	}

	/**
	 * {@inheritDoc}  This implementation is bi-directional.
	 */
	public Object getDirection() {
		return BI_DIRECTIONAL;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.REFERENCE_BASED_DATA_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.PAIR_DEP_RETRIEVER);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Reference Based Data Dependence processing");
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ReferenceBasedDataDA.analyze() - " + toString());
		}

		if (aliasedUD.isStable()) {
			stable();
		} else {
			unstable();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Reference Based Data Dependence processing");
		}
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		return aliasedUD + " " + staticFieldRefUD;
	}

	///CLOVER:ON

	/**
	 * Extracts information provided by environment at initialization time.  The user can configure this analysis to include
	 * static field reference based dependence information by passing in an implementation of <code>IUseDefInfo</code>
	 * implementation mapped to <code>IUseDefInfo.GLOBAL_USE_DEF_ID</code> constant in the information map.
	 *
	 * @throws InitializationException if an implementation that provides aliased interprocedural use-def information is not
	 * 		   provided.
	 *
	 * @pre info.get(IUseDefInfo.ALIASED_USE_DEF_ID) != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		aliasedUD = (IUseDefInfo) info.get(IUseDefInfo.ALIASED_USE_DEF_ID);

		if (aliasedUD == null) {
			throw new InitializationException(IUseDefInfo.ALIASED_USE_DEF_ID + " was not provided.");
		}

		staticFieldRefUD = (IUseDefInfo) info.get(IUseDefInfo.GLOBAL_USE_DEF_ID);

		if (staticFieldRefUD == null) {
			LOGGER.info(IUseDefInfo.GLOBAL_USE_DEF_ID + " was not provided.  Hence, static field reference based dependence"
				+ " info will not be provided.");
		}
	}
}

// End of File
