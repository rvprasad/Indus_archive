
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

package edu.ksu.cis.indus.xmlizer;

import edu.ksu.cis.indus.processing.ProcessingController;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;


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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractXMLizer.class);

	/**
	 * This is the id generator used during xmlization.
	 */
	private IJimpleIDGenerator idGenerator;

	/**
	 * This is the directory in which the file containing the xmlized data will be placed.
	 */
	private String xmlOutDir;

	/**
	 * This is a custom xml outputter class with preconfigured formatting infor.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected final class CustomXMLOutputter
	  extends XMLOutputter {
		/**
		 * @see org.znerd.xmlenc.XMLOutputter#XMLOutputter(Writer,String)
		 */
		public CustomXMLOutputter(final Writer writer, final String encoding)
		  throws IllegalStateException, IllegalArgumentException, UnsupportedEncodingException {
			super(writer, encoding);
			setEscaping(true);
			setIndentation("  ");
			setLineBreak(LineBreak.UNIX);
		}
	}

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
	 * Retrieves the a name based on the given name for the file into which the xml data will be written into.
	 *
	 * @param name to be considered when coming up with the file name.
	 *
	 * @return the name of the file.
	 *
	 * @pre name != null
	 * @post result != null
	 */
	public abstract String getFileName(final String name);

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

/*
   ChangeLog:
   $Log$
   Revision 1.19  2004/04/22 23:02:49  venku
   - moved writeXML into IXMLizer.

   Revision 1.18  2004/04/22 22:12:09  venku
   - made changes to jimple xmlizer to dump each class into a separate file.
   - ripple effect.

   Revision 1.17  2004/04/18 08:59:02  venku
   - enabled test support for slicer.
   Revision 1.16  2004/04/01 22:34:19  venku
   - changed xmlization logic.
   Revision 1.15  2004/03/26 07:15:49  venku
   - documentation.
   Revision 1.14  2004/03/05 11:59:40  venku
   - documentation.
   Revision 1.13  2004/02/14 23:16:49  venku
   - coding convention.
   Revision 1.12  2004/02/11 10:00:20  venku
   - added a new custom xml outputter class.
   Revision 1.11  2004/02/11 09:37:21  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.
   Revision 1.10  2004/02/09 17:40:57  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
   Revision 1.9  2004/02/09 06:49:05  venku
   - deleted dependency xmlization and test classes.
   Revision 1.8  2004/02/09 04:39:40  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.7  2004/02/09 02:00:11  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.6  2003/12/28 00:41:48  venku
   - logging.
   Revision 1.5  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.4  2003/12/09 10:20:49  venku
   - formatting.
   Revision 1.3  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.2  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.1  2003/12/08 11:59:44  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizer inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.
 */
