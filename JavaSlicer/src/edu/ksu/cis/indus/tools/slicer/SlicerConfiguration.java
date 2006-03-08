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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.soot.ApplicationClassesOnlyPredicate;
import edu.ksu.cis.indus.slicer.SliceType;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.ThreadEscapeInfoBasedCallingContextRetrieverV2;
import edu.ksu.cis.indus.staticanalyses.dependency.DivergenceDA;
import edu.ksu.cis.indus.staticanalyses.dependency.ExitControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.InterProceduralDivergenceDA;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationInsensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationSensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.SynchronizationDA;
import edu.ksu.cis.indus.tools.AbstractToolConfiguration;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.IToolConfigurationFactory;
import edu.ksu.cis.indus.tools.slicer.contextualizers.DeadlockPreservingCriteriaCallStackContextualizer;
import edu.ksu.cis.indus.tools.slicer.contextualizers.ISliceCriteriaContextualizer;
import edu.ksu.cis.indus.tools.slicer.criteria.generators.DeadlockPreservingCriteriaGenerator;
import edu.ksu.cis.indus.tools.slicer.criteria.generators.ISliceCriteriaGenerator;
import edu.ksu.cis.indus.tools.slicer.criteria.generators.StmtTypeBasedSliceCriteriaGenerator;
import edu.ksu.cis.indus.tools.slicer.criteria.predicates.AssertionSliceCriteriaPredicate;
import edu.ksu.cis.indus.tools.slicer.criteria.predicates.EscapingSliceCriteriaPredicate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.ThrowStmt;

/**
 * This represents a configurationCollection of the slicer. The slicer tool should be configured via an object of this class
 * obtained from the slicer tool. The type of the propoerty values are documented with the property identifiers.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerConfiguration
		extends AbstractToolConfiguration
		implements Cloneable, IToolConfigurationFactory {

	/**
	 * This indicates all-synchronization-constructs-should-be-considered deadlock criteria selection strategy.
	 */
	static final Comparable<String> ALL_SYNC_CONSTRUCTS = "ALL_SYNC_CONSTRUCTS";

	/**
	 * This identifies the property that governs which assertions will be selected.
	 */
	static final Comparable<String> ASSERTIONS_IN_APPLICATION_CLASSES_ONLY = "consider assertions in application classes only";

	/**
	 * This identifies the property that determines if call site sensitive ready dependence is used.
	 */
	static final Comparable<String> CALL_SITE_SENSITIVE_READY_DA = "call site sensitive ready dependence";

	/**
	 * This identifies the property that control the limit on the length of the calling context.
	 */
	static final Comparable<String> CALLING_CONTEXT_LENGTH = "calling context length";

	/**
	 * This identifies the property that determines if exceptional exit sensitive control dependence (based on implicit
	 * unchecked exceptions) should be used to create the slice.
	 */
	static final Comparable<String> COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD = "common unchecked exceptional exit sensitive control dependence";

	/**
	 * This indicates all-synchronization-constructs-with-escaping-monitors-should-be-considered-in-a-context-sensitive-manner
	 * deadlock criteria selection strategy.
	 */
	static final Comparable<String> CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS = "CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS";

	/**
	 * This identifies the property that determines the strategy used to select criteria to preserve deadlock.
	 */
	static final Comparable<String> DEADLOCK_CRITERIA_SELECTION_STRATEGY = "deadlock criteria selection strategy";

	/**
	 * This is the default limit on the length of the calling contexts.
	 */
	static final Integer DEFAULT_CALLING_CONTEXT_LIMIT = new Integer(10);

	/**
	 * This identifies the property that indicates if equivalence class based interference dependence should be used instead
	 * of naive type-based interference dependence. This is tied to values of <i>slicer:natureOfInterThreadAnalysis</i>
	 * attribute in <code>slicerConfig_JiBXBinding.xml</code>.
	 */
	static final Comparable<String> EQUIVALENCE_CLASS_BASED_INFO = "EQUIVALENCE_CLASS_BASED_INFO";

	/**
	 * This indicates all-synchronization-constructs-with-escaping-monitors-should-be-considered deadlock criteria selection
	 * strategy.
	 */
	static final Comparable<String> ESCAPING_SYNC_CONSTRUCTS = "ESCAPING_SYNC_CONSTRUCTS";

	/**
	 * This identifies the option to create executable slice.
	 */
	static final Comparable<String> EXECUTABLE_SLICE = "executable slice";

	/**
	 * This identifies the property that determines if explicit exceptional exit sensitive control dependence should be used
	 * to create the slice.
	 */
	static final Comparable<String> EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE = "explicit exceptional exit sensitive control dependence";

	/**
	 * This indicates pure inter-procedural setting.
	 */
	static final Comparable<String> INTER_PROCEDURAL_ONLY = "INTER_PROCEDURAL_ONLY";

	/**
	 * This indicates intra- and inter-procedural setting.
	 */
	static final Comparable<String> INTRA_AND_INTER_PROCEDURAL = "INTRA_AND_INTER_PROCEDURAL";

	/**
	 * This indicates pure intra-procedural setting.
	 */
	static final Comparable<String> INTRA_PROCEDURAL_ONLY = "INTRA_PROCEDURAL_ONLY";

	/**
	 * This identifies the property that indicates the nature of divergence dependence, i.e., intra, inter, and intra-inter.
	 */
	static final Comparable<String> NATURE_OF_DIVERGENCE_DA = "nature of divergence dependence";

	/**
	 * This identifies the property that indicates the nature of interference dependence, i.e., type based, etc.
	 */
	static final Comparable<String> NATURE_OF_INTERFERENCE_DA = "nature of interference dependence";

	/**
	 * This identifies the property that indicates the nature of ready dependence, i.e., type based, etc.
	 */
	static final Comparable<String> NATURE_OF_READY_DA = "nature of ready dependence";

	/**
	 * This identifies the property that determines if non-termination sensitive control dependence should be used to create
	 * the slice.
	 */
	static final Comparable<String> NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE = "Non termination sensitive control dependence";

	/**
	 * This identifies the property that determines if property aware slicing is required.
	 */
	static final Comparable<String> PROPERTY_AWARE = "property aware slicing";

	/**
	 * This identifies the property that indicates if slice criteria should be automatically picked to preserve the
	 * deadlocking property of the program.
	 */
	static final Comparable<String> SLICE_FOR_DEADLOCK = "slice for deadlock";

	/**
	 * This identifies the property that indicates if slice criteria should be automatically picked to preserve assertions in
	 * the program.
	 */
	static final Comparable<String> SLICE_TO_PRESERVE_ASSERTIONS = "slice to preserve assertions";

	/**
	 * This identifies the property that indicates the slice type, i.e., forward or complete slice.
	 */
	static final Comparable<String> SLICE_TYPE = "slice type";

	/**
	 * This identifies the property that indicates if symbol and equivalence class based interference dependence should be
	 * used instead of naive type-based interference dependence. This is tied to values of
	 * <i>slicer:natureOfInterThreadAnalysis</i> attribute in <code>slicerConfig_JiBXBinding.xml</code>.
	 */
	static final Comparable<String> SYMBOL_AND_EQUIVCLS_BASED_INFO = "SYMBOL_AND_EQUIVCLS_BASED_INFO";

	/**
	 * This identifies the property that governs which synchronization constructs will be selected to preserve deadlocking
	 * property.
	 */
	static final Comparable<String> SYNCS_IN_APPLICATION_CLASSES_ONLY = "consider synchronization constructs in application classes only";

	/**
	 * This indicates type based information. This is tied to values of <i>slicer:natureOfInterThreadAnalysis</i> attribute
	 * in <code>slicerConfig_JiBXBinding.xml</code>.
	 */
	static final Comparable<String> TYPE_BASED_INFO = "TYPE_BASED_INFO";

	/**
	 * This identifies the property that indicates if divergence dependence should be considered for slicing.
	 */
	static final Comparable<String> USE_DIVERGENCEDA = "use divergence dependence";

	/**
	 * This identifies the property that indicates if interference dependence should be considered for slicing.
	 */
	static final Comparable<String> USE_INTERFERENCEDA = "use interference dependence";

	/**
	 * This identifies the property that indicates if object flow information should be used in the context of interference
	 * dependence.
	 */
	static final Comparable<String> USE_OFA_FOR_INTERFERENCE_DA = "use ofa for interference";

	/**
	 * This identifies the property that indicates if object flow information should be used in the context of ready
	 * dependence.
	 */
	static final Comparable<String> USE_OFA_FOR_READY_DA = "use ofa for ready";

	/**
	 * This identifies the property that indicates if ready dependence should be considered for slicing.
	 */
	static final Comparable<String> USE_READYDA = "use ready dependence";

	/**
	 * This identifies the property that indicates if rule1 of ready dependence be used. Rule 1: m is dependent on n if m and
	 * n occur in the same thread and n is an enter monitor statement.
	 */
	static final Comparable<String> USE_RULE1_IN_READYDA = "use rule1 in ready dependence";

	/**
	 * This identifies the property that indicates if rule2 of ready dependence be used. Rule 2: m is dependent on n if m and
	 * n occur in different threads and m and n are is exit monitor and enter monitor statements, respectively.
	 */
	static final Comparable<String> USE_RULE2_IN_READYDA = "use rule2 in ready dependence";

	/**
	 * This identifies the property that indicates if rule3 of ready dependence be used. Rule 3: m is dependent on n if m and
	 * n occur in the same thread and m has a call to java.lang.Object.wait.
	 */
	static final Comparable<String> USE_RULE3_IN_READYDA = "use rule3 in ready dependence";

	/**
	 * This identifies the property that indicates if rule4 of ready dependence be used. Rule 4: m is dependent on n if m and
	 * n occur in the different thread and m and n have calls to java.lang.Object.wait(XXX) and java.lang.Object.notifyXXX(),
	 * respectively..
	 */
	static final Comparable<String> USE_RULE4_IN_READYDA = "use rule4 in ready dependence";

	/**
	 * This identifies the property that indicates if safe lock analysis should be used in the context of ready dependence.
	 */
	static final Comparable<String> USE_SLA_FOR_READY_DA = "use sla for ready";

	/**
	 * This identifies the property that indicates if ready dependence should be considered for slicing.
	 */
	static final Comparable<String> USE_SYNCHRONIZATIONDA = "use synchronization dependences";

	/**
	 * This is the id for the assertion preserving criteria generator.
	 */
	private static final Comparable<String> ASSERTION_PRESERVING_CRITERIA_GENERATOR_ID = "assertion preserving criteria generator id";

	/**
	 * This is the id for the deadlock preserving criteria generator.
	 */
	private static final Comparable<String> DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID = "deadlock preserving criteria generator id";

	/**
	 * This is the factory object to create configurations.
	 */
	private static final IToolConfigurationFactory FACTORY_SINGLETON = new SlicerConfiguration();

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SlicerConfiguration.class);

	/**
	 * The collection of ids of the dependences to be considered for slicing.
	 */
	private final Collection<IDependencyAnalysis.DependenceSort> dependencesToUse;

	/**
	 * This maps identifiers to criteria generators.
	 */
	private final Map<Object, ISliceCriteriaGenerator<?, ?>> id2critGenerators;

	/**
	 * This maps IDs to dependency analyses.
	 */
	private final Map<IDependencyAnalysis.DependenceSort, Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>> id2dependencyAnalyses;

	/**
	 * Creates a new SlicerConfiguration object.
	 */
	protected SlicerConfiguration() {
		id2critGenerators = new HashMap<Object, ISliceCriteriaGenerator<?, ?>>();
		id2dependencyAnalyses = new HashMap<IDependencyAnalysis.DependenceSort, Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>>();
		dependencesToUse = new HashSet<IDependencyAnalysis.DependenceSort>();
		propertyIds.add(NATURE_OF_INTERFERENCE_DA);
		propertyIds.add(USE_OFA_FOR_INTERFERENCE_DA);
		propertyIds.add(USE_INTERFERENCEDA);
		propertyIds.add(USE_READYDA);
		propertyIds.add(NATURE_OF_READY_DA);
		propertyIds.add(USE_OFA_FOR_READY_DA);
		propertyIds.add(USE_RULE1_IN_READYDA);
		propertyIds.add(USE_RULE2_IN_READYDA);
		propertyIds.add(USE_RULE3_IN_READYDA);
		propertyIds.add(USE_RULE4_IN_READYDA);
		propertyIds.add(USE_SLA_FOR_READY_DA);
		propertyIds.add(SLICE_FOR_DEADLOCK);
		propertyIds.add(SLICE_TO_PRESERVE_ASSERTIONS);
		propertyIds.add(DEADLOCK_CRITERIA_SELECTION_STRATEGY);
		propertyIds.add(SLICE_TYPE);
		propertyIds.add(EXECUTABLE_SLICE);
		propertyIds.add(PROPERTY_AWARE);
		propertyIds.add(NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE);
		propertyIds.add(EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE);
		propertyIds.add(COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD);
		propertyIds.add(USE_DIVERGENCEDA);
		propertyIds.add(NATURE_OF_DIVERGENCE_DA);
		propertyIds.add(INTRA_PROCEDURAL_ONLY);
		propertyIds.add(INTRA_AND_INTER_PROCEDURAL);
		propertyIds.add(INTER_PROCEDURAL_ONLY);
		propertyIds.add(USE_SYNCHRONIZATIONDA);
		propertyIds.add(CALL_SITE_SENSITIVE_READY_DA);
		propertyIds.add(ASSERTIONS_IN_APPLICATION_CLASSES_ONLY);
		propertyIds.add(SYNCS_IN_APPLICATION_CLASSES_ONLY);
		propertyIds.add(CALLING_CONTEXT_LENGTH);
	}

	/**
	 * Retrieves the configuration factory object.
	 * 
	 * @return the configuration factory.
	 * @post result != null
	 */
	static IToolConfigurationFactory getFactory() {
		return FACTORY_SINGLETON;
	}

	/**
	 * IFactory method to create a configuration. This is used by the factory and in java-2-xml binding. It is adviced to use
	 * the factory object rather than using this method.
	 * 
	 * @return a new instance of a configuration.
	 * @post result != null
	 */
	static SlicerConfiguration makeToolConfiguration() {
		final SlicerConfiguration _result = new SlicerConfiguration();
		_result.setConfigName("tool_configuration_" + System.currentTimeMillis());
		_result.initialize();
		return _result;
	}

	/**
	 * Checks if assertions only in application classes will be considered.
	 * 
	 * @return <code>true</code> if assertions only in application classes will be considered.; <code>false</code>,
	 *         otherwise.
	 */
	public boolean areAssertionsOnlyInAppClassesConsidered() {
		return getBooleanProperty(ASSERTIONS_IN_APPLICATION_CLASSES_ONLY);
	}

	/**
	 * Checks if common unchecked exception based exit sensitive control dependence should be considered.
	 * 
	 * @return <code>true</code> if common unchecked exception based exit control dependence should be considered;
	 *         <code>false</code>, otherwise.
	 */
	public boolean areCommonUncheckedExceptionsConsidered() {
		return getBooleanProperty(COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD);
	}

	/**
	 * Checks if synchronization constructs only in application classes will be considered.
	 * 
	 * @return <code>true</code> if synchronization constructs only in application classes will be considered.;
	 *         <code>false</code>, otherwise.
	 */
	public boolean areSynchronizationsOnlyInAppClassesConsidered() {
		return getBooleanProperty(SYNCS_IN_APPLICATION_CLASSES_ONLY);
	}

	/**
	 * Sets the propery the governs if only assertions in application classes are considered.
	 * 
	 * @param value <code>true</code> if only assertions in application classes should be considered; <code>false</code>,
	 *            otherwise.
	 */
	public void considerAssertionsInAppClassesOnly(final boolean value) {
		setProperty(ASSERTIONS_IN_APPLICATION_CLASSES_ONLY, Boolean.valueOf(value));
	}

	/**
	 * Sets if implicit common unchecked exception based exit sensitive control dependence should be considered.
	 * 
	 * @param value <code>true</code> indicates implicit common unchecked exception based exit sensitive control dependence
	 *            should be considered; <code>false</code>, otherwise.
	 */
	public void considerCommonUncheckedExceptions(final boolean value) {
		setProperty(COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD, Boolean.valueOf(value));
	}

	/**
	 * Sets the propery the governs if only synchronization constructs in application classes are considered.
	 * 
	 * @param value <code>true</code> if only synchronization constructs in application classes should be considered;
	 *            <code>false</code>, otherwise.
	 */
	public void considerSynchronizationsInAppClassesOnly(final boolean value) {
		setProperty(SYNCS_IN_APPLICATION_CLASSES_ONLY, Boolean.valueOf(value));
	}

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfigurationFactory#createToolConfiguration()
	 */
	public IToolConfiguration createToolConfiguration() {
		return makeToolConfiguration();
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override public boolean equals(final Object object) {
		boolean _result = false;

		if (object instanceof SlicerConfiguration) {
			final SlicerConfiguration _config = (SlicerConfiguration) object;
			_result = new EqualsBuilder().appendSuper(super.equals(object)).append(this.propertyIds, _config.propertyIds)
					.append(this.id2dependencyAnalyses, _config.id2dependencyAnalyses).append(this.dependencesToUse,
							_config.dependencesToUse).append(this.properties, _config.properties).isEquals();
		}
		return _result;
	}

	/**
	 * Retrieves the limit on the length of the calling context.
	 * 
	 * @return the limit.
	 */
	public int getCallingContextLimit() {
		return ((Integer) getProperty(CALLING_CONTEXT_LENGTH)).intValue();
	}

	/**
	 * Retrieves the strategy used to select deadlock perserving criteria.
	 * 
	 * @return the selection strategy used.
	 */
	public String getDeadlockCriteriaSelectionStrategy() {
		String _result = (String) getProperty(DEADLOCK_CRITERIA_SELECTION_STRATEGY);

		if (_result == null) {
			_result = CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS.toString();
		}
		return _result;
	}

	/**
	 * Provides the dependency analysis corresponding to the given id.
	 * 
	 * @param id of the requested dependence analyses.
	 * @return the dependency analyses identified by <code>id</code>.
	 * @post result != null and result.oclIsKindOf(Collection(IDependencyAnalysis))
	 */
	public Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> getDependenceAnalyses(final IDependencyAnalysis.DependenceSort id) {
		Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _result = id2dependencyAnalyses.get(id);

		if (_result == null) {
			_result = Collections.emptyList();
		}
		return _result;
	}

	/**
	 * Retrieves the executability of the generated slice.
	 * 
	 * @return <code>true</code> indicates executable slice should be generated; <code>false</code>, otherwise.
	 */
	public boolean getExecutableSlice() {
		return getBooleanProperty(EXECUTABLE_SLICE);
	}

	/**
	 * Retrieves the nature of divergence dependence analysis specified by this configuration.
	 * 
	 * @return the nature of divergence dependence analysis.
	 * @post result != null
	 */
	public String getNatureOfDivergenceDepAnalysis() {
		String _result = (String) getProperty(NATURE_OF_DIVERGENCE_DA);

		if (_result == null) {
			_result = INTRA_PROCEDURAL_ONLY.toString();
		}
		return _result;
	}

	/**
	 * Retrieves the nature of interference dependence analysis specified by this configuration.
	 * 
	 * @return <code>true</code> if the use of interference dependence analysis enabled; <code>false</code>, otherwise.
	 * @post result != null
	 */
	public String getNatureOfInterferenceDepAnalysis() {
		String _result = (String) getProperty(NATURE_OF_INTERFERENCE_DA);

		if (_result == null) {
			_result = SYMBOL_AND_EQUIVCLS_BASED_INFO.toString();
		}
		return _result;
	}

	/**
	 * Retrieves the nature of ready dependence analysis specified by this configuration.
	 * 
	 * @return the nature of ready dependence analysis.
	 * @post result != null
	 */
	public String getNatureOfReadyDepAnalysis() {
		String _result = (String) getProperty(NATURE_OF_READY_DA);

		if (_result == null) {
			_result = SYMBOL_AND_EQUIVCLS_BASED_INFO.toString();
		}
		return _result;
	}

	/**
	 * Checks if property aware slices will be generated.
	 * 
	 * @return <code>true</code> if the property aware slices will be generated; <code>false</code>, otherwise.
	 */
	public boolean getPropertyAware() {
		return getBooleanProperty(PROPERTY_AWARE);
	}

	/**
	 * Checks if the slice was done to preserve deadlocking properties.
	 * 
	 * @return <code>true</code> indicates slice should preserve deadlocking properties; <code>false</code>, otherwise.
	 */
	public boolean getSliceForDeadlock() {
		return getBooleanProperty(SLICE_FOR_DEADLOCK);
	}

	/**
	 * Checks if the slice was done to preserve assertions.
	 * 
	 * @return <code>true</code> indicates slice should preserve assertions; <code>false</code>, otherwise.
	 */
	public boolean getSliceToPreserveAssertions() {
		return getBooleanProperty(SLICE_TO_PRESERVE_ASSERTIONS);
	}

	/**
	 * Retrieves the type of slice that will be generated.
	 * 
	 * @return the type of slice.
	 * @post result != null
	 */
	public SliceType getSliceType() {
		return (SliceType) getProperty(SLICE_TYPE);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override public int hashCode() {
		return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(propertyIds).append(id2dependencyAnalyses)
				.append(dependencesToUse).append(properties).toHashCode();
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#initialize()
	 */
	public void initialize() {
		// set default values for certain properties. The ordering is important.
		setProperty(USE_DIVERGENCEDA, Boolean.FALSE);
		setProperty(USE_READYDA, Boolean.FALSE);
		setProperty(USE_INTERFERENCEDA, Boolean.FALSE);
		setProperty(USE_SYNCHRONIZATIONDA, Boolean.FALSE);
		setProperty(NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE, Boolean.FALSE);
		setProperty(EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE, Boolean.FALSE);
		setProperty(COMMON_UNCHECKED_EXCEPTIONAL_EXIT_SENSITIVE_CD, Boolean.FALSE);
		setProperty(SLICE_TYPE, SliceType.BACKWARD_SLICE);
		setProperty(EXECUTABLE_SLICE, Boolean.FALSE);
		setProperty(SLICE_FOR_DEADLOCK, Boolean.FALSE);
		setProperty(SLICE_TO_PRESERVE_ASSERTIONS, Boolean.FALSE);
		setProperty(USE_OFA_FOR_INTERFERENCE_DA, Boolean.FALSE);
		setProperty(USE_OFA_FOR_READY_DA, Boolean.FALSE);
		setProperty(USE_RULE1_IN_READYDA, Boolean.FALSE);
		setProperty(USE_RULE2_IN_READYDA, Boolean.FALSE);
		setProperty(USE_RULE3_IN_READYDA, Boolean.FALSE);
		setProperty(USE_RULE4_IN_READYDA, Boolean.FALSE);
		setProperty(CALL_SITE_SENSITIVE_READY_DA, Boolean.FALSE);
		setProperty(USE_SLA_FOR_READY_DA, Boolean.FALSE);
		setProperty(PROPERTY_AWARE, Boolean.FALSE);
		setProperty(ASSERTIONS_IN_APPLICATION_CLASSES_ONLY, Boolean.FALSE);
		setProperty(SYNCS_IN_APPLICATION_CLASSES_ONLY, Boolean.FALSE);
		setProperty(CALLING_CONTEXT_LENGTH, DEFAULT_CALLING_CONTEXT_LIMIT);

		dependencesToUse.add(IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA);
		dependencesToUse.add(IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA);
		dependencesToUse.add(IDependencyAnalysis.DependenceSort.CONTROL_DA);
	}

	/**
	 * Checks if call-site sensitive ready dependence is used.
	 * 
	 * @return <code>true</code> if call-site based ready dependence is used; <code>false</code>, otherwise.
	 */
	public boolean isCallSiteSensitiveReadyUsed() {
		return getBooleanProperty(CALL_SITE_SENSITIVE_READY_DA);
	}

	/**
	 * Checks if divergence dependence analysis is enabled in this configuration.
	 * 
	 * @return <code>true</code> if the use of divergence dependence analysis is enabled; <code>false</code>, otherwise.
	 */
	public boolean isDivergenceDepAnalysisUsed() {
		return getBooleanProperty(USE_DIVERGENCEDA);
	}

	/**
	 * Checks if explicit exceptional exit sensitive control dependence should be used.
	 * 
	 * @return <code>true</code> if explicit exceptional exit control dependence should be used; <code>false</code>,
	 *         otherwise.
	 */
	public boolean isExplicitExceptionalExitSensitiveControlDependenceUsed() {
		return getBooleanProperty(EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE);
	}

	/**
	 * Checks if interference dependence analysis is enabled in this configuration.
	 * 
	 * @return <code>true</code> if the use of interference dependence analysis is enabled; <code>false</code>,
	 *         otherwise.
	 */
	public boolean isInterferenceDepAnalysisUsed() {
		return getBooleanProperty(USE_INTERFERENCEDA);
	}

	/**
	 * Checks if non-termination sensitive control dependence should be used.
	 * 
	 * @return <code>true</code> if non-termination sensitive control dependence should be used; <code>false</code>,
	 *         otherwise.
	 */
	public boolean isNonTerminationSensitiveControlDependenceUsed() {
		return getBooleanProperty(NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE);
	}

	/**
	 * Checks if OFA is being used for interference dependence calculation.
	 * 
	 * @return <code>true</code> if OFA is used for interference dependence; <code>false</code>, otherwise.
	 */
	public boolean isOFAUsedForInterference() {
		return getBooleanProperty(USE_OFA_FOR_INTERFERENCE_DA);
	}

	/**
	 * Checks if OFA is being used for ready dependence calculation.
	 * 
	 * @return <code>true</code> if OFA is used for ready dependence; <code>false</code>, otherwise.
	 */
	public boolean isOFAUsedForReady() {
		return getBooleanProperty(USE_OFA_FOR_READY_DA);
	}

	/**
	 * Checks if ready dependence analysis is enabled in this configuration.
	 * 
	 * @return <code>true</code> if the use of ready dependence analysis is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyDepAnalysisUsed() {
		return getBooleanProperty(USE_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 1 is enabled. Rule 1 being the intraprocedural ready dependence induced by
	 * enter monitor statements.
	 * 
	 * @return <code>true</code> if ready dependence analysis rule 1 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule1Used() {
		return getBooleanProperty(USE_RULE1_IN_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 2 is enabled. Rule 2 being the interprocedural ready dependence induced by
	 * enter/exit monitor statements.
	 * 
	 * @return <code>true</code> if ready dependence analysis rule 2 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule2Used() {
		return getBooleanProperty(USE_RULE2_IN_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 3 is enabled. Rule 3 being the intraprocedural ready dependence induced by
	 * wait statements.
	 * 
	 * @return <code>true</code> if ready dependence analysis rule 3 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule3Used() {
		return getBooleanProperty(USE_RULE3_IN_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 4 is enabled. Rule 4 being the interprocedural ready dependence induced by
	 * wait/notify statements.
	 * 
	 * @return <code>true</code> if ready dependence analysis rule 4 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule4Used() {
		return getBooleanProperty(USE_RULE4_IN_READYDA);
	}

	/**
	 * Checks if Safe Lock Analysis is being used for ready dependence calculation.
	 * 
	 * @return <code>true</code> if SLA is used for ready dependence; <code>false</code>, otherwise.
	 */
	public boolean isSafeLockAnalysisUsedForReady() {
		return getBooleanProperty(USE_SLA_FOR_READY_DA);
	}

	/**
	 * Checks if synchronization dependence analysis is enabled in this configuration.
	 * 
	 * @return <code>true</code> if the use of synchronization dependence analysis is enabled; <code>false</code>,
	 *         otherwise.
	 */
	public boolean isSynchronizationDepAnalysisUsed() {
		return getBooleanProperty(USE_SYNCHRONIZATIONDA);
	}

	/**
	 * sets the limit of calling context length.
	 * 
	 * @param limit obviously. If this is <= 0 then it will be set to 10.
	 */
	public void setCallingContextLimit(final int limit) {
		final Integer _i;
		if (limit < 0) {
			_i = DEFAULT_CALLING_CONTEXT_LIMIT;
		} else {
			_i = new Integer(limit);
		}
		setProperty(CALLING_CONTEXT_LENGTH, _i);
	}

	/**
	 * Sets the strategy to be used to select deadlock preserving criteria.
	 * 
	 * @param dc specifies the strategy. It has to be one of <code>ALL_SYNC_CONSTRUCTS</code> or
	 *            <code>ESCAPING_SYNC_CONSTRUCTS</code>.
	 */
	public void setDeadlockCriteriaSelectionStrategy(final String dc) {
		super.setProperty(DEADLOCK_CRITERIA_SELECTION_STRATEGY, dc);
	}

	/**
	 * Sets the executability of the generated slice.
	 * 
	 * @param value <code>true</code> indicates executable slice should be generated; <code>false</code>, otherwise.
	 * @throws IllegalStateException when executability is set on forward slices.
	 */
	public void setExecutableSlice(final boolean value) {
		if (!getSliceType().equals(SliceType.FORWARD_SLICE)) {
			setProperty(EXECUTABLE_SLICE, Boolean.valueOf(value));
		} else if (value) {
			throw new IllegalStateException("Forward Executable Slices are not supported.");
		}
	}

	/**
	 * Sets the nature of divergence dependence analysis to be used.
	 * 
	 * @param use specifies the nature of analysis. It has to be one of values defined by
	 *            <code>INTRA_PROCEDURAL_DIVERGENCE</code>, <code>INTER_PROCEDURAL_DIVERGENCE</code>, and
	 *            <code>INTRA_AND_INTER_PROCEDURAL_DIVERGENCE</code>.
	 * @pre use != null
	 */
	public void setNatureOfDivergenceDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_DIVERGENCE_DA, use);
	}

	/**
	 * Sets the nature of interference dependence analysis to be used.
	 * 
	 * @param use specifies the nature of analysis. It has to be one of values defined by
	 *            <code>EQUIVALENCE_CLASS_BASED_INFO</code>, <code>SYMBOL_AND_EQUIVCLS_BASED_INFO</code>, and
	 *            <code>TYPE_BASED_INFO</code>.
	 * @pre use != null
	 */
	public void setNatureOfInterferenceDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_INTERFERENCE_DA, use);
	}

	/**
	 * Sets the nature of ready dependence analysis to be used.
	 * 
	 * @param use specifies the nature of analysis. It has to be one of values defined by
	 *            <code>EQUIVALENCE_CLASS_BASED_INFO</code>, <code>SYMBOL_AND_EQUIVCLS_BASED_INFO</code>, and
	 *            <code>TYPE_BASED_INFO</code>.
	 * @pre use != null
	 */
	public void setNatureOfReadyDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_READY_DA, use);
	}

	/**
	 * Sets the property that governs if property aware slices will be generated.
	 * 
	 * @param value <code>true</code> indicates property aware slices should be generated; <code>false</code>, otherwise.
	 */
	public void setPropertyAware(final boolean value) {
		setProperty(PROPERTY_AWARE, Boolean.valueOf(value));
	}

	/**
	 * Sets the preservation of deadlocking in the slice.
	 * 
	 * @param value <code>true</code> indicates slice should preserve deadlocking properties; <code>false</code>,
	 *            otherwise.
	 */
	public void setSliceForDeadlock(final boolean value) {
		setProperty(SLICE_FOR_DEADLOCK, Boolean.valueOf(value));
	}

	/**
	 * Sets the preservation of assertions in the slice.
	 * 
	 * @param value <code>true</code> indicates slice should preserve assertions; <code>false</code>, otherwise.
	 */
	public void setSliceToPreserveAssertions(final boolean value) {
		setProperty(SLICE_TO_PRESERVE_ASSERTIONS, Boolean.valueOf(value));
	}

	/**
	 * Sets the type of slice to be generated.
	 * 
	 * @param type specifies the type of slice. It has to be one of values defined by <code>SlicingEngine.SliceType</code>.
	 * @pre use != null
	 */
	public void setSliceType(final SliceType type) {
		setProperty(SLICE_TYPE, type);
	}

	/**
	 * Sets if call-site sensitive ready dependence is used.
	 * 
	 * @param use <code>true</code> if call-site based ready dependence should be used; <code>false</code>, otherwise.
	 */
	public void useCallSiteSensitiveReady(final boolean use) {
		setProperty(CALL_SITE_SENSITIVE_READY_DA, Boolean.valueOf(use));
	}

	/**
	 * Configures if divergence dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useDivergenceDepAnalysis(final boolean use) {
		setProperty(USE_DIVERGENCEDA, Boolean.valueOf(use));
	}

	/**
	 * Sets if explicit exceptional exit sensitive control dependence should be used.
	 * 
	 * @param value <code>true</code> indicates explicit exceptional exit sensitive control dependence should be used;
	 *            <code>false</code>, otherwise.
	 */
	public void useExplicitExceptionalExitSensitiveControlDependence(final boolean value) {
		setProperty(EXPLICIT_EXCEPTIONAL_EXIT_SENSITIVE_CONTROL_DEPENDENCE, Boolean.valueOf(value));
	}

	/**
	 * Configures if interference dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useInterferenceDepAnalysis(final boolean use) {
		setProperty(USE_INTERFERENCEDA, Boolean.valueOf(use));
	}

	/**
	 * Sets if non-termination sensitive control dependence should be used.
	 * 
	 * @param value <code>true</code> indicates non-termination sensitive control dependence should be used;
	 *            <code>false</code>, otherwise.
	 */
	public void useNonTerminationSensitiveControlDependence(final boolean value) {
		setProperty(NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE, Boolean.valueOf(value));
	}

	/**
	 * Sets if OFA should be used during interference dependence calculation.
	 * 
	 * @param use <code>true</code> if OFA should be used; <code>false</code>, otherwise.
	 */
	public void useOFAForInterference(final boolean use) {
		setProperty(USE_OFA_FOR_INTERFERENCE_DA, Boolean.valueOf(use));
	}

	/**
	 * Sets if OFA should be used during ready dependence calculation.
	 * 
	 * @param use <code>true</code> if OFA should be used; <code>false</code>, otherwise.
	 */
	public void useOFAForReady(final boolean use) {
		setProperty(USE_OFA_FOR_READY_DA, Boolean.valueOf(use));
	}

	/**
	 * Configures if ready dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyDepAnalysis(final boolean use) {
		setProperty(USE_READYDA, Boolean.valueOf(use));
	}

	/**
	 * Configures if rule/condition 1 of ready dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule1(final boolean use) {
		setProperty(USE_RULE1_IN_READYDA, Boolean.valueOf(use));
	}

	/**
	 * Configures if rule/condition 2 of ready dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule2(final boolean use) {
		setProperty(USE_RULE2_IN_READYDA, Boolean.valueOf(use));
	}

	/**
	 * Configures if rule/condition 3 of ready dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule3(final boolean use) {
		setProperty(USE_RULE3_IN_READYDA, Boolean.valueOf(use));
	}

	/**
	 * Configures if rule/condition 4 of ready dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule4(final boolean use) {
		setProperty(USE_RULE4_IN_READYDA, Boolean.valueOf(use));
	}

	/**
	 * Sets if Safe Lock Analysis should be used during ready dependence calculation.
	 * 
	 * @param use <code>true</code> if SLA should be used; <code>false</code>, otherwise.
	 */
	public void useSafeLockAnalysisForReady(final boolean use) {
		setProperty(USE_SLA_FOR_READY_DA, Boolean.valueOf(use));
	}

	/**
	 * Configures if synchronization dependence analysis should be used during slicing.
	 * 
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useSynchronizationDepAnalysis(final boolean use) {
		setProperty(USE_SYNCHRONIZATIONDA, Boolean.valueOf(use));
	}

	/**
	 * {@inheritDoc} This implementation will always return <code>true</code>.
	 */
	@Override protected boolean processProperty(@SuppressWarnings("unused") final Comparable<?> propertyID,
			@SuppressWarnings("unused") final Object value) {
		return true;
	}

	/**
	 * Provides the id of the dependences to use for slicing.
	 * 
	 * @return a collection of id of the dependence analyses.
	 * @post result != null
	 */
	Collection<IDependencyAnalysis.DependenceSort> getIDsOfDAsToUse() {
		return Collections.unmodifiableCollection(dependencesToUse);
	}

	/**
	 * Retrieves slicing criteria generators.
	 * 
	 * @return the slice criteria generators.
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriteriaGenerator))
	 */
	Collection<ISliceCriteriaGenerator<?, ?>> getSliceCriteriaGenerators() {
		return id2critGenerators.values();
	}

	/**
	 * Set up this configuration for use.
	 */
	void setupForUse() {
		setupDeadlockPreservation();
		setupAssertionPreservation();
		setupDivergenceDependence();
		setupSynchronizationDependence();
		setupInterferenceDependence();
		setupReadyDependence();
		setupSliceTypeRelatedData();
	}

	/**
	 * Retrieves the boolean value of the given property.
	 * 
	 * @param propertyId identifies the property for which the value is required.
	 * @return the value associated with <code>propertyId</code>. Default value is <code>false</code>.
	 */
	private boolean getBooleanProperty(final Comparable<?> propertyId) {
		final Boolean _value = (Boolean) getProperty(propertyId);
		boolean _result = false;

		if (_value != null) {
			_result = _value.booleanValue();
		}

		return _result;
	}

	/**
	 * Retrieves the direction of divergence dependence that needs to be calculated.
	 * 
	 * @return the direction.
	 * @throws IllegalStateException if the direction cannot be decided due to illegal slice type.
	 */
	private IDependencyAnalysis.Direction getDivergenceDirection() throws IllegalStateException {
		final IDependencyAnalysis.Direction _result;

		if (SliceType.FORWARD_SLICE.equals(getSliceType())) {
			_result = IDependencyAnalysis.Direction.FORWARD_DIRECTION;
		} else if (SliceType.BACKWARD_SLICE.equals(getSliceType())) {
			_result = IDependencyAnalysis.Direction.BACKWARD_DIRECTION;
		} else if (SliceType.COMPLETE_SLICE.equals(getSliceType())) {
			_result = null;
		} else {
			final String _msg = "Illegal slice type :" + "" + " : " + getSliceType();
			LOGGER.error("setupDivergenceDependence" + "() -  : " + _msg);
			throw new IllegalStateException(_msg);
		}
		return _result;
	}

	/**
	 * Retrieves the class of ready dependence analysis to be used.
	 * 
	 * @param nature of ready dependence.
	 * @return the ready dependence analysis class.
	 * @throws IllegalStateException when the given nature is not supported.
	 */
	private Class<? extends ReadyDAv1> getReadyDAClass(final Comparable<?> nature) throws IllegalStateException {
		final Class<? extends ReadyDAv1> _result;

		if (SYMBOL_AND_EQUIVCLS_BASED_INFO.equals(nature)) {
			_result = ReadyDAv3.class;
		} else if (EQUIVALENCE_CLASS_BASED_INFO.equals(nature)) {
			_result = ReadyDAv2.class;
		} else if (TYPE_BASED_INFO.equals(nature)) {
			_result = ReadyDAv1.class;
		} else {
			final String _msg = "Ready dependence could not be configured due to illegal " + "dependence nature.";
			LOGGER.error("setupNatureOfReadyDep() -  : " + _msg);
			throw new IllegalStateException(_msg);
		}
		return _result;
	}

	/**
	 * Sets up assertion preserving part of the slicer.
	 */
	private void setupAssertionPreservation() {
		final Boolean _b = (Boolean) getProperty(SLICE_TO_PRESERVE_ASSERTIONS);

		if (_b.booleanValue()) {
			final StmtTypeBasedSliceCriteriaGenerator _t = new StmtTypeBasedSliceCriteriaGenerator();
			final Collection<Class<ThrowStmt>> _stmtTypes = Collections.singleton(ThrowStmt.class);
			_t.setStmtTypes(_stmtTypes);
			_t.setCriteriaFilterPredicate(new AssertionSliceCriteriaPredicate());

			if (areAssertionsOnlyInAppClassesConsidered()) {
				_t.setSiteSelectionPredicate(new ApplicationClassesOnlyPredicate());
			}
			id2critGenerators.put(ASSERTION_PRESERVING_CRITERIA_GENERATOR_ID, _t);
		} else {
			id2critGenerators.remove(ASSERTION_PRESERVING_CRITERIA_GENERATOR_ID);
		}
	}

	/**
	 * Sets up the deadlock preserving part of the slicer.
	 * 
	 * @throws IllegalStateException when the deadlock preserving part of the slicer cannot be setup.
	 */
	private void setupDeadlockPreservation() {
		if (getSliceForDeadlock()) {
			final String _property = (String) getProperty(DEADLOCK_CRITERIA_SELECTION_STRATEGY);
			final DeadlockPreservingCriteriaGenerator _t = new DeadlockPreservingCriteriaGenerator();
			id2critGenerators.put(DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID, _t);

			if (ALL_SYNC_CONSTRUCTS.equals(_property)) {
				_t.setCriteriaContextualizer(ISliceCriteriaContextualizer.DUMMY_CONTEXTUALIZER);
			} else if (ESCAPING_SYNC_CONSTRUCTS.equals(_property)) {
				_t.setCriteriaFilterPredicate(new EscapingSliceCriteriaPredicate());
				_t.setCriteriaContextualizer(ISliceCriteriaContextualizer.DUMMY_CONTEXTUALIZER);
			} else if (CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS.equals(_property)) {
				_t.setCriteriaFilterPredicate(new EscapingSliceCriteriaPredicate());
				final ThreadEscapeInfoBasedCallingContextRetrieverV2 _retriever = new ThreadEscapeInfoBasedCallingContextRetrieverV2(
						getCallingContextLimit(), IDependencyAnalysis.DependenceSort.INTERFERENCE_DA);
				_t.setCriteriaContextualizer(new DeadlockPreservingCriteriaCallStackContextualizer(_retriever));
			} else {
				final String _msg = "Deadlock preservation criteria generation could not be configured due to illegal "
						+ "criteria selection strategy.";

				LOGGER.error("setupDeadlockPreservation() -  : " + _msg);
				throw new IllegalStateException(_msg);
			}

			if (areSynchronizationsOnlyInAppClassesConsidered()) {
				_t.setSiteSelectionPredicate(new ApplicationClassesOnlyPredicate());
			}
		}
	}

	/**
	 * Sets up divergence dependence.
	 * 
	 * @throws IllegalStateException when divergence dependence cannot be setup due to invalid nature or illegal slice type.
	 */
	private void setupDivergenceDependence() {
		if (isDivergenceDepAnalysisUsed()) {
			final String _property = getNatureOfDivergenceDepAnalysis();
			final IDependencyAnalysis.Direction _direction = getDivergenceDirection();

			final boolean _interProcedural = INTRA_AND_INTER_PROCEDURAL.equals(_property);
			final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _das = new ArrayList<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

			if (_direction != null) {
				if (INTER_PROCEDURAL_ONLY.equals(_property)) {
					_das.add(InterProceduralDivergenceDA.getDivergenceDA(_direction));
				} else if (INTRA_PROCEDURAL_ONLY.equals(_property) || _interProcedural) {
					final DivergenceDA _divergenceDA = DivergenceDA.getDivergenceDA(_direction);
					_das.add(_divergenceDA);
					_divergenceDA.setConsiderCallSites(_interProcedural);
				}
			} else if (SliceType.COMPLETE_SLICE.equals(getSliceType())) {
				if (INTER_PROCEDURAL_ONLY.equals(_property)) {
					_das.add(InterProceduralDivergenceDA.getDivergenceDA(IDependencyAnalysis.Direction.FORWARD_DIRECTION));
					_das.add(InterProceduralDivergenceDA.getDivergenceDA(IDependencyAnalysis.Direction.BACKWARD_DIRECTION));
				} else if (INTRA_PROCEDURAL_ONLY.equals(_property) || _interProcedural) {
					final DivergenceDA _forwardDivergenceDA = DivergenceDA
							.getDivergenceDA(IDependencyAnalysis.Direction.FORWARD_DIRECTION);
					final DivergenceDA _backwardDivergenceDA = DivergenceDA
							.getDivergenceDA(IDependencyAnalysis.Direction.BACKWARD_DIRECTION);
					_das.add(_forwardDivergenceDA);
					_das.add(_backwardDivergenceDA);
					_forwardDivergenceDA.setConsiderCallSites(_interProcedural);
					_backwardDivergenceDA.setConsiderCallSites(_interProcedural);
				}
			}

			if (!_das.isEmpty()) {
				id2dependencyAnalyses.put(IDependencyAnalysis.DependenceSort.DIVERGENCE_DA, _das);
				dependencesToUse.add(IDependencyAnalysis.DependenceSort.DIVERGENCE_DA);
			} else {
				final String _msg = "Divergence dependence could not be configured due to illegal slice type or"
						+ "divergence dependence nature.";
				LOGGER.error("setupDivergenceDependence() -  : " + _msg);
				throw new IllegalStateException(_msg);
			}
		} else {
			dependencesToUse.remove(IDependencyAnalysis.DependenceSort.DIVERGENCE_DA);
			id2dependencyAnalyses.remove(IDependencyAnalysis.DependenceSort.DIVERGENCE_DA);
		}
	}

	/**
	 * Sets up interference dependence.
	 * 
	 * @throws IllegalStateException when interference dependence cannot be setup.
	 */
	private void setupInterferenceDependence() {
		final IDependencyAnalysis.DependenceSort _id = IDependencyAnalysis.DependenceSort.INTERFERENCE_DA;

		if (isInterferenceDepAnalysisUsed()) {
			dependencesToUse.add(_id);

			final Comparable<?> _property = getNatureOfInterferenceDepAnalysis();
			final Class<? extends InterferenceDAv1> _clazz;

			if (SYMBOL_AND_EQUIVCLS_BASED_INFO.equals(_property)) {
				_clazz = InterferenceDAv3.class;
			} else if (EQUIVALENCE_CLASS_BASED_INFO.equals(_property)) {
				_clazz = InterferenceDAv2.class;
			} else if (TYPE_BASED_INFO.equals(_property)) {
				_clazz = InterferenceDAv1.class;
			} else {
				final String _msg = "Interference dependence could not be configured due to illegal "
						+ "interference dependence nature.";
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);
				throw new IllegalStateException(_msg);
			}

			final Constructor<? extends InterferenceDAv1> _constructor;

			try {
				_constructor = _clazz.getConstructor((Class[]) null);
				final InterferenceDAv1 _newInstance = _constructor.newInstance((Object[]) null);
				id2dependencyAnalyses.put(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA, Collections
						.<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> singleton(_newInstance));
			} catch (final NoSuchMethodException _e) {
				final String _msg = "Dependence analysis does not provide zero parameter constructor :" + _clazz;
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);

				final RuntimeException _runtimeException = new RuntimeException(_msg);
				_runtimeException.initCause(_e);
				throw _runtimeException;
			} catch (final IllegalArgumentException _e) {
				final String _msg = "Dependence analysis does not provide zero-parameter constructor : " + _clazz;
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);
				throw _e;
			} catch (final SecurityException _e) {
				final String _msg = "Insufficient permission to access specified dependence analysis class : " + _clazz;
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);

				final RuntimeException _runtimeException = new RuntimeException(_msg);
				_runtimeException.initCause(_e);
				throw _runtimeException;
			} catch (final IllegalAccessException _e) {
				final String _msg = "Dependence analysis does not provide publicly accessible constructors : " + _clazz;
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);

				final RuntimeException _runtimeException = new RuntimeException(_msg);
				_runtimeException.initCause(_e);
				throw _runtimeException;
			} catch (final InvocationTargetException _e) {
				final String _msg = "constructor threw an exception : " + _clazz;
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);

				final RuntimeException _runtimeException = new RuntimeException(_msg);
				_runtimeException.initCause(_e);
				throw _runtimeException;
			} catch (final InstantiationException _e) {
				final String _msg = "Exception while instantiating the analysis : " + _clazz;
				LOGGER.error("setupInterferenceDependence() -  : " + _msg);

				final RuntimeException _runtimeException = new RuntimeException(_msg);
				_runtimeException.initCause(_e);
				throw _runtimeException;
			}

			for (final Iterator<? extends IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i = id2dependencyAnalyses.get(
					IDependencyAnalysis.DependenceSort.INTERFERENCE_DA).iterator(); _i.hasNext();) {
				final InterferenceDAv1 _ida = (InterferenceDAv1) _i.next();
				_ida.setUseOFA(isOFAUsedForInterference());
			}
		} else {
			dependencesToUse.remove(_id);
			id2dependencyAnalyses.remove(_id);
		}
	}

	/**
	 * Sets up the nature of ready dependence.
	 * 
	 * @param nature of ready dependence.
	 */
	private void setupNatureOfReadyDep(final Comparable<?> nature) {
		final Class<? extends ReadyDAv1> _clazz = getReadyDAClass(nature);

		try {
			final Set<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _temp = new HashSet<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

			if (SliceType.FORWARD_SLICE.equals(getSliceType())) {
				_temp.add((IDependencyAnalysis) _clazz.getMethod("getForwardReadyDA", (Class[]) null).invoke(null,
						(Object[]) null));
			} else if (SliceType.BACKWARD_SLICE.equals(getSliceType())) {
				_temp.add((IDependencyAnalysis) _clazz.getMethod("getBackwardReadyDA", (Class[]) null).invoke(null,
						(Object[]) null));
			} else if (SliceType.COMPLETE_SLICE.equals(getSliceType())) {
				_temp.add((IDependencyAnalysis) _clazz.getMethod("getBackwardReadyDA", (Class[]) null).invoke(null,
						(Object[]) null));
				_temp.add((IDependencyAnalysis) _clazz.getMethod("getForwardReadyDA", (Class[]) null).invoke(null,
						(Object[]) null));
			} else {
				final String _msg = "Illegal slice type :" + _clazz.toString() + " : " + getSliceType();
				LOGGER.error("setupNatureOfReadyDep" + "() -  : " + _msg);
				throw new IllegalStateException(_msg);
			}
			id2dependencyAnalyses.put(IDependencyAnalysis.DependenceSort.READY_DA, _temp);
		} catch (final NoSuchMethodException _e) {
			final String _msg = "Dependence analysis does not provide getForwardReadyDA() and/or getBackwardReadyDA() :"
					+ _clazz;
			LOGGER.error("setupNatureOfReadyDep() -  : " + _msg);

			final RuntimeException _runtimeException = new RuntimeException(_msg);
			_runtimeException.initCause(_e);
			throw _runtimeException;
		} catch (final IllegalArgumentException _e) {
			final String _msg = "Dependence analysis does not provide static zero-parameter versions of "
					+ "getForwardReadyDA() and/or getBackwardReadyDA() : " + _clazz;
			LOGGER.error("setupNatureOfReadyDep() -  : " + _msg);
			throw _e;
		} catch (final SecurityException _e) {
			final String _msg = "Insufficient permission to access specified ready dependence analysis class : " + _clazz;
			LOGGER.error("setupNatureOfReadyDep() -  : " + _msg);

			final RuntimeException _runtimeException = new RuntimeException(_msg);
			_runtimeException.initCause(_e);
			throw _runtimeException;
		} catch (final IllegalAccessException _e) {
			final String _msg = "Dependence analysis does not provide publicly accessible versions of  "
					+ "getForwardReadyDA() and/or getBackwardReadyDA() : " + _clazz;
			LOGGER.error("setupNatureOfReadyDep() -  : " + _msg);

			final RuntimeException _runtimeException = new RuntimeException(_msg);
			_runtimeException.initCause(_e);
			throw _runtimeException;
		} catch (final InvocationTargetException _e) {
			final String _msg = "getForwardReadyDA() and/or getBackwardReadyDA() threw an exception : " + _clazz;
			LOGGER.error("setupNatureOfReadyDep() -  : " + _msg);

			final RuntimeException _runtimeException = new RuntimeException(_msg);
			_runtimeException.initCause(_e);
			throw _runtimeException;
		}
	}

	/**
	 * Sets up ready dependence rules.
	 */
	private void setupRDARules() {
		int _rule = 0;

		if (isReadyRule1Used()) {
			_rule = ReadyDAv1.RULE_1;
		}

		if (isReadyRule2Used()) {
			_rule = ReadyDAv1.RULE_2;
		}

		if (isReadyRule3Used()) {
			_rule = ReadyDAv1.RULE_3;
		}

		if (isReadyRule4Used()) {
			_rule = ReadyDAv1.RULE_4;
		}

		final Collection<? extends IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _c = id2dependencyAnalyses
				.get(IDependencyAnalysis.DependenceSort.READY_DA);

		for (final Iterator<? extends IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _iter = _c.iterator(); _iter.hasNext();) {
			final ReadyDAv1 _rd = (ReadyDAv1) _iter.next();
			final int _rules = _rd.getRules();
			_rd.setRules(_rules | _rule);
		}
	}

	/**
	 * Sets up the ready dependence.
	 */
	private void setupReadyDependence() {
		final IDependencyAnalysis.DependenceSort _id = IDependencyAnalysis.DependenceSort.READY_DA;

		if (isReadyDepAnalysisUsed()) {
			dependencesToUse.add(_id);
			setupNatureOfReadyDep(getNatureOfReadyDepAnalysis());

			final boolean _usedForReady = isOFAUsedForReady();

			for (final Iterator<? extends IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i = id2dependencyAnalyses.get(_id)
					.iterator(); _i.hasNext();) {
				final ReadyDAv1 _rda = (ReadyDAv1) _i.next();
				_rda.setUseOFA(_usedForReady);
			}

			final boolean _safeLockAnalysisUsedForReady = isSafeLockAnalysisUsedForReady();

			for (final Iterator<? extends IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i = id2dependencyAnalyses.get(_id)
					.iterator(); _i.hasNext();) {
				final ReadyDAv1 _rda = (ReadyDAv1) _i.next();
				_rda.setUseSafeLockAnalysis(_safeLockAnalysisUsedForReady);
			}

			final boolean _callSiteSensitiveReadyUsed = isCallSiteSensitiveReadyUsed();

			for (final Iterator<? extends IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i = id2dependencyAnalyses.get(_id)
					.iterator(); _i.hasNext();) {
				final ReadyDAv1 _rda = (ReadyDAv1) _i.next();
				_rda.setConsiderCallSites(_callSiteSensitiveReadyUsed);
			}
			setupRDARules();
		} else {
			dependencesToUse.remove(_id);
			id2dependencyAnalyses.remove(_id);
		}
	}

	/**
	 * Sets up the part of the slicer dependent on the direction of the slice.
	 * 
	 * @throws IllegalStateException when direction dependent part of the slicer cannot be setup.
	 */
	private void setupSliceTypeRelatedData() {
		final SliceType _sliceType = getSliceType();
		id2dependencyAnalyses.put(IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA, Collections
				.<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> singleton(new IdentifierBasedDataDAv3()));
		id2dependencyAnalyses.put(IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA, Collections
				.<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> singleton(new ReferenceBasedDataDA()));

		final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _c = new HashSet<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

		if (isNonTerminationSensitiveControlDependenceUsed()) {
			_c.add(new NonTerminationSensitiveEntryControlDA());
		} else {
			_c.add(new NonTerminationInsensitiveEntryControlDA());
		}

		if (_sliceType.equals(SliceType.FORWARD_SLICE)) {
			_c.add(new ExitControlDA());
			setProperty(EXECUTABLE_SLICE, Boolean.FALSE);
		} else if (_sliceType.equals(SliceType.COMPLETE_SLICE)) {
			_c.add(new ExitControlDA());
		} else if (!_sliceType.equals(SliceType.BACKWARD_SLICE)) {
			throw new IllegalStateException("Slice type was not either of BACKWARD_SLICE, FORWARD_SLICE, "
					+ "or COMPLETE_SLICE.");
		}

		id2dependencyAnalyses.put(IDependencyAnalysis.DependenceSort.CONTROL_DA, _c);
	}

	/**
	 * Sets up synchronization dependence.
	 */
	private void setupSynchronizationDependence() {
		final IDependencyAnalysis.DependenceSort _id = IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA;

		if (isSynchronizationDepAnalysisUsed()) {
			dependencesToUse.add(_id);
			id2dependencyAnalyses.put(_id, Collections
					.<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> singleton(new SynchronizationDA()));
		} else {
			dependencesToUse.remove(_id);
			id2dependencyAnalyses.remove(_id);
		}
	}
}

// End of File
