
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
import soot.ValueBox;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.transformations.slicer.TaggingBasedSliceResidualizer.SlicingTag;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Writer;


/**
 * This implementation xmlizes slices that are captured as tags/annotation.
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
	 * The name of the slice tag.
	 */
	private final String tagName;

	/**
	 * This generates ids for jimple entities.
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * This indicates if classes are being processed.
	 */
	private boolean processingClass;

	/**
	 * This indicates if methods are being processed.
	 */
	private boolean processingMethod;

	/**
	 * This indicates if statements are being processed.
	 */
	private boolean processingStmt;

	/**
	 * Creates an instance of this class.
	 *
	 * @param out is the writer to be used to write the xml information.
	 * @param theTagName is the name of the tag used to indicate parts of the slice in the AST.
	 * @param generator is the jimple id generator.
	 *
	 * @pre out != null and theTagName != null and generator != null
	 */
	public TagBasedSliceXMLizer(final Writer out, final String theTagName, final IJimpleIDGenerator generator) {
		super(out);
		tagName = theTagName;
		idGenerator = generator;
		processingStmt = false;
		processingMethod = false;
		processingClass = false;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.Value, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		SootMethod method = context.getCurrentMethod();
		Stmt stmt = context.getStmt();

		try {
			SlicingTag tag = (SlicingTag) stmt.getTag(tagName);

			if (tag != null) {
				writer.write("\t\t\t\t<value id=\"" + idGenerator.getIdForValue(vBox, stmt, method) + "\"/>\n");
			}
		} catch (IOException e) {
			LOGGER.error("Exception while writing information about " + vBox + " occurring in " + stmt + " and "
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
				writer.write("\t\t\t</stmt>\n");
				processingStmt = false;
			}

			SlicingTag tag = (SlicingTag) stmt.getTag(tagName);

			if (tag != null) {
				writer.write("\t\t\t<stmt id=\"" + idGenerator.getIdForStmt(stmt, method) + "\">\n");
				processingStmt = true;
			}
		} catch (IOException e) {
			LOGGER.error("Exception while writing information about " + stmt + " occurring in " + method.getSignature(), e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		try {
			if (processingStmt) {
				writer.write("\t\t\t</stmt>\n");
				processingStmt = false;
			}

			if (processingMethod) {
				writer.write("\t\t</method>\n");
				processingMethod = false;
			}

			SlicingTag tag = (SlicingTag) method.getTag(tagName);

			if (tag != null) {
				writer.write("\t\t<method id=\"" + idGenerator.getIdForMethod(method) + "\">\n");
				processingMethod = true;
			}
		} catch (IOException e) {
			LOGGER.error("Exception while writing xml information about " + method.getSignature(), e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		try {
			if (processingStmt) {
				writer.write("\t\t\t</stmt>\n");
				processingStmt = false;
			}

			if (processingMethod) {
				writer.write("\t\t</method>\n");
				processingMethod = false;
			}

			if (processingClass) {
				writer.write("\t</class>\n");
				processingClass = false;
			}

			SlicingTag tag = (SlicingTag) clazz.getTag(tagName);

			if (tag != null) {
				writer.write("\t<class id=\"" + idGenerator.getIdForClass(clazz) + "\">\n");
				processingClass = true;
			}
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
				writer.write("\t\t<field id=\"" + idGenerator.getIdForField(field) + "\"/>\n");
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
			if (processingMethod) {
				writer.write("\t\t</method>\n");
			}

			if (processingClass) {
				writer.write("\t</class>\n");
			}

			writer.write("</system>\n");
		} catch (IOException e) {
			LOGGER.error("Exception while finishing up writing xml information.", e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		try {
			writer.write("<system>\n");
		} catch (IOException e) {
			LOGGER.error("Exception while starting up writing xml information.", e);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/11/23 19:41:04  venku
   - incorrect tags were being emitted.  FIXED.

   Revision 1.5  2003/11/17 15:56:56  venku
   - removed support to retrieve new statement ids.
   - added support to retrieve id for value boxes.
   Revision 1.4  2003/11/17 15:42:42  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.3  2003/11/17 15:25:17  venku
   - added new method to AbstractSliceXMLizer to flush writer.
   - called flush on xmlizer from the driver.
   - erroneous file name was being constructed. FIXED.
   - added tabbing and new line to output in TagBasedSliceXMLizer.
   Revision 1.2  2003/11/17 02:23:52  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.1  2003/11/17 01:39:42  venku
   - added slice XMLization support.
 */
