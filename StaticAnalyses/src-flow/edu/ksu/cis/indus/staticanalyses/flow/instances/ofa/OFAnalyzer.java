
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.ClassManager;
import edu.ksu.cis.indus.staticanalyses.flow.ModeFactory;
import edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationSiteSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow.FlowSensitiveIndexManager;

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
	 *
	 * @pre astim != null and allocationim != null and lexpr != null and rexpr != null and stmt != null
	 */
	private OFAnalyzer(final String tagName, final AbstractIndexManager astim, final AbstractIndexManager allocationim,
		final AbstractExprSwitch lexpr, final AbstractExprSwitch rexpr, final AbstractStmtSwitch stmt) {
		super(new AllocationContext(), tagName);

		final ModeFactory _mf = new ModeFactory();
		_mf.setASTIndexManagerPrototype(astim);
		_mf.setInstanceFieldIndexManagerPrototype(allocationim);
		_mf.setArrayIndexManagerPrototype(allocationim);
		_mf.setMethodIndexManagerPrototype(new IndexManager());
		_mf.setStaticFieldIndexManagerPrototype(new IndexManager());
		_mf.setNodePrototype(new OFAFGNode(null));
		_mf.setStmtVisitorPrototype(stmt);
		_mf.setLHSExprVisitorPrototype(lexpr);
		_mf.setRHSExprVisitorPrototype(rexpr);
		_mf.setClassManagerPrototype(new ClassManager(null));
		setModeFactory(_mf);
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null
	 */
	public static OFAnalyzer getFIOIAnalyzer(final String tagName) {
		final AbstractIndexManager _temp = new IndexManager();

		return new OFAnalyzer(tagName, _temp, _temp,
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null
	 */
	public static OFAnalyzer getFIOSAnalyzer(final String tagName) {
		return new OFAnalyzer(tagName, new IndexManager(), new AllocationSiteSensitiveIndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null
	 */
	public static OFAnalyzer getFSOIAnalyzer(final String tagName) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager(), new IndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null
	 */
	public static OFAnalyzer getFSOSAnalyzer(final String tagName) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager(), new AllocationSiteSensitiveIndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes when the IR has the
	 * following properties.
	 * 
	 * <ul>
	 * <li>
	 * local splitting transformation has been done.
	 * </li>
	 * <li>
	 * locals are defined only once.
	 * </li>
	 * </ul>
	 * 
	 * <p>
	 * These properties are ensured if the IR is created with options available from
	 * <code>edu.ksu.cis.indus.common.soot.Util.getSootOptions()</code>.
	 * </p>
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null
	 */
	public static OFAnalyzer getFSv2OIAnalyzer(final String tagName) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager(), new IndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitchv2(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitchv2(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes  when the IR has the
	 * following properties.
	 * 
	 * <ul>
	 * <li>
	 * local splitting transformation has been done.
	 * </li>
	 * <li>
	 * locals are defined only once.
	 * </li>
	 * </ul>
	 * 
	 * <p>
	 * These properties are ensured if the IR is created with options available from
	 * <code>edu.ksu.cis.indus.common.soot.Util.getSootOptions()</code>.
	 * </p>
	 *
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null and tagName != null
	 */
	public static OFAnalyzer getFSv2OSAnalyzer(final String tagName) {
		return new OFAnalyzer(tagName, new FlowSensitiveIndexManager(), new AllocationSiteSensitiveIndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitchv2(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitchv2(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
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

/*
   ChangeLog:
   $Log$
   Revision 1.11  2004/02/26 08:31:21  venku
   - refactoring - moved OFAnalyzer.isReferenceType() to Util.
   Revision 1.10  2003/12/13 19:38:58  venku
   - removed unnecessary imports.
   Revision 1.9  2003/12/05 00:53:09  venku
   - removed unused method and restricted access to certain methods.
   Revision 1.8  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/11/30 01:07:57  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/26 17:53:56  venku
   Actually we can use the types to cut down the number of edges
   between the flow nodes. The current fix uses a method in OFAnalyzer
   to check for reference types, only if the type matches the given expression
   is processed.  However, this does not apply for staticfield, instancefield, and
   array access expressions.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.2  2003/08/09 21:52:57  venku
   Change parameter names.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
