
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.IProcessingFilter;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
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

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class OFAXMLizer
  extends AbstractXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(OFAXMLizer.class);

	/**
	 * DOCUMENT ME!
	 *
	 * @author venku To change this generated comment go to  Window>Preferences>Java>Code Generation>Code Template
	 */
	private final class OFAXMLizingProcessor
	  implements IProcessor {
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final IJimpleIDGenerator idGenerator;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private OFAnalyzer ofa;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private XMLOutputter writer;

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
		 * DOCUMENT ME!
		 *
		 * @param filewriter
		 * @param analyzer DOCUMENT ME!
		 */
		public OFAXMLizingProcessor(final FileWriter filewriter, final OFAnalyzer analyzer) {
			idGenerator = getIdGenerator();
			ofa = analyzer;

			try {
				writer = new XMLOutputter(filewriter, "UTF-8");
                writer.setEscaping(true);
                writer.setIndentation("  ");
                writer.setLineBreak(LineBreak.UNIX);
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
			Collections.sort(_temp);

			try {
				for (final Iterator _i = (new HashSet(_temp)).iterator(); _i.hasNext();) {
					writer.startTag("instance");
					writer.attribute("expr", xmlizeString((String) _i.next()));
                    writer.endTag();
				}
			} catch (final IOException _e) {
				LOGGER.error("Error while xmlizing OFA information ", _e);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
		 */
		public void callback(final SootMethod method) {
			try {
				if (processingMethod) {
					writer.endTag();
				} else {
					processingMethod = true;
				}
				writer.startTag("method");
				writer.attribute("id", idGenerator.getIdForMethod(method));
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
					writer.endTag();
				}

				if (processingClass) {
					writer.endTag();
				} else {
					processingClass = true;
				}
				writer.startTag("class");
				writer.attribute("id", idGenerator.getIdForClass(clazz));
			} catch (final IOException _e) {
				LOGGER.error("Error while xmlizing OFA information ", _e);
			}

			processingMethod = false;
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
		 */
		public void callback(final SootField field) {
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
		 */
		public void consolidate() {
			try {
                writer.close();
				writer.endDocument();
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
				writer.startTag("ofa");
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
	 * @see edu.ksu.cis.indus.xmlizer.AbstractXMLizer#getFileName(java.lang.String)
	 */
	public String getFileName(final String name) {
		return "ofa_" + xmlizeString(name) + ".xml";
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param info DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.xmlizer.AbstractXMLizer#writeXML(java.util.Map)
	 */
	public void writeXML(final Map info) {
		final ProcessingController _ctrl = new ProcessingController();
		final OFAnalyzer _ofa = (OFAnalyzer) info.get(IValueAnalyzer.ID);
		final IEnvironment _env = _ofa.getEnvironment();
        final IProcessingFilter _processingFilter = new TagBasedProcessingFilter((String) info.get(IValueAnalyzer.TAG_ID));
        _processingFilter.chain(new XMLizingProcessingFilter());
		_ctrl.setProcessingFilter(_processingFilter);
		_ctrl.setEnvironment(_env);

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

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/09 17:40:53  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
   Revision 1.1  2004/02/09 07:46:37  venku
   - added new class to xmlize OFA info.
 */
