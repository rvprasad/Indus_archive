package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.jimple.Value;
import org.apache.log4j.Category;

// ASTVariantManager.java
/**
 * <p>This class provides the logic to create new variants of AST nodes.</p>
 *
 * Created: Tue Jan 22 12:46:24 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ 
 */

public class ASTVariantManager extends AbstractVariantManager {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Category cat = Category.getInstance(ASTVariantManager.class.getName());

	/**
	 * <p>Creates a new <code>ASTVariantManager</code> instance.</p>
	 *
	 * @param bfa the instance of the framework in which this instance exists.
	 * @param indexManager the manager that shall provide the indices to lookup the variants.
	 */
	ASTVariantManager(BFA bfa, AbstractIndexManager indexManager){
		super(bfa, indexManager);
	}

	/**
	 * <p>Returns a new variant representing the given AST node.</p>
	 *
	 * @param o the AST node, <code>Value</code> object, to be represented by the returned variant. 
	 * @return the variant representing the AST node, <code>o</code>.
	 */
	protected Variant getNewVariant(Object o) {
		return new ASTVariant((Value)o, bfa.getFGNode());
	}

}// ASTVariantManager
