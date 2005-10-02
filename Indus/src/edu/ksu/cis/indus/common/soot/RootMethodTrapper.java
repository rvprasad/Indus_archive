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
import java.util.HashSet;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;

/**
 * This class provides the service of trapping the root methods.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class RootMethodTrapper {

	/**
	 * This implementation traps <code>public static void main(java.lang.String[])</code> methods.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static final class MainMethodTrapper
			extends RootMethodTrapper {

		/**
		 * This implementation will only consider <code>public static void main(java.lang.String[])</code> as root methods.
		 * 
		 * @see edu.ksu.cis.indus.common.soot.RootMethodTrapper#isThisARootMethod(soot.SootMethod)
		 */
		@Override protected boolean isThisARootMethod(final SootMethod sm) {
			final boolean _result;

			if (sm.getName().equals("main") && sm.isPublic() && sm.isStatic() && sm.getParameterCount() == 1
					&& sm.getReturnType().equals(VoidType.v())
					&& sm.getParameterType(0).equals(ArrayType.v(RefType.v("java.lang.String"), 1))) {
				_result = true;
			} else {
				_result = false;
			}
			return _result;
		}
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RootMethodTrapper.class);

	/**
	 * The collection of regular expression that is used to match classes which may contain root methods.
	 */
	private final Collection<Pattern> rootClassNamePatterns = new HashSet<Pattern>();

	/**
	 * The collection of regular expression that is used to match methods which should be root methods.
	 */
	private final Collection<Pattern> rootMethodSignaturePatterns = new HashSet<Pattern>();

	/**
	 * The FQNs of the classes which can contribute entry points.
	 */
	private final Collection<String> theClassNames = new HashSet<String>();

	/**
	 * Retrieves the string patterns of the admissible root class names.
	 * 
	 * @return a collection of string patterns.
	 * @post result != null
	 */
	public Collection<String> getClassNamePatterns() {
		final Collection<String> _result = new ArrayList<String>();

		for (final Iterator<Pattern> _i = rootClassNamePatterns.iterator(); _i.hasNext();) {
			final Pattern _pattern = _i.next();
			_result.add(_pattern.pattern());
		}
		return _result;
	}

	/**
	 * Retrieves the FQNs of the admissible root class.
	 * 
	 * @return a collection of string patterns.
	 * @post result != null
	 */
	public Collection<String> getClassNames() {
		return new ArrayList<String>(theClassNames);
	}

	/**
	 * Retrieves the string patterns of admissible root method signatures.
	 * 
	 * @return a collection of string patterns.
	 * @post result != null
	 */
	public Collection<String> getMethodSignaturePatterns() {
		final Collection<String> _result = new ArrayList<String>();

		for (final Iterator<Pattern> _i = rootMethodSignaturePatterns.iterator(); _i.hasNext();) {
			final Pattern _pattern = _i.next();
			_result.add(_pattern.pattern());
		}
		return _result;
	}

	/**
	 * Set the string patterns of root class names. Only root classes can contain root methods.
	 * 
	 * @param namePatterns are patterns specified as regular expressions.
	 * @pre names != null
	 */
	public void setClassNamePatterns(final Collection<String> namePatterns) {
		rootClassNamePatterns.clear();

		for (final Iterator<String> _i = namePatterns.iterator(); _i.hasNext();) {
			final String _patternString = _i.next();

			try {
				final Pattern _pattern = Pattern.compile(_patternString);
				rootClassNamePatterns.add(_pattern);
			} catch (final PatternSyntaxException _e) {
				LOGGER.error(_patternString + " was an invalid regular expression.  Ignored.", _e);
			}
		}
	}

	/**
	 * Set the FQNs of the admissible root classes.
	 * 
	 * @param names is FQNs of application / root classes.
	 * @pre names != null
	 */
	public void setClassNames(final Collection<String> names) {
		theClassNames.clear();
		theClassNames.addAll(names);
	}

	/**
	 * Set the string patterns of admissible root method signatures. Only root classes can contain root methods.
	 * 
	 * @param namePatterns are patterns specified as regular expressions.
	 * @pre names != null
	 */
	public void setMethodSignaturePatterns(final Collection<String> namePatterns) {
		rootMethodSignaturePatterns.clear();

		for (final Iterator<String> _i = namePatterns.iterator(); _i.hasNext();) {
			final String _patternString = _i.next();

			try {
				final Pattern _pattern = Pattern.compile(_patternString);
				rootMethodSignaturePatterns.add(_pattern);
			} catch (final PatternSyntaxException _e) {
				LOGGER.error(_patternString + " was an invalid regular expression.  Ignored.", _e);
			}
		}
	}

	/**
	 * Checks if the given class is a root class (if it can contribute root methods). This implementation considers the given
	 * class as root class ff its name matches the pattern provided to identify the root class. If the patterns and class
	 * names are unspecified then this implementation will consider the given class as a root class.
	 * 
	 * @param sc is the class to check.
	 * @return <code>true</code> if <code>sc</code> should be examined for possible root method contribution;
	 *         <code>false</code>, otherwise.
	 * @pre sc != null
	 */
	protected boolean considerClassForEntryPoint(final SootClass sc) {
		boolean _result = rootClassNamePatterns.isEmpty() && theClassNames.isEmpty();
		final String _scName = sc.getName();
		final Iterator<Pattern> _i = rootClassNamePatterns.iterator();
		final int _iEnd = rootClassNamePatterns.size();

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final Pattern _pattern = _i.next();
			_result = _pattern.matcher(_scName).matches();
		}

		final Iterator<String> _j = theClassNames.iterator();
		final int _jEnd = theClassNames.size();

		for (int _jIndex = 0; _jIndex < _jEnd && !_result; _jIndex++) {
			final String _name = _j.next();
			_result = _name.equals(_scName);
		}

		return _result;
	}

	/**
	 * Checks if the given method qualifies as a root/entry method in the given system. This implementation considers the
	 * given method as root method if its name matches the pattern provided to identify the root method. If the pattern is
	 * unspecified then this implementation will consider the given method as a root/entry method.
	 * 
	 * @param sm is the method that may be an entry point into the system.
	 * @return <code>true</code> if <code>_sm</code> should be considered as a root method; <code>false</code>,
	 *         otherwise.
	 * @pre sm != null
	 */
	protected boolean isThisARootMethod(final SootMethod sm) {
		boolean _result = rootMethodSignaturePatterns.isEmpty();
		final Iterator<Pattern> _i = rootMethodSignaturePatterns.iterator();
		final int _iEnd = rootMethodSignaturePatterns.size();

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final Pattern _pattern = _i.next();
			final String _signature = sm.getSignature();
			_result = _pattern.matcher(_signature).matches();
		}

		return _result;
	}
}

// End of File
