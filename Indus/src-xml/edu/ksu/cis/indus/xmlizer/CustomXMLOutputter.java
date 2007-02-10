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

package edu.ksu.cis.indus.xmlizer;

import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.znerd.xmlenc.XMLOutputter;

/**
 * This is a custom xml outputter class with preconfigured formatting infor.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CustomXMLOutputter
		extends XMLOutputter {

	/**
	 * This creates an instance which encodes output in UTF-8 format.
	 * 
	 * @param writer used to write the xml into.
	 * @throws IllegalStateException if getState() != XMLEventListenerStates.UNINITIALIZED && getState() !=
	 *             XMLEventListenerStates.AFTER_ROOT_ELEMENT && getState() != XMLEventListenerStates.ERROR_STATE.
	 * @throws IllegalArgumentException when out == null
	 * @throws UnsupportedEncodingException should not occur.
	 * @see org.znerd.xmlenc.XMLOutputter
	 * @pre writer != null
	 */
	public CustomXMLOutputter(final Writer writer) throws IllegalStateException, IllegalArgumentException,
			UnsupportedEncodingException {
		super(writer, "UTF-8");
		initialize();
	}

	/**
	 * @see org.znerd.xmlenc.XMLOutputter#XMLOutputter(Writer,String)
	 */
	public CustomXMLOutputter(final Writer writer, final String encoding) throws IllegalStateException,
			IllegalArgumentException, UnsupportedEncodingException {
		super(writer, encoding);
		initialize();
	}

	/**
	 * Initializes this outputter's output properties.
	 */
	private void initialize() {
		setEscaping(true);
	}
}

// End of File
