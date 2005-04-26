
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

package edu.ksu.cis.indus.staticanalyses;

import edu.ksu.cis.indus.staticanalyses.flow.indexmanagement.IIndexManagementStrategy;
import edu.ksu.cis.indus.staticanalyses.flow.indexmanagement.MemoryIntensiveIndexManagementStrategy;
import edu.ksu.cis.indus.staticanalyses.flow.indexmanagement.ProcessorIntensiveIndexManagementStrategy;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;


/**
 * This class contains constants specific to staticanalyses library.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Constants {
	/** 
	 * This is the name of the property via which the constants properties file can be specified.  It's value is
	 * "indus.staticanalyses.constant.configuration.properties.file".
	 */
	public static final String CONSTANTS_CONFIGURATION_FILE_PROPERTY =
		"indus.staticanalyses.constant.configuration.properties.file";

	/** 
	 * The constant that represents the value of <code>INDEX_MANAGEMENT_STRATEGY_PROPERTY</code> to use memory intensive
	 * index management strategy. It's value is "MEMORY_INTENSIVE_INDEX_MANAGEMENT".
	 */
	public static final String MEMORY_INTENSIVE_INDEX_MANAGEMENT = "MEMORY_INTENSIVE_INDEX_MANAGEMENT";

	/** 
	 * The constant that represents the value of <code>INDEX_MANAGEMENT_STRATEGY_PROPERTY</code> to use processor intensive
	 * index management strategy. It's value is "PROCESSOR_INTENSIVE_INDEX_MANAGEMENT".
	 */
	public static final String PROCESSOR_INTENSIVE_INDEX_MANAGEMENT = "PROCESSOR_INTENSIVE_INDEX_MANAGEMENT";

	/** 
	 * The name of the property via which index management strategy can be altered.  The name is
	 * "edu.ksu.cis.indus.staticanalyses.flow.indexManagementStrategy".
	 */
	public static final String INDEX_MANAGEMENT_STRATEGY_PROPERTY =
		"edu.ksu.cis.indus.staticanalyses.flow.indexManagementStrategy";

	/** 
	 * This is the property that the user can specify to control the interval between the application of SCC-based
	 * optimziation. The name of the property is "edu.ksu.cis.indus.staticanalyses.flow.FA.sccOptimizationInterval". If
	 * unspecified, the interval defaults to <i>10000</i>.
	 */
	public static final String SCC_OPTIMIZATION_INTERVAL_PROPERTY =
		"edu.ksu.cis.indus.staticanalyses.flow.FA.sccOptimizationInterval";

	/** 
	 * The name of the property the user can use to configure the token manager class.  It's name is
	 * "edu.ksu.cis.indus.staticanalyses.tokens.TokenManagerClass".
	 */
	public static final String INDUS_STATICANALYSES_TOKENMANAGERTYPE =
		"edu.ksu.cis.indus.staticanalyses.tokens.TokenManagerClass";

	/** 
	 * This is the default length of the interval between which SCC-based optimization is applied to the flow network.  It is
	 * 1000.
	 */
	private static final int DEFAULT_SCC_OPTIMIZATION_INTERVAL = 1000;

	/** 
	 * This contains the constants.
	 */
	private static final Properties CONFIGURATIONS = new Properties();

	static {
		final String _propFileName = System.getProperty(CONSTANTS_CONFIGURATION_FILE_PROPERTY);

		if (_propFileName != null) {
			try {
				final InputStream _stream = ClassLoader.getSystemResourceAsStream(_propFileName);
				CONFIGURATIONS.load(_stream);
			} catch (IOException _e) {
				System.err.println("Well, error loading property file.  Bailing.");
				throw new RuntimeException(_e);
			}
		}
	}

	///CLOVER:OFF

	/**
	 * Creates an instance of this class.
	 */
	private Constants() {
	}

	///CLOVER:ON

	/**
	 * Returns the index management strategy.
	 *
	 * @return index management strategy.
	 *
	 * @post result != null
	 */
	public static IIndexManagementStrategy getIndexManagementStrategy() {
		final String _t = CONFIGURATIONS.getProperty(INDEX_MANAGEMENT_STRATEGY_PROPERTY);
		final IIndexManagementStrategy _result;

		if (_t != null && _t.equals(MEMORY_INTENSIVE_INDEX_MANAGEMENT)) {
			_result = new MemoryIntensiveIndexManagementStrategy();
		} else {
			_result = new ProcessorIntensiveIndexManagementStrategy();
		}
		return _result;
	}

	/**
	 * Retrieves the SCC optimization interval for flow analysis graph.  This defaults to
	 * <code>DEFAULT_SCC_OPTIMIZATION_INTERVAL</code>.
	 *
	 * @return the interval.
	 */
	public static int getSCCOptimizationIntervalForFA() {
		final int _defaultValue = DEFAULT_SCC_OPTIMIZATION_INTERVAL;
		final String _key = SCC_OPTIMIZATION_INTERVAL_PROPERTY;
		final int _result;
		_result = edu.ksu.cis.indus.common.soot.Constants.retrieveIntValue(_defaultValue, _key, CONFIGURATIONS);
		return _result;
	}

	/**
	 * Retrieves the name of the token manager class.  This is configured by the property
	 * "edu.ksu.cis.indus.staticanalyses.tokens.TokenManagerClass".  It can be one of the following.
	 * 
	 * <ul>
	 * <li>
	 * edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager
	 * </li>
	 * <li>
	 * edu.ksu.cis.indus.staticanalyses.tokens.CollectionTokenManager
	 * </li>
	 * <li>
	 * edu.ksu.cis.indus.staticanalyses.tokens.IntegerTokenManager
	 * </li>
	 * </ul>
	 * 
	 * <p>
	 * By default, an instance of <code>edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager</code> is returned.
	 * </p>
	 *
	 * @return the name of token manager class.
	 *
	 * @post result != null
	 */
	public static String getTokenManagerType() {
		final String _result;
		final String _t = CONFIGURATIONS.getProperty(INDUS_STATICANALYSES_TOKENMANAGERTYPE);

		if (_t != null) {
			_result = _t;
		} else {
			_result = "edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager";
		}
		return _result;
	}
}

// End of File
