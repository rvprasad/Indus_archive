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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.IExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;
import edu.ksu.cis.indus.staticanalyses.flow.IIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationSiteSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow.FlowSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.ArrayType;
import soot.Modifier;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

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
 * @param <T> is the type of the token set object.
 */
public final class OFAnalyzer<T extends ITokens<T, Value>>
		extends
		AbstractAnalyzer<Value, T, OFAFGNode<T>, IExprSwitch<OFAFGNode<T>>, IExprSwitch<OFAFGNode<T>>, IStmtSwitch, Type> {

	/**
	 * Creates a new <code>OFAnalyzer</code> instance.
	 * 
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
	private OFAnalyzer(final String tagName, final IPrototype<? extends IIndexManager<? extends IIndex<?>, Value>> astim,
			final IIndexManager<? extends IIndex<?>, ArrayType> arrayIM,
			final IIndexManager<? extends IIndex<?>, SootField> instancefieldIM, final IExprSwitch<OFAFGNode<T>> lexpr,
			final IExprSwitch<OFAFGNode<T>> rexpr, final IStmtSwitch stmt, final ITokenManager<T, Value, Type> tokenMgr,
			final IStmtGraphFactory<?> stmtGrphFctry) {
		super(new AllocationContext(), tagName, tokenMgr);

		fa.setupInstanceFieldVariantManager(instancefieldIM);
		fa.setupArrayVariantManager(arrayIM);
		fa.setupMethodVariantManager(new IndexManager<SootMethod>(), astim, new MethodVariantFactory<T>(Constants
				.getFAScopePattern(), stmtGrphFctry));
		fa.setupStaticFieldVariantManager(new IndexManager<SootField>());

		fa.setNodePrototype(new OFAFGNode<T>(null, tokenMgr));
		fa.setLhsVisitorPrototype(lexpr);
		fa.setRhsVisitorPrototype(rexpr);
		fa.setStmtVisitorPrototype(stmt);
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.
	 * 
	 * @param <T> is the type of the token set object.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 * @param stmtGrphFctry provides the CFGs.
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static <T extends ITokens<T, Value>> OFAnalyzer<T> getFIOIAnalyzer(final String tagName,
			final ITokenManager<T, Value, Type> tokenManager, final IStmtGraphFactory<?> stmtGrphFctry) {
		final Value2ValueMapper _type2valueMapper = new Value2ValueMapper();
		return new OFAnalyzer<T>(tagName, new IndexManager<Value>(), new IndexManager<ArrayType>(),
				new IndexManager<SootField>(), new FlowInsensitiveExprSwitch<T>(new LHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new FlowInsensitiveExprSwitch<T>(new RHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new StmtSwitch<T>(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.
	 * 
	 * @param <T> is the type of the token set object.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 * @param stmtGrphFctry provides the CFGs.
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static <T extends ITokens<T, Value>> OFAnalyzer<T> getFIOSAnalyzer(final String tagName,
			final ITokenManager<T, Value, Type> tokenManager, final IStmtGraphFactory<?> stmtGrphFctry) {
		final Value2ValueMapper _type2valueMapper = new Value2ValueMapper();
		return new OFAnalyzer<T>(tagName, new IndexManager<Value>(), new AllocationSiteSensitiveIndexManager<ArrayType>(),
				new AllocationSiteSensitiveIndexManager<SootField>(), new FlowInsensitiveExprSwitch<T>(
						new LHSConnector<OFAFGNode<T>>(), _type2valueMapper, null), new FlowInsensitiveExprSwitch<T>(
						new RHSConnector<OFAFGNode<T>>(), _type2valueMapper, null), new StmtSwitch<T>(null), tokenManager,
				stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.
	 * 
	 * @param <T> is the type of the token set object.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA. *
	 * @param stmtGrphFctry provides the CFGs.
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static <T extends ITokens<T, Value>> OFAnalyzer<T> getFSOIAnalyzer(final String tagName,
			final ITokenManager<T, Value, Type> tokenManager, final IStmtGraphFactory<?> stmtGrphFctry) {
		final Value2ValueMapper _type2valueMapper = new Value2ValueMapper();
		return new OFAnalyzer<T>(tagName, new FlowSensitiveIndexManager<Value>(), new IndexManager<ArrayType>(),
				new IndexManager<SootField>(), new FlowSensitiveExprSwitch<T>(new LHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new FlowSensitiveExprSwitch<T>(new RHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new StmtSwitch<T>(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that is flow sensitive, allocation-site insensitive, and object-transparent (rapid-type) in
	 * nature. Due to its object-transparent nature, every variable will point to utmost one object of a type. Also, if two
	 * variables points to an object of a type then they will point to the same object.
	 * 
	 * @param <T> is the type of the token set object.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA. *
	 * @param stmtGrphFctry provides the CFGs.
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static <T extends ITokens<T, Value>> OFAnalyzer<T> getFSOIRTAnalyzer(final String tagName,
			final ITokenManager<T, Value, Type> tokenManager, final IStmtGraphFactory<?> stmtGrphFctry) {
		final Value2ValueMapper _type2valueMapper = new Value2CanonicalValueMapper();
		return new OFAnalyzer<T>(tagName, new FlowSensitiveIndexManager<Value>(), new IndexManager<ArrayType>(),
				new IndexManager<SootField>(), new FlowSensitiveExprSwitch<T>(new LHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new FlowSensitiveExprSwitch<T>(new RHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new StmtSwitch<T>(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that is flow insensitive, allocation-site insensitive, and object-transparent (rapid-type) in
	 * nature. Due to its object-transparent nature, every variable will point to utmost one object of a type. Also, if two
	 * variables points to an object of a type then they will point to the same object.
	 * 
	 * @param <T> is the type of the token set object.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA. *
	 * @param stmtGrphFctry provides the CFGs.
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static <T extends ITokens<T, Value>> OFAnalyzer<T> getFIOIRTAnalyzer(final String tagName,
			final ITokenManager<T, Value, Type> tokenManager, final IStmtGraphFactory<?> stmtGrphFctry) {
		final Value2ValueMapper _type2valueMapper = new Value2CanonicalValueMapper();
		return new OFAnalyzer<T>(tagName, new FlowSensitiveIndexManager<Value>(), new IndexManager<ArrayType>(),
				new IndexManager<SootField>(), new FlowInsensitiveExprSwitch<T>(new LHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new FlowInsensitiveExprSwitch<T>(new RHSConnector<OFAFGNode<T>>(),
						_type2valueMapper, null), new StmtSwitch<T>(null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.
	 * 
	 * @param <T> is the type of the token set object.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this analysis
	 *            instance to tag parts of the AST. Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more detail.
	 * @param tokenManager manages the tokens for the objects in OFA. *
	 * @param stmtGrphFctry provides the CFGs.
	 * @return the instance of analyzer correponding to the given name.
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static <T extends ITokens<T, Value>> OFAnalyzer<T> getFSOSAnalyzer(final String tagName,
			final ITokenManager<T, Value, Type> tokenManager, final IStmtGraphFactory<?> stmtGrphFctry) {
		final Value2ValueMapper _type2valueMapper = new Value2ValueMapper();
		return new OFAnalyzer<T>(tagName, new FlowSensitiveIndexManager<Value>(),
				new AllocationSiteSensitiveIndexManager<ArrayType>(), new AllocationSiteSensitiveIndexManager<SootField>(),
				new FlowSensitiveExprSwitch<T>(new LHSConnector<OFAFGNode<T>>(), _type2valueMapper, null),
				new FlowSensitiveExprSwitch<T>(new RHSConnector<OFAFGNode<T>>(), _type2valueMapper, null), new StmtSwitch<T>(
						null), tokenManager, stmtGrphFctry);
	}

	/**
	 * Returns values associated with the given array type associated with the given allocation sites.
	 * 
	 * @param t the array type reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 * @return a collection of values the array type <code>t</code> may evaluate when associated with object created at
	 *         allocation sites given by <code>sites</code>.
	 * @pre t != null and sites != null
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
