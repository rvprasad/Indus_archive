
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import edu.ksu.cis.indus.interfaces.IPrototype;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;

import soot.tagkit.Tag;


/**
 * This class manages class related primitive information and processing such as the processing of <code>&lt;
 * clinit&gt;</code> methods of classes being analyzed.
 * 
 * <p>
 * Created: Fri Mar  8 14:10:27 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ClassManager
  implements IPrototype {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ClassManager.class);

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
	public ClassManager(final FA theAnalysis) {
		classes = new HashSet();
		this.fa = theAnalysis;
		context = new Context();
	}

	/**
	 * Creates a concrete object of the same class as this object but parameterized by <code>o</code>.
	 *
	 * @param o the instance of the analysis for which this object shall process information.  The actual type of
	 * 		  <code>o</code> needs to be <code>FA</code>.
	 *
	 * @return an instance of <code>ClassManager</code> object parameterized by <code>o</code>.
	 *
	 * @pre o != null
	 * @post result != null
	 */
	public Object getClone(final Object o) {
		return new ClassManager((FA) o);
	}

	/**
	 * This method is not supported by this class.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException this method is not supported by this class.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype() method not supported.");
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

			if (sc.hasSuperclass()) {
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

				if (_sc.hasSuperclass()) {
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
