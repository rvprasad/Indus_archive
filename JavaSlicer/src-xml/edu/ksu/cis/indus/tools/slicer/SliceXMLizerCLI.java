
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

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.slicer.transformations.TagBasedDestructiveSliceResidualizer;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.SootValueTypeManager;

import edu.ksu.cis.indus.tools.Phase;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.JimpleXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import java.net.URL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
	 * This is the suffix used for the files into which the slice information will be dumped in XML.
	 */
	public static final String SUFFIX_FOR_XMLIZATION_PURPOSES = "slicer";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceXMLizerCLI.class);

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
	 * This is the writer used to write the xmlized Jimple.
	 */
	Writer xmlizedJimpleWriter;

	/**
	 * The id generator used during xmlization.
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * This is the name of the tag to be used to tag parts of the AST occurring in the slice.
	 */
	private final String nameOfSliceTag = "SliceXMLizerCLI";

	/**
	 * This indicates if jimple should be destructively updated to reflect the slice and dumped.
	 */
	private boolean destructiveJimpleUpdate;

	/**
	 * Creates an instance of this class.
	 *
	 * @param generator used to generate the id's during xmlization.
	 *
	 * @pre generator != null
	 */
	protected SliceXMLizerCLI(final IJimpleIDGenerator generator) {
		slicer = new SlicerTool(new BitSetTokenManager(new SootValueTypeManager()));
		cfgProvider = slicer.getStmtGraphFactory();
		idGenerator = generator;
	}

	/**
	 * The entry point to the driver.
	 *
	 * @param args contains the command line arguments.
	 *
	 * @throws RuntimeException when an Throwable exception beyond our control occurs.
	 */
	public static void main(final String[] args) {
		try {
			final SliceXMLizerCLI _driver = new SliceXMLizerCLI(new UniqueJimpleIDGenerator());

			// parse command line arguments
			parseCommandLine(args, _driver);

			_driver.initialize();

			final long _startTime = System.currentTimeMillis();
			_driver.execute();

			final long _stopTime = System.currentTimeMillis();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("It took " + (_stopTime - _startTime) + "ms to identify the slice.");
			}

			_driver.writeXML();
			_driver.residualize();
		} catch (Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
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
	 * @throws RuntimeException if the file into which the xmlizer will write the data cannot be opened.
	 *
	 * @post result != null
	 */
	protected final TagBasedSliceXMLizer getXMLizer() {
		TagBasedSliceXMLizer _result;

		try {
			final Writer _out =
				new FileWriter(new File(outputDirectory + File.separator + SUFFIX_FOR_XMLIZATION_PURPOSES + ".xml"));
			_result = new TagBasedSliceXMLizer(nameOfSliceTag, idGenerator);
		} catch (IOException _e) {
			LOGGER.error("Exception while opening file to write xml information.", _e);
			throw new RuntimeException(_e);
		}
		return _result;
	}

	/**
	 * Executes the slicer.
	 */
	protected final void execute() {
		// execute the slicer
		slicer.setTagName(nameOfSliceTag);
		slicer.setSystem(scene);
		slicer.setRootMethods(rootMethods);
		slicer.setCriteria(Collections.EMPTY_LIST);
		slicer.run(Phase.STARTING_PHASE, true);
	}

	/**
	 * DOCUMENT ME!
	 */
	void writeXML() {
		dumpJimple();

		// serialize the output of the slicer
		final Map _info = new HashMap();
		_info.put(IEnvironment.ID, slicer.getEnvironment());
		_info.put(IStmtGraphFactory.ID, slicer.getStmtGraphFactory());

		final AbstractXMLizer _xmlizer = getXMLizer();
		_xmlizer.setXmlOutputDir(outputDirectory);
		_xmlizer.writeXML(_info);
	}

	/**
	 * DOCUMENT ME!
	 */
	private void dumpJimple() {
		final ICallGraphInfo _cgi = slicer.getCallGraph();
		final ProcessingController _ctrl = new ProcessingController();
		_ctrl.setStmtGraphFactory(getStmtGraphFactory());
		_ctrl.setEnvironment(slicer.getEnvironment());
		_ctrl.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));

		JimpleXMLizer _jimpler = null;

		if (xmlizedJimpleWriter != null) {
			_jimpler = new JimpleXMLizer(idGenerator);
			_jimpler.setWriter(xmlizedJimpleWriter);
			_jimpler.hookup(_ctrl);
		}

		_ctrl.process();

		if (_jimpler != null) {
			_jimpler.unhook(_ctrl);

			try {
				xmlizedJimpleWriter.flush();
				xmlizedJimpleWriter.close();
			} catch (IOException _e) {
				LOGGER.error("Failed to close the xml file based for jimple representation.", _e);
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
				"The output directory to dump the generated info.  If unspecified, defaults to current directory.");
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
		_o = new Option("j", "output-jimple", false, "Output xml representation of the jimple.");
		_o.setOptionalArg(false);
		_options.addOption(_o);
		_o = new Option("d", "destructive-jimple-update", false, "Destructively update jimple and dump it.");
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
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.tools.slicer.SliceXMLizerCLI <options> <class names>",
				_options, true);

			if (_exception != null) {
				LOGGER.fatal("Incorrect command line.  Aborting.", _exception);
				System.exit(1);
			} else {
				System.exit(0);
			}
		}
		xmlizer.setConfiguration(processCommandLineForConfiguration(_cl));
		xmlizer.destructiveJimpleUpdate = _cl.hasOption('d');
		setupOutputOptions(_cl, xmlizer);

		if (_cl.hasOption('p')) {
			xmlizer.addToSootClassPath(_cl.getOptionValue('p'));
		}

		final List _result = _cl.getArgList();

		if (_result.isEmpty()) {
			LOGGER.fatal("Please specify atleast one class that contains an entry method into the system to be sliced.");
			System.exit(1);
		}

		if (_cl.hasOption('g')) {
			xmlizer.showGUI();
		}

		if (_cl.hasOption('a')) {
			xmlizer.setConfigName(_cl.getOptionValue('a'));
		}

		xmlizer.setClassNames(_cl.getArgList());
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
			_outputDir = ".";

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Using current directory to output artifacts.");
			}
		}

		if (cl.hasOption('j')) {
			try {
				xmlizer.xmlizedJimpleWriter = new FileWriter(new File(_outputDir + File.separator + "jimple.xml"));
			} catch (IOException _e) {
				LOGGER.fatal("IO error while creating jimple dump file.  Aborting", _e);
				System.exit(1);
			}
		}
		xmlizer.setOutputDirectory(_outputDir);
	}

	/**
	 * Changes the active configuration to use.
	 *
	 * @param configID is the id of the active configuration.
	 *
	 * @pre configName != null
	 */
	private void setConfigName(final String configID) {
		slicer.setActiveConfiguration(configID);
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
		Reader _reader = null;
		String _result = null;

		if (_config != null) {
			try {
				_reader = new FileReader(_config);
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
				_reader = new InputStreamReader(_defaultConfigFileName.openStream());
			} catch (FileNotFoundException _e1) {
				LOGGER.fatal("Even default configuration file could not be found.  Aborting", _e1);
				System.exit(1);
			} catch (IOException _e2) {
				LOGGER.fatal("Could not retrieve a handle to default configuration file.  Aborting.", _e2);
				System.exit(1);
			}
		}

		try {
			final BufferedReader _br = new BufferedReader(_reader);
			final StringBuffer _buffer = new StringBuffer();

			while (_br.ready()) {
				_buffer.append(_br.readLine());
			}
			_result = _buffer.toString();
		} catch (IOException _e) {
			LOGGER.fatal("IO error while reading configuration file.  Aborting", _e);
			System.exit(1);
		}
		return _result;
	}

	/**
	 * Residualize the slice as jimple files in the output directory.
	 */
	private void residualize() {
		if (destructiveJimpleUpdate) {
			final TagBasedDestructiveSliceResidualizer _residualizer = new TagBasedDestructiveSliceResidualizer();
			_residualizer.setTagToResidualize(nameOfSliceTag);
			_residualizer.residualizeSystem(scene, slicer.getStmtGraphFactory());
		}

		final Printer _printer = Printer.v();

		for (final Iterator _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (!_sc.hasTag(SlicerTool.FLOW_ANALYSIS_TAG_NAME)) {
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualizing " + _sc);
			}

			PrintWriter _writer = null;

			try {
				final File _file = new File(outputDirectory + File.separator + _sc.getName() + ".jimple");

                // DEL_START
				/*for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
					final SootMethod _sm = (SootMethod) _j.next();

					if (_sm.isConcrete()) {
						_sm.retrieveActiveBody().validateLocals();
					}
				}*/
                // DEL_END
				_writer = new PrintWriter(new FileWriter(_file));
				// write .jimple file
				_printer.printTo(_sc, _writer);
				// write .class file
				_printer.write(_sc, outputDirectory);
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
	 * Displays the tool configuration GUI.
	 */
	private void showGUI() {
		// call the configurator on the slicer
		final Display _display = new Display();
		final Shell _shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		_shell.setText("Slicer configuration");
		slicer.getConfigurator().initialize(_shell);
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
				final BufferedWriter _configFile = new BufferedWriter(new FileWriter(configFileName));
				_configFile.write(slicer.stringizeConfiguration());
				_configFile.close();
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
}

/*
   ChangeLog:
   $Log$
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
