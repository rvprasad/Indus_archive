
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;

import edu.ksu.cis.indus.processing.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.jimple.Stmt;


/**
 * The statement visitor class.  This class provides the default implementation for all the statements that need to be dealt
 * at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to extend the
 * class as required.  It extends <code>AbstractJimpleStmtSwitch</code>.
 * 
 * <p>
 * Created: Sun Jan 27 13:28:32 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractStmtSwitch
  extends soot.jimple.AbstractStmtSwitch
  implements IPrototype {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractStmtSwitch.class);

	/**
	 * The LHS expression visitor used to this object to process LHS expressions.
	 */
	protected final AbstractExprSwitch lexpr;

	/**
	 * The RHS expression visitor used to this object to process RHS expressions.
	 */
	protected final AbstractExprSwitch rexpr;

	/**
	 * The context in which this object should process statements.  It is possible for this object to alter the context, but
	 * it should restore it back to it's initial state before returning from it's methods.
	 */
	protected final Context context;

	/**
	 * The method variant in which this visitor is used.
	 */
	protected final MethodVariant method;

	/**
	 * Creates a new <code>AbstractStmtSwitch</code> instance.
	 *
	 * @param m the method variant in which this visitor is used.
	 */
	protected AbstractStmtSwitch(final MethodVariant m) {
		method = m;

		if (m != null) {
			context = m._context;
			lexpr = m._fa.getLHSExpr(this);
			rexpr = m._fa.getRHSExpr(this);
		} else {
			context = null;
			lexpr = null;
			rexpr = null;
		}
	}

	/**
	 * This method is not supproted. To be implemented by subclasses.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * This method is not supproted. To be implemented by subclasses.
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * Handles situations when alien statement types are visited, i.e., there are no instructions available on how to handle
	 * a particular statement type.
	 *
	 * @param o the statement to be visited.
	 *
	 * @pre o != null
	 */
	public void defaultCase(final Object o) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(o + " is not handled.");
		}
	}

	/**
	 * Process the given statement.  The usual implementation would be visit the expressions in the statement.
	 *
	 * @param stmtToProcess the statement being visited or to be processed.
	 *
	 * @pre stmtToProcess != null
	 */
	protected void process(final Stmt stmtToProcess) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmtToProcess);
		}

		Stmt temp = context.setStmt(stmtToProcess);
		stmtToProcess.apply(this);
		context.setStmt(temp);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmtToProcess);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.8  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/21 10:22:24  venku
   Well, the constructor and the specs would not allow few null arguments
   which were need for prototyping.  Fixed it.
   Revision 1.4  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.2  2003/08/12 18:40:11  venku
   Ripple effect of moving IPrototype to Indus.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
