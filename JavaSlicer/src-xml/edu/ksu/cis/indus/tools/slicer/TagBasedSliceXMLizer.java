
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
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
public final class TagBasedSliceXMLizer
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
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
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
	 * @see edu.ksu.cis.indus.xmlizer.IXMLizer#getFileName(java.lang.String)
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
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory((IStmtGraphFactory) info.get(IStmtGraphFactory.ID));
		_ctrl.setStmtSequencesRetriever(_ssr);
		_ctrl.setEnvironment((IEnvironment) info.get(IEnvironment.ID));

		final IProcessingFilter _filter = new TagBasedProcessingFilter(tagName);
		_filter.chain(new XMLizingProcessingFilter());
		_ctrl.setProcessingFilter(_filter);

		try {
			final File _f = new File(getXmlOutputDir() + File.separator + getFileName((String) info.get(FILE_NAME_ID)));
			final FileWriter _writer = new FileWriter(_f);
			processor.writer = new CustomXMLOutputter(_writer);
			processor.hookup(_ctrl);
			_ctrl.process();
			processor.unhook(_ctrl);
			_writer.flush();
			_writer.close();
		} catch (IOException _e) {
			LOGGER.error("Error while xmlizing slice information ", _e);
		}
	}
}

// End of File
