
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

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
abstract class AbstractSliceXMLizer
  extends AbstractProcessor {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractSliceXMLizer.class);

	/**
	 * This is the file/stream into which the xml output will be written into.
	 */
	protected final Writer writer;

	/**
	 * Creates a new AbstractSliceXMLizer object.
	 *
	 * @param file DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	protected AbstractSliceXMLizer(final String file) {
		try {
			writer = new FileWriter(new File(file));
		} catch (IOException e) {
			LOGGER.error("Exception while opening file to dump the slice in xml format.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers interests in all values, statements, and interfaces level entities.
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.registerForAllStmts(this);
		ppc.register(this);
		ppc.registerForAllValues(this);
	}

	/**
	 * Unregisters interests in all values, statements, and interfaces level entities.
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);
		ppc.unregister(this);
		ppc.unregisterForAllValues(this);
	}
}

/*
   ChangeLog:
   $Log$
 */
