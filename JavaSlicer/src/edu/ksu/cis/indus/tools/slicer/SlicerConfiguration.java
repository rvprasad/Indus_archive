
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

import edu.ksu.cis.indus.slicer.SlicingEngine;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DivergenceDA;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;
import edu.ksu.cis.indus.tools.AbstractToolConfiguration;
import edu.ksu.cis.indus.tools.IToolConfigurationFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * This represents a configurationCollection of the slicer.  The slicer tool should be configured via an object of this class
 * obtained from the slicer tool.  The type of the propoerty values are documented with the property identifiers.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerConfiguration
  extends AbstractToolConfiguration
  implements IToolConfigurationFactory {
	/**
	 * This identifies the property that indicates if interprocedural divergence dependence should be used instead of mere
	 * intraprocedural divergent dependence.
	 */
	public static final Object INTERPROCEDURAL_DIVERGENCEDA = "interprocedural divergence dependence";

	/**
	 * This identifies the property that indicates if equivalence class based interference dependence should be used  instead
	 * of naive type-based interference dependence.
	 */
	public static final Object EQUIVALENCE_CLASS_BASED_INTERFERENCEDA = "equivalence class based interference dependennce";

	/**
	 * This identifies the property that indicates if ready dependence should be considered for slicing.
	 */
	public static final Object USE_READYDA = "use ready dependence";

	/**
	 * This identifies the property that indicates if rule1 of ready dependence be used.  Rule 1:  m is dependent on n if m
	 * and n occur in the same thread and n is an enter monitor statement.
	 */
	public static final Object USE_RULE1_IN_READYDA = "use rule1 in ready dependence";

	/**
	 * This identifies the property that indicates if rule2 of ready dependence be used.  Rule 2: m is dependent on n if m
	 * and n occur in different threads and m and n are is exit monitor and enter monitor statements, respectively.
	 */
	public static final Object USE_RULE2_IN_READYDA = "use rule2 in ready dependence";

	/**
	 * This identifies the property that indicates if rule3 of ready dependence be used.  Rule 3: m is dependent on n if m
	 * and n occur in the same thread and m has a call to java.lang.Object.wait.
	 */
	public static final Object USE_RULE3_IN_READYDA = "use rule3 in ready dependence";

	/**
	 * This identifies the property that indicates if rule4 of ready dependence be used.  Rule 4: m is dependent on n if m
	 * and n occur in the different thread and m and n have calls to java.lang.Object.wait(XXX) and
	 * java.lang.Object.notifyXXX(), respectively..
	 */
	public static final Object USE_RULE4_IN_READYDA = "use rule4 in ready dependence";

	/**
	 * This identifies the property that indicates if equivalence class based ready dependence should be used instead of
	 * naive type-based ready dependence.
	 */
	public static final Object EQUIVALENCE_CLASS_BASED_READYDA = "equivalence class based ready dependennce";

	/**
	 * This identifies the property that indicates if divergence dependence should be considered for slicing.
	 */
	public static final Object USE_DIVERGENCEDA = "use divergence dependence";

	/**
	 * This identifies the property that indicates if slice criteria should be automatically picked for slicing such that the
	 * slice has the same deadlock behavior as the original program.
	 */
	public static final Object SLICE_FOR_DEADLOCK = "slice for deadlock";

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final Object EXECUTABLE_SLICE = "executable slice";

	/**
	 * This identifies the property that indicates the slice type, i.e., forward or complete slice.
	 */
	public static final Object SLICE_TYPE = "slice type";

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final Collection PROPERTY_IDS_CACHE = new ArrayList();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static boolean uninitialized = true;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected boolean executableSlice = true;

	/**
	 * This indicates if the tool should criteria that ensure the deadlock behavior of the slice is same as that of the
	 * original program.
	 */
	protected boolean sliceForDeadlock = false;

	/**
	 * The collection of ids of the dependences to be considered for slicing.
	 *
	 * @invariant dependencesToUse.oclIsKindOf(String)
	 */
	private final Collection dependencesToUse = new HashSet();

	/**
	 * This maps IDs to dependency analyses.
	 *
	 * @invariant id2dependencyAnalysis.oclIsKindOf(Map(Object, DependencyAnalysis))
	 */
	private Map id2dependencyAnalysis = new HashMap();

	/**
	 * Creates a new SlicerConfiguration object.
	 */
	public SlicerConfiguration() {
		if (uninitialized) {
			PROPERTY_IDS_CACHE.add(USE_DIVERGENCEDA);
			PROPERTY_IDS_CACHE.add(INTERPROCEDURAL_DIVERGENCEDA);
			PROPERTY_IDS_CACHE.add(EQUIVALENCE_CLASS_BASED_INTERFERENCEDA);
			PROPERTY_IDS_CACHE.add(USE_READYDA);
			PROPERTY_IDS_CACHE.add(EQUIVALENCE_CLASS_BASED_READYDA);
			PROPERTY_IDS_CACHE.add(USE_RULE1_IN_READYDA);
			PROPERTY_IDS_CACHE.add(USE_RULE2_IN_READYDA);
			PROPERTY_IDS_CACHE.add(USE_RULE3_IN_READYDA);
			PROPERTY_IDS_CACHE.add(USE_RULE4_IN_READYDA);
			PROPERTY_IDS_CACHE.add(SLICE_FOR_DEADLOCK);
			PROPERTY_IDS_CACHE.add(SLICE_TYPE);
			PROPERTY_IDS_CACHE.add(EXECUTABLE_SLICE);
			uninitialized = false;
		}
		PROPERTY_IDS.addAll(PROPERTY_IDS_CACHE);
	}

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfigurationFactory#createToolConfiguration()
	 */
	public AbstractToolConfiguration createToolConfiguration() {
		SlicerConfiguration result = new SlicerConfiguration();
		result.initialize();
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#initialize()
	 */
	public void initialize() {
		setProperty(EQUIVALENCE_CLASS_BASED_INTERFERENCEDA, Boolean.TRUE);
		setProperty(EQUIVALENCE_CLASS_BASED_READYDA, Boolean.FALSE);
		setProperty(USE_READYDA, Boolean.FALSE);
		setProperty(USE_DIVERGENCEDA, Boolean.FALSE);
		setProperty(SLICE_TYPE, SlicingEngine.BACKWARD_SLICE);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isDivergenceDepAnalysisUsed() {
		return ((Boolean) properties.get(USE_DIVERGENCEDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isECBasedInterferenceDepAnalysisUsed() {
		return ((Boolean) properties.get(EQUIVALENCE_CLASS_BASED_INTERFERENCEDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isECBasedReadyDepAnalysisUsed() {
		return isReadyDepAnalysisUsed() && ((Boolean) properties.get(EQUIVALENCE_CLASS_BASED_READYDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isInterproceduralDivergenceDepAnalysisUsed() {
		return isDivergenceDepAnalysisUsed() && ((Boolean) properties.get(INTERPROCEDURAL_DIVERGENCEDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyDepAnalysisUsed() {
		return ((Boolean) properties.get(USE_READYDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule1Used() {
		return isReadyDepAnalysisUsed() && ((Boolean) properties.get(USE_RULE1_IN_READYDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule2Used() {
		return isReadyDepAnalysisUsed() && ((Boolean) properties.get(USE_RULE2_IN_READYDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule3Used() {
		return isReadyDepAnalysisUsed() && ((Boolean) properties.get(USE_RULE3_IN_READYDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule4Used() {
		return isReadyDepAnalysisUsed() && ((Boolean) properties.get(USE_RULE4_IN_READYDA)).booleanValue();
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param type Should not be used!
	 */
	protected void setSliceType(final String type) {
		if (type.equals(SlicingEngine.BACKWARD_SLICE) || type.equals(SlicingEngine.COMPLETE_SLICE)) {
			properties.put(SLICE_TYPE, type);
		}
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected String getSliceType() {
		return properties.get(SLICE_TYPE).toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @pre value != null
	 */
	protected boolean processProperty(final Object propertyID, final Object value) {
		boolean result = true;

		if (value instanceof Boolean) {
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
			} else if (propertyID.equals(USE_READYDA)) {
				if (val.booleanValue()) {
					dependencesToUse.add(DependencyAnalysis.READY_DA);
					id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new ReadyDAv1());
				} else {
					dependencesToUse.remove(DependencyAnalysis.READY_DA);
					id2dependencyAnalysis.remove(DependencyAnalysis.READY_DA);
				}
			} else if (propertyID.equals(USE_DIVERGENCEDA)) {
				if (val.booleanValue()) {
					dependencesToUse.add(DependencyAnalysis.DIVERGENCE_DA);
					id2dependencyAnalysis.put(DependencyAnalysis.DIVERGENCE_DA, new DivergenceDA());
				} else {
					dependencesToUse.remove(DependencyAnalysis.DIVERGENCE_DA);
					id2dependencyAnalysis.remove(DependencyAnalysis.DIVERGENCE_DA);
				}
			} else if (propertyID.equals(INTERPROCEDURAL_DIVERGENCEDA)) {
				Boolean bool = (Boolean) properties.get(USE_DIVERGENCEDA);

				if (bool != null && bool.booleanValue()) {
					DivergenceDA dda = (DivergenceDA) id2dependencyAnalysis.get(DependencyAnalysis.DIVERGENCE_DA);

					if (val.booleanValue()) {
						dda.setConsiderCallSites(true);
					} else {
						dda.setConsiderCallSites(false);
					}
				}
			} else if (propertyID.equals(SLICE_FOR_DEADLOCK)) {
				sliceForDeadlock = val.booleanValue();
			} else if (propertyID.equals(EXECUTABLE_SLICE)) {
				executableSlice = val.booleanValue();
			} else if (propertyID.equals(USE_RULE1_IN_READYDA)) {
				Boolean bool = (Boolean) properties.get(USE_READYDA);

				if (bool != null && bool.booleanValue()) {
					ReadyDAv1 rd = ((ReadyDAv1) id2dependencyAnalysis.get(DependencyAnalysis.READY_DA));
					int rules = rd.getRules();
					rd.setRules(rules | ReadyDAv1.RULE_1);
				}
			} else if (propertyID.equals(USE_RULE2_IN_READYDA)) {
				Boolean bool = (Boolean) properties.get(USE_READYDA);

				if (bool != null && bool.booleanValue()) {
					ReadyDAv1 rd = ((ReadyDAv1) id2dependencyAnalysis.get(DependencyAnalysis.READY_DA));
					int rules = rd.getRules();
					rd.setRules(rules | ReadyDAv1.RULE_2);
				}
			} else if (propertyID.equals(USE_RULE3_IN_READYDA)) {
				Boolean bool = (Boolean) properties.get(USE_READYDA);

				if (bool != null && bool.booleanValue()) {
					ReadyDAv1 rd = ((ReadyDAv1) id2dependencyAnalysis.get(DependencyAnalysis.READY_DA));
					int rules = rd.getRules();
					rd.setRules(rules | ReadyDAv1.RULE_3);
				}
			} else if (propertyID.equals(USE_RULE4_IN_READYDA)) {
				Boolean bool = (Boolean) properties.get(USE_READYDA);

				if (bool != null && bool.booleanValue()) {
					ReadyDAv1 rd = ((ReadyDAv1) id2dependencyAnalysis.get(DependencyAnalysis.READY_DA));
					int rules = rd.getRules();
					rd.setRules(rules | ReadyDAv1.RULE_4);
				}
			}
		} else {
			if (propertyID.equals(SLICE_TYPE)) {
				if (!value.equals(SlicingEngine.BACKWARD_SLICE) && !value.equals(SlicingEngine.COMPLETE_SLICE)) {
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useDivergenceDepAnalysis(final boolean use) {
		processPropertyHelper(USE_DIVERGENCEDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useECBasedInterferenceDepAnalysis(final boolean use) {
		processPropertyHelper(EQUIVALENCE_CLASS_BASED_INTERFERENCEDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useECBasedReadyDepAnalysis(final boolean use) {
		processPropertyHelper(EQUIVALENCE_CLASS_BASED_READYDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useInterproceduralDivergenceDepAnalysis(final boolean use) {
		processPropertyHelper(INTERPROCEDURAL_DIVERGENCEDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useReadyDepAnalysis(final boolean use) {
		processPropertyHelper(USE_READYDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useReadyRule1(final boolean use) {
		processPropertyHelper(USE_RULE1_IN_READYDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useReadyRule2(final boolean use) {
		processPropertyHelper(USE_RULE2_IN_READYDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useReadyRule3(final boolean use) {
		processPropertyHelper(USE_RULE3_IN_READYDA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void useReadyRule4(final boolean use) {
		processPropertyHelper(USE_RULE4_IN_READYDA, use);
	}

	/**
	 * Provides the dependency analysis corresponding to the given id.
	 *
	 * @param id of the requested dependence analysis.
	 *
	 * @return the dependency analysis identified by <code>id</code>.  <code>null</code> if there is no corresponding
	 * 		   analysis.
	 */
	DependencyAnalysis getDependenceAnalysis(final Object id) {
		return (DependencyAnalysis) id2dependencyAnalysis.get(id);
	}

	/**
	 * Provides the names of the dependences to use for slicing.
	 *
	 * @return a collection of dependence analyses.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	Collection getNamesOfDAsToUse() {
		return Collections.unmodifiableCollection(dependencesToUse);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param propertyID DOCUMENT ME!
	 * @param use DOCUMENT ME!
	 */
	private void processPropertyHelper(final Object propertyID, final boolean use) {
		Boolean value = Boolean.valueOf(use);
		properties.put(propertyID, value);
		processProperty(propertyID, value);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.11  2003/10/20 13:55:25  venku
   - Added a factory to create new configurations.
   - Simplified AbstractToolConfigurator methods.
   - The driver manages the shell.
   - Got all the gui parts running EXCEPT for changing
     the name of the configuration.
   Revision 1.10  2003/10/19 20:04:05  venku
   - class needs to be public for the purpose of
     marshalling and unmarshalling.  FIXED.
   Revision 1.9  2003/10/13 01:01:45  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.8  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.7  2003/09/27 01:09:35  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.6  2003/09/26 15:30:39  venku
   - removed PropertyIdentifier class.
   - ripple effect of the above change.
   - formatting
   Revision 1.5  2003/09/26 07:33:18  venku
   - checkpoint commit.
   Revision 1.4  2003/09/26 05:55:41  venku
   - *** empty log message ***
   Revision 1.1  2003/09/24 07:32:23  venku
   - Created an implementation of indus tool api specific to Slicer.
     The GUI needs to be setup and bandera adapter needs to be fixed.
 */
