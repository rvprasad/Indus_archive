
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

import edu.ksu.cis.indus.tools.Phase;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
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


/**
 * This is the command-line driver class for the slicer tool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerDriver.class);

	/**
	 * The default configuration.   TODO: This should be moved out to a file/resource.
	 */
	private static String configuration =
		"<slicerConfiguration " + "xmlns:slicer=\"http://indus.projects.cis.ksu.edu/slicer\""
		+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
		+ "xsi:schemaLocation=\"http://indus.projects.cis.ksu.edu/slicer slicerConfig.xsd\" activeConfiguration=\"base\">"
		+ "<configurationInfo slicetype=\"BACKWARD_SLICE\" analysis=\"base\" name=\"first\" sliceForDeadlock=\"true\">"
		+ "<divergence active=\"true\" interprocedural=\"true\"/>" + "<interference equivalenceClassBased=\"true\"/>"
		+ "<ready active=\"true\" rule1=\"true\" rule2=\"true\" rule3=\"true\" rule4=\"true\" "
		+ "equivalenceClassBased=\"true\"/>" + "</configurationInfo>" + "</slicerConfiguration>";

	/**
	 * This is the name of the directory into which the slicer will dump sliced artifacts into.
	 */
	private static File outputDirectory;

	/**
	 * This is the name of the configuration file to use.
	 */
	private static String configFileName;

	/**
	 * The entry point to the driver.
	 *
	 * @param args contains the command line arguments.
	 */
	public static void main(final String[] args) {
		// parse command line arguments
		parseCommandLine(args);

		// create the slicer tool
		SlicerTool slicer = new SlicerTool();
		slicer.destringizeConfiguration(configuration);

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

		// execute the slicer
        
		slicer.run(Phase.STARTING_PHASE);

		// serialize the output of the slicer
	}

	/**
	 * Parses the command line argument.
	 *
	 * @param args contains the command line arguments.
	 */
	private static void parseCommandLine(final String[] args) {
		// create options
		Options options = new Options();
		options.addOption("c", false, "The configuration file to use.");
		options.addOption("o", false, "The output directory to dump the slice info into.");

		// parse the arguments
		try {
			CommandLine cl = (new BasicParser()).parse(options, args);
			configFileName = cl.getOptionValue("c");

			if (configFileName == null) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Using default configuration as none was specified.");
				}
			} else {
				try {
					BufferedReader br = new BufferedReader(new FileReader(configFileName));
					StringBuffer buffer = new StringBuffer();

					while (br.ready()) {
						buffer.append(br.readLine());
					}
					configuration = buffer.toString();
				} catch (FileNotFoundException e1) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Non-existent configuration file specified. Using default configuration.");
					}
				} catch (IOException e2) {
					LOGGER.fatal("IO rror while reading configuration file.");
				}
			}

			String outputDir = cl.getOptionValue("o");

			if (outputDir == null) {
				System.out.println("Warning: Using the currennt directory to dump slicing artifacts.");
			} else {
				outputDirectory = new File(outputDir);

				if (!outputDirectory.exists()) {
					LOGGER.error("Non-existent output directory specified.  Aborting.");
					System.exit(2);
				} else if (outputDirectory.canWrite()) {
					LOGGER.error("Output directory cannot be written into.  Aborting.");
					System.exit(3);
				}
			}
		} catch (ParseException e) {
			(new HelpFormatter()).printHelp("java edu.ksu.cis.indus.tools.slicer.SlicerDriver <options> <class names>",
				options, true);
			LOGGER.fatal("Incorrect command line", e);
			System.exit(1);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/10/14 05:35:41  venku
   - documentation.
   Revision 1.1  2003/10/14 05:33:26  venku
   - First cut at slicer driver.  This will be used to drive the testing
     of the slicer.
 */
