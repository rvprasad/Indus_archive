package edu.ksu.cis.bandera.bfa;


import org.apache.log4j.Category;
import ca.mcgill.sable.soot.ArrayType;

/**
 * ArrayVariant.java
 *
 *
 * Created: Fri Jan 25 16:05:27 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ArrayVariant extends AbstractValuedVariant {

	private static final Category cat = Category.getInstance(ArrayVariant.class.getName());

	ArrayType type;

	protected ArrayVariant (ArrayType a, AbstractFGNode node){
		super(node);
		this.type = a;
	}

}// ArrayVariant
