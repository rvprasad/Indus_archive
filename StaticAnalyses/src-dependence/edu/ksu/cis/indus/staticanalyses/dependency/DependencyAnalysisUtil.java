
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import soot.jimple.Stmt;


/**
 * This class is a dependence utility class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependencyAnalysisUtil {
	/** 
	 * The collection of analysis identifiers.
	 */
	public static final Collection IDENTIFIERS;

	static {
		final Collection _ids = new ArrayList();
		_ids.add(IDependencyAnalysis.SYNCHRONIZATION_DA);
		_ids.add(IDependencyAnalysis.CONTROL_DA);
		_ids.add(IDependencyAnalysis.REFERENCE_BASED_DATA_DA);
		_ids.add(IDependencyAnalysis.DIVERGENCE_DA);
		_ids.add(IDependencyAnalysis.INTERFERENCE_DA);
		_ids.add(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);
		_ids.add(IDependencyAnalysis.READY_DA);
		IDENTIFIERS = Collections.unmodifiableCollection(_ids);
	}

	/** 
	 * This retrieves should be used if the result of the dependence query is statements. 
	 */
	public static final IDependenceRetriever STMT_RESULT_DEPENDENCE_RETRIEVER = new StmtRetriever();

	/** 
	 * This retrieves should be used if the result of the dependence query is pair consiting of a statement and method.
	 */
	public static final IDependenceRetriever PAIR_RESULT_DEPENDENCE_RETRIEVER = new PairRetriever();

	/// CLOVER:OFF

	/**
	 * Creates a new IDependencyAnalysisHelper object.
	 */
	private DependencyAnalysisUtil() {
	}

	/// CLOVER:ON

	/**
	 * The interaface used to retrieve dependence to calculate indirect dependence from direct dependence.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static interface IDependenceRetriever {
		/**
		 * Retrieves the dependees based on <code>dependence</code> from <code>da</code>.
		 *
		 * @param da to be used retrieve dependence info
		 * @param dependence that serves as the basis for retrieval.
		 * @param origContext the original context in which retrieval started.  This is required in instances when the
		 * 		  context of dependence information does not change.  For example, control dependence.
		 *
		 * @return a collection of dependence.
		 *
		 * @pre da != null and dependence != null
		 * @post result != null
		 */
		Collection getDependees(final IDependencyAnalysis da, final Object dependence, final Object origContext);

		/**
		 * Retrieves the dependents based on <code>dependence</code> from <code>da</code>.
		 *
		 * @param da to be used retrieve dependence info.
		 * @param dependence that serves as the basis for retrieval.
		 * @param origContext the original context in which retrieval started.  This is required in instances when the
		 * 		  context of dependence information does not change.  For example, control dependence.
		 *
		 * @return a collection of dependence.
		 *
		 * @pre da != null and dependence != null
		 * @post result != null
		 */
		Collection getDependents(final IDependencyAnalysis da, final Object dependence, final Object origContext);
	}

	/**
	 * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is
	 * provided in terms of statements and method pair.  Use this in cases such as ready dependence.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class PairRetriever
	  implements IDependenceRetriever {
		/**
		 * @see DependencyAnalysisUtil.IDependenceRetriever#getDependees(IDependencyAnalysis, Object, Object)
		 */
		public Collection getDependees(final IDependencyAnalysis da, final Object dependence, final Object origContext) {
			final Pair _pair = (Pair) dependence;
			return da.getDependees(_pair.getFirst(), _pair.getSecond());
		}

		/**
		 * @see DependencyAnalysisUtil.IDependenceRetriever#getDependents(IDependencyAnalysis, Object, Object)
		 */
		public Collection getDependents(final IDependencyAnalysis da, final Object dependence, final Object origContext) {
			final Pair _pair = (Pair) dependence;
			return da.getDependents(_pair.getFirst(), _pair.getSecond());
		}
	}


	/**
	 * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is
	 * provided in terms of statements and the context does not change from the orignal context.  Use this in cases such as
	 * control dependence.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class StmtRetriever
	  implements IDependenceRetriever {
		/**
		 * @see DependencyAnalysisUtil.IDependenceRetriever#getDependees(IDependencyAnalysis, Object, Object)
		 */
		public Collection getDependees(final IDependencyAnalysis da, final Object dependence, final Object origContext) {
			final Stmt _stmt = (Stmt) dependence;
			return da.getDependees(_stmt, origContext);
		}

		/**
		 * @see DependencyAnalysisUtil.IDependenceRetriever#getDependents(IDependencyAnalysis, Object, Object)
		 */
		public Collection getDependents(final IDependencyAnalysis da, final Object dependence, final Object origContext) {
			final Stmt _stmt = (Stmt) dependence;
			return da.getDependents(_stmt, origContext);
		}
	}

	/**
	 * Retrieves the transitive closure of dependee relation.  <code>context</code> and <code>retriever</code> are dependent
	 * on <code>da</code>.
	 *
	 * @param entity that serves as the seed in the transitive closure calculation.
	 * @param context in which entity occurs.
	 * @param da is the dependence analysis to be used.
	 * @param retriever used to retrieve dependence information from <code>da</code>.
	 *
	 * @return a collection of dependence.  Please refer to the documentation of the implementation class of <code>da</code>.
	 *
	 * @pre entity != null and context != null and da != null and retriever != null
	 */
	public static Collection getDependeeTransitiveClosureOf(final Object entity, final Object context,
		final IDependencyAnalysis da, final IDependenceRetriever retriever) {
		final Collection _result = new HashSet();
		final Collection _temp = new HashSet();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(_temp);
		_wb.addAllWork(da.getDependents(entity, context));

		while (_wb.hasWork()) {
			final Object _dependence = _wb.getWork();
			_wb.addAllWorkNoDuplicates(retriever.getDependees(da, _dependence, context));
		}
		_result.addAll(_temp);
		return _result;
	}

	/**
	 * Retrieves the transitive closure of dependent relation. <code>context</code> and <code>retriever</code> are dependent
	 * on <code>da</code>.
	 *
	 * @param entity that serves as the seed in the transitive closure calculation.
	 * @param context in which entity occurs.
	 * @param da is the dependence analysis to be used.
	 * @param retriever used to retrieve dependence information from <code>da</code>.
	 *
	 * @return a collection of dependence.  Please refer to the documentation of the implementation class of <code>da</code>.
	 *
	 * @pre entity != null and context != null and da != null and retriever != null
	 */
	public static Collection getDependentTransitiveClosureOf(final Object entity, final Object context,
		final IDependencyAnalysis da, final IDependenceRetriever retriever) {
		final Collection _result = new HashSet();
		final Collection _temp = new HashSet();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(_temp);
		_wb.addAllWork(da.getDependents(entity, context));

		while (_wb.hasWork()) {
			final Object _dependence = _wb.getWork();
			_wb.addAllWorkNoDuplicates(retriever.getDependents(da, _dependence, context));
		}
		_result.addAll(_temp);
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/07/21 02:11:43  venku
   - added facility to extract indirect dependence from direct dependence.

   Revision 1.2  2004/05/21 22:30:53  venku
   - documentation.
   Revision 1.1  2004/05/14 09:02:57  venku
   - refactored:
     - The ids are available in IDependencyAnalysis, but their collection is
       available via a utility class, DependencyAnalysisUtil.
     - DependencyAnalysis will have a sanity check via Unit Tests.
   - ripple effect.
 */
