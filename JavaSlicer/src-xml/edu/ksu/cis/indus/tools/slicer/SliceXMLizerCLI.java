
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
  extends SootBasedDriver {
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
	 * This is the name of the tag to be used to tag parts of the AST occurring in the slice.
	 */
	private final String nameOfSliceTag = "indus.tools.slicer.SliceXMLizerCLI:SLICER";

	/** 
	 * The name of the criteria specification file.
	 */
	private String criteriaSpecFileName;

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
		_xmlizer.setIDGenerator(new UniqueJimpleIDGenerator());

		// parse command line arguments
		parseCommandLine(args, _xmlizer);

		_xmlizer.initialize();

		final long _startTime = System.currentTimeMillis();
		_xmlizer.execute();

		final long _stopTime = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("It took " + (_stopTime - _startTime) + "ms to identify the slice.");
		}

		_xmlizer.writeXML();
		_xmlizer.residualize();
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
	 * Executes the slicer.
	 */
	protected void execute() {
		// execute the slicer
		slicer.setTagName(nameOfSliceTag);
		slicer.setSystem(scene);
		slicer.setRootMethods(rootMethods);

		final Collection _criteria = new HashSet();

		if (criteriaSpecFileName != null) {
			try {
				final InputStream _in = new FileInputStream(criteriaSpecFileName);
				final String _result = IOUtils.toString(_in);
				IOUtils.closeQuietly(_in);

				final String _criteriaSpec = _result;
				_criteria.addAll(SliceCriteriaParser.deserialize(_criteriaSpec, scene));

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Criteria specification before slicing: \n" + _result);
					LOGGER.debug("Criteria before slicing: \n" + _criteria);
				}
			} catch (final IOException _e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Error retrieved slicing criteria from " + criteriaSpecFileName, _e);
				}
			} catch (final JiBXException _e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("JiBX failed during deserialization.", _e);
				}
			}
		}
		slicer.setCriteria(_criteria);
		slicer.addToolProgressListener(new IToolProgressListener() {
				public void toolProgess(final ToolProgressEvent evt) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info(evt.getMsg() + " - " + evt.getInfo());
					}
				}
			});
		slicer.run(Phase.STARTING_PHASE, true);

		if (LOGGER.isDebugEnabled()) {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Criteria specification after slicing: \n"
						+ SliceCriteriaParser.serialize(slicer.getCriteria()));
				}
			} catch (final JiBXException _e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("JiBX faild during serialization.", _e);
				}
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
		_residualizer.residualizeSystem(scene);
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
		_o.setArgName("path");
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

		final List _result = _cl.getArgList();

		if (_result.isEmpty()) {
			LOGGER.fatal("Please specify atleast one class that contains an entry method into the system to be sliced.");
			System.exit(1);
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

		xmlizer.setClassNames(_cl.getArgList());
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
				LOGGER.warn("Non-existent configuration file specified.", _e);
				_config = null;
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
					_sm.retrieveActiveBody();
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
				LOGGER.error("Error while writing " + _sc, _e);
			} finally {
				if (_writer != null) {
					_writer.flush();
					_writer.close();
				}
			}
		}
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
		_info.put(IEnvironment.ID, new Environment(slicer.getSystem()));
		_info.put(IStmtGraphFactory.ID, slicer.getStmtGraphFactory());
		_xmlizer.setXmlOutputDir(outputDirectory);
		_xmlizer.writeXML(_info);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.50  2004/08/04 02:11:26  venku
   - documentation.
   Revision 1.49  2004/07/25 01:35:37  venku
   - ripple effect of the change to TagBasedSliceResidualizer.
   Revision 1.48  2004/07/20 05:36:05  venku
   - slicer criteria specifying options was missing.  How? FIXED.
   Revision 1.47  2004/07/10 00:49:40  venku
   - In pre-residualizaion jimple/xml dump, only the classes who are in the slice are dumped.
   Revision 1.46  2004/07/07 00:43:54  venku
   - class files are dumped only if residualization is requested.
   Revision 1.45  2004/07/04 12:17:58  venku
   - added cli option to control residualization.
   Revision 1.44  2004/07/03 00:07:56  venku
   - incorrect message.
   Revision 1.43  2004/07/02 10:08:25  venku
   - logging.
   Revision 1.42  2004/07/02 09:00:08  venku
   - added support to serialize/deserialize slice criteria. (feature #397)
   - used the above support in SliceXMLizerCLI.
   - used Jakarta Commons IO library.
   Revision 1.41  2004/06/26 10:16:35  venku
   - bug #389. FIXED.
   Revision 1.40  2004/06/14 08:39:29  venku
   - added a property to SootBasedDriver to control the type of statement graph
     factory to be used.
   - removed getDefaultFactory() from ExceptionFlowSensitiveStmtGraphFactory.
   - ripple effect.
   Revision 1.39  2004/06/12 06:47:27  venku
   - documentation.
   - refactoring.
   - coding conventions.
   - catered feature request 384, 385, and 386.
   Revision 1.38  2004/06/03 03:50:34  venku
   - changed the way help will be output on command line classes.
   Revision 1.37  2004/05/29 00:11:44  venku
   - moved the OK button out of the composite configurator.
   - the client will provide a composite to the configurator to display and
     collect info and the client will handle the closing of the composite's parent.
   Revision 1.36  2004/05/28 21:53:20  venku
   - added a method to ExceptionFlowSensitiveGraphFactory to create
     default factory objects.
   Revision 1.35  2004/05/25 19:09:39  venku
   - changed command line options.
   Revision 1.34  2004/05/14 03:10:41  venku
   - The destructively updated jimple can be dumped during tearDown() as
     by then all tests would have completed, hence, not impacting the id
     generation.
   Revision 1.33  2004/05/11 22:21:44  venku
   - added options to control pre/post residualization jimple dumps.
   Revision 1.32  2004/05/11 22:17:16  venku
   - privatized some methods.
   - enabled dumping of pre-residulization and post-residualization jimple.
   Revision 1.31  2004/05/11 11:59:48  venku
   - privatized destructivelyUpdateJimple.
   Revision 1.30  2004/05/10 12:07:45  venku
   - on thinking it seems better to dump jimple as a post-slice artifact as
     it is possible to generate the jimple for the original files rather easily.
   Revision 1.29  2004/05/10 09:40:16  venku
   - changed the way jimple is dumped.
   Revision 1.28  2004/05/10 08:12:03  venku
   - streamlined the names of tags that are used.
   - deleted SlicingTag class.  NamedTag is used instead.
   - ripple effect.
   - SliceCriteriaFactory's interface is enhanced to generate individual
     slice criterion as well as criteria set for all nodes in the given AST chunk.
   Revision 1.27  2004/05/09 11:09:46  venku
   - the client can now specify the statement graph factory to use during slicing.
   Revision 1.26  2004/05/09 11:01:14  venku
   - slice can be seen easily if the user just sees the slice.  So, there
     is no point in having -d option.  Hence, it was removed.
   - temporary directory is used to dump the slice instead of the current
     directory when no directory is mentioned.
   Revision 1.25  2004/05/09 10:41:46  venku
   - slice can be seen easily if the user just sees the slice.  So, there
     is no point in having -d option.  Hence, it was removed.
   - temporary directory is used to dump the slice instead of the current
     directory when no directory is mentioned.
   Revision 1.24  2004/05/09 09:59:41  venku
   - deleted an unused constant.
   Revision 1.23  2004/05/04 09:58:21  venku
   - the test will also drive tagbased slice residualizer via the new
     method added to SliceXMLizerCLI.
   Revision 1.22  2004/05/04 07:55:44  venku
   - id generator was not initialized correctly in slicer test setup. FIXED.
   Revision 1.21  2004/04/25 23:18:20  venku
   - coding conventions.
   Revision 1.20  2004/04/25 21:18:41  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
   Revision 1.19  2004/04/23 01:01:10  venku
   - coding conventions.
   Revision 1.18  2004/04/23 01:00:49  venku
   - trying to resolve issues with canonicalization of Jimple.
   Revision 1.17  2004/04/23 00:42:37  venku
   - trying to get canonical xmlized Jimple representation.
   Revision 1.16  2004/04/22 23:32:32  venku
   - xml file name were setup incorrectly.  FIXED.
   Revision 1.15  2004/04/22 22:12:08  venku
   - made changes to jimple xmlizer to dump each class into a separate file.
   - ripple effect.
   Revision 1.14  2004/04/22 10:23:11  venku
   - added getTokenManager() method to OFAXMLizerCLI to create
     token manager based on a system property.
   - ripple effect.
   Revision 1.13  2004/04/20 06:53:15  venku
   - documentation.
   Revision 1.12  2004/04/20 00:43:40  venku
   - The processing during residualization was driven by a graph.  This
     caused errors when the graph did not cover all of the statements.
     Hence, during residualization we will visit all parts of a method.
   Revision 1.11  2004/04/19 19:10:32  venku
   - We should not have a situation of body not found if we slice right.
     Commented the block that does this refitting.
   Revision 1.10  2004/04/18 08:59:00  venku
   - enabled test support for slicer.
   Revision 1.9  2004/04/16 20:10:41  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.8  2004/03/29 01:55:08  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.7  2004/03/26 00:25:35  venku
   - added new options to specify an active configuration from the command line.
   Revision 1.6  2004/03/21 20:13:17  venku
   - many file handles are left open. FIXED.
   Revision 1.5  2004/03/04 14:02:09  venku
   - removed a redundant exception check.
   Revision 1.4  2004/03/03 10:09:42  venku
   - refactored code in ExecutableSlicePostProcessor and TagBasedSliceResidualizer.
   Revision 1.3  2004/03/03 08:07:52  venku
   - coding convention.
   Revision 1.2  2004/03/03 08:07:31  venku
   - removed portions marked as to be removed.
   Revision 1.1  2004/03/03 08:06:17  venku
   - renamed SliceXMLizer to SliceXMLizerCLI.
   Revision 1.13  2004/03/03 08:03:52  venku
   - formatting.
   Revision 1.12  2004/02/25 23:40:31  venku
   - well package naming convention was inconsistent. FIXED.
   Revision 1.11  2004/02/09 06:54:18  venku
   - deleted dependency xmlization and test classes.
   - ripple effect.
   Revision 1.10  2004/02/09 06:49:27  venku
   - empty log message
     Revision 1.9  2004/02/09 04:39:57  venku
     -
     Revision 1.8  2004/02/09 02:21:51  venku
     - ripple effect of refactoring xmlizing framework.
     Revision 1.7  2004/02/08 03:06:16  venku
     - refactoring of xmlizers in staticanalyses.
     Revision 1.6  2004/01/22 13:07:30  venku
     - check on output directory missing.  FIXED.
     Revision 1.5  2004/01/22 01:06:32  venku
     - coding convention.
     Revision 1.4  2004/01/17 23:52:04  venku
     - minor command line fix.
     Revision 1.3  2004/01/13 10:59:42  venku
     - systemTagName is not required by TagBasedDestructiveSliceResidualizer.
       It was deleted.
     - ripple effect.
     Revision 1.2  2004/01/09 23:13:42  venku
     - formatting
     - coding convention
     - logging
     Revision 1.1  2004/01/09 07:02:11  venku
     - Made -o mandatory in SliceDriver.
     - all information is dumped into directory specified via -o.
     - Renamed SliceDriver to SliceXMLizerCLI.
     Revision 1.31  2003/12/27 20:07:45  venku
     - fixed xmlizers/driver to not throw exception
       when -h is specified
     Revision 1.30  2003/12/16 12:43:04  venku
     - changed jimple/class file dumping code.
     Revision 1.29  2003/12/16 00:29:21  venku
     - documentation.
     Revision 1.28  2003/12/15 16:35:29  venku
     - added option to dump jimple.
     Revision 1.27  2003/12/15 02:11:11  venku
     - added exception handling at outer most level.
     Revision 1.26  2003/12/13 19:46:45  venku
     - documentation.
     Revision 1.25  2003/12/13 02:29:16  venku
     - Refactoring, documentation, coding convention, and
       formatting.
     Revision 1.24  2003/12/09 12:23:48  venku
     - added support to control synchronicity of method runs.
     - ripple effect.
     Revision 1.23  2003/12/09 12:10:17  venku
     - retrieval of config file from jar fails. FIXED.
     Revision 1.22  2003/12/09 09:50:54  venku
     - amended output of string output to be XML compliant.
       This means some characters that are unrepresentable in
       XML are omitted.
     Revision 1.21  2003/12/09 04:22:14  venku
     - refactoring.  Separated classes into separate packages.
     - ripple effect.
     Revision 1.20  2003/12/08 12:20:48  venku
     - moved some classes from staticanalyses interface to indus interface package
     - ripple effect.
     Revision 1.19  2003/12/08 12:16:05  venku
     - moved support package from StaticAnalyses to Indus project.
     - ripple effect.
     - Enabled call graph xmlization.
     Revision 1.18  2003/12/02 09:42:18  venku
     - well well well. coding convention and formatting changed
       as a result of embracing checkstyle 3.2
     Revision 1.17  2003/12/01 04:20:10  venku
     - tag name should be provided for the engine before execution.
     Revision 1.16  2003/11/30 09:02:01  venku
     - incorrect processing filter used during xmlization. FIXED.
     Revision 1.15  2003/11/30 02:38:44  venku
     - changed the name of SLICING_TAG.
     Revision 1.14  2003/11/30 00:10:20  venku
     - Major refactoring:
       ProcessingController is more based on the sort it controls.
       The filtering of class is another concern with it's own
       branch in the inheritance tree.  So, the user can tune the
       controller with a filter independent of the sort of processors.
     Revision 1.13  2003/11/28 22:12:53  venku
     - dumps call graph.
     - logging.
     Revision 1.12  2003/11/28 16:37:42  venku
     - slicer tool was initialized after setup and this erased previous
       configuration info. FIXED.
     - config file opening and defaulting logic was broken. FIXED.
     Revision 1.11  2003/11/24 10:12:03  venku
     - there are no residualizers now.  There is a very precise
       slice collector which will collect the slice via tags.
     - architectural change. The slicer is hard-wired wrt to
       slice collection.  Residualization is outside the slicer.
     Revision 1.10  2003/11/24 09:01:07  venku
     - closed the jimple output stream.
     Revision 1.9  2003/11/24 01:21:57  venku
     - added command line option for jiimple output.
     Revision 1.8  2003/11/24 00:11:42  venku
     - moved the residualizers/transformers into transformation
       package.
     - Also, renamed the transformers as residualizers.
     - opened some methods and classes in slicer to be public
       so that they can be used by the residualizers.  This is where
       published interface annotation is required.
     - ripple effect of the above refactoring.
     Revision 1.7  2003/11/20 07:42:07  venku
     - added command line option "-p" to specify classpath for
       soot.
     Revision 1.6  2003/11/17 17:56:21  venku
     - reinstated initialize() method in AbstractTool and SlicerTool.  It provides a neat
       way to intialize the tool independent of how it's dependent
       parts (such as configuration) were instantiated and intialized.
     Revision 1.5  2003/11/17 16:58:12  venku
     - populateDAs() needs to be called from outside the constructor.
     - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
     Revision 1.4  2003/11/17 15:25:17  venku
     - added new method to AbstractSliceXMLizer to flush writer.
     - called flush on xmlizer from the driver.
     - erroneous file name was being constructed. FIXED.
     - added tabbing and new line to output in TagBasedSliceXMLizer.
     Revision 1.3  2003/11/17 03:22:55  venku
     - added junit test support for Slicing.
     - refactored code in test for dependency to make it more
       simple.
     Revision 1.2  2003/11/17 02:23:52  venku
     - documentation.
     - xmlizers require streams/writers to be provided to them
       rather than they constructing them.
     Revision 1.1  2003/11/17 01:39:42  venku
     - added slice XMLization support.
     Revision 1.7  2003/11/09 07:58:33  venku
     - default configuration has been put into file instead
       of embedding it into the source code.
     Revision 1.6  2003/11/05 08:26:42  venku
     - changed the xml schema for the slicer configuration.
     - The configruator, driver, and the configuration handle
       these changes.
     Revision 1.5  2003/11/03 08:05:34  venku
     - lots of changes
       - changes to get the configuration working with JiBX
       - changes to make configuration amenable to CompositeConfigurator
       - added EquivalenceClassBasedAnalysis
       - added fix for Thread's start method
     Revision 1.4  2003/10/21 08:42:44  venku
     - added code to actually drive the slicer by considering
       main() methods as root methods.  This is temporary.
       Eventually, there will be an analysis tool configurator.
     Revision 1.3  2003/10/20 13:55:25  venku
     - Added a factory to create new configurations.
     - Simplified AbstractToolConfigurator methods.
     - The driver manages the shell.
     - Got all the gui parts running EXCEPT for changing
       the name of the configuration.
     Revision 1.2  2003/10/14 05:35:41  venku
     - documentation.
     Revision 1.1  2003/10/14 05:33:26  venku
     - First cut at slicer driver.  This will be used to drive the testing
       of the slicer.
 */
