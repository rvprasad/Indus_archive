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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.annotations.AEmpty;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;

/**
 * This class represents the central access point for the information calculated in a value flow analysis. The subclass should
 * extend this class with methods to access various information about the implmented analysis. This class by itself provides
 * the interface to query generic, low-level analysis information. These interfaces should be used by implemented components
 * of the framework to extract information during the analysis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <V> DOCUMENT ME!
 */
public abstract class AbstractAnalyzer<V>
		extends AbstractStatus
		implements IValueAnalyzer<V> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalyzer.class);

	/**
	 * The context to be used when analysis information is requested and a context is not provided.
	 */
	protected Context context;

	/**
	 * The instance of the framework performing the analysis and is being represented by this analyzer object.
	 * 
	 * @invariant fa != null
	 */
	protected FA<?, Value, ?, ?, ?, ?, ?, ?, ?, ?> fa;

	/**
	 * Creates a new <code>AbstractAnalyzer</code> instance.
	 * 
	 * @param theContext the context to be used by this analysis instance.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenMgr manages the tokens that participate in the analysis.
	 * @param stmtGrphFctry the statement graph factory to use.
	 * @pre theContext != null and tagName != null and tokenMgr != null and stmtGrphFctry
	 */
	protected AbstractAnalyzer(final Context theContext, final String tagName, final ITokenManager<?, V> tokenMgr, 
			final IStmtGraphFactory<?> stmtGrphFctry) {
		this.context = theContext;
		fa = new FA(this, tagName, tokenMgr, stmtGrphFctry);
	}

	/**
	 * Analyzes the given set of classes repeatedly by considering the given set of methods as the starting point. The
	 * collected information is the union of the information calculated by considering the same set of classes but starting
	 * from each of the given methods.
	 * 
	 * @param env is the environment of classes to be analyzed.
	 * @param roots a collection of <code>SootMethod</code>s representing the various possible starting points for the
	 *            analysis.
	 * @throws IllegalStateException wen roots is <code>null</code> or roots is empty.
	 * @pre env != null and roots != null and not roots.isEmpty()
	 */
	public final void analyze(final IEnvironment env, final Collection<SootMethod> roots) {
		if (roots == null || roots.isEmpty()) {
			throw new IllegalStateException("There must be at least one root method to analyze.");
		}

		unstable();

		for (final Iterator<SootMethod> _i = roots.iterator(); _i.hasNext();) {
			final SootMethod _root = _i.next();
			fa.analyze(env, _root);
		}
		stable();
	}

	/**
	 * Analyzes the given set of classes starting from the given method.
	 * 
	 * @param env is the environment of classes to be analyzed.
	 * @param root the analysis is started from this method.
	 * @throws IllegalStateException when root == <code>null</code>
	 * @pre env != null root != null
	 */
	public final void analyze(final IEnvironment env, final SootMethod root) {
		if (root == null) {
			throw new IllegalStateException("Root method cannot be null.");
		}
		unstable();
		fa.analyze(env, root);
		stable();
	}

	/**
	 * Returns the active part of this object.
	 * 
	 * @return the active part.
	 */
	public IActivePart getActivePart() {
		return fa.getActivePart();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IAnalyzer#getContext()
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Returns the environment in which the analysis occurred.
	 * 
	 * @return the environment in which the analysis occurred.
	 * @post result != null
	 */
	public IEnvironment getEnvironment() {
		return fa;
	}

	/**
	 * @see IValueAnalyzer#getThrownValues(InvokeExpr, edu.ksu.cis.indus.processing.Context)
	 */
	public final Collection<V> getThrownValues(final InvokeExpr e, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<V> _temp = Collections.emptySet();

		if (_mv != null) {
			final InvocationVariant _iv = (InvocationVariant) _mv.getASTVariant(e, context);

			if (_iv != null) {
				_temp = _iv.getThrowNode().getValues();
			}
		}
		context = _tmpCtxt;
		return _temp;
	}

	/**
	 * @see IValueAnalyzer#getThrownValues(soot.SootMethod, edu.ksu.cis.indus.processing.Context)
	 */
	public Collection<V> getThrownValues(final SootMethod method, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant _mv = fa.queryMethodVariant(method);
		Collection<V> _temp = Collections.emptySet();

		if (_mv != null) {
			final IFGNode _tv = _mv.queryThrownNode();

			if (_tv != null) {
				_temp = _tv.getValues();
			}
		}
		context = _tmpCtxt;
		return _temp;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer#getValues(soot.Value,
	 *      edu.ksu.cis.indus.processing.Context)
	 */
	public final Collection<V> getValues(final Value astChunk, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		Collection<V> _result = Collections.emptySet();

		if (astChunk instanceof ParameterRef) {
			_result = getValues((ParameterRef) astChunk);
		} else {
			_result = getValues(astChunk);
		}
		context = _tmpCtxt;
		return _result;
	}

	/**
	 * @see IValueAnalyzer#getValuesForParameter(int, edu.ksu.cis.indus.processing.Context)
	 */
	public final Collection<V> getValuesForParameter(final int paramIndex, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<V> _temp = Collections.emptySet();

		if (_mv != null) {
			final IFGNode _queryParameterNode = _mv.queryParameterNode(paramIndex);

			if (_queryParameterNode != null) {
				_temp = _queryParameterNode.getValues();
			}
		}
		context = _tmpCtxt;
		return _temp;
	}

	/**
	 * @see IValueAnalyzer#getValuesForThis(edu.ksu.cis.indus.processing.Context)
	 */
	public final Collection<V> getValuesForThis(final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<V> _temp = Collections.emptySet();

		if (_mv != null) {
			_temp = _mv.queryThisNode().getValues();
		}
		context = _tmpCtxt;
		return _temp;
	}

	/**
	 * Reset the analyzer so that fresh run of the analysis can occur. This is intended to be called by the environment to
	 * reset the analysis.
	 */
	public final void reset() {
		unstable();
		resetAnalysis();
		fa.reset();
	}

	/**
	 * Returns the set of values associated with the given array type in the context given by <code>this.context</code>.
	 * 
	 * @param a the array type for which the values are requested.
	 * @return the collection of values associated with <code>a</code> in <code>this.context</code>.
	 * @pre a != null
	 * @post result != null
	 */
	protected final Collection<V> getValues(final ArrayType a) {
		final ValuedVariant<?> _v = fa.queryArrayVariant(a);
		Collection<V> _temp = Collections.emptySet();

		if (_v != null) {
			final IFGNode _n = _v.getFGNode();
			if (_n != null) {
				_temp = _n.getValues();
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Values for array type " + a + " in node " + _n + " are " + _temp);
			}
		}
		return _temp;
	}

	/**
	 * Returns the set of values associated with the given parameter reference in the context given by
	 * <code>this.context</code>.
	 * 
	 * @param p the parameter reference for which the values are requested.
	 * @return the collection of values associated with <code>p</code> in <code>this.context</code>.
	 * @pre p != null
	 * @post result != null
	 */
	protected final Collection<V> getValues(final ParameterRef p) {
		final IMethodVariant _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<V> _temp = Collections.EMPTY_SET;

		if (_mv != null) {
			final int _index = p.getIndex();
			final IFGNode _queryParameterNode = _mv.queryParameterNode(_index);
			if (_queryParameterNode != null) {
				_temp = _queryParameterNode.getValues();
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Values for param " + p + " in node " + _queryParameterNode + " are " + _temp);
			}
		}
		return _temp;
	}

	/**
	 * Returns the set of values associated with the given field in the context given by <code>this.context</code>.
	 * 
	 * @param sf the field for which the values are requested.
	 * @return the collection of values associated with <code>sf</code> in <code>this.context</code>.
	 * @pre sf != null
	 * @post result != null
	 */
	protected final Collection<V> getValues(final SootField sf) {
		final ValuedVariant _fv = fa.queryFieldVariant(sf);
		Collection<V> _temp = Collections.emptySet();

		if (_fv != null) {
			_temp = _fv.getFGNode().getValues();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Values for field  " + sf + " are " + _temp);
			}
		}
		return _temp;
	}

	/**
	 * Returns the set of values associated with the given AST node in the context given by <code>this.context</code>.
	 * 
	 * @param v the AST node for which the values are requested.
	 * @return the collection of values associted with <code>v</code> in <code>this.context</code>.
	 * @pre v != null
	 * @post result != null
	 */
	protected final Collection<V> getValues(final Value v) {
		final IMethodVariant _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<V> _temp = Collections.emptySet();

		if (_mv != null) {
			final ValuedVariant _astv = _mv.queryASTVariant(v, context);

			if (_astv != null) {
				_temp = _astv.getFGNode().getValues();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Values for ast " + v + " in node " + _astv.getFGNode() + " are " + _temp);
				}
			}
		}
		return _temp;
	}

	/**
	 * Reset the analyzer so that a fresh run of the analysis can occur. This is intended to be overridden by the subclasses
	 * to reset analysis specific data structures. It shall be called before the framework data structures are reset.
	 */
	@AEmpty protected final void resetAnalysis() {
		// does nothing
	}

	/**
	 * Sets the factory on the underlaying framework instance. Refer to <code>ModeFactory</code> and
	 * <code>IMethodVariantFactory</code> for more details.
	 * 
	 * @param mf is the mode factory that provides objects that dictate the mode of the analysis.
	 * @param mvf is the factory object the provides method variants.
	 * @pre mf != null and mvf != null
	 */
	protected void setFactories(final ModeFactory mf, final IMethodVariantFactory mvf) {
		fa.setFactories(mf, mvf);
	}
}

// End of File
