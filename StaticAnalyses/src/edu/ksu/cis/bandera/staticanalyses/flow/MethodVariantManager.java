package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;

/**
 * MethodVariantManager.java
 *
 *
 * Created: Tue Jan 22 05:21:42 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class MethodVariantManager extends AbstractVariantManager {

	final ASTVariantManager astvm;

	private static final Category cat = Category.getInstance(MethodVariantManager.class.getName());

	MethodVariantManager (BFA bfa, AbstractIndexManager indexManager, AbstractIndexManager astIndices){
		super(bfa, indexManager);
		this.astvm = new ASTVariantManager(bfa, astIndices);
	}

	protected Variant getNewVariant(Object o) {
		return new MethodVariant((SootMethod)o, astvm, bfa);
	}

	public static SootMethod findDeclaringMethod(SootClass sc, String sm) {
		if (sc.declaresMethod(sm)) {
			return sc.getMethod(sm);
		} else if (sc.hasSuperClass()) {
			sc = sc.getSuperClass();
			return findDeclaringMethod(sc, sm);
		} else {
			throw new IllegalStateException(sm + " not available in " + sc + ".");
		} // end of else
	}

	void reset() {
		super.reset();
		astvm.reset();
	}

}// MethodVariantManager
