
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.PoolAwareWorkBag;
import edu.ksu.cis.indus.common.datastructures.WorkList;
import edu.ksu.cis.indus.common.soot.NamedTag;

import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.optimizations.SCCBasedOptimizer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IAnalyzer;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import soot.tagkit.Tag;


/**
 * The instance of the framework which controls and manages the analysis on execution.  It acts the central repository for
 * information pertaining to various components of the framework when the analysis is in progress.  It also serves as the
 * central repository for various instances of the framework at a given time.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class FA
  implements IEnvironment,
	  IWorkBagProvider {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FA.class);

	/** 
	 * This is the collection of methods that serve as entry points into the system being analyzed.
	 *
	 * @invariant rootMethods != null
	 */
	protected final Collection rootMethods = new HashSet();

	/** 
	 * The object used to realize the "active" part of this object.
	 */
	private final IActivePart.ActivePart activePart = new IActivePart.ActivePart();

	/** 
	 * The analyzer associated with this instance of the framework.
	 *
	 * @invariant analyzer != null
	 */
	private final IAnalyzer analyzer;

	/** 
	 * The token manager that manages the tokens whose flow is instrumented by the flow analysis.
	 *
	 * @invariant tokenManager != null
	 */
	private final ITokenManager tokenManager;

	/** 
	 * The collection of workbags used in tandem during analysis.
	 */
	private final IWorkBag[] workBags;

	/** 
	 * The manager of class related primitive information and processing.
	 */
	private ClassManager classManager;

	/** 
	 * The environment which provides the set of class to be analyzed.
	 */
	private IEnvironment environment;

	/** 
	 * The current work bag among the collection of work bags being used.
	 */
	private IWorkBag currWorkBag;

	/** 
	 * The manager of method variants.
	 */
	private MethodVariantManager methodVariantManager;

	/** 
	 * The factory that provides the components during the analysis performed by this instance of the framework.
	 */
	private ModeFactory modeFactory;

	/** 
	 * The tag used to identify the elements of the AST touched by this framework instance.
	 */
	private NamedTag tag;

	/** 
	 * This optimizes flow graph based on SCC.
	 */
	private final SCCBasedOptimizer sccBasedOptimizer = new SCCBasedOptimizer();

	/** 
	 * The manager of array variants.
	 */
	private ValuedVariantManager arrayVariantManager;

	/** 
	 * The manager of instance field variants.
	 */
	private ValuedVariantManager instanceFieldVariantManager;

	/** 
	 * The manager of static field variants.
	 */
	private ValuedVariantManager staticFieldVariantManager;

	/** 
	 * This is the interval between which SCC-based optimization is applied.
	 */
	private int sccOptimizationInterval;

	/**
	 * Creates a new <code>FA</code> instance.
	 *
	 * @param theAnalyzer to be associated with this instance of the framework.
	 * @param tagName is the name of the tag that will be tacked onto parts of the AST processed by this framework instance.
	 * 		  The guarantee is that all elements so tagged were processed by the framework instance.  The inverse need not
	 * 		  be true.
	 * @param tokenMgr manages the tokens whose flow is instrumented by this instance of flow analysis.
	 *
	 * @pre analyzer != null and tagName != null and tokenMgr != null
	 */
	FA(final IAnalyzer theAnalyzer, final String tagName, final ITokenManager tokenMgr) {
		workBags = new IWorkBag[2];
		workBags[0] = new PoolAwareWorkBag(new LIFOWorkBag());
		workBags[1] = new PoolAwareWorkBag(new LIFOWorkBag());
		currWorkBag = workBags[0];
		analyzer = theAnalyzer;
		tag = new NamedTag(tagName);
		tokenManager = tokenMgr;
		sccOptimizationInterval = Constants.getSCCOptimizationIntervalForFA();
	}

	/**
	 * Retrieves the analyzer in whose context this flow analysis instance is functioning.
	 *
	 * @return the associated analysis.
	 *
	 * @post result != null
	 */
	public final IAnalyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.  If none
	 * exists, a new variant is created.
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 *
	 * @pre a != null
	 * @post result != null
	 */
	public final ValuedVariant getArrayVariant(final ArrayType a) {
		return getArrayVariant(a, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given array type in the given context.  If none exists, a new variant is
	 * created.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>.
	 *
	 * @pre a != null and context != null
	 * @post result != null
	 */
	public final ValuedVariant getArrayVariant(final ArrayType a, final Context context) {
		processType(a.baseType);
		return (ValuedVariant) arrayVariantManager.select(a, context);
	}

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 *
	 * @pre className != null
	 */
	public final SootClass getClass(final String className) {
		return environment.getClass(className);
	}

	/**
	 * Sets the value of <code>sccOptimizationInterval</code>.
	 *
	 * @param interval the new value of <code>sccOptimizationInterval</code>. Zero and negative values will turn off the
	 * 		  optimization.
	 */
	public final void setSccOptimizationInterval(final int interval) {
		this.sccOptimizationInterval = interval;
	}

	/**
	 * Retrieves the value in <code>sccOptimizationInterval</code>.
	 *
	 * @return the value in <code>sccOptimizationInterval</code>.
	 */
	public final int getSccOptimizationInterval() {
		return sccOptimizationInterval;
	}

	/**
	 * Returns the classes of the system that were analyzed/accessed by this analysis.
	 *
	 * @return a collection of classes.
	 *
	 * @post result != null and result->forall(o | o.oclIsTypeOf(soot.SootClass))
	 */
	public Collection getClasses() {
		return Collections.unmodifiableCollection(classManager.classes);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.  If none exists,
	 * a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.
	 *
	 * @pre sf != null
	 * @post result != null
	 */
	public final ValuedVariant getFieldVariant(final SootField sf) {
		return getFieldVariant(sf, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given field in the given context.  If none exists, a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant associated with the given field in the given context.
	 *
	 * @pre sf != null and context != null
	 * @post result != null
	 */
	public final ValuedVariant getFieldVariant(final SootField sf, final Context context) {
		IVariant _temp = null;
		processClass(sf.getDeclaringClass());
		processType(sf.getType());
		sf.addTag(getTag());

		if (Modifier.isStatic(sf.getModifiers())) {
			_temp = staticFieldVariantManager.select(sf, context);
		} else {
			_temp = instanceFieldVariantManager.select(sf, context);
		}
		return (ValuedVariant) _temp;
	}

	/**
	 * Returns an LHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new LHS expression visitor.
	 *
	 * @pre e != null
	 */
	public final IExprSwitch getLHSExpr(final IStmtSwitch e) {
		return modeFactory.getLHSExprVisitor(e);
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.  If none
	 * exists, a new variant is created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>.
	 *
	 * @pre sm != null
	 * @post result != null
	 */
	public final IMethodVariant getMethodVariant(final SootMethod sm) {
		return getMethodVariant(sm, analyzer.getContext());
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.  If none exists, a new variant is
	 * created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 *
	 * @return the variant corresonding to <code>sm</code> in the given context.
	 *
	 * @pre sm != null and context != null
	 * @post result != null
	 */
	public final IMethodVariant getMethodVariant(final SootMethod sm, final Context context) {
		return (IMethodVariant) methodVariantManager.select(sm, context);
	}

	/**
	 * Returns the flow graph node as created by the factory.
	 *
	 * @return a new flow graph node.
	 */
	public final IFGNode getNewFGNode() {
		return modeFactory.getFGNode(this);
	}

	/**
	 * Returns a RHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new RHS expression visitor.
	 *
	 * @pre e != null
	 */
	public final IExprSwitch getRHSExpr(final IStmtSwitch e) {
		return modeFactory.getRHSExprVisitor(e);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getRoots()
	 */
	public Collection getRoots() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Returns the associated environment.
	 *
	 * @return the associated environment.
	 *
	 * @post result != null
	 */
	public final IEnvironment getScene() {
		return environment;
	}

	/**
	 * Returns a statement visitor as created by the factory.
	 *
	 * @param e the method variant which parameterizes the statement visitor.
	 *
	 * @return a new statement visitor.
	 *
	 * @pre e != null
	 */
	public final IStmtSwitch getStmt(final IMethodVariant e) {
		return modeFactory.getStmtVisitor(e);
	}

	/**
	 * Retrieves the token manager that manages the tokens whose flow is being instrumented.
	 *
	 * @return the token manager.
	 *
	 * @post tokenManager != null
	 */
	public final ITokenManager getTokenManager() {
		return tokenManager;
	}

	/**
	 * @see IWorkBagProvider#getWorkBag()
	 */
	public final IWorkBag getWorkBag() {
		return currWorkBag;
	}

	/**
	 * Returns the active part of this object.
	 *
	 * @return the active part.
	 */
	public IActivePart getActivePart() {
		return activePart;
	}

	/**
	 * Performs type-based processing of the given class.
	 *
	 * @param clazz is the class to be processed.
	 *
	 * @pre clazz != null
	 */
	public void processClass(final SootClass clazz) {
		classManager.process(clazz);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#removeClass(soot.SootClass)
	 */
	public void removeClass(final SootClass clazz) {
		environment.removeClass(clazz);
	}

	/**
	 * Resets the framework.  The framework forgets all information allowing for a new session of analysis to executed.
	 */
	public void reset() {
		arrayVariantManager.reset();
		instanceFieldVariantManager.reset();
		methodVariantManager.reset();
		staticFieldVariantManager.reset();
		workBags[0].clear();
		workBags[1].clear();
		rootMethods.clear();
		classManager.reset();
		sccBasedOptimizer.reset();
		environment = null;
		activePart.reset();
		currWorkBag = workBags[0];
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 * 		   <code>null</code> if none exist.
	 *
	 * @pre a != null
	 * @post result != null
	 */
	final ValuedVariant queryArrayVariant(final ArrayType a) {
		return queryArrayVariant(a, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given array type in the given context.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>. <code>null</code> if none exist.
	 *
	 * @pre a != null and context != null
	 * @post result != null
	 */
	final ValuedVariant queryArrayVariant(final ArrayType a, final Context context) {
		return (ValuedVariant) arrayVariantManager.query(a, context);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.<code>null</code>
	 * 		   if none exists.
	 *
	 * @pre sf != null
	 * @post result != null
	 */
	final ValuedVariant queryFieldVariant(final SootField sf) {
		return queryFieldVariant(sf, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given field in the given context.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant associated with the given field in the given context.  <code>null</code> if none exists.
	 *
	 * @pre sf != null and context != null
	 * @post result != null
	 */
	final ValuedVariant queryFieldVariant(final SootField sf, final Context context) {
		IVariant _temp = null;

		if (Modifier.isStatic(sf.getModifiers())) {
			_temp = staticFieldVariantManager.query(sf, context);
		} else {
			_temp = instanceFieldVariantManager.query(sf, context);
		}
		return (ValuedVariant) _temp;
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>. <code>null</code> if none exist.
	 *
	 * @pre sm != null
	 * @post result != null
	 */
	final IMethodVariant queryMethodVariant(final SootMethod sm) {
		return queryMethodVariant(sm, analyzer.getContext());
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 *
	 * @return the variant corresonding to <code>sm</code> in the given context.  <code>null</code> if none exist.
	 *
	 * @pre sm != null and context != null
	 * @post result != null
	 */
	final IMethodVariant queryMethodVariant(final SootMethod sm, final Context context) {
		return (IMethodVariant) methodVariantManager.query(sm, context);
	}

	/**
	 * Sets the factories to be used in this instance of the framework.
	 *
	 * @param mf is the factory object that provides the objects that dictate the mode of analysis.
	 * @param mvf is the factory object the provides method variants.
	 *
	 * @pre mf != null and mvf != null
	 */
	void setFactories(final ModeFactory mf, final IMethodVariantFactory mvf) {
		modeFactory = mf;
		classManager = new ClassManager(this);
		arrayVariantManager = new ValuedVariantManager(this, mf.getArrayIndexManager());
		instanceFieldVariantManager = new ValuedVariantManager(this, mf.getInstanceFieldIndexManager());
		methodVariantManager = new MethodVariantManager(this, mf.getMethodIndexManager(), mf.getASTIndexManager(), mvf);
		staticFieldVariantManager = new ValuedVariantManager(this, mf.getStaticFieldIndexManager());
	}

	/**
	 * Retrieves the tag associated with the framework instance.
	 *
	 * @return the tag associated with the framework instance.
	 *
	 * @post result != null
	 */
	Tag getTag() {
		return tag;
	}

	/**
	 * Analyzes the given classes starting with <code>root</code> method.
	 *
	 * @param env which contains the classes to be analyzed.
	 * @param root the method to start the analysis from.
	 *
	 * @pre environment != null and root != null
	 */
	void analyze(final IEnvironment env, final SootMethod root) {
		environment = env;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting system processing...");
		}
		getMethodVariant(root);
		rootMethods.add(root);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting worklist processing...");
		}

		long _count = 0;
		int _bagToggleCounter = 0;
		final WorkList[] _workLists = new WorkList[2];
		_workLists[0] = new WorkList(workBags[0]);
		_workLists[1] = new WorkList(workBags[1]);

		while ((workBags[0].hasWork() || workBags[1].hasWork()) && activePart.canProceed()) {
			final int _bagToProcess = _bagToggleCounter % 2;
			_bagToggleCounter++;

			final int _bagToCollect = _bagToggleCounter % 2;

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing work pieces in workbag " + _bagToProcess);
			}
			currWorkBag = workBags[_bagToCollect];
			_count += _workLists[_bagToProcess].process();

			if (sccOptimizationInterval > 0 && (++_count > sccOptimizationInterval)) {
				collapseSCCOfNodes();
				_count = 0;
			}
		}
	}

	/**
	 * Performs type-based processing.
	 *
	 * @param type to be processed.
	 *
	 * @pre type != null
	 */
	void processType(final Type type) {
		if (type instanceof RefType) {
			classManager.process(getClass(((RefType) type).getClassName()));
		} else if (type instanceof ArrayType && ((ArrayType) type).baseType instanceof RefType) {
			classManager.process(getClass(((RefType) ((ArrayType) type).baseType).getClassName()));
		}
	}

	/**
	 * Retrieves the variants at the method interfaces.
	 *
	 * @return the variants
	 *
	 * @post result != null and result.oclIsKindOf(Collection(IVariant))
	 */
	private Collection getVariantsAtMethodInterfaces() {
		final Collection _result = new HashSet();
		final Iterator _i = methodVariantManager.getVariants().iterator();
		final int _iEnd = methodVariantManager.getVariants().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMethodVariant _mv = (IMethodVariant) _i.next();
			_result.add(_mv.queryReturnNode());
			_result.add(_mv.queryThisNode());

			for (int _j = _mv.getMethod().getParameterCount() - 1; _j >= 0; _j--) {
				_result.add(_mv.queryParameterNode(_j));
			}
		}
		_result.remove(null);
		return _result;
	}

	/**
	 * Collapses SCC of nodes.
	 */
	private void collapseSCCOfNodes() {
		final Collection _rootNodes = new HashSet();
		final Transformer _transformer = TransformerUtils.invokerTransformer("getFGNode");
		CollectionUtils.collect(instanceFieldVariantManager.getVariants(), _transformer, _rootNodes);
		CollectionUtils.collect(staticFieldVariantManager.getVariants(), _transformer, _rootNodes);
		_rootNodes.addAll(getVariantsAtMethodInterfaces());
		sccBasedOptimizer.optimize(_rootNodes, tokenManager);
	}
}

// End of File
