package edu.ksu.cis.indus.staticanalyses.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependencyAnalysisUtil {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	public static final Collection IDENTIFIERS;

	static {
		final Collection _ids = new ArrayList();
		_ids.add(IDependencyAnalysis.SYNCHRONIZATION_DA);
		_ids.add(IDependencyAnalysis.CONTROL_DA);
		_ids.add(IDependencyAnalysis.REFERENCE_BASED_DATA_DA);
		_ids.add(IDependencyAnalysis.DIVERGENCE_DA);
		_ids.add(IDependencyAnalysis.INTERFERENCE_DA);
		_ids.add(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);
		_ids.add(IDependencyAnalysis.READY_DA);
		IDENTIFIERS = Collections.unmodifiableCollection(_ids);
	}

	/**
	 * Creates a new IDependencyAnalysisHelper object.
	 */
	private DependencyAnalysisUtil() {
	}

}

/*
ChangeLog:

$Log$
*/