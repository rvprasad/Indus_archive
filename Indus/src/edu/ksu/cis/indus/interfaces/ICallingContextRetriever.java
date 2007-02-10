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

package edu.ksu.cis.indus.interfaces;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.Collections;

/**
 * This is the interface to retrieve calling contexts based on program points.
 * <ul>
 * <li>A <code>null</code> calling context means all call chains from the method should be considered.</li>
 * <li>A <code>null</code> value in the calling context indicates that the context should be considered as terminating.
 * Also, a <code>null</code> value may only occur at the bottom of the stack.</li>
 * <ul>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ICallingContextRetriever
		extends IIDBasedInfoManagement {

	/**
	 * This enum type defines values used to idenfity values in the id-based info map.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public enum Identifiers {
		/**
		 * This identifies the calling context leading to the method.
		 */
		SRC_CALLING_CONTEXT,
		/**
		 * This identifies the entity at the program point.
		 */
		SRC_ENTITY,
		/**
		 * This identifies the method enclosing the program point.
		 */
		SRC_METHOD
	}

	/**
	 * This retrives null contexts.
	 */
	ICallingContextRetriever NULL_CONTEXT_RETRIEVER = new ICallingContextRetriever() {

		@Empty public void clearInfo() {
			// does nothing
		}

		public Collection<Stack<CallTriple>> getCallingContextsForProgramPoint(
				@SuppressWarnings("unused") final Context programPointContext) {
			return NULL_CONTEXTS;
		}

		public Collection<Stack<CallTriple>> getCallingContextsForThis(@SuppressWarnings("unused") final Context methodContext) {
			return NULL_CONTEXTS;
		}

		public Object removeInfo(@SuppressWarnings("unused") final Comparable<?> infoID) {
			return null;
		}

		public Object setInfoFor(@SuppressWarnings("unused") final Comparable<?> infoID,
				@SuppressWarnings("unused") final Object info) {
			return null;
		}
	};

	/**
	 * This is a collection of calling contexts that imply all call chains should be followed.
	 */
	Collection<Stack<CallTriple>> NULL_CONTEXTS = Collections.<Stack<CallTriple>> singleton(null);;

	/**
	 * Retrieves the calling contexts for the program point specified in the given context.
	 * 
	 * @param programPointContext of interest.
	 * @return a collection of calling contexts for the given program point.
	 * @pre programPointContext != null
	 * @post result != null
	 */
	Collection<Stack<CallTriple>> getCallingContextsForProgramPoint(Context programPointContext);

	/**
	 * Retrieves the calling contexts for the method specified in the given context based on it's "this" variable.
	 * 
	 * @param methodContext of interest.
	 * @return a collection of calling contexts for the given method based on it's "this" variable.
	 * @pre methodContext != null
	 * @post result != null
	 */
	Collection<Stack<CallTriple>> getCallingContextsForThis(Context methodContext);
}

// End of File
