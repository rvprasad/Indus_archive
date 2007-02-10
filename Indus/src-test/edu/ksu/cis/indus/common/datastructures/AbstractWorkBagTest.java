
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.IndusTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This class tests <code>AbstractWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractWorkBagTest
  extends IndusTestCase {
	/** 
	 * The workbag.
	 */
	protected IWorkBag wb;

	/**
	 * Tests <code>addAllWork</code>.
	 */
	public void testAddAllWork() {
		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);

		while (wb.hasWork()) {
			assertTrue(_workPieces.contains(wb.getWork()));
		}
	}

	/**
	 * Tests <code>addAllWorkNoDuplicates</code>.
	 */
	public void testAddAllWorkNoDuplicates() {
		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);

		final int _two = 2;
		final int _five = 5;

		final List _subList = new ArrayList(_workPieces.subList(_two, _five));
		_subList.add(new Object());

		final Collection _temp = wb.addAllWorkNoDuplicates(_subList);
		assertTrue(_subList.containsAll(_temp));
		assertTrue(!_temp.containsAll(_subList));
	}

	/**
	 * Tests <code>addWorkNoDuplicate</code>.
	 */
	public void testAddWorkNoDuplicates() {
		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);
		assertFalse(wb.addWorkNoDuplicates(_workPieces.get(0)));
		assertTrue(wb.addWorkNoDuplicates(new Object()));
	}

	/**
	 * Tests <code>clear</code>.
	 */
	public final void testClear() {
		wb.clear();
		assertFalse(wb.hasWork());

		try {
			wb.getWork();
			///CLOVER:OFF            
			fail("This is incorrect behavior.");
			///CLOVER:ON
		} catch (IllegalStateException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Tests <code>getWork</code> and <code>hasWork</code>.
	 */
	public void testGetWorkandHasWork() {
		final Object _object = new Object();
		wb.addWork(_object);
		assertTrue(wb.hasWork());

		final Object _temp = wb.getWork();
		assertTrue(_temp != null);
		assertTrue(_temp == _object);
	}

	/**
	 * Provides new collection of work pieces.
	 *
	 * @return a collection of work pieces.
	 */
	protected List getWorkPieces() {
		final List _result = new ArrayList();
		final int _ten = 10;

		for (int _i = 1; _i <= _ten; _i++) {
			_result.add(String.valueOf(_i));
		}
		return _result;
	}
}

// End of File
