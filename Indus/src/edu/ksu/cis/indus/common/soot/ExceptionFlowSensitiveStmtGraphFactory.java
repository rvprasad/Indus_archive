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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import soot.jimple.JimpleBody;

/**
 * This class provides <code>ExceptionFlowSensitiveStmtGraph</code>s.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExceptionFlowSensitiveStmtGraphFactory
		extends AbstractStmtGraphFactory<ExceptionFlowSensitiveStmtGraph> {

	/**
	 * The collection of exception names that are relevant while dealing with synchronization constructs.
	 */
	public static final Collection<String> SYNC_RELATED_EXCEPTIONS = Collections.singleton("java.lang.Throwable");

	/**
	 * The names of the exceptions via which the control flow should be ignored.
	 */
	@NonNullContainer @NonNull private Collection<String> exceptionsToIgnore = new ArrayList<String>();

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
	 * @pre namesOfExceptionToIgnore->forall(o | ClassLoader.getSystemClassLoader().loadClass(o) != null)
	 */
	public ExceptionFlowSensitiveStmtGraphFactory(
			@NonNull @NonNullContainer final Collection<String> namesOfExceptionToIgnore,
			final boolean dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock) {
		flag = dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock;
		exceptionsToIgnore.addAll(namesOfExceptionToIgnore);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override protected ExceptionFlowSensitiveStmtGraph getStmtGraphForBody(@NonNull final JimpleBody body) {
		return new ExceptionFlowSensitiveStmtGraph(body, exceptionsToIgnore, flag);
	}
}

// End of File
