package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.jimple.Value;
import org.apache.log4j.Category;

/**
 * ASTVariantManager.java
 *
 *
 * Created: Tue Jan 22 12:46:24 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ASTVariantManager extends AbstractVariantManager {

	private static final Category cat = Category.getInstance(ASTVariantManager.class.getName());

	ASTVariantManager(BFA bfa, AbstractIndexManager indexManager){
		super(bfa, indexManager);
	}

	protected Variant getNewVariant(Object o) {
		return new ASTVariant((Value)o, bfa.getFGNode());
	}

}// ASTVariantManager
