
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

import org.eclipse.swt.widgets.Shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static String configuration =
		"<slicerConfiguration " + "xmlns:slicer=\"http://indus.projects.cis.ksu.edu/slicer\""
		+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
		+ "xsi:schemaLocation=\"http://indus.projects.cis.ksu.edu/slicer slicerConfig.xsd\" activeConfiguration=\"base\">"
		+ "<configurationInfo slicetype=\"backward slice\" analysis=\"base\" name=\"first\" sliceForDeadlock=\"true\">"
		+ "<divergence active=\"true\" interprocedural=\"true\"/>" + "<interference equivalenceClassBased=\"true\"/>"
		+ "<ready active=\"true\" rule1=\"true\" rule2=\"true\" rule3=\"true\" rule4=\"true\" equivalenceClassBased=\"true\"/>"
		+ "</configurationInfo>" + "</slicerConfiguration>";

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static File outputDirectory;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static String configFileName;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String[] args) {
		// parse command line arguments
		parseCommandLine(args);

		// create the slicer tool
		SlicerTool slicer = new SlicerTool();
		slicer.destringizeConfiguration(configuration);

		// call the configurator on the slicer
		slicer.getConfigurator().display(new Shell());

		// save the configuration
		try {
			BufferedWriter configFile = new BufferedWriter(new FileWriter(configFileName));
			configFile.write(slicer.stringizeConfiguration());
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Could not write the configuration file.", e);
			}
			System.out.println(slicer.stringizeConfiguration());
		}

		// execute the slicer
		slicer.run(Phase.STARTING_PHASE);

		// serialize the output of the slicer
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param args
	 */
	private static void parseCommandLine(String[] args) {
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
 */
