
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;


/**
 * This class manages of  method variants.  This only provides the implementation to create new method variants.  The super
 * class is responsible of managing the variants.
 * 
 * <p>
 * Created: Tue Jan 22 05:21:42 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class MethodVariantManager
  extends AbstractVariantManager {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(MethodVariantManager.class);

	/** 
	 * A prototype object used to create index managers related to AST nodes.  Objects created via this prototype object are
	 * used by <code>MethodVariant</code>s to manage the variants corresponding to the AST nodes that exists in them.
	 */
	protected final IPrototype astIMPrototype;

	/**
	 * Creates a new <code>MethodVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.  This parameter cannot be
	 * 		  <code>null</code>.
	 * @param indexManager the manager to indices which are used to map methods to their variants.  This parameter cannot be
	 * 		  <code>null</code>.
	 * @param astIndexManagerPrototype the prototype object used to create index managers related to AST nodes.  This
	 * 		  implementation should support <code>getClone()</code>.
	 *
	 * @pre theAnalysis != null and indexManager != null and astIndexManagerPrototype != null
	 */
	MethodVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager,
		final IPrototype astIndexManagerPrototype) {
		super(theAnalysis, indexManager);
		this.astIMPrototype = astIndexManagerPrototype;
	}

	/**
	 * Returns the class, starting from the given class and above it in the class hierarchy, that declares the given method.
	 *
	 * @param sc the class from which to start the search in the class hierarchy.  This parameter cannot be
	 * 		  <code>null</code>.
	 * @param sm the method to search for in the class hierarchy.  This parameter cannot be <code>null</code>.
	 *
	 * @return the <code>SootMethod</code> corresponding to the implementation of <code>sm</code>.
	 *
	 * @throws IllegalStateException if <code>sm</code> is not available in the given branch of the class hierarchy.
	 *
	 * @pre sc != null and sm != null
	 * @post result != null
	 */
	public static SootMethod findDeclaringMethod(final SootClass sc, final SootMethod sm) {
		final SootMethod _result;

		if (sc.declaresMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType())) {
			_result = sc.getMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType());
		} else if (sc.hasSuperclass()) {
			_result = findDeclaringMethod(sc.getSuperclass(), sm);
		} else {
			throw new IllegalStateException("Method " + sm + " not available in class " + sc + ".");
		}
		return _result;
	}

	/**
	 * Returns a new variant of the method represented by <code>o</code>.
	 *
	 * @param o the method whose variant is to be returned.
	 *
	 * @return the new <code>MethodVariant</code> corresponding to method <code>o</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(SootMethod)
	 * @post result != null
	 */
	protected IVariant getNewVariant(final Object o) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("STATS: Processing method: " + o + "\t number: " + (auxGetVariantCount() + 1));
		}

		return new MethodVariant((SootMethod) o, new ASTVariantManager(fa, (AbstractIndexManager) astIMPrototype.getClone()),
			fa);
	}
}

// End of File
