package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.jimple.Stmt;


public class InitResolver extends AbstractProcessor {

    /**
     * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
     */
    public void hookup(final ProcessingController ppc) {
    // TODO: Auto-generated method stub

    }

    /**
     * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
     */
    public void unhook(final ProcessingController ppc) {
    // TODO: Auto-generated method stub

    }

    /**
     * @param stmt
     * @param method
     * @return
     */
    public Stmt getInitCallStmt(final Stmt stmt, final SootMethod method) {
        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
     */
    public void consolidate() {
        // TODO: Auto-generated method stub
        super.consolidate();
    }
    
    /**
     * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
     */
    public void processingBegins() {
        // TODO: Auto-generated method stub
        super.processingBegins();
    }
}


/*
ChangeLog:

$Log$
*/