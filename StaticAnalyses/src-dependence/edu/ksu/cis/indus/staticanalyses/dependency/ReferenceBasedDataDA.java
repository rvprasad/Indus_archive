
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;


/**
 * This class provides data dependence information which considers references and hence the effects of aliasing. It is an
 * adapter for an interprocedural use-def analysis which considers the effects of aliasing.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ReferenceBasedDataDA
  extends DependencyAnalysis {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ReferenceBasedDataDA.class);

	/**
	 * This provides inter-procedural use-def information which considers the effects of aliasing.
	 */
	protected IUseDefInfo aliasedUD;

	/**
	 * A cache context object to be used to retrieve information from <code>interProceduralUD</code>.
	 */
	private final Context contextCache = new Context();

	/**
	 * Return the statements on which field/array access in <code>stmt</code> in <code>method</code> depends on.
	 *
	 * @param stmt in which aliased data is read.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which affect the data being read in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(Stmt) and method.isOclKindOf(SootMethod)
	 * @post result.oclIsKindOf(Pair(AssignStmt, SootMethod))
	 * @post result->forall(o | o.getFirst().getLeftOf().oclIsKindOf(FieldRef) or
	 * 		 o.getFirst().getLeftOf().oclIsKindOf(ArrayRef))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object stmt, final Object method) {
		contextCache.setRootMethod((SootMethod) method);

		Collection _result = Collections.EMPTY_LIST;
		final Stmt _theStmt = (Stmt) stmt;

		if (_theStmt.containsArrayRef() || _theStmt.containsFieldRef()) {
			contextCache.setRootMethod((SootMethod) method);
			_result = aliasedUD.getDefs(_theStmt, contextCache);
		}
		return _result;
	}

	/**
	 * Return the statements which depend on the field/array access in <code>stmt</code> in <code>method</code>.
	 *
	 * @param stmt in which aliased data is written.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which are affectted by the data write in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(AssignStmt) and method.isOclKindOf(SootMethod)
	 * @post result.oclIsKindOf(Pair(AssignStmt, SootMethod))
	 * @post result->forall(o | o.getFirst().getRightOp().oclIsKindOf(FieldRef) or
	 * 		 o.getFirst().getRightOp().oclIsKindOf(ArrayRef))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependents(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;

		if (stmt instanceof AssignStmt) {
			final AssignStmt _assign = (AssignStmt) stmt;

			if (_assign.containsArrayRef() || _assign.containsFieldRef()) {
				contextCache.setRootMethod((SootMethod) method);
				_result = aliasedUD.getUses((DefinitionStmt) stmt, contextCache);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getId()
	 */
	public Object getId() {
		return DependencyAnalysis.REFERENCE_BASED_DATA_DA;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return aliasedUD.isStable();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Reference Based Data Dependence processing");
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Reference Based Data Dependence processing");
		}
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Reference-based Data dependence as calculated by " + this.getClass().getName()
				+ "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependentMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final List _stmts = getStmtList((SootMethod) _entry.getKey());
			int _count = 0;

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Collection _c = (Collection) _j.next();
				final Stmt _stmt = (Stmt) _stmts.get(_count++);

				for (final Iterator _k = _c.iterator(); _k.hasNext();) {
					_temp.append("\t\t" + _stmt + " <-- " + _k.next() + "\n");
				}
				_localEdgeCount += _c.size();
			}
			_result.append("\tFor " + _entry.getKey() + " there are " + _localEdgeCount
				+ " Reference-based Data dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " Reference-based Data dependence edges exist.");
		return _result.toString();
	}

	/**
	 * Extracts information provided by environment at initialization time.
	 *
	 * @throws InitializationException if an implementation that provides aliased interprocedural use-def information is not
	 * 		   provided.
	 *
	 * @pre info.get(IUseDefInfo.ID) != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		aliasedUD = (IUseDefInfo) info.get(IUseDefInfo.ID);

		if (aliasedUD == null) {
			throw new InitializationException(IUseDefInfo.ID + " was not provided.");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.18  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.17  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.16  2003/11/25 19:03:40  venku
   - added more stringent tests to check if the underlying
     info should be used.
   Revision 1.15  2003/11/16 18:41:03  venku
   - incorrect ID was returned.  FIXED.
   Revision 1.14  2003/11/12 03:56:32  venku
   - requires DefinitionStmt as input for getDependents()
   Revision 1.13  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.12  2003/11/10 02:26:29  venku
   - coding convention.
   Revision 1.11  2003/11/10 02:24:30  venku
   - coding convention.
   Revision 1.10  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.9  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.8  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.7  2003/09/14 23:29:32  venku
   - deferred status changes to contained AliasedUseDefInfo analysis.
   Revision 1.6  2003/09/12 22:33:09  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.5  2003/08/25 09:30:41  venku
   Renamed AliasedDataDA to ReferenceBasedDataDA.
   Renamed NonAliasedDataDA to IdentifierBasedDataDA.
   Renamed the IDs for the above analyses.
   Revision 1.4  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.3  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.1  2003/08/09 23:29:09  venku
   Renamed InterProceduralDataDAv1 to AliasedDataDA
   Renamed IntraProceduralDataDA to NonAliasedDataDA
 */
