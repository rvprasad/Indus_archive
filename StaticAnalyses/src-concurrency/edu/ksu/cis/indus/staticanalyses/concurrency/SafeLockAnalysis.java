
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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 * TODO: Implement this.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SafeLockAnalysis
  extends AbstractAnalysis {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(SafeLockAnalysis.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	IMonitorInfo monitorInfo;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean stable;

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
	    stable = false;
		if (monitorInfo.isStable()) {
			// process c1, c2, and c3
			stable = true;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		monitorInfo = null;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		monitorInfo = (IMonitorInfo) info.get(IMonitorInfo.ID);

		if (monitorInfo == null) {
			LOGGER.error("An interface with id, " + IMonitorInfo.ID + ", was not provided.");
			throw new InitializationException("An interface with id, " + IMonitorInfo.ID + ", was not provided.");
		}
	}

}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.

   Revision 1.5  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.4  2003/09/28 03:17:13  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/09/15 01:42:21  venku
   - removed unnecessary TODO markers.
   Revision 1.2  2003/09/12 23:21:15  venku
   - committing to avoid annoyance.
   Revision 1.1  2003/09/12 23:15:40  venku
   - committing to avoid annoyance.
 */
