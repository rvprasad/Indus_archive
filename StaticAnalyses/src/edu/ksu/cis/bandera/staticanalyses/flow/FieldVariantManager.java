package edu.ksu.cis.bandera.bfa;


import org.apache.log4j.Category;
import ca.mcgill.sable.soot.SootField;

/**
 * FieldVariantManager.java
 *
 *
 * Created: Fri Jan 25 14:33:09 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FieldVariantManager extends AbstractVariantManager {

	private static final Category cat = Category.getInstance(FieldVariantManager.class.getName());

	public FieldVariantManager (BFA bfa, AbstractIndexManager indexManager){
		super(bfa, indexManager);
	}

	protected Variant getNewVariant(Object o) {
		return new FieldVariant((SootField)o, bfa.getFGNode());
	}

}// FieldVariantManager
