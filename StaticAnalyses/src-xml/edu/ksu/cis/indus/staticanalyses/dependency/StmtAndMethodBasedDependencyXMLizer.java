/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.Stmt;

/**
 * This xmlizes dependency info for dependencies at statement level. The dependency is expressed as dependency between a pair
 * of statement and method and statements or pairs of statement and method as in Control, Divergence, Ready, and
 * Synchronization dependence are examples.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T1> is the type of the dependent entity.
 * @param <E2> is the type of the dependee entity.
 */
final class StmtAndMethodBasedDependencyXMLizer<T1 extends Stmt, E2 extends Stmt>
		extends AbstractProcessor {

	/**
	 * A hack class to handle types of dependee and dependent triggers.  This would be unnecessary if Java generics retained
	 * type parameters to be inspected at runtime.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	final class Handler {

		/**
		 * Checks if the given statement can pass as a dependee object.
		 * 
		 * @param stmt of interest.
		 * @return <code>true</code> if the given statement can pass as a dependee object.
		 */
		boolean canHandleDependee(final Stmt stmt) {
			final boolean _result;
			if (analysis instanceof IdentifierBasedDataDA) {
				_result = stmt instanceof DefinitionStmt;
			} else if (analysis instanceof ReferenceBasedDataDA || analysis instanceof InterferenceDAv1) {
				_result = stmt instanceof AssignStmt;
			} else if (analysis instanceof SynchronizationDA) {
				_result = stmt instanceof MonitorStmt;
			} else {
				_result = true;
			}
			return _result;
		}

		/**
		 * Checks if the given statement can pass as a dependent object.
		 * 
		 * @param stmt of interest.
		 * @return <code>true</code> if the given statement can pass as a dependent object.
		 */
		boolean canHandleDependent(final Stmt stmt) {
			final boolean _result;
			if (analysis instanceof ReferenceBasedDataDA || analysis instanceof InterferenceDAv1) {
				_result = stmt instanceof AssignStmt;
			} else {
				_result = true;
			}
			return _result;
		}

		/**
		 * Casts the given object into a form amenable as dependee trigger type.
		 * 
		 * @param stmt to be cast
		 * @return the cast object.
		 */
		E2 castToCompatibleDependee(final Stmt stmt) {
			return ((Class<E2>) StmtAndMethodBasedDependencyXMLizer.this.getClass().getTypeParameters()[1].getBounds()[0])
					.cast(stmt);
		}

		/**
		 * Casts the given object into a form amenable as dependent trigger type.
		 * 
		 * @param stmt to be cast
		 * @return the cast object.
		 */
		T1 castToCompatibleDependent(final Stmt stmt) {
			return ((Class<T1>) StmtAndMethodBasedDependencyXMLizer.this.getClass().getTypeParameters()[0].getBounds()[0])
					.cast(stmt);
		}

	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StmtAndMethodBasedDependencyXMLizer.class);

	/**
	 * This is the dependency analysis whose information should be xmlized.
	 */
	IDependencyAnalysis<T1, SootMethod, ?, E2, SootMethod, ?> analysis;

	/**
	 * DOCUMENT ME!
	 */
	private Handler handler;

	/**
	 * This is used to generate id's for xml elements.
	 */
	private IJimpleIDGenerator idGenerator;

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
	 * This is the writer used to write the xml information.
	 */
	private XMLOutputter writer;

	/**
	 * Creates a new StmtAndMethodBasedDependencyXMLizer object.
	 * 
	 * @param out is the writer to be used to write xml data.
	 * @param generator to be used to generate id's.
	 * @param depAnalysis is the analysis whose information should be xmlized.
	 * @pre out != null and generator != null and depAnalysis != null
	 */
	public StmtAndMethodBasedDependencyXMLizer(final XMLOutputter out, final IJimpleIDGenerator generator,
			final IDependencyAnalysis<T1, SootMethod, ?, E2, SootMethod, ?> depAnalysis) {
		writer = out;
		idGenerator = generator;
		analysis = depAnalysis;
		handler = new Handler();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	@Override public void callback(final SootClass clazz) {
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
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	@Override public void callback(final SootMethod method) {
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
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();
		try {
			final Collection<?> _dependents;

			if (handler.canHandleDependee(stmt)) {
				_dependents = analysis.getDependents(handler.castToCompatibleDependee(stmt), _method);
			} else {
				_dependents = Collections.emptySet();
			}

			final Collection<?> _dependees;
			if (handler.canHandleDependent(stmt)) {
				if (analysis instanceof IdentifierBasedDataDA) {
					_dependees = ((IdentifierBasedDataDA) analysis).getDependees(stmt, _method);
				} else {
					_dependees = analysis.getDependees(handler.castToCompatibleDependent(stmt), _method);
				}
			} else {
				_dependees = Collections.emptySet();
			}

			if (!(_dependents.isEmpty() && _dependees.isEmpty())) {
				writer.startTag("dependency_info");
				writer.attribute("stmtId", idGenerator.getIdForStmt(stmt, _method));
				totalDependences += _dependents.size();

				for (final Iterator<?> _i = _dependents.iterator(); _i.hasNext();) {
					final Object _o = _i.next();
					String _tid = null;

					if (_o instanceof Pair) {
						final Object _first = ((Pair) _o).getFirst();
						if (_first instanceof Stmt) {
							final Pair<Stmt, SootMethod> _pair = (Pair) _o;
							_tid = idGenerator.getIdForStmt(_pair.getFirst(), _pair.getSecond());
						} else {
							final Pair<?, Stmt> _pair = (Pair) _o;
							_tid = idGenerator.getIdForStmt(_pair.getSecond(), _method);
						}
					} else if (_o instanceof Stmt) {
						_tid = idGenerator.getIdForStmt((Stmt) _o, _method);
					}

					if (_tid != null) {
						writer.startTag("dependent");
						writer.attribute("tid", _tid);
						writer.endTag();
					}
				}

				for (final Iterator<?> _i = _dependees.iterator(); _i.hasNext();) {
					final Object _o = _i.next();
					String _eid = null;

					if (_o instanceof Pair) {
						final Pair<Stmt, SootMethod> _pair = (Pair) _o;
						_eid = idGenerator.getIdForStmt(_pair.getFirst(), _pair.getSecond());
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
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("No informtion: " + stmt + " @ " + _method + " -- Dependents = " + _dependents.isEmpty() + " "
						+ "Dependees = " + _dependees.isEmpty() + " " + analysis.getIds());
			}
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing dependency info.", _e);
			}
		} catch (final ClassCastException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error retrieving dependency info for " + stmt + " in the context " + context, _e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	@Override public void consolidate() {
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
	@Override public void processingBegins() {
		try {
			writer.declaration();
			writer.startTag("dependency");
			final List<IDependencyAnalysis.DependenceSort> _t = new ArrayList<IDependencyAnalysis.DependenceSort>(analysis
					.getIds());
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
