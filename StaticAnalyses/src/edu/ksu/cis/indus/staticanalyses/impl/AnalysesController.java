
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

package edu.ksu.cis.indus.staticanalyses.impl;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.IActivePart;

import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.IAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class is provides the control class for the analyses suite. The analyses progress in phases. It may be so that some
 * application require a particular sequence in which each analysis should progress. Hence, the applications provide an
 * implementation of controller interface to drive the analyses in a particular sequence of phases.
 * 
 * <p>
 * This implementation will drive the given analyses such that each analysei is executed only when the anlaysis indicates
 * that all it's prerequesites have been fulfilled.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class AnalysesController {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AnalysesController.class);

	/** 
	 * The map of analysis being controlled by this object. It maps names of analysis to the analysis object.
	 *
	 * @invariant participatingAnalyses != null
	 * @invariant participatingAnalyses.oclIsTypeOf(Map(Object, AbstractAnalysis)
	 */
	protected final Map participatingAnalyses;

	/** 
	 * This is the preprocessing controlling agent.
	 *
	 * @invariant preprocessController != null;
	 */
	protected final ProcessingController preprocessController;

	/** 
	 * The object used to realize the "active" part of this object.
	 */
	private final IActivePart.ActivePart activePart = new IActivePart.ActivePart();

	/** 
	 * This provides basic block graphs for the analyses.
	 */
	private BasicBlockGraphMgr basicBlockGraphMgr;

	/** 
	 * This is a map of name to objects which provide information that maybe used by analyses, but is of no use to the
	 * controller.
	 */
	private Map info;

	/**
	 * Creates a new AbstractAnalysesController object.
	 *
	 * @param infoPrm is a map of name to objects which provide information that maybe used by analyses, but is of no use to
	 * 		  the controller.
	 * @param pc is the preprocessing controller.
	 * @param bbgMgr provides basic blocks graphs for methods.  If this is non-null then the analyses are initialized with
	 * 		  this graph manager.  If not, the graph managers of the analyses will not be initialized.  Hence, it should be
	 * 		  done by the application.
	 *
	 * @pre pc != null
	 */
	public AnalysesController(final Map infoPrm, final ProcessingController pc, final BasicBlockGraphMgr bbgMgr) {
		participatingAnalyses = new HashMap();
		info = infoPrm;
		preprocessController = pc;
		basicBlockGraphMgr = bbgMgr;
	}

	/**
	 * Provides the implementation registered for the given analysis purpose.
	 *
	 * @param id of the requested analyses.  This has to be one of the names(XXX_DA) defined in this class.
	 *
	 * @return the implementation registered for the given purpose.  Changes to this collection is visible to the controller.
	 *
	 * @post result != null and result->forall(o | o != null and o.oclIsKindOf(AbstractAnalysis))
	 */
	public final Collection getAnalyses(final Object id) {
		final Collection _result;

		if (participatingAnalyses != null) {
			_result = (Collection) participatingAnalyses.get(id);
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Adds the implementations to be used for analysis.
	 *
	 * @param id of the analysis.
	 * @param analyses are the implementations of the named analysis.
	 *
	 * @pre id != null and analyses != null and analysis->forall(o | o != null and o.oclIsKindOf(IAnalysis))
	 */
	public final void addAnalyses(final Object id, final Collection analyses) {
		CollectionsUtilities.putAllIntoListInMap(participatingAnalyses, id, analyses);
	}

	/**
	 * Returns the active part of this object.
	 *
	 * @return the active part.
	 */
	public IActivePart getActivePart() {
		return activePart;
	}

	/**
	 * Executes the analyses in the registered order.
	 */
	public void execute() {
		boolean _analyzing;
		final Collection _done = new ArrayList();

		do {
			_analyzing = false;

			for (final Iterator _i = participatingAnalyses.keySet().iterator(); _i.hasNext() && activePart.canProceed();) {
				final String _daName = (String) _i.next();
				final Collection _c = (Collection) participatingAnalyses.get(_daName);

				for (final Iterator _j = _c.iterator(); _j.hasNext();) {
					final IAnalysis _analysis = (IAnalysis) _j.next();

					if (_analysis != null && !_done.contains(_analysis)) {
						_analysis.analyze();

						final boolean _t = _analysis.isStable();

						if (_t) {
							_done.add(_analysis);
						}
						_analyzing |= _t;
					}
				}
			}
		} while (_analyzing);
	}

	/**
	 * Initializes the controller. Analyses are initialized and then driven to preprocess the system (in that order only).
	 */
	public void initialize() {
		final Collection _failed = new ArrayList();
		final Collection _preprocessors = new HashSet();

		for (final Iterator _k = participatingAnalyses.keySet().iterator(); _k.hasNext() && activePart.canProceed();) {
			final Object _key = _k.next();
			final Collection _c = (Collection) participatingAnalyses.get(_key);

			for (final Iterator _j = _c.iterator(); _j.hasNext() && activePart.canProceed();) {
				final IAnalysis _analysis = (IAnalysis) _j.next();

				try {
					_analysis.initialize(info);

					if (_analysis.doesPreProcessing()) {
						final IProcessor _p = _analysis.getPreProcessor();
						_p.hookup(preprocessController);
						_preprocessors.add(_p);
					}

					if (basicBlockGraphMgr != null) {
						_analysis.setBasicBlockGraphManager(basicBlockGraphMgr);
					}
				} catch (final InitializationException _e) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(_analysis.getClass() + " failed to initialize, hence, it will not executed.", _e);
					}
					_failed.add(_key);

					if (_analysis.doesPreProcessing()) {
						_preprocessors.remove(_analysis.getPreProcessor());
					}
				}
			}

			for (final Iterator _i = _failed.iterator(); _i.hasNext();) {
				_c.remove(_i.next());
			}
		}

		if (activePart.canProceed()) {
			preprocessController.process();
		}

		for (final Iterator _i = _preprocessors.iterator(); _i.hasNext();) {
			((IProcessor) _i.next()).unhook(preprocessController);
		}
	}

	/**
	 * Resets the internal data structures of the controller.  This resets the participating analyses.  This does not reset
	 * the Object Flow Analysis instance.
	 */
	public void reset() {
		for (final Iterator _i = participatingAnalyses.values().iterator(); _i.hasNext();) {
			final Collection _c = (Collection) _i.next();

			for (final Iterator _j = _c.iterator(); _j.hasNext();) {
				final IAnalysis _analysis = (IAnalysis) _j.next();
				_analysis.reset();
			}
		}
		participatingAnalyses.clear();
		activePart.activate();
	}
}

// End of File
