
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

import edu.ksu.cis.indus.processing.ProcessingController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class provides basic infrastructure required to xmlize information in Indus.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractXMLizer
  implements IXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractXMLizer.class);

	/**
	 * This indicates if jimple should be dumped in XML form.
	 */
	protected boolean dumpXMLizedJimple;

	/**
	 * This is the id generator used during xmlization.
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * This is the directory in which the file containing the xmlized data will be placed.
	 */
	private String xmlOutDir;

	/**
	 * Set the xml id generator to be used in xml data generation.
	 *
	 * @param generator generates the id used in xml data.
	 *
	 * @pre generator != null
	 */
	public final void setGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * Retrieves the xml id generator used by this object.
	 *
	 * @return the xml id generator.
	 */
	public final IJimpleIDGenerator getIdGenerator() {
		return idGenerator;
	}

	/**
	 * Set the directory into which xml data should be dumped.
	 *
	 * @param xmlOutputDir is the directory into which xml data should be dumped.
	 *
	 * @throws IllegalArgumentException when the given directory does not exist or cannot be written into.
	 *
	 * @pre xmlOutputDir != null
	 */
	public final void setXmlOutputDir(final String xmlOutputDir) {
		final File _f = new File(xmlOutputDir);

		if (!_f.exists() | !_f.canWrite()) {
			LOGGER.error("XML output directory should exists with proper permissions.");
			throw new IllegalArgumentException("XML output directory should exists with proper permissions.");
		}
		xmlOutDir = xmlOutputDir;
	}

	/**
	 * Retrieves the directory into which xml data will be dumped into.
	 *
	 * @return the directory into which xml data will be dumped.
	 */
	public final String getXmlOutputDir() {
		return xmlOutDir;
	}

	/**
	 * Writes information in XML form.
	 *
	 * @param rootname is the name of the root method which may be used to create the file name ofthe file into which xml
	 * 		  form should be dumped.
	 * @param info is a map in which information required for xmlization will be provided.
	 *
	 * @pre rootname != null and info != null
	 */
	public abstract void writeXML(final String rootname, final Map info);

	/**
	 * Dumps the jimple into a file.  The name of the file is built from <code>rootname</code> and the parts of the jimple
	 * that will be dumped is controlled by <code>xmlcgipc</code>.
	 *
	 * @param rootname is the name of the root method which is used to create the file name of the file into which jimple
	 * 		  should be dumped.
	 * @param xmlcgipc is the processing controller to be used to control the dumping operation.  The user can use this
	 * 		  controller to control the methods and classes to be included in the dump.  This controller sholuld be able to
	 * 		  have deterministic behavior over a given set of class files.
	 *
	 * @pre rootname != null and xmlcgipc != null
	 */
	public final void dumpJimple(final String rootname, final ProcessingController xmlcgipc) {
		if (dumpXMLizedJimple) {
			final JimpleXMLizer _t = new JimpleXMLizer(idGenerator);
			Writer _writer;

			try {
				_writer =
					new FileWriter(new File(getXmlOutputDir() + File.separator
							+ rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + "jimple.xml"));
				_t.setWriter(_writer);
				_t.hookup(xmlcgipc);
				xmlcgipc.process();
				_t.unhook(xmlcgipc);
				_writer.flush();
				_writer.close();
			} catch (IOException _e) {
				LOGGER.error("Error while opening/writing/closing jimple xml file.  Aborting.", _e);
				System.exit(1);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/02/09 02:00:11  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.6  2003/12/28 00:41:48  venku
   - logging.
   Revision 1.5  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.4  2003/12/09 10:20:49  venku
   - formatting.
   Revision 1.3  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.2  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.1  2003/12/08 11:59:44  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizer inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.
 */
