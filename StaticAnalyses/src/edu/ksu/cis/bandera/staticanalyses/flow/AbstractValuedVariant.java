package edu.ksu.cis.bandera.bfa;

import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//AbstractValuedVariant.java
/**
 *
 * <p>Variant of entities associated with data such as AST nodes and fields.  All such data related variants should extend
 * this class.</p>
 *
 * <p>Created: Tue Jan 22 15:44:48 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractValuedVariant implements Variant {

	/**
	 * <p>The flow graph node associated with this variant.</p>
	 *
	 */
	protected FGNode node;

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractValuedVariant.class);

	/**
	 * <p>Creates a new <code>AbstractValuedVariant</code> instance.</p>
	 *
	 * @param node the flow graph node associated with this variant.
	 */
	AbstractValuedVariant (FGNode node){
		this.node = node;
	}

	/**
	 * <p>Sets the given node as the flow graph node of this variant.</p>
	 *
	 * @param node the node to be set as the flow graph node of this variant.
	 */
	public void setFGNode(FGNode node) {
		this.node = node;
	}

	/**
	 * <p>Returns the flow graph node associated with this node.</p>
	 *
	 * @return the flow graph node associated with this node.
	 */
	public FGNode getFGNode() {
		return node;
	}

	/**
	 * <p>Returns the set of values associated with this variant.</p>
	 *
	 * @return the set of values associated with this variant.
	 */
	public final Collection getValues() {
		return node.getValues();
	}

	/**
	 * <p>Performs nothing.  This will be called after a variant is created.</p>
	 */
	public void process() {}

}// AbstractValuedVariant
