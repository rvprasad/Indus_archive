
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.IProcessingFilter;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.CustomXMLOutputter;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.znerd.xmlenc.XMLOutputter;

import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class can be used to xmlize object flow information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class OFAXMLizer
  extends AbstractXMLizer {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(OFAXMLizer.class);

	/**
	 * This class is used by the xmlizer to xmlize OFA information.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class OFAXMLizingProcessor
	  extends AbstractProcessor {
		/** 
		 * The id generator to be used during xmlization.
		 */
		private final IJimpleIDGenerator idGenerator;

		/** 
		 * The OFA instance whose information should be xmlized.
		 */
		private OFAnalyzer ofa;

		/** 
		 * The instance used to write xml data.
		 */
		private XMLOutputter xmlWriter;

		/** 
		 * This indicates if a class is being processed.
		 */
		private boolean processingClass;

		/** 
		 * This indicates if a method is being processed.
		 */
		private boolean processingMethod;

		/**
		 * Creates an instance of the processor.
		 *
		 * @param filewriter via which the xml data will be written.
		 * @param analyzer is the OFA instance whose information should be xmlized.
		 *
		 * @pre filewriter != null and analyzer != null
		 */
		public OFAXMLizingProcessor(final FileWriter filewriter, final OFAnalyzer analyzer) {
			idGenerator = getIdGenerator();
			ofa = analyzer;

			try {
				xmlWriter = new CustomXMLOutputter(filewriter);
			} catch (final UnsupportedEncodingException _e) {
				LOGGER.error("This is not supposed to happen", _e);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
		 */
		public void callback(final ValueBox vBox, final Context context) {
			context.setProgramPoint(vBox);

			final List _temp = new ArrayList();

			for (final Iterator _i = ofa.getValues(vBox.getValue(), context).iterator(); _i.hasNext();) {
				_temp.add(_i.next().toString());
			}

			if (!_temp.isEmpty()) {
				Collections.sort(_temp);

				try {
					final Stmt _stmt = context.getStmt();
					final SootMethod _method = context.getCurrentMethod();
					xmlWriter.startTag("program_point");
					xmlWriter.attribute("id", idGenerator.getIdForValueBox(vBox, _stmt, _method));

					for (final Iterator _i = (new HashSet(_temp)).iterator(); _i.hasNext();) {
						xmlWriter.startTag("object");
						xmlWriter.attribute("expr", xmlizeString((String) _i.next()));
						xmlWriter.endTag();
					}
					xmlWriter.endTag();
				} catch (final IOException _e) {
					LOGGER.error("Error while xmlizing OFA information ", _e);
				}
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
		 */
		public void callback(final SootMethod method) {
			try {
				if (processingMethod) {
					xmlWriter.endTag();
				} else {
					processingMethod = true;
				}
				xmlWriter.startTag("method");
				xmlWriter.attribute("id", idGenerator.getIdForMethod(method));
				xmlWriter.attribute("name", method.getSignature());
			} catch (final IOException _e) {
				LOGGER.error("Error while xmlizing OFA information ", _e);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
		 */
		public void callback(final SootClass clazz) {
			try {
				if (processingMethod) {
					xmlWriter.endTag();
				}

				if (processingClass) {
					xmlWriter.endTag();
				} else {
					processingClass = true;
				}
				xmlWriter.startTag("class");
				xmlWriter.attribute("id", idGenerator.getIdForClass(clazz));
				xmlWriter.attribute("name", clazz.getName());
			} catch (final IOException _e) {
				LOGGER.error("Error while xmlizing OFA information ", _e);
			}

			processingMethod = false;
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
		 */
		public void consolidate() {
			try {
				xmlWriter.endDocument();
			} catch (final IOException _e) {
				LOGGER.error("Error while xmlizing OFA information ", _e);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.registerForAllValues(this);
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
		 */
		public void processingBegins() {
			processingClass = false;
			processingMethod = false;

			try {
				xmlWriter.declaration();
				xmlWriter.startTag("ofa");
			} catch (final IOException _e) {
				LOGGER.error("Error while xmlizing OFA information ", _e);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregisterForAllValues(this);
			ppc.unregister(this);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IXMLizer#getFileName(java.lang.String)
	 */
	public String getFileName(final String name) {
		return "ofa_" + xmlizeString(name) + ".xml";
	}

	/**
	 * Writes the object flow information as xml.
	 *
	 * @param info maps well known indus interface ids to the implementation that provide these interfaces.
	 *
	 * @pre info != null
	 *
	 * @see edu.ksu.cis.indus.xmlizer.AbstractXMLizer#writeXML(java.util.Map)
	 */
	public void writeXML(final Map info) {
		final ProcessingController _ctrl = new ProcessingController();
		final OFAnalyzer _ofa = (OFAnalyzer) info.get(IValueAnalyzer.ID);
		final IEnvironment _env = _ofa.getEnvironment();
		final IProcessingFilter _processingFilter = new TagBasedProcessingFilter((String) info.get(IValueAnalyzer.TAG_ID));
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory((IStmtGraphFactory) info.get(IStmtGraphFactory.ID));
		_ctrl.setStmtSequencesRetriever(_ssr);
		_ctrl.setProcessingFilter(_processingFilter);
		_ctrl.setEnvironment(_env);
		_processingFilter.chain(new XMLizingProcessingFilter());

		final File _f = new File(getXmlOutputDir() + File.separator + getFileName((String) info.get(FILE_NAME_ID)));
		FileWriter _writer;

		try {
			_writer = new FileWriter(_f);

			final IProcessor _processor = new OFAXMLizingProcessor(_writer, _ofa);
			_processor.hookup(_ctrl);
			_ctrl.process();
			_processor.unhook(_ctrl);
			_writer.close();
		} catch (final IOException _e) {
			LOGGER.error("Error while xmlizing OFA information ", _e);
		}
	}
}

// End of File
