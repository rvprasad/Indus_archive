/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2003, 2006 Venkatesh Prasad Ranganath
 * 
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * 
 * A copy can be found at  http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 * 
 * You can contact Venkatesh Prasad Ranganath at 
 * 	venkateshprasad.ranganath@gmail.com
 * 
 * SAnToS Laboratory and Venkatesh Prasad Ranganath jointly own 
 * copyrights to 0.8 and earlier versions of this software.  
 * 
 * Venkatesh Prasad Ranganath owns sole copyrights to artifacts 
 * and implementation introduced into Indus beyond version 0.8.
 *******************************************************************************/
package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import edu.ksu.cis.indus.common.collections.MapUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;

/**
 * This class can be used to generated slice criteria based on statements identified by line numbers.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class LineNumberBasedCriteriaGenerator
		extends AbstractStmtBasedSliceCriteriaGenerator<Stmt> {

	/**
	 * A mapping from class names to the line numbers in the class.
	 */
	private Map<String, Collection<String>> className2lineNos;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param props is mapping from fully qualified class names to a comma separated string of line numbers from which the
	 *            slice criteria are generated.
	 */
	public LineNumberBasedCriteriaGenerator(final Properties props) {
		className2lineNos = new HashMap<String, Collection<String>>();
		for (final Object _key : props.keySet()) {
			final String _className = (String) _key;
			final List<String> _lineNos = Arrays.asList(props.getProperty(_className).split(","));
			className2lineNos.put(_className, _lineNos);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected boolean shouldConsiderStmt(final Stmt stmt) {
		boolean _result = false;
		final LineNumberTag _lnt = (LineNumberTag) stmt.getTag("LineNumberTag");
		if (_lnt != null) {
			final String _className = getProcessingMethod().getDeclaringClass().getName();
			final Collection<String> _lineNos = MapUtils.queryCollection(className2lineNos, _className);
			_result = _lineNos.contains(_lnt.toString());
		}
		return _result;
	}
}

// End of File