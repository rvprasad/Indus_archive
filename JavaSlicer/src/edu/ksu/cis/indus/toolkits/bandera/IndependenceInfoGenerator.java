
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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class IndependenceInfoGenerator {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(CustomProcessor.class);

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String METHOD_PREFIX = "{|";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String METHOD_SUFFIX = "|}";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String VIRTUAL_PREFIX = "+|";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String VIRTUAL_SUFFIX = "|+";

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CustomProcessor
	  extends AbstractProcessor {
		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Collection analyses;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Collection virtualMethods = new HashSet();

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Map map = new HashMap();

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final Collection defStmts = new HashSet();

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final IEscapeInfo einfo;

		/**
		 * Creates an instance of this class.
		 *
		 * @param das DOCUMENT ME!
		 * @param escapeInfo DOCUMENT ME!
		 */
		public CustomProcessor(final Collection das, final IEscapeInfo escapeInfo) {
			analyses = das;
			einfo = escapeInfo;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param stmt DOCUMENT ME!
		 * @param context DOCUMENT ME!
		 */
		public void callback(final Stmt stmt, final Context context) {
			final SootMethod _currentMethod = context.getCurrentMethod();
			populateInterference(stmt, _currentMethod);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param vBox DOCUMENT ME!
		 * @param context DOCUMENT ME!
		 */
		public void callback(final ValueBox vBox, final Context context) {
			if (vBox.getValue() instanceof VirtualInvokeExpr) {
				final VirtualInvokeExpr _v = (VirtualInvokeExpr) vBox.getValue();
				virtualMethods.add(_v.getMethod());
			} else {
				final SootMethod _currentMethod = context.getCurrentMethod();
				final Stmt _stmt = context.getStmt();
				populateInterference(_stmt, _currentMethod);

				final DefinitionStmt _dstmt = (DefinitionStmt) _stmt;

				if (_dstmt.getLeftOpBox() == vBox) {
					defStmts.add(new Pair(_stmt, _currentMethod));
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		public void consolidate() {
			calculateWriteWriteInterference();

			final Map _result = new HashMap();
			final Map _method2birsig = new HashMap();

			final Iterator _i = map.entrySet().iterator();
			final int _iEnd = map.entrySet().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Pair _pair = (Pair) _entry.getKey();
				final Collection _interferees = (Collection) _entry.getValue();
				final String _t = generateBIRRep(_pair, _method2birsig);

				if (_t != null) {
					final Iterator _j = _interferees.iterator();
					final int _jEnd = _interferees.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Pair _p = (Pair) _j.next();
						final String _t2 = generateBIRRep(_p, _method2birsig);

						if (_t2 != null) {
							CollectionsUtilities.putIntoSetInMap(_result, _t, _t2);
						}
					}
				}
			}

			try {
				final ObjectOutputStream _output = new ObjectOutputStream(new FileOutputStream("independence"));
				_output.writeObject(_result);
				_output.close();

				final ObjectInputStream _input = new ObjectInputStream(new FileInputStream("independence"));
				final Map _temp = (Map) _input.readObject();
				_input.close();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("consolidate()" + _temp);
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("consolidate()" + _result.equals(_temp));
				}
			} catch (final FileNotFoundException _e) {
				final IllegalStateException _r = new IllegalStateException();
				_r.initCause(_e);
				throw _r;
			} catch (final IOException _e) {
				final IllegalStateException _r = new IllegalStateException();
				_r.initCause(_e);
				throw _r;
			} catch (final ClassNotFoundException _e) {
				final IllegalStateException _r = new IllegalStateException();
				_r.initCause(_e);
				throw _r;
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(InvokeStmt.class, this);
			ppc.register(EnterMonitorStmt.class, this);
			ppc.register(ExitMonitorStmt.class, this);
			ppc.register(ArrayRef.class, this);
			ppc.register(FieldRef.class, this);
			ppc.register(VirtualInvokeExpr.class, this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(InvokeStmt.class, this);
			ppc.unregister(EnterMonitorStmt.class, this);
			ppc.unregister(ExitMonitorStmt.class, this);
			ppc.unregister(ArrayRef.class, this);
			ppc.unregister(FieldRef.class, this);
			ppc.unregister(VirtualInvokeExpr.class, this);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param stmt DOCUMENT ME!
		 * @param method DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		private Collection getInterferees(final Stmt stmt, final SootMethod method) {
			final Collection _result = new HashSet();
			final Iterator _i = analyses.iterator();
			final int _iEnd = analyses.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
				_result.addAll(_da.getDependees(stmt, method));
				_result.addAll(_da.getDependents(stmt, method));
			}
			return _result;
		}

		/**
		 * DOCUMENT ME!
		 */
		private void calculateWriteWriteInterference() {
			final Iterator _i = defStmts.iterator();
			final int _iEnd = defStmts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Pair _p1 = (Pair) _i.next();
				final Stmt _s1 = (Stmt) _p1.getFirst();
				final SootMethod _m1 = (SootMethod) _p1.getSecond();
				final Iterator _j = defStmts.iterator();

				for (int _jIndex = 0; _jIndex < _iEnd; _jIndex++) {
					final Pair _p2 = (Pair) _j.next();
					final Stmt _s2 = (Stmt) _p2.getFirst();
					final SootMethod _m2 = (SootMethod) _p2.getSecond();

					if (shared(_s1, _m1, _s2, _m2)) {
						CollectionsUtilities.putIntoSetInMap(map, _p1, _p2);
						CollectionsUtilities.putIntoSetInMap(map, _p2, _p1);
					}
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param p DOCUMENT ME!
		 * @param method2birsig DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		private String generateBIRRep(final Pair p, final Map method2birsig) {
			final Stmt _stmt = (Stmt) p.getFirst();
			final SootMethod _method = (SootMethod) p.getSecond();
			String _sig = (String) method2birsig.get(_method);

			if (_sig == null) {
				_sig = constructMethodName(virtualMethods.contains(_method), _method);
				method2birsig.put(_method, _sig);
			}

			final List _sl = new ArrayList(_method.retrieveActiveBody().getUnits());
			final int _index = _sl.indexOf(_stmt);
			final String _result;

			if (_index == -1) {
				_result = null;
			} else {
				_result = _sig + " loc" + _index + " ";
			}

			return _result;
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param stmt DOCUMENT ME!
		 * @param currentMethod DOCUMENT ME!
		 */
		private void populateInterference(final Stmt stmt, final SootMethod currentMethod) {
			final Collection _interferes = new HashSet();
			_interferes.addAll(getInterferees(stmt, currentMethod));
			CollectionsUtilities.putAllIntoSetInMap(map, new Pair(stmt, currentMethod), _interferes);
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param s1 is one statement of interest.
		 * @param m1 is the method containing <code>s1</code>.
		 * @param s2 is the second statement of interest.
		 * @param m2 is the method containing <code>s2</code>.
		 *
		 * @return <code>true</code> if both <code>s1</code> and <code>s2</code> contain either array/field ref in the
		 * 		   l-position; <code>false</code>, otherwise.
		 *
		 * @pre s1 != null and m1 != null and s2 != null and m2 != null
		 */
		private boolean shared(final Stmt s1, final SootMethod m1, final Stmt s2, final SootMethod m2) {
			final boolean _result;

			if (s1.containsArrayRef() && s2.containsArrayRef()) {
				_result = einfo.shared(s1.getArrayRef(), m1, s2.getArrayRef(), m2);
			} else if (s1.containsFieldRef() && s2.containsFieldRef()) {
				_result = einfo.shared(s1.getFieldRef(), m1, s2.getFieldRef(), m2);
			} else {
				_result = false;
			}
			return _result;
		}
	}

	/**
	 * This method constructs the BIR representation of the name of a method.
	 *
	 * @param isVirtual shows whether or not this method is invoked in a virtual invoke expression.
	 * @param sm The soot method whose name will be compiled here.
	 *
	 * @return the BIR representation of the name of sm.
	 */
	public static String constructMethodName(final boolean isVirtual, final SootMethod sm) {
		final SootClass _sc = sm.getDeclaringClass();
		final String _className = _sc.getName();
		final String _methodName = sm.getName();
		final int _size = sm.getParameterCount();
		final String[] _paramTypeNames = new String[_size];

		//get the array of the soot types of the parameters of the method
		for (int _i = 0; _i < _size; _i++) {
			_paramTypeNames[_i] = sm.getParameterType(_i).toString();
		}

		//construct the method name.
		final StringBuffer _sb = new StringBuffer(isVirtual ? VIRTUAL_PREFIX
															: METHOD_PREFIX);
		_sb.append(_className);
		_sb.append('.');
		_sb.append(_methodName);
		_sb.append('(');

		final int _size1 = _paramTypeNames.length;

		if (_size1 > 0) {
			_sb.append(_paramTypeNames[0]);

			for (int _i = 1; _i < _size1; _i++) {
				_sb.append(',');
				_sb.append(_paramTypeNames[_i]);
			}
		}

		_sb.append(')');
		_sb.append(isVirtual ? VIRTUAL_SUFFIX
							 : METHOD_SUFFIX);

		return _sb.toString();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tool DOCUMENT ME!
	 */
	public void dumpIndependenceInfo(final SlicerTool tool) {
		final Collection _das = new HashSet();
		final Iterator _i = tool.getDAs().iterator();
		final int _iEnd = tool.getDAs().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();

			if (_da.getIds().contains(IDependencyAnalysis.INTERFERENCE_DA)
				  || _da.getIds().contains(IDependencyAnalysis.READY_DA)) {
				_das.add(_da);
			}
		}

		final ProcessingController _pc = new ProcessingController();
		_pc.setEnvironment(tool.getSystem());

		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setBbgFactory(tool.getBasicBlockGraphManager());
		_pc.setStmtSequencesRetriever(_ssr);

		final CustomProcessor _proc = new CustomProcessor(_das, tool.getECBA());
		_proc.hookup(_pc);
		_pc.process();
		_proc.unhook(_pc);
	}
}

// End of File
