
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

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAXMLizerCLI;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.IXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class provides the command line interface to xmlize call graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CallGraphXMLizerCLI
  extends SootBasedDriver {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraphXMLizerCLI.class);

	/** 
	 * This indicates if cumulative or separate call graphs should be generated when there are more than one root methods.
	 */
	private boolean cumulative;

	/**
	 * The entry point to the program via command line.
	 *
	 * @param args is the command line arguments.
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("c", "cumulative", false, "Builds one call graph that includes all root methods.");
		_options.addOption(_option);
		_option =
			new Option("o", "output", true,
				"Directory into which xml files will be written into.  Defaults to current directory if omitted");
		_option.setArgs(1);
		_option.setArgName("output-dir");
		_options.addOption(_option);
		_option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		_option.setArgName("dump-jimple");
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", true, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption('h')) {
				final String _cmdLineSyn = "java " + CallGraphXMLizerCLI.class.getName();
				(new HelpFormatter()).printHelp(_cmdLineSyn.length(), _cmdLineSyn, "", _options, "", true);
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

			if (_cl.getArgList().isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}
			_cli.setCumulative(_cl.hasOption('c'));
			_cli.setClassNames(_cl.getArgList());
			_cli.addToSootClassPath(_cl.getOptionValue('p'));
			_cli.initialize();
			_cli.execute(_xmlizer, _cl.hasOption('j'));
		} catch (ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);

			final String _cmdLineSyn = "java " + CallGraphXMLizerCLI.class.getName();
			(new HelpFormatter()).printHelp(_cmdLineSyn.length(), _cmdLineSyn, "", _options, "");
		}
	}

	/**
	 * Sets cumulative mode.
	 *
	 * @param option <code>true</code> indicates one cumulative call graph for all root methods; <code>false</code> indicates
	 * 		  separate call graphs for each root method.
	 */
	private void setCumulative(final boolean option) {
		cumulative = option;
	}

	/**
	 * Xmlize the given system.
	 *
	 * @param xmlizer to be used for xmlizing the call graph.
	 * @param dumpJimple <code>true</code> indicates xmlized jimple should be dumped; <code>false</code>, otherwise.
	 *
	 * @pre xmlizer != null
	 */
	private void execute(final IXMLizer xmlizer, final boolean dumpJimple) {
		setLogger(LOGGER);

		final String _tagName = "CallGraphXMLizer:FA";
		final IValueAnalyzer _aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager());

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final ICallGraphInfo _cgi = new CallGraph(new PairManager(false, true));
		final Collection _rm = new ArrayList();
		final ProcessingController _xmlcgipc = new ProcessingController();
		_xmlcgipc.setStmtGraphFactory(getStmtGraphFactory());

		_pc.setAnalyzer(_aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));
		_pc.setStmtGraphFactory(getStmtGraphFactory());
		_xmlcgipc.setEnvironment(_aa.getEnvironment());
		_xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));
		_xmlcgipc.setStmtGraphFactory(getStmtGraphFactory());

		final Map _info = new HashMap();
		_info.put(ICallGraphInfo.ID, _cgi);

		final List _roots = new ArrayList();

		if (cumulative) {
			_roots.add(getRootMethods());
		} else {
			_roots.addAll(getRootMethods());
		}
		Collections.sort(_roots, ToStringBasedComparator.SINGLETON);
		writeInfo("Root methods are: " + _roots.size() + "\n" + _roots);

		for (final Iterator _k = _roots.iterator(); _k.hasNext();) {
			_rm.clear();

			final Object _root = _k.next();
			final String _fileBaseName = OFAXMLizerCLI.getBaseNameOfFileAndRootMethods(_root, _rm);
			writeInfo("RootMethod: " + _root);
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
			_info.put(AbstractXMLizer.FILE_NAME_ID, _fileBaseName);
			_info.put(IStmtGraphFactory.ID, getStmtGraphFactory());
			xmlizer.writeXML(_info);

			if (dumpJimple && xmlizer instanceof AbstractXMLizer) {
				((AbstractXMLizer) xmlizer).dumpJimple(_fileBaseName, xmlizer.getXmlOutputDir(), _xmlcgipc);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.16  2004/06/22 14:34:54  venku
   - added commandline option to enable cumulative or separate analyses when there is more than root method.
   Revision 1.15  2004/06/12 06:45:22  venku
   - magically, the exception without "+ 10" in helpformatter of  CLI vanished.
   Revision 1.14  2004/06/03 03:50:33  venku
   - changed the way help will be output on command line classes.
   Revision 1.13  2004/05/25 21:20:44  venku
   - changed the root name used to dump files.
   Revision 1.12  2004/05/25 19:11:31  venku
   - added option to control class path and dump reachables.
   Revision 1.11  2004/05/11 22:30:21  venku
   - fixed command line errors.
   Revision 1.10  2004/04/25 23:18:18  venku
   - coding conventions.
   Revision 1.9  2004/04/25 21:18:37  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
   Revision 1.8  2004/04/22 23:32:30  venku
   - xml file name were setup incorrectly.  FIXED.
   Revision 1.7  2004/04/22 10:23:10  venku
   - added getTokenManager() method to OFAXMLizerCLI to create
     token manager based on a system property.
   - ripple effect.
   Revision 1.6  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.5  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.4  2004/03/26 07:17:05  venku
   - documentation.
   - forced -c option as being required.
   Revision 1.3  2004/03/05 11:59:45  venku
   - documentation.
   Revision 1.2  2004/02/11 09:37:18  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.
   Revision 1.1  2004/02/09 17:40:53  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
 */
