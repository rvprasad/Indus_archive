
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

import edu.ksu.cis.indus.staticanalyses.dependency.DivergenceDA;

import java.util.ArrayList;


/**
 * This class drives divergence dependence analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class DDADriver
  extends DADriver {
	/**
	 * Creates a new DDADriver object.
	 *
	 * @param args is command line arguments.
	 */
	protected DDADriver(final String[] args) {
		super(args);
		ecbaRequired = false;
	}

	/**
	 * Entry point of the driver.
	 *
	 * @param args command line arguemnts.
	 */
	public static void main(final String[] args) {
		(new DDADriver(args)).run();
	}

	/**
	 * Initializes the collection of dependence analyses with the divergence dependence analyses to be driven.
	 */
	protected void initialize() {
		das = new ArrayList();

		DivergenceDA dda = new DivergenceDA();
		dda.setConsiderCallSites(true);
		das.add(dda);
		das.add(new DivergenceDA());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.2  2003/09/11 02:39:48  venku
   - drives 2 analyses: intraprocedural and interprocedural.
   Revision 1.1  2003/09/02 12:28:41  venku
   - Installing drivers for all dependence analyses.
 */
