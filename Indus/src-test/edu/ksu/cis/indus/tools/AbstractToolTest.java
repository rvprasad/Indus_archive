
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.tools;

import junit.framework.TestCase;


/**
 * This tests <code>AbstractTool</code>.  <i>This test case is incomplete.</i> 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AbstractToolTest
  extends TestCase {
	/** 
	 * The instance of the tool to test.
	 */
	private TestTool testTool;

	/**
	 * An implementation of <code>AbstractTool</code>.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class TestTool
	  extends AbstractTool {
		/** 
		 * A dummy phase object.
		 */
		final Phase localPH = Phase.createPhase();

		/** 
		 * Synchronization flag.
		 */
		boolean finished;

		/**
		 * @see edu.ksu.cis.indus.tools.ITool#getPhase()
		 */
		public Object getPhase() {
			return null;
		}

		/**
		 * @see edu.ksu.cis.indus.tools.ITool#destringizeConfiguration(java.lang.String)
		 */
		public boolean destringizeConfiguration(final String stringizedForm) {
			return true;
		}

		/**
		 * @see edu.ksu.cis.indus.tools.ITool#initialize()
		 */
		public void initialize() {
		}

		/**
		 * @see edu.ksu.cis.indus.tools.ITool#reset()
		 */
		public void reset() {
		}

		/**
		 * @see edu.ksu.cis.indus.tools.ITool#stringizeConfiguration()
		 */
		public String stringizeConfiguration() {
			return null;
		}

		/**
		 * @see edu.ksu.cis.indus.tools.AbstractTool#execute(Phase)
		 */
		protected void execute(final Phase phase)
		  throws InterruptedException {
			movingToNextPhase();
			localPH.nextMajorPhase();
			finished = true;
		}
	}

	/**
	 * Tests <code>pause()</code> and <code>resume()</code> in asynchronous mode.
	 */
	public final void testPauseAndResumeInAsyncMode() {
		try {
			testTool.pause();
		} catch (final Exception _e) {
			fail("Should be able to pause tool that is not running.");
		}

		final Phase _ph = Phase.createPhase();
		testTool.pause();
		testTool.run(_ph, false);
		_ph.nextMinorPhase();
		assertTrue(testTool.localPH.isEarlierThan(_ph));
		testTool.resume();

		while (!testTool.finished) {
			;
		}
		assertTrue(_ph.isEarlierThan(testTool.localPH));
	}
    
    /**
     * Tests <code>pause()</code> and <code>resume()</code> in synchronous mode.
     */
    public final void testPauseAndResumeInSyncMode() {
        try {
            testTool.pause();
        } catch (final Exception _e) {
            fail("Should be able to pause tool that is not running.");
        }

        final Phase _ph = Phase.createPhase();
        testTool.pause();
        final Thread _thread = new Thread(new Runnable() {
            public void run() {
                testTool.run(_ph, true);       
            }
        });
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException _e) {
            final IllegalStateException _t = new IllegalStateException();
            _t.initCause(_e);
            throw _t;
        }

        assertTrue(_ph.equalsMajor(testTool.localPH));
        assertTrue(_ph.equalsMinor(testTool.localPH));
        
        _thread.interrupt();
        
        
    }

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
		testTool = new TestTool();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		super.tearDown();
		testTool = null;
	}
}

// End of File
