
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CallGraphXMLizerCLI
  extends SootBasedDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraphXMLizerCLI.class);

	/**
	 * The entry point to the program via command line.
	 *
	 * @param args is the command line arguments.
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("c", "classes", true, "A list of space separate class names to be analyzed");
		_option.setArgs(Option.UNLIMITED_VALUES);
		_option.setValueSeparator(' ');
		_options.addOption(_option);
		_option =
			new Option("o", "output", true,
				"Directory into which xml files will be written into.  Defaults to current directory if omitted");
		_option.setArgs(1);
		_options.addOption(_option);
		_option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		_options.addOption(_option);

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.staticanalyses.xmlizer.CallGraphXMLizer ", _options);
				System.exit(1);
			}

			final CallGraphXMLizer _xmlizer = new CallGraphXMLizer();
			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}
			_xmlizer.setXmlOutputDir(_outputDir);
			_xmlizer.setGenerator(new UniqueJimpleIDGenerator());

			final CallGraphXMLizerCLI _cli = new CallGraphXMLizerCLI();
			_cli.setClassNames(_cl.getOptionValues('c'));
			_cli.initialize();
			_cli.execute(_xmlizer, _cl.hasOption('j'));
		} catch (ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizerDriver",
				_options);
		}
	}

	/**
	 * Xmlize the given system.
	 *
	 * @param xmlizer DOCUMENT ME!
	 * @param dumpJimple DOCUMENT ME!
	 */
	private void execute(final AbstractXMLizer xmlizer, final boolean dumpJimple) {
		setLogger(LOGGER);

		final String _tagName = "CallGraphXMLizer:FA";
		final IValueAnalyzer _aa = OFAnalyzer.getFSOSAnalyzer(_tagName);

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final ICallGraphInfo _cgi = new CallGraph();
		final Collection _rm = new ArrayList();
		final ProcessingController _xmlcgipc = new ProcessingController();

		_pc.setAnalyzer(_aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));
		_xmlcgipc.setEnvironment(_aa.getEnvironment());
		_xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));

		final Map _info = new HashMap();
		_info.put(ICallGraphInfo.ID, _cgi);

		for (final Iterator _k = getRootMethods().iterator(); _k.hasNext();) {
			_rm.clear();

			final SootMethod _root = (SootMethod) _k.next();
			_rm.add(_root);

			final String _rootname = _root.getSignature();
			writeInfo("RootMethod: " + _rootname);
			writeInfo("BEGIN: FA");

			final long _start = System.currentTimeMillis();
			_aa.reset();
			getBbm().reset();

			_aa.analyze(getScene(), _rm);

			final long _stop = System.currentTimeMillis();
			addTimeLog("FA", _stop - _start);
			writeInfo("END: FA");
			((CallGraph) _cgi).reset();
			_processors.clear();
			_processors.add(_cgi);
			_pc.reset();
			_pc.driveProcessors(_processors);
			_processors.clear();
			xmlizer.dumpJimple(_rootname, _xmlcgipc);
			_info.put(AbstractXMLizer.FILE_NAME_ID, _rootname);
			xmlizer.writeXML(_info);

			if (dumpJimple) {
				xmlizer.dumpJimple(_rootname, _xmlcgipc);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
