
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

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.znerd.xmlenc.XMLOutputter;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;

import soot.jimple.Stmt;

import soot.util.Chain;


/**
 * This class can be used to xmlize a system represented as Jimple.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class JimpleXMLizer
  extends AbstractProcessor {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(JimpleXMLizer.class);

	/**
	 * The id generator used during xmlization of the jimple.
	 *
	 * @invariant idGenerator != null
	 */
	private final IJimpleIDGenerator idGenerator;

	/**
	 * The Jimple statement xmlizer.
	 *
	 * @invariant stmtXmlizer != null
	 */
	private final JimpleStmtXMLizer stmtXmlizer;

	/**
	 * This indicates if the type that is being processed at present or just befor this point in time is a class or an
	 * interface.
	 */
	private String currType;

	/**
	 * The directory in which xmlized jimple will be dumped.  If <code>null</code>, it will be redirected to standard output.
	 */
	private String dumpDirectory;

	/**
	 * The suffix to be added to jimple files when they dumped.
	 */
	private String fileSuffix;

	/**
	 * The outputter to be used to write xml data.
	 */
	private XMLOutputter writer;

	/**
	 * This indicates if the processing of a class has begun.  This is  set in the callback for a class.
	 */
	private boolean processingClass;

	/**
	 * This indicates if the processing of a method has begun.  This is  set in the callback for a method.
	 */
	private boolean processingMethod;

	/**
	 * Creates a new JimpleXMLizer object.
	 *
	 * @param generator used to generate ids of xml elements.
	 *
	 * @pre generator != null
	 */
	public JimpleXMLizer(final IJimpleIDGenerator generator) {
		idGenerator = generator;
		stmtXmlizer = new JimpleStmtXMLizer(new JimpleValueXMLizer(generator), generator);
	}

	/**
	 * Sets the options to xmlize jimple.
	 *
	 * @param directory into which xmlized jimple will be dumped.  If the given non-<code>null</code> directory does not
	 * 		  exist then it will be created.  In case of <code>null</code> directory, it will dump the output into
	 * 		  <code>System.out</code>.
	 * @param suffix is appended to the generated file.  If <code>null</code>, no suffix is appended.
	 */
	public final void setDumpOptions(final String directory, final String suffix) {
		dumpDirectory = directory;

		if (directory != null) {
			final File _dir = new File(directory);

			if (!_dir.exists()) {
				_dir.mkdirs();
			}
		}

		if (suffix == null) {
			fileSuffix = "";
		} else {
			fileSuffix = "_" + suffix;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(final Stmt stmt, final Context context) {
		stmtXmlizer.setMethod(context.getCurrentMethod());
		stmtXmlizer.apply(stmt);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public final void callback(final SootMethod method) {
		try {
			if (processingMethod) {
				writer.endTag();
			} else {
				processingMethod = true;
			}
			writer.startTag("method");
			writer.attribute("name", AbstractXMLizer.xmlizeString(method.getName()));
			writer.attribute("id", idGenerator.getIdForMethod(method));

			// capture info about modifiers
			writer.attribute("abstract", String.valueOf(method.isAbstract()));
			writer.attribute("static", String.valueOf(method.isStatic()));
			writer.attribute("native", String.valueOf(method.isNative()));
			writer.attribute("synchronized", String.valueOf(method.isSynchronized()));

			writer.attribute("accessSpec", getAccessSpecifier(method.getModifiers()));

			// capture info about signature
			writer.startTag("signature");
			writer.startTag("returnType");
			writer.attribute("typeId", idGenerator.getIdForType(method.getReturnType()));
			writer.endTag();

			int _j = 0;

			for (final Iterator _i = method.getParameterTypes().iterator(); _i.hasNext();) {
				writer.startTag("paramType");
				writer.attribute("typeId", idGenerator.getIdForType((Type) _i.next()));
				writer.attribute("position", String.valueOf(_j++));
				writer.endTag();
			}

			for (final Iterator _i = method.getExceptions().iterator(); _i.hasNext();) {
				writer.startTag("exception");
				writer.attribute("typeId", idGenerator.getIdForClass((SootClass) _i.next()));
				writer.endTag();
			}
			writer.endTag();

			if (method.isConcrete()) {
				xmlizeTrapListAndLocals(method);
			}
		} catch (IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public final void callback(final SootClass clazz) {
		try {
			if (processingClass) {
				writer.endDocument();
			} else {
				processingClass = true;
			}

			// Create new file
			final String _classId = setupFile(clazz);

			if (clazz.isInterface()) {
				currType = "interface";
			} else {
				currType = "class";
			}
			writer.startTag(currType);
			writer.attribute("name", clazz.getJavaStyleName());
			writer.attribute("id", _classId);
			writer.attribute("package", clazz.getJavaPackageName());
			writer.attribute("abstract", String.valueOf(clazz.isAbstract()));
			writer.attribute("accessSpec", getAccessSpecifier(clazz.getModifiers()));

			if (clazz.hasSuperclass()) {
				final SootClass _sc = clazz.getSuperclass();
				writer.startTag("superclass");
				writer.attribute("typeId", idGenerator.getIdForClass(_sc));
				writer.endTag();
			}

			if (clazz.getInterfaceCount() > 0) {
				writer.startTag("interfaceList");

				for (final Iterator _i = clazz.getInterfaces().iterator(); _i.hasNext();) {
					final SootClass _inter = (SootClass) _i.next();
					writer.startTag("superinterface");
					writer.attribute("typeId", idGenerator.getIdForClass(_inter));
					writer.endTag();
				}
				writer.endTag();
			}

			processingMethod = false;
		} catch (IOException _e) {
			LOGGER.warn("Error while writing xmlized jimple info.", _e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public final void callback(final SootField field) {
		try {
			writer.startTag("field");
			writer.attribute("name", field.getName());
			writer.attribute("id", idGenerator.getIdForField(field));
			writer.attribute("typeId", idGenerator.getIdForType(field.getType()));
			writer.attribute("static", String.valueOf(field.isStatic()));
			writer.attribute("final", String.valueOf(field.isFinal()));

			String _accessSpec = "";

			if (field.isPublic()) {
				_accessSpec = "public";
			} else if (field.isPrivate()) {
				_accessSpec = "private";
			} else if (field.isProtected()) {
				_accessSpec = "proctected";
			}
			writer.attribute("accessSpec", _accessSpec);
			writer.endTag();
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public final void consolidate() {
		try {
			writer.endDocument();
			writer.getWriter().close();
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", _e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void hookup(final ProcessingController ppc) {
		ppc.registerForAllStmts(this);
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);
		ppc.unregister(this);
	}

	/**
	 * Retrieves the string that represents the access specifier in the given modifers.
	 *
	 * @param modifiers from which the access specifier should be extracted as a string.
	 *
	 * @return the stringized form of the access specifier.
	 *
	 * @post result != null
	 */
	private String getAccessSpecifier(final int modifiers) {
		String _result = "";

		if (Modifier.isPublic(modifiers)) {
			_result = "public";
		} else if (Modifier.isPrivate(modifiers)) {
			_result = "private";
		} else if (Modifier.isProtected(modifiers)) {
			_result = "proctected";
		}
		return _result;
	}

	/**
	 * Sets up the file to write the xmlized jimple for the given class.
	 *
	 * @param clazz to be xmlized.
	 *
	 * @return the id of the class.
	 *
	 * @throws IOException when there is an error while operating on the file corresponding to the class.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	private String setupFile(final SootClass clazz)
	  throws IOException {
		final String _classId = idGenerator.getIdForClass(clazz);

		if (dumpDirectory == null) {
			if (writer == null) {
				writer = new CustomXMLOutputter(new BufferedWriter(new OutputStreamWriter(System.out)));
			} else {
				writer.reset(writer.getWriter(), writer.getEncoding());
			}
			stmtXmlizer.setWriter(writer);
		} else {
			final String _filename = dumpDirectory + File.separator + _classId + fileSuffix + ".xml";

			try {
				if (writer != null && writer.getWriter() != null) {
					writer.getWriter().close();
				}

				final File _file = new File(_filename);
				writer = new CustomXMLOutputter(new BufferedWriter(new FileWriter(_file)));
				stmtXmlizer.setWriter(writer);
				writer.declaration();
			} catch (final IOException _e) {
				LOGGER.error("Exception while trying to open " + _filename, _e);
				throw _e;
			}
		}
		return _classId;
	}

	/**
	 * Xmlizes traps and locals in the given method.
	 *
	 * @param method in which to xmlize.
	 *
	 * @pre method != null
	 */
	private void xmlizeTrapListAndLocals(final SootMethod method) {
		final Body _body = method.retrieveActiveBody();

		final Chain _traps = _body.getTraps();

		try {
			// capture info about traps
			if (!_traps.isEmpty()) {
				writer.startTag("traplist");

				for (final Iterator _i = _traps.iterator(); _i.hasNext();) {
					final Trap _trap = (Trap) _i.next();
					writer.startTag("trap");
					writer.attribute("typeId", idGenerator.getIdForClass(_trap.getException()));
					writer.attribute("beginId", idGenerator.getIdForStmt((Stmt) _trap.getBeginUnit(), method));
					writer.attribute("endId", idGenerator.getIdForStmt((Stmt) _trap.getEndUnit(), method));
					writer.attribute("handlerId", idGenerator.getIdForStmt((Stmt) _trap.getHandlerUnit(), method));
					writer.endTag();
				}
				writer.endTag();
			}

			// capture info about locals
			for (final Iterator _i = _body.getLocals().iterator(); _i.hasNext();) {
				final Local _l = (Local) _i.next();
				writer.startTag("local");
				writer.attribute("id", idGenerator.getIdForLocal(_l, method));
				writer.attribute("name", _l.getName());
				writer.attribute("typeId", idGenerator.getIdForType(_l.getType()));
				writer.endTag();
			}
		} catch (final IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", _e);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.41  2004/05/13 03:12:33  venku
   - CustomXMLOutputter defaults to UTF-8 encoding.
   - Added a new method to AbstractXMLizer to encode strings.
   - Strings are encoded before writing them as CDATA in JimpleValueXMLizer.
   - ripple effect.

   Revision 1.40  2004/05/13 01:14:46  venku
   - it should be synchronized.

   Revision 1.39  2004/05/13 01:14:21  venku
   - added declaration and dtd content to all xml documents.
   - removed redundant value element, the child of string constant.

   Revision 1.38  2004/05/09 09:59:25  venku
   - logical error. FIXED.

   Revision 1.37  2004/05/09 09:28:18  venku
   - documentation.

   Revision 1.36  2004/05/09 08:24:08  venku
   - all xmlizers use xmlenc to write xml data.
   Revision 1.35  2004/05/06 09:31:01  venku
   - used xmlenc library to write xml instead of manual tag generation.
   Revision 1.34  2004/04/25 23:18:21  venku
   - coding conventions.
   Revision 1.33  2004/04/25 21:18:39  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
   Revision 1.32  2004/04/22 23:32:31  venku
   - xml file name were setup incorrectly.  FIXED.
   Revision 1.31  2004/04/22 22:12:09  venku
   - made changes to jimple xmlizer to dump each class into a separate file.
   - ripple effect.
   Revision 1.30  2004/04/20 06:53:18  venku
   - documentation.
   Revision 1.29  2004/04/18 00:02:18  venku
   - added support to dump jimple.xml while testing.
   Revision 1.28  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.27  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.26  2003/12/09 09:50:46  venku
   - amended output of string output to be XML compliant.
     This means some characters that are unrepresentable in
     XML are omitted.
   Revision 1.25  2003/12/02 11:36:16  venku
   - coding convention.
   Revision 1.24  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.23  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
   Revision 1.22  2003/11/30 02:12:41  venku
   - root element specification of the DOCTYPE changed.
   Revision 1.21  2003/11/30 01:17:11  venku
   - renamed CGBasedXMLizingFilter to CGBasedXMLizingProcessingFilter.
   - renamed XMLizingController to XMLizingProcessingFilter.
   - ripple effect.
   Revision 1.20  2003/11/30 00:10:17  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.19  2003/11/28 09:40:38  venku
   - the way information is captured is changed.
      Revision 1.18  2003/11/26 18:26:08  venku
      - capture a whole lot more information for classes and methods.
      - removed unnecessary info from the attributes.
      Revision 1.17  2003/11/25 19:10:08  venku
      - added type attribute to local variables.
      Revision 1.16  2003/11/24 06:45:23  venku
      - corrected xml encoding errors along with tag name emission errors.
      Revision 1.15  2003/11/24 06:27:54  venku
      - static invoke expr is also routed through writeInvokeExpr().
      Revision 1.14  2003/11/24 01:20:27  venku
      - enhanced output formatting.
      Revision 1.13  2003/11/24 00:54:03  venku
      - deleted  getstream() method as it was not used.
      Revision 1.12  2003/11/17 15:57:03  venku
      - removed support to retrieve new statement ids.
      - added support to retrieve id for value boxes.
      Revision 1.11  2003/11/16 18:37:42  venku
      - renamed UniqueIDGenerator to UniqueJimpleIDGenerator.
      Revision 1.10  2003/11/12 04:47:12  venku
      - < needed to be escaped. FIXED.
      Revision 1.9  2003/11/12 04:40:06  venku
      - emitted wrong tag for method and classes. FIXED.
      Revision 1.8  2003/11/10 07:52:58  venku
      - beginning tag for xmlized jimple element was missing. FIXED.
      Revision 1.7  2003/11/10 07:49:22  venku
      - documentation.
      Revision 1.6  2003/11/10 03:29:51  venku
      - logged exceptions.
      Revision 1.5  2003/11/10 03:13:04  venku
      - uses abstract implementation of IProcessor.
      Revision 1.4  2003/11/10 03:04:17  venku
      - method and class elements were closed incorrectly. FIXED.
      Revision 1.3  2003/11/10 02:42:00  venku
      - uses register/unregister for all statements.
      Revision 1.2  2003/11/07 11:14:44  venku
      - Added generator class for xmlizing purpose.
      - XMLizing of Jimple works, but takes long.
        Probably, reachable method dump should fix it.  Another rainy day problem.
      Revision 1.1  2003/11/07 06:27:03  venku
      - Made the XMLizer classes concrete by moving out the
        id generation logic outside.
      - Added an interface which provides the id required for
        xmlizing Jimple.
      Revision 1.1  2003/11/06 10:01:25  venku
      - created support for xmlizing Jimple in a customizable manner.
 */
