
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

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;


/**
 * This class represents scope definition. It can be used to filter classes, methods, and fields based on names  and
 * hierarchical relation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SpecificationBasedScopeDefinition {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SpecificationBasedScopeDefinition.class);

	/** 
	 * Indentation space during serialization.
	 */
	private static final int INDENT = 4;

	/** 
	 * The collection of class-level specification.
	 *
	 * @invariant classSpecs.oclIsKindOf(Collection(ClassSpecification))
	 */
	private Collection classSpecs;

	/** 
	 * The collection of field-level specification.
	 *
	 * @invariant classSpecs.oclIsKindOf(Collection(FieldSpecification))
	 */
	private Collection fieldSpecs;

	/** 
	 * The collection of method-level specification.
	 *
	 * @invariant classSpecs.oclIsKindOf(Collection(MethodSpecification))
	 */
	private Collection methodSpecs;

	/** 
	 * The name of this specification.
	 */
	private String name;

	/**
	 * Sets the value of <code>classSpecs</code>.
	 *
	 * @param theClassSpecs the new value of <code>classSpecs</code>.
	 */
	public void setClassSpecs(final Collection theClassSpecs) {
		this.classSpecs = theClassSpecs;
	}

	/**
	 * Sets the value of <code>fieldSpecs</code>.
	 *
	 * @param theFieldSpecs the new value of <code>fieldSpecs</code>.
	 */
	public void setFieldSpecs(final Collection theFieldSpecs) {
		this.fieldSpecs = theFieldSpecs;
	}

	/**
	 * Retrieves the value in <code>fieldSpecs</code>.
	 *
	 * @return the value in <code>fieldSpecs</code>.
	 */
	public Collection getFieldSpecs() {
		return fieldSpecs;
	}

	/**
	 * Checks if the given class is in the scope in the given system.
	 *
	 * @param clazz to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the given class is in the scope in the given system; <code>false</code>, otherwise.
	 *
	 * @pre clazz != null and system != null
	 */
	public boolean isInScope(final SootClass clazz, final IEnvironment system) {
		final Iterator _i = classSpecs.iterator();
		final int _iEnd = classSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final ClassSpecification _cs = (ClassSpecification) _i.next();
			_result |= _cs.isInScope(clazz, system);
		}
		return _result;
	}

	/**
	 * Checks if the given method is in the scope in the given system.
	 *
	 * @param method to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the given method is in the scope in the given system; <code>false</code>, otherwise.
	 *
	 * @pre method != null and system != null
	 */
	public boolean isInScope(final SootMethod method, final IEnvironment system) {
		final Iterator _i = methodSpecs.iterator();
		final int _iEnd = methodSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final MethodSpecification _ms = (MethodSpecification) _i.next();
			_result |= _ms.isInScope(method, system);
		}
		return _result;
	}

	/**
	 * Checks if the given field is in the scope in the given system.
	 *
	 * @param field to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the given field is in the scope in the given system; <code>false</code>, otherwise.
	 *
	 * @pre field != null and system != null
	 */
	public boolean isInScope(final SootField field, final IEnvironment system) {
		final Iterator _i = fieldSpecs.iterator();
		final int _iEnd = fieldSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final FieldSpecification _fs = (FieldSpecification) _i.next();
			_result |= _fs.isInScope(field, system);
		}
		return _result;
	}

	/**
	 * Deserializes the given content into a scope definition.
	 *
	 * @param contents to be deserialized.
	 *
	 * @return a scope definition.
	 *
	 * @throws JiBXException when the content cannot be deserialized due to IO exceptions or malformed contents.
	 *
	 * @pre contents != null
	 */
	public static SpecificationBasedScopeDefinition deserialize(final String contents)
	  throws JiBXException {
		final SpecificationBasedScopeDefinition _result;
		IUnmarshallingContext _unmarshallingContext;

		try {
			final IBindingFactory _bindingFactory = BindingDirectory.getFactory(SpecificationBasedScopeDefinition.class);
			_unmarshallingContext = _bindingFactory.createUnmarshallingContext();
		} catch (final JiBXException _e) {
			LOGGER.fatal("Error while setting up JiBX.  Aborting.", _e);
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
	 *
	 * @return the serialized form the scope definition.
	 *
	 * @throws JiBXException when the content cannot be serialized.
	 *
	 * @pre scopeDef != null
	 */
	public static String serialize(final SpecificationBasedScopeDefinition scopeDef)
	  throws JiBXException {
		final String _result;
		IMarshallingContext _marshallingContext;

		try {
			final IBindingFactory _bindingFactory = BindingDirectory.getFactory(SpecificationBasedScopeDefinition.class);
			_marshallingContext = _bindingFactory.createMarshallingContext();
			_marshallingContext.setIndent(INDENT);
		} catch (final JiBXException _e) {
			LOGGER.fatal("Error while setting up JiBX.", _e);
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
	 * Retrieves the value in <code>classSpecs</code>.
	 *
	 * @return the value in <code>classSpecs</code>.
	 */
	public Collection getClassSpecs() {
		return classSpecs;
	}

	/**
	 * Sets the value of <code>methodSpecs</code>.
	 *
	 * @param theMethodSpecs the new value of <code>methodSpecs</code>.
	 */
	public void setMethodSpecs(final Collection theMethodSpecs) {
		this.methodSpecs = theMethodSpecs;
	}

	/**
	 * Retrieves the value in <code>methodSpecs</code>.
	 *
	 * @return the value in <code>methodSpecs</code>.
	 */
	public Collection getMethodSpecs() {
		return methodSpecs;
	}

	/**
	 * Sets the value of <code>name</code>.
	 *
	 * @param nameOfTheSpec the new value of <code>name</code>.
	 */
	public void setName(final String nameOfTheSpec) {
		this.name = nameOfTheSpec;
	}

	/**
	 * Retrieves the value in <code>name</code>.
	 *
	 * @return the value in <code>name</code>.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Creates the container for specifications. This is used by java-xml binding.
	 *
	 * @return a container.
	 *
	 * @post result != null
	 */
	static Collection createSpecContainer() {
		return new ArrayList();
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
