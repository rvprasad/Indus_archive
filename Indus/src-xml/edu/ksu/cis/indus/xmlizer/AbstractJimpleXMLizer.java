
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

package edu.ksu.cis.indus.xmlizer;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.io.StringWriter;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractJimpleXMLizer
  implements IProcessor {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private AbstractStmtXMLizer stmtXmlizer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private StringWriter xmlizedSystem = new StringWriter();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingClass = false;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingMethod = true;

	/**
	 * Creates a new AbstractJimpleXMLizer object.
	 *
	 * @param stmtXML DOCUMENT ME!
	 */
	AbstractJimpleXMLizer(final AbstractStmtXMLizer stmtXML) {
		stmtXmlizer = stmtXML;
		stmtXmlizer.setOutputStream(xmlizedSystem);
		stmtXmlizer.valueXMLizer.setOutputStream(xmlizedSystem);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public final String getXMLizedForm() {
		return xmlizedSystem.toString();
	}

	/**
	 * This does nothing.
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.Value, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(Value value, Context context) {
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(Stmt stmt, Context context) {
		stmtXmlizer.apply(stmt);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public final void callback(SootMethod method) {
		if (processingMethod) {
			//TODO: output method end tag
		} else {
			processingMethod = true;
		}

		// TODO: output method begin tag
		// TODO: output method information
		stmtXmlizer.newMethod(getNewMethodId());
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public final void callback(SootClass clazz) {
		if (processingClass) {
			//TODO: output class end tag
		} else {
			processingClass = true;
		}

		// TODO: output class begin tag
		// TODO: output class information
		processingMethod = false;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public final void callback(SootField field) {
		// TODO: output field tag with information
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public final void consolidate() {
		if (processingMethod) {
			;
		}

		// TODO: output method end tag
		if (processingClass) {
			;
		}

		// TODO: output class end tag
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void hookup(ProcessingController ppc) {
		ppc.register(Stmt.class, this);
		ppc.register(this);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void reset() {
		xmlizedSystem.flush();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void unhook(ProcessingController ppc) {
		ppc.unregister(Stmt.class, this);
		ppc.unregister(this);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected abstract Object getNewClassId();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected abstract Object getNewMethodId();
}

/*
   ChangeLog:
   $Log$
 */
