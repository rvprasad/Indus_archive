
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

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.NamedTag;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.IProcessingFilter;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.CustomXMLOutputter;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.znerd.xmlenc.XMLOutputter;

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
final class TagBasedSliceXMLizer
  extends AbstractXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(TagBasedSliceXMLizer.class);

	/**
	 * The name of the tag to residualize.
	 *
	 * @invariant tagName != null
	 */
	final String tagName;

	/**
	 * The processor used while xmlization.
	 *
	 * @invariant processor != null
	 */
	private final TagBasedSliceProcessor processor;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theTagName is the name of the tag used to indicate parts of the slice in the AST.
	 * @param generator is the jimple id generator.
	 *
	 * @pre out != null and theTagName != null and generator != null
	 */
	public TagBasedSliceXMLizer(final String theTagName, final IJimpleIDGenerator generator) {
		setGenerator(generator);
		processor = new TagBasedSliceProcessor(generator);
		tagName = theTagName;
	}

	/**
	 * This class processes the system during xmlization.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class TagBasedSliceProcessor
	  extends AbstractProcessor {
		/**
		 * This is the file/stream into which the xml output will be written into.
		 */
		protected XMLOutputter writer;

		/**
		 * This generates ids for jimple entities.
		 */
		private final IJimpleIDGenerator idGenerator;

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
		 * @param generator used to generate the id's for AST fragments during xmlization.
		 *
		 * @pre generator != null
		 */
		public TagBasedSliceProcessor(final IJimpleIDGenerator generator) {
			idGenerator = generator;
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.Value, edu.ksu.cis.indus.processing.Context)
		 */
		public void callback(final ValueBox vBox, final Context context) {
			final SootMethod _method = context.getCurrentMethod();
			final Stmt _stmt = context.getStmt();

			try {
				final NamedTag _tag = (NamedTag) vBox.getTag(tagName);

				if (_tag != null) {
					writer.startTag("value");
					writer.attribute("id", idGenerator.getIdForValueBox(vBox, _stmt, _method));
					writer.endTag();
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
					writer.endTag();
					processingStmt = false;
				}

				final NamedTag _tag = (NamedTag) stmt.getTag(tagName);

				if (_tag != null) {
					writer.startTag("stmt");
					writer.attribute("id", idGenerator.getIdForStmt(stmt, _method));
					processingStmt = true;
				}
			} catch (IOException _e) {
				LOGGER.error("Exception while writing information about " + stmt + " occurring in " + _method.getSignature(),
					_e);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
		 */
		public void callback(final SootMethod method) {
			try {
				if (processingStmt) {
					writer.endTag();
					processingStmt = false;
				}

				if (processingMethod) {
					writer.endTag();
					processingMethod = false;
				}

				final NamedTag _tag = (NamedTag) method.getTag(tagName);

				if (_tag != null) {
					writer.startTag("method");
					writer.attribute("id", idGenerator.getIdForMethod(method));
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
					writer.endTag();
					processingStmt = false;
				}

				if (processingMethod) {
					writer.endTag();
					processingMethod = false;
				}

				if (processingClass) {
					writer.endTag();
					processingClass = false;
				}

				final NamedTag _tag = (NamedTag) clazz.getTag(tagName);

				if (_tag != null) {
					writer.startTag("class");
					writer.attribute("id", idGenerator.getIdForClass(clazz));
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
			final NamedTag _tag = (NamedTag) field.getTag(tagName);

			if (_tag != null) {
				try {
					writer.startTag("field");
					writer.attribute("id", idGenerator.getIdForField(field));
					writer.endTag();
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
				writer.endDocument();
			} catch (IOException _e) {
				LOGGER.error("Exception while finishing up writing xml information.", _e);
			}
		}

		/**
		 * Registers interests in all values, statements, and interfaces level entities.
		 *
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.registerForAllStmts(this);
			ppc.register(this);
			ppc.registerForAllValues(this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
		 */
		public void processingBegins() {
			try {
			    writer.declaration();
			    writer.dtd("system", "-//INDUS:JAVASLICER:TAGBASEDSLICE//DTD project//EN", "slice.xsd");
				writer.startTag("system");
			} catch (IOException _e) {
				LOGGER.error("Exception while starting up writing xml information.", _e);
			}
		}

		/**
		 * Unregisters interests in all values, statements, and interfaces level entities.
		 *
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregisterForAllStmts(this);
			ppc.unregister(this);
			ppc.unregisterForAllValues(this);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.AbstractXMLizer#getFileName(java.lang.String)
	 */
	public String getFileName(final String name) {
		String _result = xmlizeString(name);

		if (_result.length() == 0) {
			_result = "slice.xml";
		} else {
			_result = "slice_" + _result + ".xml";
		}
		return _result;
	}

	/**
	 * Writes the slice as XML document.
	 *
	 * @param info maps various ids to their implementations as required by this xmlizer.
	 *
	 * @pre info != null
	 * @pre info.containsKey(IStmtGraphFactory.ID) and info.containsKey(IEnvironment.ID) and info.containsKey(FILE_NAME_ID)
	 */
	public void writeXML(final Map info) {
		final ProcessingController _ctrl = new ProcessingController();
		_ctrl.setStmtGraphFactory((IStmtGraphFactory) info.get(IStmtGraphFactory.ID));
		_ctrl.setEnvironment((IEnvironment) info.get(IEnvironment.ID));

		final IProcessingFilter _filter = new TagBasedProcessingFilter(tagName);
		_filter.chain(new XMLizingProcessingFilter());
		_ctrl.setProcessingFilter(_filter);

		try {
			final File _f = new File(getXmlOutputDir() + File.separator + getFileName((String) info.get(FILE_NAME_ID)));
			final FileWriter _writer = new FileWriter(_f);
			processor.writer = new CustomXMLOutputter(_writer, "UTF-8");
			processor.hookup(_ctrl);
			_ctrl.process();
			processor.unhook(_ctrl);
			_writer.flush();
			_writer.close();
		} catch (IOException _e) {
			LOGGER.error("Error while xmlizing OFA information ", _e);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.22  2004/05/10 08:12:03  venku
   - streamlined the names of tags that are used.
   - deleted SlicingTag class.  NamedTag is used instead.
   - ripple effect.
   - SliceCriteriaFactory's interface is enhanced to generate individual
     slice criterion as well as criteria set for all nodes in the given AST chunk.

   Revision 1.21  2004/05/09 09:59:59  venku
   - closed tags prematurely.  FIXED.
   Revision 1.20  2004/05/09 08:24:38  venku
   - all xmlizers use xmlenc to write xml data.
   - Hence, new library dependence on xmlenc.jar.
   Revision 1.19  2004/04/23 00:42:37  venku
   - trying to get canonical xmlized Jimple representation.
   Revision 1.18  2004/04/22 23:32:32  venku
   - xml file name were setup incorrectly.  FIXED.
   Revision 1.17  2004/04/22 22:59:58  venku
   - coding conventions.
   Revision 1.16  2004/04/20 06:53:15  venku
   - documentation.
   Revision 1.15  2004/04/18 08:59:00  venku
   - enabled test support for slicer.
   Revision 1.14  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
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
