package edu.ksu.cis.bandera.bfa;



import ca.mcgill.sable.soot.BodyRepresentation;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;
import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.SimpleLocalDefs;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.util.Iterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * MethodVariant.java
 *
 *
 * Created: Tue Jan 22 05:27:59 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class MethodVariant implements Variant {

	private static final Logger logger = LogManager.getLogger(MethodVariant.class.getName());

	protected final AbstractStmtSwitch stmt;

	protected final AbstractFGNode[] parameters;

	protected final AbstractFGNode thisVar;

	protected final AbstractFGNode returnVar;

	protected final ASTVariantManager astvm;

	public static final BodyRepresentation bodyrep = Jimple.v();

	public final BFA bfa;

	public final Context context;

	public final SootMethod sm;

	public final SimpleLocalDefs defs;

	protected MethodVariant (SootMethod sm, ASTVariantManager astvm, BFA bfa) {
		this.sm = sm;
		this.bfa = bfa;
		context = (Context)bfa.analyzer.context.clone();
		context.callNewMethod(sm);

		logger.debug("Method:" + sm + context + "\n" + astvm.getClass());

		bfa.classManager.process(sm);

		if (!sm.isStatic()) {
			thisVar = bfa.getFGNode();
		} // end of if (!sm.isStatic())
		else {
			thisVar = null;
		} // end of else

		if (!(sm.getReturnType() instanceof VoidType)) {
			returnVar = bfa.getFGNode();
		} // end of if (sm.getReturnType() instanceof VoidType)
		else {
			returnVar = null;
		} // end of else

		if (sm.getParameterCount() > 0) {
			parameters = new AbstractFGNode[sm.getParameterCount()];
			for (int i = 0; i < sm.getParameterCount(); i++) {
				parameters[i] = bfa.getFGNode();
			} // end of for (int i = 0; i < sm.getParameterCount(); i++)
		} // end of if (sm.getParameterCount() > 0)
		else {
			parameters = new AbstractFGNode[0];
		} // end of else

		this.astvm = astvm;

		if (sm.isBodyStored(bodyrep)) {
			stmt = bfa.getStmt(this);
			logger.debug("Starting processing statements of " + sm);
			StmtList list = ((StmtBody)sm.getBody(bodyrep)).getStmtList();
			defs = new SimpleLocalDefs(new CompleteStmtGraph(list));
			for (Iterator i = list.iterator(); i.hasNext();) {
				stmt.process((Stmt)i.next());
			} // end of for (Iterator i = list.iterator(); i.hasNext();)
			logger.debug("Finished processing statements of " + sm);
		} else {
			stmt = null;
			defs = null;
		} // end of else

	}

	public final FGNode getASTNode(Value v) {
		return getASTVariant(v).getFGNode();
	}

	public final FGNode getASTNode(Value v, Context c) {
		return getASTVariant(v, c).getFGNode();
	}

	public final ASTVariant getASTVariant(Value v) {
		return (ASTVariant)astvm.select(v, context);
	}

	public final ASTVariant getASTVariant(Value v, Context context) {
		return (ASTVariant)astvm.select(v, context);
	}

	public final FGNode getParameterNode(int index) {
		return parameters[index];
	}

	public final FGNode getReturnNode() {
		return returnVar;
	}

	public final FGNode getThisNode() {
		return thisVar;
	}

}// MethodVariant
