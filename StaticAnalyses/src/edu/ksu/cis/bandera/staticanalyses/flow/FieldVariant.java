package edu.ksu.cis.bandera.bfa;


import org.apache.log4j.Category;
import ca.mcgill.sable.soot.SootField;

/**
 * FieldVariant.java
 *
 *
 * Created: Fri Jan 25 14:29:09 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FieldVariant extends AbstractValuedVariant {

	private static final Category cat = Category.getInstance(FieldVariant.class.getName());

	public final SootField field;

	public FieldVariant (SootField field, AbstractFGNode node) {
		super(node);
		this.field = field;
	}

}// FieldVariant
