
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.AbstractJimpleValueSwitch;
import ca.mcgill.sable.soot.jimple.AbstractStmtSwitch;
import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.EnterMonitorStmt;
import ca.mcgill.sable.soot.jimple.ExitMonitorStmt;
import ca.mcgill.sable.soot.jimple.IdentityStmt;
import ca.mcgill.sable.soot.jimple.IfStmt;
import ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.LookupSwitchStmt;
import ca.mcgill.sable.soot.jimple.NewArrayExpr;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NewMultiArrayExpr;
import ca.mcgill.sable.soot.jimple.RetStmt;
import ca.mcgill.sable.soot.jimple.ReturnStmt;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.StaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.TableSwitchStmt;
import ca.mcgill.sable.soot.jimple.ThrowStmt;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * This class controls the post processing for an analysis.  The analysis as realised by BFA is very low-level.  The
 * information is raw.  This needs to be massaged via post processing.  Each post processor can registered interest in
 * particular types of AST chunks.  The controller will walk over the analyzed system and call the registered post
 * processors. The post processors then collect information from the analysis in form which is more accessible to the other
 * applications. This visitor will notify the interested post processors with the given AST node and then visit it's
 * children.
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
	 * The analyzer instance that provides the low-level analysis information to be be further processed.
	 */
	protected AbstractAnalyzer analyzer;

	/**
	 * The collection of post processors registered with this controller.  This maintains the insertion order.
	 *
	 * @invariant processors->forall(o | o.isOclKindOf(Processor))
	 */
	protected final Collection processors = new ArrayList();

	/**
	 * The context in which the AST chunk is visited during post processing.
	 */
	protected Context context = new Context();

	/**
	 * This maps a class to the post processors interested in processing the analysis information pertaining to AST nodes of
	 * class type.
	 *
	 * @invariant class2processors.keySet()->forall( o | o.oclType = Class)
	 * @invariant class2processors.valueSet()->forall( o | o.oclIsKindOf(java.util.Set))
	 * @invariant class2processors.valueSet()->forall(o | o->forall( p | p.isOclKindOf(Processor)))
	 */
	protected final Map class2processors = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final ValueSwitcher valueSwitcher = new ValueSwitcher();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final StmtSwitcher stmtSwitcher = new StmtSwitcher(valueSwitcher);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class StmtSwitcher
	  extends AbstractStmtSwitch {
		/**
		 * Processes expressions in the statement.
		 */
		private final ValueSwitcher vs;

		/**
		 * Creates a new StmtSwitcher object.
		 *
		 * @param vs is the expressions processor.
		 */
		StmtSwitcher(ValueSwitcher vs) {
			this.vs = vs;
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseAssignStmt(ca.mcgill.sable.soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(AssignStmt stmt) {
			defaultCase(AssignStmt.class, stmt);
			context.setProgramPoint(stmt.getLeftOpBox());
			stmt.getLeftOp().apply(vs);
			context.setProgramPoint(stmt.getRightOpBox());
			stmt.getRightOp().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseEnterMonitorStmt(ca.mcgill.sable.soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
			defaultCase(EnterMonitorStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseExitMonitorStmt(ca.mcgill.sable.soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
			defaultCase(ExitMonitorStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseIdentityStmt(ca.mcgill.sable.soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(IdentityStmt stmt) {
			defaultCase(IdentityStmt.class, stmt);
			context.setProgramPoint(stmt.getLeftOpBox());
			stmt.getLeftOp().apply(vs);
			context.setProgramPoint(stmt.getRightOpBox());
			stmt.getRightOp().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseIfStmt(ca.mcgill.sable.soot.jimple.IfStmt)
		 */
		public void caseIfStmt(IfStmt stmt) {
			defaultCase(IfStmt.class, stmt);
			context.setProgramPoint(stmt.getConditionBox());
			stmt.getCondition().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseInvokeStmt(ca.mcgill.sable.soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(InvokeStmt stmt) {
			defaultCase(InvokeStmt.class, stmt);
			context.setProgramPoint(stmt.getInvokeExprBox());
			stmt.getInvokeExpr().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseLookupSwitchStmt(ca.mcgill.sable.soot.jimple.LookupSwitchStmt)
		 */
		public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
			defaultCase(LookupSwitchStmt.class, stmt);
			context.setProgramPoint(stmt.getKeyBox());
			stmt.getKey().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseRetStmt(ca.mcgill.sable.soot.jimple.RetStmt)
		 */
		public void caseRetStmt(RetStmt stmt) {
			defaultCase(RetStmt.class, stmt);
			context.setProgramPoint(stmt.getStmtAddressBox());
			stmt.getStmtAddress().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseReturnStmt(ca.mcgill.sable.soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(ReturnStmt stmt) {
			defaultCase(ReturnStmt.class, stmt);
			context.setProgramPoint(stmt.getReturnValueBox());
			stmt.getReturnValue().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseTableSwitchStmt(ca.mcgill.sable.soot.jimple.TableSwitchStmt)
		 */
		public void caseTableSwitchStmt(TableSwitchStmt stmt) {
			defaultCase(TableSwitchStmt.class, stmt);
			context.setProgramPoint(stmt.getKeyBox());
			stmt.getKey().apply(vs);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseThrowStmt(ca.mcgill.sable.soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(ThrowStmt stmt) {
			defaultCase(ThrowStmt.class, stmt);
			context.setProgramPoint(stmt.getOpBox());
			stmt.getOp().apply(vs);
		}

		/**
		 * Calls the post processors interested in <code>o</code>.  This handles the case when <code>o</code> is of type
		 * <code>ca. mcgill.sable.soot.Jimple.Stmt</code>.
		 *
		 * @param objClass the class of <code>o</code>.
		 * @param o the AST Node to be processed.
		 */
		private void defaultCase(Class objClass, Object o) {
			Collection processors = (Collection) class2processors.get(objClass);

			if(processors != null) {
				Stmt stmt = (Stmt) o;

				for(Iterator i = processors.iterator(); i.hasNext();) {
					Processor pp = (Processor) i.next();
					pp.callback(stmt, context);
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class ValueSwitcher
	  extends AbstractJimpleValueSwitch {
		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
			defaultCase(InterfaceInvokeExpr.class, v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewArrayExpr(ca.mcgill.sable.soot.jimple.NewArrayExpr)
		 */
		public void caseNewArrayExpr(NewArrayExpr v) {
			defaultCase(NewArrayExpr.class, v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewExpr(ca.mcgill.sable.soot.jimple.NewExpr)
		 */
		public void caseNewExpr(NewExpr v) {
			defaultCase(NewExpr.class, v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewMultiArrayExpr(ca.mcgill.sable.soot.jimple.NewMultiArrayExpr)
		 */
		public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
			defaultCase(NewMultiArrayExpr.class, v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseSpecialInvokeExpr(ca.mcgill.sable.soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
			defaultCase(SpecialInvokeExpr.class, v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseStaticInvokeExpr(ca.mcgill.sable.soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(StaticInvokeExpr v) {
			defaultCase(StaticInvokeExpr.class, v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseVirtualInvokeExpr(ca.mcgill.sable.soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
			defaultCase(VirtualInvokeExpr.class, v);
		}

		/**
		 * Calls the post processors interested in <code>o</code>.  This handles the case when <code>o</code> is of type
		 * <code>ca.mcgill.sable.soot.Jimple.Value</code>.
		 *
		 * @param objClass the class of <code>o</code>.
		 * @param o the AST node to be processed.
		 */
		private void defaultCase(Class objClass, Object o) {
			Collection processors = (Collection) class2processors.get(objClass);

			if(processors != null) {
				Value value = (Value) o;

				for(Iterator i = processors.iterator(); i.hasNext();) {
					Processor pp = (Processor) i.next();
					pp.callback(value, context);
				}
			}
		}
	}

	/**
	 * Sets the analyzer which provides the information to be post processed.
	 *
	 * @param analyzer an instance of the BFA.
	 */
	public void setAnalyzer(AbstractAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * Controls the post processing activity.
	 */
	public void process() {
		for(Iterator i = processors.iterator(); i.hasNext();) {
			Processor pp = (Processor) i.next();
			pp.setAnalyzer(analyzer);
		}
		processClasses(analyzer.getClasses());

		for(Iterator i = processors.iterator(); i.hasNext();) {
			((Processor) i.next()).consolidate();
		}
	}

	/**
	 * Controls the processing of class level entities.
	 *
	 * @param classes to be processed.
	 */
	public void processClasses(Collection classes) {
		for(Iterator i = classes.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();
			LOGGER.info("Processing class " + sc);

			for(Iterator k = processors.iterator(); k.hasNext();) {
				Processor pp = (Processor) k.next();
				pp.callback(sc);

				for(ca.mcgill.sable.util.Iterator j = sc.getFields().iterator(); j.hasNext();) {
					SootField field = (SootField) j.next();
					pp.callback(field);
				}
			}
			processMethods(Util.convert("java.util.ArrayList", sc.getMethods()));
		}
	}

	/**
	 * Controls the processing of method level entities.
	 *
	 * @param methods to be processed.
	 */
	public void processMethods(Collection methods) {
		Jimple jimple = Jimple.v();

		for(Iterator j = methods.iterator(); j.hasNext();) {
			SootMethod sm = (SootMethod) j.next();
			context.setRootMethod(sm);

			for(Iterator k = processors.iterator(); k.hasNext();) {
				((Processor) k.next()).callback(sm);
			}
			LOGGER.info("Processing Method " + sm);

			try {
				StmtList sl = ((JimpleBody) sm.getBody(jimple)).getStmtList();

				for(ca.mcgill.sable.util.Iterator k = sl.iterator(); k.hasNext();) {
					Stmt stmt = (Stmt) k.next();
					context.setStmt(stmt);
					stmt.apply(stmtSwitcher);
				}
			} catch(Exception e) {
				LOGGER.warn("Well, exception while processing statements of a method may mean the processor does not"
					+ " recognize the given method or it's parts or method has not stored jimple representation. : "
					+ sm.getSignature(), e);
			}
		}
	}

	/**
	 * Registers the interest of <code>processor</code> in processing AST chunk of type <code>interest</code>.
	 *
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of post processor.
	 *
	 * @throws IllegalArgumentException when <code>interest</code> is not of type <code>java.lang.Class</code>.
	 *
	 * @pre interest.oclType = Class
	 */
	public void register(Object interest, Processor processor) {
		if(!(interest instanceof Class)) {
			throw new IllegalArgumentException(
				"In this ProcessingController implementation, interest has to be of type java.lang.Class");
		}

		Class valueClass = (Class) interest;
		Set temp = (Set) class2processors.get(valueClass);

		if(temp == null) {
			temp = new HashSet();
			class2processors.put(valueClass, temp);
		}
		temp.add(processor);

		if(!processors.contains(processor)) {
			processors.add(processor);
		}
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.2  2003/02/21 07:22:22  venku
Changed \@pre to $pre in the ocl constraints specified in Javadoc.

Revision 1.1  2003/02/20 19:19:31  venku
Processing was the general agenda, not post processing.
Post processing was a flavor.  So, changed the post processing
logic to be generic for processing and adaptable when requried.


*****/
