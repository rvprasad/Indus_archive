package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//AbstractVariantManager.java
/**
 * <p>This class manages variants.  An variant manager classes should extend this class.  This class embodies the logic to
 * manage the variants.</p>
 *
 * <p>Created: Tue Jan 22 05:21:42 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractVariantManager {

	/**
	 * <p>The instance of the framework in which this object is used.</p>
	 *
	 */
	protected final BFA bfa;

	/**
	 * <p>A map from indices to variants.</p>
	 *
	 */
	private final Map index2variant = new HashMap();

	/**
	 * <p>A manager of indices which map entities to variants.</p>
	 *
	 */
	private final AbstractIndexManager indexManager;

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractVariantManager.class);

	/**
	 * <p>Creates a new <code>AbstractVariantManager</code> instance.</p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 * @param indexManager the manager of indices which map the entities to variants.  This cannot be <code>null</code>.
	 */
	AbstractVariantManager (BFA bfa, AbstractIndexManager indexManager){
		this.bfa = bfa;
		this.indexManager = indexManager;
	}

	/**
	 * <p>Returns the new variant correponding to the given object.</p>
	 *
	 * @param o the object whose corresponding variant is to be returned.
	 * @return the new variant corresponding to the given object.
	 */
	protected abstract Variant getNewVariant(Object o);

	/**
	 * <p>Returns the variant corresponding to the given entity in the given context.  If a variant does not exist, a new one
	 * is created.  If one exists, it shall be returned.</p>
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant correponding to the entity in the given context.
	 */
	public final Variant select(Object o, Context context) {
		Index index = indexManager.getIndex(o, context);
		Variant temp = null;

		logger.debug("Entering - Index: " + index + "\n" + o + "\n" + context + "\n" + bfa.analyzer.active + "\n" +
					 index.hashCode());

		if (index2variant.containsKey(index)) {
			temp = (Variant)index2variant.get(index);
		} // end of if (index2variant.containsKey(index))
		else if (bfa.analyzer.active) {
			temp = getNewVariant(o);
			index2variant.put(index, temp);
		} // end of if (index2variant.containsKey(index)) else

		logger.debug("Exiting - Index: " + index + "\n" + o + "\n" + context + "\n" + bfa.analyzer.active);

		return temp;
	}

	/**
	 * <p>Resets the manager.  All internal data structures are reset to enable a new session of usage.</p>
	 *
	 */
	void reset() {
		logger.debug("Variant manager being reset.");
		index2variant.clear();
		indexManager.reset();
	}

}// AbstractVariantManager
