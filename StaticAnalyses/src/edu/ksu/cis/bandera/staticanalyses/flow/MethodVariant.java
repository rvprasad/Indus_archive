package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.BodyRepresentation;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;
import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.SimpleLocalDefs;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.util.ArrayList;
import ca.mcgill.sable.util.Iterator;
import ca.mcgill.sable.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//MethodVariant.java
/**
 * <p>The variant that represents a method implementation.  It maintains variant specific information about local variables
 * and the AST nodes in associated method. It also maintains information about the parameters, this variable, and return
 * values, if any are present. </p>
 *
 * <p>Created: Tue Jan 22 05:27:59 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class MethodVariant implements Variant {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(MethodVariant.class.getName());

	/**
	 * <p>The statement visitor used to process in the statement in the correpsonding method.</p>
	 *
	 */
	protected AbstractStmtSwitch stmt;

	/**
	 * <p>The array of flow graph nodes associated with the parameters of thec corresponding method.  This will be
	 * <code>null</code>, if the associated method has not parameters..</p>
	 *
	 */
	protected final FGNode[] parameters;

	/**
	 * <p>The flow graph nodes associated with the this variable of the corresponding method.  This will be
	 * <code>null</code>, if the associated method is <code>static</code>.</p>
	 *
	 */
	protected final FGNode thisVar;

	/**
	 * <p>The flow graph node associated with an abstract single return point of the corresponding method.  This will be
	 * <code>null</code>, if the associated method's return type is <code>void</code>.</p>
	 *
	 */
	protected final FGNode returnVar;

	/**
	 * <p>The manager of AST node variants.  This is required as in Jimple, the same AST node instance may occur at different
	 * locations in the AST as it serves the purpose of AST representation.</p>
	 *
	 */
	protected final ASTVariantManager astvm;

	/**
	 * <p>This object is used to create <code>Jimple</code> representation of the associated method.  This is required to
	 * extract the list of statement corresponding to the method body and walk over it.</p>
	 *
	 */
	public static final BodyRepresentation bodyrep = Jimple.v();

	/**
	 * <p>The instance of <code>BFA</code> which was responsible for the creation of this variant.
	 *
	 */
	public final BFA bfa;

	/**
	 * <p>The context which resulted in the creation of this variant.</p>
	 *
	 */
	public final Context context;

	/**
	 * <p>The method represented by this variant.</p>
	 *
	 */
	public final SootMethod sm;

	/**
	 * <p>This provides the def sites for local variables in the associated method.  This is used in conjunction with
	 * flow-sensitive information calculation.</p>
	 *
	 */
	protected SimpleLocalDefs defs;

	/**
	 * <p>Creates a new <code>MethodVariant</code> instance.</p>
	 *
	 * @param sm the method represented by this variant.  This parameter cannot be <code>null</code>.
	 * @param astvm the manager of flow graph nodes corresponding to the AST nodes of <code>sm</code>.  This parameter cannot
	 * be <code>null</code>.
	 * @param bfa the instance of <code>BFA</code> which was responsible for the creation of this variant.  This parameter
	 * cannot be <code>null</code>.
	 */
	protected MethodVariant (SootMethod sm, ASTVariantManager astvm, BFA bfa) {
		this.sm = sm;
		this.bfa = bfa;
		context = (Context)bfa.analyzer.context.clone();
		context.callNewMethod(sm);

		logger.debug(">> Method:" + sm + context + "\n" + astvm.getClass());

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

		
		logger.debug("<< Method:" + sm + context + "\n");
	}

	/**
	 * <p>Returns the flow graph node associated with the given AST node in the context defined by
	 * <code>this.context</code>.</p>
	 *
	 * @param v the AST node whose associted flow graph node is to be returned.
	 * @return the flow graph node associated with <code>v</code> in the context <code>this.context</code>.
	 */
	public final FGNode getASTNode(Value v) {
		return getASTVariant(v, context).getFGNode();
	}

	/**
	 * <p>Returns the flow graph node associated with the given AST node in the given context.  Creates a new one if none
	 * exists. </p>
	 *
	 * @param v the AST node whose associted flow graph node is to be returned.
	 * @param c the context in which the flow graph node was associated with <code>v</code>.
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.
	 */
	public final FGNode getASTNode(Value v, Context c) {
		return getASTVariant(v, c).getFGNode();
	}

	/**
	 * <p>Returns the variant associated with the given AST node in the context defined by <code>this.context</code>.  Creates
	 * a new one if none exists.</p>
	 *
	 * @param v the AST node whose associted variant is to be returned.
	 * @return the variant associated with <code>v</code> in the context <code>this.context</code>.
	 */
	public final ASTVariant getASTVariant(Value v) {
		return (ASTVariant)astvm.select(v, context);
	}

	/**
	 * <p>Returns the variant associated with the given AST node in the given context.  Creates a new one if none
	 * exists.</p>
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param context the context in which the variant was associated with <code>v</code>.
	 * @return the variant associated with <code>v</code> in the context <code>c</code>.
	 */
	public final ASTVariant getASTVariant(Value v, Context context) {
		return (ASTVariant)astvm.select(v, context);
	}

	/**
	 * <p> Returns the definitions of local variable <code>l</code> that arrive at statement <code>s</code>.</p>
	 * 
	 * @param l the local for which the definitions are requested.
	 * @param s the statement at which the definitions are requested.
	 * @return the list of definitions of <code>l</code> that arrive at statement <code>s</code>.
	 */
	public List getDefsOfAt(Local l, Stmt s) {
		if (defs == null)
			return new ArrayList();
		else 
			return defs.getDefsOfAt(l, s);	
	}

	/**
	 * <p>Processes the body of the method implementation associated with this variant.</p>
	 */
	public void process() {
		if (sm.isBodyStored(bodyrep)) {
			stmt = bfa.getStmt(this);
			logger.debug(">>>> Starting processing statements of " + sm);
			StmtList list = ((StmtBody)sm.getBody(bodyrep)).getStmtList();
			defs = new SimpleLocalDefs(new CompleteStmtGraph(list));
			for (Iterator i = list.iterator(); i.hasNext();) {
				stmt.process((Stmt)i.next());
			} // end of for (Iterator i = list.iterator(); i.hasNext();)
			logger.debug("<<<< Finished processing statements of " + sm);
		} else {
			stmt = null;
			defs = null;
		} // end of else
	}

	/**
	 * <p>Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.</p>
	 *
	 * @param v the AST node whose associted variant is to be returned.
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.  If none exists,
	 * <code>null</code> is returned.
	 */
	public final FGNode queryASTNode(Value v) {
		return queryASTNode(v, context);
	}

	/**
	 * <p>Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.</p>
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param c the context in which the variant was associated with <code>v</code>.
	 * @return  the flow graph node associated with <code>v</code> in context <code>c</code>.  If none exists,
	 * <code>null</code> is returned.
	 */
	public final FGNode queryASTNode(Value v, Context c) {
		ASTVariant var = queryASTVariant(v, c);
		FGNode temp = null;
		if (var != null) {
			temp = var.getFGNode();
		} // end of if (v != null)
		return temp;
	}

	/**
	 * <p>Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.</p>
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param c a <code>Context</code> value
	 * @return the variant associated with <code>v</code> in the context <code>c</code>.  If none exists,
	 * <code>null</code> is returned.
	 */
	public final ASTVariant queryASTVariant(Value v, Context c) {
		return (ASTVariant)astvm.query(v, c);
	}

	/**
	 * <p>Returns the flow graph node associated with the given parameter.</p>
	 *
	 * @param index the index of the parameter in the parameter list of the associated method.
	 * @return the flow graph node associated with the <code>index</code>th parameter in the parameter list of the associated
	 * method.  It returns <code>null</code> if the method has no parameters.
	 */
	public final FGNode queryParameterNode(int index) {
		FGNode temp = null;
		if (index >= 0 && index <= sm.getParameterCount())
			temp = parameters[index];
		return temp;
	}

	/**
	 * <p>Returns the flow graph node that represents an abstract single return point of the associated method.</p>
	 *
	 * @return the flow graph node that represents an abstract single return point of the associated method.
	 * <code>null</code> if the corresponding method does not return a value.
	 */
	public final FGNode queryReturnNode() {
		return returnVar;
	}

	/**
	 * <p>Returns the flow graph node associated with the <code>this</code> variable of the associated method.</p>
	 *
	 * @return Returns the flow graph node associated with the <code>this</code> variable of the associated method.
	 * <code>null</code> if the corresponding method is <code>static</code>.
	 */
	public final FGNode queryThisNode() {
		return thisVar;
	}

}// MethodVariant
