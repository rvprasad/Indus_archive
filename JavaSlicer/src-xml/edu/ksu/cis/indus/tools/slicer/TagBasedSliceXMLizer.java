
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

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.slicer.SlicingTag;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This implementation xmlizes slices that are captured as tags/annotation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class TagBasedSliceXMLizer extends AbstractProcessor {
    /**
     * The logger used by instances of this class to log messages.
     */
    private static final Log LOGGER = LogFactory.getLog(TagBasedSliceXMLizer.class);

    /**
     * This is the file/stream into which the xml output will be written into.
     */
    protected final Writer writer;

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
	    writer = out;
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
		final SootMethod _method = context.getCurrentMethod();
		final Stmt _stmt = context.getStmt();

		try {
			final SlicingTag _tag = (SlicingTag) vBox.getTag(tagName);

			if (_tag != null) {
				writer.write("\t\t\t\t<value id=\"" + idGenerator.getIdForValueBox(vBox, _stmt, _method) + "\"/>\n");
			}
		} catch (IOException _e) {
			LOGGER.error("Exception while writing information about " + vBox + " occurring in " + _stmt + " and "
				+ _method.getSignature(), _e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();

		try {
			if (processingStmt) {
				writer.write("\t\t\t</stmt>\n");
				processingStmt = false;
			}

			final SlicingTag _tag = (SlicingTag) stmt.getTag(tagName);

			if (_tag != null) {
				writer.write("\t\t\t<stmt id=\"" + idGenerator.getIdForStmt(stmt, _method) + "\">\n");
				processingStmt = true;
			}
		} catch (IOException _e) {
			LOGGER.error("Exception while writing information about " + stmt + " occurring in " + _method.getSignature(), _e);
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

			final SlicingTag _tag = (SlicingTag) method.getTag(tagName);

			if (_tag != null) {
				writer.write("\t\t<method id=\"" + idGenerator.getIdForMethod(method) + "\">\n");
				processingMethod = true;
			}
		} catch (IOException _e) {
			LOGGER.error("Exception while writing xml information about " + method.getSignature(), _e);
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

			final SlicingTag _tag = (SlicingTag) clazz.getTag(tagName);

			if (_tag != null) {
				writer.write("\t<class id=\"" + idGenerator.getIdForClass(clazz) + "\">\n");
				processingClass = true;
			}
		} catch (IOException _e) {
			LOGGER.error("Exception while writing xml information about " + clazz.getName(), _e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public void callback(final SootField field) {
		final SlicingTag _tag = (SlicingTag) field.getTag(tagName);

		if (_tag != null) {
			try {
				writer.write("\t\t<field id=\"" + idGenerator.getIdForField(field) + "\"/>\n");
			} catch (IOException _e) {
				LOGGER.error("Exception while writing xml information about " + field.getSignature(), _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		try {
			if (processingStmt) {
				writer.write("\t\t</stmt>\n");
			}

			if (processingMethod) {
				writer.write("\t\t</method>\n");
			}

			if (processingClass) {
				writer.write("\t</class>\n");
			}

			writer.write("</system>\n");
		} catch (IOException _e) {
			LOGGER.error("Exception while finishing up writing xml information.", _e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		try {
			writer.write("<system>\n");
		} catch (IOException _e) {
			LOGGER.error("Exception while starting up writing xml information.", _e);
		}
	}

    /**
     * Flushes and closes the associated stream.
     */
    public  void flush() {
    	try {
    		writer.flush();
    		writer.close();
    	} catch (IOException _e) {
    		LOGGER.error("Exception while closing slice xmlization stream.", _e);
    	}
    }

    /**
     * Registers interests in all values, statements, and interfaces level entities.
     *
     * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
     */
    public  void hookup(final ProcessingController ppc) {
    	ppc.registerForAllStmts(this);
    	ppc.register(this);
    	ppc.registerForAllValues(this);
    }

    /**
     * Unregisters interests in all values, statements, and interfaces level entities.
     *
     * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
     */
    public  void unhook(final ProcessingController ppc) {
    	ppc.unregisterForAllStmts(this);
    	ppc.unregister(this);
    	ppc.unregisterForAllValues(this);
    }
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.12  2003/11/30 09:46:38  venku
   - coding conventions.
   Revision 1.11  2003/11/30 09:45:35  venku
   - tag check on statement was used while tagging valueBox. FIXED.
   Revision 1.10  2003/11/25 16:23:08  venku
   - closing tag missing for statements. FIXED.
   Revision 1.9  2003/11/24 16:51:34  venku
   - ripple effect of moving inner classes in TaggingBasedSliceCollector as external classes.
   Revision 1.8  2003/11/24 10:12:03  venku
   - there are no residualizers now.  There is a very precise
     slice collector which will collect the slice via tags.
   - architectural change. The slicer is hard-wired wrt to
     slice collection.  Residualization is outside the slicer.
   Revision 1.7  2003/11/24 00:11:42  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
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
