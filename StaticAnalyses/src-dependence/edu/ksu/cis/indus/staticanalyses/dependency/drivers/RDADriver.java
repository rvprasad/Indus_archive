
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.dependency.drivers;

import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;

import java.util.ArrayList;


/**
 * This class drives ready dependency analyses.
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class RDADriver
  extends IDADriver {
	/**
	 * Creates a new RDADriver object.
	 *
	 * @param args is command line arguments.
	 */
	protected RDADriver(final String[] args) {
		super(args);
	}

	/**
	 * Entry point of the driver.
	 *
	 * @param args command line arguemnts.
	 */
	public static void main(final String[] args) {
		(new RDADriver(args)).run();
	}

	/**
	 * Initializes the collection of dependence analyses with the ready dependence analyses to be driven.
	 */
	protected void initialize() {
		das = new ArrayList();
		das.add(new ReadyDAv1());
		das.add(new ReadyDAv2());
	}
}

/*
   ChangeLog:
   $Log$
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
