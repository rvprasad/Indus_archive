
package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;

import edu.ksu.cis.bandera.staticanalyses.flow.Prototype;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


//ClassManager.java

/**
 * <p>This class manages class related primitive information and processing such as the processing of <code><clinit></code>
 * methods of classes being analyzed.</p>
 *
 * <p>Created: Fri Mar  8 14:10:27 2002.</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ClassManager
  implements Prototype {
	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ClassManager.class);

	/**
	 * <p>The instance of the framework in which this object is used.
	 *
	 */
	public final BFA bfa;

	/**
	 * <p>The collection of classes for which the information has been processed.</p>
	 *
	 */
	public final Collection classes;

	/**
	 * <p>Describe variable <code>context</code> here.</p>
	 *
	 */
	final Context context;

	/**
	 * <p>Creates a new <code>ClassManager</code> instance.</p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 */
	public ClassManager(BFA bfa) {
		classes  = new HashSet();
		this.bfa = bfa;
		context  = new Context();
	}

	/**
	 * <p>Processes the given class for assimilating class related primitive information into the analysis.  This
	 * implementation hooks in the class initialization method into the analysis. </p>
	 *
	 * @param sc the class to be processed.  This cannot be <code>null</code>.
	 */
	public void process(SootClass sc) {
		if(!classes.contains(sc)) {
			classes.add(sc);

			if(sc.declaresMethod("<clinit>")) {
				context.setRootMethod(sc.getMethod("<clinit>"));
				bfa.getMethodVariant(sc.getMethod("<clinit>"), context);
			} // end of if (sc.declaresMethod("<clinit>"))

			while(sc.hasSuperClass()) {
				sc = sc.getSuperClass();

				if(sc.declaresMethod("<clinit>")) {
					context.setRootMethod(sc.getMethod("<clinit>"));
					bfa.getMethodVariant(sc.getMethod("<clinit>"), context);
				} // end of if (sc.declaresMethod("<clinit>"))
			} // end of while (sc.hasSuperClass())
		} // end of if (!classes.contains(sc))
	}

	/**
	 * <p>Processes the class declaring the given method.</p>
	 *
	 * @param sm the method whose declaring class is to be processed.  This cannot be <code>null</code>.
	 */
	public void process(SootMethod sm) {
		process(sm.getDeclaringClass());
	}

	/**
	 * <p>Creates a concrete object of the same class as this object but parameterized by <code>o</code>.</p>
	 *
	 * @param o the instance of the analysis for which this object shall process information.  The actual type of
	 * <code>o</code> needs to be <code>BFA</code>.  This cannot be <code>null</code>.
	 * @return an instance of <code>ClassManager</code> object parameterized by <code>o</code>.
	 */
	public Object prototype(Object o) {
		return new ClassManager((BFA)o);
	}

	/**
	 * <p>This method is not supported by this class.
	 *
	 * @throws <code>UnsupportedOperationException</code> this method is not supported by this class.
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype() method not supported.");
	}

	/**
	 * <p>Resets the manager.  Removes all information maintained about any classes. </p>
	 *
	 */
	public void reset() {
		classes.clear();
	}
} // ClassManager
