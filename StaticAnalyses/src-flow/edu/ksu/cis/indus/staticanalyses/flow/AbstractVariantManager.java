
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.processing.Context;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class manages variants.  An variant manager classes should extend this class.  This class embodies the logic to
 * manage the variants.
 * 
 * <p>
 * Created: Tue Jan 22 05:21:42 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractVariantManager {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractVariantManager.class);

	/**
	 * The instance of the framework in which this object is used.
	 *
	 * @invariant fa != null
	 */
	protected final FA fa;

	/**
	 * A manager of indices that map entities to variants.
	 *
	 * @invariant indexManager != null
	 */
	private final AbstractIndexManager idxManager;

	/**
	 * A map from indices to variants.
	 *
	 * @invariant index2variant != null
	 */
	private final Map index2variant = new HashMap();

	/**
	 * Creates a new <code>AbstractVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices that map the entities to variants.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	AbstractVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager) {
		this.fa = theAnalysis;
		this.idxManager = indexManager;
	}

	/**
	 * Returns the variant corresponding to the given entity in the given context, if one exists.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant correponding to the entity in the given context, if one exists.  <code>null</code> if none exist.
	 *
	 * @pre o != null and context != null
	 */
	public final IVariant query(final Object o, final Context context) {
		return (IVariant) index2variant.get(idxManager.getIndex(o, context));
	}

	/**
	 * Returns the variant corresponding to the given entity in the given context.  If a variant does not exist, a new one is
	 * created.  If one exists, it shall be returned.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant correponding to the entity in the given context.
	 *
	 * @pre o != null and context != null
	 * @post result != null
	 */
	public final IVariant select(final Object o, final Context context) {
		IIndex index = idxManager.getIndex(o, context);
		IVariant temp = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering - IIndex: " + index + "\n" + o + "\n" + context);
		}

		if (index2variant.containsKey(index)) {
			temp = (IVariant) index2variant.get(index);
		} else if (!fa._analyzer.isStable()) {
			temp = getNewVariant(o);
			index2variant.put(index, temp);
			temp.process();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Exiting - IIndex");
		}

		return temp;
	}

	/**
	 * Returns the new variant correponding to the given object. This is a template method to be provided by concrete
	 * implementations.
	 *
	 * @param o the object whose corresponding variant is to be returned.
	 *
	 * @return the new variant corresponding to the given object.
	 *
	 * @pre o != null
	 * @post result != null
	 */
	protected abstract IVariant getNewVariant(final Object o);

	/**
	 * Returns the total variants managed by this manager.
	 *
	 * @return number of variants managed.
	 */
	protected int auxGetVariantCount() {
		return index2variant.values().size();
	}

	/**
	 * Resets the manager.  All internal data structures are reset to enable a new session of usage.
	 */
	void reset() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("IVariant manager being reset.");
		}
		index2variant.clear();
		idxManager.reset();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/12/07 09:08:20  venku
   - logging.

   Revision 1.8  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/30 22:39:20  venku
   Added support to query statistics of the managers.
   Revision 1.4  2003/08/21 03:47:11  venku
   Ripple effect of adding IStatus.
   Documentation.
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.13  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
