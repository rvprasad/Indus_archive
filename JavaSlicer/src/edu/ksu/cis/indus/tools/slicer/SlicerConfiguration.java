
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
import edu.ksu.cis.indus.staticanalyses.dependency.EntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.ExitControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.SynchronizationDA;

import edu.ksu.cis.indus.tools.AbstractToolConfiguration;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.IToolConfigurationFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


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
	 * This identifies the property that indicates if interprocedural divergence dependence should be used instead of mere
	 * intraprocedural divergent dependence.
	 */
	static final Object INTERPROCEDURAL_DIVERGENCEDA = "interprocedural divergence dependence";

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
	 * This identifies the property that indicates if slice criteria should be automatically picked for slicing such that the
	 * slice has the same deadlock behavior as the original program.
	 */
	static final Object SLICE_FOR_DEADLOCK = "slice for deadlock";

	/**
	 * This identifies the option to create executable slice.
	 */
	static final Object EXECUTABLE_SLICE = "executable slice";

	/**
	 * This identifies the property that indicates the slice type, i.e., forward or complete slice.
	 */
	static final Object SLICE_TYPE = "slice type";

	/**
	 * This is the factory object to create configurations.
	 */
	private static IToolConfigurationFactory factorySingleton = new SlicerConfiguration();

	/**
	 * This indicates if executable slice should be generated.
	 */
	protected boolean executableSlice = true;

	/**
	 * This indicates if the tool should criteria that ensure the deadlock behavior of the slice is same as that of the
	 * original program.
	 */
	protected boolean sliceForDeadlock;

	/**
	 * The collection of ids of the dependences to be considered for slicing.
	 *
	 * @invariant dependencesToUse.oclIsKindOf(String)
	 */
	private final Collection dependencesToUse = new HashSet();

	/**
	 * This maps IDs to dependency analyses.
	 *
	 * @invariant id2dependencyAnalyses.oclIsKindOf(Map(Object, Collection(DependencyAnalysis)))
	 */
	private final Map id2dependencyAnalyses = new HashMap();

	/**
	 * Creates a new SlicerConfiguration object.
	 */
	protected SlicerConfiguration() {
		propertyIds.add(USE_DIVERGENCEDA);
		propertyIds.add(INTERPROCEDURAL_DIVERGENCEDA);
		propertyIds.add(NATURE_OF_INTERFERENCE_DA);
		propertyIds.add(USE_READYDA);
		propertyIds.add(NATURE_OF_READY_DA);
		propertyIds.add(USE_RULE1_IN_READYDA);
		propertyIds.add(USE_RULE2_IN_READYDA);
		propertyIds.add(USE_RULE3_IN_READYDA);
		propertyIds.add(USE_RULE4_IN_READYDA);
		propertyIds.add(SLICE_FOR_DEADLOCK);
		propertyIds.add(SLICE_TYPE);
		propertyIds.add(EXECUTABLE_SLICE);
	}

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfigurationFactory#createToolConfiguration()
	 */
	public IToolConfiguration createToolConfiguration() {
		return makeToolConfiguration();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation by default uses interference dependency analysis based on equivalence class-based escape analysis.
	 * It does not use ready and divergence dependences.  It defaults to calculating executable backward slices based on
	 * slicing for deadlock.
	 * </p>
	 *
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#initialize()
	 */
	public void initialize() {
		// default required fixed dependency analyses
		dependencesToUse.add(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA);
		id2dependencyAnalyses.put(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA,
			Collections.singleton(new IdentifierBasedDataDA()));
		dependencesToUse.add(DependencyAnalysis.REFERENCE_BASED_DATA_DA);
		id2dependencyAnalyses.put(DependencyAnalysis.REFERENCE_BASED_DATA_DA,
			Collections.singleton(new ReferenceBasedDataDA()));
		dependencesToUse.add(DependencyAnalysis.SYNCHRONIZATION_DA);
		id2dependencyAnalyses.put(DependencyAnalysis.SYNCHRONIZATION_DA, Collections.singleton(new SynchronizationDA()));
		dependencesToUse.add(DependencyAnalysis.INTERFERENCE_DA);
		dependencesToUse.add(DependencyAnalysis.CONTROL_DA);

		// set default values for certain properties
		setProperty(NATURE_OF_INTERFERENCE_DA, SYMBOL_AND_EQUIVCLS_BASED_INFO);
		setProperty(USE_READYDA, Boolean.FALSE);
		setProperty(USE_DIVERGENCEDA, Boolean.FALSE);
		setProperty(SLICE_TYPE, SlicingEngine.BACKWARD_SLICE);
		setProperty(EXECUTABLE_SLICE, Boolean.TRUE);
		setProperty(SLICE_FOR_DEADLOCK, Boolean.TRUE);
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
	protected boolean isInterproceduralDivergenceDepAnalysisUsed() {
		return getBooleanProperty(INTERPROCEDURAL_DIVERGENCEDA);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void setNatureOfInterferenceDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_INTERFERENCE_DA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected String getNatureOfInterferenceDepAnalysis() {
		return (String) properties.get(NATURE_OF_INTERFERENCE_DA);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param use Should not be used!
	 */
	protected void setNatureOfReadyDepAnalysis(final String use) {
		super.setProperty(NATURE_OF_READY_DA, use);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected String getNatureOfReadyDepAnalysis() {
		String _result = (String) properties.get(NATURE_OF_READY_DA);

		if (_result == null) {
			_result = SYMBOL_AND_EQUIVCLS_BASED_INFO.toString();
		}
		return _result;
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
		return getBooleanProperty(USE_RULE1_IN_READYDA);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule2Used() {
		return getBooleanProperty(USE_RULE2_IN_READYDA);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule3Used() {
		return getBooleanProperty(USE_RULE3_IN_READYDA);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @return Should not be used!
	 */
	protected boolean isReadyRule4Used() {
		return getBooleanProperty(USE_RULE4_IN_READYDA);
	}

	/**
	 * This method is used for java-xml binding <b>only</b>.  Hence, this is not part of the supported interface.
	 *
	 * @param type Should not be used!
	 */
	protected void setSliceType(final String type) {
		if (SlicingEngine.SLICE_TYPES.contains(type)) {
			properties.put(SLICE_TYPE, type);

			if (type.equals(SlicingEngine.BACKWARD_SLICE)) {
				Collection _c = (Collection) id2dependencyAnalyses.get(DependencyAnalysis.CONTROL_DA);

				if (_c == null) {
					_c = new HashSet();
					id2dependencyAnalyses.put(DependencyAnalysis.CONTROL_DA, _c);
				} else {
					_c.clear();
				}
				_c.add(new EntryControlDA());
			} else if (type.equals(SlicingEngine.FORWARD_SLICE)) {
				Collection _c = (Collection) id2dependencyAnalyses.get(DependencyAnalysis.CONTROL_DA);

				if (_c == null) {
					_c = new HashSet();
					id2dependencyAnalyses.put(DependencyAnalysis.CONTROL_DA, _c);
				} else {
					_c.clear();
				}
				_c.add(new ExitControlDA());
			} else if (type.equals(SlicingEngine.COMPLETE_SLICE)) {
				Collection _c = (Collection) id2dependencyAnalyses.get(DependencyAnalysis.CONTROL_DA);

				if (_c == null) {
					_c = new HashSet();
					id2dependencyAnalyses.put(DependencyAnalysis.CONTROL_DA, _c);
				} else {
					_c.clear();
				}
				_c.add(new EntryControlDA());
				_c.add(new ExitControlDA());
			}
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
		boolean _result = true;

		if (value instanceof Boolean) {
			final Boolean _val = (Boolean) value;

			if (propertyID.equals(USE_READYDA)) {
				processUseProperty(_val, DependencyAnalysis.READY_DA, Collections.singleton(new ReadyDAv1()));
			} else if (propertyID.equals(USE_DIVERGENCEDA)) {
				processUseProperty(_val, DependencyAnalysis.DIVERGENCE_DA, Collections.singleton(new DivergenceDA()));
			} else if (propertyID.equals(INTERPROCEDURAL_DIVERGENCEDA)) {
				processInterProceduralDivergenceDAProperty();
			} else if (propertyID.equals(SLICE_FOR_DEADLOCK)) {
				sliceForDeadlock = _val.booleanValue();
			} else if (propertyID.equals(EXECUTABLE_SLICE)) {
				executableSlice = _val.booleanValue();
			} else {
				processRDARuleProperties(propertyID);
			}
		} else if (propertyID.equals(SLICE_TYPE)) {
			if (!SlicingEngine.SLICE_TYPES.contains(value)) {
				_result = false;
			}
		} else if (propertyID.equals(NATURE_OF_INTERFERENCE_DA)) {
			_result = processIDANatureProperty(value);
		} else if (propertyID.equals(NATURE_OF_READY_DA)) {
			_result = processRDANatureProperty(value);
		}
		return _result;
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
	 * Retrieves the configuration factory object.
	 *
	 * @return the configuration factory.
	 *
	 * @post result != null
	 */
	static IToolConfigurationFactory getFactory() {
		return factorySingleton;
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
		_result.setConfigName("configuration" + System.currentTimeMillis());
		_result.initialize();
		return _result;
	}

	/**
	 * Provides the dependency analysis corresponding to the given id.
	 *
	 * @param id of the requested dependence analyses.
	 *
	 * @return the dependency analyses identified by <code>id</code>.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	Collection getDependenceAnalysis(final Object id) {
		Collection _result = (Collection) id2dependencyAnalyses.get(id);

		if (_result == null) {
			_result = Collections.EMPTY_LIST;
		}
		return _result;
	}

	/**
	 * Provides the id of the dependences to use for slicing.
	 *
	 * @return a collection of id of the dependence analyses.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(String))
	 */
	Collection getNamesOfDAsToUse() {
		return Collections.unmodifiableCollection(dependencesToUse);
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
			id2dependencyAnalyses.put(DependencyAnalysis.INTERFERENCE_DA, Collections.singleton(new InterferenceDAv3()));
		} else if (property.equals(EQUIVALENCE_CLASS_BASED_INFO)) {
			id2dependencyAnalyses.put(DependencyAnalysis.INTERFERENCE_DA, Collections.singleton(new InterferenceDAv2()));
		} else if (property.equals(TYPE_BASED_INFO)) {
			id2dependencyAnalyses.put(DependencyAnalysis.INTERFERENCE_DA, Collections.singleton(new InterferenceDAv1()));
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * Processes the property that indicates if the divergence dependence should be interprocedural.
	 */
	private void processInterProceduralDivergenceDAProperty() {
		final Boolean _bool = (Boolean) properties.get(USE_DIVERGENCEDA);

		if (_bool != null && _bool.booleanValue()) {
			final Collection _c = (Collection) id2dependencyAnalyses.get(DependencyAnalysis.DIVERGENCE_DA);
			final boolean _temp = _bool.booleanValue();

			for (final Iterator _iter = _c.iterator(); _iter.hasNext();) {
				final DivergenceDA _dda = (DivergenceDA) _iter.next();

				_dda.setConsiderCallSites(_temp);
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
		super.setProperty(id, Boolean.valueOf(value));
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
			id2dependencyAnalyses.put(DependencyAnalysis.READY_DA, Collections.singleton(new ReadyDAv3()));
		} else if (property.equals(EQUIVALENCE_CLASS_BASED_INFO)) {
			id2dependencyAnalyses.put(DependencyAnalysis.READY_DA, Collections.singleton(new ReadyDAv2()));
		} else if (property.equals(TYPE_BASED_INFO)) {
			id2dependencyAnalyses.put(DependencyAnalysis.READY_DA, Collections.singleton(new ReadyDAv1()));
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

			final Collection _c = (Collection) id2dependencyAnalyses.get(DependencyAnalysis.READY_DA);

			for (final Iterator _iter = _c.iterator(); _iter.hasNext();) {
				final ReadyDAv1 _rd = (ReadyDAv1) _iter.next();
				final int _rules = _rd.getRules();
				_rd.setRules(_rules | _rule);
			}
		}
	}

	/**
	 * Process properties that indicates which dependence analysis should be used.
	 *
	 * @param val is the boolean that indicates the inclusion/exclusion of the analysis.
	 * @param daID is the id of the analysis.
	 * @param das is the collection of analyses that should be included (if <code>val</code> indicates inclusion).
	 *
	 * @pre val != null and daID != null and das != null
	 * @invariant das.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	private void processUseProperty(final Boolean val, final Object daID, final Collection das) {
		if (val.booleanValue()) {
			dependencesToUse.add(daID);
			id2dependencyAnalyses.put(daID, das);
		} else {
			dependencesToUse.remove(daID);
			id2dependencyAnalyses.remove(daID);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.25  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.24  2003/12/02 11:32:01  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.23  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.22  2003/12/02 01:30:50  venku
   - coding conventions and formatting.
   Revision 1.21  2003/11/28 16:40:26  venku
   - cosmetic.
   Revision 1.20  2003/11/25 17:51:26  venku
   - split control dependence into 2 classes.
     EntryControlDA handled control DA as required for backward slicing.
     ExitControlDA handles control DA as required for forward slicing.
   - ripple effect.
   Revision 1.19  2003/11/16 18:33:01  venku
   - fixed an error while returning the DAs.
   Revision 1.18  2003/11/16 18:24:08  venku
   - added methods to retrive active dependencies.
   - documentation and formatting.
   Revision 1.17  2003/11/09 08:12:49  venku
   - values of all boolean properties are discovered by getBooleanProperty().
     If the property does not exist, a default value will be returned.
   - initialization will populate default values for all properties.
   - configurations are given default name on creation.
   - factory method will initialize the configuration after creation.
   Revision 1.16  2003/11/05 08:26:42  venku
   - changed the xml schema for the slicer configuration.
   - The configruator, driver, and the configuration handle
     these changes.
   Revision 1.15  2003/11/05 02:46:54  venku
   - added control dependence into the list of dependences to use.
   Revision 1.14  2003/11/03 08:05:34  venku
   - lots of changes
     - changes to get the configuration working with JiBX
     - changes to make configuration amenable to CompositeConfigurator
     - added EquivalenceClassBasedAnalysis
     - added fix for Thread's start method
   Revision 1.13  2003/10/21 06:07:01  venku
   - added support for executable slice.
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
