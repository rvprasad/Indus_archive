
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;

import edu.ksu.cis.indus.slicer.SlicingEngine;

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
import edu.ksu.cis.indus.tools.slicer.criteria.predicates.ISliceCriteriaPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JThrowStmt;


/**
 * This represents a configurationCollection of the slicer.  The slicer tool should be configured via an object of this class
 * obtained from the slicer tool.  The type of the propoerty values are documented with the property identifiers.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerConfiguration
  extends AbstractToolConfiguration
  implements IToolConfigurationFactory {
	/** 
	 * This identifies the property that indicates if equivalence class based interference dependence should be used  instead
	 * of naive type-based interference dependence. This is tied to values of <i>slicer:natureOfInterThreadAnalysis</i>
	 * attribute in <code>slicerConfig_JiBXBinding.xml</code>.
	 */
	static final Object EQUIVALENCE_CLASS_BASED_INFO = "EQUIVALENCE_CLASS_BASED_INFO";

	/** 
	 * This identifies the property that indicates if symbol and equivalence class based interference dependence should be
	 * used  instead of naive type-based interference dependence. This is tied to values of
	 * <i>slicer:natureOfInterThreadAnalysis</i>  attribute in <code>slicerConfig_JiBXBinding.xml</code>.
	 */
	static final Object SYMBOL_AND_EQUIVCLS_BASED_INFO = "SYMBOL_AND_EQUIVCLS_BASED_INFO";

	/** 
	 * This indicates type based information. This is tied to values of <i>slicer:natureOfInterThreadAnalysis</i>  attribute
	 * in <code>slicerConfig_JiBXBinding.xml</code>.
	 */
	static final Object TYPE_BASED_INFO = "TYPE_BASED_INFO";

	/** 
	 * This identifies the property that indicates the nature of interference dependence, i.e., type based, etc.
	 */
	static final Object NATURE_OF_INTERFERENCE_DA = "nature of interference dependence";

	/** 
	 * This identifies the property that indicates the nature of ready dependence, i.e., type based, etc.
	 */
	static final Object NATURE_OF_READY_DA = "nature of ready dependence";

	/** 
	 * This identifies the property that indicates if interference dependence should be considered for slicing.
	 */
	static final Object USE_INTERFERENCEDA = "use interference dependence";

	/** 
	 * This identifies the property that indicates if ready dependence should be considered for slicing.
	 */
	static final Object USE_READYDA = "use ready dependence";

	/** 
	 * This identifies the property that indicates if rule1 of ready dependence be used.  Rule 1:  m is dependent on n if m
	 * and n occur in the same thread and n is an enter monitor statement.
	 */
	static final Object USE_RULE1_IN_READYDA = "use rule1 in ready dependence";

	/** 
	 * This identifies the property that indicates if ready dependence should be considered for slicing.
	 */
	static final Object USE_SYNCHRONIZATIONDA = "use synchronization dependences";

	/** 
	 * This identifies the property that indicates if rule2 of ready dependence be used.  Rule 2: m is dependent on n if m
	 * and n occur in different threads and m and n are is exit monitor and enter monitor statements, respectively.
	 */
	static final Object USE_RULE2_IN_READYDA = "use rule2 in ready dependence";

	/** 
	 * This identifies the property that indicates if rule3 of ready dependence be used.  Rule 3: m is dependent on n if m
	 * and n occur in the same thread and m has a call to java.lang.Object.wait.
	 */
	static final Object USE_RULE3_IN_READYDA = "use rule3 in ready dependence";

	/** 
	 * This identifies the property that indicates if rule4 of ready dependence be used.  Rule 4: m is dependent on n if m
	 * and n occur in the different thread and m and n have calls to java.lang.Object.wait(XXX) and
	 * java.lang.Object.notifyXXX(), respectively..
	 */
	static final Object USE_RULE4_IN_READYDA = "use rule4 in ready dependence";

	/** 
	 * This identifies the property that indicates if divergence dependence should be considered for slicing.
	 */
	static final Object USE_DIVERGENCEDA = "use divergence dependence";

	/** 
	 * This identifies the property that indicates the nature of divergence dependence, i.e., intra, inter, and intra-inter.
	 */
	static final Object NATURE_OF_DIVERGENCE_DA = "nature of divergence dependence";

	/** 
	 * This indicates pure intra-procedural setting.
	 */
	static final Object INTRA_PROCEDURAL_ONLY = "INTRA_PROCEDURAL_ONLY";

	/** 
	 * This indicates pure inter-procedural setting.
	 */
	static final Object INTER_PROCEDURAL_ONLY = "INTER_PROCEDURAL_ONLY";

	/** 
	 * This indicates intra- and inter-procedural setting.
	 */
	static final Object INTRA_AND_INTER_PROCEDURAL = "INTRA_AND_INTER_PROCEDURAL";

	/** 
	 * This identifies the property that indicates if slice criteria should be automatically picked to preserve the
	 * deadlocking property of the program.
	 */
	static final Object SLICE_FOR_DEADLOCK = "slice for deadlock";

	/** 
	 * This identifies the property that indicates if slice criteria should be automatically picked to preserve assertions in
	 * the program.
	 */
	static final Object SLICE_TO_PRESERVE_ASSERTIONS = "slice to preserve assertions";

	/** 
	 * This indicates all-synchronization-constructs-should-be-considered deadlock criteria selection strategy.
	 */
	static final Object ALL_SYNC_CONSTRUCTS = "ALL_SYNC_CONSTRUCTS";

	/** 
	 * This indicates all-synchronization-constructs-with-escaping-monitors-should-be-considered deadlock criteria selection
	 * strategy.
	 */
	static final Object ESCAPING_SYNC_CONSTRUCTS = "ESCAPING_SYNC_CONSTRUCTS";

	/** 
	 * This indicates
	 * all-synchronization-constructs-with-escaping-monitors-should-be-considered-in-a-context-sensitive-manner deadlock
	 * criteria selection strategy.
	 */
	static final Object CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS = "CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS";

	/** 
	 * This identifies the option to create executable slice.
	 */
	static final Object EXECUTABLE_SLICE = "executable slice";

	/** 
	 * This identifies the property that indicates the slice type, i.e., forward or complete slice.
	 */
	static final Object SLICE_TYPE = "slice type";

	/** 
	 * This identifies the property that indicates if object flow information should be used in the context of interference
	 * dependence.
	 */
	static final Object USE_OFA_FOR_INTERFERENCE_DA = "use ofa for interference";

	/** 
	 * This identifies the property that indicates if object flow information should be used in the context of ready
	 * dependence.
	 */
	static final Object USE_OFA_FOR_READY_DA = "use ofa for ready";

	/** 
	 * This identifies the property that indicates if safe lock analysis should be used in the context of ready dependence.
	 */
	static final Object USE_SLA_FOR_READY_DA = "use sla for ready";

	/** 
	 * This identifies the property that determines the strategy used to select criteria to preserve deadlock.
	 */
	static final Object DEADLOCK_CRITERIA_SELECTION_STRATEGY = "deadlock criteria selection strategy";

	/** 
	 * This identifies the property that determines if property aware slicing is required.
	 */
	static final Object PROPERTY_AWARE = "property aware slicing";

	/** 
	 * This identifies the property that determines if the slice is non-termination sensitive.
	 */
	static final Object NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE = "Non termination sensitive";

	/** 
	 * This identifies the property that determines if call site sensitive ready dependence is used.
	 */
	static final Object CALL_SITE_SENSITIVE_READY_DA = "call site sensitive ready dependence";

	/** 
	 * This is the factory object to create configurations.
	 */
	private static final IToolConfigurationFactory FACTORY_SINGLETON = new SlicerConfiguration();

	/** 
	 * This is the id of criteria generator for deadlock preserving criteria generation.
	 */
	private static final Object DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID = "deadlock preserving criteria generator id";

	/** 
	 * This is the id of criteria generator for assertion preserving criteria generation.
	 */
	private static final Object ASSERTION_PRESERVING_CRITERIA_GENERATOR_ID = "assertion preserving criteria generator id";

	/** 
	 * The collection of ids of the dependences to be considered for slicing.
	 *
	 * @invariant dependencesToUse.oclIsKindOf(String)
	 */
	private final Collection dependencesToUse = new HashSet();

	/** 
	 * This maps identifiers to criteria generators.
	 *
	 * @invariant id2critGenerators.oclIsKindOf(Map(Object, ISliceCriteriaGenerator))
	 */
	private final Map id2critGenerators = new HashMap();

	/** 
	 * This maps IDs to dependency analyses.
	 *
	 * @invariant id2dependencyAnalyses.oclIsKindOf(Map(Object, Collection(AbstractDependencyAnalysis)))
	 */
	private final Map id2dependencyAnalyses = new HashMap();

	/**
	 * Creates a new SlicerConfiguration object.
	 */
	protected SlicerConfiguration() {
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
		propertyIds.add(USE_DIVERGENCEDA);
		propertyIds.add(NATURE_OF_DIVERGENCE_DA);
		propertyIds.add(INTRA_PROCEDURAL_ONLY);
		propertyIds.add(INTRA_AND_INTER_PROCEDURAL);
		propertyIds.add(INTER_PROCEDURAL_ONLY);
		propertyIds.add(USE_SYNCHRONIZATIONDA);
		propertyIds.add(CALL_SITE_SENSITIVE_READY_DA);
	}

	/**
	 * Checks if call-site sensitive ready dependence is used.
	 *
	 * @return <code>true</code> if call-site based ready dependence is used; <code>false</code>, otherwise.
	 */
	public boolean isCallSiteSensitiveReadyUsed() {
		return ((Boolean) properties.get(CALL_SITE_SENSITIVE_READY_DA)).booleanValue();
	}

	/**
	 * Sets the strategy to be used to select deadlock preserving criteria.
	 *
	 * @param dc specifies the strategy.  It has to be one of <code>ALL_SYNC_CONSTRUCTS</code> or
	 * 		  <code>ESCAPING_SYNC_CONSTRUCTS</code>.
	 */
	public void setDeadlockCriteriaSelectionStrategy(final String dc) {
		super.setProperty(DEADLOCK_CRITERIA_SELECTION_STRATEGY, dc);
	}

	/**
	 * Retrieves the strategy used to select deadlock perserving criteria.
	 *
	 * @return the selection strategy used.
	 */
	public String getDeadlockCriteriaSelectionStrategy() {
		return (String) properties.get(DEADLOCK_CRITERIA_SELECTION_STRATEGY);
	}

	/**
	 * Provides the dependency analysis corresponding to the given id.
	 *
	 * @param id of the requested dependence analyses.
	 *
	 * @return the dependency analyses identified by <code>id</code>.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(IDependencyAnalysis))
	 */
	public Collection getDependenceAnalyses(final Object id) {
		Collection _result = (Collection) id2dependencyAnalyses.get(id);

		if (_result == null) {
			_result = Collections.EMPTY_LIST;
		}
		return _result;
	}

	/**
	 * Checks if divergence dependence analysis is enabled in this configuration.
	 *
	 * @return <code>true</code> if the use of divergence dependence analysis is enabled; <code>false</code>, otherwise.
	 */
	public boolean isDivergenceDepAnalysisUsed() {
		return ((Boolean) properties.get(USE_DIVERGENCEDA)).booleanValue();
	}

	/**
	 * Sets the executability of the generated slice.
	 *
	 * @param value <code>true</code> indicates executable slice should be generated; <code>false</code>, otherwise.
	 *
	 * @throws IllegalStateException when executability is set on forward slices.
	 */
	public void setExecutableSlice(final boolean value) {
		if (!getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			setProperty(EXECUTABLE_SLICE, Boolean.valueOf(value));
		} else if (value) {
			throw new IllegalStateException("Forward Executable Slices are not supported.");
		}
	}

	/**
	 * Retrieves the executability of the generated slice.
	 *
	 * @return <code>true</code> indicates executable slice should be generated; <code>false</code>, otherwise.
	 */
	public boolean getExecutableSlice() {
		return ((Boolean) getProperty(EXECUTABLE_SLICE)).booleanValue();
	}

	/**
	 * Checks if interference dependence analysis is enabled in this configuration.
	 *
	 * @return <code>true</code> if the use of interference dependence analysis is enabled; <code>false</code>, otherwise.
	 */
	public boolean isInterferenceDepAnalysisUsed() {
		return ((Boolean) properties.get(USE_INTERFERENCEDA)).booleanValue();
	}

	/**
	 * Sets the nature of divergence dependence analysis to be used.
	 *
	 * @param use specifies the nature of analysis.  It has to be one of values defined by
	 * 		  <code>INTRA_PROCEDURAL_DIVERGENCE</code>,  <code>INTER_PROCEDURAL_DIVERGENCE</code>, and
	 * 		  <code>INTRA_AND_INTER_PROCEDURAL_DIVERGENCE</code>.
	 *
	 * @pre use != null
	 */
	public void setNatureOfDivergenceDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_DIVERGENCE_DA, use);
	}

	/**
	 * Retrieves the nature of divergence dependence analysis specified by this configuration.
	 *
	 * @return the nature of divergence dependence analysis.
	 *
	 * @post result != null
	 */
	public String getNatureOfDivergenceDepAnalysis() {
		String _result = (String) properties.get(NATURE_OF_DIVERGENCE_DA);

		if (_result == null) {
			_result = INTRA_PROCEDURAL_ONLY.toString();
		}
		return _result;
	}

	/**
	 * Sets the nature of interference dependence analysis to be used.
	 *
	 * @param use specifies the nature of analysis.  It has to be one of values defined by
	 * 		  <code>EQUIVALENCE_CLASS_BASED_INFO</code>,  <code>SYMBOL_AND_EQUIVCLS_BASED_INFO</code>, and
	 * 		  <code>TYPE_BASED_INFO</code>.
	 *
	 * @pre use != null
	 */
	public void setNatureOfInterferenceDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_INTERFERENCE_DA, use);
	}

	/**
	 * Retrieves the nature of interference dependence analysis specified by this configuration.
	 *
	 * @return <code>true</code> if the use of interference dependence analysis enabled; <code>false</code>, otherwise.
	 *
	 * @post result != null
	 */
	public String getNatureOfInterferenceDepAnalysis() {
		return (String) properties.get(NATURE_OF_INTERFERENCE_DA);
	}

	/**
	 * Sets the nature of ready dependence analysis to be used.
	 *
	 * @param use specifies the nature of analysis.  It has to be one of values defined by
	 * 		  <code>EQUIVALENCE_CLASS_BASED_INFO</code>,  <code>SYMBOL_AND_EQUIVCLS_BASED_INFO</code>, and
	 * 		  <code>TYPE_BASED_INFO</code>.
	 *
	 * @pre use != null
	 */
	public void setNatureOfReadyDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_READY_DA, use);
	}

	/**
	 * Retrieves the nature of ready dependence analysis specified by this configuration.
	 *
	 * @return the nature of ready dependence analysis.
	 *
	 * @post result != null
	 */
	public String getNatureOfReadyDepAnalysis() {
		String _result = (String) properties.get(NATURE_OF_READY_DA);

		if (_result == null) {
			_result = SYMBOL_AND_EQUIVCLS_BASED_INFO.toString();
		}
		return _result;
	}

	/**
	 * Checks if non-termination sensitive control dependence should be used.
	 *
	 * @return <code>true</code> if non-termination sensitive control dependence should be used; <code>false</code>,
	 * 		   otherwise.
	 */
	public boolean isNonTerminationSensitiveControlDependenceUsed() {
		return ((Boolean) getProperty(NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE)).booleanValue();
	}

	/**
	 * Checks if OFA is being used for interference dependence calculation.
	 *
	 * @return <code>true</code> if OFA is used for interference dependence; <code>false</code>, otherwise.
	 */
	public boolean isOFAUsedForInterference() {
		return ((Boolean) properties.get(USE_OFA_FOR_INTERFERENCE_DA)).booleanValue();
	}

	/**
	 * Checks if OFA is being used for ready dependence calculation.
	 *
	 * @return <code>true</code> if OFA is used for ready dependence; <code>false</code>, otherwise.
	 */
	public boolean isOFAUsedForReady() {
		return ((Boolean) properties.get(USE_OFA_FOR_READY_DA)).booleanValue();
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
	 * Checks if property aware slices will be generated.
	 *
	 * @return <code>true</code> if the property aware slices will be generated; <code>false</code>, otherwise.
	 */
	public boolean getPropertyAware() {
		return ((Boolean) getProperty(PROPERTY_AWARE)).booleanValue();
	}

	/**
	 * Checks if ready dependence analysis is enabled in this configuration.
	 *
	 * @return <code>true</code> if the use of ready dependence analysis is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyDepAnalysisUsed() {
		return ((Boolean) properties.get(USE_READYDA)).booleanValue();
	}

	/**
	 * Checks if ready dependence condition/rule 1 is enabled.  Rule 1 being the intraprocedural ready dependence induced by
	 * enter monitor statements.
	 *
	 * @return <code>true</code> if ready dependence analysis rule 1 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule1Used() {
		return getBooleanProperty(USE_RULE1_IN_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 2 is enabled.  Rule 2 being the interprocedural ready dependence induced by
	 * enter/exit monitor statements.
	 *
	 * @return <code>true</code> if ready dependence analysis rule 2 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule2Used() {
		return getBooleanProperty(USE_RULE2_IN_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 3 is enabled.  Rule 3 being the intraprocedural ready dependence induced by
	 * wait statements.
	 *
	 * @return <code>true</code> if ready dependence analysis rule 3 is enabled; <code>false</code>, otherwise.
	 */
	public boolean isReadyRule3Used() {
		return getBooleanProperty(USE_RULE3_IN_READYDA);
	}

	/**
	 * Checks if ready dependence condition/rule 4 is enabled.  Rule 4 being the interprocedural ready dependence induced by
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
		return ((Boolean) properties.get(USE_SLA_FOR_READY_DA)).booleanValue();
	}

	/**
	 * Sets the preservation of deadlocking in the slice.
	 *
	 * @param value <code>true</code> indicates slice should preserve deadlocking properties; <code>false</code>, otherwise.
	 */
	public void setSliceForDeadlock(final boolean value) {
		setProperty(SLICE_FOR_DEADLOCK, Boolean.valueOf(value));
	}

	/**
	 * Checks if the slice was done to preserve deadlocking properties.
	 *
	 * @return <code>true</code> indicates slice should preserve deadlocking properties; <code>false</code>, otherwise.
	 */
	public boolean getSliceForDeadlock() {
		return ((Boolean) getProperty(SLICE_FOR_DEADLOCK)).booleanValue();
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
	 * Checks if the slice was done to preserve assertions.
	 *
	 * @return <code>true</code> indicates slice should preserve assertions; <code>false</code>, otherwise.
	 */
	public boolean getSliceToPreserveAssertions() {
		return ((Boolean) getProperty(SLICE_TO_PRESERVE_ASSERTIONS)).booleanValue();
	}

	/**
	 * Sets the type of slice to be generated.
	 *
	 * @param type specifies the type of slice.  It has to be one of values defined by
	 * 		  <code>SlicingEngine.BACKWARD_SLICE</code>,  <code>SlicingEngine.FORWARD_SLICE</code>, and
	 * 		  <code>SlicingEngine.COMPLETE_SLICE</code>.
	 *
	 * @pre use != null
	 */
	public void setSliceType(final String type) {
		if (SlicingEngine.SLICE_TYPES.contains(type)) {
			properties.put(SLICE_TYPE, type);

			final Collection _c = CollectionsUtilities.getSetFromMap(id2dependencyAnalyses, IDependencyAnalysis.CONTROL_DA);

			if (type.equals(SlicingEngine.BACKWARD_SLICE)) {
				_c.clear();

				if (isNonTerminationSensitiveControlDependenceUsed()) {
					_c.add(new NonTerminationSensitiveEntryControlDA());
				} else {
					_c.add(new NonTerminationInsensitiveEntryControlDA());
				}
			} else if (type.equals(SlicingEngine.FORWARD_SLICE)) {
				_c.clear();
				_c.add(new ExitControlDA());
				setProperty(EXECUTABLE_SLICE, Boolean.FALSE);
			} else if (type.equals(SlicingEngine.COMPLETE_SLICE)) {
				_c.clear();

				if (isNonTerminationSensitiveControlDependenceUsed()) {
					_c.add(new NonTerminationSensitiveEntryControlDA());
				} else {
					_c.add(new NonTerminationInsensitiveEntryControlDA());
				}

				_c.add(new ExitControlDA());
			}
		}
	}

	/**
	 * Retrieves the type of slice that will be generated.
	 *
	 * @return the type of slice.
	 *
	 * @post result != null
	 */
	public String getSliceType() {
		return properties.get(SLICE_TYPE).toString();
	}

	/**
	 * Checks if synchronization dependence analysis is enabled in this configuration.
	 *
	 * @return <code>true</code> if the use of synchronization dependence analysis is enabled; <code>false</code>, otherwise.
	 */
	public boolean isSynchronizationDepAnalysisUsed() {
		return ((Boolean) properties.get(USE_SYNCHRONIZATIONDA)).booleanValue();
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
	public boolean equals(final Object object) {
		boolean _result = false;

		if (object instanceof SlicerConfiguration) {
			final SlicerConfiguration _config = (SlicerConfiguration) object;
			_result =
				new EqualsBuilder().appendSuper(super.equals(object)).append(this.propertyIds, _config.propertyIds)
									 .append(this.id2dependencyAnalyses, _config.id2dependencyAnalyses)
									 .append(this.dependencesToUse, _config.dependencesToUse)
									 .append(this.properties, _config.properties).isEquals();
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(propertyIds).append(id2dependencyAnalyses)
											.append(dependencesToUse).append(properties).toHashCode();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This configuration defaults to use interference and ready dependency analyses based on equivalence class-based escape
	 * analysis, synchronization dependences, identifier based dependences, reference based dependence, and  calculate
	 * executable backward slice required to detect deadlocks.
	 * </p>
	 *
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#initialize()
	 */
	public void initialize() {
		// set default values for certain properties.  The ordering is important.
		setProperty(NON_TERMINATION_SENSITIVE_CONTROL_DEPENDENCE, Boolean.TRUE);
		setProperty(USE_DIVERGENCEDA, Boolean.FALSE);
		setProperty(SLICE_TYPE, SlicingEngine.BACKWARD_SLICE);
		setProperty(EXECUTABLE_SLICE, Boolean.TRUE);
		setProperty(SLICE_FOR_DEADLOCK, Boolean.TRUE);
		setProperty(SLICE_TO_PRESERVE_ASSERTIONS, Boolean.FALSE);
		setProperty(NATURE_OF_INTERFERENCE_DA, SYMBOL_AND_EQUIVCLS_BASED_INFO);
		setProperty(USE_OFA_FOR_INTERFERENCE_DA, Boolean.TRUE);
		setProperty(USE_READYDA, Boolean.TRUE);
		setProperty(USE_INTERFERENCEDA, Boolean.TRUE);
		setProperty(NATURE_OF_READY_DA, SYMBOL_AND_EQUIVCLS_BASED_INFO);
		setProperty(USE_OFA_FOR_READY_DA, Boolean.TRUE);
		setProperty(USE_RULE1_IN_READYDA, Boolean.TRUE);
		setProperty(USE_RULE2_IN_READYDA, Boolean.TRUE);
		setProperty(USE_RULE3_IN_READYDA, Boolean.TRUE);
		setProperty(USE_RULE4_IN_READYDA, Boolean.TRUE);
		setProperty(CALL_SITE_SENSITIVE_READY_DA, Boolean.TRUE);
		setProperty(USE_SLA_FOR_READY_DA, Boolean.TRUE);
		setProperty(PROPERTY_AWARE, Boolean.FALSE);
		setProperty(USE_SYNCHRONIZATIONDA, Boolean.FALSE);

		id2critGenerators.put(DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID, new DeadlockPreservingCriteriaGenerator());
		setProperty(DEADLOCK_CRITERIA_SELECTION_STRATEGY, CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS);

		// default required fixed dependency analyses
		dependencesToUse.add(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);
		id2dependencyAnalyses.put(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA,
			Collections.singleton(new IdentifierBasedDataDAv3()));
		dependencesToUse.add(IDependencyAnalysis.REFERENCE_BASED_DATA_DA);
		id2dependencyAnalyses.put(IDependencyAnalysis.REFERENCE_BASED_DATA_DA,
			Collections.singleton(new ReferenceBasedDataDA()));
		dependencesToUse.add(IDependencyAnalysis.CONTROL_DA);
	}

	/**
	 * Sets if call-site sensitive ready dependence is used.
	 *
	 * @param use <code>true</code> if call-site based ready dependence should be used; <code>false</code>, otherwise.
	 */
	public void useCallSiteSensitiveReady(final boolean use) {
		processPropertyHelper(CALL_SITE_SENSITIVE_READY_DA, use);
	}

	/**
	 * Configures if divergence dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useDivergenceDepAnalysis(final boolean use) {
		processPropertyHelper(USE_DIVERGENCEDA, use);
	}

	/**
	 * Configures if interference dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useInterferenceDepAnalysis(final boolean use) {
		processPropertyHelper(USE_INTERFERENCEDA, use);
	}

	/**
	 * Sets if non-termination sensitive or non-termination insensitive control dependence should be used.
	 *
	 * @param value <code>true</code> indicates non-termination sensitive control dependence should be used;
	 * 		  <code>false</code>, otherwise.
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
		processPropertyHelper(USE_OFA_FOR_INTERFERENCE_DA, use);
	}

	/**
	 * Sets if OFA should be used during ready dependence calculation.
	 *
	 * @param use <code>true</code> if OFA should be used; <code>false</code>, otherwise.
	 */
	public void useOFAForReady(final boolean use) {
		processPropertyHelper(USE_OFA_FOR_READY_DA, use);
	}

	/**
	 * Configures if ready dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyDepAnalysis(final boolean use) {
		processPropertyHelper(USE_READYDA, use);
	}

	/**
	 * Configures if rule/condition 1 of ready dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule1(final boolean use) {
		processPropertyHelper(USE_RULE1_IN_READYDA, use);
	}

	/**
	 * Configures if rule/condition 2 of ready dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule2(final boolean use) {
		processPropertyHelper(USE_RULE2_IN_READYDA, use);
	}

	/**
	 * Configures if rule/condition 3 of ready dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule3(final boolean use) {
		processPropertyHelper(USE_RULE3_IN_READYDA, use);
	}

	/**
	 * Configures if rule/condition 4 of ready dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useReadyRule4(final boolean use) {
		processPropertyHelper(USE_RULE4_IN_READYDA, use);
	}

	/**
	 * Sets if Safe Lock Analysis should be used during ready dependence calculation.
	 *
	 * @param use <code>true</code> if SLA should be used; <code>false</code>, otherwise.
	 */
	public void useSafeLockAnalysisForReady(final boolean use) {
		processPropertyHelper(USE_SLA_FOR_READY_DA, use);
	}

	/**
	 * Configures if synchronization dependence analysis should be used during slicing.
	 *
	 * @param use <code>true</code> if it should be used; <code>false</code>, otherwise.
	 */
	public void useSynchronizationDepAnalysis(final boolean use) {
		processPropertyHelper(USE_SYNCHRONIZATIONDA, use);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @pre value != null
	 */
	protected boolean processProperty(final Object propertyID, final Object value) {
		boolean _result = true;

		if (value instanceof Boolean) {
			processBooleanProperty(propertyID, (Boolean) value);
		} else if (propertyID.equals(SLICE_TYPE)) {
			if (!SlicingEngine.SLICE_TYPES.contains(value)) {
				_result = false;
			}
			setSliceType(value.toString());
		} else if (propertyID.equals(NATURE_OF_INTERFERENCE_DA)) {
			_result = processIDANatureProperty(value);
		} else if (propertyID.equals(NATURE_OF_READY_DA)) {
			_result = processRDANatureProperty(value);
		} else if (propertyID.equals(DEADLOCK_CRITERIA_SELECTION_STRATEGY)) {
			processDeadlockCriteriaSelectionStrategy(value);
		} else if (propertyID.equals(NATURE_OF_DIVERGENCE_DA)) {
			_result = processDDANatureProperty(value);
		}
		return _result;
	}

	/**
	 * Retrieves the configuration factory object.
	 *
	 * @return the configuration factory.
	 *
	 * @post result != null
	 */
	static IToolConfigurationFactory getFactory() {
		return FACTORY_SINGLETON;
	}

	/**
	 * Provides the id of the dependences to use for slicing.
	 *
	 * @return a collection of id of the dependence analyses.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Object))
	 */
	Collection getIDsOfDAsToUse() {
		return Collections.unmodifiableCollection(dependencesToUse);
	}

	/**
	 * Retrieves slicing criteria generators.
	 *
	 * @return the slice criteria generators.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriteriaGenerator))
	 */
	Collection getSliceCriteriaGenerators() {
		return id2critGenerators.values();
	}

	/**
	 * Factory method to create a configuration. This is used by the factory and in java-2-xml binding.  It is adviced to use
	 * the factory object rather than using this method.
	 *
	 * @return a new instance of a configuration.
	 *
	 * @post result != null
	 */
	static SlicerConfiguration makeToolConfiguration() {
		final SlicerConfiguration _result = new SlicerConfiguration();
		_result.setConfigName("tool_configuration_" + System.currentTimeMillis());
		_result.initialize();
		return _result;
	}

	/**
	 * Retrieves the boolean value of the given property.
	 *
	 * @param propertyId identifies the property for which the value is required.
	 *
	 * @return the value associated with <code>propertyId</code>.  Default value is <code>false</code>.
	 */
	private boolean getBooleanProperty(final Object propertyId) {
		final Boolean _value = (Boolean) properties.get(propertyId);
		boolean _result = false;

		if (_value != null) {
			_result = _value.booleanValue();
		}

		return _result;
	}

	/**
	 * Processes boolean property.
	 *
	 * @param propertyID is the id of the property to be set based on <code>booleanValue</code>.
	 * @param booleanValue is the value  that decides the value of the property identified by <code>propertyID</code>.
	 *
	 * @pre propertyID != null and booleanValue != null
	 */
	private void processBooleanProperty(final Object propertyID, final Boolean booleanValue) {
		if (propertyID.equals(SLICE_FOR_DEADLOCK)) {
			if (booleanValue.booleanValue()) {
				id2critGenerators.put(DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID, new DeadlockPreservingCriteriaGenerator());
			} else {
				id2critGenerators.remove(DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID);
			}
		} else if (propertyID.equals(SLICE_TO_PRESERVE_ASSERTIONS)) {
			if (booleanValue.booleanValue()) {
				final StmtTypeBasedSliceCriteriaGenerator _t = new StmtTypeBasedSliceCriteriaGenerator();
				final Collection _stmtTypes = new ArrayList();
				_stmtTypes.add(JAssignStmt.class);
				_stmtTypes.add(JThrowStmt.class);
				_t.setStmtTypes(_stmtTypes);
				_t.setCriteriaFilter(new AssertionSliceCriteriaPredicate());
				id2critGenerators.put(ASSERTION_PRESERVING_CRITERIA_GENERATOR_ID, _t);
			} else {
				id2critGenerators.remove(ASSERTION_PRESERVING_CRITERIA_GENERATOR_ID);
			}
		} else if (propertyID.equals(USE_DIVERGENCEDA)) {
			processUseDivergenceProperty(booleanValue);
		} else if (propertyID.equals(USE_SYNCHRONIZATIONDA)) {
			processUseSynchronizationProperty(booleanValue);
		} else {
			processInterferenceDARelatedProperties(propertyID, booleanValue);
			processReadyDARelatedProperties(propertyID, booleanValue);
		}
	}

	/**
	 * Processes the property that dictates the nature of the divergence dependence.
	 *
	 * @param property is the property.
	 *
	 * @return <code>true</code> if the property value was a valid;  <code>false</code>, otherwise.
	 *
	 * @pre property != null
	 */
	private boolean processDDANatureProperty(final Object property) {
		boolean _result = true;

		final Object _direction;

		if (getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			_direction = IDependencyAnalysis.FORWARD_DIRECTION;
		} else if (getSliceType().equals(SlicingEngine.BACKWARD_SLICE)) {
			_direction = IDependencyAnalysis.BACKWARD_DIRECTION;
		} else {
			_direction = null;
		}

		final boolean _interProcedural = property.equals(INTRA_AND_INTER_PROCEDURAL);

		if (_direction != null) {
			if (property.equals(INTER_PROCEDURAL_ONLY)) {
				id2dependencyAnalyses.put(IDependencyAnalysis.DIVERGENCE_DA,
					Collections.singleton(InterProceduralDivergenceDA.getDivergenceDA(_direction)));
			} else if (property.equals(INTRA_PROCEDURAL_ONLY) || _interProcedural) {
				final DivergenceDA _divergenceDA = DivergenceDA.getDivergenceDA(_direction);
				id2dependencyAnalyses.put(IDependencyAnalysis.DIVERGENCE_DA, Collections.singleton(_divergenceDA));
				_divergenceDA.setConsiderCallSites(_interProcedural);
			}
		} else if (getSliceType().equals(SlicingEngine.COMPLETE_SLICE)) {
			final Collection _das = new ArrayList();

			if (property.equals(INTER_PROCEDURAL_ONLY)) {
				_das.add(InterProceduralDivergenceDA.getDivergenceDA(IDependencyAnalysis.FORWARD_DIRECTION));
				_das.add(InterProceduralDivergenceDA.getDivergenceDA(IDependencyAnalysis.BACKWARD_DIRECTION));
			} else if (property.equals(INTRA_PROCEDURAL_ONLY) || _interProcedural) {
				final DivergenceDA _forwardDivergenceDA = DivergenceDA.getDivergenceDA(IDependencyAnalysis.FORWARD_DIRECTION);
				final DivergenceDA _backwardDivergenceDA =
					DivergenceDA.getDivergenceDA(IDependencyAnalysis.BACKWARD_DIRECTION);
				_das.add(_forwardDivergenceDA);
				_das.add(_backwardDivergenceDA);
				_forwardDivergenceDA.setConsiderCallSites(_interProcedural);
				_backwardDivergenceDA.setConsiderCallSites(_interProcedural);
			}

			id2dependencyAnalyses.put(IDependencyAnalysis.DIVERGENCE_DA, _das);
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * Processes the property that dictates the strategy use to select deadlock criteria.
	 *
	 * @param property is the property.
	 *
	 * @return <code>true</code> if the property value was a valid;  <code>false</code>, otherwise.
	 *
	 * @pre property != null
	 */
	private boolean processDeadlockCriteriaSelectionStrategy(final Object property) {
		boolean _result = false;

		if (getSliceForDeadlock()) {
			_result = true;

			final ISliceCriteriaGenerator _t =
				(ISliceCriteriaGenerator) id2critGenerators.get(DEADLOCK_PRESERVING_CRITERIA_GENERATOR_ID);

			if (property.equals(ALL_SYNC_CONSTRUCTS)) {
				_t.setCriteriaFilter(ISliceCriteriaPredicate.DUMMY_FILTER);
				_t.setCriteriaContextualizer(ISliceCriteriaContextualizer.DUMMY_CONTEXTUALIZER);
			} else if (property.equals(ESCAPING_SYNC_CONSTRUCTS)) {
				_t.setCriteriaFilter(new EscapingSliceCriteriaPredicate());
				_t.setCriteriaContextualizer(ISliceCriteriaContextualizer.DUMMY_CONTEXTUALIZER);
			} else if (property.equals(CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS)) {
				_t.setCriteriaFilter(new EscapingSliceCriteriaPredicate());
				_t.setCriteriaContextualizer(new DeadlockPreservingCriteriaCallStackContextualizer());
			} else {
				_result = false;
			}
		}
		return _result;
	}

	/**
	 * Processes the property that dictates the nature of the interference dependence.
	 *
	 * @param property is the property.
	 *
	 * @return <code>true</code> if the property value was a valid;  <code>false</code>, otherwise.
	 *
	 * @pre property != null
	 */
	private boolean processIDANatureProperty(final Object property) {
		boolean _result = true;

		if (property.equals(SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			id2dependencyAnalyses.put(IDependencyAnalysis.INTERFERENCE_DA, Collections.singleton(new InterferenceDAv3()));
		} else if (property.equals(EQUIVALENCE_CLASS_BASED_INFO)) {
			id2dependencyAnalyses.put(IDependencyAnalysis.INTERFERENCE_DA, Collections.singleton(new InterferenceDAv2()));
		} else if (property.equals(TYPE_BASED_INFO)) {
			id2dependencyAnalyses.put(IDependencyAnalysis.INTERFERENCE_DA, Collections.singleton(new InterferenceDAv1()));
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * Processes properties related to Interference dependence analysis.
	 *
	 * @param propertyID obviously.
	 * @param booleanValue obviously.
	 *
	 * @pre propertyID != null and booleanValue != null
	 */
	private void processInterferenceDARelatedProperties(final Object propertyID, final Boolean booleanValue) {
		if (propertyID.equals(USE_INTERFERENCEDA)) {
			final Object _id = IDependencyAnalysis.INTERFERENCE_DA;

			if (booleanValue.booleanValue()) {
				dependencesToUse.add(_id);
				processIDANatureProperty(SYMBOL_AND_EQUIVCLS_BASED_INFO);
			} else {
				dependencesToUse.remove(_id);
				id2dependencyAnalyses.remove(_id);
			}
		} else if (propertyID.equals(USE_OFA_FOR_INTERFERENCE_DA)) {
			for (final Iterator _i = ((Collection) id2dependencyAnalyses.get(IDependencyAnalysis.INTERFERENCE_DA)).iterator();
				  _i.hasNext();) {
				final InterferenceDAv1 _ida = (InterferenceDAv1) _i.next();
				_ida.setUseOFA(booleanValue.booleanValue());
			}
		}
	}

	/**
	 * Processes the given property and it's value.
	 *
	 * @param id of the property to be processed.
	 * @param value of the property.
	 */
	private void processPropertyHelper(final Object id, final boolean value) {
		setProperty(id, Boolean.valueOf(value));
	}

	/**
	 * Processes the property that dictates the nature of the ready dependence.
	 *
	 * @param property is the property.
	 *
	 * @return <code>true</code> if the property value was a valid;  <code>false</code>, otherwise.
	 *
	 * @pre property != null
	 */
	private boolean processRDANatureProperty(final Object property) {
		boolean _result;
		_result = true;

		if (property.equals(SYMBOL_AND_EQUIVCLS_BASED_INFO)) {
			final Collection _temp = new HashSet();

			if (getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
				_temp.add(ReadyDAv3.getForwardReadyDA());
			} else if (getSliceType().equals(SlicingEngine.BACKWARD_SLICE)) {
				_temp.add(ReadyDAv3.getBackwardReadyDA());
			} else if (getSliceType().equals(SlicingEngine.COMPLETE_SLICE)) {
				_temp.add(ReadyDAv3.getBackwardReadyDA());
				_temp.add(ReadyDAv3.getForwardReadyDA());
			}
			id2dependencyAnalyses.put(IDependencyAnalysis.READY_DA, _temp);
		} else if (property.equals(EQUIVALENCE_CLASS_BASED_INFO)) {
			final Collection _temp = new HashSet();

			if (getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
				_temp.add(ReadyDAv2.getForwardReadyDA());
			} else if (getSliceType().equals(SlicingEngine.BACKWARD_SLICE)) {
				_temp.add(ReadyDAv2.getBackwardReadyDA());
			} else if (getSliceType().equals(SlicingEngine.COMPLETE_SLICE)) {
				_temp.add(ReadyDAv2.getBackwardReadyDA());
				_temp.add(ReadyDAv2.getForwardReadyDA());
			}
			id2dependencyAnalyses.put(IDependencyAnalysis.READY_DA, _temp);
		} else if (property.equals(TYPE_BASED_INFO)) {
			final Collection _temp = new HashSet();

			if (getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
				_temp.add(ReadyDAv1.getForwardReadyDA());
			} else if (getSliceType().equals(SlicingEngine.BACKWARD_SLICE)) {
				_temp.add(ReadyDAv1.getBackwardReadyDA());
			} else if (getSliceType().equals(SlicingEngine.COMPLETE_SLICE)) {
				_temp.add(ReadyDAv1.getBackwardReadyDA());
				_temp.add(ReadyDAv1.getForwardReadyDA());
			}
			id2dependencyAnalyses.put(IDependencyAnalysis.READY_DA, _temp);
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * Processes the property that indicates which of the ready dependency rules should be considered.
	 *
	 * @param propertyID is one of <code>USE_RULE1_IN_READYDA</code>, <code>USE_RULE2_IN_READYDA</code>,
	 * 		  <code>USE_RULE3_IN_READYDA</code>, and <code>USE_RULE4_IN_READYDA</code>.
	 *
	 * @pre propertyID != null
	 */
	private void processRDARuleProperties(final Object propertyID) {
		final Boolean _bool = (Boolean) properties.get(USE_READYDA);

		if (_bool != null && _bool.booleanValue()) {
			int _rule = 0;

			if (propertyID.equals(USE_RULE1_IN_READYDA)) {
				_rule = ReadyDAv1.RULE_1;
			} else if (propertyID.equals(USE_RULE2_IN_READYDA)) {
				_rule = ReadyDAv1.RULE_2;
			} else if (propertyID.equals(USE_RULE3_IN_READYDA)) {
				_rule = ReadyDAv1.RULE_3;
			} else if (propertyID.equals(USE_RULE4_IN_READYDA)) {
				_rule = ReadyDAv1.RULE_4;
			}

			final Collection _c = (Collection) id2dependencyAnalyses.get(IDependencyAnalysis.READY_DA);

			for (final Iterator _iter = _c.iterator(); _iter.hasNext();) {
				final ReadyDAv1 _rd = (ReadyDAv1) _iter.next();
				final int _rules = _rd.getRules();
				_rd.setRules(_rules | _rule);
			}
		}
	}

	/**
	 * Processes properties related to Ready dependence analysis.
	 *
	 * @param propertyID obviously.
	 * @param booleanValue obviously.
	 *
	 * @pre propertyID != null and booleanValue != null
	 */
	private void processReadyDARelatedProperties(final Object propertyID, final Boolean booleanValue) {
		if (propertyID.equals(USE_READYDA)) {
			final Object _id = IDependencyAnalysis.READY_DA;

			if (booleanValue.booleanValue()) {
				dependencesToUse.add(_id);
				processRDANatureProperty(SYMBOL_AND_EQUIVCLS_BASED_INFO);
			} else {
				dependencesToUse.remove(_id);
				id2dependencyAnalyses.remove(_id);
			}
		} else if (propertyID.equals(USE_OFA_FOR_READY_DA)) {
			for (final Iterator _i = ((Collection) id2dependencyAnalyses.get(IDependencyAnalysis.READY_DA)).iterator();
				  _i.hasNext();) {
				final ReadyDAv1 _rda = (ReadyDAv1) _i.next();
				_rda.setUseOFA(booleanValue.booleanValue());
			}
		} else if (propertyID.equals(USE_SLA_FOR_READY_DA)) {
			for (final Iterator _i = ((Collection) id2dependencyAnalyses.get(IDependencyAnalysis.READY_DA)).iterator();
				  _i.hasNext();) {
				final ReadyDAv1 _rda = (ReadyDAv1) _i.next();
				_rda.setUseSafeLockAnalysis(booleanValue.booleanValue());
			}
		} else if (propertyID.equals(CALL_SITE_SENSITIVE_READY_DA)) {
			for (final Iterator _i = ((Collection) id2dependencyAnalyses.get(IDependencyAnalysis.READY_DA)).iterator();
				  _i.hasNext();) {
				final ReadyDAv1 _rda = (ReadyDAv1) _i.next();
				_rda.setConsiderCallSites(booleanValue.booleanValue());
			}
		} else {
			processRDARuleProperties(propertyID);
		}
	}

	/**
	 * Process property that indicates if divergence dependence analysis should be used.
	 *
	 * @param val is the boolean that indicates the inclusion/exclusion of the analysis.
	 *
	 * @pre val != null
	 */
	private void processUseDivergenceProperty(final Boolean val) {
		final Object _id = IDependencyAnalysis.DIVERGENCE_DA;

		if (val.booleanValue()) {
			dependencesToUse.add(_id);
			processDDANatureProperty(getNatureOfDivergenceDepAnalysis());
		} else {
			dependencesToUse.remove(_id);
			id2dependencyAnalyses.remove(_id);
		}
	}

	/**
	 * Process property that indicates if synchronization dependence analysis should be used.
	 *
	 * @param val is the boolean that indicates the inclusion/exclusion of the analysis.
	 *
	 * @pre val != null
	 */
	private void processUseSynchronizationProperty(final Boolean val) {
		final Object _id = IDependencyAnalysis.SYNCHRONIZATION_DA;

		if (val.booleanValue()) {
			dependencesToUse.add(_id);
			id2dependencyAnalyses.put(_id, Collections.singleton(new SynchronizationDA()));
		} else {
			dependencesToUse.remove(_id);
			id2dependencyAnalyses.remove(_id);
		}
	}
}

// End of File
