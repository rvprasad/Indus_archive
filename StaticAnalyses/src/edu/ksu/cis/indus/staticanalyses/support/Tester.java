
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2002, 2003, 2004.
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

package edu.ksu.cis.indus.staticanalyses.support;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import soot.jimple.Jimple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class Tester {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(Tester.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final ArrayType STR_ARRAY_TYPE = ArrayType.v(RefType.v("java.lang.String"), 1);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected Collection classes = new HashSet();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final Map times = new LinkedHashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected Collection rootMethods = new HashSet();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final Jimple jimple = Jimple.v();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	protected void addTimeLog(String key, long value) {
		times.put(getClass().getName() + ":" + key, new Long(value));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected final Scene loadupClassesAndCollectMains(String[] args) {
		Scene result = Scene.v();
		boolean flag = false;
		Collection names = Arrays.asList(args);

		for (int i = 0; i < args.length; i++) {
			result.loadClassAndSupport(args[i]);
		}

		Collection mc = new HashSet();
		mc.addAll(result.getClasses());

		for (Iterator i = mc.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (Util.implementsInterface(sc, "java.lang.Runnable")) {
				flag = true;
			}

			if (names.contains(sc.getName())) {
				Collection methods = sc.getMethods();

				for (Iterator j = methods.iterator(); j.hasNext();) {
					SootMethod sm = (SootMethod) j.next();

					try {
						populateRootMethods(sm);
					} catch (Exception e) {
						LOGGER.warn("Method " + sm + " doesnot have body.", e);
					}
				}
			}
		}

		if (flag) {
			result.getSootClass("java.lang.Runnable");

			SootClass sc = result.getSootClass("java.lang.Thread");
			SootMethod sm = sc.getMethodByName("start");
			populateRootMethods(sm);
            Util.setThreadStartBody(sm);
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected abstract void execute();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 */
	protected void populateRootMethods(SootMethod sm) {
		if (sm.getName().equals("main") && sm.getParameterCount() == 1 && sm.getParameterType(0).equals(STR_ARRAY_TYPE)) {
			rootMethods.add(sm);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected void printTimingStats() {
		System.out.println("Timing statistics:");

		for (Iterator i = times.keySet().iterator(); i.hasNext();) {
			Object e = i.next();
			System.out.println(e + " => " + times.get(e) + "ms");
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
