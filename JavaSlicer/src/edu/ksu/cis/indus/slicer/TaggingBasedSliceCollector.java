
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.ValueBox;

import soot.jimple.Stmt;

import soot.tagkit.Host;

import soot.util.Chain;


/**
 * This collects the parts of the system that form the slice by tagging the AST of the system.  The residualization should be
 * drivern by the tags in the AST.
 * 
 * <p>
 * After residualization, the application can query the system for tags of kind <code>SlicingTag</code> and retrieve slicing
 * information of the system.  However, as locals cannot be tagged, the application should use the references to locals in
 * the slice to include them.  Similar technique should be used for catch table.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class TaggingBasedSliceCollector {
	/**
	 * An instance to be used to satisfy <code>Tag.getValue()</code> call on <code>SlicingTag</code> objects.
	 */
	static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TaggingBasedSliceCollector.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection taggedMethods = new HashSet();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private SlicingEngine engine;

	/**
	 * The tag to be used during transformation.
	 */
	private SlicingTag tag;

	/**
	 * The name of the tag instance active in this instance of the transformer.
	 */
	private String tagName;

	/**
	 * Creates a new TaggingBasedSliceCollector object.
	 *
	 * @param theEngine DOCUMENT ME!
	 */
	TaggingBasedSliceCollector(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param host DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected boolean hasBeenCollected(final Host host) {
		final SlicingTag _temp = (SlicingTag) host.getTag(tagName);
		return _temp != null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected void completeSlicing() {
		if (engine.sliceType.equals(SlicingEngine.BACKWARD_SLICE) && engine.executableSlice) {
			makeBackwardSliceExecutable();
		}
		processGotos();
	}

	/**
	 * Set the tag name to be used.
	 *
	 * @param theTagName to be used during this transformation.  If none are specified, then a default built-in tag name is
	 * 		  used.
	 */
	void setTagName(final String theTagName) {
		if (theTagName != null) {
			tag = new SlicingTag(theTagName);
			tagName = theTagName;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	String getTagName() {
		return tagName;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param host DOCUMENT ME!
	 */
	void collect(final Host host) {
		tagHost(host);
	}

	/**
	 * Resets internal data structure.  Tag related information is not reset.
	 */
	void reset() {
		taggedMethods.clear();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	private void makeBackwardSliceExecutable() {
		final BasicBlockGraphMgr _bbgMgr = engine.getSlicedBasicBlockGraphMgr();

		// pick all return/throw points in the methods.
		for (final Iterator _i = taggedMethods.iterator(); _i.hasNext();) {
			final SootMethod _method = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = _bbgMgr.getBasicBlockGraph(_method);
			final Collection _tails = _bbg.getTails();

			for (final Iterator _j = _tails.iterator(); _j.hasNext();) {
				final BasicBlock _bb = (BasicBlock) _j.next();
				final Stmt _stmt = _bb.getTrailerStmt();

				if (_stmt.getTag(tagName) == null) {
					//collect(stmt, method);
					collect(_stmt);
					collect(_method);
				}
			}

			/*
			 * Include the first statement of the handler for all traps which cover atleast one statement included in the
			 * slice
			 */
			final Body _body = _method.getActiveBody();

			if (_body != null) {
				final Chain _sl = _body.getUnits();

				for (final Iterator _j = _sl.iterator(); _j.hasNext();) {
					final Stmt _stmt = (Stmt) _j.next();

					if (hasBeenCollected(_stmt)) {
						for (final Iterator _k = TrapManager.getTrapsAt(_stmt, _body).iterator(); _k.hasNext();) {
							collect(((Trap) _k.next()).getHandlerUnit());
						}
					}
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not get body for method " + _method.getSignature());
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("END: Generating criteria for exception");
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	private void processGotos() {
		IGotoProcessor gotoProcessor = null;

		if (engine.sliceType.equals(SlicingEngine.BACKWARD_SLICE)) {
			gotoProcessor = new SliceGotoProcessor(this, true);
		} else if (engine.sliceType.equals(SlicingEngine.FORWARD_SLICE)) {
			gotoProcessor = new SliceGotoProcessor(this, false);
		} else if (engine.sliceType.equals(SlicingEngine.COMPLETE_SLICE)) {
			gotoProcessor = new CompleteSliceGotoProcessor(this);
		}

		final BasicBlockGraphMgr _bbgMgr = engine.getSlicedBasicBlockGraphMgr();

		// include all gotos required to recreate the control flow of the system.
		for (final Iterator _i = taggedMethods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = _bbgMgr.getBasicBlockGraph(_sm);

			if (_bbg == null) {
				continue;
			}
			gotoProcessor.preprocess(_sm);

			for (final Iterator _j = _bbg.getNodes().iterator(); _j.hasNext();) {
				final BasicBlock _bb = (BasicBlock) _j.next();
				gotoProcessor.process(_bb);
			}
			gotoProcessor.postprocess();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param host DOCUMENT ME!
	 */
	private void tagHost(final Host host) {
		SlicingTag theTag;

		theTag = tag;

		final SlicingTag _hostTag = (SlicingTag) host.getTag(tagName);

		if (_hostTag == null) {
			if (host.getTag(tagName) != null) {
				host.removeTag(tagName);
			}
			host.addTag(theTag);

			if (host instanceof SootMethod) {
				taggedMethods.add(host);
			}

			if (LOGGER.isDebugEnabled()) {
				Object o = host;

				if (host instanceof ValueBox) {
					o = ((ValueBox) host).getValue();
				}
				LOGGER.debug("Tagged: " + o);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.11  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.10  2003/12/07 22:13:12  venku
   - renamed methods in TaggingBasedSliceCollector.

   Revision 1.9  2003/12/04 12:10:12  venku
   - changes that take a stab at interprocedural slicing.

   Revision 1.8  2003/12/02 09:42:17  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/12/01 12:21:25  venku
   - methods in collector underwent a lot of change to minimize them.
   - ripple effect.
   Revision 1.6  2003/11/30 13:21:35  venku
   - removed uncalled method.
   - changed the logic used to check tags of statements.
   Revision 1.5  2003/11/30 02:38:44  venku
   - changed the name of SLICING_TAG.
   Revision 1.4  2003/11/25 00:00:45  venku
   - added support to include gotos in the slice.
   - added logic to include all tail points in the slice after slicing
     and only in case of backward executable slice.
   - added logic to include exceptions in a limited way.
   Revision 1.3  2003/11/24 18:21:30  venku
   - logging.
   Revision 1.2  2003/11/24 16:47:31  venku
   - moved inner classes as external class.
   - made TaggingBasedSliceCollector package private.
   - removed inheritance based dependence on ITransformer
     for TaggingBasedSliceCollector.
   Revision 1.1  2003/11/24 10:11:32  venku
   - there are no residualizers now.  There is a very precise
     slice collector which will collect the slice via tags.
   - architectural change. The slicer is hard-wired wrt to
     slice collection.  Residualization is outside the slicer.
   Revision 1.1  2003/11/24 09:46:49  venku
   - moved ISliceCollector and TaggingBasedSliceCollector
     into slicer package.
   - The idea is to collect the slice based on annotation which
     can be as precise as we require and then layer on
     top of that the slicer residualization logic, either constructive or destructive.
   Revision 1.2  2003/11/24 07:31:03  venku
   - deleted method2locals, executable, and sliceType as they were not used.
   Revision 1.1  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.17  2003/11/17 01:39:42  venku
   - added slice XMLization support.
   Revision 1.16  2003/11/16 23:12:17  venku
   - coding convention.
   Revision 1.15  2003/11/16 22:55:31  venku
   - added new methods to support processing of seed criteria.
     This is not same as slicing seed criteria of which we do not
     make any distinction.
   Revision 1.14  2003/11/13 14:08:08  venku
   - added a new tag class for the purpose of recording branching information.
   - renamed fixReturnStmts() to makeExecutable() and raised it
     into ISliceCollector interface.
   - ripple effect.
   Revision 1.13  2003/11/05 09:05:28  venku
   - For strange reasons the StringTag does not fulfill our needs.
     So, we introduce a new class, SlicingTag.
   Revision 1.12  2003/11/05 08:32:50  venku
   - transformation are supported per entity basis.  This
     means each expression in a statement and needs to
     be tagged separately.  The containing statement
     should also be tagged likewise.
   Revision 1.11  2003/11/03 08:02:31  venku
   - ripple effect of changes to ITransformer.
   - added logging.
   - optimization.
   Revision 1.10  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.9  2003/10/13 01:00:09  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.8  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.7  2003/09/27 01:27:46  venku
   - documentation.
   Revision 1.6  2003/09/26 15:08:35  venku
   - ripple effect of changes in ITransformer.
   Revision 1.5  2003/09/15 07:52:08  venku
   - added a new transformer interface specifically targetted for slicing.
   - implemented the above interface.
   Revision 1.4  2003/08/25 07:17:38  venku
   Exposed initialize() as a public method.
   Removed SlicingTag class and used StringTag instead.
   Revision 1.3  2003/08/21 09:30:31  venku
    - added a new transform() method which can transform at the level of ValueBox.
    - CloningBasedSliceResidualizer does not do anything in this new method.
   Revision 1.2  2003/08/20 18:31:22  venku
   Documentation errors fixed.
   Revision 1.1  2003/08/19 12:55:50  venku
   This is a tag-based non-destructive slicing transformation implementation.
 */
