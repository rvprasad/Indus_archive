
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
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
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.indus.staticanalyses.support.SootDependentTest;

import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyTest
  extends SootDependentTest {
	/**
	 * This is the flow analyser used by the analyses being tested.
	 */
	protected AbstractAnalyzer aa;

	/**
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	protected Collection das;

	/**
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

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
	private final String xmlInDir;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final String xmlOutDir;

	/**
	 * Constructor for DependencyTest.
	 *
	 * @param namesOfClasses DOCUMENT ME!
	 * @param xmlOutputDir DOCUMENT ME!
	 * @param xmlInputDir DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public DependencyTest(final String[] namesOfClasses, final String xmlOutputDir, final String xmlInputDir) {
		setClassNames(namesOfClasses);

		if (xmlOutputDir == null) {
			File f = new File(xmlOutputDir);

			if (f == null || !f.exists() | !f.canWrite()) {
				throw new IllegalArgumentException("XML output directory should exists with proper permissions.");
			}
		}
		xmlOutDir = xmlOutputDir;

		if (xmlInputDir == null) {
			File f = new File(xmlInputDir);

			if (f == null || !f.exists() || !f.canRead()) {
				throw new IllegalArgumentException("XML input directory should exists with proper permissions.");
			}
		}
		xmlInDir = xmlInputDir;
		logger = LogFactory.getLog(DependencyTest.class);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.dependency");
		String propFileName = System.getProperty("indus.dependencytest.properties.file");
		setupTests(propFileName, suite);
		return suite;
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
	 * @see TestCase#setUp()
	 */
	protected void setUp() {
		super.setUp();
		execute();
	}

	/**
	 * Drives the analyses.
	 *
	 * @post scene != null and aa != null
	 */
	private final void execute() {
		writeInfo("Loading classes....");
		scene = loadupClassesAndCollectMains();
		aa = OFAnalyzer.getFSOSAnalyzer();

		ValueAnalyzerBasedProcessingController pc = new ValueAnalyzerBasedProcessingController();
		Collection processors = new ArrayList();
		ICallGraphInfo cgi = new CallGraph();
		IThreadGraphInfo tgi = new ThreadGraph(cgi, new CFGAnalysis(cgi, bbm));
		Collection rm = new ArrayList();
		cgipc = new CGBasedProcessingController(cgi);

		pc.setAnalyzer(aa);
		cgipc.setAnalyzer(aa);

		info.put(ICallGraphInfo.ID, cgi);
		info.put(IThreadGraphInfo.ID, tgi);
		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ID, new AliasedUseDefInfo(aa));

		if (ecbaRequired) {
			ecba = new EquivalenceClassBasedEscapeAnalysis(cgi, tgi, bbm);
			info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		}

		for (Iterator k = rootMethods.iterator(); k.hasNext();) {
			rm.clear();
			rm.add(k.next());
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
			process(pc, processors);
			writeInfo("CALL GRAPH:\n" + ((CallGraph) cgi).dumpGraph());
			processors.clear();
			((ThreadGraph) tgi).reset();
			processors.add(tgi);
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

			for (Iterator i = das.iterator(); i.hasNext();) {
				writeXML(i.next());
			}
			writeInfo("Total classes loaded: " + scene.getClasses().size());
			printTimingStats();
		}
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

		if (ecbaRequired) {
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

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if (da.getPreProcessor() != null) {
				da.getPreProcessor().unhook(cgipc);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param propFileName DOCUMENT ME!
	 * @param suite DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	private static final void setupTests(final String propFileName, final TestSuite suite) {
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(new File(propFileName)));

			String[] configs = properties.getProperty("configs").split(" ");

			for (int i = 0; i <= configs.length; i++) {
				String config = configs[i];
				String[] temp = properties.getProperty(config + ".classNames").split(" ");
				String xmlOutputDir = properties.getProperty(config + ".xmlOutputDir");
				String xmlInputDir = properties.getProperty(config + ".xmlInputDir");
				Test test;

				try {
					test = new DependencyTest(temp, xmlOutputDir, xmlInputDir);
				} catch (IllegalArgumentException e) {
					test = null;
				}

				if (test != null) {
					suite.addTest(test);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Specified property file does not exist.");
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
