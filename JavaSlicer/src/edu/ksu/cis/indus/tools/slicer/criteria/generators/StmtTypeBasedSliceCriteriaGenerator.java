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

package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.ReflectionBasedSupertypePredicate;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.Stmt;

/**
 * This class can be used to generate slice criteria based on statements of certain types/classes specified by the user. The
 * criteria is also guided by a specification matcher.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class StmtTypeBasedSliceCriteriaGenerator
		extends AbstractStmtBasedSliceCriteriaGenerator<Stmt> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StmtTypeBasedSliceCriteriaGenerator.class);

	/**
	 * The types of the statements to be considered as slice criteria.
	 */
	private final Collection<Class<? extends Stmt>> stmtTypes = new HashSet<Class<? extends Stmt>>();

	/**
	 * This is used to check type conformance.
	 */
	private final ReflectionBasedSupertypePredicate<Stmt> subClassPredicate = new ReflectionBasedSupertypePredicate<Stmt>();

	/**
	 * Sets the types of the statements to be considered as slice criteria.
	 * 
	 * @param <T> is the type of the statement AST nodes.
	 * @param types of the statements.
	 */
	public <T extends Stmt> void setStmtTypes(@NonNull @NonNullContainer final Collection<Class<T>> types) {
		stmtTypes.clear();
		stmtTypes.addAll(types);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected boolean shouldConsiderStmt(final Stmt stmt) {
		subClassPredicate.setsubType(stmt.getClass());
		final boolean _result = CollectionUtils.exists(stmtTypes, subClassPredicate);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("shouldConsiderStmt(Stmt stmt = " + stmt + ":" + stmt.getClass() + ") = " + _result);
		}

		return _result;
	}
}

// End of File
