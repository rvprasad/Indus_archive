
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

import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.support.SootBasedDriver;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractXMLizer
  extends SootBasedDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractXMLizer.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected boolean dumpXMLizedJimple;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String xmlOutDir;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param generator DOCUMENT ME!
	 */
	public final void setGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param xmlOutputDir DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public final void setXMLOutputDir(final String xmlOutputDir) {
		if (xmlOutputDir != null) {
			File f = new File(xmlOutputDir);

			if (f == null || !f.exists() | !f.canWrite()) {
				throw new IllegalArgumentException("XML output directory should exists with proper permissions.");
			}
		}
		xmlOutDir = xmlOutputDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public final String getXmlOutDir() {
		return xmlOutDir;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected abstract void writeXML(final String rootname, final Map info);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param rootname DOCUMENT ME!
	 * @param xmlcgipc DOCUMENT ME!
	 */
	protected final void dumpJimple(final String rootname, final ProcessingController xmlcgipc) {
		if (dumpXMLizedJimple) {
			final JimpleXMLizer _t = new JimpleXMLizer(idGenerator);
			FileWriter _writer;

			try {
				_writer =
					new FileWriter(new File(getXmlOutDir() + File.separator
							+ rootname.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + "jimple.xml"));
				_t.setWriter(_writer);
				_t.hookup(xmlcgipc);
				xmlcgipc.process();
				_t.unhook(xmlcgipc);
				_writer.flush();
				_writer.close();
			} catch (IOException e) {
				LOGGER.error("Error while opening/writing/closing jimple xml file.  Aborting.", e);
				System.exit(1);
			}
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
	protected final void process(final ProcessingController pc, final Collection processors) {
		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IProcessor) i.next();

			processor.hookup(pc);
		}

		writeInfo("BEGIN: FA post processing");

		long start = System.currentTimeMillis();
		pc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("FA post processing", stop - start);
		writeInfo("END: FA post processing");

		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IProcessor) i.next();

			processor.unhook(pc);
		}
	}
    /**
     * DOCUMENT ME!
     * @return DOCUMENT ME!
     * 
     */
    public final IJimpleIDGenerator getIdGenerator() {
        return idGenerator;
    }
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/08 11:59:44  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizer inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.

 */
