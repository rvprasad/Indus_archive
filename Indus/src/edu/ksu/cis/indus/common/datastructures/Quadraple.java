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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This class represents a quadraple of objects. The hashcode/stringized rep. of this object is derived from it's
 * constituents.
 * <p>
 * Instances of this class can occur in <i>optimized</i> or <i>unoptimized</i> modes. In optimized mode, the
 * hashcode/stringized rep. are precalculated at creation time or on call to <code>optimize()</code>. Hence, any future
 * calls to <code>hashCode()</code> and <code>toString()</code> will return this cached copy. In the unoptimized mode, the
 * hashcode/stringized rep. are calculated on the fly upon request. It is possible to toggle an instance between optimized and
 * unoptimized mode.
 * </p>
 * <p>
 * The above feature of this class can lead to a situation where the hashcode of an instance obtained via
 * <code>hashCode()</code> in optimized mode is not equal to the hashcode of the instance if calculated on the fly. This
 * will not affect the equality test of instances rather only the preformance of container classes using these instances as
 * keys.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T1> The type of the first element in this quadraple.
 * @param <T2> The type of the second element in this quadraple.
 * @param <T3> The type of the third element in this quadraple.
 * @param <T4> The type of the fourth element in this quadraple.
 */
public final class Quadraple<T1, T2, T3, T4>
		implements Cloneable {

	/**
	 * The first object in this quadraple.
	 */
	@Immutable protected T1 first;

	/**
	 * The fourth object in this quadraple.
	 */
	@Immutable protected T4 fourth;

	/**
	 * The second object in this quadraple.
	 */
	@Immutable protected T2 second;

	/**
	 * The third object in this quadraple.
	 */
	@Immutable protected T3 third;

	/**
	 * A cached copy of the hash code of this object.
	 */
	private int hashCode;

	/**
	 * A cached copy of the stringized representation of this object.
	 */
	private String str;

	/**
	 * Creates a new optimized Quadraple object.
	 * 
	 * @param firstParam the first object of this quadraple.
	 * @param secondParam the second object of this quadraple.
	 * @param thirdParam the third object of this quadraple
	 * @param fourthParam fourth element of the requested quadraple.
	 */
	public Quadraple(@Immutable final T1 firstParam, @Immutable final T2 secondParam, @Immutable final T3 thirdParam,
			@Immutable final T4 fourthParam) {
		this.first = firstParam;
		this.second = secondParam;
		this.third = thirdParam;
		this.fourth = fourthParam;
	}

	/**
	 * Clones this object. The contents are cloned based on shallow-copying semantics.
	 * 
	 * @return a cloned copy of this quadraple.
	 * @throws CloneNotSupportedException if <code>super.clone()</code> fails.
	 */
	@NonNull @Immutable @Override public Quadraple<T1, T2, T3, T4> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked") final Quadraple<T1, T2, T3, T4> _quadraple = (Quadraple) super.clone();
		return _quadraple;
	}

	/**
	 * Checks if the given object is equal to this quadraple.
	 * 
	 * @param o is the object to be tested for equality with this object.
	 * @return <code>true</code> if <code>o</code> is equal to this quadraple; <code>false</code>, otherwise.
	 * @post result == true implies o.oclTypeOf(Quadraple) and (o.first.equals(first) or o.first == first) and
	 *       (o.second.equals(second) or o.second == second) and (o.third.equals(third) or o.third == third)
	 */
	@Override public boolean equals(@Immutable final Object o) {
		boolean _result = false;

		if (o instanceof Quadraple) {
			final Quadraple<?, ?, ?, ?> _temp = (Quadraple) o;
			_result = (this == o) || ((first == _temp.first) || ((first != null) && first.equals(_temp.first)))
					&& ((second == _temp.second) || ((second != null) && second.equals(_temp.second)))
					&& ((third == _temp.third) || ((third != null) && third.equals(_temp.third)))
					&& ((fourth == _temp.fourth) || ((fourth != null) && fourth.equals(_temp.fourth)));
		}
		return _result;
	}

	/**
	 * Returns the first object in the quadraple.
	 * 
	 * @return the first object in the quadraple.
	 */
	@Functional public T1 getFirst() {
		return first;
	}

	/**
	 * Returns the fourth object in the quadraple.
	 * 
	 * @return the fourth object in the quadraple.
	 */
	@Functional public T4 getFourth() {
		return fourth;
	}

	/**
	 * Returns the second object in the quadraple.
	 * 
	 * @return the second object in the quadraple.
	 */
	@Functional public T2 getSecond() {
		return second;
	}

	/**
	 * Returns the third object in the quadraple.
	 * 
	 * @return the third object in the quadraple.
	 */
	@Functional public T3 getThird() {
		return third;
	}

	/**
	 * Returns the hash code for this quadraple. Depending on how the object was created the cached value or the value
	 * calculated on the fly is returned.
	 * 
	 * @return the hash code of this quadraple.
	 */
	@Override public int hashCode() {
		int _result;

		if (str == null) {
			_result = hash();
		} else {
			_result = hashCode;
		}
		return _result;
	}

	/**
	 * Optimizes this object with regard to hashCode and stringized representation retrival. It (re)calculates the hashcode
	 * and the stringized representation of this object and caches the new values.
	 * 
	 * @post str != null
	 */
	public void optimize() {
		hashCode = hash();
		str = stringize();
	}

	/**
	 * Returns a stringified representation of this object. Depending on how the object was created the cached value or the
	 * value calculated on the fly is returned.
	 * 
	 * @return stringified representation of this object.
	 */
	@NonNull @Override public String toString() {
		String _result;

		if (str == null) {
			_result = stringize();
		} else {
			_result = str;
		}
		return _result;
	}

	/**
	 * Unoptimizes this object with regard to hashCode and stringized representation retrival. It forgets any cached values so
	 * that they calculates on the fly when requested next.
	 * 
	 * @post str == null
	 */
	public void unoptimize() {
		str = null;
	}

	/**
	 * Provides the hashcode of this object.
	 * 
	 * @return the hashcode of this object.
	 */
	protected int hash() {
		int _result = 17;

		if (first != null) {
			_result = 37 * _result + first.hashCode();
		}

		if (second != null) {
			_result = 37 * _result + second.hashCode();
		}

		if (third != null) {
			_result = 37 * _result + third.hashCode();
		}

		if (fourth != null) {
			_result = 37 * _result + fourth.hashCode();
		}
		return _result;
	}

	/**
	 * Provides the stringized representation of this object.
	 * 
	 * @return the stringized representation of this object.
	 */
	@Functional protected String stringize() {
		return ("(" + first + ", " + second + ", " + third + ", " + fourth + ")").intern();
	}
}

// End of File
