package edu.ksu.cis.bandera.staticanalyses.flow;


import ca.mcgill.sable.soot.ArrayType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//ArrayVariant.java
/**
 * Variant representing arrays.
 *
 * <p>Created: Fri Jan 25 16:05:27 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class ArrayVariant extends AbstractValuedVariant {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ArrayVariant.class);

	/**
	 * <p>The array type being represented by this variant.</p>
	 *
	 */
	public final ArrayType type;

	/**
	 * <p>Creates a new <code>ArrayVariant</code> instance.</p>
	 *
	 * @param a the array type to which this variant corresonds to.
	 * @param node the flow graph node corresponding to this variant.
	 */
	protected ArrayVariant (ArrayType a, FGNode node){
		super(node);
		this.type = a;
	}

}// ArrayVariant
