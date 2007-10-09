/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.RefType;
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
		extends AbstractSliceCriteriaPredicate<Stmt> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AssertionSliceCriteriaPredicate.class);

	/**
	 * Creates an instance of this class.
	 */
	public AssertionSliceCriteriaPredicate() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(final Stmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("evaluate(entity = " + stmt + ":" + stmt.getClass() + ")");
		}

		boolean _result = false;

		for (final Iterator<ValueBox> _i = stmt.getUseAndDefBoxes().iterator(); _i.hasNext() && !_result;) {
			final ValueBox _vb = _i.next();
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
