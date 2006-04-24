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

package edu.ksu.cis.indus.common.scoping;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * This class represents scope definition. It can be used to filter classes, methods, and fields based on names and
 * hierarchical relation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SpecificationBasedScopeDefinition {

	/**
	 * Indentation space during serialization.
	 */
	private static final int INDENT = 4;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationBasedScopeDefinition.class);

	/**
	 * The collection of class-level specification.
	 */
	@NonNull @NonNullContainer private Collection<ClassSpecification> classSpecs;

	/**
	 * The collection of field-level specification.
	 */
	@NonNull @NonNullContainer private Collection<FieldSpecification> fieldSpecs;

	/**
	 * The collection of method-level specification.
	 */
	@NonNull @NonNullContainer private Collection<MethodSpecification> methodSpecs;

	/**
	 * The name of this specification.
	 */
	@NonNull @NonNullContainer private String name;

	/**
	 * Creates an instance of this class.
	 */
	@Empty public SpecificationBasedScopeDefinition() {
		super();
	}

	/**
	 * Deserializes the given content into a scope definition.
	 * 
	 * @param contents to be deserialized.
	 * @return a scope definition.
	 * @throws JiBXException when the content cannot be deserialized due to IO exceptions or malformed contents.
	 */
	public static SpecificationBasedScopeDefinition deserialize(@NonNull final String contents) throws JiBXException {
		final SpecificationBasedScopeDefinition _result;
		IUnmarshallingContext _unmarshallingContext;

		try {
			final IBindingFactory _bindingFactory = BindingDirectory.getFactory(SpecificationBasedScopeDefinition.class);
			_unmarshallingContext = _bindingFactory.createUnmarshallingContext();
		} catch (final JiBXException _e) {
			LOGGER.error("Error while setting up JiBX.  Aborting.", _e);
			throw _e;
		}

		try {
			final StringReader _reader = new StringReader(contents);
			_result = (SpecificationBasedScopeDefinition) _unmarshallingContext.unmarshalDocument(_reader, null);
		} catch (final JiBXException _e) {
			LOGGER.error("Error while deserializing scope specification.", _e);
			throw _e;
		}

		return _result;
	}

	/**
	 * Serializes the given scope definition.
	 * 
	 * @param scopeDef to be serialized.
	 * @return the serialized form the scope definition.
	 * @throws JiBXException when the content cannot be serialized.
	 */
	public static String serialize(@NonNull final SpecificationBasedScopeDefinition scopeDef) throws JiBXException {
		final String _result;
		IMarshallingContext _marshallingContext;

		try {
			final IBindingFactory _bindingFactory = BindingDirectory.getFactory(SpecificationBasedScopeDefinition.class);
			_marshallingContext = _bindingFactory.createMarshallingContext();
			_marshallingContext.setIndent(INDENT);
		} catch (final JiBXException _e) {
			LOGGER.error("Error while setting up JiBX.", _e);
			throw _e;
		}

		try {
			final StringWriter _writer = new StringWriter();
			_marshallingContext.marshalDocument(scopeDef, "UTF-8", Boolean.TRUE, _writer);
			_writer.flush();
			_result = _writer.toString();
		} catch (final JiBXException _e) {
			LOGGER.error("Error while marshalling scope specification.", _e);
			throw _e;
		}
		return _result;
	}

	/**
	 * Creates the container for specifications. This is used by java-xml binding.
	 * 
	 * @param <T> is a specification type.
	 * @return a container.
	 */
	@NonNull static <T extends ISpecification> Collection<T> createSpecContainer() {
		return new ArrayList<T>();
	}

	/**
	 * Retrieves the value in <code>classSpecs</code>.
	 * 
	 * @return the value in <code>classSpecs</code>.
	 */
	@Functional @NonNull public Collection<ClassSpecification> getClassSpecs() {
		return classSpecs;
	}

	/**
	 * Retrieves the value in <code>fieldSpecs</code>.
	 * 
	 * @return the value in <code>fieldSpecs</code>.
	 */
	@Functional @NonNull public Collection<FieldSpecification> getFieldSpecs() {
		return fieldSpecs;
	}

	/**
	 * Retrieves the value in <code>methodSpecs</code>.
	 * 
	 * @return the value in <code>methodSpecs</code>.
	 */
	@Functional @NonNull public Collection<MethodSpecification> getMethodSpecs() {
		return methodSpecs;
	}

	/**
	 * Retrieves the value in <code>name</code>.
	 * 
	 * @return the value in <code>name</code>.
	 */
	@Functional @NonNull public String getName() {
		return name;
	}

	/**
	 * Checks if the given class is in the scope in the given system.
	 * 
	 * @param clazz to be checked.
	 * @param system in which to check.
	 * @return <code>true</code> if the given class is in the scope in the given system; <code>false</code>, otherwise.
	 */
	@Functional public boolean isInScope(@NonNull final SootClass clazz, @NonNull final IEnvironment system) {
		final Iterator<ClassSpecification> _i = classSpecs.iterator();
		final int _iEnd = classSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final ClassSpecification _cs = _i.next();
			_result |= _cs.isInScope(clazz, system);
		}
		return _result;
	}

	/**
	 * Checks if the given field is in the scope in the given system.
	 * 
	 * @param field to be checked.
	 * @param system in which to check.
	 * @return <code>true</code> if the given field is in the scope in the given system; <code>false</code>, otherwise.
	 */
	@Functional public boolean isInScope(@NonNull final SootField field, @NonNull final IEnvironment system) {
		final Iterator<FieldSpecification> _i = fieldSpecs.iterator();
		final int _iEnd = fieldSpecs.size();
		boolean _result;
		if (!classSpecs.isEmpty()) {
			_result = isInScope(field.getDeclaringClass(), system);
		} else {
			_result = false;
		}

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final FieldSpecification _fs = _i.next();
			_result |= _fs.isInScope(field, system);
		}
		return _result;
	}

	/**
	 * Checks if the given method is in the scope in the given system.
	 * 
	 * @param method to be checked.
	 * @param system in which to check.
	 * @return <code>true</code> if the given method is in the scope in the given system; <code>false</code>, otherwise.
	 */
	@Functional public boolean isInScope(@NonNull final SootMethod method, @NonNull final IEnvironment system) {
		final Iterator<MethodSpecification> _i = methodSpecs.iterator();
		final int _iEnd = methodSpecs.size();
		boolean _result;
		if (!classSpecs.isEmpty()) {
			_result = isInScope(method.getDeclaringClass(), system);
		} else {
			_result = false;
		}

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final MethodSpecification _ms = _i.next();
			_result |= _ms.isInScope(method, system);
		}
		return _result;
	}

	/**
	 * Sets the value of <code>classSpecs</code>.
	 * 
	 * @param theClassSpecs the new value of <code>classSpecs</code>.
	 */
	public void setClassSpecs(@NonNull final Collection<ClassSpecification> theClassSpecs) {
		this.classSpecs = theClassSpecs;
	}

	/**
	 * Sets the value of <code>fieldSpecs</code>.
	 * 
	 * @param theFieldSpecs the new value of <code>fieldSpecs</code>.
	 */
	public void setFieldSpecs(@NonNull final Collection<FieldSpecification> theFieldSpecs) {
		this.fieldSpecs = theFieldSpecs;
	}

	/**
	 * Sets the value of <code>methodSpecs</code>.
	 * 
	 * @param theMethodSpecs the new value of <code>methodSpecs</code>.
	 */
	public void setMethodSpecs(@NonNull final Collection<MethodSpecification> theMethodSpecs) {
		this.methodSpecs = theMethodSpecs;
	}

	/**
	 * Sets the value of <code>name</code>.
	 * 
	 * @param nameOfTheSpec the new value of <code>name</code>.
	 */
	public void setName(@NonNull final String nameOfTheSpec) {
		this.name = nameOfTheSpec;
	}

	/**
	 * Resets internal data structures.
	 */
	void reset() {
		classSpecs.clear();
		methodSpecs.clear();
		fieldSpecs.clear();
	}
}

// End of File
