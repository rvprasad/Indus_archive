
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

package edu.ksu.cis.indus.tools.slicer;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.transformations.slicer.TagBasedSlicingTransformer.SlicingTag;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class TagBasedSliceXMLizer
  extends AbstractSliceXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TagBasedSliceXMLizer.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final String tagName;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingClass;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingMethod;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingStmt;

	/**
	 * DOCUMENT ME!
	 *
	 * @param oDir DOCUMENNT ME!
	 * @param theTagName DOCUMENT ME!
	 * @param generator DOCUMENT ME!
	 */
	public TagBasedSliceXMLizer(final String oDir, final String theTagName, final IJimpleIDGenerator generator) {
		super(oDir);
		tagName = theTagName;
		idGenerator = generator;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.Value, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Value value, final Context context) {
		SootMethod method = context.getCurrentMethod();
		Stmt stmt = context.getStmt();

		try {
			SlicingTag tag = (SlicingTag) stmt.getTag(tagName);

			if (tag != null) {
				writer.write("<value id=\"" + idGenerator.getIdForStmt(stmt, method) + "\"/>");
			}
		} catch (IOException e) {
			LOGGER.error("Exception while writing information about " + value + " occurring in " + stmt + " and "
				+ method.getSignature(), e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		SootMethod method = context.getCurrentMethod();

		try {
			if (processingStmt) {
				writer.write("</stmt>");
			}

			SlicingTag tag = (SlicingTag) stmt.getTag(tagName);

			if (tag != null) {
				writer.write("<stmt id=\"" + idGenerator.getIdForStmt(stmt, method) + "\">");
			}

			processingStmt = true;
		} catch (IOException e) {
			LOGGER.error("Exception while writing information about " + stmt + " occurring in " + method.getSignature(), e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		try {
			if (processingMethod) {
				writer.write("</method>");
				processingStmt = false;
			}

			SlicingTag tag = (SlicingTag) method.getTag(tagName);

			if (tag != null) {
				writer.write("<method id=\"" + idGenerator.getIdForMethod(method) + "\">");
			}

			processingMethod = true;
		} catch (IOException e) {
			LOGGER.error("Exception while writing xml information about " + method.getSignature(), e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		try {
			if (processingMethod) {
				writer.write("</method>");
				processingMethod = false;
			}

			if (processingClass) {
				writer.write("</class>");
			}

			SlicingTag tag = (SlicingTag) clazz.getTag(tagName);

			if (tag != null) {
				writer.write("<class id=\"" + idGenerator.getIdForClass(clazz) + "\">");
			}
			processingClass = true;
		} catch (IOException e) {
			LOGGER.error("Exception while writing xml information about " + clazz.getName(), e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public void callback(final SootField field) {
		SlicingTag tag = (SlicingTag) field.getTag(tagName);

		if (tag != null) {
			try {
				writer.write("<field id=\"" + idGenerator.getIdForField(field) + "\"/>");
			} catch (IOException e) {
				LOGGER.error("Exception while writing xml information about " + field.getSignature(), e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		try {
			writer.write("</system>");
		} catch (IOException e) {
			LOGGER.error("Exception while finishing up writing xml information.", e);
		}
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		try {
			writer.write("<system>");
		} catch (IOException e) {
			LOGGER.error("Exception while starting up writing xml information.", e);
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
