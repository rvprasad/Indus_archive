package edu.ksu.cis.indus.common.soot;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.TrapManager;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;


public class ExceptionFlowSensitiveStmtGraph extends UnitGraph {
    /**
     * DOCUMENT ME! 
     * @param unitBody DOCUMENT ME!
     * @param addExceptionEdges DOCUMENT ME!
     */
    protected ExceptionFlowSensitiveStmtGraph(final Body unitBody) {
        super(unitBody, true);
        
        for (final Iterator _i = TrapManager.getTrappedUnitsOf(unitBody).iterator(); _i.hasNext();) {
            final Stmt _unit = (Stmt) _i.next();
            final List _traps = TrapManager.getTrapsAt(_unit, unitBody);
            
        }
    }
}


/*
ChangeLog:

$Log$
*/