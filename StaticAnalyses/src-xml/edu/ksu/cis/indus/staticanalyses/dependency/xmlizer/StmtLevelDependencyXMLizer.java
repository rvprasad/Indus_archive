
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

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.support.Pair;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import java.io.IOException;
import java.io.Writer;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This xmlizes dependency info for dependencies at statement level.  The dependency is expressed as dependency between a
 * pair of statement and method and statements or pairs of statement and method as in Control, Divergence, Ready, and
 * Synchronization dependence are examples.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class StmtLevelDependencyXMLizer
  extends AbstractDependencyXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(StmtLevelDependencyXMLizer.class);

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private int totalDependences;

	/**
	 * @see AbstractDependencyXMLizer#AbstractDependencyXMLizer(Writer, IJimpleIDGenerator, DependencyAnalysis)
	 */
	public StmtLevelDependencyXMLizer(final Writer out, final IJimpleIDGenerator generator,
		final DependencyAnalysis depAnalysis) {
		super(out, generator, depAnalysis);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		SootMethod method = context.getCurrentMethod();
		Collection dependencies = analysis.getDependents(stmt, method);

		try {
			if (!dependencies.isEmpty()) {
				writer.write("\t\t\t<dependency_info dependeeId=\"" + idGenerator.getIdForStmt(stmt, method) + "\">\n");
				totalDependences += dependencies.size();

				for (Iterator i = dependencies.iterator(); i.hasNext();) {
					Object o = i.next();

					if (o instanceof Pair) {
						Pair pair = (Pair) o;
						writer.write("\t\t\t\t<dependent id=\""
							+ idGenerator.getIdForStmt((Stmt) pair.getFirst(), (SootMethod) pair.getSecond()) + "\"/>\n");
					} else if (o instanceof Stmt) {
						writer.write("\t\t\t\t<dependent id=\"" + idGenerator.getIdForStmt((Stmt) o, method) + "\"/>\n");
					}
				}
				writer.write("\t\t\t</dependency_info>\n");
			}
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.registerForAllStmts(this);
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);
		ppc.unregister(this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.xmlizer.AbstractDependencyXMLizer#getTotalNumberOfDependences()
	 */
	protected int getTotalNumberOfDependences() {
		return totalDependences;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/12/08 10:57:59  venku
   - outputs count and id of dependences.

   Revision 1.6  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/11/17 17:17:57  venku
   - dumb iteration error. FIXED.
   Revision 1.4  2003/11/17 15:42:46  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.3  2003/11/17 01:35:54  venku
   - renamed out to writer in AbstractDependencyXMLizer
   - added methods to spit out root element tags.
   Revision 1.2  2003/11/12 10:45:36  venku
   - soot class path can be set in SootBasedDriver.
   - dependency tests are xmlunit based.
   Revision 1.1  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.3  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.2  2003/11/10 20:05:02  venku
   - formatting.
   Revision 1.1  2003/11/10 08:26:09  venku
   - enabled XMLization of statement level dependency information.
 */
