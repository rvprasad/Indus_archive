
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

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;

import soot.jimple.Stmt;

import soot.util.Chain;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.ProcessingController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Iterator;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IJimpleIDGenerator idGenerator;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final StmtXMLizer stmtXmlizer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String currType;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Writer xmlizedSystem;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingClass = false;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean processingMethod = false;

	/**
	 * Creates a new JimpleXMLizer object.
	 *
	 * @param generator DOCUMENT ME!
	 */
	public JimpleXMLizer(final IJimpleIDGenerator generator) {
		idGenerator = generator;
		stmtXmlizer = new StmtXMLizer(new ValueXMLizer(generator), generator);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param writer DOCUMENT ME!
	 */
	public void setWriter(final Writer writer) {
		xmlizedSystem = writer;
		stmtXmlizer.setWriter(writer);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param s DOCUMENT ME!
	 */
	public static void main(String[] s) {
		JimpleXMLizer xmlizer = new JimpleXMLizer(new UniqueJimpleIDGenerator());
		ProcessingController pc = new ProcessingController();
		Scene scene = Scene.v();
		Environment env = new Environment(scene);
		pc.setEnvironment(env);
		pc.setProcessingFilter(new XMLizingProcessingFilter());

		for (int i = 0; i < s.length; i++) {
			scene.loadClassAndSupport(s[i]);
		}

		Writer writer = new OutputStreamWriter(System.out);
		xmlizer.setWriter(writer);
		xmlizer.hookup(pc);
		pc.process();
		xmlizer.unhook(pc);

		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(Stmt stmt, Context context) {
		stmtXmlizer.setMethod(context.getCurrentMethod());
		stmtXmlizer.apply(stmt);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public final void callback(SootMethod method) {
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

			String accessSpec = "";

			if (method.isPublic()) {
				accessSpec = "public";
			} else if (method.isPrivate()) {
				accessSpec = "private";
			} else if (method.isProtected()) {
				accessSpec = "proctected";
			}
			xmlizedSystem.write("\t\t  accessSpec=\"" + accessSpec + "\">\n");

			// capture info about signature
			xmlizedSystem.write("\t\t\t<signature>\n");
			xmlizedSystem.write("\t\t\t\t<returnType typeId=\"" + idGenerator.getIdForType(method.getReturnType()) + "\"/>\n");

			if (method.getParameterCount() > 0) {
				int j = 0;

				for (Iterator i = method.getParameterTypes().iterator(); i.hasNext();) {
					xmlizedSystem.write("\t\t\t\t<paramType typeId=\"" + idGenerator.getIdForType((Type) i.next())
						+ "\" position=\"" + j++ + "\"/>\n");
				}
			}

			if (method.getExceptions().size() > 0) {
				for (Iterator i = method.getExceptions().iterator(); i.hasNext();) {
					xmlizedSystem.write("\t\t\t\t<exception typeId=\"" + idGenerator.getIdForClass((SootClass) i.next())
						+ "\"/>\n");
				}
			}
			xmlizedSystem.write("\t\t\t</signature>\n");

			if (method.isConcrete()) {
				Body body = method.retrieveActiveBody();

				Chain traps = body.getTraps();

				// capture info about traps
				if (!traps.isEmpty()) {
					xmlizedSystem.write("\t\t\t<traplist>\n");

					for (Iterator i = traps.iterator(); i.hasNext();) {
						Trap trap = (Trap) i.next();
						xmlizedSystem.write("\t\t\t\t<trap typeId=\"" + idGenerator.getIdForClass(trap.getException())
							+ "\" beginId=\"" + idGenerator.getIdForStmt((Stmt) trap.getBeginUnit(), method) + "\" endId=\""
							+ idGenerator.getIdForStmt((Stmt) trap.getEndUnit(), method) + "\" handlerId=\""
							+ idGenerator.getIdForStmt((Stmt) trap.getHandlerUnit(), method) + "\"/>\n");
					}
					xmlizedSystem.write("\t\t\t</traplist>\n");
				}

				// capture info about locals
				for (Iterator i = body.getLocals().iterator(); i.hasNext();) {
					Local l = (Local) i.next();
					xmlizedSystem.write("\t\t\t<local id=\"" + idGenerator.getIdForLocal(l, method) + "\" name=\""
						+ l.getName() + "\" typeId=\"" + idGenerator.getIdForType(l.getType()) + "\"/>\n");
				}
			}
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", e);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public final void callback(SootClass clazz) {
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

			String accessSpec = "";

			if (clazz.isPublic()) {
				accessSpec = "public";
			} else if (clazz.isPrivate()) {
				accessSpec = "private";
			} else if (clazz.isProtected()) {
				accessSpec = "proctected";
			}
			xmlizedSystem.write("\t  accessSpec=\"" + accessSpec + "\">\n");

			if (clazz.hasSuperclass()) {
				xmlizedSystem.write("\t\t<superclass typeId=\"" + idGenerator.getIdForClass(clazz.getSuperclass()) + "\"/>\n");
			}

			if (clazz.getInterfaceCount() > 0) {
				xmlizedSystem.write("\t\t<interfaceList>\n");

				for (Iterator i = clazz.getInterfaces().iterator(); i.hasNext();) {
					SootClass inter = (SootClass) i.next();
					xmlizedSystem.write("\t\t\t<superinterface typeId=\"" + idGenerator.getIdForClass(inter) + "\"/>\n");
				}
				xmlizedSystem.write("\t\t</interfaceList>\n");
			}

			processingMethod = false;
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public final void callback(SootField field) {
		try {
			xmlizedSystem.write("\t\t<field name=\"" + field.getName() + "\" id=\"" + idGenerator.getIdForField(field)
				+ "\" typeId=\"" + idGenerator.getIdForType(field.getType()) + "\"\n");
			xmlizedSystem.write("\t\t  static=\"" + field.isStatic() + "\"\n");
			xmlizedSystem.write("\t\t  final=\"" + field.isFinal() + "\"\n");

			String accessSpec = "";

			if (field.isPublic()) {
				accessSpec = "public";
			} else if (field.isPrivate()) {
				accessSpec = "private";
			} else if (field.isProtected()) {
				accessSpec = "proctected";
			}
			xmlizedSystem.write("\t\t  accessSpec=\"" + accessSpec + "\"/>\n");
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", e);
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
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", e);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void hookup(ProcessingController ppc) {
		ppc.registerForAllStmts(this);
		ppc.register(this);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void unhook(ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);
		ppc.unregister(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		try {
			xmlizedSystem.write("<!DOCTYPE jimple PUBLIC \"-//ANT//DTD project//EN\" \"jimple.dtd\">\n");
			xmlizedSystem.write("<jimple>\n");
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Error while writing xmlized jimple info.", e);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
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
