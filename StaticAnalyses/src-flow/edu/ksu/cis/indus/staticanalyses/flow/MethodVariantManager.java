
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
		if (sc.declaresMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType())) {
			return sc.getMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType());
		} else if (sc.hasSuperclass()) {
			return findDeclaringMethod(sc.getSuperclass(), sm);
		} else {
			throw new IllegalStateException("Method " + sm + " not available in class " + sc + ".");
		}
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

/*
   ChangeLog:
   $Log$
   Revision 1.8  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.7  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/30 23:18:34  venku
   A small quirk in stat logging. FIXED.
   Revision 1.5  2003/08/30 23:15:17  venku
   Added support to display statistics in managers.
   Revision 1.4  2003/08/30 22:39:20  venku
   Added support to query statistics of the managers.
   Revision 1.3  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
