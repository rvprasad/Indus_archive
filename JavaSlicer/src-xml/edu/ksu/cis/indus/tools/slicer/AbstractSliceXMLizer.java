
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

import java.io.Writer;


/**
 * This provides basic infrastructure to xmlize a slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
abstract class AbstractSliceXMLizer
  extends AbstractProcessor {
	/**
	 * This is the file/stream into which the xml output will be written into.
	 */
	protected final Writer writer;

	/**
	 * Creates a new AbstractSliceXMLizer object.
	 *
	 * @param out is the writer with which the xml information should be written.
	 */
	protected AbstractSliceXMLizer(final Writer out) {
		writer = out;
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
   Revision 1.1  2003/11/17 01:39:42  venku
   - added slice XMLization support.
 */
