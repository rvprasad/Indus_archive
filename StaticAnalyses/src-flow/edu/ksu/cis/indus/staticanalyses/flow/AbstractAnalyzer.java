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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

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
 * @param <SYM> is the type of symbol whose flow is being analyzed. 
 * @param <T>  is the type of the token set object.
 * @param <LE> is the type of the lhs expression visitor.
 * @param <N> is the type of the summary node in the flow analysis.
 * @param <SS> is the type of the statement visitor.
 * @param <RE> is the type of the rhs expression visitor.
 * @param <R> is the type of the symbol types.
 */
public abstract class AbstractAnalyzer<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>, LE extends IExprSwitch<N>, RE extends IExprSwitch<N>, SS extends IStmtSwitch, R>
		extends AbstractStatus
		implements IValueAnalyzer<SYM> {

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
	protected FA<SYM, T, N, R> fa;

	/**
	 * Creates a new <code>AbstractAnalyzer</code> instance.
	 *
	 * @param theContext the context to be used by this analysis instance.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenMgr manages the tokens that participate in the analysis.
	 * @pre theContext != null and tagName != null and tokenMgr != null
	 */
	protected AbstractAnalyzer(final Context theContext, final String tagName, final ITokenManager<T, SYM, R> tokenMgr) {
		this.context = theContext;
		fa = new FA<SYM, T, N, R>(this, tagName, tokenMgr);
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer#getActivePart()
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
	public final Collection<SYM> getThrownValues(final InvokeExpr e, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant<N> _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<SYM> _temp = Collections.emptySet();

		if (_mv != null) {
			final InvocationVariant<N> _iv = (InvocationVariant) _mv.getASTVariant(e, context);

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
	public Collection<SYM> getThrownValues(final SootMethod method, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant<N> _mv = fa.queryMethodVariant(method);
		Collection<SYM> _temp = Collections.emptySet();

		if (_mv != null) {
			final N _tv = _mv.queryThrownNode();

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
	public final Collection<SYM> getValues(final Value astChunk, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		Collection<SYM> _result = Collections.emptySet();

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
	public final Collection<SYM> getValuesForParameter(final int paramIndex, final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant<N> _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<SYM> _temp = Collections.emptySet();

		if (_mv != null) {
			final N _queryParameterNode = _mv.queryParameterNode(paramIndex);

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
	public final Collection<SYM> getValuesForThis(final Context ctxt) {
		final Context _tmpCtxt = context;
		context = ctxt;

		final IMethodVariant<N> _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<SYM> _temp = Collections.emptySet();

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
	protected final Collection<SYM> getValues(final ArrayType a) {
		final ValuedVariant<N> _v = fa.queryArrayVariant(a);
		Collection<SYM> _temp = Collections.emptySet();

		if (_v != null) {
			final N _n = _v.getFGNode();
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
	protected final Collection<SYM> getValues(final ParameterRef p) {
		final IMethodVariant<N> _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<SYM> _temp = Collections.<SYM>emptySet();

		if (_mv != null) {
			final int _index = p.getIndex();
			final N _queryParameterNode = _mv.queryParameterNode(_index);
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
	protected final Collection<SYM> getValues(final SootField sf) {
		final ValuedVariant<N> _fv = fa.queryFieldVariant(sf);
		Collection<SYM> _temp = Collections.emptySet();

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
	protected final Collection<SYM> getValues(final Value v) {
		final IMethodVariant<N> _mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection<SYM> _temp = Collections.emptySet();

		if (_mv != null) {
			final ValuedVariant<N> _astv = _mv.queryASTVariant(v, context);

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
	@Empty protected final void resetAnalysis() {
		// does nothing
	}
}

// End of File
