
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

import edu.ksu.cis.indus.common.structures.Pair.PairManager;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

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
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.xmlizer.CGBasedXMLizingProcessingFilter;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.JimpleXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

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
 * This class xmlizes dependence information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyXMLizer
  extends AbstractXMLizer {
	/**
	 * This is used to identify statement level dependence producing analysis.
	 */
	public static final Object STMT_LEVEL_DEPENDENCY;

	/**
	 * This maps dependency ids to dependence sort ids (STMT_LEVEL_DEPENDENCY).
	 */
	protected static final Properties PROPERTIES;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependencyXMLizer.class);

	static {
		STMT_LEVEL_DEPENDENCY = "STMT_LEVEL_DEPENDENCY";
		PROPERTIES = new Properties();

		String _propFileName = System.getProperty("indus.dependencyxmlizer.properties.file");

		if (_propFileName == null) {
			_propFileName = "edu/ksu/cis/indus/staticanalyses/dependency/xmlizer/DependencyXMLizer.properties";
		}

		final InputStream _stream = ClassLoader.getSystemResourceAsStream(_propFileName);

		try {
			PROPERTIES.load(_stream);
		} catch (IOException _e) {
			System.out.println("Well, error loading property file.  Bailing.");
			throw new RuntimeException(_e);
		}
	}

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
	 * This is the directory into which the xml output will be written into.
	 */
	protected String xmlOutDir;

	/**
	 * This indicates if EquivalenceClassBasedAnalysis should be executed.  Subclasses should set this appropriately.
	 */
	protected boolean ecbaRequired;

	/**
	 * This provides equivalence class based escape analysis.
	 */
	EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * This provides call-graph based processing controller.
	 */
	ValueAnalyzerBasedProcessingController cgipc;

	/**
	 * This provides use-def info based on aliasing.
	 */
	private AliasedUseDefInfo aliasUD;

	/**
	 * This dependency ids to dependence sort ids (STMT_LEVEL_DEPENDENCY).
	 */
	private Properties properties;

	/**
	 * Creates a new DependencyXMLizer object.
	 *
	 * @param useECBA <code>true</code> indicates equivalence class based escape analysis should be used; <code>false</code>,
	 * 		  otherwise.
	 */
	public DependencyXMLizer(final boolean useECBA) {
		setLogger(LogFactory.getLog(DependencyXMLizer.class));
		setProperties(PROPERTIES);
		ecbaRequired = useECBA;
	}

	/**
	 * Retrieves the dependences being xmlized.
	 *
	 * @return a collection of dependence analysis.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(DependenceAnalysis))
	 */
	public final List getDAs() {
		return Collections.unmodifiableList(das);
	}

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args is the command line arguments.
	 *
	 * @pre args != null
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("c", "classes", true, "A list of space separate class names to be analyzed");
		_option.setRequired(true);
		_option.setArgs(Option.UNLIMITED_VALUES);
		_option.setValueSeparator(' ');
		_options.addOption(_option);
		_option =
			new Option("o", "output", true,
				"Directory into which xml files will be written into.  Defaults to current directory if omitted");
		_option.setArgs(1);
		_options.addOption(_option);
		_option.setRequired(false);
		_option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		_options.addOption(_option);

		final Object[][] _dasOptions =
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
				{ "n", "dda", "Divergence dependence", new DivergenceDA() },
			};

		for (int _i = 0; _i < _dasOptions.length; _i++) {
			final String _shortOption = _dasOptions[_i][0].toString();
			final String _longOption = _dasOptions[_i][1].toString();
			final String _description = _dasOptions[_i][2].toString();
			_option = new Option(_shortOption, _longOption, false, _description);
			_options.addOption(_option);
		}

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);
			final DependencyXMLizer _xmlizer = new DependencyXMLizer(true);
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

			for (int _i = 0; _i < _dasOptions.length; _i++) {
				if (_cl.hasOption(_dasOptions[_i][0].toString())) {
					_xmlizer.populateDA((DependencyAnalysis) _dasOptions[_i][3]);
				}
			}
			_xmlizer.initialize();
			_xmlizer.execute();
			_xmlizer.reset();
		} catch (ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer", _options);
		}
	}

	/**
	 * Retrieves the root methods of the system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	public final Collection getRootMethods() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Drives the analyses.
	 */
	public final void execute() {
		setLogger(LOGGER);

		final String _tagName = "DependencyXMLizer:FA";
		aa = OFAnalyzer.getFSOSAnalyzer(_tagName);

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final ICallGraphInfo _cgi = new CallGraph();
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, new CFGAnalysis(_cgi, bbm));
		final Collection _rm = new ArrayList();
		final ProcessingController _xmlcgipc = new ProcessingController();
		cgipc = new ValueAnalyzerBasedProcessingController();

		_pc.setAnalyzer(aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));
		cgipc.setAnalyzer(aa);
		cgipc.setProcessingFilter(new CGBasedProcessingFilter(_cgi));
		_xmlcgipc.setEnvironment(aa.getEnvironment());
		_xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));

		aliasUD = new AliasedUseDefInfo(aa);
		info.put(ICallGraphInfo.ID, _cgi);
		info.put(IThreadGraphInfo.ID, _tgi);
		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ID, aliasUD);

		if (ecbaRequired) {
			ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, _tgi, bbm);
			info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		}

		for (final Iterator _k = rootMethods.iterator(); _k.hasNext();) {
			_rm.clear();

			final SootMethod _root = (SootMethod) _k.next();
			_rm.add(_root);

			final String _rootname = _root.getSignature();
			writeInfo("RootMethod: " + _rootname);
			writeInfo("BEGIN: FA");

			long _start = System.currentTimeMillis();
			aa.reset();
			bbm.reset();

			if (ecbaRequired) {
				ecba.reset();
			}
			aa.analyze(scene, _rm);

			long _stop = System.currentTimeMillis();
			addTimeLog("FA", _stop - _start);
			writeInfo("END: FA");
			((CallGraph) _cgi).reset();
			_processors.clear();
			_processors.add(_cgi);
			_pc.reset();
			process(_pc, _processors);
			writeInfo("CALL GRAPH:\n" + ((CallGraph) _cgi).dumpGraph());
			_processors.clear();
			((ThreadGraph) _tgi).reset();
			_processors.add(_tgi);
			cgipc.reset();
			process(cgipc, _processors);
			writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).dumpGraph());
			setupDependencyAnalyses();
			writeInfo("BEGIN: dependency analyses");

			for (final Iterator _i = das.iterator(); _i.hasNext();) {
				final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
				_start = System.currentTimeMillis();
				_da.analyze();
				_stop = System.currentTimeMillis();
				addTimeLog(_da.getClass().getName() + "[" + _da.hashCode() + "] analysis", _stop - _start);
			}
			writeInfo("END: dependency analyses");

			final Map _threadMap = new HashMap();
			writeInfo("\nThread mapping:");

			int _count = 1;

			for (final Iterator _j = _tgi.getAllocationSites().iterator(); _j.hasNext();) {
				final NewExprTriple _element = (NewExprTriple) _j.next();
				final String _tid = "T" + _count++;
				_threadMap.put(_element, _tid);

				if (_element.getMethod() == null) {
					writeInfo(_tid + " -> " + _element.getExpr().getType());
				} else {
					writeInfo(_tid + " -> " + _element.getStmt() + "@" + _element.getMethod());
				}
			}

			writeXML(_rootname, info);
			writeInfo("Total classes loaded: " + scene.getClasses().size());
			printTimingStats();

			if (dumpXMLizedJimple) {
				final JimpleXMLizer _t = new JimpleXMLizer(new UniqueJimpleIDGenerator());
				Writer _writer;

				try {
					_writer =
						new FileWriter(new File(getXmlOutDir() + File.separator
								+ _rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + "jimple.xml"));
					_t.setWriter(_writer);
					_t.hookup(_xmlcgipc);
					_xmlcgipc.process();
					_t.unhook(_xmlcgipc);
					_writer.flush();
					_writer.close();
				} catch (IOException _e) {
					LOGGER.error("Error while opening/writing/closing jimple xml file.  Aborting.", _e);
					System.exit(1);
				}
			}
		}
	}

	/**
	 * Flushes the writes associated with each xmlizers.
	 *
	 * @param xmlizers to be flushed.
	 * @param ctrl to unhook the xmlizers from.
	 *
	 * @pre xmlizers != null and ctrl != null
	 */
	public final void flushXMLizers(final Map xmlizers, final ProcessingController ctrl) {
		for (final Iterator _i = xmlizers.keySet().iterator(); _i.hasNext();) {
			final IProcessor _p = (IProcessor) _i.next();
			_p.unhook(ctrl);

			try {
				final FileWriter _f = (FileWriter) xmlizers.get(_p);
				_f.flush();
				_f.close();
			} catch (IOException _e) {
				_e.printStackTrace();
				LOGGER.error("Failed to close the xml file based on " + _p.getClass(), _e);
			}
		}
	}

	/**
	 * Initializes the xmlizers.
	 *
	 * @param rootname is the name of the root method.
	 * @param ctrl is the controller to be used to initialize the xmlizers and to which to hook up the xmlizers to xmlize the
	 * 		  dependence information.
	 *
	 * @return a map of xmlizers and the associated writers.
	 *
	 * @pre rootname != null and ctrl != null
	 * @post result != null and result.oclIsKindOf(Map(StmtLevelDependencyXMLizer, Writer))
	 */
	public final Map initXMLizers(final String rootname, final ProcessingController ctrl) {
		final Map _result = new HashMap();

		if (xmlOutDir == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Defaulting to current directory for xml output.");
			}
			xmlOutDir = ".";
		}

		for (final Iterator _i = das.iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();

			final File _f =
				new File(xmlOutDir + File.separator + _da.getId() + "_" + das.indexOf(_da) + "_"
					+ rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml");

			try {
				final FileWriter _writer = new FileWriter(_f);
				final StmtLevelDependencyXMLizer _xmlizer = getXMLizerFor(_writer, _da);

				if (_xmlizer == null) {
					LOGGER.error("No xmlizer specified for dependency calculated by " + _da.getClass()
						+ ".  No xml file written.");
					_writer.close();
				} else {
					_xmlizer.hookup(ctrl);
					_result.put(_xmlizer, _writer);
				}
			} catch (IOException _e) {
				LOGGER.error("Failed to write the xml file based on " + _da.getClass() + " for system rooted at " + rootname,
					_e);
			}
		}
		return _result;
	}

	/**
	 * Populates the collection of dependences that will be xmlized.
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
	 * Reset internal data structures.
	 */
	public final void reset() {
		das.clear();
		aa = null;
		cgipc = null;
		info.clear();
		ecba = null;
	}

	/**
	 * Set the properties that map from dependence ids and dependence xmlization ids.
	 *
	 * @param props is the mapping.
	 *
	 * @pre props != null
	 */
	protected final void setProperties(final Properties props) {
		properties = props;
	}

	/**
	 * Write the xml for the given root and given information map.
	 *
	 * @param root is the entry point to the system.
	 * @param infoArg is the map of information (id to implementation mapping).
	 *
	 * @pre root != null and infoArg != null
	 * @pre infoArg.oclIsKindOf(Map(Object, Object))
	 */
	protected final void writeXML(final String root, final Map infoArg) {
		final ProcessingController _ctrl = new ProcessingController();
		_ctrl.setEnvironment(aa.getEnvironment());
		_ctrl.setProcessingFilter(new CGBasedXMLizingProcessingFilter((ICallGraphInfo) infoArg.get(ICallGraphInfo.ID)));

		final Map _xmlizers = initXMLizers(root, _ctrl);
		_ctrl.process();
		flushXMLizers(_xmlizers, _ctrl);
	}

	/**
	 * Retrives the xmlizer for the given dependence analysis based on the properties.
	 *
	 * @param writer to be used by the xmlizer.
	 * @param da is the dependence analysis for which the xmlizer is requested.
	 *
	 * @return the xmlizer.
	 *
	 * @pre writer != null and da != null
	 * @post result != null
	 */
	private StmtLevelDependencyXMLizer getXMLizerFor(final Writer writer, final DependencyAnalysis da) {
		StmtLevelDependencyXMLizer _result = null;
		final String _xmlizerId = da.getId().toString();

		final String _temp = properties.getProperty(_xmlizerId);

		if (_temp.equals(STMT_LEVEL_DEPENDENCY)) {
			_result = new StmtLevelDependencyXMLizer(writer, getIdGenerator(), da);
		} else {
			LOGGER.error("Unknown dependency xmlizer type requested.  Bailing on this.");
		}
		return _result;
	}

	/**
	 * Add the given dependence analysis to the xmlization run.
	 *
	 * @param da is the dependence analysis to be added.
	 *
	 * @pre da != null
	 */
	private void populateDA(final DependencyAnalysis da) {
		das.add(da);
	}

	/**
	 * Sets up the dependence analyses to be driven.
	 */
	private void setupDependencyAnalyses() {
		final Collection _failed = new ArrayList();

		for (final Iterator _i = das.iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
			_da.reset();
			_da.setBasicBlockGraphManager(bbm);

			if (_da.doesPreProcessing()) {
				_da.getPreProcessor().hookup(cgipc);
			}

			try {
				_da.initialize(info);
			} catch (InitializationException _e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(_da.getClass() + " failed to initialize, hence, will not be executed.", _e);
					_failed.add(_da);
				}
			}
		}
		das.removeAll(_failed);
		aliasUD.reset();
		aliasUD.hookup(cgipc);

		if (ecbaRequired) {
			ecba.reset();
			ecba.hookup(cgipc);
		}

		writeInfo("BEGIN: preprocessing for dependency analyses");

		final long _start = System.currentTimeMillis();
		cgipc.process();

		final long _stop = System.currentTimeMillis();
		addTimeLog("Dependency preprocessing", _stop - _start);
		writeInfo("END: preprocessing for dependency analyses");

		if (ecbaRequired) {
			ecba.unhook(cgipc);
			ecba.execute();
		}
		aliasUD.unhook(cgipc);

		for (final Iterator _i = das.iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();

			if (_da.getPreProcessor() != null) {
				_da.getPreProcessor().unhook(cgipc);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.28  2003/12/09 09:50:50  venku
   - amended output of string output to be XML compliant.
     This means some characters that are unrepresentable in
     XML are omitted.
   Revision 1.27  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.26  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.25  2003/12/08 12:15:56  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.24  2003/12/08 11:59:47  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizer inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.
   Revision 1.23  2003/12/08 11:53:25  venku
   - formatting.
   Revision 1.22  2003/12/08 10:58:52  venku
   - changed command-line interface.
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
