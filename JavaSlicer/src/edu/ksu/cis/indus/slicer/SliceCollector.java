
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

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.NamedTag;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

import soot.tagkit.Host;


/**
 * This collects the parts of the system that form the slice by tagging the AST of the system.  This just tags the parts of
 * the system that form the slice.  It is primarily intended to be driven by the slicing engine.  However, the application
 * may do some post processing and may use this to extend the slice in ways appropriate for the application.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SliceCollector {
	/** 
	 * An instance to be used to satisfy <code>Tag.getValue()</code> call on <code>SlicingTag</code> objects.
	 */
	static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceCollector.class);

	/** 
	 * The collection of classes that were tagged.
	 */
	private final Collection taggedClasses = new HashSet();

	/** 
	 * The collection of methods that were tagged.
	 */
	private final Collection taggedMethods = new HashSet();

	/** 
	 * The tag to be used during transformation.
	 */
	private NamedTag tag;

	/** 
	 * This is the slicing engine to be used for slicing.
	 */
	private SlicingEngine engine;

	/** 
	 * The name of the tag instance active in this instance of the transformer.
	 */
	private String tagName;

	/**
	 * Creates a new SliceCollector object.
	 *
	 * @param theEngine is the slicing tool/engine that calculates the slice.
	 */
	SliceCollector(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * Retrieves the classes included in the slice.
	 *
	 * @return a collection of classes included in the slice.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 */
	public Collection getClassesInSlice() {
		return Collections.unmodifiableCollection(taggedClasses);
	}

	/**
	 * Retrieves <code>Host</code> objects from hosts which are collected by the tag used by this collector.
	 *
	 * @param hosts is the collection of hosts
	 *
	 * @return a collection of collected hosts.
	 *
	 * @pre hosts != null and hosts.oclIsKindOf(Collection(Host))
	 * @post result != null and result.oclIsKindOf(Collection(Host)) and hosts.containsAll(result)
	 */
	public List getCollected(final Collection hosts) {
		final List _result = new ArrayList();

		for (final Iterator _i = hosts.iterator(); _i.hasNext();) {
			final Host _host = (Host) _i.next();

			if (hasBeenCollected(_host)) {
				_result.add(_host);
			}
		}
		return _result;
	}

	/**
	 * Retrieves the methods included in the slice.
	 *
	 * @return a collection of methods included in the slice.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	public Collection getMethodsInSlice() {
		return Collections.unmodifiableCollection(taggedMethods);
	}

	/**
	 * Retrieves the tag name used by this collector.
	 *
	 * @return the name of the tag used.
	 *
	 * @post result != null
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Checks if the given host has been collected/tagged.
	 *
	 * @param host to be checked.
	 *
	 * @return <code>true</code>
	 */
	public boolean hasBeenCollected(final Host host) {
		final NamedTag _temp = (NamedTag) host.getTag(tagName);
		return _temp != null;
	}

	/**
	 * Tags the given host with a name tag of the configured name.  This is used to indicate that the host is included in the
	 * slice.
	 *
	 * @param host is a part of the AST to be tagged.
	 *
	 * @pre host != null
	 */
	public void includeInSlice(final Host host) {
		final NamedTag _hostTag = (NamedTag) host.getTag(tagName);

		if (_hostTag == null) {
			host.addTag(tag);

			if (host instanceof SootMethod) {
				taggedMethods.add(host);
			} else if (host instanceof SootClass) {
				taggedClasses.add(host);
			}

			if (LOGGER.isDebugEnabled()) {
				Object _o = host;

				if (host instanceof ValueBox) {
					_o = ((ValueBox) host).getValue();
				}
				LOGGER.debug("Tagged[1]: " + _o);
			}
		} else if (_hostTag != tag) {
			host.removeTag(tagName);
			host.addTag(tag);

			if (LOGGER.isDebugEnabled()) {
				Object _temp;

				if (host instanceof ValueBox) {
					_temp = ((ValueBox) host).getValue();
				} else {
					_temp = host;
				}
				LOGGER.debug("Tagged[2]: " + _temp);
			}
		} else if (LOGGER.isDebugEnabled()) {
			Object _temp;

			if (host instanceof ValueBox) {
				_temp = ((ValueBox) host).getValue();
			} else {
				_temp = host;
			}
			LOGGER.debug("Already Tagged: " + _temp);
		}
	}

	/**
	 * Includes the given collection of objects into the slice.
	 *
	 * @param hosts to be included in the slice.
	 *
	 * @pre hosts != null and hosts.oclIsKindOf(Collection(Host))
	 * @pre hosts->forall(o | o != null)
	 */
	public void includeInSlice(final Collection hosts) {
		for (final Iterator _i = hosts.iterator(); _i.hasNext();) {
			final Host _host = (Host) _i.next();
			includeInSlice(_host);
		}
	}

	/**
	 * Processes the goto statements in the system to ensure that appropriate gotos are included to ensure the control flow
	 * is not broken.  This is possible when parts of 2 basic blocks are interspersed with gotos stringing these parts
	 * together into a basic block.
	 */
	public void processGotos() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing Gotos");
		}

		final SliceGotoProcessor _gotoProcessor = new SliceGotoProcessor(this);
		final BasicBlockGraphMgr _bbgMgr = engine.getBasicBlockGraphManager();
		_gotoProcessor.process(taggedMethods, _bbgMgr);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringWriter _sw = new StringWriter();
		final PrintWriter _pw = new PrintWriter(_sw);

		for (final Iterator _i = taggedClasses.iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();
			_pw.println("Class: " + _sc);

			_pw.println("  Fields:");

			for (final Iterator _j = getCollected(_sc.getFields()).iterator(); _j.hasNext();) {
				_pw.println("    " + _j.next());
			}

			_pw.println("  Methods:");

			for (final Iterator _j = getCollected(_sc.getMethods()).iterator(); _j.hasNext();) {
				final SootMethod _method = (SootMethod) _j.next();
				_pw.println("    " + _method);

				if (_method.hasActiveBody()) {
					for (final Iterator _k = getCollected(_method.getActiveBody().getUnits()).iterator(); _k.hasNext();) {
						final Stmt _stmt = (Stmt) _k.next();
						_pw.println("      " + _stmt);

						for (final Iterator _l = getCollected(_stmt.getUseAndDefBoxes()).iterator(); _l.hasNext();) {
							_pw.println("        " + ((ValueBox) _l.next()).getValue());
						}
					}
				}
			}
		}
		_pw.flush();
		_pw.close();
		return _sw.getBuffer().toString();
	}

	/**
	 * Set the tag name to be used.
	 *
	 * @param theTagName to be used during this transformation.  If none are specified, then a default built-in tag name is
	 * 		  used.
	 */
	void setTagName(final String theTagName) {
		if (theTagName != null) {
			tag = new NamedTag(theTagName);
			tagName = theTagName;
		}
	}

	/**
	 * Ensures that the control flow between the program points included in the slice preserve the semantics.  Gotos give
	 * rise to this situation.
	 */
	void completeSlicing() {
		processGotos();
	}

	/**
	 * Resets internal data structure.  Tag related information is not reset.
	 */
	void reset() {
		taggedMethods.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2004/06/12 06:47:27  venku
   - documentation.
   - refactoring.
   - coding conventions.
   - catered feature request 384, 385, and 386.
   Revision 1.12  2004/05/31 21:38:10  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.11  2004/05/10 08:12:03  venku
   - streamlined the names of tags that are used.
   - deleted SlicingTag class.  NamedTag is used instead.
   - ripple effect.
   - SliceCriteriaFactory's interface is enhanced to generate individual
     slice criterion as well as criteria set for all nodes in the given AST chunk.
   Revision 1.10  2004/04/24 08:26:13  venku
   - changed the details of logging of pre/post processing slices.
   Revision 1.9  2004/04/24 07:48:26  venku
   - added toString() method for convenience.
   Revision 1.8  2004/02/01 22:16:16  venku
   - renamed set/getSlicedBBGMgr to set/getBasicBlockGraphManager
     in SlicingEngine.
   - ripple effect.
   Revision 1.7  2004/01/25 08:56:00  venku
   - formatting and coding convention.
   Revision 1.6  2004/01/24 01:48:58  venku
   - added logic to track tagged class
   - added method to retrieve tagged classes
   - removed commons-collection filter as it was not being used.
   Revision 1.5  2004/01/22 01:01:40  venku
   - coding convention.
   Revision 1.4  2004/01/19 11:39:11  venku
   - added new batched includeInSlice() method to SliceCollector.
   - used new includeInSlice() method in CompleteSliceGotoProcessor.
   Revision 1.3  2004/01/13 09:42:52  venku
   - coding convention.
   Revision 1.2  2004/01/13 07:48:03  venku
   - exposed the methods that are included in the slice via getMethodsInSlice() method.
   Revision 1.1  2004/01/13 04:33:39  venku
   - Renamed TaggingBasedSliceCollector to SliceCollector.
   - Ripple effect in the engine.
   - SlicingEngine does not handle issues such as executability
     as they do not affect the generated slice.  The slice can be
     transformed independent of the slice via postprocessing to
     adhere to such properties.
   Revision 1.18  2004/01/11 03:44:50  venku
   - ripple effect of changes to GotoProcessors.
   Revision 1.17  2003/12/16 12:44:49  venku
   - safety check.
   Revision 1.16  2003/12/16 00:13:12  venku
   - logging.
   Revision 1.15  2003/12/15 16:31:46  venku
   - deleted tagHost and inlined it in includeInSlice.
   Revision 1.14  2003/12/13 19:46:33  venku
   - documentation of SliceCollector.
   - renamed collect() to includeInSlice().
   Revision 1.13  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.12  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.11  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.10  2003/12/07 22:13:12  venku
   - renamed methods in SliceCollector.
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
   - made SliceCollector package private.
   - removed inheritance based dependence on ITransformer
     for SliceCollector.
   Revision 1.1  2003/11/24 10:11:32  venku
   - there are no residualizers now.  There is a very precise
     slice collector which will collect the slice via tags.
   - architectural change. The slicer is hard-wired wrt to
     slice collection.  Residualization is outside the slicer.
   Revision 1.1  2003/11/24 09:46:49  venku
   - moved ISliceCollector and SliceCollector
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
