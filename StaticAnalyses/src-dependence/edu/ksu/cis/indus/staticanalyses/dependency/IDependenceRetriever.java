
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

import java.util.Collection;


/**
 * The interaface used to retrieve dependence to calculate indirect dependence from direct dependence.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface IDependenceRetriever {
	/** 
	 * This retriever can be used to retrieve dependence from analysis that return pairs of statement and method when
	 * dependence info is retrieved.
	 */
	public static final IDependenceRetriever PAIR_DEP_RETRIEVER = new PairRetriever();

	/** 
	 * This retriever can be used to retrieve dependence from analysis that return statements when dependence info is
	 * retrieved.
	 */
	public static final IDependenceRetriever STMT_DEP_RETRIEVER = new StmtRetriever();

	/**
	 * Retrieves the dependees based on <code>dependence</code> from <code>da</code>.
	 *
	 * @param da to be used retrieve dependence info
	 * @param dependence that serves as the basis for retrieval.
	 * @param origContext the original context in which retrieval started.  This is required in instances when the context of
	 * 		  dependence information does not change.  For example, control dependence.
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
	 * @param origContext the original context in which retrieval started.  This is required in instances when the context of
	 * 		  dependence information does not change.  For example, control dependence.
	 *
	 * @return a collection of dependence.
	 *
	 * @pre da != null and dependence != null
	 * @post result != null
	 */
	Collection getDependents(final IDependencyAnalysis da, final Object dependence, final Object origContext);
}

/*
   ChangeLog:
   $Log$
 */
