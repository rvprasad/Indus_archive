package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.SootMethod;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * AbstractVariantManager.java
 *
 *
 * Created: Tue Jan 22 05:21:42 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractVariantManager {

	protected final BFA bfa;

	private final Map index2variant = new HashMap();

	private final AbstractIndexManager indexManager;

	private static final Logger logger = LogManager.getLogger(AbstractVariantManager.class);

	AbstractVariantManager (BFA bfa, AbstractIndexManager indexManager){
		this.bfa = bfa;
		this.indexManager = indexManager;
	}

	protected abstract Variant getNewVariant(Object o);

	public final Variant select(Object o, Context context) {
		Index index = indexManager.getIndex(o, context);
		Variant temp = null;

		logger.debug("Entering - Index: " + index + "\n" + o + "\n" + context + "\n" + index2variant + "\n" +
					 bfa.analyzer.active + "\n" + index.hashCode());

		if (index2variant.containsKey(index)) {
			temp = (Variant)index2variant.get(index);
		} // end of if (index2variant.containsKey(index))
		else if (bfa.analyzer.active) {
			temp = getNewVariant(o);
			index2variant.put(index, temp);
		} // end of if (index2variant.containsKey(index)) else

		logger.debug("Exiting - Index: " + index + "\n" + o + "\n" + context + "\n" + index2variant + "\n" +
					 bfa.analyzer.active);

		return temp;
	}

	void reset() {
		logger.debug("Variant manager being reset.");
		index2variant.clear();
		indexManager.reset();
	}

}// AbstractVariantManager
