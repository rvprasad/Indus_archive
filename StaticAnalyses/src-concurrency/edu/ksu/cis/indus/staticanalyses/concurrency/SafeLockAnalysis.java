

package edu.ksu.cis.indus.staticanalyses.concurrency;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IMonitorInfo;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class SafeLockAnalysis extends AbstractAnalysis {

    IMonitorInfo monitorInfo;

    private static final Log LOGGER = LogFactory.getLog(SafeLockAnalysis.class);
    /**
     * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
     */
    public void reset() {
        super.reset();
        monitorInfo = null;
    }

    /**
     * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
     */
    protected void setup() throws InitializationException {
        monitorInfo = (IMonitorInfo)info.get(IMonitorInfo.ID);
        if (monitorInfo  == null) { 
            LOGGER.error("An interface with id, " + IMonitorInfo.ID + ", was not provided.");
            throw new InitializationException("An interface with id, " + IMonitorInfo.ID + ", was not provided.");
        }            
    }

    /**
     * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
     */
    public void analyze() {
        if (monitorInfo.isStable()) {
            
            processNoWaitFreeLoops();
            processNoWaitOnOtherLocks();
            processNoLockingOfUnsafeLocks();
        }

    }

    /**
     * 
     */
    private void processNoLockingOfUnsafeLocks() {
        // TODO: Auto-generated method stub
        
    }

    /**
     * 
     */
    private void processNoWaitOnOtherLocks() {
        // TODO: Auto-generated method stub
        
    }

    /**
     * 
     */
    private void processNoWaitFreeLoops() {
        // TODO: Auto-generated method stub
        
    }
    
    private boolean stable;

    /**
     * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
     */
    public boolean isStable() {
        return stable;
    }

}


/*
ChangeLog:

$Log$
*/