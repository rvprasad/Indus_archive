
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.slicer.SliceCollector;
import edu.ksu.cis.indus.slicer.SliceGotoProcessor;
import edu.ksu.cis.indus.slicer.transformations.ExecutableSlicePostProcessorAndModifier;

import edu.ksu.cis.indus.tools.slicer.processing.ISlicePostProcessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.SootClass;


/**
 * This is a helper class for the slicer tool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerToolHelper {
	/**
	 * Creates an instance of this class.
	 */
	private SlicerToolHelper() {
	}

	/**
	 * Injects executability using the given processor into the slice calculated by the given tool.
	 *
	 * @param tool of interest.
	 * @param processor to be used to inject executability.
	 *
	 * @pre tool != null and processor != null
	 */
	public static void injectExecutability(final SlicerTool tool, final ISlicePostProcessor processor) {
		final SliceCollector _collector = tool.getSliceCollector();
		final Collection _methods = _collector.getMethodsInSlice();
		final SliceGotoProcessor _gotoProcessor = new SliceGotoProcessor(_collector);
		final BasicBlockGraphMgr _bbgMgr = tool.getBasicBlockGraphManager();
		processor.process(_methods, _bbgMgr, _collector);
		_gotoProcessor.process(_methods, _bbgMgr);
	}

	/**
	 * Optimizes the slice calculated by the given tool for space.  This method should be called after residualization.
	 *
	 * @param tool in which the slice should be optimized.
	 *
	 * @pre tool != null
	 */
	public static void optimizeForSpaceAfterResidualization(final SlicerTool tool) {
		final Collection _exclusions = new HashSet();
		_exclusions.add("java.io.Serializable");
		_exclusions.add("java.lang.Throwable");
		_exclusions.add("java.lang.Cloneable");
		_exclusions.add("java.lang.Runnable");

		final Collection _classesToErase = new HashSet();
		final Collection _classes = tool.getSystem().getClasses();
		final Iterator _i = _classes.iterator();
		final int _iEnd = _classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = (SootClass) _i.next();

			if (_sc.getMethods().size() == 0 && _sc.getFields().size() == 0 && !_exclusions.contains(_sc.getName())) {
				_classesToErase.add(_sc);
			}
		}
		Util.eraseClassesFrom(_classesToErase, tool.getSystem());
	}

	/**
	 * Optimizes the slice calculated by the given tool for space.  This method should be called before residualization.
	 *
	 * @param tool in which the slice should be optimized.
	 *
	 * @pre tool != null
	 */
	public static void optimizeForSpaceBeforeResidualization(final SlicerTool tool) {
		final ExecutableSlicePostProcessorAndModifier _processor =
			new ExecutableSlicePostProcessorAndModifier(tool.getSystem());
		injectExecutability(tool, _processor);
	}
}

// End of File
