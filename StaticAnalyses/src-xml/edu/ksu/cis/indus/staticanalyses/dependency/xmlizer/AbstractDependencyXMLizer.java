
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

package edu.ksu.cis.indus.staticanalyses.dependency.xmlizer;

import soot.SootClass;
import soot.SootMethod;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Writer;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractDependencyXMLizer
  extends AbstractProcessor {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractDependencyXMLizer.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected DependencyAnalysis analysis;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected IJimpleIDGenerator idGenerator;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected Writer writer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected boolean processingClass = false;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected boolean processingMethod = false;

	/**
	 * Creates a new AbstractDependencyXMLizer object.
	 *
	 * @param out DOCUMENT ME!
	 * @param generator DOCUMENT ME!
	 * @param depAnalysis DOCUMENT ME!
	 */
	public AbstractDependencyXMLizer(final Writer out, final IJimpleIDGenerator generator,
		final DependencyAnalysis depAnalysis) {
		writer = out;
		idGenerator = generator;
		analysis = depAnalysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		try {
			if (processingMethod) {
				writer.write("\t\t</method>\n");
			}

			if (processingClass) {
				writer.write("\t</class>\n");
			} else {
				processingClass = true;
			}
			writer.write("\t<class signature=\"" + clazz.getName() + "\" id=\"" + idGenerator.getIdForClass(clazz) + "\">\n");
			processingMethod = false;
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		try {
			if (processingMethod) {
				writer.write("\t\t</method>\n");
			} else {
				processingMethod = true;
			}
			writer.write("\t\t<method signature=\"" + method.getSubSignature().replaceAll("\\<", "&lt;") + "\" id=\""
				+ idGenerator.getIdForMethod(method) + "\">\n");
			idGenerator.resetStmtCounter();
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		try {
			if (processingMethod) {
				writer.write("\t\t</method>\n");
			}

			if (processingClass) {
				writer.write("\t</class>\n");
			}
			writer.write("</dependency>\n");
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		try {
			writer.write("<dependency>\n");
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", e);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/11/17 01:35:54  venku
   - renamed out to writer in AbstractDependencyXMLizer
   - added methods to spit out root element tags.
   Revision 1.2  2003/11/12 10:45:36  venku
   - soot class path can be set in SootBasedDriver.
   - dependency tests are xmlunit based.
   Revision 1.1  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.2  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.1  2003/11/10 08:26:09  venku
   - enabled XMLization of statement level dependency information.
 */
