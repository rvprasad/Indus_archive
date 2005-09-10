
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(SliceCollector.class);

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
	 * Retrieves the uncollected value boxes among the given value boxes.
	 *
	 * @param valueBoxes of interest.
	 *
	 * @return uncollect value boxes.
	 *
	 * @pre valueBoxes != null and valueBoxes.oclIsKindOf(Collection(ValueBox))
	 * @post result != null and result.oclIsKindOf(Collection(ValueBox)) and valueBoxes.containsAll(result)
	 */
	public Collection getUncollected(final List valueBoxes) {
		final Collection _result = new HashSet();
		final Iterator _i = valueBoxes.iterator();
		final int _iEnd = valueBoxes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final ValueBox _vb = (ValueBox) _i.next();

			if (!_vb.hasTag(tagName)) {
				_result.add(_vb);
			}
		}
		return _result;
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
		} else if (!_hostTag.equals(tag)) {
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
		taggedClasses.clear();
	}
}

// End of File
