
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

package edu.ksu.cis.bandera.staticanalyses.support;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.ClassFile;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashSet;


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
	public static final ArrayType strArray = ArrayType.v(RefType.v("java.lang.String"), 1);

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
	protected Collection rootMethods = new HashSet();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected final SootClassManager loadupClassesAndCollectMains(String args[]) {
		SootClassManager result = new SootClassManager();
		Jimple jimple = Jimple.v();
		ClassFile classfile = ClassFile.v();

		for(int i = 0; i < args.length; i++) {
			result.getClass(args[i]);
		}

		ca.mcgill.sable.util.Collection mc = new ca.mcgill.sable.util.HashSet();
		mc.addAll(result.getClasses());

		for(ca.mcgill.sable.util.Iterator i = mc.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();
			ca.mcgill.sable.util.Collection methods = sc.getMethods();

			for(ca.mcgill.sable.util.Iterator j = methods.iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();

				try {
					JimpleBody body = new JimpleBody(sm, sm.getBody(classfile), 0);
					sm.storeBody(jimple, body);
					populateRootMethods(sm);
				} catch(Exception e) {
					LOGGER.warn("Method " + sm + " doesnot have body.", e);
				}
			}
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 */
	protected void populateRootMethods(SootMethod sm) {
		if(sm.getName().equals("main") && sm.getParameterCount() == 1 && sm.getParameterType(0).equals(strArray)) {
			rootMethods.add(sm);
		}
		Util.setThreadStartBody(sm);
	}
}

/*****
 ChangeLog:

$Log$

*****/
