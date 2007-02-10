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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.ITransformer;
import edu.ksu.cis.indus.common.collections.InvokeTransformer;
import edu.ksu.cis.indus.common.datastructures.IWork;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.PoolAwareWorkBag;
import edu.ksu.cis.indus.common.datastructures.WorkList;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.optimizations.SCCBasedOptimizer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IAnalyzer;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.tagkit.Tag;

/**
 * The instance of the framework which controls and manages the analysis on execution. It acts the central repository for
 * information pertaining to various components of the framework when the analysis is in progress. It also serves as the
 * central repository for various instances of the framework at a given time.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <SYM> is the type of symbol whose flow is being analyzed.
 * @param <T>  is the type of the token set object.
 * @param <N> is the type of the summary node in the flow analysis.
 * @param <R> is the type of the symbol types.
 */
public class FA<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>, R>
		implements IEnvironment, IWorkBagProvider {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FA.class);

	/**
	 * This is the collection of methods that serve as entry points into the system being analyzed.
	 *
	 * @invariant rootMethods != null
	 */
	protected final Collection<SootMethod> rootMethods = new HashSet<SootMethod>();

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
	 * The manager of array variants.
	 */
	private ValuedVariantManager<ArrayType, SYM, T, N, R> arrayVariantManager;

	/**
	 * The manager of class related primitive information and processing.
	 */
	private final ClassManager classManager;

	/**
	 * The current work bag among the collection of work bags being used.
	 */
	private IWorkBag<IWork> currWorkBag;

	/**
	 * The environment which provides the set of class to be analyzed.
	 */
	private IEnvironment environment;

	/**
	 * The manager of instance field variants.
	 */
	private ValuedVariantManager<SootField, SYM, T, N, R> instanceFieldVariantManager;

	/**
	 * The prototype for lhs expression visitor.
	 */
	private IPrototype<? extends IExprSwitch<N>> lhsVisitorPrototype;

	/**
	 * The manager of method variants.
	 */
	private MethodVariantManager<SYM, T, N, R> methodVariantManager;

	/**
	 * The prototype for flow graph node.
	 */
	private IPrototype<N> nodePrototype;

	/**
	 * The prototype for lhs expression visitor.
	 */
	private IPrototype<? extends IExprSwitch<N>> rhsVisitorPrototype;

	/**
	 * This optimizes flow graph based on SCC.
	 */
	private final SCCBasedOptimizer<SYM, T, N> sccBasedOptimizer = new SCCBasedOptimizer<SYM, T, N>();

	/**
	 * This is the interval between which SCC-based optimization is applied.
	 */
	private int sccOptimizationInterval;

	/**
	 * The manager of static field variants.
	 */
	private ValuedVariantManager<SootField, SYM, T, N, R> staticFieldVariantManager;

	/**
	 * The prototype for statement visitor.
	 */
	private IPrototype<? extends IStmtSwitch> stmtVisitorPrototype;

	/**
	 * The tag used to identify the elements of the AST touched by this framework instance.
	 */
	private NamedTag tag;

	/**
	 * The token manager that manages the tokens whose flow is instrumented by the flow analysis.
	 *
	 * @invariant tokenManager != null
	 */
	private final ITokenManager<T, SYM, R> tokenManager;

	/**
	 * The collection of workbags used in tandem during analysis.
	 */
	private final IWorkBag<IWork>[] workBags;

	/**
	 * Creates a new <code>FA</code> instance.
	 *
	 * @param theAnalyzer to be associated with this instance of the framework.
	 * @param tagName is the name of the tag that will be tacked onto parts of the AST processed by this framework instance.
	 *            The guarantee is that all elements so tagged were processed by the framework instance. The inverse need not
	 *            be true.
	 * @param tokenMgr manages the tokens whose flow is instrumented by this instance of flow analysis.
	 * @pre analyzer != null and tagName != null and tokenMgr != null and stmtGraphFactory != null
	 */
	FA(final IAnalyzer theAnalyzer, final String tagName, final ITokenManager<T, SYM, R> tokenMgr) {
		workBags = new IWorkBag[2];
		workBags[0] = new PoolAwareWorkBag<IWork>(new LIFOWorkBag<IWork>());
		workBags[1] = new PoolAwareWorkBag<IWork>(new LIFOWorkBag<IWork>());
		currWorkBag = workBags[0];
		analyzer = theAnalyzer;
		tag = new NamedTag(tagName);
		tokenManager = tokenMgr;
		sccOptimizationInterval = Constants.getSCCOptimizationIntervalForFA();
		classManager = new ClassManager(this);
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
	 * Retrieves the analyzer in whose context this flow analysis instance is functioning.
	 *
	 * @return the associated analysis.
	 * @post result != null
	 */
	public final IAnalyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>. If none
	 * exists, a new variant is created.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 * @pre a != null
	 * @post result != null
	 */
	public final ValuedVariant<N> getArrayVariant(final ArrayType a) {
		return getArrayVariant(a, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given array type in the given context. If none exists, a new variant is
	 * created.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>.
	 * @pre a != null and context != null
	 * @post result != null
	 */
	public final ValuedVariant<N> getArrayVariant(final ArrayType a, final Context context) {
		processType(a.baseType);
		return arrayVariantManager.select(a, context);
	}

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 * @return the requested class.
	 * @pre className != null
	 */
	public final SootClass getClass(final String className) {
		return environment.getClass(className);
	}

	/**
	 * Returns the classes of the system that were analyzed/accessed by this analysis.
	 *
	 * @return a collection of classes.
	 * @post result != null
	 */
	public Collection<SootClass> getClasses() {
		return Collections.unmodifiableCollection(classManager.classes);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>. If none
	 * exists, a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.
	 * @pre sf != null
	 * @post result != null
	 */
	public final ValuedVariant<N> getFieldVariant(final SootField sf) {
		return getFieldVariant(sf, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given field in the given context. If none exists, a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant associated with the given field in the given context.
	 * @pre sf != null and context != null
	 * @post result != null
	 */
	public final ValuedVariant<N> getFieldVariant(final SootField sf, final Context context) {
		ValuedVariant<N> _temp = null;
		processClass(sf.getDeclaringClass());
		processType(sf.getType());
		sf.addTag(getTag());

		if (Modifier.isStatic(sf.getModifiers())) {
			_temp = staticFieldVariantManager.select(sf, context);
		} else {
			_temp = instanceFieldVariantManager.select(sf, context);
		}
		return _temp;
	}

	/**
	 * Returns an LHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 * @return a new LHS expression visitor.
	 * @pre e != null
	 */
	public final IExprSwitch<N> getLHSExpr(final IStmtSwitch e) {
		return lhsVisitorPrototype.getClone(e);
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>. If none
	 * exists, a new variant is created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>.
	 * @pre sm != null
	 * @post result != null
	 */
	public final IMethodVariant<N> getMethodVariant(final SootMethod sm) {
		return getMethodVariant(sm, analyzer.getContext());
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context. If none exists, a new variant is
	 * created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 * @return the variant corresonding to <code>sm</code> in the given context.
	 * @pre sm != null and context != null
	 * @post result != null
	 */
	public final IMethodVariant<N> getMethodVariant(final SootMethod sm, final Context context) {
		return methodVariantManager.select(sm, context);
	}

	/**
	 * Returns the flow graph node as created by the factory.
	 *
	 * @return a new flow graph node.
	 */
	public final N getNewFGNode() {
		return nodePrototype.getClone(this);
	}

	/**
	 * Returns a RHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 * @return a new RHS expression visitor.
	 * @pre e != null
	 */
	public final IExprSwitch<N> getRHSExpr(final IStmtSwitch e) {
		return rhsVisitorPrototype.getClone(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<SootMethod> getRoots() {
		return Collections.unmodifiableCollection(rootMethods);
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
	 * Returns the associated environment.
	 *
	 * @return the associated environment.
	 * @post result != null
	 */
	public final IEnvironment getScene() {
		return environment;
	}

	/**
	 * Returns a statement visitor as created by the factory.
	 *
	 * @param e the method variant which parameterizes the statement visitor.
	 * @return a new statement visitor.
	 * @pre e != null
	 */
	public final IStmtSwitch getStmt(final IMethodVariant<N> e) {
		return stmtVisitorPrototype.getClone(e);
	}

	/**
	 * Retrieves the token manager that manages the tokens whose flow is being instrumented.
	 *
	 * @return the token manager.
	 * @post tokenManager != null
	 */
	public final ITokenManager<T, SYM, R> getTokenManager() {
		return tokenManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IWorkBag<IWork> getWorkBag() {
		return currWorkBag;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasClass(final String scName) {
		return environment.hasClass(scName);
	}

	/**
	 * Performs type-based processing of the given class.
	 *
	 * @param clazz is the class to be processed.
	 * @pre clazz != null
	 */
	public void processClass(final SootClass clazz) {
		classManager.process(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeClass(final SootClass clazz) {
		environment.removeClass(clazz);
	}

	/**
	 * Resets the framework. The framework forgets all information allowing for a new session of analysis to executed.
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
		activePart.activate();
		currWorkBag = workBags[0];
	}

	/**
	 * Sets the prototype of the LHS expression visitor.
	 *
	 * @param prototype obviously.
	 * @pre lhsVisitorPrototype != null
	 */
	public final void setLhsVisitorPrototype(final IPrototype<? extends IExprSwitch<N>> prototype) {
		this.lhsVisitorPrototype = prototype;
	}

	/**
	 * Sets the prototype of flow node.
	 *
	 * @param node is the prototype flow node.
	 */
	public void setNodePrototype(N node) {
		nodePrototype = node;
	}

	/**
	 * Sets the prototype of the RHS expression visitor.
	 *
	 * @param prototype obviously.
	 */
	public final void setRhsVisitorPrototype(final IPrototype<? extends IExprSwitch<N>> prototype) {
		this.rhsVisitorPrototype = prototype;
	}

	/**
	 * Sets the value of <code>sccOptimizationInterval</code>.
	 *
	 * @param interval the new value of <code>sccOptimizationInterval</code>. Zero and negative values will turn off the
	 *            optimization.
	 */
	public final void setSccOptimizationInterval(final int interval) {
		this.sccOptimizationInterval = interval;
	}

	/**
	 * Sets the prototype of the statement visitor.
	 *
	 * @param prototype obviously.
	 */
	public final void setStmtVisitorPrototype(final IPrototype<? extends IStmtSwitch> prototype) {
		this.stmtVisitorPrototype = prototype;
	}

	/**
	 * Sets up the variant manager to handle variance of array objects.
	 *
	 * @param arrayIM is the variant manager. 
	 */
	public void setupArrayVariantManager(IIndexManager<? extends IIndex<?>, ArrayType> arrayIM) {
		arrayVariantManager = new ValuedVariantManager<ArrayType, SYM, T, N, R>(this, arrayIM);
	}

	/**
	 * Sets up the variant manager to handle variance of instance fields.
	 *
	 * @param instancefieldIM is the variant manager.
	 */
	public void setupInstanceFieldVariantManager(final IIndexManager<? extends IIndex<?>, SootField> instancefieldIM) {
		instanceFieldVariantManager = new ValuedVariantManager<SootField, SYM, T, N, R>(this, instancefieldIM);
	}

	/**
	 * Sets up the variant manager to handle variance of methods.
	 *
	 * @param mi is the variant manager.
	 * @param astIMPrototype is the prototype AST variant manager.
	 * @param mvf is the factory that handles method variance.
	 */
	public void setupMethodVariantManager(final IIndexManager<? extends IIndex<?>, SootMethod> mi,
			final IPrototype<? extends IIndexManager<? extends IIndex<?>, Value>> astIMPrototype,
			final IMethodVariantFactory<SYM, T, N, R> mvf) {
		methodVariantManager = new MethodVariantManager<SYM, T, N, R>(this, mi, astIMPrototype, mvf);
	}

	/**
	 * 	 * Sets up the variant manager to handle variance of static fields.
	 *
	 * @param staticfieldIM is the variant manager.
	 */
	public void setupStaticFieldVariantManager(final IIndexManager<? extends IIndex<?>, SootField> staticfieldIM) {
		staticFieldVariantManager = new ValuedVariantManager<SootField, SYM, T, N, R>(this, staticfieldIM);
	}

	/**
	 * Analyzes the given classes starting with <code>root</code> method.
	 *
	 * @param env which contains the classes to be analyzed.
	 * @param root the method to start the analysis from.
	 * @pre environment != null and root != null
	 */
	@SuppressWarnings("unchecked") void analyze(final IEnvironment env, final SootMethod root) {
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
		final WorkList<IWork>[] _workLists = new WorkList[2];
		_workLists[0] = new WorkList<IWork>(workBags[0]);
		_workLists[1] = new WorkList<IWork>(workBags[1]);

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
	 * Retrieves the tag associated with the framework instance.
	 *
	 * @return the tag associated with the framework instance.
	 * @post result != null
	 */
	Tag getTag() {
		return tag;
	}

	/**
	 * Performs type-based processing.
	 *
	 * @param type to be processed.
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
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 *         <code>null</code> if none exist.
	 * @pre a != null
	 * @post result != null
	 */
	final ValuedVariant<N> queryArrayVariant(final ArrayType a) {
		return queryArrayVariant(a, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given array type in the given context.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>. <code>null</code> if none
	 *         exist.
	 * @pre a != null and context != null
	 * @post result != null
	 */
	final ValuedVariant<N> queryArrayVariant(final ArrayType a, final Context context) {
		return arrayVariantManager.query(a, context);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.<code>null</code>
	 *         if none exists.
	 * @pre sf != null
	 * @post result != null
	 */
	final ValuedVariant<N> queryFieldVariant(final SootField sf) {
		return queryFieldVariant(sf, analyzer.getContext());
	}

	/**
	 * Returns the variant associated with the given field in the given context.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant associated with the given field in the given context. <code>null</code> if none exists.
	 * @pre sf != null and context != null
	 * @post result != null
	 */
	final ValuedVariant<N> queryFieldVariant(final SootField sf, final Context context) {
		ValuedVariant<N> _temp = null;

		if (Modifier.isStatic(sf.getModifiers())) {
			_temp = staticFieldVariantManager.query(sf, context);
		} else {
			_temp = instanceFieldVariantManager.query(sf, context);
		}
		return _temp;
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>. <code>null</code> if none
	 *         exist.
	 * @pre sm != null
	 * @post result != null
	 */
	final IMethodVariant<N> queryMethodVariant(final SootMethod sm) {
		return queryMethodVariant(sm, analyzer.getContext());
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 * @return the variant corresonding to <code>sm</code> in the given context. <code>null</code> if none exist.
	 * @pre sm != null and context != null
	 * @post result != null
	 */
	final IMethodVariant<N> queryMethodVariant(final SootMethod sm, final Context context) {
		return methodVariantManager.query(sm, context);
	}

	/**
	 * Collapses SCC of nodes.
	 */
	private void collapseSCCOfNodes() {
		final Collection<N> _rootNodes = new HashSet<N>();
		final ITransformer<ValuedVariant<N>, N> _transformer = new InvokeTransformer<ValuedVariant<N>, N>("getFGNode");
		CollectionUtils.transform(instanceFieldVariantManager.getVariants(), _transformer, _rootNodes);
		CollectionUtils.transform(staticFieldVariantManager.getVariants(), _transformer, _rootNodes);
		_rootNodes.addAll(getVariantsAtMethodInterfaces());
		sccBasedOptimizer.optimize(_rootNodes, tokenManager);
	}

	/**
	 * Retrieves the variants at the method interfaces.
	 *
	 * @return the variants
	 * @post result != null and result.oclIsKindOf(Collection(IVariant))
	 */
	private Collection<N> getVariantsAtMethodInterfaces() {
		final Collection<N> _result = new HashSet<N>();
		final Iterator<IMethodVariant<N>> _i = methodVariantManager.getVariants().iterator();
		final int _iEnd = methodVariantManager.getVariants().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMethodVariant<N> _mv = _i.next();
			_result.add(_mv.queryReturnNode());
			_result.add(_mv.queryThisNode());

			for (int _j = _mv.getMethod().getParameterCount() - 1; _j >= 0; _j--) {
				_result.add(_mv.queryParameterNode(_j));
			}
		}
		_result.remove(null);
		return _result;
	}
}

// End of File
