package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.Value;

import java.util.Collection;

import org.apache.log4j.Category;

/**
 * AbstractAnalyzer.java
 *
 *
 * Created: Fri Jan 25 14:49:45 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractAnalyzer {

	private static final Category cat = Category.getInstance(AbstractAnalyzer.class.getName());

	protected BFA bfa;

	protected AbstractAnalyzer (String name, ModeFactory mf){
		bfa = new BFA(name, this, mf);
	}

	public final void analyze(SootClassManager scm, SootMethod root) {
		bfa.analyze(scm, root);
	}

	public final Collection getArrayValues(ArrayType a, Context c) {
		return bfa.getArrayVariant(a, c).getFGNode().getValues();
	}

	public final Collection getASTValues(Value v, SootMethod sm, Context c) {
		MethodVariant mv = bfa.getMethodVariant(sm, c);
		ASTVariant astv = mv.getASTVariant(v, c);
		return astv.getFGNode().getValues();
	}

	public final Collection getFieldValues(SootField sf, Context c) {
		return bfa.getFieldVariant(sf, c).getFGNode().getValues();
	}

	public void reset() {
		resetAnalysis();
	}

	protected final void resetAnalysis() {
		bfa.reset();
	}

}// AbstractAnalyzer
