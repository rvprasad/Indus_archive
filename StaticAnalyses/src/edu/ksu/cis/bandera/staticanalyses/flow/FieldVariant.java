package edu.ksu.cis.bandera.staticanalyses.flow;


import ca.mcgill.sable.soot.SootField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//FieldVariant.java

/**
 * <p>The variant that represents a field.</p>
 *
 * <p>Created: Fri Jan 25 14:29:09 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class FieldVariant extends AbstractValuedVariant {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(FieldVariant.class);

	/**
	 * <p>The field represented by this variant.</p>
	 *
	 */
	public final SootField field;

	/**
	 * <p>Creates a new <code>FieldVariant</code> instance.</p>
	 *
	 * @param field the field to be represented by this variant.  This cannot be <code>null</code>.
	 * @param node the node associated with this variant.  This cannot be <code>null</code>.
	 */
	public FieldVariant (SootField field, FGNode node) {
		super(node);
		this.field = field;
	}

}// FieldVariant
