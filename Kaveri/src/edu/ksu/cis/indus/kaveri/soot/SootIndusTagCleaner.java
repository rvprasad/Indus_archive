/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
 
package edu.ksu.cis.indus.kaveri.soot;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;


import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.util.Chain;


/**
 * @author ganeshan
 *
 * Purge all the tags from the soot classes.
 */
public class SootIndusTagCleaner extends Job  {
        
    private String tagToClean;
    
    private final String myJobFamilyId = "edu.ksu.cis.indus.kaveri.soottagremover";
    /**
     * Constructor.
     * @param name Name of the job
     * @param indusTag The tag to clean
     */
    public SootIndusTagCleaner(final String  name, final String indusTag) {
        super(name);       
        this.tagToClean = indusTag;
    }

    /**
     * Remove the indus tag from the soot class.
     * @param sootClass The soot class.
     * 
     */
    private void purgeSootClass(SootClass sootClass) {        
        sootClass.removeTag(tagToClean);
        final Chain _fieldChain = sootClass.getFields();
        for (Iterator iter = _fieldChain.snapshotIterator(); iter.hasNext();) {
            final SootField _sf = (SootField) iter.next();
            _sf.removeTag(tagToClean);            
        }        
        
        final List _lst = sootClass.getMethods();
        for (int _i = 0; _i < _lst.size(); _i++) {           
            final SootMethod _sm = (SootMethod) _lst.get(_i);            
            purgeSootMethod(_sm);
        }
    }

    /**
     * Purge the soot method.
     * @param sm The soot method
     */
    private void purgeSootMethod(SootMethod _sm) {
        _sm.removeTag(tagToClean);
        if (_sm.hasActiveBody()) {            
            final Body _body = _sm.getActiveBody();
            purgeBody(_body);
        }
        
    }

    /**
     * Purge the tags from the body.
     * @param body The soot body
     */
    private void purgeBody(Body body) {
        final Chain _chain = body.getUnits();
        for (Iterator iter = _chain.snapshotIterator(); iter.hasNext();) {
            final Stmt _stmt = (Stmt) iter.next();
            _stmt.removeTag(tagToClean);                        
        }                        
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IStatus run(IProgressMonitor monitor) {
        final Chain _sootClasses = Scene.v().getClasses();
        monitor.beginTask(getName(), _sootClasses.size());
        int _ctr = 0;
        KaveriErrorLog.logInformation(this + " beginning slice tag cleanup");
        for (Iterator iter = _sootClasses.snapshotIterator(); iter.hasNext();) {
            if (monitor.isCanceled()) {
                break;
            }
            final SootClass _class = (SootClass) iter.next();
            purgeSootClass(_class);
            monitor.worked(_ctr++);
        }
        KaveriErrorLog.logInformation(this + " finished slice tag cleanup");
        return Status.OK_STATUS;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
     */
    public boolean belongsTo(Object family) {        
        return family.equals(myJobFamilyId);
    }
}
