
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

package edu.ksu.cis.indus.transformations.slicer;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

import soot.tagkit.StringTag;

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
  extends AbstractTransformer
  implements ISlicingBasedTransformer {
	/**
	 * An instance to be used to satisfy <code>Tag.getValue()</code> call on <code>SlicingTag</code> objects.
	 */
	static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 * Default name of slicing tags.
	 */
	private static final String SLICING_TAG = "Slicing Tag";

	/**
	 * The system to be transformed.
	 */
	protected Scene system;

	/**
	 * This maps transformed methods to their transformed locals.
	 */
	private final Map method2locals = new HashMap();

	/**
	 * The name of the tag instance active in this instance of the transformer.
	 */
	private String tagName = SLICING_TAG;

	/**
	 * The tag to be used during transformation.
	 */
	private StringTag tag;

	/**
	 * Set the tag name to be used.
	 *
	 * @param theTagName to be used during this transformation.  If none are specified, then a default built-in tag name is
	 * 		  used.
	 */
	public void setTagName(final String theTagName) {
		if (theTagName != null) {
			tag = new StringTag(theTagName);
			tagName = theTagName;
		} else {
			tag = new StringTag(tagName);
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
	 * Returns <code>true</code> as this transformer can handle partial inclusions.
	 *
	 * @return <code>true</code>
	 *
	 * @see edu.ksu.cis.indus.transformations.slicer.ISlicingBasedTransformer#handlesPartialInclusions()
	 */
	public boolean handlesPartialInclusions() {
		return true;
	}

	/**
	 * Initializes the transformer.
	 *
	 * @param theSystem that is to be sliced.
	 *
	 * @pre theSystem != null
	 */
	public void initialize(final Scene theSystem) {
		system = theSystem;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#reset()
	 */
	public void reset() {
		method2locals.clear();
		tagName = SLICING_TAG;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#transform(soot.jimple.Stmt, soot.SootMethod)
	 */
	public void transform(final Stmt stmt, final SootMethod method) {
		Collection temp = stmt.getUseAndDefBoxes();

		for (Iterator i = temp.iterator(); i.hasNext();) {
			ValueBox vb = (ValueBox) i.next();

			if (vb.getTag(tagName) == null) {
				vb.addTag(tag);
			}
		}

		if (stmt.getTag(tagName) == null) {
			stmt.addTag(tag);
		}

		if (method.getTag(tagName) == null) {
			method.addTag(tag);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#transform(soot.jimple.ValueBox, soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	public void transform(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		if (vBox.getTag(tagName) == null) {
			vBox.addTag(tag);
		}

		if (stmt.getTag(tagName) == null) {
			stmt.addTag(tag);
		}

		if (method.getTag(tagName) == null) {
			method.addTag(tag);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/09/15 07:52:08  venku
   - added a new transformer interface specifically targetted for slicing.
   - implemented the above interface.
   Revision 1.4  2003/08/25 07:17:38  venku
   Exposed initialize() as a public method.
   Removed SlicingTag class and used StringTag instead.
   Revision 1.3  2003/08/21 09:30:31  venku
    - added a new transform() method which can transform at the level of ValueBox.
    - CloningBasedSlicingTransformer does not do anything in this new method.
   Revision 1.2  2003/08/20 18:31:22  venku
   Documentation errors fixed.
   Revision 1.1  2003/08/19 12:55:50  venku
   This is a tag-based non-destructive slicing transformation implementation.
 */
