
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
		 * The logger used by instances of this class to log messages.
		 */
		private final Log LOGGER = LogFactory.getLog(CustomProcessor.class);

		/**
		 * Creates an instance of this class.
		 *
		 * @param das DOCUMENT ME!
		 */
		public CustomProcessor(final Collection das) {
			analyses = das;
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
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		public void consolidate() {
			final Collection _result = new ArrayList();

			final Map _method2birsig = new HashMap();

			final Iterator _i = map.entrySet().iterator();
			final int _iEnd = map.entrySet().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Pair _pair = (Pair) _entry.getKey();
				final Collection _interferees = (Collection) _entry.getValue();
				final StringBuffer _sb = new StringBuffer();
				_sb.append(generateBIRRep(_pair, _method2birsig));

				final Iterator _j = _interferees.iterator();
				final int _jEnd = _interferees.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final Pair _p = (Pair) _j.next();
					_sb.append(generateBIRRep(_p, _method2birsig));
				}
				_result.add(_sb.toString());
			}

			try {
				final ObjectOutputStream _output = new ObjectOutputStream(new FileOutputStream("independence"));
				_output.writeObject(_result);
				_output.close();

				final ObjectInputStream _input = new ObjectInputStream(new FileInputStream("independence"));
				final List _temp = (List) _input.readObject();
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
		 * @param stmt DOCUMENT ME!
		 * @param method DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		private Object getBIRLocation(final Stmt stmt, final SootMethod method) {
			final List _sl = new ArrayList(method.retrieveActiveBody().getUnits());
			return "loc" + _sl.indexOf(stmt);
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

			return _sig + " " + getBIRLocation(_stmt, _method);
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

		final CustomProcessor _proc = new CustomProcessor(_das);
		_proc.hookup(_pc);
		_pc.process();
		_proc.unhook(_pc);
	}
}

// End of File
