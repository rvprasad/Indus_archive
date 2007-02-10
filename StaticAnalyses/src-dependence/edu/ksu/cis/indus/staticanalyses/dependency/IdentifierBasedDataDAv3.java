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

package edu.ksu.cis.indus.staticanalyses.dependency;


import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysisv2;
import soot.Local;
import soot.SootMethod;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

/**
 * This class provides intraprocedural data dependency information based on identifiers. Local variables in a method enable
 * such dependence. Given a def site, the use site is tracked based on the id being defined and used. Hence, information about
 * field/array access via primaries which are local variables is inaccurate in such a setting, hence, it is not provided by
 * this class. Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
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
	@Override protected IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> getLocalUseDefAnalysis(final SootMethod method) {
		return new LocalUseDefAnalysisv2(getBasicBlockGraph(method));
	}
}

// End of File
