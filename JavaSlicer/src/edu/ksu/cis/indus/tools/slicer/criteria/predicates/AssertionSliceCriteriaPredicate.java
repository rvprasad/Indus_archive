
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

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.RefType;
import soot.SootField;
import soot.Value;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This filter can be used to identify code that are involved in <code>assert</code> statements <b>as compiled by Sun's java
 * compiler</b>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class AssertionSliceCriteriaPredicate
  extends AbstractSliceCriteriaPredicate {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AssertionSliceCriteriaPredicate.class);

	/**
	 * {@inheritDoc}
	 *
	 * @pre entity.oclIsKindOf(Stmt)
	 */
	public boolean evaluate(final Object entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("evaluate(entity = " + entity + ":" + entity.getClass() + ")");
		}

		final Stmt _stmt = (Stmt) entity;
		boolean _result = false;

		if (_stmt.containsFieldRef()) {
			final SootField _field = _stmt.getFieldRef().getField();
			_result |= _field.getName().equals("$assertionDisabled");
		}

		for (final Iterator _i = _stmt.getUseAndDefBoxes().iterator(); _i.hasNext() && !_result;) {
			final ValueBox _vb = (ValueBox) _i.next();
			final Value _v = _vb.getValue();

			if (_v.getType() instanceof RefType) {
				final RefType _rt = (RefType) _v.getType();
				final String _name = _rt.getClassName();
				_result |= _name.equals("java.lang.AssertionError");
			}
		}
		return _result;
	}
}

// End of File
