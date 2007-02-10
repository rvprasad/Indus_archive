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
