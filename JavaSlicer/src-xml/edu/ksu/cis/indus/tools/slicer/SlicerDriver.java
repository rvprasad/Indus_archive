
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

import edu.ksu.cis.indus.staticanalyses.dependency.xmlizer.CGBasedXMLizingController;
import edu.ksu.cis.indus.staticanalyses.dependency.xmlizer.DependencyXMLizer;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.support.SootBasedDriver;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.transformations.slicer.TagBasedSlicingTransformer;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.net.URL;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * This is the command-line driver class for the slicer tool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerDriver
  extends SootBasedDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerDriver.class);

	/**
	 * This is the name of the configuration file to use.
	 */
	private static String configFileName;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final String SUFFIX_FOR_XMLIZATION_PURPOSES = "slicer";

	/**
	 * This is the name of the directory into which the slicer will dump sliced artifacts into.
	 */
	protected String outputDirectory;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	SlicerTool slicer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * DOCUMENT ME!
	 *
	 * @param generator DOCUMENT ME!
	 */
	protected SlicerDriver(final IJimpleIDGenerator generator) {
		slicer = new SlicerTool();
		idGenerator = generator;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CustomDependencyXMLizer
	  extends DependencyXMLizer {
		/**
		 * Creates a new CustomDependencyXMLizer object.
		 */
		CustomDependencyXMLizer() {
			super(false);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		public void populateDAs() {
			das.addAll(slicer.getDAs());
		}
	}

	/**
	 * The entry point to the driver.
	 *
	 * @param args contains the command line arguments.
	 */
	public static void main(final String[] args) {
		SlicerDriver driver = new SlicerDriver(new UniqueJimpleIDGenerator());

		// parse command line arguments
		parseCommandLine(args, driver);

		driver.initialize();
		driver.setUpTransformer();
		driver.execute();
		// serialize the output of the slicer
		driver.writeXML();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param configuration DOCUMENT ME!
	 */
	protected void setConfiguration(final String configuration) {
		slicer.destringizeConfiguration(configuration);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param oDir DOCUMENT ME!
	 */
	protected void setOutputDirectory(final String oDir) {
		outputDirectory = oDir;
	}

	/**
	 * DOCUMENT ME!
	 */
	protected void setUpTransformer() {
		slicer.setTransformer(new TagBasedSlicingTransformer());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	protected AbstractSliceXMLizer getXMLizer() {
		AbstractSliceXMLizer result;

		try {
			Writer out = new FileWriter(new File(outputDirectory + File.separator + SUFFIX_FOR_XMLIZATION_PURPOSES + ".xml"));
			result = new TagBasedSliceXMLizer(out, TagBasedSlicingTransformer.SLICING_TAG, idGenerator);
		} catch (IOException e) {
			LOGGER.error("Exception while opening file to write xml information.", e);
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected void execute() {
		// execute the slicer
		slicer.setSystem(scene);
		slicer.setRootMethods(rootMethods);
		slicer.setCriteria(Collections.EMPTY_LIST);
		slicer.initialize();
		slicer.run(Phase.STARTING_PHASE);

		while (!slicer.isStable()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.error("Error while waiting for the tool to finish.", e);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	void writeXML() {
		ICallGraphInfo cgi = slicer.getCallGraph();
		CGBasedXMLizingController ctrl = new CGBasedXMLizingController(cgi);
		ctrl.setEnvironment(slicer.getEnvironment());

		AbstractSliceXMLizer sliceIP = getXMLizer();
		CustomDependencyXMLizer dep = new CustomDependencyXMLizer();
		dep.setClassNames(rootMethods);
		dep.setGenerator(idGenerator);
		dep.populateDAs();
		sliceIP.hookup(ctrl);

		Map xmlizers = dep.initXMLizers(SUFFIX_FOR_XMLIZATION_PURPOSES, ctrl);
		ctrl.process();
		dep.flushXMLizers(xmlizers, ctrl);
		sliceIP.unhook(ctrl);
		sliceIP.flush();
	}

	/**
	 * Parses the command line argument.
	 *
	 * @param args contains the command line arguments.
	 * @param xmlizer DOCUMENT ME!
	 */
	private static void parseCommandLine(final String[] args, final SlicerDriver xmlizer) {
		// create options
		Options options = new Options();
		Option o = new Option("c", false, "The configuration file to use.  If unspecified, uses default configuration file.");
		o.setArgs(1);
		options.addOption(o);
		o = new Option("o", false,
				"The output directory to dump the slice info into.  If unspecified, defaults to current directory.");
		o.setArgs(1);
		options.addOption(o);
		o = new Option("g", false, "Display gui for configuration.");
		o.setOptionalArg(false);
		options.addOption(o);
		o = new Option("p", false, "Prepend this to soot class path.");
		o.setArgs(1);
		options.addOption(o);

		CommandLine cl = null;

		// parse the arguments
		try {
			cl = (new BasicParser()).parse(options, args);

			String config = cl.getOptionValue("c");
			configFileName = config;

			if (config == null) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Using default configuration as none was specified.");
				}

				URL defaultConfigFileName =
					ClassLoader.getSystemResource("edu/ksu/cis/indus/tools/slicer/default_slicer_configuration.xml");
				config = defaultConfigFileName.getFile();
			}

			try {
				BufferedReader br = new BufferedReader(new FileReader(config));
				StringBuffer buffer = new StringBuffer();

				while (br.ready()) {
					buffer.append(br.readLine());
				}
				xmlizer.setConfiguration(buffer.toString());
			} catch (FileNotFoundException e1) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Non-existent configuration file specified. Using default configuration.");
				}
			} catch (IOException e2) {
				LOGGER.fatal("IO error while reading configuration file.");
			}

			String outputDir = cl.getOptionValue("o");

			if (outputDir == null) {
				System.out.println("Warning: Using the currennt directory to dump slicing artifacts.");
				outputDir = ".";
			}
			xmlizer.setOutputDirectory(outputDir);

			String classpath = cl.getOptionValue("p");

			if (classpath != null) {
				xmlizer.addToSootClassPath(classpath);
			}
		} catch (ParseException e) {
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.tools.slicer.SlicerDriver <options> <class names>",
				options, true);
			LOGGER.fatal("Incorrect command line", e);
			System.exit(1);
		}

		List result = cl.getArgList();

		if (result.isEmpty()) {
			LOGGER.fatal("Please specify atleast one class that contains an entry method into the system to be sliced.");
			System.exit(1);
		}

		if (cl.hasOption('g')) {
			xmlizer.showGUI();
		}

		xmlizer.setClassNames(cl.getArgList());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	private void showGUI() {
		// call the configurator on the slicer
		Display display = new Display();
		Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Slicer configuration");
		slicer.getConfigurator().initialize(shell);
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();

		// save the configuration
		try {
			if (configFileName != null) {
				BufferedWriter configFile = new BufferedWriter(new FileWriter(configFileName));
				configFile.write(slicer.stringizeConfiguration());
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Configuration file name is unspecified.  Printing to console.");
				}
				System.out.println(slicer.stringizeConfiguration());
			}
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Could not write the configuration file.  Printing to console", e);
			}
			System.out.println(slicer.stringizeConfiguration());
		}
	}
}

/*
   ChangeLog:
   $Log$
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
