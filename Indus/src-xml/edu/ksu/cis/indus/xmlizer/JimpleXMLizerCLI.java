
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

package edu.ksu.cis.indus.xmlizer;

import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.ProcessingController;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Scene;


/**
 * This utility class can be used to xmlize jimple.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class JimpleXMLizerCLI {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(JimpleXMLizerCLI.class);

	///CLOVER:OFF

	/**
	 * <i>This constructor cannot be used.</i>
	 */
	private JimpleXMLizerCLI() {
	}

	///CLOVER:ON

	/**
	 * The entry point to execute this xmlizer from command prompt.
	 *
	 * @param s is the command-line arguments.
	 *
	 * @pre s != null
	 */
	public static void main(final String[] s) {
		final Scene _scene = Scene.v();

		final Options _options = new Options();
		Option _o =
			new Option("d", "dump directory", true,
				"The directory in which to write the xml files.  "
				+ "If unspecified, the xml output will be directed standard out.");
		_o.setArgs(1);
		_o.setArgName("path");
		_o.setOptionalArg(false);
		_o.setRequired(true);
		_options.addOption(_o);
		_o = new Option("h", "help", false, "Display message.");
		_options.addOption(_o);

		final HelpFormatter _help = new HelpFormatter();

		try {
			final CommandLine _cl = (new BasicParser()).parse(_options, s);
			final String[] _args = _cl.getArgs();

			if (_cl.hasOption('h')) {
				_help.printHelp("java edu.ksu.cis.indus.JimpleXMLizer <options> <class names>", _options, true);
			} else {
				for (int _i = 0; _i < _args.length; _i++) {
					_scene.loadClassAndSupport(_args[_i]);
				}
				writeJimpleAsXML(_scene, _cl.getOptionValue('d'), null, new UniqueJimpleIDGenerator());
			}
		} catch (ParseException _e) {
			LOGGER.error("Error while parsing command line");
			_help.printHelp("java edu.ksu.cis.indus.JimpleXMLizer <options> <class names>", _options, true);
		}
	}

	/**
	 * Writes the jimple in the scene via the writer.
	 *
	 * @param scene in which the jimple to be dumped resides.
	 * @param directory with which jimple is dumped. If <code>null</code>, the output will be redirected to standarad output.
	 * @param suffix to be appended each file name.  If <code>null</code>, no suffix is appended.
	 * @param jimpleIDGenerator is the id generator to be used during xmlization.
	 *
	 * @pre scene != null and jimpleIDGenerator != null
	 */
	public static void writeJimpleAsXML(final Scene scene, final String directory, final String suffix,
		final IJimpleIDGenerator jimpleIDGenerator) {
		final JimpleXMLizer _xmlizer = new JimpleXMLizer(jimpleIDGenerator);
		final Environment _env = new Environment(scene);
		final ProcessingController _pc = new ProcessingController();
		_pc.setStmtGraphFactory(new ExceptionFlowSensitiveStmtGraphFactory(
				ExceptionFlowSensitiveStmtGraphFactory.SYNC_RELATED_EXCEPTIONS,
				true));
		_pc.setEnvironment(_env);
		_pc.setProcessingFilter(new XMLizingProcessingFilter());
		_xmlizer.setDumpOptions(directory, suffix);
		_xmlizer.hookup(_pc);
		_pc.process();
		_xmlizer.unhook(_pc);
	}
}

/*
   ChangeLog:
   $Log$
 */
