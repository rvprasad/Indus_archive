
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
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.Scene;
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
	 * The stream into which the xmlized data is written into.
	 */
	private Writer xmlizedSystem;

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
	 * Sets the writer/stream to be used by this xmlizer.  This is the stream into which the xml data is written into.
	 *
	 * @param writer used to write the xml data into a stream.
	 *
	 * @pre writer != null
	 */
	public final void setWriter(final Writer writer) {
		xmlizedSystem = writer;
		stmtXmlizer.setWriter(writer);
	}

	/**
	 * The entry point to execute this xmlizer from command prompt.
	 *
	 * @param s is the command-line arguments.
	 *
	 * @pre s != null
	 */
	public static void main(final String[] s) {
		final JimpleXMLizer _xmlizer = new JimpleXMLizer(new UniqueJimpleIDGenerator());
		final ProcessingController _pc = new ProcessingController();
		final Scene _scene = Scene.v();
		final Environment _env = new Environment(_scene);
		_pc.setEnvironment(_env);
		_pc.setProcessingFilter(new XMLizingProcessingFilter());

		for (int _i = 0; _i < s.length; _i++) {
			_scene.loadClassAndSupport(s[_i]);
		}

		final Writer _writer = new BufferedWriter(new OutputStreamWriter(System.out));
		_xmlizer.setWriter(_writer);
		_xmlizer.hookup(_pc);
		_pc.process();
		_xmlizer.unhook(_pc);

		try {
			_writer.flush();
			_writer.close();
		} catch (IOException _e) {
			_e.printStackTrace();
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
				xmlizedSystem.write("\t\t</method>\n");
			} else {
				processingMethod = true;
			}
			xmlizedSystem.write("\t\t<method name=\"" + method.getName().replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;")
				+ "\" id=\"" + idGenerator.getIdForMethod(method) + "\"\n");

			// capture info about modifiers
			xmlizedSystem.write("\t\t  abstract=\"" + method.isAbstract() + "\"\n");
			xmlizedSystem.write("\t\t  static=\"" + method.isStatic() + "\"\n");
			xmlizedSystem.write("\t\t  native=\"" + method.isNative() + "\"\n");
			xmlizedSystem.write("\t\t  synchronized=\"" + method.isSynchronized() + "\"\n");

			xmlizedSystem.write("\t\t  accessSpec=\"" + getAccessSpecifier(method.getModifiers()) + "\">\n");

			// capture info about signature
			xmlizedSystem.write("\t\t\t<signature>\n");

			final String _indent = "\t\t\t\t";
			xmlizedSystem.write(_indent + "<returnType typeId=\"" + idGenerator.getIdForType(method.getReturnType())
				+ "\"/>\n");

			int _j = 0;

			for (final Iterator _i = method.getParameterTypes().iterator(); _i.hasNext();) {
				xmlizedSystem.write(_indent + "<paramType typeId=\"" + idGenerator.getIdForType((Type) _i.next())
					+ "\" position=\"" + _j++ + "\"/>\n");
			}

			for (final Iterator _i = method.getExceptions().iterator(); _i.hasNext();) {
				xmlizedSystem.write(_indent + "<exception typeId=\"" + idGenerator.getIdForClass((SootClass) _i.next())
					+ "\"/>\n");
			}
			xmlizedSystem.write("\t\t\t</signature>\n");

			if (method.isConcrete()) {
				final Body _body = method.retrieveActiveBody();

				final Chain _traps = _body.getTraps();

				// capture info about traps
				if (!_traps.isEmpty()) {
					xmlizedSystem.write("\t\t\t<traplist>\n");

					for (final Iterator _i = _traps.iterator(); _i.hasNext();) {
						final Trap _trap = (Trap) _i.next();
						xmlizedSystem.write("\t\t\t\t<trap typeId=\"" + idGenerator.getIdForClass(_trap.getException())
							+ "\" beginId=\"" + idGenerator.getIdForStmt((Stmt) _trap.getBeginUnit(), method) + "\" endId=\""
							+ idGenerator.getIdForStmt((Stmt) _trap.getEndUnit(), method) + "\" handlerId=\""
							+ idGenerator.getIdForStmt((Stmt) _trap.getHandlerUnit(), method) + "\"/>\n");
					}
					xmlizedSystem.write("\t\t\t</traplist>\n");
				}

				// capture info about locals
				for (final Iterator _i = _body.getLocals().iterator(); _i.hasNext();) {
					final Local _l = (Local) _i.next();
					xmlizedSystem.write("\t\t\t<local id=\"" + idGenerator.getIdForLocal(_l, method) + "\" name=\""
						+ _l.getName() + "\" typeId=\"" + idGenerator.getIdForType(_l.getType()) + "\"/>\n");
				}
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
			if (processingMethod) {
				xmlizedSystem.write("\t\t</method>\n");
			}

			if (processingClass) {
				xmlizedSystem.write("\t</" + currType + ">\n");
			} else {
				processingClass = true;
			}

			if (clazz.isInterface()) {
				currType = "interface";
			} else {
				currType = "class";
			}
			xmlizedSystem.write("\t<" + currType + " name=\"" + clazz.getJavaStyleName() + "\" id=\""
				+ idGenerator.getIdForClass(clazz) + "\" package=\"" + clazz.getJavaPackageName() + "\"\n");

			xmlizedSystem.write("\t  abstract=\"" + clazz.isAbstract() + "\"\n");

			xmlizedSystem.write("\t  accessSpec=\"" + getAccessSpecifier(clazz.getModifiers()) + "\">\n");

			if (clazz.hasSuperclass()) {
				final SootClass _sc = clazz.getSuperclass();
				xmlizedSystem.write("\t\t<superclass typeId=\"" + idGenerator.getIdForClass(_sc) + "\"/>\n");
			}

			if (clazz.getInterfaceCount() > 0) {
				xmlizedSystem.write("\t\t<interfaceList>\n");

				for (final Iterator _i = clazz.getInterfaces().iterator(); _i.hasNext();) {
					final SootClass _inter = (SootClass) _i.next();
					xmlizedSystem.write("\t\t\t<superinterface typeId=\"" + idGenerator.getIdForClass(_inter) + "\"/>\n");
				}
				xmlizedSystem.write("\t\t</interfaceList>\n");
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
			xmlizedSystem.write("\t\t<field name=\"" + field.getName() + "\" id=\"" + idGenerator.getIdForField(field)
				+ "\" typeId=\"" + idGenerator.getIdForType(field.getType()) + "\"\n");
			xmlizedSystem.write("\t\t  static=\"" + field.isStatic() + "\"\n");
			xmlizedSystem.write("\t\t  final=\"" + field.isFinal() + "\"\n");

			String _accessSpec = "";

			if (field.isPublic()) {
				_accessSpec = "public";
			} else if (field.isPrivate()) {
				_accessSpec = "private";
			} else if (field.isProtected()) {
				_accessSpec = "proctected";
			}
			xmlizedSystem.write("\t\t  accessSpec=\"" + _accessSpec + "\"/>\n");
		} catch (IOException _e) {
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
			if (processingMethod) {
				xmlizedSystem.write("\t\t</method>\n");
			}

			if (processingClass) {
				xmlizedSystem.write("\t</" + currType + ">\n");
			}
			xmlizedSystem.write("</jimple>\n");
		} catch (IOException _e) {
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
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public final void processingBegins() {
		try {
			xmlizedSystem.write("<!DOCTYPE jimple PUBLIC \"-//ANT//DTD project//EN\" \"jimple.dtd\">\n");
			xmlizedSystem.write("<jimple>\n");
		} catch (IOException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", _e);
			}
		}
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
}

/*
   ChangeLog:
   $Log$
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
