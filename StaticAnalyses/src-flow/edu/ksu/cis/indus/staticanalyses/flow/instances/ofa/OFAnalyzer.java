
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

import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
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

import soot.Modifier;
import soot.SootField;


/**
 * This  class serves as the interface to the external world for Object flow analysis information.
 * 
 * <p>
 * The values returned on querying this analysis are AST chunks corresponding to object allocation/creation sites.
 * </p>
 * 
 * <p>
 * Created: Wed Jan 30 18:49:43 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class OFAnalyzer
  extends AbstractAnalyzer {
	/**
	 * Creates a new <code>OFAnalyzer</code> instance.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 * @param astim the prototype of the index manager to be used in conjunction with AST nodes.
	 * @param allocationim the prototype of the index manager to be used in conjunction with fields and arrays.
	 * @param lexpr the LHS expression visitor prototype.
	 * @param rexpr the RHS expression visitor prototype.
	 * @param stmt the statement visitor prototype.
	 * @param tokenMgr manages the tokens for the objects in OFA.
	 *
	 * @pre astim != null and allocationim != null and lexpr != null and rexpr != null and stmt != null and tokenMgr != null
	 */
	private OFAnalyzer(final String tagName, final AbstractIndexManager astim, final AbstractIndexManager allocationim,
		final AbstractExprSwitch lexpr, final AbstractExprSwitch rexpr, final AbstractStmtSwitch stmt,
		final ITokenManager tokenMgr) {
		super(new AllocationContext(), tagName, tokenMgr);

		final ModeFactory _mf = new ModeFactory();
		_mf.setASTIndexManagerPrototype(astim);
		_mf.setInstanceFieldIndexManagerPrototype(allocationim);
		_mf.setArrayIndexManagerPrototype(allocationim);
		_mf.setMethodIndexManagerPrototype(new IndexManager());
		_mf.setStaticFieldIndexManagerPrototype(new IndexManager());
		_mf.setNodePrototype(new OFAFGNode(null, tokenMgr));
		_mf.setStmtVisitorPrototype(stmt);
		_mf.setLHSExprVisitorPrototype(lexpr);
		_mf.setRHSExprVisitorPrototype(rexpr);
		setFactories(_mf, new MethodVariantFactory());
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFIOIAnalyzer(final String tagName, final ITokenManager tokenManager) {
		return new OFAnalyzer(tagName, new IndexManager(), new IndexManager(),
			new FlowInsensitiveExprSwitch(null, new LHSConnector()), new FlowInsensitiveExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager);
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFIOSAnalyzer(final String tagName, final ITokenManager tokenManager) {
		return new OFAnalyzer(tagName, new IndexManager(), new AllocationSiteSensitiveIndexManager(),
			new FlowInsensitiveExprSwitch(null, new LHSConnector()), new FlowInsensitiveExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager);
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFSOIAnalyzer(final String tagName, final ITokenManager tokenManager) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager(), new IndexManager(),
			new FlowSensitiveExprSwitch(null, new LHSConnector()), new FlowSensitiveExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager);
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 * @param tokenManager manages the tokens for the objects in OFA.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null and tokenMgr != null
	 */
	public static OFAnalyzer getFSOSAnalyzer(final String tagName, final ITokenManager tokenManager) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager(), new AllocationSiteSensitiveIndexManager(),
			new FlowSensitiveExprSwitch(null, new LHSConnector()), new FlowSensitiveExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.StmtSwitch(null), tokenManager);
	}

	/**
	 * Returns values associated with the given field associated with the given allocation sites.
	 *
	 * @param f the field reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 *
	 * @return a collection of values the field <code>f</code> may evaluate when associated with object created at allocation
	 * 		   sites given by <code>sites</code>.
	 *
	 * @pre f != null and sites != null
	 * @pre sites.oclIsKindOf(Collection(Object))
	 */
	public Collection getValues(final SootField f, final Collection sites) {
		Object _temp = null;
		Collection _retValues;
		final AllocationContext _ctxt = (AllocationContext) context;

		if (Modifier.isStatic(f.getModifiers())) {
			_retValues = getValues(f);
		} else {
			_retValues = new HashSet();
			_temp = _ctxt.getAllocationSite();

			for (final Iterator _i = sites.iterator(); _i.hasNext();) {
				_ctxt.setAllocationSite(_i.next());
				_retValues.addAll(getValues(f));
			}
			_ctxt.setAllocationSite(_temp);
		}
		return _retValues.isEmpty() ? Collections.EMPTY_SET
									: _retValues;
	}
}

// End of File
