
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

/**
 * This interface identifies active objects.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath </a>
 * @author $Author$
 * @version $Revision$
 */
public interface IActivePart {
	/**
	 * This implementation can be used as a component to be responsible for the "active" part of an object.  By default, the
	 * part is in an executable state.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	final class ActivePart
	  implements IActivePart {
		/** 
		 * This indicates if the active object can proceed with processing.
		 */
		private boolean active = true;

		/**
		 * @see IActivePart#activate()
		 */
		public void activate() {
			active = true;
		}

		/**
		 * Checks if the active object can proceed.
		 *
		 * @return <code>true</code> if the active object can proceed; <code>false</code>, otherwise.
		 */
		public boolean canProceed() {
			return active;
		}

		/**
		 * @see IActivePart#deactivate()
		 */
		public void deactivate() {
			active = false;
		}
	}

	/**
	 * Activates the active object.  Any subsequent executions will be proceed.
	 */
	void activate();

	/**
	 * Inactivates the active object.  Any current execution will be aborted.
	 */
	void deactivate();
}

// End of File
