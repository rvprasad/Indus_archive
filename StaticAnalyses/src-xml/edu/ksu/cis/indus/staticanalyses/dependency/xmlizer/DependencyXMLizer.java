
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

package edu.ksu.cis.indus.staticanalyses.dependency.xmlizer;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DivergenceDA;
import edu.ksu.cis.indus.staticanalyses.dependency.EntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.ExitControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.SynchronizationDA;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.indus.staticanalyses.support.SootBasedDriver;
import edu.ksu.cis.indus.staticanalyses.xmlizer.CGBasedXMLizingProcessingFilter;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.JimpleXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
public class DependencyXMLizer
  extends SootBasedDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependencyXMLizer.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected static final Properties PROPERTIES;

	static {
		STMT_LEVEL_DEPENDENCY = "STMT_LEVEL_DEPENDENCY";
		PROPERTIES = new Properties();

		String propFileName = System.getProperty("indus.dependencyxmlizer.properties.file");

		if (propFileName == null) {
			propFileName = "edu/ksu/cis/indus/staticanalyses/dependency/xmlizer/DependencyXMLizer.properties";
		}

		InputStream stream = ClassLoader.getSystemResourceAsStream(propFileName);

		try {
			PROPERTIES.load(stream);
		} catch (IOException e) {
			System.out.println("Well, error loading property file.  Bailing.");
			throw new RuntimeException(e);
		}
	}

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final Object STMT_LEVEL_DEPENDENCY;

	/**
	 * This is the flow analyser used by the analyses being tested.
	 */
	protected AbstractAnalyzer aa;

	/**
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	protected List das = new ArrayList();

	/**
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected String xmlOutDir;

	/**
	 * This indicates if EquivalenceClassBasedAnalysis should be executed.  Subclasses should set this appropriately.
	 */
	protected boolean ecbaRequired;

	/**
	 * This provides equivalence class based escape analysis.
	 */
	EquivalenceClassBasedEscapeAnalysis ecba = null;

	/**
	 * This provides call-graph based processing controller.
	 */
	ValueAnalyzerBasedProcessingController cgipc;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private AliasedUseDefInfo aliasUD;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Properties properties;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean dumpXMLizedJimple;

	/**
	 * Creates a new DependencyXMLizer object.
	 *
	 * @param useECBA DOCUMENT ME!
	 */
	public DependencyXMLizer(final boolean useECBA) {
		setLogger(LogFactory.getLog(DependencyXMLizer.class));
		setProperties(PROPERTIES);
		ecbaRequired = useECBA;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public List getDAs() {
		return Collections.unmodifiableList(das);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param s DOCUMENT ME!
	 */
	public static void main(final String[] s) {
		Object[][] dasOptions =
			{
				{ "a", "ibdda", "Identifier based data dependence", new IdentifierBasedDataDA() },
				{ "b", "rbdda", "Reference based data dependence", new ReferenceBasedDataDA() },
				{ "d", "ncda", "Entry control dependence", new EntryControlDA() },
				{ "e", "xcda", "Exit control dependence", new ExitControlDA() },
				{ "f", "sda", "Synchronization dependence", new SynchronizationDA() },
				{ "g", "rda1", "Ready dependence v1", new ReadyDAv1() },
				{ "h", "rda2", "Ready dependence v2", new ReadyDAv2() },
				{ "i", "rda3", "Ready dependence v3", new ReadyDAv3() },
				{ "k", "ida1", "Interference dependence v1", new InterferenceDAv1() },
				{ "l", "ida2", "Interference dependence v2", new InterferenceDAv2() },
				{ "m", "ida3", "Interference dependence v3", new InterferenceDAv3() },
				{ "n", "dda", "Divergence dependence", new DivergenceDA() }
			};
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

		for (int i = 0; i < dasOptions.length; i++) {
			option = new Option(dasOptions[i][0].toString(), dasOptions[i][1].toString(), false, dasOptions[i][2].toString());
			options.addOption(option);
		}

		PosixParser parser = new PosixParser();

		try {
			CommandLine cl = parser.parse(options, s);
			DependencyXMLizer xmlizer = new DependencyXMLizer(true);
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

			for (int i = 0; i < dasOptions.length; i++) {
				if (cl.hasOption(dasOptions[i][0].toString())) {
					xmlizer.populateDA((DependencyAnalysis) dasOptions[i][3]);
				}
			}
			xmlizer.initialize();
			xmlizer.execute();
			xmlizer.reset();
		} catch (ParseException e) {
			LOGGER.error("Error while parsing command line.", e);
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer", options);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getRootMethods() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param xmlOutputDir DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public void setXMLOutputDir(final String xmlOutputDir) {
		if (xmlOutputDir != null) {
			File f = new File(xmlOutputDir);

			if (f == null || !f.exists() | !f.canWrite()) {
				throw new IllegalArgumentException("XML output directory should exists with proper permissions.");
			}
		}
		xmlOutDir = xmlOutputDir;
	}

	/**
	 * Returns the directory into which xml output will be written into.
	 *
	 * @return the directory into which xml output will be written
	 *
	 * @post result != null
	 */
	public String getXmlOutDir() {
		return this.xmlOutDir;
	}

	/**
	 * Drives the analyses.
	 */
	public void execute() {
		setLogger(LOGGER);

		String tagName = "DependencyXMLizer:FA";
		aa = OFAnalyzer.getFSOSAnalyzer(tagName);

		ValueAnalyzerBasedProcessingController pc = new ValueAnalyzerBasedProcessingController();
		Collection processors = new ArrayList();
		ICallGraphInfo cgi = new CallGraph();
		IThreadGraphInfo tgi = new ThreadGraph(cgi, new CFGAnalysis(cgi, bbm));
		Collection rm = new ArrayList();
		ProcessingController xmlcgipc = new ProcessingController();
		cgipc = new ValueAnalyzerBasedProcessingController();

		pc.setAnalyzer(aa);
		pc.setProcessingFilter(new TagBasedProcessingFilter(tagName));
		cgipc.setAnalyzer(aa);
		cgipc.setProcessingFilter(new CGBasedProcessingFilter(cgi));
		xmlcgipc.setEnvironment(aa.getEnvironment());
		xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(cgi));

		aliasUD = new AliasedUseDefInfo(aa);
		info.put(ICallGraphInfo.ID, cgi);
		info.put(IThreadGraphInfo.ID, tgi);
		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ID, aliasUD);

		if (ecbaRequired) {
			ecba = new EquivalenceClassBasedEscapeAnalysis(cgi, tgi, bbm);
			info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		}

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

			if (ecbaRequired) {
				ecba.reset();
			}
			aa.analyze(scene, rm);

			long stop = System.currentTimeMillis();
			addTimeLog("FA", stop - start);
			writeInfo("END: FA");
			((CallGraph) cgi).reset();
			processors.clear();
			processors.add(cgi);
			pc.reset();
			process(pc, processors);
			writeInfo("CALL GRAPH:\n" + ((CallGraph) cgi).dumpGraph());
			processors.clear();
			((ThreadGraph) tgi).reset();
			processors.add(tgi);
			cgipc.reset();
			process(cgipc, processors);
			writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) tgi).dumpGraph());
			setupDependencyAnalyses();
			writeInfo("BEGIN: dependency analyses");

			for (Iterator i = das.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				start = System.currentTimeMillis();
				da.analyze();
				stop = System.currentTimeMillis();
				addTimeLog(da.getClass().getName() + "[" + da.hashCode() + "] analysis", stop - start);
			}
			writeInfo("END: dependency analyses");

			Map threadMap = new HashMap();
			writeInfo("\nThread mapping:");

			int count = 1;

			for (java.util.Iterator j = tgi.getAllocationSites().iterator(); j.hasNext();) {
				NewExprTriple element = (NewExprTriple) j.next();
				String tid = "T" + count++;
				threadMap.put(element, tid);

				if (element.getMethod() == null) {
					writeInfo(tid + " -> " + element.getExpr().getType());
				} else {
					writeInfo(tid + " -> " + element.getStmt() + "@" + element.getMethod());
				}
			}

			writeXML(rootname, cgi);
			writeInfo("Total classes loaded: " + scene.getClasses().size());
			printTimingStats();

			if (dumpXMLizedJimple) {
				final JimpleXMLizer _t = new JimpleXMLizer(new UniqueJimpleIDGenerator());
				FileWriter _writer;

				try {
					_writer =
						new FileWriter(new File(getXmlOutDir() + File.separator
								+ rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + "jimple.xml"));
					_t.setWriter(_writer);
					_t.hookup(xmlcgipc);
					xmlcgipc.process();
					_t.unhook(xmlcgipc);
					_writer.flush();
					_writer.close();
				} catch (IOException e) {
					LOGGER.error("Error while opening/writing/closing jimple xml file.  Aborting.", e);
					System.exit(1);
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param xmlizers DOCUMENT ME!
	 * @param ctrl DOCUMENT ME!
	 */
	public final void flushXMLizers(final Map xmlizers, final ProcessingController ctrl) {
		for (Iterator i = xmlizers.keySet().iterator(); i.hasNext();) {
			IProcessor p = (IProcessor) i.next();
			p.unhook(ctrl);

			try {
				FileWriter f = (FileWriter) xmlizers.get(p);
				f.flush();
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("Failed to close the xml file based on " + p.getClass(), e);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param root DOCUMENT ME!
	 * @param ctrl DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final Map initXMLizers(final String root, final ProcessingController ctrl) {
		Map result = new HashMap();

		if (xmlOutDir == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Defaulting to current directory for xml output.");
			}
			xmlOutDir = ".";
		}

		for (Iterator iter = das.iterator(); iter.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) iter.next();

			File f =
				new File(xmlOutDir + File.separator + da.getId() + "_" + das.indexOf(da) + "_"
					+ root.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml");

			try {
				FileWriter writer = new FileWriter(f);
				AbstractDependencyXMLizer xmlizer = getXMLizerFor(writer, da);

				if (xmlizer == null) {
					LOGGER.error("No xmlizer specified for dependency calculated by " + da.getClass()
						+ ".  No xml file written.");
					writer.close();
				} else {
					xmlizer.hookup(ctrl);
					result.put(xmlizer, writer);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to write the xml file based on " + da.getClass() + " for system rooted at " + root, e);
			}
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param generator DOCUMENT ME!
	 */
	public void setGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void populateDAs() {
		// The order is important for the purpose of Testing as it influences the output file name
		das.add(new IdentifierBasedDataDA());
		das.add(new ReferenceBasedDataDA());
		das.add(new EntryControlDA());
		das.add(new ExitControlDA());
		das.add(new SynchronizationDA());
		das.add(new InterferenceDAv1());
		das.add(new InterferenceDAv2());
		das.add(new InterferenceDAv3());
		das.add(new ReadyDAv1());
		das.add(new ReadyDAv2());
		das.add(new ReadyDAv3());
		das.add(new DivergenceDA());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void reset() {
		das.clear();
		aa = null;
		cgipc = null;
		info.clear();
		ecba = null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param props DOCUMENT ME!
	 */
	protected void setProperties(final Properties props) {
		properties = props;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param root DOCUMENT ME!
	 * @param cgi DOCUMENT ME!
	 */
	protected void writeXML(final String root, final ICallGraphInfo cgi) {
		ProcessingController ctrl = new ProcessingController();
		ctrl.setEnvironment(aa.getEnvironment());
		ctrl.setProcessingFilter(new CGBasedXMLizingProcessingFilter(cgi));

		Map xmlizers = initXMLizers(root, ctrl);
		ctrl.process();
		flushXMLizers(xmlizers, ctrl);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param f DOCUMENT ME!
	 * @param da DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private AbstractDependencyXMLizer getXMLizerFor(final FileWriter f, final DependencyAnalysis da) {
		AbstractDependencyXMLizer result = null;
		String xmlizerId = da.getId().toString();

		String temp = properties.getProperty(xmlizerId);

		if (temp.equals(STMT_LEVEL_DEPENDENCY)) {
			result = new StmtLevelDependencyXMLizer(f, idGenerator, da);
		} else {
			LOGGER.error("Unknown dependency xmlizer type requested.  Bailing on this.");
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param da DOCUMENT ME!
	 */
	private void populateDA(final DependencyAnalysis da) {
		das.add(da);
	}

	/**
	 * Drive the given processors by the given controller.  This is helpful to batch pre/post-processors.
	 *
	 * @param pc controls the processing activity.
	 * @param processors is the collection of processors.
	 *
	 * @pre processors.oclIsKindOf(Collection(IValueAnalyzerBasedProcessor))
	 */
	private final void process(final ProcessingController pc, final Collection processors) {
		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IValueAnalyzerBasedProcessor) i.next();

			processor.hookup(pc);
		}

		writeInfo("BEGIN: FA post processing");

		long start = System.currentTimeMillis();
		pc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("FA post processing", stop - start);
		writeInfo("END: FA post processing");

		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IValueAnalyzerBasedProcessor) i.next();

			processor.unhook(pc);
		}
	}

	/**
	 * Sets up the dependence analyses to be driven.
	 */
	private final void setupDependencyAnalyses() {
		Collection failed = new ArrayList();

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();
			da.reset();
			da.setBasicBlockGraphManager(bbm);

			if (da.doesPreProcessing()) {
				da.getPreProcessor().hookup(cgipc);
			}

			try {
				da.initialize(info);
			} catch (InitializationException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(da.getClass() + " failed to initialize, hence, will not be executed.", e);
					failed.add(da);
				}
			}
		}
		das.removeAll(failed);
		aliasUD.reset();
		aliasUD.hookup(cgipc);

		if (ecbaRequired) {
			ecba.reset();
			ecba.hookup(cgipc);
		}

		writeInfo("BEGIN: preprocessing for dependency analyses");

		long start = System.currentTimeMillis();
		cgipc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("Dependency preprocessing", stop - start);
		writeInfo("END: preprocessing for dependency analyses");

		if (ecbaRequired) {
			ecba.unhook(cgipc);
			ecba.execute();
		}
		aliasUD.unhook(cgipc);

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if (da.getPreProcessor() != null) {
				da.getPreProcessor().unhook(cgipc);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.21  2003/12/08 09:47:53  venku
   - set the logger.
   - reset() is called on used objects before they are reused.
   - changed the command-line interface.
   Revision 1.20  2003/12/05 13:46:55  venku
   - coding convention.
   Revision 1.19  2003/12/05 13:44:50  venku
   - xmlization messed up the controller.  FIXED.
   Revision 1.18  2003/12/05 09:17:44  venku
   - added support xmlize the jimple.
   Revision 1.17  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.16  2003/12/02 00:48:27  venku
   - coding conventions.
   Revision 1.15  2003/12/01 13:33:20  venku
   - added support to pick dependences to run from command line.
   Revision 1.14  2003/11/30 01:38:52  venku
   - incorporated tag based filtering during CG construction.
   Revision 1.13  2003/11/30 01:17:15  venku
   - renamed CGBasedXMLizingFilter to CGBasedXMLizingProcessingFilter.
   - renamed XMLizingController to XMLizingProcessingFilter.
   - ripple effect.
   Revision 1.12  2003/11/30 01:07:58  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.11  2003/11/30 00:10:24  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.10  2003/11/25 19:04:29  venku
   - aliased use def analysis was never executed. FIXED.
   Revision 1.9  2003/11/25 17:51:23  venku
   - split control dependence into 2 classes.
     EntryControlDA handled control DA as required for backward slicing.
     ExitControlDA handles control DA as required for forward slicing.
   - ripple effect.
   Revision 1.8  2003/11/25 17:24:23  venku
   - changed the order of dependence for convenience.
   Revision 1.7  2003/11/17 16:58:15  venku
   - populateDAs() needs to be called from outside the constructor.
   - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
   Revision 1.6  2003/11/17 15:42:46  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.5  2003/11/17 03:22:59  venku
   - added junit test support for Slicing.
   - refactored code in test for dependency to make it more
     simple.
   Revision 1.4  2003/11/16 18:45:32  venku
   - renamed UniqueIDGenerator to UniqueJimpleIDGenerator.
   - logging.
   - split writeXML() such that it can be used from other drivers
     such as SlicerDriver.
   Revision 1.3  2003/11/15 21:27:49  venku
   - added logging.
   Revision 1.2  2003/11/12 10:45:36  venku
   - soot class path can be set in SootBasedDriver.
   - dependency tests are xmlunit based.
   Revision 1.1  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.3  2003/11/12 05:08:10  venku
   - DependencyXMLizer.properties will hold dependency xmlization
     related properties.
   Revision 1.2  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.1  2003/11/11 10:11:27  venku
   - in the process of making XMLization a user
     application and at the same time a tester application.
 */
