/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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

import java.io.IOException;
import java.io.Writer;
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

		/**
		 * The writer into which OFA info will be written into.
		 */
		private final Writer writer;

		/**
		 * Creates an instance of the processor.
		 * 
		 * @param analyzer is the OFA instance whose information should be xmlized.
		 * @param w is the writer into which data will be written.
		 * @pre analyzer != null and w != null
		 */
		public OFAXMLizingProcessor(final OFAnalyzer<?> analyzer, final Writer w) {
			ofa = analyzer;
			writer = w;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
		 */
		@Override public void callback(final ValueBox vBox, final Context context) {
			try {
				writer.append("\n\t\t<");
				writer.append(vBox.getValue().toString());
				writer.append(">: ");

				for (final Iterator<?> _i = ofa.getValues(vBox.getValue(), context).iterator(); _i.hasNext();) {
					writer.append(_i.next().toString());
					writer.append(", ");
				}
			} catch (final IOException _e) {
				throw new RuntimeException(_e);
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
		 */
		@Override public void callback(final SootMethod method) {
			try {
				writer.append('\n');
				writer.append(method.getSignature());
			} catch (final IOException _e) {
				throw new RuntimeException(_e);
			}

			xmlizeParameterValues(method);
			xmlizeThrownValues(method);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.jimple.Stmt,
		 *      edu.ksu.cis.indus.processing.Context)
		 */
		@Override public void callback(final Stmt stmt, @SuppressWarnings("unused") final Context context) {
			try {
				writer.append("\n\t");
				writer.append(stmt.toString());
			} catch (final IOException _e) {
				throw new RuntimeException(_e);
			}

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
			try {
				writer.append("\n\tThrows: ");
				for (final Iterator<?> _i = ofa.getThrownValues(method, _context).iterator(); _i.hasNext();) {
					writer.append(_i.next().toString());
					writer.append(", ");
				}
			} catch (final IOException _e) {
				throw new RuntimeException(_e);
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
			try {
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
			} catch (final IOException _e) {
				throw new RuntimeException(_e);
			}

		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.registerForAllValues(this);
			ppc.registerForAllStmts(this);
			ppc.register(this);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregisterForAllValues(this);
			ppc.unregisterForAllStmts(this);
			ppc.unregister(this);
		}
	}

	/**
	 * Retrieves object flow information as a textual string.
	 * 
	 * @param info maps well known indus interface ids to the implementation that provide these interfaces.
	 * @param writer to write the data into.
	 * @pre info != null and writer != null
	 */
	public void getOFAInfoAsString(final Map info, final Writer writer) {
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

		final OFAXMLizingProcessor _processor = new OFAXMLizingProcessor(_ofa, writer);
		_processor.hookup(_ctrl);
		_ctrl.process();
		_processor.unhook(_ctrl);
	}
}

// End of File
