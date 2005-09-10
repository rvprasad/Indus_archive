
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
final class StmtAndMethodBasedDependencyXMLizer
  extends AbstractProcessor {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StmtAndMethodBasedDependencyXMLizer.class);

	/** 
	 * This is the dependency analysis whose information should be xmlized.
	 */
	private IDependencyAnalysis analysis;

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
	 * Creates a new StmtAndMethodBasedDependencyXMLizer object.
	 *
	 * @param out is the writer to be used to write xml data.
	 * @param generator to be used to generate id's.
	 * @param depAnalysis is the analysis whose information should be xmlized.
	 *
	 * @pre out != null and generator != null and depAnalysis != null
	 */
	public StmtAndMethodBasedDependencyXMLizer(final XMLOutputter out, final IJimpleIDGenerator generator,
		final IDependencyAnalysis depAnalysis) {
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
						writer.startTag("dependee");
						writer.attribute("eid", _eid);
						writer.endTag();
					}
				}
				writer.endTag();
			}
		} catch (final IOException _e) {
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
		} catch (final IOException _e) {
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
			writer.declaration();
			writer.startTag("dependency");
			final List _t = new ArrayList(analysis.getIds());
			Collections.sort(_t);
			writer.attribute("id", _t.toString());
			writer.attribute("class", analysis.getClass().getName());
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

// End of File
