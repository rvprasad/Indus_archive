package edu.ksu.cis.bandera.staticanalyses.flow;


import ca.mcgill.sable.soot.jimple.Value;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//ASTVariant.java
/**
 * <p>This class represents a variant of an AST node.</p>
 *
 * Created: Tue Jan 22 13:05:04 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class ASTVariant extends AbstractValuedVariant {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ASTVariant.class);

	/**
	 * <p>The <code>Value</code> object represented by this variant.</p>
	 *
	 */
	public final Value v;

	/**
	 * <p>Creates a new <code>ASTVariant</code> instance.</p>
	 *
	 * @param v the value represented by this variant.
	 * @param node the flow graph node that summarizes the values for this variant.
	 */
	protected ASTVariant (Value v, FGNode node){
		super(node);
		this.v = v;
	}
}// ASTVariant
