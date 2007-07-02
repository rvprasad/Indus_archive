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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.collections.ChainedTransformer;
import edu.ksu.cis.indus.common.collections.ITransformer;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis.SiteContextRetriever;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadWriteInfo.class);

	/**
	 * This is used to retrieve the alias set for "this" from a given method context.
	 */
	private static final ITransformer<MethodContext, AliasSet> THIS_ALIAS_SET_RETRIEVER = new ITransformer<MethodContext, AliasSet>() {

		public AliasSet transform(final MethodContext input) {
			return input.thisAS;
		}
	};

	/**
	 * The creating/containing object.
	 */
	final EquivalenceClassBasedEscapeAnalysis analysis;

	/**
	 * This is the default verdict for access-path based read queries.
	 */
	boolean readDefaultValue;

	/**
	 * This is the default verdict for access-path based write queries.
	 */
	boolean writeDefaultValue;

	/**
	 * This retrieves the method context of a method.
	 */
	private final ITransformer<SootMethod, MethodContext> methodCtxtRetriever = new ITransformer<SootMethod, MethodContext>() {

		public MethodContext transform(final SootMethod input) {
			final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _t = analysis.method2Triple
					.get(input);
			return _t != null ? _t.getFirst() : null;
		}
	};

	/**
	 * Creates an instance of this class.
	 * 
	 * @param instance that creates this instance.
	 * @pre instance != null
	 */
	ReadWriteInfo(final EquivalenceClassBasedEscapeAnalysis instance) {
		this.analysis = instance;
		readDefaultValue = true;
		writeDefaultValue = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canMethodInduceSharingBetweenArgAndReceiver(final CallTriple callerTriple, final int argPos)
			throws IllegalArgumentException {
		final SootMethod _method = callerTriple.getMethod();
		analysis.validate(argPos, _method);

		final SiteContextRetriever _siteContextRetriever = this.analysis.new SiteContextRetriever(callerTriple);
		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				this.analysis.new ArgParamAliasSetRetriever(argPos))).transform(_method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER)).transform(_method);

		return checkForObjectSharingBetween(_as1, _as2);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canMethodInduceSharingBetweenArgs(final CallTriple callerTriple, final int argPos1, final int argPos2)
			throws IllegalArgumentException {
		final SootMethod _method = callerTriple.getMethod();
		analysis.validate(argPos1, _method);
		analysis.validate(argPos2, _method);

		final SiteContextRetriever _siteContextRetriever = this.analysis.new SiteContextRetriever(callerTriple);
		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				this.analysis.new ArgParamAliasSetRetriever(argPos1))).transform(_method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				this.analysis.new ArgParamAliasSetRetriever(argPos2))).transform(_method);

		return checkForObjectSharingBetween(_as1, _as2);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canMethodInduceSharingBetweenParamAndThis(final SootMethod method, final int paramPos)
			throws IllegalArgumentException {
		analysis.validate(method);
		analysis.validate(paramPos, method);

		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				this.analysis.new ArgParamAliasSetRetriever(paramPos))).transform(method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER)).transform(method);

		return checkForObjectSharingBetween(_as1, _as2);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canMethodInduceSharingBetweenParams(final SootMethod method, final int paramPos1, final int paramPos2)
			throws IllegalArgumentException {
		analysis.validate(paramPos1, method);
		analysis.validate(paramPos2, method);

		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				this.analysis.new ArgParamAliasSetRetriever(paramPos1))).transform(method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				this.analysis.new ArgParamAliasSetRetriever(paramPos2))).transform(method);

		return checkForObjectSharingBetween(_as1, _as2);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#doesInvocationReadGlobalData(CallTriple)
	 */
	public boolean doesInvocationReadGlobalData(final CallTriple callerTriple) {
		final SootMethod _caller = callerTriple.getMethod();
		return globalDataReadWriteInfoHelper(_caller, this.analysis.new SiteContextRetriever(callerTriple), true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#doesInvocationWriteGlobalData(CallTriple)
	 */
	public boolean doesInvocationWriteGlobalData(final CallTriple callerTriple) {
		final SootMethod _caller = callerTriple.getMethod();
		return globalDataReadWriteInfoHelper(_caller, this.analysis.new SiteContextRetriever(callerTriple), false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean doesMethodInduceSharingBetweenAccesspaths(final CallTriple callerTriple, final int argPos1,
			final String[] accesspath1, final int argPos2, final String[] accesspath2) throws IllegalArgumentException {
		final SootMethod _method = callerTriple.getMethod();

		analysis.validate(argPos1, _method);
		analysis.validate(argPos2, _method);

		final SiteContextRetriever _siteContextRetriever = this.analysis.new SiteContextRetriever(callerTriple);
		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				this.analysis.new ArgParamAliasSetRetriever(argPos1))).transform(_method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				this.analysis.new ArgParamAliasSetRetriever(argPos2))).transform(_method);

		return checkForObjectSharingViaAccessPaths(_as1, accesspath1, _as2, accesspath2);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean doesMethodInduceSharingBetweenAccesspaths(final CallTriple callerTriple, final int argPos,
			final String[] argAccesspath, final String[] receiverBasedAccesspath) throws IllegalArgumentException {
		final SootMethod _method = callerTriple.getMethod();
		if (_method.isStatic()) {
			throw new IllegalArgumentException("Call site should invoke an instance method.");
		}

		analysis.validate(argPos, _method);

		final SiteContextRetriever _siteContextRetriever = this.analysis.new SiteContextRetriever(callerTriple);
		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				this.analysis.new ArgParamAliasSetRetriever(argPos))).transform(_method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(_siteContextRetriever,
				ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER)).transform(_method);

		return checkForObjectSharingViaAccessPaths(_as1, argAccesspath, _as2, receiverBasedAccesspath);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean doesMethodInduceSharingBetweenAccesspaths(final SootMethod method, final int paramPos1,
			final String[] accesspath1, final int paramPos2, final String[] accesspath2) throws IllegalArgumentException {
		analysis.validate(method);
		analysis.validate(paramPos1, method);
		analysis.validate(paramPos2, method);

		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				this.analysis.new ArgParamAliasSetRetriever(paramPos1))).transform(method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				this.analysis.new ArgParamAliasSetRetriever(paramPos2))).transform(method);

		return checkForObjectSharingViaAccessPaths(_as1, accesspath1, _as2, accesspath2);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean doesMethodInduceSharingBetweenAccesspaths(final SootMethod method, final int paramPos,
			final String[] paramAccesspath, final String[] thisBasedAccesspath) throws IllegalArgumentException {
		if (method.isStatic()) {
			throw new IllegalArgumentException("The method should invoke an instance method.");
		}

		analysis.validate(paramPos, method);

		final AliasSet _as1 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				this.analysis.new ArgParamAliasSetRetriever(paramPos))).transform(method);
		final AliasSet _as2 = (new ChainedTransformer<SootMethod, MethodContext, AliasSet>(methodCtxtRetriever,
				ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER)).transform(method);

		return checkForObjectSharingViaAccessPaths(_as1, paramAccesspath, _as2, thisBasedAccesspath);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.interfaces.IReadWriteInfo#doesMethodReadGlobalData(soot.SootMethod)
	 */
	public boolean doesMethodReadGlobalData(final SootMethod method) {
		return globalDataReadWriteInfoHelper(method, methodCtxtRetriever, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#doesMethodWriteGlobalData(SootMethod)
	 */
	public boolean doesMethodWriteGlobalData(final SootMethod method) {
		return globalDataReadWriteInfoHelper(method, methodCtxtRetriever, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<? extends Comparable<?>> getIds() {
		return Collections.singleton(IReadWriteInfo.ID);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isArgumentBasedAccessPathRead(edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple, int,
	 *      java.lang.String[], boolean)
	 */
	public boolean isArgumentBasedAccessPathRead(final CallTriple callerTriple, final int argPos, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException {
		final SootMethod _callee = callerTriple.getExpr().getMethod();

		this.analysis.validate(argPos, _callee);

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				this.analysis.new SiteContextRetriever(callerTriple), this.analysis.new ArgParamAliasSetRetriever(argPos));
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isArgumentBasedAccessPathWritten(CallTriple, int, String[], boolean)
	 */
	public boolean isArgumentBasedAccessPathWritten(final CallTriple callerTriple, final int argPos,
			final String[] accesspath, final boolean recurse) {
		final SootMethod _callee = callerTriple.getExpr().getMethod();

		this.analysis.validate(argPos, _callee);

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				this.analysis.new SiteContextRetriever(callerTriple), this.analysis.new ArgParamAliasSetRetriever(argPos));
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isParameterBasedAccessPathRead(soot.SootMethod, int, java.lang.String[], boolean)
	 */
	public boolean isParameterBasedAccessPathRead(final SootMethod method, final int paramPos, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException {
		this.analysis.validate(paramPos, method);

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				methodCtxtRetriever, this.analysis.new ArgParamAliasSetRetriever(paramPos));
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isParameterBasedAccessPathWritten(SootMethod, int, String[], boolean)
	 */
	public boolean isParameterBasedAccessPathWritten(final SootMethod method, final int paramPos, final String[] accesspath,
			final boolean recurse) {
		this.analysis.validate(paramPos, method);

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				methodCtxtRetriever, this.analysis.new ArgParamAliasSetRetriever(paramPos));
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isReceiverBasedAccessPathRead(CallTriple, java.lang.String[], boolean)
	 */
	public boolean isReceiverBasedAccessPathRead(final CallTriple callerTriple, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException {
		if (callerTriple.getExpr().getMethod().isStatic()) {
			throw new IllegalArgumentException("The invoked method should be non-static.");
		}

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				this.analysis.new SiteContextRetriever(callerTriple), ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isReceiverBasedAccessPathWritten(CallTriple, String[], boolean)
	 */
	public boolean isReceiverBasedAccessPathWritten(final CallTriple callerTriple, final String[] accesspath,
			final boolean recurse) {
		if (callerTriple.getExpr().getMethod().isStatic()) {
			throw new IllegalArgumentException("The invoked method should be non-static.");
		}

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				this.analysis.new SiteContextRetriever(callerTriple), ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.interfaces.IReadWriteInfo#isThisBasedAccessPathRead(soot.SootMethod, java.lang.String[],
	 *      boolean)
	 */
	public boolean isThisBasedAccessPathRead(final SootMethod method, final String[] accesspath, final boolean recurse)
			throws IllegalArgumentException {
		this.analysis.validate(method);

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				methodCtxtRetriever, ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IReadWriteInfo#isThisBasedAccessPathWritten(SootMethod, String[], boolean)
	 */
	public boolean isThisBasedAccessPathWritten(final SootMethod method, final String[] accesspath, final boolean recurse) {
		this.analysis.validate(method);

		final ITransformer<SootMethod, AliasSet> _transformer = new ChainedTransformer<SootMethod, MethodContext, AliasSet>(
				methodCtxtRetriever, ReadWriteInfo.THIS_ALIAS_SET_RETRIEVER);
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, false);
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
	 * Checks if an object may be shared between the object graphs represented by the given alias sets.
	 * 
	 * @param as1 is an alias set of interest.
	 * @param as2 is another alias set of interest.
	 * @return <code>true</code> if an object may be shared between the object graphs represented by the given alias sets;
	 *         <code>false</code>, otherwise.
	 * @pre as1 != null and as2 != null
	 */
	private boolean checkForObjectSharingBetween(final AliasSet as1, final AliasSet as2) {
		boolean _result = false;
		final Set<AliasSet> _reachable = new HashSet<AliasSet>();
		final IWorkBag<AliasSet> _wb = new HistoryAwareFIFOWorkBag<AliasSet>(_reachable);
		_wb.addWork(as1);
		while (_wb.hasWork()) {
			final AliasSet _as = _wb.getWork();
			_wb.addAllWorkNoDuplicates(_as.getFieldMap().values());
		}

		final Collection<AliasSet> _temp = new HashSet<AliasSet>(_reachable);
		_wb.clear();
		_reachable.clear();
		_wb.addWork(as2);
		while (_wb.hasWork() && !_result) {
			final AliasSet _as = _wb.getWork();
			if (_temp.contains(_as)) {
				_result = true;
			} else {
				_wb.addAllWorkNoDuplicates(_as.getFieldMap().values());
			}
		}

		return _result;
	}

	/**
	 * Checks if the alias set at the end of the accesspaths rooted at the given alias sets are the same/shared.
	 * 
	 * @param as1 is an alias set of interest. // *
	 * @param accesspath1 is an accesspath rooted at <code>as1</code>.
	 * @param as2 is another alias set of interest.
	 * @param accesspath2 is an accesspath rooted at <code>as2</code>.
	 * @return <code>true</code> if the alias set at the end of the accesspaths rooted at the given alias sets are the
	 *         same/shared; <code>false</code>, otherwise.
	 * @pre as1 != null and as2 != null and accesspath1 != null and accesspath2 != null
	 */
	private boolean checkForObjectSharingViaAccessPaths(final AliasSet as1, final String[] accesspath1, final AliasSet as2,
			final String[] accesspath2) {
		final AliasSet _endPoint1 = getAliasSetAtEndPointOfAccessPathRootedAt(accesspath1, as1);
		final AliasSet _endPoint2 = getAliasSetAtEndPointOfAccessPathRootedAt(accesspath2, as2);

		return _endPoint1 != null && _endPoint2 != null && _endPoint1.find() == _endPoint2.find();
	}

	/**
	 * Retrieves the alias set associated with the end of the access path rooted at the given alias set.
	 * 
	 * @param accesspath of interest. This is rooted at <code>as</code>.
	 * @param as is the alias set of interest.
	 * @return the alias set associated with the end of the access path rooted at the given alias set, if any.
	 * @pre accesspath != null and as != null
	 */
	private AliasSet getAliasSetAtEndPointOfAccessPathRootedAt(final String[] accesspath, final AliasSet as) {
		final AliasSet _result;
		final String[] _s1;

		if (accesspath.length == 0) {
			_s1 = new String[0];
		} else {
			_s1 = new String[accesspath.length - 1];
			System.arraycopy(accesspath, 0, _s1, 0, _s1.length);
		}

		final Pair<AliasSet, String[]> _pair = new Pair<AliasSet, String[]>(as.find(), _s1);

		if (analysis.query2handle.containsKey(_pair)) {
			_result = analysis.query2handle.get(_pair);
		} else {
			_result = as.getAccessPathEndPoint(_s1);
			analysis.query2handle.put(_pair, _result);
		}
		return _result;
	}

	/**
	 * Checks if the given method either reads or writes global data.
	 * 
	 * @param method of interest.
	 * @param retriever to be used.
	 * @param read <code>true</code> indicates read information is requested; <code>false</code> indidates write info is
	 *            requested.
	 * @return <code>true</code> if any global data was read when <code>read</code> is <code>true</code> and
	 *         <code>false</code> if it was not read when <code>read</code> was <code>true</code>. <code>true</code>
	 *         if any global data was written when <code>read</code> is <code>false</code> and <code>false</code> if it
	 *         was not written when <code>read</code> was <code> false</code>.
	 * @pre method != null and retriever != null
	 */
	private boolean globalDataReadWriteInfoHelper(final SootMethod method,
			final ITransformer<SootMethod, MethodContext> retriever, final boolean read) {
		final boolean _result;
		final MethodContext _ctxt = retriever.transform(method);

		if (_ctxt != null) {
			if (read) {
				_result = _ctxt.isGlobalDataRead();
			} else {
				_result = _ctxt.isGlobalDataWritten();
			}
		} else {
			_result = read ? readDefaultValue : writeDefaultValue;

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning default value - " + _result);
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
	 *            <code>false</code>, otherwise.
	 * @param retriever to be used to get the method context and the alias set.
	 * @param read <code>true</code> indicates read information is requested; <code>false</code> indidates write info is
	 *            requested.
	 * @return <code>true</code> if the given access path was read when <code>read</code> is <code>true</code> and
	 *         <code>false</code> if it was not read when <code>read</code> was <code>true</code>. <code>true</code>
	 *         if the given access path was written when <code>read</code> is <code>false</code> and <code>false</code>
	 *         if it was not written when <code>read</code> was <code> false</code>.
	 * @pre method != null and accesspath != null and retriever != null
	 */
	private boolean instanceDataReadWriteHelper(final SootMethod method, final String[] accesspath, final boolean recurse,
			final ITransformer<SootMethod, AliasSet> retriever, final boolean read) {
		final AliasSet _aliasSet = retriever.transform(method);
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

			final Pair<AliasSet, String[]> _pair = new Pair<AliasSet, String[]>(_aliasSet.find(), _s);

			if (this.analysis.query2handle.containsKey(_pair)) {
				_endPoint = this.analysis.query2handle.get(_pair);
			} else {
				_endPoint = _aliasSet.getAccessPathEndPoint(_s);
				this.analysis.query2handle.put(_pair, _endPoint);
			}
		}

		final boolean _result;

		if (_endPoint == null) {
			_result = read ? readDefaultValue : writeDefaultValue;

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("isAccessPathOperatedHelper(method = " + method + ", accesspath = " + Arrays.asList(accesspath)
						+ ", recurse = " + recurse + ", retriver = " + retriever + ") - No recorded information for "
						+ method + " is available.  Returning default value - " + _result);
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
