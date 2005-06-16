
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;

import soot.SootMethod;


/**
 * This class provides implementation of <code>IReadWriteInfo</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class ReadWriteInfo
  extends AbstractStatus
  implements IReadWriteInfo {

    /**
     * The logger used by instances of this class to log messages.
     */
    private static final Log LOGGER = LogFactory.getLog(ReadWriteInfo.class);

	/** 
	 * This is used to retrieve the alias set for "this" from a given method context.
	 */
	private static final Transformer THIS_ALIAS_SET_RETRIEVER =
		new Transformer() {
			public Object transform(final Object input) {
				return ((MethodContext) input).thisAS;
			}
		};

	/** 
	 * The creating/containing object.
	 */
	final EquivalenceClassBasedEscapeAnalysis analysis;

	/** 
	 * This retrieves the method context of a method.
	 */
	private final Transformer methodCtxtRetriever =
		new Transformer() {
			public Object transform(final Object input) {
				final Triple _t = (Triple) analysis.method2Triple.get(input);
				return _t != null ? _t.getFirst()
								  : null;
			}
		};

	/**
	 * Creates an instance of this class.
	 *
	 * @param instance that creates this instance.
	 *
	 * @pre instance != null
	 */
	ReadWriteInfo(final EquivalenceClassBasedEscapeAnalysis instance) {
		this.analysis = instance;
	}

	/**
	 * @see IReadWriteInfo#isArgumentBasedAccessPathRead(edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple, int,
	 * 		java.lang.String[], boolean)
	 */
	public boolean isArgumentBasedAccessPathRead(final CallTriple callerTriple, final int argPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException {
		final SootMethod _callee = callerTriple.getExpr().getMethod();

		this.analysis.validate(argPos, _callee);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(this.analysis.new SiteContextRetriever(callerTriple),
				this.analysis.new ArgParamAliasSetRetriever(argPos));
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IReadWriteInfo#isArgumentBasedAccessPathWritten(CallTriple, int, String[],     boolean)
	 */
	public boolean isArgumentBasedAccessPathWritten(final CallTriple callerTriple, final int argPos,
		final String[] accesspath, final boolean recurse) {
		final SootMethod _callee = callerTriple.getExpr().getMethod();

		this.analysis.validate(argPos, _callee);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(this.analysis.new SiteContextRetriever(callerTriple),
				this.analysis.new ArgParamAliasSetRetriever(argPos));
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IReadWriteInfo.ID);
	}

	/**
	 * @see IReadWriteInfo#isParameterBasedAccessPathRead(soot.SootMethod, int, java.lang.String[], boolean)
	 */
	public boolean isParameterBasedAccessPathRead(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException {
		this.analysis.validate(paramPos, method);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(methodCtxtRetriever, this.analysis.new ArgParamAliasSetRetriever(paramPos));
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IReadWriteInfo#isParameterBasedAccessPathWritten(SootMethod, int, String[], boolean)
	 */
	public boolean isParameterBasedAccessPathWritten(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse) {
		this.analysis.validate(paramPos, method);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(methodCtxtRetriever, this.analysis.new ArgParamAliasSetRetriever(paramPos));
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, false);
	}

	/**
	 * @see IReadWriteInfo#isReceiverBasedAccessPathRead(CallTriple,     java.lang.String[], boolean)
	 */
	public boolean isReceiverBasedAccessPathRead(final CallTriple callerTriple, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException {
		if (callerTriple.getExpr().getMethod().isStatic()) {
			throw new IllegalArgumentException("The invoked method should be non-static.");
		}

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(this.analysis.new SiteContextRetriever(callerTriple),
				ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IReadWriteInfo#isReceiverBasedAccessPathWritten(CallTriple, String[], boolean)
	 */
	public boolean isReceiverBasedAccessPathWritten(final CallTriple callerTriple, final String[] accesspath,
		final boolean recurse) {
		if (callerTriple.getExpr().getMethod().isStatic()) {
			throw new IllegalArgumentException("The invoked method should be non-static.");
		}

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(this.analysis.new SiteContextRetriever(callerTriple),
				ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IReadWriteInfo#isThisBasedAccessPathRead(soot.SootMethod, java.lang.String[],
	 * 		boolean)
	 */
	public boolean isThisBasedAccessPathRead(final SootMethod method, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException {
		this.analysis.validate(method);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(methodCtxtRetriever, ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IReadWriteInfo#isThisBasedAccessPathWritten(SootMethod, String[], boolean)
	 */
	public boolean isThisBasedAccessPathWritten(final SootMethod method, final String[] accesspath, final boolean recurse) {
		this.analysis.validate(method);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(methodCtxtRetriever, ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, false);
	}

	/**
	 * @see IReadWriteInfo#doesInvocationReadGlobalData(CallTriple)
	 */
	public boolean doesInvocationReadGlobalData(final CallTriple callerTriple) {
		final SootMethod _caller = callerTriple.getMethod();
		return globalDataReadWriteInfoHelper(_caller, this.analysis.new SiteContextRetriever(callerTriple), true);
	}

	/**
	 * @see IReadWriteInfo#doesInvocationWriteGlobalData(CallTriple)
	 */
	public boolean doesInvocationWriteGlobalData(final CallTriple callerTriple) {
		final SootMethod _caller = callerTriple.getMethod();
		return globalDataReadWriteInfoHelper(_caller, this.analysis.new SiteContextRetriever(callerTriple), false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IReadWriteInfo#doesMethodReadGlobalData(soot.SootMethod)
	 */
	public boolean doesMethodReadGlobalData(final SootMethod method) {
		return globalDataReadWriteInfoHelper(method, ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER, true);
	}

	/**
	 * @see IReadWriteInfo#doesMethodWriteGlobalData(SootMethod)
	 */
	public boolean doesMethodWriteGlobalData(final SootMethod method) {
		return globalDataReadWriteInfoHelper(method, ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER, false);
	}

	/**
	 * This exposes <code>super.stable</code>.
	 */
	void stableAdapter() {
		super.stable();
	}

	/**
	 * This exposes <code>super.unstable</code>.
	 */
	void unstableAdapter() {
		super.unstable();
	}

	/**
	 * Checks if the given method either reads or writes global data.
	 *
	 * @param method of interest.
	 * @param retriever to be used.
	 * @param read <code>true</code> indicates read information is requested; <code>false</code> indidates write  info is
	 * 		  requested.
	 *
	 * @return <code>true</code> if any global data was read when <code>read</code> is <code>true</code> and
	 * 		   <code>false</code> if it was not read when <code>read</code> was <code>true</code>. <code>true</code> if any
	 * 		   global data was written when <code>read</code> is <code>false</code> and <code>false</code> if it was not
	 * 		   written when <code>read</code> was <code> false</code>.
	 *
	 * @pre method != null and retriever != null
	 */
	private boolean globalDataReadWriteInfoHelper(final SootMethod method, final Transformer retriever, final boolean read) {
		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);
		final boolean _result;

		if (_triple != null) {
			final MethodContext _ctxt = (MethodContext) retriever.transform(_triple);

			if (read) {
				_result = _ctxt.isGlobalDataRead();
			} else {
				_result = _ctxt.isGlobalDataWritten();
			}
		} else {
			_result = read ? this.analysis.readDefaultValue
						   : this.analysis.writeDefaultValue;

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method
					+ " is available.  Returning default value - " + _result);
			}
		}
		return _result;
	}

	/**
	 * Calculates the read-write information based on an access path rooted at the given method using the given transformer.
	 *
	 * @param method in which the accesspath is rooted.
	 * @param accesspath of interest.
	 * @param recurse <code>true</code> indicates that read/write beyond the end point should be considered.
	 * 		  <code>false</code>, otherwise.
	 * @param retriever to be used to get the method context and the alias set.
	 * @param read <code>true</code> indicates read information is requested; <code>false</code> indidates write  info is
	 * 		  requested.
	 *
	 * @return <code>true</code> if the given access path was read when <code>read</code> is <code>true</code> and
	 * 		   <code>false</code> if it was not read when <code>read</code> was <code>true</code>. <code>true</code> if the
	 * 		   given access path was written when <code>read</code> is <code>false</code> and <code>false</code> if it was
	 * 		   not written when <code>read</code> was <code> false</code>.
	 *
	 * @pre method != null and accesspath != null and retriever != null
	 */
	private boolean instanceDataReadWriteHelper(final SootMethod method, final String[] accesspath, final boolean recurse,
		final Transformer retriever, final boolean read) {
		final AliasSet _aliasSet = (AliasSet) retriever.transform(method);
		final int _pathLength = accesspath.length;
		final boolean _zeroLenghtPath = _pathLength == 0;
		final AliasSet _endPoint;

		if (_aliasSet == null) {
			_endPoint = null;
		} else {
			final String[] _s;

			if (_zeroLenghtPath) {
				_s = new String[0];
			} else {
				_s = new String[_pathLength - 1];
				System.arraycopy(accesspath, 0, _s, 0, _s.length);
			}

			final Pair _pair = new Pair(_aliasSet.find(), _s);

			if (this.analysis.query2handle.containsKey(_pair)) {
				_endPoint = (AliasSet) this.analysis.query2handle.get(_pair);
			} else {
				_endPoint = _aliasSet.getAccessPathEndPoint(_s);
				this.analysis.query2handle.put(_pair, _endPoint);
			}
		}

		final boolean _result;

		if (_endPoint == null) {
			_result = read ? this.analysis.readDefaultValue
						   : this.analysis.writeDefaultValue;

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("isAccessPathOperatedHelper(method = " + method
					+ ", accesspath = " + Arrays.asList(accesspath) + ", recurse = " + recurse + ", retriver = " + retriever
					+ ") - No recorded information for " + method + " is available.  Returning default value - " + _result);
			}
		} else {
			if (read) {
				if (_zeroLenghtPath) {
					_result = _endPoint.isAccessed();
				} else {
					_result = _endPoint.wasFieldRead(accesspath[_pathLength - 1], recurse);
				}
			} else {
				if (_zeroLenghtPath) {
					_result = false;
				} else {
					_result = _endPoint.wasFieldWritten(accesspath[_pathLength - 1], recurse);
				}
			}
		}
		return _result;
	}
}

// End of File
