package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;

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

	private static final Category cat = Category.getInstance(AbstractVariantManager.class.getName());

	AbstractVariantManager (BFA bfa, AbstractIndexManager indexManager){
		this.bfa = bfa;
		this.indexManager = indexManager;
	}

	protected abstract Variant getNewVariant(Object o);

	public final Variant select(Object o, Context context) {
		Index index = indexManager.getIndex(o, context);
		Variant temp;

		if (index2variant.containsKey(index)) {
			temp = (Variant)index2variant.get(index);
		} // end of if (index2variant.containsKey(index))
		else {
			temp = getNewVariant(o);
			index2variant.put(index, temp);
		} // end of if (index2variant.containsKey(index)) else
		return temp;
	}

	void reset() {
		index2variant.clear();
		indexManager.reset();
	}

}// AbstractVariantManager
