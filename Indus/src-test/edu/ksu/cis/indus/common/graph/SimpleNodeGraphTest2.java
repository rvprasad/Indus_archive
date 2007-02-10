
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.graph;

import java.util.Collection;
import java.util.HashSet;


/**
 * This class tests <code>getPseudoTails()</code> for a special case.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleNodeGraphTest2
  extends SimpleNodeGraphTest {
	/**
	 * @see edu.ksu.cis.indus.common.graph.SimpleNodeGraphTest#testGetNodesInPathBetween()
	 */
	public void testGetNodesInPathBetween() {
		final Collection _t = new HashSet();
		_t.add(sng.getNode("b"));
		_t.add(sng.getNode("d"));

		final Collection _nodes = dg.getNodesOnPathBetween(_t);
		_t.add(sng.getNode("c"));
		assertTrue(_nodes.containsAll(_t));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#testGetTails()
	 */
	public void testGetTails() {
		assertTrue(dg.getTails().size() == 2);
		assertTrue(dg.getTails().contains(name2node.get("d")));
		assertTrue(dg.getTails().contains(name2node.get("e")));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final SimpleNodeGraph<String>_sng = new SimpleNodeGraph();
		name2node.put("a", _sng.getNode("a"));
		name2node.put("b", _sng.getNode("b"));
		name2node.put("c", _sng.getNode("c"));
		name2node.put("d", _sng.getNode("d"));
		name2node.put("e", _sng.getNode("e"));

		_sng.addEdgeFromTo( name2node.get("a"),  name2node.get("b"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("c"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("d"));
		_sng.addEdgeFromTo( name2node.get("c"),  name2node.get("d"));
		_sng.addEdgeFromTo( name2node.get("c"),  name2node.get("e"));
		_sng.addEdgeFromTo( name2node.get("d"),  name2node.get("b"));
		_sng.addEdgeFromTo( name2node.get("e"),  name2node.get("b"));
		setSNG(_sng);

		numberOfCycles = 3;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestAddEdgeFromTo()
	 */
	protected void localtestAddEdgeFromTo() {
		// do nothing.
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsAncestorOf()
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(dg.isAncestorOf( name2node.get("a"),  name2node.get("d")));
		assertFalse(dg.isAncestorOf( name2node.get("d"),  name2node.get("a")));
		assertTrue(dg.isAncestorOf( name2node.get("b"),  name2node.get("c")));
		assertFalse(dg.isAncestorOf( name2node.get("c"),  name2node.get("b")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsReachable()
	 */
	protected void localtestIsReachable() {
		assertTrue(dg.isReachable( name2node.get("b"),  name2node.get("d"), true));
		assertTrue(dg.isReachable( name2node.get("b"),  name2node.get("d"), false));
		assertFalse(dg.isReachable( name2node.get("c"),  name2node.get("a"), true));
	}
}

// End of File
