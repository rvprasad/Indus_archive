package edu.ksu.cis.bandera.bfa;

import org.apache.log4j.Category;
import ca.mcgill.sable.soot.jimple.Value;

/**
 * ASTVariant.java
 *
 *
 * Created: Tue Jan 22 13:05:04 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ASTVariant extends AbstractValuedVariant {

	private static final Category cat = Category.getInstance(ASTVariant.class.getName());

	public final Value v;

	protected ASTVariant (Value v, AbstractFGNode node){
		super(node);
		this.v = v;
	}
}// ASTVariant
