
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

import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.znerd.xmlenc.LineBreak;
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
	 * This creates an instance which encodes output in UTF-16 format.
	 *
	 * @see org.znerd.xmlenc.XMLOutputter#XMLOutputter(Writer)
	 */
	public CustomXMLOutputter(final Writer writer)
	  throws IllegalStateException, IllegalArgumentException, UnsupportedEncodingException {
		super(writer, "UTF-8");
		initialize();
	}

	/**
	 * @see org.znerd.xmlenc.XMLOutputter#XMLOutputter(Writer,String)
	 */
	public CustomXMLOutputter(final Writer writer, final String encoding)
	  throws IllegalStateException, IllegalArgumentException, UnsupportedEncodingException {
		super(writer, encoding);
		initialize();
	}

	/**
	 * Initializes this outputter's output properties.
	 */
	private void initialize() {
		setEscaping(true);
		setIndentation(DEFAULT_INDENTATION);
		setLineBreak(LineBreak.NONE);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/05/10 12:31:00  venku
   - a pretty printer should be used while viewing the xml doc rather
     than writing the XML doc in pretty format.  Fixed CustomXMLOutputter
     to output lean-mean XML document.
   Revision 1.2  2004/05/09 08:23:31  venku
   - generalized creation logic.
   Revision 1.1  2004/04/25 21:18:39  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
 */
