package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.ParameterRef;
import ca.mcgill.sable.soot.jimple.ThisRef;
import ca.mcgill.sable.soot.jimple.Value;
import java.util.Collection;
import org.apache.log4j.Logger;

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

	private static final Logger logger = Logger.getLogger(AbstractAnalyzer.class.getName());

	protected BFA bfa;

	public final Context context;

	protected boolean active;

	protected AbstractAnalyzer (String name, ModeFactory mf){
		context = new Context();
		bfa = new BFA(name, this, mf);
		bfa.init();
		active = false;
	}

	public final void analyze(SootClassManager scm, SootMethod root) {
		if (root == null) {
			throw new IllegalStateException("Root method cannot be null.");
		} // end of if (root == null)
		context.setRootMethod(root);
		active = true;
		bfa.analyze(scm);
		active = false;
	}

	public final Collection getValues(ArrayType a) {
		return bfa.getArrayVariant(a, context).getFGNode().getValues();
	}

	public final Collection getValues(ParameterRef p) {
		MethodVariant mv = bfa.getMethodVariant((SootMethod)context.getCurrentMethod(), context);
		return mv.getParameterNode(p.getIndex()).getValues();
	}

	public final Collection getValues(SootField sf) {
		return bfa.getFieldVariant(sf, context).getFGNode().getValues();
	}

	public final Collection getValues(ThisRef t) {
		MethodVariant mv = bfa.getMethodVariant((SootMethod)context.getCurrentMethod(), context);
		return mv.getThisNode().getValues();
	}

	public final Collection getValues(Value v) {
		MethodVariant mv = bfa.getMethodVariant((SootMethod)context.getCurrentMethod(), context);
		ASTVariant astv = mv.getASTVariant(v, context);
		return astv.getFGNode().getValues();
	}

	public void reset() {
		resetAnalysis();
	}

	protected final void resetAnalysis() {
		bfa.reset();
	}

}// AbstractAnalyzer
