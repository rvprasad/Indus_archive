
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

package edu.ksu.cis.indus.tools.slicer.criteria.specification;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.slicer.ISliceCriterion;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;


/**
 * This class provides facility to serialize and deserialize slice criteria.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SliceCriteriaParser {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SliceCriteriaParser.class);

	/** 
	 * Indentation to be used during serialization.
	 */
	private static final int INDENT = 4;

	///CLOVER:OFF

	/**
	 * Creates a new SliceCriteriaParser object.
	 */
	@Empty private SliceCriteriaParser() {
		// Does nothing
	}

	///CLOVER:ON

	/**
	 * Deserializes the given criteria specification into criteria objects.
	 *
	 * @param criteriaSpec is the criteria specification in serialized form.
	 * @param scene relative on which the deserialization occurs.
	 *
	 * @return a collection of criteria.
	 *
	 * @throws JiBXException when either JiBX cannot be setup or error occurs during deserialization.
	 *
	 * @pre criteriaSpec != null and scene != null
	 * @post result != null
	 */
	public static Collection<ISliceCriterion> deserialize(final String criteriaSpec, final Scene scene)
	  throws JiBXException {
		final Collection<ISliceCriterion>_result;
		IBindingFactory _bindingFactory;
		IUnmarshallingContext _unmarshallingContext;

		try {
			_bindingFactory = BindingDirectory.getFactory(SliceCriteriaSpec.class);
			_unmarshallingContext = _bindingFactory.createUnmarshallingContext();
		} catch (final JiBXException _e) {
			LOGGER.error("Error while setting up JiBX.  Aborting.", _e);
			throw _e;
		}

		try {
			final StringReader _reader = new StringReader(criteriaSpec);
			final SliceCriteriaSpec _sliceCriteriaSpec =
				(SliceCriteriaSpec) _unmarshallingContext.unmarshalDocument(_reader, null);
			_result = _sliceCriteriaSpec.getCriteria(scene);
		} catch (final JiBXException _e) {
			LOGGER.error("Error while deserializing slice criteria.", _e);
			throw _e;
		}

		return _result;
	}

	/**
	 * Serializes the given criteria into criteria specification.
	 *
	 * @param criteria is the criteria to be serialized.
	 *
	 * @return the criteria specification.
	 *
	 * @throws JiBXException when either JiBX cannot be setup or error occurs during serialization.
	 *
	 * @pre criteria != null
	 * @pre criteria.oclIsKindOf(ISliceCriterion)
	 * @post result != null
	 */
	public static String serialize(final Collection<ISliceCriterion> criteria)
	  throws JiBXException {
		final String _result;
		IBindingFactory _bindingFactory;
		IMarshallingContext _marshallingContext;

		try {
			_bindingFactory = BindingDirectory.getFactory(SliceCriteriaSpec.class);
			_marshallingContext = _bindingFactory.createMarshallingContext();
			_marshallingContext.setIndent(INDENT);
		} catch (final JiBXException _e) {
			LOGGER.error("Error while setting up JiBX.", _e);
			throw _e;
		}

		try {
			final StringWriter _writer = new StringWriter();
			_marshallingContext.marshalDocument(SliceCriteriaSpec.getSliceCriteriaSpec(criteria), "UTF-8", Boolean.TRUE,
				_writer);
			_writer.flush();
			_result = _writer.toString();
		} catch (final JiBXException _e) {
			LOGGER.error("Error while marshalling Slicer configuration.", _e);
			throw _e;
		}
		return _result;
	}
}

// End of File
