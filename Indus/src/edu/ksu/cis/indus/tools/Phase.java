
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

package edu.ksu.cis.indus.tools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class represents a phase.  The tools/analyses/processes are expected to run in phases.  In particular, the execution
 * can proceed in major phases which consist of 0 or many minor phases.  This mode of execution can be tracked by instances
 * of this class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Phase
  implements Cloneable {
	/**
	 * This represents the phase in which a tool/analyses/process starts.
	 */
	public static final Phase STARTING_PHASE;

	/**
	 * This represents the phase in which a tool/analyses/process finishes.
	 */
	public static final Phase FINISHED_PHASE;

	static {
		Phase i = new Phase();
		STARTING_PHASE = i;
		i = new Phase();
		i.finished();
		FINISHED_PHASE = i;
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(Phase.class);

	/**
	 * This is the major phase.
	 *
	 * @invariant 0 leq _major
	 */
	private int major;

	/**
	 * This is the minor phase.
	 *
	 * @invariant 0 leq _minor
	 */
	private int minor;

	/**
	 * Creates a new Phase object.
	 */
	private Phase() {
	}

	/**
	 * Creates a new Phase object that represents the starting phase.
	 *
	 * @return an instance of this class.
	 */
	public static Phase createPhase() {
		final Phase _result = new Phase();
		_result.major = 0;
		_result.minor = 0;
		return _result;
	}

	/**
	 * Checks if this object is earlier than the given object.
	 *
	 * @param phase to be compared with this object.
	 *
	 * @return <code>true</code> if this phase object is earlier than the given phase; <code>false</code>, otherwise.
	 *
	 * @pre phase != null
	 */
	public boolean isEarlierThan(final Phase phase) {
		return (major < phase.major) || (major == phase.major && minor < phase.minor);
	}

	/**
	 * Clones this object.
	 *
	 * @return the clone.
	 */
	public Object clone() {
		Phase result = null;

		try {
			result = (Phase) super.clone();
			result.major = major;
			result.minor = minor;
		} catch (CloneNotSupportedException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cloning of phase failed.", e);
			}
		}
		return result;
	}

	/**
	 * Checks if the given object is equal to this object.
	 *
	 * @param o is the object to be checked for equality with this object.
	 *
	 * @return <code>true</code> if this object is equal to <code>o</code>; <code>false</code>, otherwise.
	 */
	public boolean equal(final Object o) {
		boolean result = false;

		if (o != null && o instanceof Phase) {
			final Phase _p = (Phase) o;
			result = equalsMajor(_p) && equalsMinor(_p);
		}
		return result;
	}

	/**
	 * Checks if this instance and the given phase object represent the same major phase.
	 *
	 * @param p is phase with which to compare.
	 *
	 * @return <code>true</code> if both represent the same major phase; <code>false</code>, otherwise.
	 */
	public boolean equalsMajor(final Phase p) {
		return major == p.major;
	}

	/**
	 * Checks if this instance and the given phase object represent the same minor phase.
	 *
	 * @param p is the phase with which to compare.
	 *
	 * @return <code>true</code> if both represent the same minor phase; <code>false</code>, otherwise.
	 */
	public boolean equalsMinor(final Phase p) {
		return minor == p.minor;
	}

	/**
	 * Modifies this instance to represent the finished phase.
	 */
	public void finished() {
		major = Integer.MAX_VALUE;
		minor = Integer.MAX_VALUE;
	}

	/**
	 * Modifies this instance to represent the next major phase.
	 */
	public void nextMajorPhase() {
		major++;
		minor = 0;
	}

	/**
	 * Modifies this instance to represent the next minor phase.
	 */
	public void nextMinorPhase() {
		minor++;
	}

	/**
	 * Resets this instance to represent the starting phase.
	 */
	public void reset() {
		major = 0;
		minor = 0;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/11/18 21:40:00  venku
   - added a new method to check ordering relation between 2 phase object.
   Revision 1.2  2003/09/26 15:00:01  venku
   - The configuration of tools in Indus has been placed in this package.
   - Formatting.
   Revision 1.1  2003/09/26 05:56:10  venku
   - a checkpoint commit.
 */
