
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.dependency.controller;

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * An naive implementation of Controller. This implementation will run the analysis in the order of METHOD_LOCAL_DATA,
 * CLASS_DATA, CONTROL, SYNCHRONIZATION, INTERFERENCE, DIVERGENCE, and READY.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SimpleController
  extends Controller {
	/**
	 * <p>
	 * Creates a new SimpleController object.
	 * </p>
	 *
	 * @param info is a map from name to objects which provide information that analyses may use, but is of no use to the
	 * 		  controller.
	 * @param pc is the preprocess controller.
	 */
	public SimpleController(Map info, ProcessingController pc) {
		super(info, pc);

		List temp = new ArrayList();
		temp.add(METHOD_LOCAL_DATA_DA);
		temp.add(CLASS_DATA_DA);
		temp.add(CONTROL_DA);
		temp.add(SYNCHRONIZATION_DA);
		temp.add(INTERFERENCE_DA);
		temp.add(DIVERGENCE_DA);
		temp.add(READY_DA);
		participatingAnalysesNames = Collections.unmodifiableList(temp);
	}

	/**
	 * <p>
	 * Executes the analysis in the order they were registered.
	 * </p>
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.Controller#execute()
	 */
	public void execute() {
		boolean analyzing;

		do {
			analyzing = false;

			for(Iterator i = participatingAnalysesNames.iterator(); i.hasNext();) {
				String daName = (String) i.next();
				DependencyAnalysis temp = (DependencyAnalysis) participatingAnalyses.get(daName);

				if(temp != null) {
					analyzing |= temp.analyze();
				}
			}
		} while(analyzing);
		stable = true;
	}
}

/*****
 ChangeLog:

$Log$

*****/
