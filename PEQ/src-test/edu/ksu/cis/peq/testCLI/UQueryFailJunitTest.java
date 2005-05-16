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
 
package edu.ksu.cis.peq.testCLI;

import java.util.Collection;

import junit.framework.Assert;
import junit.framework.TestCase;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import edu.ksu.cis.peq.queryengine.IUQMatcher;
import edu.ksu.cis.peq.queryengine.UniversalQueryEngine$v1;
import edu.ksu.cis.peq.test.fsm.FSMBuilder;
import edu.ksu.cis.peq.test.graph.GraphBuilder$v2;
import edu.ksu.cis.peq.test.misc.Matcher;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UQueryFailJunitTest extends TestCase {

    private IGraphEngine gEngine;
    private IFSM fsm;
    private IUQMatcher matcher;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        gEngine = new GraphBuilder$v2();
        fsm = new FSMBuilder();
        matcher = new Matcher();
        
    }
    

    public void testExecutePass() {
        UniversalQueryEngine$v1 e = new UniversalQueryEngine$v1(gEngine, fsm, matcher);        
            e.execute();
            final Collection _c = e.getResults();
            
            Assert.assertTrue(_c.size() == 0);                   
    }

}
