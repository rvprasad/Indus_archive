package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import edu.ksu.cis.bandera.bfa.Prototype;
import java.util.Collection;
import java.util.HashSet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * ClassManager.java
 *
 *
 * Created: Fri Mar  8 14:10:27 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ClassManager implements Prototype {

	private static final Logger logger = LogManager.getLogger(ClassManager.class);

	public final BFA bfa;

	public final Collection classes;

	final Context context;

	public ClassManager (BFA bfa){
		classes = new HashSet();
		this.bfa = bfa;
		context = new Context();
	}

	public void process(SootClass sc) {
		if (!classes.contains(sc)) {
			classes.add(sc);

			if (sc.declaresMethod("<clinit>")) {
					bfa.methodManager.select(sc.getMethod("<clinit>"), context);
			} // end of if (sc.declaresMethod("<clinit>"))
			while (sc.hasSuperClass()) {
				sc = sc.getSuperClass();
				if (sc.declaresMethod("<clinit>")) {
					bfa.methodManager.select(sc.getMethod("<clinit>"), context);
				} // end of if (sc.declaresMethod("<clinit>"))

			} // end of while (sc.hasSuperClass())

		} // end of if (!classes.contains(sc))

	}

	public void process(SootMethod sm) {
		process(sm.getDeclaringClass());
	}

	public Object prototype(Object param1) {
		return new ClassManager((BFA)param1);
	}

	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype() method not supported.");
	}

	public void reset() {
		classes.clear();
	}

}// ClassManager
