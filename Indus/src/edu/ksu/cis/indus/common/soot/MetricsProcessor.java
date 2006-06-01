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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.decorator.TObjectIntHashMapDecorator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import soot.Body;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.InvokeExprBox;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * This class counts the instances of various types of AST chunks that occur in a system.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class MetricsProcessor
		extends AbstractProcessor {

	/**
	 * This enumeration provides the keys to the metrics calculated by the enclosing processor.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public enum MetricKeys {
		/**
		 * The key to identify the stats for application classes.
		 */
		APPLICATION_STATISTICS,

		/**
		 * The key to identify the stats for library classes.
		 */
		LIBRARY_STATISTICS,

		/**
		 * The key to identify number of catch blocks.
		 */
		NUM_OF_CATCH_BLOCKS,

		/**
		 * The key to identify the number of classes metrics.
		 */
		NUM_OF_CLASSES,

		/**
		 * The key to identify the number of constant method arguments.
		 */
		NUM_OF_CONSTANT_METHOD_ARGUMENTS,

		/**
		 * The key to identify the number of exceptional exit points in methods.
		 */
		NUM_OF_EXCEPTIONAL_EXIT_POINTS_IN_METHODS,

		/**
		 * The key to identify the number of fields metrics.
		 */
		NUM_OF_FIELDS,

		/**
		 * The key to identify final fields.
		 */
		NUM_OF_FINAL_FIELDS,

		/**
		 * The key to identify number of local variables.
		 */
		NUM_OF_LOCALS,

		/**
		 * The key to identify the number of methods metrics.
		 */
		NUM_OF_METHODS,

		/**
		 * The key to identify the number of methods with multiple exit points metrics.
		 */
		NUM_OF_METHODS_WITH_MULTIPLE_EXIT_POINTS,

		/**
		 * The key to identify the number of methods with zero points metrics.
		 */
		NUM_OF_METHODS_WITH_ZERO_EXIT_POINTS,
		/**
		 * The key to identify the number of normal exit points in methods.
		 */
		NUM_OF_NORMAL_EXIT_POINTS_IN_METHODS;
	}

	/**
	 * This records the statistics for application classes. It maps the above constants or the name of the class of the AST
	 * objects to the number of occurrences of the corresponding entities.
	 */
	private final TObjectIntHashMap applicationStatistics = new TObjectIntHashMap();

	/**
	 * This records the statistics for library classes. It maps the above constants or the name of the class of the AST
	 * objects to the number of occurrences of the corresponding entities.
	 */
	private final TObjectIntHashMap libraryStatistics = new TObjectIntHashMap();

	/**
	 * This serves as a reference to statistics collection. This will be varied depending on the class that is being
	 * processed.
	 */
	private TObjectIntHashMap statistics;

	/**
	 * Creates an instance of this class.
	 */
	public MetricsProcessor() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@Immutable @NonNull final SootClass clazz) {
		if (clazz.isApplicationClass()) {
			statistics = applicationStatistics;
		} else {
			statistics = libraryStatistics;
		}

		if (!statistics.increment(MetricKeys.NUM_OF_CLASSES)) {
			statistics.put(MetricKeys.NUM_OF_CLASSES, 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@Immutable @NonNull final SootField field) {
		if (!statistics.increment(MetricKeys.NUM_OF_FIELDS)) {
			statistics.put(MetricKeys.NUM_OF_FIELDS, 1);
		}
		if (field.isFinal()) {
			if (!statistics.increment(MetricKeys.NUM_OF_FINAL_FIELDS)) {
				statistics.put(MetricKeys.NUM_OF_FINAL_FIELDS, 1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@Immutable @NonNull final SootMethod method) {
		if (!statistics.increment(MetricKeys.NUM_OF_METHODS)) {
			statistics.put(MetricKeys.NUM_OF_METHODS, 1);
		}

		if (method.hasActiveBody()) {
			final Body _body = method.retrieveActiveBody();

			// gather data about catch blocks in the current method.
			final int _trapCount = _body.getTraps().size();

			if (!statistics.adjustValue(MetricKeys.NUM_OF_CATCH_BLOCKS, _trapCount)) {
				statistics.put(MetricKeys.NUM_OF_CATCH_BLOCKS, _trapCount);
			}

			// gather data about locals in the current method.
			final int _localCount = _body.getLocalCount();

			if (!statistics.adjustValue(MetricKeys.NUM_OF_LOCALS, _localCount)) {
				statistics.put(MetricKeys.NUM_OF_LOCALS, _localCount);
			}

			calculateReturnPointStats(_body);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@Immutable @NonNull final Stmt stmt, @SuppressWarnings("unused") final Context context) {
		if (!statistics.increment(stmt.getClass().getName())) {
			statistics.put(stmt.getClass().getName(), 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@Immutable @NonNull final ValueBox vBox, @SuppressWarnings("unused") final Context context) {
		if (!statistics.increment(vBox.getValue().getClass().getName())) {
			statistics.put(vBox.getValue().getClass().getName(), 1);
		}

		if (vBox instanceof InvokeExprBox) {
			int _constArgs = 0;
			final InvokeExpr _expr = (InvokeExpr) vBox.getValue();
			final Iterator<?> _i = _expr.getArgs().iterator();
			final int _iEnd = _expr.getArgs().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Object _arg = _i.next();

				if (_arg instanceof Constant) {
					_constArgs++;
				}
			}

			if (!statistics.adjustValue(MetricKeys.NUM_OF_CONSTANT_METHOD_ARGUMENTS, _constArgs)) {
				statistics.put(MetricKeys.NUM_OF_CONSTANT_METHOD_ARGUMENTS, _constArgs);
			}
		}
	}

	/**
	 * Returns a map from constants (defined in this class or the names of the class of the AST objects) to the
	 * <code>Integer</code> representing the number of occurrences of entities of the corresponding kind.
	 * 
	 * @return a map.
	 */
	@NonNull @Functional public Map<MetricKeys, Map<Object, Integer>> getStatistics() {
		final Map<MetricKeys, Map<Object, Integer>> _result = new HashMap<MetricKeys, Map<Object, Integer>>();
		final Map<Object, Integer> _map1 = new TreeMap<Object, Integer>(ToStringBasedComparator.getComparator());
		_map1.putAll(new TObjectIntHashMapDecorator(applicationStatistics));
		_result.put(MetricKeys.APPLICATION_STATISTICS, _map1);
		final Map<Object, Integer> _map2 = new TreeMap<Object, Integer>(ToStringBasedComparator.getComparator());
		_map2.putAll(new TObjectIntHashMapDecorator(libraryStatistics));
		_result.put(MetricKeys.LIBRARY_STATISTICS, _map2);
		return _result;
	}

	/**
	    * Retrives a summarized statistics (only for keys mentioned in MetricKeys).
	    * 
	    * @return the summarized statistics.
	    */
	   @NonNull @Functional @NonNullContainer public Map<MetricKeys, Map<Object, Integer>> getSummary() {
	       final Map<Object, Integer> _app = new HashMap<Object, Integer>();
	       for (final Object o : applicationStatistics.keys()) {
	           if (o instanceof MetricKeys) {
	               _app.put(o, applicationStatistics.get(o));
	           }
	       }
	       final Map<Object, Integer> _lib = new HashMap<Object, Integer>();
	       for (final Object o : libraryStatistics.keys()) {
	           if (o instanceof MetricKeys) {
	               _lib.put(o, libraryStatistics.get(o));
	           }
	       }
	       final Map<MetricKeys, Map<Object, Integer>> _result = new HashMap<MetricKeys, Map<Object, Integer>>();
	       _result.put(MetricKeys.APPLICATION_STATISTICS, _app);
	       _result.put(MetricKeys.LIBRARY_STATISTICS, _app);
	       return _result;
	   }

	/**
	 * {@inheritDoc}
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.registerForAllStmts(this);
		ppc.registerForAllValues(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void processingBegins() {
		applicationStatistics.clear();
		libraryStatistics.clear();
	}
    /**
	 * {@inheritDoc}
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
		ppc.unregisterForAllValues(this);
	}

	/**
	 * Calculates the return point statistics for the given method body.
	 * 
	 * @param body of the method to be processed.
	 */
	private void calculateReturnPointStats(@NonNull @Immutable final Body body) {
		int _returnPoints = 0;
		int _throwPoints = 0;
		final UnitGraph _completeUnitGraph = new CompleteUnitGraph(body);
		@SuppressWarnings("unchecked") final Collection<Stmt> _tails = _completeUnitGraph.getTails();
		@SuppressWarnings("unchecked") final Iterator<Stmt> _i = _tails.iterator();
		final int _iEnd = _tails.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _stmt = _i.next();

			if (_stmt instanceof ReturnStmt || _stmt instanceof ReturnVoidStmt) {
				_returnPoints++;
			} else if (_stmt instanceof ThrowStmt) {
				_throwPoints++;
			}
		}

		if (!statistics.adjustValue(MetricKeys.NUM_OF_NORMAL_EXIT_POINTS_IN_METHODS, _returnPoints)) {
			statistics.put(MetricKeys.NUM_OF_NORMAL_EXIT_POINTS_IN_METHODS, _returnPoints);
		}

		if (!statistics.adjustValue(MetricKeys.NUM_OF_EXCEPTIONAL_EXIT_POINTS_IN_METHODS, _throwPoints)) {
			statistics.put(MetricKeys.NUM_OF_EXCEPTIONAL_EXIT_POINTS_IN_METHODS, _throwPoints);
		}

		final int _exitPoints = _returnPoints + _throwPoints;

		if (_exitPoints > 1) {
			if (!statistics.increment(MetricKeys.NUM_OF_METHODS_WITH_MULTIPLE_EXIT_POINTS)) {
				statistics.put(MetricKeys.NUM_OF_METHODS_WITH_MULTIPLE_EXIT_POINTS, 1);
			}
		} else if (_exitPoints == 0) {
			if (!statistics.increment(MetricKeys.NUM_OF_METHODS_WITH_ZERO_EXIT_POINTS)) {
				statistics.put(MetricKeys.NUM_OF_METHODS_WITH_ZERO_EXIT_POINTS, 1);
			}
		}
	}
}

// End of File
