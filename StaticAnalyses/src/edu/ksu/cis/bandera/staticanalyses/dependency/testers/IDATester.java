
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

package edu.ksu.cis.bandera.staticanalyses.dependency.testers;

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.bandera.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.bandera.staticanalyses.dependency.InterferenceDAv2;
import edu.ksu.cis.bandera.staticanalyses.dependency.controller.CGBasedProcessingController;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interference.EquivalenceClassBasedAnalysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class IDATester
  extends DATester {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(IDATester.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection das;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private EquivalenceClassBasedAnalysis ecba;

	/**
	 * Creates a new IDATester object.
	 */
	protected IDATester() {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String args[]) {
		IDATester t = new IDATester();
		t.initialize();
		t.run(args);
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.testers.DATester#getDependencyAnalyses()
	 */
	protected Collection getDependencyAnalyses() {
		CallGraphInfo cgi = (CallGraphInfo) info.get(CallGraphInfo.ID);

		if(cgi == null) {
			throw new IllegalArgumentException(CallGraphInfo.ID + " was not provided in info.");
		}

		ThreadGraphInfo tgi = (ThreadGraphInfo) info.get(ThreadGraphInfo.ID);

		if(tgi == null) {
			throw new IllegalArgumentException(ThreadGraphInfo.ID + " was not provided in info.");
		}
		ecba = new EquivalenceClassBasedAnalysis(scm, cgi, tgi);
		info.put(EquivalenceClassBasedAnalysis.ID, ecba);

		ProcessingController ppc = new CGBasedProcessingController(cgi);
		ppc.setAnalyzer(aa);

		for(Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if(da.getPreProcessor() != null) {
				da.getPreProcessor().hookup(ppc);
			}
			da.initialize(method2cmpltStmtGraph, info);
		}
		ecba.hookup(ppc);
		LOGGER.info("BEGIN: preprocessing for dependency analyses");
		long start = System.currentTimeMillis();
		ppc.process();
		long stop = System.currentTimeMillis();
		addTimeLog("Dependency preprocessing", stop - start);
		LOGGER.info("END: preprocessing for dependency analyses");
		ecba.unhook(ppc);
		ecba.execute();

		for(Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if(da.getPreProcessor() != null) {
				da.getPreProcessor().unhook(ppc);
			}
		}
		return Collections.unmodifiableCollection(das);
	}

	/**
	 * Creates a new IDATester object.
	 */
	protected void initialize() {
		das = new ArrayList();
		das.add(new InterferenceDAv1());
		das.add(new InterferenceDAv2());
	}
}

/*****
 ChangeLog:

$Log$

*****/
