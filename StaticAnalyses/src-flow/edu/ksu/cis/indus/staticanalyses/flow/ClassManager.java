
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootMethod;

import soot.tagkit.Tag;


/**
 * This class manages class related primitive information and processing such as the processing of <code>&lt;
 * clinit&gt;</code> methods of classes being analyzed.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ClassManager {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassManager.class);

	/** 
	 * The collection of classes for which the information has been processed.
	 */
	final Collection classes;

	/** 
	 * Describe variable <code>context</code> here.
	 *
	 * @invariant context != null
	 */
	private final Context context;

	/** 
	 * The instance of the framework in which this object is used.
	 *
	 * @invariant fa != null
	 */
	private final FA fa;

	/**
	 * Creates a new <code>ClassManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 *
	 * @pre theAnalysis != null
	 */
	ClassManager(final FA theAnalysis) {
		classes = new HashSet();
		this.fa = theAnalysis;
		context = new Context();
	}

	/**
	 * Processes the given class for assimilating class related primitive information into the analysis.  This implementation
	 * hooks in the class initialization method into the analysis.  This will also mark the field with the flow analysis
	 * tag.
	 *
	 * @param sc the class to be processed.  This cannot be <code>null</code>.
	 *
	 * @pre sc != null
	 */
	protected void process(final SootClass sc) {
		if (!classes.contains(sc)) {
			final Tag _theTag = fa.getTag();
			classes.add(sc);
			sc.addTag(_theTag);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("considered " + sc.getName());
			}

			includeClassInitializer(sc);

			final IWorkBag _wb = new HistoryAwareFIFOWorkBag(classes);
			final Collection _temp = new HashSet();

			if (Util.hasSuperclass(sc)) {
				_temp.add(sc.getSuperclass());
			}
			_temp.addAll(sc.getInterfaces());
			_wb.addAllWorkNoDuplicates(_temp);

			while (_wb.hasWork()) {
				final SootClass _sc = (SootClass) _wb.getWork();
				_sc.addTag(_theTag);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("considered " + _sc.getName());
				}
				includeClassInitializer(_sc);
				_temp.clear();

				if (Util.hasSuperclass(_sc)) {
					_temp.add(_sc.getSuperclass());
				}
				_temp.addAll(_sc.getInterfaces());

				_wb.addAllWorkNoDuplicates(_temp);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("STATS: # of classes processed is " + classes.size());
			}
		}
	}

	/**
	 * Resets the manager.  Removes all information maintained about any classes.
	 */
	protected void reset() {
		classes.clear();
	}

	/**
	 * Includes the class initializer into the flow graph.
	 *
	 * @param sc is the class of which the initializer is included.
	 *
	 * @pre sc != null
	 */
	private void includeClassInitializer(final SootClass sc) {
		if (sc.declaresMethodByName("<clinit>")) {
			final SootMethod _method = sc.getMethodByName("<clinit>");

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("considered " + _method);
			}

			fa.getMethodVariant(_method, context);
		}
	}
}

// End of File
