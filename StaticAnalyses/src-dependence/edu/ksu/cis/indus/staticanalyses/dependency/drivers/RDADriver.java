
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

import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv3;

import java.util.ArrayList;


/**
 * This class drives ready dependency analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @deprecated
 */
public final class RDADriver
  extends DADriver {
	/**
	 * Creates a new RDADriver object.
	 *
	 * @param args is command line arguments.
	 */
	protected RDADriver(final String[] args) {
		super(args);
		ecbaRequired = true;
	}

	/**
	 * Entry point of the driver.
	 *
	 * @param args command line arguemnts.
	 */
	public static void main(final String[] args) {
		DADriver da = new RDADriver(args);

		ReadyDAv1 rd = new ReadyDAv1();
		da.das.add(rd);
		rd = new ReadyDAv2();
		da.das.add(rd);
		rd = new ReadyDAv3();
		da.das.add(rd);
		da.execute();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.11  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.10  2003/11/07 12:00:04  venku
   - added driving code for ReadyDAv3.
   Revision 1.9  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.8  2003/09/02 11:30:56  venku
   - Enabled toggling ECBA instance.
   Revision 1.7  2003/09/01 11:56:59  venku
   - set the rules for some test, and the reminiscient code is lying.
   Revision 1.6  2003/08/25 11:47:37  venku
   Fixed minor glitches.
   Revision 1.5  2003/08/25 09:15:52  venku
   Initialization of interProcedural was missing in ReadyDAv1.
   Ripple effect of this and previous change in ReadyDAv1/2 in RDADriver.
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
