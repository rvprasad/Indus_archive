
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.transformations.slicer;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import soot.jimple.Stmt;

import soot.tagkit.Tag;

import edu.ksu.cis.indus.transformations.common.AbstractTransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This transforms the given system based on the decisions of a slicing engine.  The results of the transformation are
 * captured as tags attached to the given system.
 * 
 * <p>
 * After transformation, the application can query the system for tags of kind <code>SlicingTag</code> and retrieve slicing
 * information of the system.  However, as locals cannot be tagged, the application will have to obtain that information
 * from this class.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TagBasedSlicingTransformer
  extends AbstractTransformer {
	/**
	 * An instance to be used to satisfy <code>Tag.getValue()</code> call on <code>SlicingTag</code> objects.
	 */
	static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 * The system to be transformed.
	 */
	protected Scene system;

	/**
	 * The name to be used for the tags.
	 */
	String tagName;

	/**
	 * This maps transformed methods to their transformed locals.
	 */
	private final Map method2locals = new HashMap();

	/**
	 * The tag used to annotate the system with slicing information.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public class SlicingTag
	  implements Tag {
		/**
		 * @see soot.tagkit.Tag#getName()
		 */
		public String getName() {
			return tagName;
		}

		/**
		 * @see soot.tagkit.Tag#getValue()
		 */
		public byte[] getValue() {
			return EMPTY_BYTE_ARRAY;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.SootClass)
	 */
	public SootClass getTransformed(final SootClass clazz) {
		return clazz.getTag(tagName) != null ? clazz
											 : null;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.SootField)
	 */
	public SootField getTransformed(final SootField field) {
		return field.getTag(tagName) != null ? field
											 : null;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.SootMethod)
	 */
	public SootMethod getTransformed(final SootMethod method) {
		return method.getTag(tagName) != null ? method
											  : null;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Stmt getTransformed(final Stmt untransformedStmt, final SootMethod untransformedMethod) {
		Stmt result = null;

		if (untransformedMethod.getTag(tagName) != null && untransformedStmt.getTag(tagName) != null) {
			result = untransformedStmt;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformedClasses()
	 */
	public Collection getTransformedClasses() {
		Collection result = new ArrayList();

		for (Iterator i = system.getClasses().iterator(); i.hasNext();) {
			SootClass clazz = (SootClass) i.next();

			if (clazz.getTag(tagName) != null) {
				result.add(clazz);
			}
		}
		return result.isEmpty() ? Collections.EMPTY_LIST
								: result;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformedLocal(soot.Local, soot.SootMethod)
	 */
	public Local getTransformedLocal(final Local local, final SootMethod method) {
		Local result = null;

		if (method.getTag(tagName) != null) {
			Collection locals = (Collection) method2locals.get(method);

			if (locals != null && locals.contains(local)) {
				result = local;
			}
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformedSootClass(java.lang.String)
	 */
	public SootClass getTransformedSootClass(final String name) {
		SootClass result = system.getSootClass(name);

		if (result != null && result.getTag(tagName) == null) {
			result = null;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getUntransformed(soot.SootClass)
	 */
	public SootClass getUntransformed(final SootClass clazz) {
		return clazz.getTag(tagName) != null ? clazz
											 : null;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getUntransformed(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Stmt getUntransformed(final Stmt transformedStmt, final SootMethod transformedMethod) {
		Stmt result = null;

		if (transformedMethod.getTag(tagName) != null && transformedStmt.getTag(tagName) != null) {
			result = transformedStmt;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#completeTransformation()
	 */
	public void completeTransformation() {
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#reset()
	 */
	public void reset() {
		method2locals.clear();
		tagName = null;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#transform(soot.jimple.Stmt, soot.SootMethod)
	 */
	public void transform(final Stmt stmt, final SootMethod method) {
	}

	/**
	 * Initializes the transformer.
	 *
	 * @param theSystem that is to be sliced.
	 * @param theTagName to be used during this transformation.  If none are specified, then a default built-in tag name is
	 * 		  used.
	 *
	 * @pre theSystem != null
	 */
	protected void initialize(final Scene theSystem, final String theTagName) {
		system = theSystem;

		if (theTagName != null) {
			tagName = theTagName;
		} else {
			tagName = "SLICING_TAG";
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/19 12:55:50  venku
   This is a tag-based non-destructive slicing transformation implementation.

 */
