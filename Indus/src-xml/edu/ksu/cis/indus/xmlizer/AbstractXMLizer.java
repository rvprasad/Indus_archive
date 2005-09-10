
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

package edu.ksu.cis.indus.xmlizer;

import edu.ksu.cis.indus.processing.ProcessingController;

import java.io.File;

import java.nio.ByteBuffer;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides basic infrastructure required to xmlize information in Indus.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractXMLizer
  implements IXMLizer {
	/** 
	 * The id used to retrieve the file name from the info map in <code>writeXML</code>.
	 */
	public static final Object FILE_NAME_ID = "FILE_NAME_ID";

	/** 
	 * The mask used to extract the lower nibble of a byte.
	 */
	private static final int LOWER_NIBBLE_MASK = 0x0f;

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXMLizer.class);

	/** 
	 * The hexadecimal digits!
	 */
	private static final char[] HEX_DIGITS =
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/** 
	 * The number of places to shift to get the higher nibble into the lower nibble position in a byte.
	 */
	private static final int NO_SHIFTS_FROM_HIGHER_TO_LOWER_NIBBLE = 4;

	/** 
	 * This is the id generator used during xmlization.
	 */
	private IJimpleIDGenerator idGenerator;

	/** 
	 * This is the directory in which the file containing the xmlized data will be placed.
	 */
	private String xmlOutDir;

	/**
	 * Set the xml id generator to be used in xml data generation.
	 *
	 * @param generator generates the id used in xml data.
	 *
	 * @pre generator != null
	 */
	public final void setGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * Retrieves the xml id generator used by this object.
	 *
	 * @return the xml id generator.
	 */
	public final IJimpleIDGenerator getIdGenerator() {
		return idGenerator;
	}

	/**
	 * Set the directory into which xml data should be dumped.
	 *
	 * @param xmlOutputDir is the directory into which xml data should be dumped.
	 *
	 * @throws IllegalArgumentException when the given directory does not exist or cannot be written into.
	 *
	 * @pre xmlOutputDir != null
	 */
	public final void setXmlOutputDir(final String xmlOutputDir) {
		final File _f = new File(xmlOutputDir);

		if (!_f.exists() | !_f.canWrite()) {
			LOGGER.error("XML output directory should exists with proper permissions.");
			throw new IllegalArgumentException("XML output directory should exists with proper permissions.");
		}
		xmlOutDir = xmlOutputDir;
	}

	/**
	 * Retrieves the directory into which xml data will be dumped into.
	 *
	 * @return the directory into which xml data will be dumped.
	 */
	public final String getXmlOutputDir() {
		return xmlOutDir;
	}

	/**
	 * Dumps the jimple into a file.
	 *
	 * @param name is the basis of the name of the files into which jimple should be dumped.
	 * @param dumpDir is the directory into which the files should be dumped.
	 * @param xmlcgipc is the processing controller to be used to control the dumping operation.  The user can use this
	 * 		  controller to control the methods and classes to be included in the dump.  This controller sholuld be able to
	 * 		  have deterministic behavior over a given set of class files.
	 *
	 * @pre name != null and xmlcgipc != null
	 */
	public final void dumpJimple(final String name, final String dumpDir, final ProcessingController xmlcgipc) {
		final JimpleXMLizer _t = new JimpleXMLizer(idGenerator);

		_t.setDumpOptions(dumpDir, getFileName(name).replaceAll("\\.xml$", ""));
		_t.hookup(xmlcgipc);
		xmlcgipc.process();
		_t.unhook(xmlcgipc);
	}

	/**
	 * Normalizes the given string into a form that is a valid xml identifier.
	 *
	 * @param string to be normalized.
	 *
	 * @return normalized string.
	 *
	 * @pre string != null
	 * @post result != null
	 */
	public static final String xmlizeString(final String string) {
		String _result = "";

		if (string != null) {
			final char[] _chars = string.toCharArray();

			// if the string doesn't have any of the magic characters, leave
			// it alone.
			final boolean _needsEncoding = needsEncoding(_chars);

			String _ret = string;

			if (_needsEncoding) {
				final StringBuffer _strBuf = new StringBuffer();

				for (int _i = 0; _i < _chars.length; _i++) {
					final int _printableCharCode = 127;

					switch (_chars[_i]) {
						case '&' :
							_strBuf.append("&amp;");
							break;

						case '\"' :
							_strBuf.append("&quot;");
							break;

						case '\'' :
							_strBuf.append("&apos;");
							break;

						case '<' :
							_strBuf.append("&lt;");
							break;

						case '\r' :
							_strBuf.append("&#xd;");
							break;

						case '>' :
							_strBuf.append("&gt;");
							break;

						default :

							if ((_chars[_i]) > _printableCharCode) {
								_strBuf.append("&#");
								_strBuf.append((int) _chars[_i]);
								_strBuf.append(";");
							} else {
								_strBuf.append(_chars[_i]);
							}
					}
				}

				_ret = _strBuf.toString();
			}
			_result = _ret.replaceAll(":", "_");
		}
		return _result;
	}

	/**
	 * Encodes the given string according to the specified encoding.
	 *
	 * @param string to be encoded.
	 * @param encoding to be used.
	 *
	 * @return the encoded string.
	 *
	 * @pre string != null and encoding != null
	 * @post result != null
	 */
	public static String encode(final String string, final String encoding) {
		final Charset _cs = Charset.forName(encoding);
		final ByteBuffer _bb = _cs.encode(string);
		final byte[] _bytes = _bb.array();
		final StringBuffer _sb = new StringBuffer("\\u");

		for (int _i = 0; _i < _bytes.length; _i++) {
			final byte _b = _bytes[_i];
			final char[] _chars =
				{
					HEX_DIGITS[(_b >> NO_SHIFTS_FROM_HIGHER_TO_LOWER_NIBBLE) & LOWER_NIBBLE_MASK],
					HEX_DIGITS[_b & LOWER_NIBBLE_MASK],
				};
			_sb.append(new String(_chars));
		}

		return _sb.toString();
	}

	/**
	 * Checks if the characters in the given array should be encoded.
	 *
	 * @param charArray which is checked if it requires encoding.
	 *
	 * @return <code>true</code> if there are characters in the array that should be encoded; <code>false</code>, otherwise.
	 *
	 * @pre charArray != null
	 */
	private static boolean needsEncoding(final char[] charArray) {
		boolean _needsEncoding = false;

search: 
		for (int _i = 0; _i < charArray.length; _i++) {
			switch (charArray[_i]) {
				case '&' :
				case '"' :
				case '\'' :
				case '<' :
				case '>' :
					_needsEncoding = true;
					break search;

				default :
					continue;
			}
		}
		return _needsEncoding;
	}
}

// End of File
