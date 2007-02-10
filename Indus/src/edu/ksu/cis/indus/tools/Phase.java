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

package edu.ksu.cis.indus.tools;

import edu.ksu.cis.indus.annotations.Empty;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a phase. The tools/analyses/processes are expected to run in phases. In particular, the execution can
 * proceed in major phases which consist of 0 or many minor phases. This mode of execution can be tracked by instances of this
 * class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Phase
		implements Cloneable {

	/**
	 * This represents the phase in which a tool/analyses/process finishes.
	 */
	public static final Phase FINISHED_PHASE;

	/**
	 * This represents the phase in which a tool/analyses/process starts.
	 */
	public static final Phase STARTING_PHASE;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Phase.class);

	static {
		Phase _i = new Phase();
		STARTING_PHASE = _i;
		_i = new Phase();
		_i.finished();
		FINISHED_PHASE = _i;
	}

	/**
	 * This is the major phase.
	 * 
	 * @invariant 0 &lt;= _major
	 */
	private int major;

	/**
	 * This is the minor phase.
	 * 
	 * @invariant 0 &lt;= _minor
	 */
	private int minor;

	/**
	 * Creates a new Phase object.
	 */
	@Empty private Phase() {
		// does nothing
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
	 * Clones this object.
	 * 
	 * @return the clone.
	 */
	@Override public Phase clone() {
		Phase _result = null;

		try {
			_result = (Phase) super.clone();
			_result.major = major;
			_result.minor = minor;
		} catch (CloneNotSupportedException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cloning of phase failed.", _e);
			}
		}
		return _result;
	}

	/**
	 * Checks if the given object is equal to this object.
	 * 
	 * @param o is the object to be checked for equality with this object.
	 * @return <code>true</code> if this object is equal to <code>o</code>; <code>false</code>, otherwise.
	 */
	@Override public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof Phase) {
			final Phase _p = (Phase) o;
			_result = equalsMajor(_p) && equalsMinor(_p);
		}
		return _result;
	}

	/**
	 * Checks if this instance and the given phase object represent the same major phase.
	 * 
	 * @param p is phase with which to compare.
	 * @return <code>true</code> if both represent the same major phase; <code>false</code>, otherwise.
	 */
	public boolean equalsMajor(final Phase p) {
		return major == p.major;
	}

	/**
	 * Checks if this instance and the given phase object represent the same minor phase.
	 * 
	 * @param p is the phase with which to compare.
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
	 * @see Object#hashCode()
	 */
	@Override public int hashCode() {
		return major + minor;
	}

	/**
	 * Checks if this object is earlier than the given object.
	 * 
	 * @param phase to be compared with this object.
	 * @return <code>true</code> if this phase object is earlier than the given phase; <code>false</code>, otherwise.
	 * @pre phase != null
	 */
	public boolean isEarlierThan(final Phase phase) {
		return (major < phase.major) || (major == phase.major && minor < phase.minor);
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).append("minor", this.minor).append("major", this.major).toString();
	}
}

// End of File
