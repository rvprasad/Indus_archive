
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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
 * This class xmlizes call graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CallGraphXMLizer
  extends AbstractXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraphXMLizer.class);

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

			_xmlizer.dumpXMLizedJimple = _cl.hasOption('j');

			_xmlizer.setXMLOutputDir(_outputDir);
			_xmlizer.setClassNames(_cl.getOptionValues('c'));
			_xmlizer.setGenerator(new UniqueJimpleIDGenerator());

			_xmlizer.initialize();
			_xmlizer.execute();
		} catch (ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer", _options);
		}
	}

	/**
	 * Writes the call graph in XML.
	 *
	 * @param rootname is the name of the root method
	 * @param info is a map of id's to implementation that satisfies the interface associated with the id.
	 *
	 * @pre rootname != null and info != null
	 * @pre info.oclIsKindOf(Map(Object, Object))
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsKindOf(ICallGraphInfo)
	 */
	protected final void writeXML(final String rootname, final Map info) {
		final File _f =
			new File(getXmlOutDir() + File.separator + rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml");
		final FileWriter _writer;

		try {
			_writer = new FileWriter(_f);

			final ICallGraphInfo _cgi = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

			_writer.write("<callgraph>\n");

			for (final Iterator _i = _cgi.getReachableMethods().iterator(); _i.hasNext();) {
				final SootMethod _caller = (SootMethod) _i.next();
				_writer.write("\t<caller id=\"" + getIdGenerator().getIdForMethod(_caller) + "\">\n");

				for (final Iterator _j = _cgi.getCallees(_caller).iterator(); _j.hasNext();) {
					final SootMethod _callee = (SootMethod) _j.next();
					_writer.write("\t\t<callee id=\"" + getIdGenerator().getIdForMethod(_callee) + "\">\n");
				}
				_writer.write("\t</caller>\n");
			}
			_writer.write("</callgraph>\n");
			_writer.flush();
			_writer.close();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Xmlize the given system.
	 */
	public void execute() {
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

		for (final Iterator _k = rootMethods.iterator(); _k.hasNext();) {
			_rm.clear();

			final SootMethod _root = (SootMethod) _k.next();
			_rm.add(_root);

			final String _rootname = _root.getSignature();
			writeInfo("RootMethod: " + _rootname);
			writeInfo("BEGIN: FA");

			final long _start = System.currentTimeMillis();
			_aa.reset();
			bbm.reset();

			_aa.analyze(scene, _rm);

			final long _stop = System.currentTimeMillis();
			addTimeLog("FA", _stop - _start);
			writeInfo("END: FA");
			((CallGraph) _cgi).reset();
			_processors.clear();
			_processors.add(_cgi);
			_pc.reset();
			process(_pc, _processors);
			_processors.clear();
			dumpJimple(_rootname, _xmlcgipc);
			writeXML(_rootname, _info);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/02/08 03:05:46  venku
   - renamed xmlizer packages to be in par with the packages
     that contain the classes whose data is being xmlized.

   Revision 1.5  2003/12/27 20:07:40  venku
   - fixed xmlizers/driver to not throw exception
     when -h is specified

   Revision 1.4  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.3  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.2  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.1  2003/12/08 11:59:47  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizer inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.
 */
