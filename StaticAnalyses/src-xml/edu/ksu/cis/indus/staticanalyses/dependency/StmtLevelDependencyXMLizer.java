
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import java.io.IOException;
import java.io.Writer;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.znerd.xmlenc.XMLOutputter;

import soot.SootClass;
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
final class StmtLevelDependencyXMLizer
  extends AbstractProcessor {
    //  TODO: roll all xml emitting fragments to use xmlenc
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(StmtLevelDependencyXMLizer.class);

	/**
	 * This is the dependency analysis whose information should be xmlized.
	 */
	private DependencyAnalysis analysis;

	/**
	 * This is used to generate id's for xml elements.
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * This is the writer used to write the xml information.
	 */
	private XMLOutputter writer;

	/**
	 * This indicates if a class is being processed.
	 */
	private boolean processingClass;

	/**
	 * This indicates if a method is being processed.
	 */
	private boolean processingMethod;

	/**
	 * This counts the number of dependences discovered.
	 */
	private int totalDependences;

	/**
	 * Creates a new StmtLevelDependencyXMLizer object.
	 *
	 * @param out is the writer to be used to write xml data.
	 * @param generator to be used to generate id's.
	 * @param depAnalysis is the analysis whose information should be xmlized.
	 *
	 * @pre out != null and generator != null and depAnalysis != null
	 */
	public StmtLevelDependencyXMLizer(final XMLOutputter out, final IJimpleIDGenerator generator,
		final DependencyAnalysis depAnalysis) {
		writer = out;
		idGenerator = generator;
		analysis = depAnalysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();
		final Collection _dependents = analysis.getDependents(stmt, _method);
		final Collection _dependees = analysis.getDependees(stmt, _method);

		try {
			if (!(_dependents.isEmpty() && _dependees.isEmpty())) {
			    writer.startTag("dependency_info");
			    writer.attribute("stmtId", idGenerator.getIdForStmt(stmt, _method));
				totalDependences += _dependents.size();

				for (final Iterator _i = _dependents.iterator(); _i.hasNext();) {
					final Object _o = _i.next();
					String _tid = null;
					if (_o instanceof Pair) {
						final Pair _pair = (Pair) _o;
						_tid = idGenerator.getIdForStmt((Stmt) _pair.getFirst(), (SootMethod) _pair.getSecond());
					} else if (_o instanceof Stmt) {
					    _tid = idGenerator.getIdForStmt((Stmt) _o, _method);
					}
					if (_tid != null) {
					writer.startTag("dependent");
					writer.attribute("tid", _tid);
					writer.endTag();
					}
				}

				for (final Iterator _i = _dependees.iterator(); _i.hasNext();) {
					final Object _o = _i.next();
					String _eid = null;
					if (_o instanceof Pair) {
						final Pair _pair = (Pair) _o;
						_eid = idGenerator.getIdForStmt((Stmt) _pair.getFirst(), (SootMethod) _pair.getSecond());
					} else if (_o instanceof Stmt) {
					    _eid = idGenerator.getIdForStmt((Stmt) _o, _method);
					}
					if (_eid != null) {
						writer.startTag("dependent");
						writer.attribute("eid", _eid);
						writer.endTag();
						}
				}
				writer.endTag();
			}
		} catch (IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		try {
			if (processingMethod) {
				writer.endTag();
			}

			if (processingClass) {
				writer.endTag();
			} else {
				processingClass = true;
			}
			writer.startTag("class");
			writer.attribute("id", idGenerator.getIdForClass(clazz));
			processingMethod = false;
		} catch (IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		try {
			if (processingMethod) {
				writer.endTag();
			} else {
				processingMethod = true;
			}
			writer.startTag("method");
			writer.attribute("id", idGenerator.getIdForMethod(method));
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		try {
			if (processingMethod) {
				writer.endTag();
			}

			if (processingClass) {
				writer.endTag();
			}
			writer.startTag("count");
			writer.pcdata(String.valueOf(totalDependences));
			writer.endDocument();
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", _e);
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
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		try {
		    writer.startTag("dependency");
		    writer.attribute("id", String.valueOf(analysis.getId()));
		    writer.attribute("class", analysis.getClass().getName().toString());
		    writer.endTag();
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);
		ppc.unregister(this);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/04/25 23:18:18  venku
   - coding conventions.

   Revision 1.7  2004/04/25 21:18:37  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.

   Revision 1.6  2004/03/29 09:33:37  venku
   - using a "id" attrirbute can mess things up during xml comparison.
     Hence, use it only if it is approriate. FIXED.
   Revision 1.5  2004/02/25 23:34:29  venku
   - classes that should not be visible should be invisible :-)
   Revision 1.4  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.3  2004/02/09 17:40:53  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
   Revision 1.2  2004/02/09 06:49:02  venku
   - deleted dependency xmlization and test classes.
   Revision 1.1  2004/02/08 03:05:46  venku
   - renamed xmlizer packages to be in par with the packages
     that contain the classes whose data is being xmlized.
   Revision 1.12  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.11  2003/12/16 06:13:02  venku
   - incorrect attribute emitted for class id. FIXED.
   Revision 1.10  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.9  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.8  2003/12/08 12:15:56  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
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
   - Switched the contents of DependencyXMLizerDriver and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.2  2003/11/10 20:05:02  venku
   - formatting.
   Revision 1.1  2003/11/10 08:26:09  venku
   - enabled XMLization of statement level dependency information.
 */
