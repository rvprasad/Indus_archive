
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

package edu.ksu.cis.indus.staticanalyses.xmlizer;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import edu.ksu.cis.indus.xmlizer.*;
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
 * DOCUMENT ME!
 * 
 * <p></p>
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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected IValueAnalyzer aa;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param s DOCUMENT ME!
	 */
	public static void main(String[] s) {
		Options options = new Options();
		Option option = new Option("c", "classes", true, "A list of space separate class names to be analyzed");
		option.setRequired(true);
		option.setArgs(Option.UNLIMITED_VALUES);
		option.setValueSeparator(' ');
		options.addOption(option);
		option =
			new Option("o", "output", true,
				"Directory into which xml files will be written into.  Defaults to current directory if omitted");
		option.setArgs(1);
		options.addOption(option);
		option.setRequired(false);
		option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		options.addOption(option);

		PosixParser parser = new PosixParser();

		try {
			CommandLine cl = parser.parse(options, s);
			CallGraphXMLizer xmlizer = new CallGraphXMLizer();
			String outputDir = cl.getOptionValue('o');

			if (outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				outputDir = ".";
			}

			xmlizer.dumpXMLizedJimple = cl.hasOption('j');

			xmlizer.setXMLOutputDir(outputDir);
			xmlizer.setClassNames(cl.getOptionValues('c'));
			xmlizer.setGenerator(new UniqueJimpleIDGenerator());

			xmlizer.initialize();
			xmlizer.execute();
		} catch (ParseException e) {
			LOGGER.error("Error while parsing command line.", e);
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer", options);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param rootname
	 * @param info
	 */
	protected final void writeXML(final String rootname, final Map info) {
		File f = new File(getXmlOutDir() + File.separator + rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml");
		FileWriter writer;

		try {
			writer = new FileWriter(f);

			ICallGraphInfo cgi = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

			writer.write("<callgraph>\n");

			for (final Iterator i = cgi.getReachableMethods().iterator(); i.hasNext();) {
				SootMethod caller = (SootMethod) i.next();
				writer.write("\t<caller id=\"" + getIdGenerator().getIdForMethod(caller) + "\">\n");

				for (final Iterator j = cgi.getCallees(caller).iterator(); j.hasNext();) {
					SootMethod callee = (SootMethod) j.next();
					writer.write("\t\t<callee id=\"" + getIdGenerator().getIdForMethod(callee) + "\">\n");
				}
				writer.write("\t</caller>\n");
			}
			writer.write("</callgraph>\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	private void execute() {
		setLogger(LOGGER);

		String tagName = "CallGraphXMLizer:FA";
		aa = OFAnalyzer.getFSOSAnalyzer(tagName);

		ValueAnalyzerBasedProcessingController pc = new ValueAnalyzerBasedProcessingController();
		Collection processors = new ArrayList();
		ICallGraphInfo cgi = new CallGraph();
		Collection rm = new ArrayList();
		ProcessingController xmlcgipc = new ProcessingController();

		pc.setAnalyzer(aa);
		pc.setProcessingFilter(new TagBasedProcessingFilter(tagName));
		xmlcgipc.setEnvironment(aa.getEnvironment());
		xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(cgi));

		Map info = new HashMap();
		info.put(ICallGraphInfo.ID, cgi);

		for (Iterator k = rootMethods.iterator(); k.hasNext();) {
			rm.clear();

			SootMethod root = (SootMethod) k.next();
			rm.add(root);

			String rootname = root.getSignature();
			writeInfo("RootMethod: " + rootname);
			writeInfo("BEGIN: FA");

			long start = System.currentTimeMillis();
			aa.reset();
			bbm.reset();

			aa.analyze(scene, rm);

			long stop = System.currentTimeMillis();
			addTimeLog("FA", stop - start);
			writeInfo("END: FA");
			((CallGraph) cgi).reset();
			processors.clear();
			processors.add(cgi);
			pc.reset();
			process(pc, processors);
			processors.clear();
			dumpJimple(rootname, xmlcgipc);
			writeXML(rootname, info);
		}
	}
}

/*
   ChangeLog:
   $Log$
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
