package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.ArrayType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//ArrayVariantManager.java
/**
 * <p>This class manages variants corresponding to arrays.  </p>
 *
 * <p>Created: Fri Jan 25 13:50:16 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class ArrayVariantManager extends AbstractVariantManager {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ArrayVariantManager.class);

	/**
	 * <p>Creates a new <code>ArrayVariantManager</code> instance.</p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 * @param indexManager the manager of indices which map array variants to arrays.  This cannot be <code>null</code>.
	 */
	ArrayVariantManager (BFA bfa, AbstractIndexManager indexManager){
		super(bfa, indexManager);
	}

	/**
	 * <p>Returns a new array variant corresponding to the given array type.</p>
	 *
	 * @param o the <code>ArrayType</code> whose variant is to be returned.
	 * @return a new <code>ArrayVariant</code> corresponding to <code>o</code>.
	 */
	protected Variant getNewVariant(Object o) {
		return new ArrayVariant((ArrayType)o, bfa.getFGNode());
	}

}// ArrayVariantManager
