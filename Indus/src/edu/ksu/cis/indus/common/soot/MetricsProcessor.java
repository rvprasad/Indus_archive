
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

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import gnu.trove.TObjectIntHashMap;

import gnu.trove.decorator.TObjectIntHashMapDecorator;

import java.util.HashMap;
import java.util.Map;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;


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
	 * This constant identifies the number of classes in the system.
	 */
	public static final Object NUM_OF_CLASSES = "The number of classes";

	/** 
	 * This constant identifies the number of fields in the system.
	 */
	public static final Object NUM_OF_FIELDS = "The number of fields";

	/** 
	 * This constant identifies the number of methods in the system.
	 */
	public static final Object NUM_OF_METHODS = "The number of methods";

	/** 
	 * This constant identifies the number of methods in the system with multiple exit points.
	 */
	public static final Object NUM_OF_METHODS_WITH_MULTIPLE_EXIT_POINTS = "The number of methods with multiple exit points";

	/** 
	 * This constant identifies the number of methods in the system with zero exit points.
	 */
	public static final Object NUM_OF_METHODS_WITH_ZERO_EXIT_POINTS = "The number of methods with zero points";

	/** 
	 * This records the statistics. It maps the above constants or the name of the class of the AST objects to the number of
	 * occurrences of the corresponding entities.
	 */
	final TObjectIntHashMap statistics = new TObjectIntHashMap();

	/** 
	 * This is an accounting variable.
	 */
	int exitPoints;

	/**
	 * Returns a map from constants (defined in this class or the names of the class of the AST objects) to the
	 * <code>Integer</code> representing the number of occurrences of entities of the corresponding kind.
	 *
	 * @return a map.
	 *
	 * @post result != null and result.oclIsKindOf(Map(Object, Integer))
	 */
	public Map getStatistics() {
		return new HashMap(new TObjectIntHashMapDecorator(statistics));
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		if (!statistics.increment(NUM_OF_CLASSES)) {
			statistics.put(NUM_OF_CLASSES, 1);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public void callback(final SootField field) {
		if (!statistics.increment(NUM_OF_FIELDS)) {
			statistics.put(NUM_OF_FIELDS, 1);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		if (!statistics.increment(NUM_OF_METHODS)) {
			statistics.put(NUM_OF_METHODS, 1);
		}

		if (exitPoints > 1) {
			if (!statistics.increment(NUM_OF_METHODS_WITH_MULTIPLE_EXIT_POINTS)) {
				statistics.put(NUM_OF_METHODS_WITH_MULTIPLE_EXIT_POINTS, 1);
			}
		} else if (exitPoints == 0) {
			if (!statistics.increment(NUM_OF_METHODS_WITH_ZERO_EXIT_POINTS)) {
				statistics.put(NUM_OF_METHODS_WITH_ZERO_EXIT_POINTS, 1);
			}
		}
		exitPoints = 0;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		if (!statistics.increment(stmt.getClass().getName())) {
			statistics.put(stmt.getClass().getName(), 1);
		}

		if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt || stmt instanceof ThrowStmt) {
			exitPoints++;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		if (!statistics.increment(vBox.getValue().getClass().getName())) {
			statistics.put(vBox.getValue().getClass().getName(), 1);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.registerForAllStmts(this);
		ppc.registerForAllValues(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		statistics.clear();
		exitPoints = 0;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
		ppc.unregisterForAllValues(this);
	}
}

// End of File
