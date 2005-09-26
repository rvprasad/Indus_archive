/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common.soot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import soot.jimple.JimpleBody;

import soot.toolkits.graph.UnitGraph;

/**
 * This class provides <code>ExceptionFlowSensitiveStmtGraph</code>s.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExceptionFlowSensitiveStmtGraphFactory
		extends AbstractStmtGraphFactory {

	/**
	 * The collection of exception names that are relevant while dealing with synchronization constructs.
	 */
	public static final Collection<String> SYNC_RELATED_EXCEPTIONS = Collections.singleton("java.lang.Throwable");

	/**
	 * The names of the exceptions via which the control flow should be ignored.
	 * 
	 * @invariant exceptionToIgnore.oclIsKindOf(Collection(String))
	 */
	private Collection<String> exceptionsToIgnore = new ArrayList<String>();

	/**
	 * This flag indicates if the unit graph should be like complete unit graph or like trap unit graph in terms considering
	 * control from the statement before the try block.
	 */
	private final boolean flag;

	/**
	 * Creates a new instance of this class. This is identical to calling <code>new
	 * ExceptionFlowSensitiveStmtGraphFactory(SYNC_RELATED_EXCEPTIONS, true)</code>.
	 */
	public ExceptionFlowSensitiveStmtGraphFactory() {
		this(SYNC_RELATED_EXCEPTIONS, true);
	}

	/**
	 * Creates a new ExceptionFlowSensitiveStmtGraphFactory object.
	 * 
	 * @param namesOfExceptionToIgnore are the fully qualified names of the exceptions that determine the control edges to be
	 *            ignored.
	 * @param dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock <code>true</code> indicates if the edge from the unit
	 *            before the unit that begins the trap protected region to the handler unit should be omitted;
	 *            <code>false</code>, otherwise.
	 * @pre namesOfExceptionToIgnore != null and namesOfExceptionToIgnore.oclIsKindOf(Collection(String))
	 * @pre namesOfExceptionToIgnore->forall(o | ClassLoader.getSystemClassLoader().loadClass(o) != null)
	 */
	public ExceptionFlowSensitiveStmtGraphFactory(final Collection<String> namesOfExceptionToIgnore,
			final boolean dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock) {
		flag = dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock;
		exceptionsToIgnore.addAll(namesOfExceptionToIgnore);
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getStmtGraphForBody(soot.jimple.JimpleBody)
	 */
	@Override protected UnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new ExceptionFlowSensitiveStmtGraph(body, exceptionsToIgnore, flag);
	}
}

// End of File
