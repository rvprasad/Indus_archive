
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

import edu.ksu.cis.indus.common.soot.Util;

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

import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
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
	 * This tracks the fields of the current class that should be deleted.
	 *
	 * @invariant fieldsToKill.oclIsKindOf(Collection(SootField))
	 */
	private final Collection fieldsToKill = new HashSet();

	/**
	 * This tracks the methods of the current class that should be deleted.
	 *
	 * @invariant methodsToKill.oclIsKindOf(Collection(SootMethod))
	 */
	private final Collection methodsToKill = new HashSet();

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
	 * This class residualizes statements.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class StmtResidualizer
	  extends AbstractStmtSwitch {
		/*
		 * THINK: In enter/exit montior, invoke, and conditional statements, there is no meaning in marking the statement as
		 * required and not the containing expression.  (Or can it be?) Hence, for such statements we assume that the
		 * containing expressions will be residualized. If so, then we can leave the expression untouched.  Only in the case
		 * of invocation statement do we need to process the arguments to the call.
		 *
		 * TODO: Depending on the relevant branches of the conditional various branches of the conditional can be sliced to
		 * improve precision.
		 */

		/**
		 * This is the instance to be used to residualize expressions/values.
		 */
		final ValueResidualizer valueProcessor = new ValueResidualizer();

		/**
		 * The logger used by instances of this class to log messages.
		 */
		private final Log stmtResidualizerLogger = LogFactory.getLog(StmtResidualizer.class);

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			valueProcessor.residualize(stmt.getInvokeExprBox());
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setKey(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setKey(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(Util.getDefaultValueFor(_vBox.getValue().getType()));
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
		 * @throws IllegalStateException when the rhs is included in the slice and the lhs is not included in the slice.
		 *
		 * @pre stmt != null
		 */
		private void residualizeDefStmt(final DefinitionStmt stmt) {
			if (!((Host) stmt.getLeftOpBox()).hasTag(tagToResidualize)) {
				final ValueBox _rightOpBox = stmt.getRightOpBox();

				/*
				 * If the definition statement is marked and the lhs is unmarked then the rhs should be a marked invoke expr.
				 */
				if (_rightOpBox.hasTag(tagToResidualize) && !stmt.containsInvokeExpr()) {
					final String _message =
						"Incorrect slice.  "
						+ "How can a def statement and it's non-invoke rhs be marked with the lhs unmarked? ->" + stmt;
					stmtResidualizerLogger.error(_message);
					throw new IllegalStateException(_message);
				}

				final Jimple _jimple = Jimple.v();

				if (stmt.containsInvokeExpr()) {
					final Value _expr = stmt.getRightOp();
					final Stmt _stmt = _jimple.newInvokeStmt(_expr);
					valueProcessor.residualize(stmt.getRightOpBox());
					setResult(_stmt);
				} else {
					setResult(_jimple.newNopStmt());
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
		 * @see soot.jimple.RefSwitch#defaultCase(java.lang.Object)
		 */
		public void defaultCase(final Object v) {
			final Value _v = (Value) v;

			for (final Iterator _i = _v.getUseBoxes().iterator(); _i.hasNext();) {
				final ValueBox _vBox = (ValueBox) _i.next();
				residualize(_vBox);
			}
		}

		/**
		 * Residualizes the value.
		 *
		 * @param vBox contains <code>value</code>.
		 *
		 * @pre value != null and vBox != null
		 */
		public void residualize(final ValueBox vBox) {
			final Value _value = vBox.getValue();

			if (!((Host) vBox).hasTag(tagToResidualize)) {
				vBox.setValue(Util.getDefaultValueFor(_value.getType()));
			} else {
				_value.apply(this);
			}
		}
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
			consolidateMethod();

			if (method.hasTag(tagToResidualize)) {
				currMethod = method;
				methodsToKill.remove(method);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Residualized method " + method);
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Deleting method " + method);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		consolidateClass();

		if (clazz.hasTag(tagToResidualize)) {
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
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public void callback(final SootField field) {
		if (currClass != null && field.hasTag(tagToResidualize)) {
			fieldsToKill.remove(field);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualized field " + field);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		consolidateClass();
	}

	/**
	 * Consolidate the current method.
	 *
	 * @post currMethod = null
	 */
	public void consolidateMethod() {
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
				final Entry _entry = (Entry) _i.next();
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

			oldStmt2newStmt.clear();
			_body.validateLocals();
			_body.validateTraps();
			_body.validateUnitBoxes();
			_body.validateUses();
			NopEliminator.v().transform(_body);

			/*
			 * It is possible that some methods are marked but none of their statements are marked.  This can happen in
			 * executable slice with no specialization.  Hence, the body needs to be fixed for the code to be executable.
			 */
			if (_body.getUnits().isEmpty()) {
				// remove all traps 
				_body.getTraps().clear();
				// remove all locals
				_body.getLocals().clear();

				final Chain _temp = _body.getUnits();
				final Type _retType = currMethod.getReturnType();

				if (_retType instanceof VoidType) {
					_temp.add(_jimple.newReturnVoidStmt());
				} else {
					_temp.add(_jimple.newReturnStmt(Util.getDefaultValueFor(_retType)));
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Finishing method " + currMethod);
		}
		currMethod = null;
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
		classesToKill.addAll(theScene.getClasses());
		oldStmt2newStmt.clear();
		stmtsToBeNOPed.clear();
		methodsToKill.clear();
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
		_pc.setProcessingFilter(new TagBasedProcessingFilter(tagToResidualize));
		_pc.setEnvironment(new Environment(scene));
		hookup(_pc);
		_pc.process();
		unhook(_pc);
		_pc.reset();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Deleting classes: " + classesToKill);
		}

		for (final Iterator _i = classesToKill.iterator(); _i.hasNext();) {
			scene.removeClass((SootClass) _i.next());
		}
	}

	/**
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

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("BEGIN: Finishing class " + currClass);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing methods: " + methodsToKill);
			}

			for (final Iterator _i = methodsToKill.iterator(); _i.hasNext();) {
				currClass.removeMethod((SootMethod) _i.next());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing fields: " + fieldsToKill);
			}

			for (final Iterator _i = fieldsToKill.iterator(); _i.hasNext();) {
				currClass.removeField((SootField) _i.next());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("END: Finishing class " + currClass);
			}
			currClass = null;
		}
		methodsToKill.clear();
		fieldsToKill.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.17  2004/01/31 01:48:18  venku
   - for odd reasons, various transformers provided in SOOT fail,
     hence, they are not used anymore.
   Revision 1.16  2004/01/30 23:57:11  venku
   - uses various body transformers to optimize the body.
   - uses entry control DA to pick only the required exit
     points while making the slice executable.
   Revision 1.15  2004/01/25 07:50:20  venku
   - changes to accomodate class hierarchy fixup and handling of
     statements which are marked as true but in which none of the
     expressions are marked as true.
   Revision 1.14  2004/01/24 01:43:40  venku
   - moved getDefaultValueFor() to Util.
   Revision 1.13  2004/01/22 01:07:00  venku
   - coding convention.
   Revision 1.12  2004/01/22 01:06:13  venku
   - coding convention.
   Revision 1.11  2004/01/17 23:25:20  venku
   - value was being cast into a Host.  FIXED.
   Revision 1.10  2004/01/14 12:01:02  venku
   - documentation.
   - error was not flagged when incorrect slice is detected. FIXED.
   Revision 1.9  2004/01/13 10:59:42  venku
   - systemTagName is not required by TagBasedDestructiveSliceResidualizer.
     It was deleted.
   - ripple effect.
   Revision 1.8  2004/01/11 03:38:03  venku
   - entire method bodies may be deleted or not included in the
     first place (due to inheritance).  If so, a suitable return
     statement is injected.
   - We now only process methods which are tagged with
     tagToResidualize rather than systemTagName.
   Revision 1.7  2004/01/09 23:15:37  venku
   - chnaged the method,field, and class kill logic.
   - changed method and class finishing logic.
   - removed unnecessary residualization when some
     properties about statements such as enter monitor
     and invoke expr are known.
   Revision 1.6  2004/01/09 07:03:07  venku
   - added annotations for code to be removed.
   Revision 1.5  2003/12/16 12:43:33  venku
   - fixed many errors during destruction of the system.
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
