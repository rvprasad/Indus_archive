package edu.ksu.cis.bandera.staticanalyses.flow;


import ca.mcgill.sable.soot.SootField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//FieldVariantManager.java
/**
 * <p>This class manages field variants.  This class only provides the implementation to create new field variants.  The super
 * class is responsible of managing the variants.</p>
 *
 * <p>Created: Fri Jan 25 14:33:09 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class FieldVariantManager extends AbstractVariantManager {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(FieldVariantManager.class);

	/**
	 * <p>Creates a new <code>FieldVariantManager</code> instance.</p>
	 *
	 * @param bfa the instance of the framework in which this object is used. This parameter cannot be <code>null</code>.
	 * @param indexManager the manager of indices which are used to map fields to their variants.  This parameter cannot be
	 * <code>null</code>.
	 */
	public FieldVariantManager (BFA bfa, AbstractIndexManager indexManager){
		super(bfa, indexManager);
	}

	/**
	 * <p>Returns a new variant of the field represented by <code>o</code>.</p>
	 *
	 * @param o the field whose variant is to be returned.  The actual type of <code>o</code> needs to be
	 * <code>SootField</code>.
	 * @return the variant associated with the field represetned by <code>o</code>.
	 */
	protected Variant getNewVariant(Object o) {
		return new FieldVariant((SootField)o, bfa.getFGNode());
	}

}// FieldVariantManager
