
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

package edu.ksu.cis.indus.staticanalyses.tokens.soot;

import edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationEvaluator;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import soot.jimple.NullConstant;


/**
 * This implementation detects relation between values and new types that are explored during  analysis.  This is required in
 * situations when a value can be assigned to types of sort X but not all types of sort X are  explored before seeing the
 * value during analysis.  An example is <code>null</code> can be assigned to any reference types  but not all reference
 * types will be explored before <code>null</code> is explored.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class SootDynamicTokenTypeEvaluator
  implements IDynamicTokenTypeRelationEvaluator {
	/** 
	 * This predicate filters out <code>NullConstant</code> values.
	 */
	public static final Predicate NULL_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object object) {
				return object instanceof NullConstant;
			}
		};

	/** 
	 * This is the collection of types that have already been marked as compatible with <code>null</code>.
	 */
	private final Collection typesForNullConst = new ArrayList();

	/** 
	 * This indicates that <code>null</code> was seen.
	 */
	private boolean nullConstSeen;

	/**
	 * @see IDynamicTokenTypeRelationEvaluator#getValuesConformingTo(Collection, IType)
	 */
	public Collection getValuesConformingTo(final Collection values, final IType type) {
		Collection _result;
		nullConstSeen = nullConstSeen || CollectionUtils.exists(values, NULL_PREDICATE);

		if (nullConstSeen && !typesForNullConst.contains(type)) {
			typesForNullConst.add(type);
			_result = Collections.singleton(CollectionUtils.find(values, NULL_PREDICATE));
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationEvaluator#reset()
	 */
	public void reset() {
		nullConstSeen = false;
		typesForNullConst.clear();
	}
}

// End of File
