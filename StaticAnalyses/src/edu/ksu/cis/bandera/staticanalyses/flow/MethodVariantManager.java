package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//MethodVariantManager.java

/**
 * <p>This class manages of  method variants.  This only provides the implementation to create new method variants.  The super
 * class is responsible of managing the variants.</p> 
 *
 * <p>Created: Tue Jan 22 05:21:42 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class MethodVariantManager extends AbstractVariantManager {

	/**
	 * <p>A prototype object used to create index managers related to AST nodes.  Objects created via this prototype object
	 * are used by <code>MethodVariant</code>s to manage the variants corresponding to the AST nodes that exists in them.</p>
	 *
	 */
	protected final AbstractIndexManager astIndexManager;

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(MethodVariantManager.class);

	/**
	 * <p>Creates a new <code>MethodVariantManager</code> instance.</p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This parameter cannot be <code>null</code>.
	 * @param indexManager the manager to indices which are used to map methods to their variants.  This parameter cannot be
	 * <code>null</code>.
	 * @param astIndexManager the prototype object used to create index managers related to AST nodes.  This parameter cannot
	 * be <code>null</code>.
	 */
	MethodVariantManager (BFA bfa, AbstractIndexManager indexManager, AbstractIndexManager astIndexManager){
		super(bfa, indexManager);
		this.astIndexManager =  astIndexManager;
	}

	/**
	 * <p>Returns a new variant of the method represented by <code>o</code>.
	 *
	 * @param o the method whose variant is to be returned.  The actual type of <code>o</code> needs to be
	 * <code>SootMethod</code>.
	 * @return the new <code>MethodVariant</code> corresponding to method <code>o</code>.
	 */
	protected Variant getNewVariant(Object o) {
		return new MethodVariant((SootMethod)o, new ASTVariantManager(bfa, (AbstractIndexManager)astIndexManager.prototype()),
								 bfa);
	}

	/**
	 * <p>Returns the class, starting from the given class and above it in the class hierarchy, that declares the given
	 * method.</p>
	 *
	 * @param sc the class from which to start the search in the class hierarchy.  This parameter cannot be <code>null</code>.
	 * @param sm the method to search for in the class hierarchy.  This parameter cannot be <code>null</code>.
	 * @return the <code>SootMethod</code> corresponding to the implementation of <code>sm</code>.
	 * @throws <code>IllegalStateException</code> if <code>sm</code> is not available in the given branch of the class
	 * hierarchy.
	 */
	public static SootMethod findDeclaringMethod(SootClass sc, SootMethod sm) {
		logger.debug(sc + "." + sm.getName());
		if (sc.declaresMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType())) {
			return sc.getMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType());
		} else if (sc.hasSuperClass()) {
			sc = sc.getSuperClass();
			return findDeclaringMethod(sc, sm);
		} else {
			throw new IllegalStateException("Method " + sm + " not available in class" + sc + ".");
		} // end of else
	}

}// MethodVariantManager
