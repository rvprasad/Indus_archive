
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.staticanalyses.dependency.ControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DivergenceDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.SynchronizationDA;
import edu.ksu.cis.indus.tools.ToolConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This represents the configuration of the slicer.  The slicer tool should be configured via an object of this class
 * obtained from the slicer.  All properties in this configuration have boolean values.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerConfiguration
  extends ToolConfiguration {
	/**
	 * This identifies the property that indicates if interprocedural divergence dependence should be used instead of mere
	 * intraprocedural divergent dependence.
	 */
	public static final PropertyIdentifier INTERPROCEDURAL_DIVERGENCEDA =
		ToolConfiguration.createPropertyIdentifier("interprocedural divergence dependence");

	/**
	 * This identifies the property that indicates if equivalence class based interference dependence should be used  instead
	 * of naive type-based interference dependence.
	 */
	public static final PropertyIdentifier EQUIVALENCE_CLASS_BASED_INTERFERENCEDA =
		ToolConfiguration.createPropertyIdentifier("equivalence class based interference dependennce");

	/**
	 * This identifies the property that indicates if ready dependence should be considered for slicing.
	 */
	public static final PropertyIdentifier USE_READYDA = ToolConfiguration.createPropertyIdentifier("use ready dependence");

	/**
	 * This identifies the property that indicates if rule1 of ready dependence be used.  Rule 1:  m is dependent on n if m
	 * and n occur in the same thread and n is an enter monitor statement.
	 */
	public static final PropertyIdentifier USE_RULE1_IN_READYDA =
		ToolConfiguration.createPropertyIdentifier("use rule1 in ready dependence");

	/**
	 * This identifies the property that indicates if rule2 of ready dependence be used.  Rule 2: m is dependent on n if m
	 * and n occur in different threads and m and n are is exit monitor and enter monitor statements, respectively.
	 */
	public static final PropertyIdentifier USE_RULE2_IN_READYDA =
		ToolConfiguration.createPropertyIdentifier("use rule2 in ready dependence");

	/**
	 * This identifies the property that indicates if rule3 of ready dependence be used.  Rule 3: m is dependent on n if m
	 * and n occur in the same thread and m has a call to java.lang.Object.wait.
	 */
	public static final PropertyIdentifier USE_RULE3_IN_READYDA =
		ToolConfiguration.createPropertyIdentifier("use rule3 in ready dependence");

	/**
	 * This identifies the property that indicates if rule4 of ready dependence be used.  Rule 4: m is dependent on n if m
	 * and n occur in the different thread and m and n have calls to java.lang.Object.wait(XXX) and
	 * java.lang.Object.notifyXXX(), respectively..
	 */
	public static final PropertyIdentifier USE_RULE4_IN_READYDA =
		ToolConfiguration.createPropertyIdentifier("use rule4 in ready dependence");

	/**
	 * This identifies the property that indicates if equivalence class based ready dependence should be used instead of
	 * naive type-based ready dependence.
	 */
	public static final PropertyIdentifier EQUIVALENCE_CLASS_BASED_READYDA =
		ToolConfiguration.createPropertyIdentifier("equivalence class based ready dependennce");

	/**
	 * This identifies the property that indicates if divergence dependence should be considered for slicing.
	 */
	public static final PropertyIdentifier USE_DIVERGENCEDA =
		ToolConfiguration.createPropertyIdentifier("use divergence dependence");

	/**
	 * This identifies the property that indicates if slice criteria should be automatically picked for slicing such that the
	 * slice has the same deadlock behavior as the original program.
	 */
	public static final PropertyIdentifier SLICE_FOR_DEADLOCK =
		ToolConfiguration.createPropertyIdentifier("slice for deadlock");

	/**
	 * This is set of property ids recognized by slicer configuration.
	 */
	static final Set PROPERTY_IDS = new HashSet();

	static {
		PROPERTY_IDS.add(INTERPROCEDURAL_DIVERGENCEDA);
		PROPERTY_IDS.add(EQUIVALENCE_CLASS_BASED_INTERFERENCEDA);
		PROPERTY_IDS.add(EQUIVALENCE_CLASS_BASED_READYDA);
		PROPERTY_IDS.add(USE_READYDA);
		PROPERTY_IDS.add(USE_DIVERGENCEDA);
		PROPERTY_IDS.add(USE_RULE1_IN_READYDA);
		PROPERTY_IDS.add(USE_RULE2_IN_READYDA);
		PROPERTY_IDS.add(USE_RULE3_IN_READYDA);
		PROPERTY_IDS.add(USE_RULE4_IN_READYDA);
		PROPERTY_IDS.add(SLICE_FOR_DEADLOCK);
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerConfiguration.class);

	/**
	 * The collection of ids of the dependences to be considered for slicing.
	 *
	 * @invariant dependencesToUse.oclIsKindOf(String)
	 */
	final Collection dependencesToUse = new HashSet();

	/**
	 * This maps IDs to dependency analyses.
	 *
	 * @invariant id2dependencyAnalysis.oclIsKindOf(Map(Object, DependencyAnalysis))
	 */
	Map id2dependencyAnalysis = new HashMap();

	/**
	 * This maps properties of this configuration to their values.
	 *
	 * @invariant properties != null
	 */
	final Map properties = new HashMap();

	/**
	 * Creates a new SlicerConfiguration object.
	 */
	SlicerConfiguration() {
		id2dependencyAnalysis = new HashMap();
		id2dependencyAnalysis.put(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA, new IdentifierBasedDataDA());
		id2dependencyAnalysis.put(DependencyAnalysis.SYNCHRONIZATION_DA, new SynchronizationDA());
		id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new ReadyDAv2());
		id2dependencyAnalysis.put(DependencyAnalysis.INTERFERENCE_DA, new InterferenceDAv2());
		id2dependencyAnalysis.put(DependencyAnalysis.CONTROL_DA, new ControlDA());
		id2dependencyAnalysis.put(DependencyAnalysis.DIVERGENCE_DA, new DivergenceDA());
		id2dependencyAnalysis.put(DependencyAnalysis.REFERENCE_BASED_DATA_DA, new ReferenceBasedDataDA());
		dependencesToUse.addAll(id2dependencyAnalysis.keySet());
	}

	/**
	 * {@inheritDoc} The given <code>propertyID</code> should be a valid property id declared in this class.  If not, an
	 * exception will be raised.
	 *
	 * @pre value.oclIsKindOf(java.lang.Boolean)
	 */
	public void setProperty(final PropertyIdentifier propertyID, final Object value) {
		if (!PROPERTY_IDS.contains(propertyID)) {
			String message = "Invalid property identifier specified: " + propertyID;
			LOGGER.error(message);
			throw new IllegalArgumentException(message);
		}
		properties.put(propertyID, value);

		Boolean val = (Boolean) value;

		if (propertyID.equals(EQUIVALENCE_CLASS_BASED_READYDA)) {
			if (val.booleanValue()) {
				id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new ReadyDAv2());
			} else {
				id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new ReadyDAv1());
			}
		} else if (propertyID.equals(EQUIVALENCE_CLASS_BASED_INTERFERENCEDA)) {
			if (val.booleanValue()) {
				id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new InterferenceDAv2());
			} else {
				id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new InterferenceDAv1());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(final PropertyIdentifier id) {
		return properties.get(id);
	}
}

/*
   ChangeLog:
   $Log$
 */
