
/*
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

package edu.ksu.cis.indus.slicer;

import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @author venku To change this generated comment go to  Window>Preferences>Java>Code Generation>Code Template
 */
public class BranchingSlicingTag
  extends SlicingTag {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	List targets;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private int count;

	/**
	 * Creates a new BranchingSlicingTag object.
	 *
	 * @param name DOCUMENT ME!
	 * @param targetCount DOCUMENT ME!
	 * @param isSeed DOCUMENT ME!
	 */
	BranchingSlicingTag(final String name, final int targetCount, final boolean isSeed) {
		super(name, isSeed);
		targets = new ArrayList(targetCount);
		count = targetCount;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public int getTargetCount() {
		return targets.size();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param index DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Stmt getTargetStmt(final int index) {
		return (Stmt) targets.get(index);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param index DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 *
	 * @throws IndexOutOfBoundsException DOCUMENT ME!
	 */
	void setTargetStmt(final int index, final Stmt stmt) {
		if (index >= 0 || index < count) {
			targets.add(index, stmt);
		} else {
			throw new IndexOutOfBoundsException(index + " is outside the range 0-" + (count - 1) + ".");
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
