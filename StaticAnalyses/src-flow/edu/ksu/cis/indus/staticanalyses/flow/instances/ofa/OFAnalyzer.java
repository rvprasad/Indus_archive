/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;
import edu.ksu.cis.indus.staticanalyses.flow.IIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.ModeFactory;
import edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationSiteSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow.FlowSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.ArrayType;
import soot.Modifier;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.toolkits.graph.CompleteUnitGraph;

/**
 * This class serves as the interface to the external world for Object flow analysis information.
 * <p>
 * The values returned on querying this analysis are AST chunks corresponding to object allocation/creation sites.
 * </p>
 * <p>
 * Created: Wed Jan 30 18:49:43 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class OFAnalyzer
		extends AbstractAnalyzer<Value> {

	/**
	 * Creates a new <code>OFAnalyzer</code> instance.
	 *
	 * @param <LE> DOCUMENT ME!
	 * @param <RE> DOCUMENT ME!
	 * @param <SS> DOCUMENT ME!
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param astim the prototype of the index manager to be used in conjunction with AST nodes.
	 * @param arrayIM the prototype of the index manager to be used in conjunction with arrays.
	 * @param instancefieldIM the prototype of the index manager to be used in conjunction with fields.
	 * @param lexpr the LHS expression visitor prototype.
	 * @param rexpr the RHS expression visitor prototype.
	 * @param stmt the statement visitor prototype.
	 * @param tokenMgr manages the tokens for the objects in OFA.
	 * @param stmtGrphFctry the statement graph factory to use.
	 * @pre astim != null and allocationim != null and lexpr != null and rexpr != null and stmt != null and tokenMgr != null
	 */
	private OFAnalyzer(
			final String tagName,
			final IPrototype<IIndexManager<? extends IIndex, Value>> astim,
			final IIndexManager<?, ArrayType> arrayIM,
			final IIndexManager<?, SootField> instancefieldIM,
			final IExprSwitch<OFAFGNode> lexpr,
			final IExprSwitch<OFAFGNode> rexpr,
			final IStmtSwitch stmt,
			final ITokenManager<?, Value> tokenMgr,
			final IStmtGraphFactory<?> stmtGrphFctry) {
		super(new AllocationContext(), tagName, tokenMgr);

		final ModeFactory<IIndexManager<? extends IIndex, ArrayType>,
		IIndexManager<? extends IIndex, Value>,
		IIndexManager<? extends IIndex, SootField>,
		IExprSwitch< OFAFGNode>,
		IIndexManager<? extends IIndex, SootMethod>,
		OFAFGNode, IExprSwitch< OFAFGNode>, IStmtSwitch,
		IIndexManager<? extends IIndex, SootField>> _mf = new ModeFactory<IIndexManager<? extends IIndex, ArrayType>,
		IIndexManager<? extends IIndex, Value>, IIndexManager<? extends IIndex, SootField>,
		IExprSwitch< OFAFGNode>, IIndexManager<? extends IIndex, SootMethod>,
		OFAFGNode, IExprSwitch< OFAFGNode>, IStmtSwitch, IIndexManager<? extends IIndex, SootField>>();
		_mf.setASTIndexManagerPrototype(astim);
		_mf.setInstanceFieldIndexManager(instancefieldIM);
		_mf.setArrayIndexManager(arrayIM);
		_mf.setMethodIndexManager(new IndexManager<SootMethod>());
		_mf.setStaticFieldIndexManager(new IndexManager<SootField>());
		_mf.setNodePrototype(new OFAFGNode(null, tokenMgr));
		_mf.setStmtVisitorPrototype(stmt);
		_mf.setLHSExprVisitorPrototype(lexpr);
		_mf.setRHSExprVisitorPrototype(rexpr);
		setFactories(_mf, new MethodVariantFactory(Constants.getFAScopePattern(), stmtGrphFctry));
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 * @param stmtGrphFctry DOCUMENT ME!
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFIOIAnalyzer(final String tagName, final ITokenManager<?, Value> tokenManager,
			final IStmtGraphFactory<?> stmtGrphFctry) {
		return new OFAnalyzer(tagName, new IndexManager<Value>(), new IndexManager<ArrayType>(),
				new IndexManager<SootField>(), new FlowInsensitiveExprSwitch(null, new LHSConnector()),
				new FlowInsensitiveExprSwitch(null, new RHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 * @param stmtGrphFctry DOCUMENT ME!
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFIOSAnalyzer(final String tagName, final ITokenManager<?, Value> tokenManager,
			final IStmtGraphFactory<?> stmtGrphFctry) {
		return new OFAnalyzer(tagName, new IndexManager<Value>(),
				new AllocationSiteSensitiveIndexManager<ArrayType>(), new AllocationSiteSensitiveIndexManager<SootField>(),
				new FlowInsensitiveExprSwitch(null, new LHSConnector()), new FlowInsensitiveExprSwitch(null,
						new RHSConnector()), new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null),
				tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA. *
	 * @param stmtGrphFctry DOCUMENT ME!
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFSOIAnalyzer(final String tagName, final ITokenManager<?, Value> tokenManager,
			final IStmtGraphFactory<?> stmtGrphFctry) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager<Value>(), new IndexManager<ArrayType>(),
				new IndexManager<SootField>(), new FlowSensitiveExprSwitch(null, new LHSConnector()),
				new FlowSensitiveExprSwitch(null, new RHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA. *
	 * @param stmtGrphFctry DOCUMENT ME!
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFSOSAnalyzer(final String tagName, final ITokenManager<?, Value> tokenManager,
			final IStmtGraphFactory<?> stmtGrphFctry) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager<Value>(),
				new AllocationSiteSensitiveIndexManager<ArrayType>(), new AllocationSiteSensitiveIndexManager<SootField>(),
				new FlowSensitiveExprSwitch(null, new LHSConnector()), new FlowSensitiveExprSwitch(null, new RHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns values associated with the given array type associated with the given allocation sites.
	 *
	 * @param t the array type reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 * @return a collection of values the array type <code>t</code> may evaluate when associated with object created at
	 *         allocation sites given by <code>sites</code>.
	 * @pre t != null and sites != null
	 * @pre sites.oclIsKindOf(Collection(Object))
	 */
	public Collection<Value> getValues(final ArrayType t, final Collection<Value> sites) {
		Object _temp = null;
		Collection<Value> _retValues;
		final AllocationContext _ctxt = (AllocationContext) context;

		_retValues = new HashSet<Value>();
		_temp = _ctxt.getAllocationSite();

		for (final Iterator<Value> _i = sites.iterator(); _i.hasNext();) {
			_ctxt.setAllocationSite(_i.next());
			_retValues.addAll(getValues(t));
		}
		_ctxt.setAllocationSite(_temp);

		return _retValues.isEmpty() ? Collections.<Value> emptySet() : _retValues;
	}

	/**
	 * Returns values associated with the given field associated with the given allocation sites.
	 *
	 * @param f the field reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 * @return a collection of values the field <code>f</code> may evaluate when associated with object created at
	 *         allocation sites given by <code>sites</code>.
	 * @pre f != null and sites != null
	 * @pre sites.oclIsKindOf(Collection(Object))
	 */
	public Collection<Value> getValues(final SootField f, final Collection<Value> sites) {
		Object _temp = null;
		Collection<Value> _retValues;
		final AllocationContext _ctxt = (AllocationContext) context;

		if (Modifier.isStatic(f.getModifiers())) {
			_retValues = getValues(f);
		} else {
			_retValues = new HashSet<Value>();
			_temp = _ctxt.getAllocationSite();

			for (final Iterator<Value> _i = sites.iterator(); _i.hasNext();) {
				_ctxt.setAllocationSite(_i.next());
				_retValues.addAll(getValues(f));
			}
			_ctxt.setAllocationSite(_temp);
		}
		return _retValues.isEmpty() ? Collections.<Value> emptySet() : _retValues;
	}
}

// End of File
