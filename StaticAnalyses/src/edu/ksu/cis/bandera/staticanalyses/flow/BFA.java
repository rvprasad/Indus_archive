package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * BFA.java
 *
 *
 * Created: Tue Jan 22 00:45:10 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class BFA {

	private static final Logger logger = LogManager.getLogger(BFA.class);

	private static final Map instances = new HashMap();

	private SootClassManager scm;

	final WorkList worklist;

	public final AbstractAnalyzer analyzer;

	private final ModeFactory modeFactory;

	ArrayVariantManager arrayManager;

	ClassManager classManager;

	FieldVariantManager instanceFieldManager;

	MethodVariantManager methodManager;

	FieldVariantManager staticFieldManager;

	BFA (String name, AbstractAnalyzer analyzer, ModeFactory mf) {
		worklist = new WorkList();
		modeFactory = mf;
		this.analyzer = analyzer;
		BFA.instances.put(name, this);
		this.classManager = modeFactory.getClassManager(this);

		arrayManager = new ArrayVariantManager(this, modeFactory.getArrayIndexManager());
		instanceFieldManager = new FieldVariantManager(this, modeFactory.getInstanceFieldIndexManager());
		methodManager = new MethodVariantManager(this, modeFactory.getMethodIndexManager(), modeFactory.getASTIndexManager());
		staticFieldManager = new FieldVariantManager(this, modeFactory.getStaticFieldIndexManager());
	}

	void analyze(SootClassManager scm, SootMethod root) {
		this.scm = scm;
		methodManager.select(root, analyzer.context);
		worklist.process();
	}

	public final ArrayVariant getArrayVariant(ArrayType a) {
		return getArrayVariant(a, analyzer.context);
	}

	public final ArrayVariant getArrayVariant(ArrayType a, Context context) {
		return (ArrayVariant)arrayManager.select(a, context);
	}

	public static final BFA getBFA(String name) {
		BFA temp = null;
		if (instances.containsKey(name)) {
			temp = (BFA)instances.get(name);
		}
		return temp;
	}

	public final SootClass getClass(String className) {
		return scm.getClass(className);
	}

	public final FieldVariant getFieldVariant(SootField sf) {
		return getFieldVariant(sf, analyzer.context);
	}

	public final FieldVariant getFieldVariant(SootField sf, Context context) {
		Variant temp = null;
		if (Modifier.isStatic(sf.getModifiers())) {
			temp = staticFieldManager.select(sf, context);
		} else {
			temp = instanceFieldManager.select(sf, context);
		} // end of else
		return (FieldVariant)temp;
	}

	public final AbstractFGNode getFGNode() {
		return modeFactory.getFGNode(worklist);
	}

	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getLHSExpr(e);
	}

	public final MethodVariant getMethodVariant(SootMethod sm) {
		return getMethodVariant(sm, analyzer.context);
	}

	public final MethodVariant getMethodVariant(SootMethod sm, Context context) {
		return (MethodVariant)methodManager.select(sm, context);
	}

	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch e) {
		return modeFactory.getRHSExpr(e);
	}

	public final SootClassManager getSootClassManager() {
		return scm;
	}

	public final AbstractStmtSwitch getStmt(MethodVariant e) {
		return modeFactory.getStmt(e);
	}

	public void reset() {
		arrayManager.reset();
		instanceFieldManager.reset();
		methodManager.reset();
		staticFieldManager.reset();

		worklist.clear();
		scm = null;
	}

}// BFA
