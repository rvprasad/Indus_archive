/*
 * IFSM.java
 *
 * Created on December 12, 2004, 8:25 PM
 */

package edu.ksu.cis.indus.peq.fsm;

import edu.ksu.cis.indus.peq.fsm.datastructures.common.IFSMToken;

/**
 * Represents the finite state machine.
 * @author  Ganeshan
 */
public interface IFSM {
	/**
	 * Takes a transition based on the fsm token.
	 * @param objFsmToken The fsm token
	 * @return boolean
	 */
    boolean doTransition(final IFSMToken objFsmToken);
}
