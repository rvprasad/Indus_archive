
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

package edu.ksu.cis.indus.staticanalyses.dependency.drivers;

import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDA;

import java.util.ArrayList;


/**
 * This class drives identifier-based data dependence analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class IBDDADriver
  extends DADriver {
	/**
	 * Creates a new IBDDADriver object.
	 *
	 * @param args is command line arguments.
	 */
	protected IBDDADriver(final String[] args) {
		super(args);
	}

	/**
	 * Entry point of the driver.
	 *
	 * @param args command line arguemnts.
	 */
	public static void main(final String[] args) {
		(new IBDDADriver(args)).run();
	}

	/**
	 * Initializes the collection of dependence analyses with the identifier-based data dependence analyses to be driven.
	 */
	protected void initialize() {
		das = new ArrayList();
		das.add(new IdentifierBasedDataDA());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/09/02 12:28:41  venku
   - Installing drivers for all dependence analyses.
 */
