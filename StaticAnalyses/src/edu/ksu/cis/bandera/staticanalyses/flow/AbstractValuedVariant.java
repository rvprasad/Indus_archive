package edu.ksu.cis.bandera.bfa;

import org.apache.log4j.Category;
import java.util.Collection;

/**
 * AbstractValuedVariant.java
 *
 * This is required to represent entities associated with nodes unlike control associated variants
 * like method variants.
 *
 * Created: Tue Jan 22 15:44:48 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractValuedVariant implements Variant {

	protected AbstractFGNode node;

	private static final Category cat = Category.getInstance(AbstractValuedVariant.class.getName());

	AbstractValuedVariant (AbstractFGNode node){
		setFGNode(node);
	}

	public void setFGNode(AbstractFGNode node) {
		this.node = node;
	}

	public AbstractFGNode getFGNode() {
		return node;
	}

	public final Collection getValues() {
		return node.getValues();
	}

}// AbstractValuedVariant
