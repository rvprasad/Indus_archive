
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
import soot.Value;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

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
  implements IProcessor {
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
	private boolean processingMethod = true;

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
		JimpleXMLizer xmlizer = new JimpleXMLizer(new UniqueIDGenerator());
		ProcessingController pc = new XMLizingController();
		Scene scene = Scene.v();
		Environment env = new Environment(scene);
		pc.setEnvironment(env);

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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public final String getXMLizedForm() {
		return xmlizedSystem.toString();
	}

	/**
	 * This does nothing.
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.Value, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(Value value, Context context) {
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(Stmt stmt, Context context) {
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
				xmlizedSystem.write("<method/>");
			} else {
				processingMethod = true;
			}

			xmlizedSystem.write("<class signature=\"" + method.getSubSignature() + "\" id=\""
				+ idGenerator.getIdForMethod(method) + "\"/>");
			stmtXmlizer.setMethod(method);
			idGenerator.resetStmtCounter();

			if (method.isConcrete()) {
				Body body = method.retrieveActiveBody();

				for (Iterator i = body.getLocals().iterator(); i.hasNext();) {
					Local l = (Local) i.next();
					xmlizedSystem.write("<local id=\"" + idGenerator.getIdForLocal(l, method) + "\" name=\"" + l.getName()
						+ "\"/>");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public final void callback(SootClass clazz) {
		try {
			if (processingClass) {
				xmlizedSystem.write("</class>");
			} else {
				processingClass = true;
			}

			xmlizedSystem.write("<class signature=\"" + clazz.getName() + "\" id=\"" + idGenerator.getIdForClass(clazz)
				+ "\"/>");
			processingMethod = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public final void callback(SootField field) {
		try {
			xmlizedSystem.write("<field signature=\"" + field.getSubSignature() + "\" id=\""
				+ idGenerator.getIdForField(field) + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public final void consolidate() {
		try {
			if (processingMethod) {
				xmlizedSystem.write("</method>");
			}

			if (processingClass) {
				xmlizedSystem.write("</class>");
			}
		} catch (IOException e) {
			e.printStackTrace();
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
}

/*
   ChangeLog:
   $Log$
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
