package edu.ksu.cis.bandera.bfa;


import org.apache.log4j.Category;
import ca.mcgill.sable.soot.ArrayType;

/**
 * ArrayVariantManager.java
 *
 *
 * Created: Fri Jan 25 13:50:16 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ArrayVariantManager extends AbstractVariantManager {

	private static final Category cat = Category.getInstance(ArrayVariantManager.class.getName());

	ArrayVariantManager (BFA bfa, AbstractIndexManager indexManager){
		super(bfa, indexManager);
	}

	protected Variant getNewVariant(Object o) {
		return new ArrayVariant((ArrayType)o, bfa.getFGNode());
	}

}// ArrayVariantManager
