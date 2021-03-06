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

package edu.ksu.cis.indus.slicer.transformations;

import edu.ksu.cis.indus.common.collections.IFactory;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.Constants;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysisv2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.LocalCreation;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.util.Chain;

/**
 * This class residualizes the slice by destructively updating the system from which the slice was built.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TagBasedDestructiveSliceResidualizer
		extends AbstractProcessor {

	/**
	 * A factory to create pair to contain members of a class.
	 */
	static final IFactory<Pair<Collection<SootMethod>, Collection<SootField>>> PAIR_VALUE_FACTORY = new IFactory<Pair<Collection<SootMethod>, Collection<SootField>>>() {

		public Pair<Collection<SootMethod>, Collection<SootField>> create() {
			return new Pair<Collection<SootMethod>, Collection<SootField>>(new ArrayList<SootMethod>(),
					new ArrayList<SootField>());
		}
	};

	/**
	 * This class residualizes statements.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class StmtResidualizer
			extends AbstractStmtSwitch {

		/**
		 * This is the instance to be used to residualize expressions/values.
		 */
		final ValueResidualizer valueProcessor = new ValueResidualizer();

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		@Override public void caseAssignStmt(final AssignStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		@Override public void caseIdentityStmt(final IdentityStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		@Override public void caseInvokeStmt(final InvokeStmt stmt) {
			if (stmt.getInvokeExprBox().hasTag(theNameOfTagToResidualize)) {
				valueProcessor.residualize(stmt.getInvokeExprBox());
			} else {
				setResult(Jimple.v().newNopStmt());
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		@Override public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(theNameOfTagToResidualize)) {
				stmt.setKey(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		@Override public void caseReturnStmt(final ReturnStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(theNameOfTagToResidualize)) {
				stmt.setOp(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		@Override public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(theNameOfTagToResidualize)) {
				stmt.setKey(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		@Override public void caseThrowStmt(final ThrowStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(theNameOfTagToResidualize)) {
				final Value _val = stmt.getOp();
				final RefType _type = (RefType) _val.getType();
				final Jimple _jimple = Jimple.v();

				if (localUseDef == null) {
					localUseDef = new LocalUseDefAnalysisv2(bbgMgr.getBasicBlockGraph(currMethod));
				}

				final Collection<DefinitionStmt> _defs = localUseDef.getDefs(stmt, currMethod);
				boolean _injectNewCode = true;
				final Iterator<DefinitionStmt> _j = _defs.iterator();
				final int _jEnd = _defs.size();

				for (int _jIndex = 0; _jIndex < _jEnd && _injectNewCode; _jIndex++) {
					final DefinitionStmt _def = _j.next();
					_injectNewCode = !_def.getLeftOpBox().hasTag(theNameOfTagToResidualize);
				}

				if (_injectNewCode) {
					// add a new local to the body
					final LocalCreation _lc = new LocalCreation(currMethod.getActiveBody().getLocals());
					final Local _local = _lc.newLocal(_type);
					localsToKeep.add(_local);

					// create an exception of the thrown type and assign it to the created local.
					final NewExpr _newExpr = _jimple.newNewExpr(_type);
					final AssignStmt _astmt = _jimple.newAssignStmt(_local, _newExpr);
					final Tag _tag = new NamedTag(theNameOfTagToResidualize);
					_astmt.addTag(_tag);
					_astmt.getLeftOpBox().addTag(_tag);
					_astmt.getRightOpBox().addTag(_tag);

					final SootClass _clazz = _type.getSootClass();

					// retain the class
					if (!_clazz.hasTag(theNameOfTagToResidualize)) {
						classesToKill.remove(_clazz);
						_clazz.addTag(tagToResidualize);
					}

					// find an <init> method on the type and prepare the argument list
					final SootMethod _init = prepareInitIn(_clazz);
					final List<Value> _args = new ArrayList<Value>(_init.getParameterCount());

					for (int _i = _init.getParameterCount() - 1; _i >= 0; _i--) {
						_args.add(Util.getDefaultValueFor(_init.getParameterType(_i)));
					}

					// invoke <init> on the local
					final InvokeExpr _iexpr = _jimple.newSpecialInvokeExpr(_local, _init, _args);
					final InvokeStmt _istmt = _jimple.newInvokeStmt(_iexpr);
					_istmt.addTag(_tag);
					_istmt.getInvokeExprBox().addTag(_tag);
					((SpecialInvokeExpr) _iexpr).getBaseBox().addTag(_tag);

					// prepare a new list of statements of predecessors to be inserted before the throw statement in the final
					// body of the method.
					final List<Stmt> _stmts = new ArrayList<Stmt>();
					_stmts.add(_astmt);
					_stmts.add(_istmt);
					stmt2predecessors.put(stmt, _stmts);
					stmt.setOp(_local);
				}
			}
		}

		/**
		 * Residualizes the given statement.
		 * 
		 * @param stmt to be residualized.
		 * @return <code>true</code> if the statement should be replaced by the result in the residue; <code>false</code>,
		 *         otherwise.
		 * @pre stmt != null
		 */
		boolean residualize(final Stmt stmt) {
			boolean _result = false;

			if (stmt.hasTag(theNameOfTagToResidualize)) {
				setResult(null);
				stmt.apply(this);

				final Stmt _newStmt = (Stmt) getResult();

				if (_newStmt != null) {
					oldStmt2newStmt.put(stmt, _newStmt);
				}
				_result = true;
			}
			return _result;
		}

		/**
		 * Sucks in vanilla &lt;init&gt; methods if it exists in the given class. If not it will insert one. It will also
		 * massage the class hierarchy branch to be executable.
		 * 
		 * @param clazz in which we are searching for the constructor.
		 * @return the constructor method.
		 * @pre clazz != null
		 * @post result != null
		 */
		private SootMethod prepareInitIn(final SootClass clazz) {
			SootMethod _superinit = null;

			if (clazz.hasSuperclass()) {
				_superinit = prepareInitIn(clazz.getSuperclass());
			}

			SootMethod _init = null;
			boolean _existsButIsNotIncluded = false;
			if (clazz.declaresMethod("<init>", Collections.EMPTY_LIST, VoidType.v())) {
				_init = clazz.getMethod("<init>", Collections.EMPTY_LIST, VoidType.v());
				_existsButIsNotIncluded = _init != null ? !_init.hasTag(theNameOfTagToResidualize) : false;
			}

			if (_init == null || _existsButIsNotIncluded) {
				if (_existsButIsNotIncluded) {
					clazz.removeMethod(_init);

					final Pair<Collection<SootMethod>, Collection<SootField>> _pair;
					_pair = MapUtils.getFromMapUsingFactory(class2members, clazz, PAIR_VALUE_FACTORY);
					final Collection<SootMethod> _clazzMethodsToKill = _pair.getFirst();
					_clazzMethodsToKill.remove(_init);
				}

				// SPECIALIZATION: Specialization from here on.
				_init = new SootMethod("<init>", Collections.EMPTY_LIST, VoidType.v());
				_init.addTag(tagToResidualize);

				final Jimple _jimple = Jimple.v();
				final Body _body = _jimple.newBody(_init);

				if (_superinit != null) {
					final RefType _clazzType = RefType.v(clazz);
					final LocalCreation _lc = new LocalCreation(_body.getLocals());
					final Local _this = _lc.newLocal("_this", _clazzType);
					final IdentityStmt _astmt = _jimple.newIdentityStmt(_this, _jimple.newThisRef(_clazzType));
					_astmt.addTag(tagToResidualize);
					_astmt.getLeftOpBox().addTag(tagToResidualize);
					_astmt.getRightOpBox().addTag(tagToResidualize);
					_body.getUnits().add(_astmt);

					final InvokeExpr _iexpr = _jimple.newSpecialInvokeExpr(_this, _superinit);
					final InvokeStmt _istmt = _jimple.newInvokeStmt(_iexpr);
					_istmt.addTag(tagToResidualize);
					_istmt.getInvokeExprBox().addTag(tagToResidualize);
					((SpecialInvokeExpr) _iexpr).getBaseBox().addTag(tagToResidualize);
					_body.getUnits().add(_istmt);
				}

				final Stmt _retStmt = _jimple.newReturnVoidStmt();
				_retStmt.addTag(tagToResidualize);
				_body.getUnits().add(_retStmt);
				_init.setActiveBody(_body);
				clazz.addMethod(_init);

				if (!clazz.hasTag(theNameOfTagToResidualize)) {
					clazz.addTag(tagToResidualize);
				}
			}

			return _init;
		}

		/**
		 * Residualize the definition statement.
		 * 
		 * @param stmt to be residualized.
		 * @throws IllegalStateException when the rhs is included in the slice and the lhs is not included in the slice.
		 * @pre stmt != null
		 */
		private void residualizeDefStmt(final DefinitionStmt stmt) {
			if (!stmt.getLeftOpBox().hasTag(theNameOfTagToResidualize)) {
				final ValueBox _rightOpBox = stmt.getRightOpBox();

				/*
				 * If the definition statement is marked and the lhs is unmarked then the rhs should be a marked invoke expr.
				 */
				if (_rightOpBox.hasTag(theNameOfTagToResidualize) && !stmt.containsInvokeExpr()) {
					final String _message = "Incorrect slice.  "
							+ "How can a def statement and it's non-invoke rhs be marked with the lhs unmarked? ->" + stmt;
					LOGGER.error(_message);
					throw new IllegalStateException(_message);
				}

				if (stmt.containsInvokeExpr()) {
					final Value _expr = stmt.getRightOp();
					final Stmt _stmt = Jimple.v().newInvokeStmt(_expr);
					valueProcessor.residualize(stmt.getRightOpBox());
					setResult(_stmt);
				} else {
					setResult(Jimple.v().newNopStmt());
				}
			} else {
				valueProcessor.residualize(stmt.getRightOpBox());
			}
		}
	}

	/**
	 * This class residualizes expressions and values.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class ValueResidualizer
			extends AbstractJimpleValueSwitch {

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
		 */
		@Override public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			residualize(v.getBaseBox());
			residualizeInvokeExpr(v);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
		 */
		@Override public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			residualize(v.getBaseBox());
			residualizeInvokeExpr(v);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
		 */
		@Override public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			residualizeInvokeExpr(v);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
		 */
		@Override public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			residualize(v.getBaseBox());
			residualizeInvokeExpr(v);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see soot.jimple.RefSwitch#defaultCase(java.lang.Object)
		 */
		@Override public void defaultCase(final Object v) {
			final Value _v = (Value) v;

			for (final Iterator<ValueBox> _i = _v.getUseBoxes().iterator(); _i.hasNext();) {
				final ValueBox _vBox = _i.next();
				residualize(_vBox);
			}
		}

		/**
		 * Residualizes the value.
		 * 
		 * @param vBox contains <code>value</code>.
		 * @pre value != null and vBox != null
		 */
		public void residualize(final ValueBox vBox) {
			final Value _value = vBox.getValue();

			if (!((Host) vBox).hasTag(theNameOfTagToResidualize)) {
				vBox.setValue(Util.getDefaultValueFor(_value.getType()));
			} else {
				_value.apply(this);
			}
		}

		/**
		 * Residualizes the invocation expression in manner that is safe to the Jimple implementation.
		 * 
		 * @param v is the expression to residualize.
		 * @pre v != null
		 */
		private void residualizeInvokeExpr(final InvokeExpr v) {
			/*
			 * HACK: This is required because in jimple the value boxes are "kinded". You cannot stick a NullConstant into a
			 * value box that held a local because the LocalBox cannot hold an Immediate unlike a ImmediateBox. Instead you
			 * need to go through the context enclosing containing the box to fix the contents of the box.
			 */
			for (int _i = v.getArgCount() - 1; _i >= 0; _i--) {
				final ValueBox _vb = v.getArgBox(_i);

				if (!_vb.hasTag(theNameOfTagToResidualize)) {
					v.setArg(_i, Util.getDefaultValueFor(_vb.getValue().getType()));
				}
			}
		}
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(TagBasedDestructiveSliceResidualizer.class);

	/**
	 * This is a mapping from classes to it's members that should be removed.
	 */
	final Map<SootClass, Pair<Collection<SootMethod>, Collection<SootField>>> class2members;

	/**
	 * The collection of classes to be removed from the system after residualization.
	 */
	final Collection<SootClass> classesToKill;

	/**
	 * The method being processed.
	 */
	SootMethod currMethod;

	/**
	 * The system to be residualized.
	 */
	IEnvironment environment;

	/**
	 * This tracks the locals of the current method that should be deleted.
	 */
	final Collection<Value> localsToKeep;

	/**
	 * Local use-def analysis to be used during residualization.
	 */
	IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> localUseDef;

	/**
	 * This tracks the methods of the current class that should be deleted.
	 */
	final Collection<SootMethod> methodsToKill;

	/**
	 * This maps statements in the system to new statements that should be included in the slice.
	 */
	final Map<Stmt, Stmt> oldStmt2newStmt;

	/**
	 * This maps a statement to a sequence of statements that need to be inserted before the key statement the statement
	 * sequence of the method body.
	 */
	final Map<Stmt, List<Stmt>> stmt2predecessors;

	/**
	 * The tag that identify the parts of the system that shall be residualized.
	 */
	NamedTag tagToResidualize;

	/**
	 * The name of the tag used to identify the parts of the system to be residualized.
	 */
	String theNameOfTagToResidualize;

	/**
	 * The basic block graph manager for the system being residualized.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/**
	 * The class being processed up until now.
	 */
	private SootClass currClass;

	/**
	 * This tracks the fields of the current class that should be deleted.
	 */
	private final Collection<SootField> fieldsToKill;

	/**
	 * This is used to process the statements during residualization.
	 */
	private final StmtResidualizer stmtProcessor;

	/**
	 * This is the collection of statements which are to be replaced by NOP statements in the residue.
	 */
	private final Collection<Stmt> stmtsToBeNOPed;

	/**
	 * Creates an instance of this class.
	 */
	public TagBasedDestructiveSliceResidualizer() {
		super();
		stmtsToBeNOPed = new ArrayList<Stmt>();
		stmtProcessor = new StmtResidualizer();
		fieldsToKill = new HashSet<SootField>();
		stmt2predecessors = new HashMap<Stmt, List<Stmt>>();
		oldStmt2newStmt = new HashMap<Stmt, Stmt>();
		methodsToKill = new HashSet<SootMethod>();
		localsToKeep = new HashSet<Value>();
		classesToKill = new HashSet<SootClass>();
		class2members = new HashMap<SootClass, Pair<Collection<SootMethod>, Collection<SootField>>>(Constants
				.getNumOfClassesInApplication());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	@Override public void callback(final SootClass clazz) {
		consolidateClass();

		if (clazz.hasTag(theNameOfTagToResidualize)) {
			currClass = clazz;
			classesToKill.remove(clazz);
			methodsToKill.addAll(clazz.getMethods());
			fieldsToKill.addAll(clazz.getFields());

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualized class " + clazz);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	@Override public void callback(final SootField field) {
		if (currClass != null) {
			fieldsToKill.remove(field);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualized field " + field);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	@Override public void callback(final SootMethod method) {
		if (currClass != null) {
			consolidateMethod();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing method " + method);
			}

			currMethod = method;
			methodsToKill.remove(method);
			localsToKeep.clear();
			localUseDef = null;
		}
	}

	/**
	 * Called by the processing controller to process a statement.
	 * 
	 * @param stmt is the statement to be processed.
	 * @param ctxt <i>ignored</i>.
	 * @pre stmt != null
	 */
	@Override public void callback(final Stmt stmt, @SuppressWarnings("unused") final Context ctxt) {
		if (currMethod != null) {
			pruneLocals(stmt);

			final boolean _flag = stmtProcessor.residualize(stmt);

			if (!_flag) {
				stmtsToBeNOPed.add(stmt);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	@Override public void consolidate() {
		consolidateClass();

		for (final Iterator<SootClass> _i = class2members.keySet().iterator(); _i.hasNext();) {
			final SootClass _class = _i.next();
			final Pair<Collection<SootMethod>, Collection<SootField>> _members = class2members.get(_class);
			final Collection<SootMethod> _methods = _members.getFirst();
			final Collection<SootField> _fields = _members.getSecond();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("BEGIN: Finishing class " + _class);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing methods: " + _methods);
			}

			for (final Iterator<SootMethod> _j = _methods.iterator(); _j.hasNext();) {
				_class.removeMethod(_j.next());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing fields: " + _fields);
			}

			for (final Iterator<SootField> _j = _fields.iterator(); _j.hasNext();) {
				_class.removeField(_j.next());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("END: Finishing class " + _class);
			}
		}
		class2members.clear();
		currClass = null;
		currMethod = null;
		methodsToKill.clear();
		fieldsToKill.clear();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.registerForAllStmts(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	@Override public void processingBegins() {
		currMethod = null;
		currClass = null;
		classesToKill.clear();
		classesToKill.addAll(environment.getClasses());
		oldStmt2newStmt.clear();
		stmtsToBeNOPed.clear();
		methodsToKill.clear();
	}

	/**
	 * Residualizes the given system.
	 * 
	 * @param env is the system to be residualized.
	 * @pre env != null
	 */
	public void residualizeSystem(final IEnvironment env) {
		environment = env;

		final ProcessingController _pc = new ProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();

		/*
		 * We use a new factory for complete statement graph instead of the existing factory as the existing factory may
		 * present a projection of the graph which may cause us to skip over some statements.
		 */
		_ssr.setStmtGraphFactory(new CompleteStmtGraphFactory());

		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(theNameOfTagToResidualize));
		_pc.setEnvironment(env);
		hookup(_pc);
		_pc.process();
		unhook(_pc);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Deleting classes: " + classesToKill);
		}

		for (final Iterator<SootClass> _i = classesToKill.iterator(); _i.hasNext();) {
			env.removeClass(_i.next());
		}
	}

	/**
	 * Sets the basic block graph manager to be used.
	 * 
	 * @param basicBlockGraphMgr to be used.
	 * @pre basicBlockGraphMgr != null
	 */
	public void setBasicBlockGraphMgr(final BasicBlockGraphMgr basicBlockGraphMgr) {
		bbgMgr = basicBlockGraphMgr;
	}

	/**
	 * Sets the name of that tag that identifies the parts of the system that need to be residualized.
	 * 
	 * @param nameOfTagToResidualize is the name of the tag.
	 * @pre nameOfTagToResidualize != null
	 */
	public void setTagToResidualize(final String nameOfTagToResidualize) {
		theNameOfTagToResidualize = nameOfTagToResidualize;
		tagToResidualize = new NamedTag(theNameOfTagToResidualize);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
	}

	/**
	 * Consolidate the current class and method.
	 * 
	 * @post currClass = null and methodsToKill.size() = 0 and fieldsToKill.size() = 0 and currMethod = null
	 */
	private void consolidateClass() {
		if (currClass != null) {
			consolidateMethod();
			class2members.put(currClass, new Pair<Collection<SootMethod>, Collection<SootField>>(new ArrayList<SootMethod>(
					methodsToKill), new ArrayList<SootField>(fieldsToKill)));
			methodsToKill.clear();
			fieldsToKill.clear();
			currClass = null;
		}
	}

	/**
	 * Consolidate the current method.
	 * 
	 * @post currMethod = null
	 */
	private void consolidateMethod() {
		if (currMethod == null) {
			return;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Finishing method " + currMethod + "[concrete: " + currMethod.isConcrete() + "]");
		}

		if (currMethod.isConcrete()) {
			// replace statements marked as nop statements with nop statements.
			final JimpleBody _body = (JimpleBody) currMethod.getActiveBody();
			final Chain _ch = _body.getUnits();
			final Jimple _jimple = Jimple.v();

			if (LOGGER.isDebugEnabled()) {
				final List<Stmt> _l = new ArrayList<Stmt>(_ch);
				final StringBuffer _sb = new StringBuffer();
				for (final Stmt _stmt : stmtsToBeNOPed) {
					_sb.append(_l.indexOf(_stmt));
					_sb.append(", ");
				}
				LOGGER.debug("Stmts being NOP-ed: " + _sb);
			}

			for (final Iterator<Stmt> _i = stmtsToBeNOPed.iterator(); _i.hasNext();) {
				final Stmt _stmt = _i.next();
				final Object _pred = _ch.getPredOf(_stmt);
				_ch.remove(_stmt);

				final Stmt _newStmt = _jimple.newNopStmt();

				if (_pred == null) {
					_ch.addFirst(_newStmt);
				} else {
					_ch.insertAfter(_newStmt, _pred);
				}
			}
			stmtsToBeNOPed.clear();

			// replace statements with new statements as recorded earlier.
			for (final Iterator<Map.Entry<Stmt, Stmt>> _i = oldStmt2newStmt.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry<Stmt, Stmt> _entry = _i.next();
				final Stmt _oldStmt = _entry.getKey();
				final Stmt _newStmt = _entry.getValue();
				_ch.insertAfter(_newStmt, _oldStmt);
				_ch.remove(_oldStmt);
			}

			// inject any cooked up predecessors
			for (final Iterator<Map.Entry<Stmt, List<Stmt>>> _i = stmt2predecessors.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry<Stmt, List<Stmt>> _entry = _i.next();
				final Stmt _stmt = _entry.getKey();
				final List<Stmt> _preds = _entry.getValue();
				_ch.insertBefore(_preds, _stmt);
			}

			stmt2predecessors.clear();
			oldStmt2newStmt.clear();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Locals " + _body.getLocals());
				LOGGER.debug("Retaining:" + localsToKeep);
			}

			// prune locals and traps
			_body.getLocals().retainAll(localsToKeep);
			removeTrapsNotInSlice(_body);

			injectReturnsIntoDanglingNopBlocks(_ch);
			// transformations built into Soot
			NopEliminator.v().transform(_body);
			UnreachableCodeEliminator.v().transform(_body);
			ConditionalBranchFolder.v().transform(_body);
			UnconditionalBranchFolder.v().transform(_body);
			UnusedLocalEliminator.v().transform(_body);
			_body.validateLocals();
			_body.validateTraps();
			_body.validateUnitBoxes();
			_body.validateUses();

			/*
			 * It is possible that some methods are marked but none of their statements are marked. This can happen in
			 * executable slice with no specialization. Hence, the body needs to be fixed for the code to be executable. If
			 * there are NOP's in the body then the following condition will fail. For this reason, this block should happen
			 * after the previous transformations block.
			 */
			if (_body.getUnits().isEmpty()) {
				pluginSignatureCorrectBody(_body, _jimple);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Finishing method " + currMethod);
		}
		currMethod = null;
	}

	/**
	 * Injects an appropriate return statement as the trailer statement of each tail basic block that contains only NOP
	 * statements.
	 * 
	 * @param stmtList to be altered.
	 * @pre stmtList != null and basicBlockGraph != null
	 */
	private void injectReturnsIntoDanglingNopBlocks(final Chain stmtList) {
		/*
		 * HACK: This is a fall out of executability processing. if () { throw a; } else { if () { ret b; } else { ret c; } }
		 * Let the above snippet be the tail of method. If none of these statements are in the slice, then an exit point is
		 * picked randomly. If this is "throw a" is picked, then the else block is nop-ed (refer to
		 * ExecutableSlicePostProcessor.pickReturnPoint()), i.e., there will be a path via which the control from the method
		 * will fall through. To prevent this we plugin a return statement in the tail basic blocks containing only nop
		 * statement. Remember that the basic block graph should be that of the untransformed method and this phase cannot
		 * happen any earlier.
		 */
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEFORE - Stmts: " + stmtList);
		}

		final Stmt _end = (Stmt) stmtList.getLast();

		if (_end instanceof NopStmt) {
			final Type _retType = currMethod.getReturnType();
			final Stmt _newStmt;

			if (_retType instanceof VoidType) {
				_newStmt = Jimple.v().newReturnVoidStmt();
			} else {
				_newStmt = Jimple.v().newReturnStmt(Util.getDefaultValueFor(_retType));
			}
			stmtList.insertAfter(_newStmt, _end);
			stmtList.remove(_end);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("AFTER - Stmts: " + stmtList);
		}
	}

	/**
	 * Plugs in a body that satisfies the signature of the current method given by <code>currMethod</code>.
	 * 
	 * @param body of <code>currMethod</code>.
	 * @param jimpleFactory to be used create parts of the body.
	 * @pre body != null and jimple != null
	 */
	private void pluginSignatureCorrectBody(final JimpleBody body, final Jimple jimpleFactory) {
		// remove all traps
		body.getTraps().clear();
		// remove all locals
		body.getLocals().clear();

		final Chain _temp = body.getUnits();

		// add the identity statements to pop out "this" parameter.
		if (!currMethod.isStatic()) {
			final RefType _type = RefType.v(currMethod.getDeclaringClass());
			final LocalCreation _lc = new LocalCreation(body.getLocals());
			final Local _this = _lc.newLocal("_this", _type);
			final IdentityStmt _astmt = jimpleFactory.newIdentityStmt(_this, jimpleFactory.newThisRef(_type));
			_astmt.addTag(tagToResidualize);
			_astmt.getLeftOpBox().addTag(tagToResidualize);
			_astmt.getRightOpBox().addTag(tagToResidualize);
			body.getUnits().add(_astmt);
		}

		// add the identity statements to pop out the parameters
		final Collection<Type> _paramType = currMethod.getParameterTypes();
		final int _end = _paramType.size();
		final Iterator<Type> _i = _paramType.iterator();

		for (int _pCount = 0; _pCount < _end; _pCount++) {
			final Type _pType = _i.next();
			final LocalCreation _lc = new LocalCreation(body.getLocals());
			final Local _this = _lc.newLocal("_local", _pType);
			final IdentityStmt _astmt = jimpleFactory.newIdentityStmt(_this, jimpleFactory.newParameterRef(_pType, _pCount));
			_astmt.addTag(tagToResidualize);
			_astmt.getLeftOpBox().addTag(tagToResidualize);
			_astmt.getRightOpBox().addTag(tagToResidualize);
			body.getUnits().add(_astmt);
		}

		// add the return statements.
		final Type _retType = currMethod.getReturnType();

		if (_retType instanceof VoidType) {
			_temp.add(jimpleFactory.newReturnVoidStmt());
		} else {
			_temp.add(jimpleFactory.newReturnStmt(Util.getDefaultValueFor(_retType)));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Injected lame body for " + currMethod);
		}
	}

	/**
	 * Prunes the locals in the given stmt.
	 * 
	 * @param stmt in which to process the locals.
	 * @pre stmt != null
	 */
	private void pruneLocals(final Stmt stmt) {
		if (stmt.hasTag(theNameOfTagToResidualize)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Pruning locals in " + stmt);
			}

			final List<ValueBox> _useAndDefBoxes = stmt.getUseAndDefBoxes();
			for (final Iterator<ValueBox> _k = Util.getHostsWithTag(_useAndDefBoxes, theNameOfTagToResidualize).iterator(); _k
					.hasNext();) {
				final ValueBox _vBox = _k.next();
				final Value _value = _vBox.getValue();

				if (_value instanceof Local) {
					localsToKeep.add(_value);
				}
			}
		}
	}

	/**
	 * Removes the traps not included in the slice.
	 * 
	 * @param body to be mutated.
	 * @pre body != null
	 */
	private void removeTrapsNotInSlice(final Body body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Collecting handlers for " + currMethod);
		}

		final Chain _traps = body.getTraps();
		final Iterator<Trap> _i = _traps.iterator();
		final int _iEnd = _traps.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Trap _trap = _i.next();
			final Stmt _handlerStmt = (Stmt) _trap.getHandlerUnit();

			if (!_handlerStmt.hasTag(theNameOfTagToResidualize)
					|| !(_handlerStmt instanceof IdentityStmt && ((IdentityStmt) _handlerStmt).getRightOp() instanceof CaughtExceptionRef)
					|| _trap.getBeginUnit() == _trap.getEndUnit()) {
				_i.remove();
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("RETAINED TRAPS: " + body.getTraps());
			LOGGER.debug("END: Collecting handlers for " + currMethod);
		}
	}
}

// End of File
