
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

import edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysisv2;

import soot.SootMethod;


/**
 * This class provides intraprocedural data dependency information based on identifiers.  Local variables in a method enable
 * such dependence. Given a def site, the use site is tracked based on the id being defined and used. Hence, information
 * about field/array access via primaries which are local variables is inaccurate in such a setting, hence, it is not
 * provided by this class. Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
 * 
 * <p>
 * This implementation is based on <code>edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysisv2</code> class.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class IdentifierBasedDataDAv3
  extends IdentifierBasedDataDAv2 {
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDAv2#getLocalUseDefAnalysis(soot.SootMethod)
	 */
	protected IUseDefInfo getLocalUseDefAnalysis(final SootMethod method) {
		// TODO: Auto-generated method stub
		return new LocalUseDefAnalysisv2(getBasicBlockGraph(method));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/07/21 11:36:26  venku
   - Extended IUseDefInfo interface to provide both local and non-local use def info.
   - ripple effect.
   - deleted ContainmentPredicate.  Instead, used CollectionUtils.containsAny() in
     ECBA and AliasedUseDefInfo analysis.
   - Added new faster implementation of LocalUseDefAnalysisv2
   - Used LocalUseDefAnalysisv2

 */
