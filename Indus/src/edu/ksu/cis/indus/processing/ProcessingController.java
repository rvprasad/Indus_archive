
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

package edu.ksu.cis.indus.processing;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.indus.interfaces.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class controls the post processing for an analysis.  The analyses such as FA are very low-level.  The information is
 * raw.  This needs to be massaged via post processing.  Each post processor can registered interest in particular types of
 * AST chunks.  The controller will walk over the analyzed system and call the registered post processors. The post
 * processors then collect information from the analysis in form which is more accessible to the other applications. This
 * visitor will notify the interested post processors with the given AST node and then visit it's children.
 * 
 * <p>
 * Please note that the processor should be registered/unregistered separately for interface-level (class/method)  processing
 * and functional (method-body) processing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ProcessingController {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ProcessingController.class);

	/**
	 * The collection of processors registered with this controller to process interfaces (class/method).   This maintains
	 * the insertion order.
	 *
	 * @invariant interfaceProcessors->forall(o | o.isOclKindOf(IProcessor))
	 */
	protected final Collection interfaceProcessors = new ArrayList();

	/**
	 * The context in which the AST chunk is visited during post processing.
	 */
	protected Context context = new Context();

	/**
	 * This maps a class to the post processors interested in processing the analysis information pertaining to AST nodes of
	 * class type.
	 *
	 * @invariant class2processors.oclIsKindOf(Map(Class, Set(IProcessors)))
	 */
	protected final Map class2processors = new HashMap();

	/**
	 * This walks over the statements for processing.
	 */
	protected final StmtSwitcher stmtSwitcher = new StmtSwitcher(new ValueSwitcher());

	/**
	 * This defines the environment in which the processing runs.
	 */
	private IEnvironment env;

	/**
	 * This class visits the statements of the methods and calls the call-back methods of the registered processors.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class StmtSwitcher
	  extends AbstractStmtSwitch {
		/**
		 * This walks expressions in the statement.
		 */
		private final ValueSwitcher valueSwitcher;

		/**
		 * Creates a new StmtSwitcher object.
		 *
		 * @param vs is the expressions processor.
		 */
		StmtSwitcher(final ValueSwitcher vs) {
			this.valueSwitcher = vs;
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			defaultCase(AssignStmt.class, stmt);
			context.setProgramPoint(stmt.getLeftOpBox());
			stmt.getLeftOp().apply(valueSwitcher);
			context.setProgramPoint(stmt.getRightOpBox());
			stmt.getRightOp().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
		 */
		public void caseBreakpointStmt(final BreakpointStmt stmt) {
			defaultCase(BreakpointStmt.class, stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
			defaultCase(EnterMonitorStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
			defaultCase(ExitMonitorStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
		 */
		public void caseGotoStmt(final GotoStmt stmt) {
			defaultCase(GotoStmt.class, stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			defaultCase(IdentityStmt.class, stmt);
			context.setProgramPoint(stmt.getLeftOpBox());
			stmt.getLeftOp().apply(valueSwitcher);
			context.setProgramPoint(stmt.getRightOpBox());
			stmt.getRightOp().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
		 */
		public void caseIfStmt(final IfStmt stmt) {
			defaultCase(IfStmt.class, stmt);
			context.setProgramPoint(stmt.getConditionBox());
			stmt.getCondition().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			defaultCase(InvokeStmt.class, stmt);
			context.setProgramPoint(stmt.getInvokeExprBox());
			stmt.getInvokeExpr().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			defaultCase(LookupSwitchStmt.class, stmt);
			context.setProgramPoint(stmt.getKeyBox());
			stmt.getKey().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
		 */
		public void caseNopStmt(final NopStmt stmt) {
			defaultCase(NopStmt.class, stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
		 */
		public void caseRetStmt(final RetStmt stmt) {
			defaultCase(RetStmt.class, stmt);
			context.setProgramPoint(stmt.getStmtAddressBox());
			stmt.getStmtAddress().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			defaultCase(ReturnStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
		 */
		public void caseReturnVoidStmt(final ReturnVoidStmt stmt) {
			defaultCase(ReturnVoidStmt.class, stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			defaultCase(TableSwitchStmt.class, stmt);
			context.setProgramPoint(stmt.getKeyBox());
			stmt.getKey().apply(valueSwitcher);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			defaultCase(ThrowStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(valueSwitcher);
		}

		/**
		 * Calls the processors interested in processing objects of type <code>objClass</code>.
		 *
		 * @param objClass the class of <code>o</code>.
		 * @param o the AST INode to be processed.
		 */
		private void defaultCase(final Class objClass, final Object o) {
			Collection temp = (Collection) class2processors.get(objClass);

			if (temp != null) {
				Stmt stmt = (Stmt) o;

				for (Iterator i = temp.iterator(); i.hasNext();) {
					IProcessor pp = (IProcessor) i.next();
					pp.callback(stmt, context);
				}
			}
		}
	}


	/**
	 * This class walks the expressions and calls the call-methods of the registered processors.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class ValueSwitcher
	  extends AbstractJimpleValueSwitch {
		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr( soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			defaultCase(InterfaceInvokeExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewArrayExpr( soot.jimple.NewArrayExpr)
		 */
		public void caseNewArrayExpr(final NewArrayExpr v) {
			defaultCase(NewArrayExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewExpr( soot.jimple.NewExpr)
		 */
		public void caseNewExpr(final NewExpr v) {
			defaultCase(NewExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr( soot.jimple.NewMultiArrayExpr)
		 */
		public void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
			defaultCase(NewMultiArrayExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr( soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			defaultCase(SpecialInvokeExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr( soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			defaultCase(StaticInvokeExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr( soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			defaultCase(VirtualInvokeExpr.class, v);
		}

		/**
		 * Calls the processors interested in processing object of type <code>objClass</code>.
		 *
		 * @param objClass the class of <code>o</code>.
		 * @param o the AST node to be processed.
		 */
		private void defaultCase(final Class objClass, final Object o) {
			Collection temp = (Collection) class2processors.get(objClass);

			if (temp != null) {
				Value value = (Value) o;

				for (Iterator i = temp.iterator(); i.hasNext();) {
					IProcessor pp = (IProcessor) i.next();
					pp.callback(value, context);
				}
			}
		}
	}

	/**
	 * Sets the environment which provides
	 *
	 * @param environment an instance of the FA.
	 */
	public void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * Controls the processing activity.
	 */
	public final void process() {
		Collection processors = new HashSet();
		processors.addAll(interfaceProcessors);

		for (Iterator i = class2processors.values().iterator(); i.hasNext();) {
			processors.addAll((Collection) i.next());
		}

		initializeProcessors(processors);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: processing classes");
		}
		processClasses(env.getClasses());

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: processing classes");
			LOGGER.info("BEGIN: processor consolidation");
		}

		for (Iterator i = processors.iterator(); i.hasNext();) {
			((IProcessor) i.next()).consolidate();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: processor consolidation");
		}
	}

	/**
	 * Registers the processor.  It indicates that the processor is interested in processing AST chunk of type
	 * <code>interest</code>.
	 *
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of post processor.
	 */
	public void register(final Class interest, final IProcessor processor) {
		Set temp = (Set) class2processors.get(interest);

		if (temp == null) {
			temp = new HashSet();
			class2processors.put(interest, temp);
		}
		temp.add(processor);
	}

	/**
	 * Registers the processor for class and method interface processing only.
	 *
	 * @param processor the instance of post processor.
	 */
	public void register(final IProcessor processor) {
		if (!interfaceProcessors.contains(processor)) {
			interfaceProcessors.add(processor);
		}
	}

	/**
	 * Unregisters the processor.  It indicates that the processor is no longer interested in processing AST chunk of type
	 * <code>interest</code>.
	 *
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of post processor.
	 *
	 * @throws IllegalArgumentException when there are no processors who have registered to process <code>interest</code>.
	 */
	public void unregister(final Class interest, final IProcessor processor) {
		Set temp = (Set) class2processors.get(interest);

		if (temp == null) {
			throw new IllegalArgumentException("There are no processors registered  for " + interest.getName());
		}
		temp.remove(processor);
	}

	/**
	 * Unregisters the processor for class and method interface processing only.
	 *
	 * @param processor the instance of post processor.
	 */
	public void unregister(final IProcessor processor) {
		interfaceProcessors.remove(processor);
	}

	/**
	 * Filter out classes from the given collection of classes.
	 *
	 * @param classes is the classes to be filtered.
	 *
	 * @return a collection containing the classes which were not filtered out.
	 *
	 * @pre classes != null and classes.oclIsKindOf(Collection(SootClass))
	 * @post result != null
	 */
	protected Collection filterClasses(final Collection classes) {
		return classes;
	}

	/**
	 * Filter out methods from the given collection of methods.
	 *
	 * @param methods is the methods to be filtered.
	 *
	 * @return a collection containing the methods which were not filtered out.
	 *
	 * @pre methods != null and methods.oclIsKindOf(Collection(SootMethod))
	 * @post result != null
	 */
	protected Collection filterMethods(final Collection methods) {
		return methods;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param processors DOCUMENT ME!
	 */
	protected void initializeProcessors(final Collection processors) {
	}

	/**
	 * Controls the processing of class level entities.
	 *
	 * @param theClasses to be processed.
	 *
	 * @pre theClasses != null and theClasses.oclIsKindOf(Collection(SootClass))
	 */
	protected void processClasses(final Collection theClasses) {
		Collection classes = filterClasses(theClasses);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Classes to be processed:\n" + classes);
		}

		for (Iterator i = classes.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing class " + sc);
			}

			for (Iterator k = interfaceProcessors.iterator(); k.hasNext();) {
				IProcessor pp = (IProcessor) k.next();
				pp.callback(sc);

				for (Iterator j = sc.getFields().iterator(); j.hasNext();) {
					SootField field = (SootField) j.next();
					pp.callback(field);
				}
			}
			processMethods(sc.getMethods());
		}
	}

	/**
	 * Controls the processing of methods and their bodies.
	 *
	 * @param theMethods to be processed.
	 *
	 * @pre theMethods != null and theMethods.oclIsKindOf(Collection(SootMethod))
	 */
	protected void processMethods(final Collection theMethods) {
		Collection methods = filterMethods(theMethods);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Methods to be processed:\n" + methods);
		}

		List sl = new ArrayList();

		for (Iterator j = methods.iterator(); j.hasNext();) {
			SootMethod sm = (SootMethod) j.next();
			context.setRootMethod(sm);

			for (Iterator k = interfaceProcessors.iterator(); k.hasNext();) {
				((IProcessor) k.next()).callback(sm);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing method " + sm);
			}

			if (sm.isConcrete()) {
				try {
					sl.clear();
					sl.addAll(sm.retrieveActiveBody().getUnits());

					for (Iterator k = sl.iterator(); k.hasNext();) {
						Stmt stmt = (Stmt) k.next();
						context.setStmt(stmt);
						stmt.apply(stmtSwitcher);
					}
				} catch (RuntimeException e) {
					LOGGER.warn("Well, exception while processing statements of a method may mean the processor does not"
						+ " recognize the given method or it's parts or method has not stored in jimple representation. : "
						+ sm.getSignature(), e);
				}
			} else {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/06 05:15:05  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.

   Revision 1.8  2003/10/21 08:41:04  venku
   - Changed the methods/classes get filtered.
   Revision 1.7  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/09/08 02:21:16  venku
   - processors will need to register separately for functional procesing
     and inteface processing.
   Revision 1.5  2003/08/25 08:36:27  venku
   Coding convention.
   Revision 1.4  2003/08/25 08:07:26  venku
   Extracts the classes for processing from the environment.
   It now has support to be driven by the environment alone.
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/11 06:38:25  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
