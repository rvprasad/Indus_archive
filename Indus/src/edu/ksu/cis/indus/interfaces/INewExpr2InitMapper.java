package edu.ksu.cis.indus.interfaces;

import soot.SootMethod;
import soot.jimple.Stmt;
/**
* This interface is used to extract the invocation site that calls the constructor on the created
* instance. In Jimple, one can pick the new expression and pick the immediate following statement with
* <code>&lt;init&gt;</code> invocation expression.  Note that both these statements should occur in the same method.
* However, this can be incorrect in some cases.  A more sound approach is to this approach is to use a data-flow based 
* approach.
*/
public interface INewExpr2InitMapper {
    /**
     * Retrieves the init invocation expression containing statement corresponding to the given new expression containing
     * statement.
     *
     * @param newExprStmt is the statement with the new expression.
     * @param method in which <code>newExprStmt</code> occurs.
     *
     * @return the statement in which the corresponding init invocation expression occurring statement
     *
     * @post result != null and result.contains(InvokeExpr) and result.getInvokeExpr().oclIsKindOf(SpecialInvokeExpr)
     */
    Stmt getInitCallStmtForNewExprStmt(final Stmt newExprStmt, final SootMethod method);
}

/*
 ChangeLog:

 $Log$
 Revision 1.1  2004/02/01 23:33:47  venku
 - extracted the interface of NewExpr2InitMapper to make the
   end analyses configurable.

 */