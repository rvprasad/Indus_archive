
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

package edu.ksu.cis.indus.transformations.slicer;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.RefLikeType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

import soot.jimple.toolkits.scalar.NopEliminator;

import soot.tagkit.Host;

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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TagBasedDestructiveSliceResidualizer.class);

	/**
	 * This maps statements in the system to new statements that should be included in the slice.
	 *
	 * @invariant oldStmt2newStmt != null
	 * @invariant oldStmt2newStmt.oclIsKindOf(Map(Stmt, Stmt))
	 */
	final Map oldStmt2newStmt = new HashMap();

	/**
	 * The system to be residualized.
	 */
	Scene theScene;

	/**
	 * The name of the tag used to identify the parts of the system to be residualized.
	 */
	String tagToResidualize;

	/**
	 * The collection of classes to be removed from the system after residualization.
	 */
	private final Collection classesToKill = new HashSet();

	/**
	 * This is used to process the statements during residualization.
	 */
	private final StmtResidualizer stmtProcessor = new StmtResidualizer();

	/**
	 * This is the collection of statements which are to be replaced by NOP statements in the residue.
	 *
	 * @invariant stmtsToBeNOPed.oclIsKindOf(Collection(Stmt))
	 */
	private List stmtsToBeNOPed = new ArrayList();

	/**
	 * The class being processed up until now.
	 */
	private SootClass currClass;

	/**
	 * The method being processed.
	 */
	private SootMethod currMethod;

	/**
	 * The name of the tag that identifies the relevant part of the system.
	 */
	private String systemTagName;

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
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
		 */
		public void caseIfStmt(final IfStmt stmt) {
			final ValueBox _vBox = stmt.getConditionBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setCondition(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			final ValueBox _vBox = stmt.getInvokeExprBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				final Jimple _jimple = Jimple.v();
				final Value _t = _jimple.newStaticInvokeExpr(theScene.getSootClass("java.lang.System").getMethodByName("gc"));
				stmt.setInvokeExpr(_t);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setKey(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox.getValue()).hasTag(tagToResidualize)) {
				stmt.setKey(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * Residualizes the given statement.
		 *
		 * @param stmt to be residualized.
		 *
		 * @return <code>true</code> if the statement should be replaced by the result in the residue; <code>false</code>,
		 * 		   otherwise.
		 *
		 * @pre stmt != null
		 */
		boolean residualize(final Stmt stmt) {
			boolean _result = false;

			if (stmt.hasTag(tagToResidualize)) {
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
		 * Residualize the definition statement.
		 *
		 * @param stmt to be residualized.
		 *
		 * @pre stmt != null
		 */
		private void residualizeDefStmt(final DefinitionStmt stmt) {
			if (!((Host) stmt.getLeftOpBox()).hasTag(tagToResidualize)) {
				final Jimple _jimple = Jimple.v();
				final Value _expr = stmt.getRightOp();
				final Stmt _stmt = _jimple.newInvokeStmt(_expr);
				valueProcessor.residualize(_expr, stmt.getRightOpBox());
				setResult(_stmt);
			} else {
				valueProcessor.residualize(stmt.getRightOp(), stmt.getRightOpBox());
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
		 * @see soot.jimple.RefSwitch#defaultCase(java.lang.Object)
		 */
		public void defaultCase(final Object v) {
			Value _v = (Value) v;

			for (final Iterator _i = _v.getUseBoxes().iterator(); _i.hasNext();) {
				final ValueBox _vBox = (ValueBox) _i.next();
				setResult(null);
				_vBox.getValue().apply(this);

				_v = (Value) getResult();

				if (_v != null) {
					_vBox.setValue(_v);
				}
			}
		}

		/**
		 * Residualizes the value.
		 *
		 * @param value to be residualized.
		 * @param vBox contains <code>value</code>.
		 *
		 * @pre value != null and vBox != null
		 */
		public void residualize(final Value value, final ValueBox vBox) {
			if (!((Host) vBox).hasTag(tagToResidualize)) {
				vBox.setValue(getDefaultValueFor(value.getType()));
			} else {
				setResult(null);
				value.apply(this);

				final Value _v = (Value) getResult();

				if (_v != null) {
					vBox.setValue(_v);
				}
			}
		}
	}

	/**
	 * Set the tag that identifies the relevant part of the given system.
	 *
	 * @param nameOfSystemTag of the tag.
	 *
	 * @pre nameOfSystemTag != null
	 */
	public void setSystemTag(final String nameOfSystemTag) {
		systemTagName = nameOfSystemTag;
	}

	/**
	 * Sets the name of that tag that identifies the parts of the system that need to be residualized.
	 *
	 * @param nameOfTagToResidualize is the name of the tag.
	 *
	 * @pre nameOfTagToResidualize != null
	 */
	public void setTagToResidualize(final String nameOfTagToResidualize) {
		tagToResidualize = nameOfTagToResidualize;
	}

	/**
	 * Called by the processing controller to process a statement.
	 *
	 * @param stmt is the statement to be processed.
	 * @param ctxt <i>ignored</i>.
	 *
	 * @pre stmt != null
	 */
	public void callback(final Stmt stmt, final Context ctxt) {
		if (currMethod != null && !stmtProcessor.residualize(stmt)) {
			stmtsToBeNOPed.add(stmt);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		if (currClass != null) {
			finishUpProcessingMethod();

			if (method.hasTag(tagToResidualize)) {
				currMethod = method;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Residualized method " + method);
				}
			} else {
				currMethod = null;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Deleted method " + method);
				}
			}
		} else {
			currMethod = null;

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Deleted method " + method);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		finishUpProcessingMethod();
		finishUpProcessingClass();

		if (clazz.hasTag(tagToResidualize)) {
			currClass = clazz;

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualized class " + clazz);
			}
		} else {
			classesToKill.add(clazz);
			currClass = null;
			currMethod = null;

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Deleted class " + clazz);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		finishUpProcessingMethod();
		finishUpProcessingClass();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.registerForAllStmts(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		currMethod = null;
		currClass = null;
		classesToKill.clear();
	}

	/**
	 * Residualizes the given system.
	 *
	 * @param scene is the system to be residualized.
	 *
	 * @pre scene != null
	 */
	public void residualizeSystem(final Scene scene) {
		theScene = scene;

		final ProcessingController _pc = new ProcessingController();
		_pc.setProcessingFilter(new TagBasedProcessingFilter(systemTagName));
		_pc.setEnvironment(new Environment(scene));
		hookup(_pc);
		_pc.process();
		unhook(_pc);
		_pc.reset();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Removing classes: " + classesToKill);
		}
		scene.getClasses().removeAll(classesToKill);

		/*
		   final IProcessor _erasure = new ErasingProcessor();
		   _pc.setProcessingFilter(new AntiTagBasedProcessingFilter(tagName));
		   _erasure.hookup(_pc);
		   _pc.process();
		   _erasure.unhook(_pc);
		 */
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
	}

	/**
	 * Retrieves the default value for the given type.
	 *
	 * @param type for which the default value is requested.
	 *
	 * @return the default value
	 *
	 * @throws IllegalArgumentException when an invalid type is provided.
	 *
	 * @pre type != null
	 * @post result != null
	 */
	Value getDefaultValueFor(final Type type) {
		Value _result = null;

		if (type instanceof RefLikeType) {
			_result = NullConstant.v();
		} else if (type instanceof IntType) {
			_result = IntConstant.v(0);
		} else if (type instanceof CharType) {
			_result = IntConstant.v(0);
		} else if (type instanceof ByteType) {
			_result = IntConstant.v(0);
		} else if (type instanceof BooleanType) {
			_result = IntConstant.v(0);
		} else if (type instanceof DoubleType) {
			_result = DoubleConstant.v(0);
		} else if (type instanceof FloatType) {
			_result = FloatConstant.v(0);
		} else if (type instanceof LongType) {
			_result = LongConstant.v(0);
		} else if (type instanceof ShortType) {
			_result = IntConstant.v(0);
		} else {
			LOGGER.error("Illegal type specified.");
			throw new IllegalArgumentException("Illegal type specified.");
		}

		return _result;
	}

	/**
	 * Finish processing the current class by erasing the body of untagged methods and deleting fields.
	 */
	private void finishUpProcessingClass() {
		if (currClass != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Finishing class " + currClass);
			}

			final Collection _temp = new HashSet();

			for (final Iterator _i = currClass.getMethods().iterator(); _i.hasNext();) {
				final SootMethod _sm = (SootMethod) _i.next();

				if (!_sm.hasTag(tagToResidualize)) {
					_temp.add(_sm);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing methods: " + _temp);
			}

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				currClass.removeMethod((SootMethod) _i.next());
			}
			_temp.clear();

			for (final Iterator _j = currClass.getFields().iterator(); _j.hasNext();) {
				final SootField _sf = (SootField) _j.next();

				if (!_sf.hasTag(tagToResidualize)) {
					_temp.add(_sf);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing fields: " + _temp);
			}

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				currClass.removeField((SootField) _i.next());
			}
		}
	}

	/**
	 * Finish processing the current method by replacing statements with nops or new statements as recorded during
	 * processing.
	 */
	private void finishUpProcessingMethod() {
		if (currMethod != null && currMethod.isConcrete()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Finishing method " + currMethod);
			}

			// replace statements marked as nop statements with nop statements.
			final JimpleBody _body = (JimpleBody) currMethod.getActiveBody();
			final Chain _ch = _body.getUnits();
			final Jimple _jimple = Jimple.v();

			for (final Iterator _i = stmtsToBeNOPed.iterator(); _i.hasNext();) {
				final Stmt _stmt = (Stmt) _i.next();
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
			for (final Iterator _i = oldStmt2newStmt.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Stmt _oldStmt = (Stmt) _entry.getKey();
				final Object _pred = _ch.getPredOf(_oldStmt);
				_ch.remove(_oldStmt);

				final Object _newStmt = _entry.getValue();

				if (_pred == null) {
					_ch.addFirst(_newStmt);
				} else {
					_ch.insertAfter(_newStmt, _pred);
				}
			}
			System.out.println("Method Body of " + currMethod);

			for (final Iterator _i = _ch.iterator(); _i.hasNext();) {
				try {
					System.out.println(_i.next());
				} catch (RuntimeException r) {
					r.printStackTrace();
				}
			}

			oldStmt2newStmt.clear();
			_body.validateLocals();
			_body.validateTraps();
			_body.validateUnitBoxes();
			_body.validateUses();
			NopEliminator.v().transform(_body);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/12/15 16:30:57  venku
   - safety checks and formatting.
   Revision 1.3  2003/12/14 17:00:51  venku
   - enabled nop elimination
   - fixed return statement in methods.
   Revision 1.2  2003/12/14 16:40:30  venku
   - added residualization logic.
   - incorporate the residualizer in the tool.
   Revision 1.1  2003/12/09 11:02:38  venku
   - synchronization forced commit.
 */
