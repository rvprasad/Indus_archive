
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.staticanalyses.InitializationException;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class InterferenceDAv4
  extends InterferenceDAv3 {
	/**
	 * @see InterferenceDAv3#isDependentOn(Pair, Pair)
	 */
	protected boolean isDependentOn(Pair dependent, Pair dependee) {
		boolean _result = super.isDependentOn(dependent, dependee);

		if (_result) {
			; // TODO:
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws InitializationException when object flow analysis is not provided.
	 *
	 * @pre info.get(OFAnalyzer.ID) != null and info.get(OFAnalyzer.ID).oclIsTypeOf(OFAnalyzer)
	 *
	 * @see InterferenceDAv3#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
	}
}

/*
   ChangeLog:
   $Log$
 */
