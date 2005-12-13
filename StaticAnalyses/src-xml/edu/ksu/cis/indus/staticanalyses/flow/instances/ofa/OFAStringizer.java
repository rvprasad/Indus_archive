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
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class OFAStringizer {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(OFAStringizer.class);

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
		 * The OFA instance whose information should be xmlized.
		 */
		private OFAnalyzer<?> ofa;

		private final StringWriter writer;

		/**
		 * Creates an instance of the processor.
		 *
		 * @param analyzer is the OFA instance whose information should be xmlized.
		 * @pre analyzer != null
		 */
		public OFAXMLizingProcessor(final OFAnalyzer<?> analyzer) {
			ofa = analyzer;
			writer = new StringWriter();
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
		 */
		@Override public void callback(final ValueBox vBox, final Context context) {
			writer.append("\n\t\t<");
			writer.append(vBox.getValue().toString());
			writer.append(">: ");

			for (final Iterator<?> _i = ofa.getValues(vBox.getValue(), context).iterator(); _i.hasNext();) {
				writer.append(_i.next().toString());
				writer.append(", ");
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
		 */
		@Override public void callback(final SootMethod method) {
			writer.append('\n');
			writer.append(method.getSignature());
			xmlizeParameterValues(method);
			xmlizeThrownValues(method);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
		 */
		@Override public void callback(final Stmt stmt, @SuppressWarnings("unused") final Context context) {
			writer.append("\n\t");
			writer.append(stmt.toString());
		}

		/**
		 * Xmlize information about the exceptions thrown by this method.
		 *
		 * @param method of interest.
		 * @pre method != null
		 */
		private void xmlizeThrownValues(final SootMethod method) {
			final Context _context = new Context();
			_context.setRootMethod(method);
			writer.append("\n\tThrows: ");
			for (final Iterator<?> _i = ofa.getThrownValues(method, _context).iterator(); _i.hasNext();) {
				writer.append(_i.next().toString());
				writer.append(", ");
			}
		}

		/**
		 * Xmlize information about the arguments provided for the given method.
		 *
		 * @param method of interest
		 * @pre method != null
		 */
		private void xmlizeParameterValues(final SootMethod method) {
			final Context _context = new Context();
			_context.setRootMethod(method);
			for (int _j = 0; _j < method.getParameterCount(); _j++) {
				writer.append("\n\t");
				writer.append("Param_");
				writer.append(String.valueOf(_j));
				writer.append(": ");
				for (final Iterator<?> _i = ofa.getValuesForParameter(_j, _context).iterator(); _i.hasNext();) {
					writer.append(_i.next().toString());
					writer.append(", ");
				}
			}
		}


		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.registerForAllValues(this);
			ppc.registerForAllStmts(this);
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregisterForAllValues(this);
			ppc.unregisterForAllStmts(this);
			ppc.unregister(this);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override public String toString() {
			return writer.toString();
		}
	}

	/**
	 * Retrieves object flow information as a textual string.
	 *
	 * @param info maps well known indus interface ids to the implementation that provide these interfaces.
	 * @return string representation of OFA info.
	 * @pre info != null
	 *
	 */
	public String getOFAInfoAsString(final Map info) {
		final ProcessingController _ctrl = new ProcessingController();
		final OFAnalyzer<?> _ofa = (OFAnalyzer) info.get(IValueAnalyzer.ID);
		final IEnvironment _env = _ofa.getEnvironment();
		final IProcessingFilter _processingFilter = new TagBasedProcessingFilter((String) info.get(IValueAnalyzer.TAG_ID));
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory((IStmtGraphFactory) info.get(IStmtGraphFactory.ID));
		_ctrl.setStmtSequencesRetriever(_ssr);
		_ctrl.setProcessingFilter(_processingFilter);
		_ctrl.setEnvironment(_env);
		_processingFilter.chain(new XMLizingProcessingFilter());

		final OFAXMLizingProcessor _processor = new OFAXMLizingProcessor(_ofa);
		_processor.hookup(_ctrl);
		_ctrl.process();
		_processor.unhook(_ctrl);
		return _processor.toString();
	}
}

// End of File
