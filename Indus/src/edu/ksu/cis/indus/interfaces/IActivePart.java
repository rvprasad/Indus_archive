
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
