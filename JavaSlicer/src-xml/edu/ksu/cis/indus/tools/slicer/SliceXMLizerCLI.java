
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.IProcessingFilter;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.transformations.TagBasedDestructiveSliceResidualizer;

import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;

import edu.ksu.cis.indus.tools.IToolProgressListener;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.criteria.SliceCriteriaParser;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.IXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.collections.MapUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.layout.RowLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.jibx.runtime.JiBXException;

import soot.Printer;
import soot.SootClass;
import soot.SootMethod;


/**
 * This is the command-line driver class for the slicer tool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SliceXMLizerCLI
  extends SootBasedDriver
  implements IToolProgressListener {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(SliceXMLizerCLI.class);

	/** 
	 * This is the name of the configuration file to use.
	 */
	private static String configFileName;

	/** 
	 * This is the name of the directory into which the slicer will dump sliced artifacts into.
	 */
	protected String outputDirectory;

	/** 
	 * The instance of the slicer tool.
	 */
	SlicerTool slicer;

	/** 
	 * This directory into which jimple should be dumped.
	 */
	String jimpleXMLDumpDir;

	/** 
	 * The id generator used during xmlization.
	 */
	private IJimpleIDGenerator idGenerator;

	/** 
	 * The name of the criteria specification file.
	 */
	private String criteriaSpecFileName;

	/** 
	 * This is the name of the tag to be used to tag parts of the AST occurring in the slice.
	 */
	private final String nameOfSliceTag = "indus.tools.slicer.SliceXMLizerCLI:SLICER";

	/** 
	 * The name of the slice scope specification file.
	 */
	private String sliceScopeSpecFileName;

	/** 
	 * This indicates if jimple representation of the system after residualiztion should be dumped.
	 */
	private boolean postResJimpleDump;

	/** 
	 * This indicates if jimple representation of the system after residualiztion should be dumped in XML form.
	 */
	private boolean postResXMLJimpleDump;

	/** 
	 * This indicates if jimple representation of the system after slicing and before residualization should be dumped.
	 */
	private boolean preResJimpleDump;

	/** 
	 * This indicates if jimple representation of the system after slicing and before residualiztion should be dumped in XML
	 * form.
	 */
	private boolean preResXMLJimpleDump;

	/** 
	 * This indicates if the slice should be residualized.
	 */
	private boolean residualize;

	/**
	 * Creates an instance of this class.
	 */
	protected SliceXMLizerCLI() {
		slicer = new SlicerTool(TokenUtil.getTokenManager(), new ExceptionFlowSensitiveStmtGraphFactory());
		cfgProvider = slicer.getStmtGraphFactory();
	}

	/**
	 * The entry point to the driver.
	 *
	 * @param args contains the command line arguments.
	 */
	public static void main(final String[] args) {
		final SliceXMLizerCLI _xmlizer = new SliceXMLizerCLI();

		// parse command line arguments
		parseCommandLine(args, _xmlizer);

		_xmlizer.initialize();

		final long _startTime = System.currentTimeMillis();
		_xmlizer.execute();

		final long _stopTime = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("It took " + (_stopTime - _startTime) + "ms to identify the slice.");
		}

		_xmlizer.setIDGenerator(new UniqueJimpleIDGenerator());
		_xmlizer.writeXML();
		_xmlizer.residualize();
		_xmlizer.outputStats();
		_xmlizer.slicer.reset();
		_xmlizer.reset();
	}

	/**
	 * @see IToolProgressListener#toolProgess(IToolProgressListener.ToolProgressEvent)
	 */
	public void toolProgess(final ToolProgressEvent evt) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(evt.getMsg() + " - " + evt.getInfo());
		}
	}

	/**
	 * Sets the configuration to be used.
	 *
	 * @param configuration is the stringized form of the slicer configuration.
	 *
	 * @pre configuration != null
	 */
	protected final void setConfiguration(final String configuration) {
		slicer.destringizeConfiguration(configuration);
	}

	/**
	 * Retrieves the xmlizer to be used to xmlizer the slice.
	 *
	 * @return the slice xmlizer.
	 *
	 * @post result != null
	 */
	protected final TagBasedSliceXMLizer getXMLizer() {
		return new TagBasedSliceXMLizer(nameOfSliceTag, idGenerator);
	}

	/**
	 * Sets the id generator to use during xmlization.
	 *
	 * @param generator used to generate the id's during xmlization.
	 *
	 * @pre generator != null
	 */
	protected void setIDGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * Sets the output directory into which files should be dumped.
	 *
	 * @param oDir is the output directory.
	 *
	 * @pre oDir != null
	 */
	protected final void setOutputDirectory(final String oDir) {
		outputDirectory = oDir;
	}

	/**
	 * Executes the slicer.
	 */
	protected void execute() {
		// execute the slicer
		slicer.setTagName(nameOfSliceTag);
		slicer.setSystem(new Environment(scene));
		slicer.setRootMethods(rootMethods);

		final Collection _criteria = processCriteriaSpecFile();
		slicer.setSliceScopeDefinition(processSliceScopeSpecFile());
		slicer.setCriteria(_criteria);
		slicer.addToolProgressListener(this);
		slicer.run(Phase.STARTING_PHASE, true);

		if (LOGGER.isInfoEnabled()) {
			try {
				LOGGER.info("Criteria specification after slicing: \n" + SliceCriteriaParser.serialize(slicer.getCriteria()));
			} catch (final JiBXException _e) {
				LOGGER.info("JiBX failed during serialization.", _e);
			}
		}

		// We use default slicer configuration in which criteria to preserve deadlock properties are created on behalf the CLI
		// Hence, the CLI should return these criteria to the pool.
		for (final Iterator _i = slicer.getCriteria().iterator(); _i.hasNext();) {
			final ISliceCriterion _criterion = (ISliceCriterion) _i.next();
			_criterion.returnToPool();
		}
	}

	/**
	 * Updates jimple destructively.
	 */
	void destructivelyUpdateJimple() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Residualizing");
		}

		final TagBasedDestructiveSliceResidualizer _residualizer = new TagBasedDestructiveSliceResidualizer();
		_residualizer.setTagToResidualize(nameOfSliceTag);
		_residualizer.setBasicBlockGraphMgr(slicer.getBasicBlockGraphManager());
		_residualizer.residualizeSystem(slicer.getSystem());
	}

	/**
	 * Dump xmlized jimple.
	 *
	 * @param base from which the names of the jimple file will be built.
	 *
	 * @pre base != null
	 */
	void dumpJimpleAsXML(final String base) {
		final IXMLizer _xmlizer = getXMLizer();

		if (jimpleXMLDumpDir != null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: Dumping XMLized Jimple");
			}

			final ProcessingController _ctrl = new ProcessingController();
			final IProcessingFilter _filter = new XMLizingProcessingFilter();
			_filter.chain(new TagBasedProcessingFilter(nameOfSliceTag));
			_ctrl.setStmtGraphFactory(getStmtGraphFactory());
			_ctrl.setEnvironment(new Environment(scene));
			_ctrl.setProcessingFilter(_filter);
			((AbstractXMLizer) _xmlizer).dumpJimple(base, jimpleXMLDumpDir, _ctrl);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: Dumping XMLized Jimple");
			}
		}
	}

	/**
	 * Parses the command line argument.
	 *
	 * @param args contains the command line arguments.
	 * @param xmlizer used to xmlize the slice.
	 *
	 * @pre args != null and xmlizer != null
	 */
	private static void parseCommandLine(final String[] args, final SliceXMLizerCLI xmlizer) {
		// create options
		final Options _options = new Options();
		Option _o =
			new Option("c", "config-file", true,
				"The configuration file to use.  If unspecified, uses default configuration file.");
		_o.setArgs(1);
		_o.setArgName("config-file");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("a", "active-config", true,
				"The alternate configuration to use instead of the one specified in the configuration.");
		_o.setArgs(1);
		_o.setArgName("config");
		_o.setLongOpt("active-config");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("o", "output-dir", true,
				"The output directory to dump the generated info.  If unspecified, picks a temporary directory.");
		_o.setArgs(1);
		_o.setArgName("path");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("g", "gui-config", false, "Display gui for configuration.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("p", "soot-classpath", true, "Prepend this to soot class path.");
		_o.setArgs(1);
		_o.setArgName("classpath");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("h", "help", false, "Display message.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("i", "output-xml-jimple-before-res", false,
				"Output xml representation of the jimple BEFORE residualization.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("j", "output-xml-jimple-after-res", false,
				"Output xml representation of the jimple AFTER residualization. This only works with -r option.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("I", "output-jimple-before-res", false, "Output jimple BEFORE residualization.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("J", "output-jimple-after-res", false,
				"Output jimple AFTER residualization. This only works with -r option.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("s", "criteria-spec-file", true, "Use the slice criteria specified in this file.");
		_o.setArgs(1);
		_o.setArgName("crit-spec-file");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("S", "slice-scope-spec-file", true, "Use the scope specified in this file.");
		_o.setArgs(1);
		_o.setArgName("slice-scope-spec-file");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("r", "residualize", false,
				"Residualize after slicing. This will also dump the class files for the residualized classes.");
		_o.setOptionalArg(false);
		_options.addOption(_o);

		CommandLine _cl = null;

		// parse the arguments
		Exception _exception = null;

		try {
			_cl = (new BasicParser()).parse(_options, args);
		} catch (ParseException _e) {
			_exception = _e;
		}

		if (_exception != null || _cl.hasOption("h")) {
			final String _cmdLineSyn = "java " + SliceXMLizerCLI.class.getName() + "<options> <class names>";
			(new HelpFormatter()).printHelp(_cmdLineSyn.length(), _cmdLineSyn, "", _options, "", true);

			if (_exception != null) {
				LOGGER.fatal("Incorrect command line.  Aborting.", _exception);
				System.exit(1);
			} else {
				System.exit(0);
			}
		}
		xmlizer.setConfiguration(processCommandLineForConfiguration(_cl));
		setupOutputOptions(_cl, xmlizer);

		if (_cl.hasOption('p')) {
			xmlizer.addToSootClassPath(_cl.getOptionValue('p'));
		}

		if (_cl.hasOption('a')) {
			xmlizer.setConfigName(_cl.getOptionValue('a'));
		}

		if (_cl.hasOption('g')) {
			xmlizer.showGUI();
		}

		if (_cl.hasOption('s')) {
			xmlizer.setSliceCriteriaSpecFile(_cl.getOptionValue('s'));
		}

		if (_cl.hasOption('r')) {
			xmlizer.setResidulization(true);
		}

		if (_cl.hasOption('S')) {
			xmlizer.setSliceScopeSpecFile(_cl.getOptionValue('S'));
		}

		final List _result = _cl.getArgList();

		if (_result.isEmpty()) {
			LOGGER.fatal("Please specify atleast one class that contains an entry method into the system to be sliced.");
			System.exit(1);
		}

		xmlizer.setClassNames(_cl.getArgList());
	}

	/**
	 * Processes the command line for slicer tool configuration information.  Defaults to a configuration if none are
	 * specified.
	 *
	 * @param cl is the parsed command line.
	 *
	 * @return the tool configuration as a string.
	 *
	 * @post result != null
	 * @pre cl != null
	 */
	private static String processCommandLineForConfiguration(final CommandLine cl) {
		String _config = cl.getOptionValue("c");
		InputStream _inStream = null;
		String _result = null;

		if (_config != null) {
			try {
				_inStream = new FileInputStream(_config);
			} catch (FileNotFoundException _e) {
				final String _msg = "Non-existent configuration file specified.";
				LOGGER.error(_msg, _e);

				final IllegalArgumentException _i = new IllegalArgumentException(_msg);
				_i.initCause(_e);
				throw _i;
			}
		} else {
			LOGGER.info("No configuration file specified.");
		}

		configFileName = _config;

		if (_config == null) {
			LOGGER.info("Trying to use default configuration.");

			final URL _defaultConfigFileName =
				ClassLoader.getSystemResource("edu/ksu/cis/indus/tools/slicer/default_slicer_configuration.xml");

			try {
				_inStream = _defaultConfigFileName.openStream();
			} catch (FileNotFoundException _e1) {
				LOGGER.fatal("Even default configuration file could not be found.  Aborting", _e1);
				System.exit(1);
			} catch (IOException _e2) {
				LOGGER.fatal("Could not retrieve a handle to default configuration file.  Aborting.", _e2);
				System.exit(1);
			}
		}

		try {
			_result = IOUtils.toString(_inStream);
		} catch (IOException _e) {
			LOGGER.fatal("IO error while reading configuration file.  Aborting", _e);
			IOUtils.closeQuietly(_inStream);
			System.exit(1);
		}
		return _result;
	}

	/**
	 * Sets up the output options according to the command line args.
	 *
	 * @param cl contains the command line.
	 * @param xmlizer that needs to be configured.
	 *
	 * @pre cl != null and xmlizer != null
	 */
	private static void setupOutputOptions(final CommandLine cl, final SliceXMLizerCLI xmlizer) {
		final String _outputDir;

		if (cl.hasOption('o')) {
			_outputDir = cl.getOptionValue("o");
		} else {
			final File _tempDir =
				new File(System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis() + "_slicer");
			_tempDir.mkdirs();
			_outputDir = _tempDir.getAbsolutePath();

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Using " + _outputDir + " as the directory to dump information");
			}
		}

		xmlizer.preResXMLJimpleDump = cl.hasOption('i');
		xmlizer.postResXMLJimpleDump = cl.hasOption('j') && cl.hasOption('r');
		xmlizer.preResJimpleDump = cl.hasOption('I');
		xmlizer.postResJimpleDump = cl.hasOption('J') && cl.hasOption('r');

		if (xmlizer.preResXMLJimpleDump
			  || xmlizer.postResXMLJimpleDump
			  || xmlizer.preResJimpleDump
			  || xmlizer.postResJimpleDump) {
			xmlizer.jimpleXMLDumpDir = _outputDir;
		}

		xmlizer.setOutputDirectory(_outputDir);
	}

	/**
	 * Changes the active configuration to use.
	 *
	 * @param configID is the id of the active configuration.
	 *
	 * @pre configID != null
	 */
	private void setConfigName(final String configID) {
		slicer.setActiveConfiguration(configID);
	}

	/**
	 * Sets if the slice should be residualized.
	 *
	 * @param flag <code>true</code> indicates the slice should be residualized; <code>false</code>, otherwise.
	 */
	private void setResidulization(final boolean flag) {
		residualize = flag;
	}

	/**
	 * Sets the name of the file containing the slice criteria specification.
	 *
	 * @param fileName of the slice criteria spec.
	 *
	 * @pre fileName != null
	 */
	private void setSliceCriteriaSpecFile(final String fileName) {
		criteriaSpecFileName = fileName;
	}

	/**
	 * Sets the name of the file containing the slice scope specification.
	 *
	 * @param fileName of the slice scope spec.
	 *
	 * @pre fileName != null
	 */
	private void setSliceScopeSpecFile(final String fileName) {
		sliceScopeSpecFileName = fileName;
	}

	/**
	 * Dumps jimple for the classes in the scene.  The jimple file names will end with the given suffix.
	 *
	 * @param suffix to be appended to the file name.
	 * @param jimpleFile <code>true</code> indicates that class files should be dumped as well; <code>false</code>,
	 * 		  otherwise.
	 * @param classFile <code>true</code> indicates that class files should be dumped as well; <code>false</code>, otherwise.
	 */
	private void dumpJimple(final String suffix, final boolean jimpleFile, final boolean classFile) {
		final Printer _printer = Printer.v();

		for (final Iterator _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (!_sc.hasTag(nameOfSliceTag)) {
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Dumping jimple for " + _sc);
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.isConcrete()) {
					try {
						_sm.retrieveActiveBody();
					} catch (final RuntimeException _e) {
						LOGGER.warn("Failed to retrieve body for method " + _sm, _e);
					}
				}
			}

			PrintWriter _writer = null;

			try {
				// write .jimple file
				if (jimpleFile) {
					final File _file = new File(outputDirectory + File.separator + _sc.getName() + ".jimple" + suffix);
					_writer = new PrintWriter(new FileWriter(_file));
					_printer.printTo(_sc, _writer);
				}

				// write .class file
				if (classFile) {
					_printer.write(_sc, outputDirectory);
				}
			} catch (final IOException _e) {
				LOGGER.warn("Error while writing " + _sc, _e);
			} catch (final RuntimeException _e) {
				LOGGER.warn("Error while writing class file of " + _sc, _e);
			} finally {
				if (_writer != null) {
					_writer.flush();
					_writer.close();
				}
			}
		}
	}

	/**
	 * Outputs the statistics for the system.
	 */
	private void outputStats() {
		if (LOGGER.isInfoEnabled()) {
			final MetricsProcessor _processor = new MetricsProcessor();
			final ProcessingController _pc = new ProcessingController();
			_pc.setProcessingFilter(new TagBasedProcessingFilter(SlicerTool.FLOW_ANALYSIS_TAG_NAME));
			_processor.hookup(_pc);
			_pc.setEnvironment(new Environment(scene));
			_pc.process();
			_processor.unhook(_pc);

			final ByteArrayOutputStream _stream1 = new ByteArrayOutputStream();
			MapUtils.verbosePrint(new PrintStream(_stream1), "PRE SLICING STATISTICS:", _processor.getStatistics());
			LOGGER.info(_stream1.toString());

			_pc.setProcessingFilter(new TagBasedProcessingFilter(nameOfSliceTag));
			_processor.hookup(_pc);
			_pc.setEnvironment(new Environment(scene));
			_pc.process();
			_processor.unhook(_pc);

			final ByteArrayOutputStream _stream2 = new ByteArrayOutputStream();
			MapUtils.verbosePrint(new PrintStream(_stream2), "POST SLICING STATISTICS:", _processor.getStatistics());
			LOGGER.info(_stream2.toString());
		}
	}

	/**
	 * Process the criteria specification file.
	 *
	 * @return a collection of criteria
	 *
	 * @throws IllegalArgumentException when the errors occur while accessing the specified file.
	 * @throws IllegalStateException when the parsing of the specified file fails.
	 *
	 * @post result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	private Collection processCriteriaSpecFile()
	  throws IllegalArgumentException, IllegalStateException {
		final Collection _criteria = new HashSet();

		if (criteriaSpecFileName != null) {
			try {
				final InputStream _in = new FileInputStream(criteriaSpecFileName);
				final String _result = IOUtils.toString(_in);
				IOUtils.closeQuietly(_in);

				final String _criteriaSpec = _result;
				_criteria.addAll(SliceCriteriaParser.deserialize(_criteriaSpec, scene));

				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Criteria specification before slicing: \n" + _result);
					LOGGER.info("Criteria before slicing: \n" + _criteria);
				}
			} catch (final IOException _e) {
				if (LOGGER.isWarnEnabled()) {
					final String _msg = "Error retrieved slicing criteria from " + criteriaSpecFileName;
					LOGGER.error(_msg, _e);

					final IllegalArgumentException _i = new IllegalArgumentException(_msg);
					_i.initCause(_e);
					throw _i;
				}
			} catch (final JiBXException _e) {
				if (LOGGER.isWarnEnabled()) {
					final String _msg = "JiBX failed during deserialization.";
					LOGGER.error(_msg, _e);

					final IllegalStateException _i = new IllegalStateException(_msg);
					_i.initCause(_e);
					throw _i;
				}
			}
		}
		return _criteria;
	}

	/**
	 * Process the scope specification file.
	 *
	 * @return the scope definition.
	 */
	private SpecificationBasedScopeDefinition processSliceScopeSpecFile() {
		SpecificationBasedScopeDefinition _result = null;

		if (sliceScopeSpecFileName != null) {
			try {
				final InputStream _in = new FileInputStream(sliceScopeSpecFileName);
				final String _contents = IOUtils.toString(_in);
				IOUtils.closeQuietly(_in);
				_result = SpecificationBasedScopeDefinition.deserialize(_contents);
			} catch (final IOException _e) {
				if (LOGGER.isWarnEnabled()) {
					final String _msg = "Error retrieved specification from " + sliceScopeSpecFileName;
					LOGGER.error(_msg, _e);

					final IllegalArgumentException _i = new IllegalArgumentException(_msg);
					_i.initCause(_e);
					throw _i;
				}
			} catch (final JiBXException _e) {
				if (LOGGER.isWarnEnabled()) {
					final String _msg = "JiBX failed during deserialization.";
					LOGGER.error(_msg, _e);

					final IllegalStateException _i = new IllegalStateException(_msg);
					_i.initCause(_e);
					throw _i;
				}
			}
		}
		return _result;
	}

	/**
	 * Residualize the slice as jimple files in the output directory.
	 */
	private void residualize() {
		if (preResXMLJimpleDump) {
			dumpJimpleAsXML("unsliced");
		}

		if (preResJimpleDump) {
			dumpJimple("_preRes", true, false);
		}

		if (residualize) {
			destructivelyUpdateJimple();
			dumpJimple("", false, true);
		}

		if (postResJimpleDump) {
			dumpJimple("_postRes", true, false);
		}

		if (postResXMLJimpleDump) {
			dumpJimpleAsXML("sliced");
		}
	}

	/**
	 * Displays the tool configuration GUI.
	 */
	private void showGUI() {
		// call the configurator on the slicer
		final Display _display = new Display();
		final Shell _shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		_shell.setText("Slicer configuration");

		// create a composite for the configurator to display information.
		final RowLayout _rowLayout = new RowLayout();
		_rowLayout.type = SWT.VERTICAL;
		_shell.setLayout(_rowLayout);
		slicer.getConfigurator().initialize(new Composite(_shell, SWT.NONE));

		// add a OK button to close the window.
		final Button _ok = new Button(_shell, SWT.PUSH);
		_ok.setText("Ok");
		_ok.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					_shell.dispose();
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});
		_shell.layout();
		_shell.pack();
		_shell.open();

		while (!_shell.isDisposed()) {
			if (!_display.readAndDispatch()) {
				_display.sleep();
			}
		}
		_display.dispose();

		// save the configuration
		try {
			if (configFileName != null) {
				FileUtils.writeStringToFile(new File(configFileName), slicer.stringizeConfiguration(), "UTF-8");
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Configuration file name is unspecified.  Printing to console.");
				}
				System.out.println(slicer.stringizeConfiguration());
			}
		} catch (IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Could not write the configuration file.  Printing to console", _e);
			}
			System.out.println(slicer.stringizeConfiguration());
		}
	}

	/**
	 * Write the slice as XML document.
	 */
	private void writeXML() {
		final IXMLizer _xmlizer = getXMLizer();

		// serialize the output of the slicer
		final Map _info = new HashMap();
		_info.put(IEnvironment.ID, slicer.getSystem());
		_info.put(IStmtGraphFactory.ID, slicer.getStmtGraphFactory());
		_xmlizer.setXmlOutputDir(outputDirectory);
		_xmlizer.writeXML(_info);
	}
}

// End of File
