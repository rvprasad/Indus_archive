
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

package edu.ksu.cis.indus.slicer;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;

import soot.tagkit.Tag;

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.transformations.common.ITransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * This residualizes the given system based on the decisions of a slicing engine.  The system is residualized by tagging
 * parts of the original system which occur in the slice.
 * 
 * <p>
 * After residualization, the application can query the system for tags of kind <code>SlicingTag</code> and retrieve slicing
 * information of the system.  However, as locals cannot be tagged, the application will have to obtain that information
 * from this class.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TaggingBasedSliceCollector
  implements ITransformer  {
	/**
	 * An instance to be used to satisfy <code>Tag.getValue()</code> call on <code>SlicingTag</code> objects.
	 */
	static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 * Default name of slicing tags.
	 */
	public static final String SLICING_TAG = "Slicing Tag";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TaggingBasedSliceCollector.class);

	/**
	 * The system to be transformed.
	 */
	protected Scene system;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private SlicingTag seedTag = new SlicingTag(SLICING_TAG, true);

	/**
	 * The tag to be used during transformation.
	 */
	private SlicingTag tag = new SlicingTag(SLICING_TAG, false);

	/**
	 * The name of the tag instance active in this instance of the transformer.
	 */
	private String tagName = SLICING_TAG;

    private Object sliceType;

    private Collection taggedMethods = new HashSet();
    
    private BasicBlockGraphMgr bbgMgr;

	/**
	 * DOCUMENT ME!
	 *
	 * @author venku To change this generated comment go to  Window>Preferences>Java>Code Generation>Code Template
	 */
	public class BranchingSlicingTag
	  extends SlicingTag {
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		List targets;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private int count;

		/**
		 * Creates a new BranchingSlicingTag object.
		 *
		 * @param name DOCUMENT ME!
		 * @param targetCount DOCUMENT ME!
		 * @param isSeed DOCUMENT ME!
		 */
		BranchingSlicingTag(final String name, final int targetCount, final boolean isSeed) {
			super(name, isSeed);
			targets = new ArrayList(targetCount);
			count = targetCount;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		public int getTargetCount() {
			return targets.size();
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param index DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public Stmt getTargetStmt(final int index) {
			return (Stmt) targets.get(index);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param index DOCUMENT ME!
		 * @param stmt DOCUMENT ME!
		 *
		 * @throws IndexOutOfBoundsException DOCUMENT ME!
		 */
		void setTargetStmt(final int index, final Stmt stmt) {
			if (index >= 0 || index < count) {
				targets.add(index, stmt);
			} else {
				throw new IndexOutOfBoundsException(index + " is outside the range 0-" + (count - 1) + ".");
			}
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class SlicingTag
	  implements Tag {
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final String name;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final boolean seed;

		/**
		 * Creates a new SlicingTag object.
		 *
		 * @param theName DOCUMENT ME!
		 * @param isSeed DOCUMENT ME!
		 */
		public SlicingTag(final String theName, final boolean isSeed) {
			name = theName;
			seed = isSeed;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		public String getName() {
			return name;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		public boolean isSeed() {
			return seed;
		}

		/**
		 * @see soot.tagkit.Tag#getValue()
		 */
		public byte[] getValue() {
			return name.getBytes();
		}
	}

	/**
	 * Set the tag name to be used.
	 *
	 * @param theTagName to be used during this transformation.  If none are specified, then a default built-in tag name is
	 * 		  used.
	 */
	public void setTagName(final String theTagName) {
		if (theTagName != null) {
			tag = new SlicingTag(theTagName, false);
			tagName = theTagName;
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
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.ValueBox, soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	public ValueBox getTransformed(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		ValueBox result = null;
		Stmt transformed = getTransformed(stmt, method);

		if (transformed != null && vBox.getTag(tagName) != null) {
			result = vBox;
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
		// TODO: Auto-generated method stub
	}

	/**
	 * {@inheritDoc}  This implementation can handle all slice types defined in <code>SlicingEngine</code> be it executable
	 * or non-executable except executable forward slices.
	 */
	public boolean handleSliceType(final Object theSliceType, final boolean executableSlice) {
        sliceType = theSliceType;
        boolean result = SlicingEngine.SLICE_TYPES.contains(theSliceType);
        result &= !(theSliceType.equals(SlicingEngine.FORWARD_SLICE) && executableSlice);
        return result;
	}

	/**
	 * Returns <code>true</code> as this transformer can handle partial inclusions.
	 *
	 * @return <code>true</code>
	 *
	 * @see edu.ksu.cis.indus.slicer.ISliceCollector#handlesPartialInclusions()
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
	 * @see edu.ksu.cis.indus.transformations.slicer.ISliceCollector#makeExecutable()
	 */
	public void makeExecutable() {
        if (sliceType.equals(SlicingEngine.BACKWARD_SLICE))
            makeBackwardSliceExecutable();
	}

    private void makeBackwardSliceExecutable() {
        for (Iterator i = taggedMethods.iterator(); i.hasNext(); ) {
            SootMethod sm = (SootMethod) i.next();
            BasicBlockGraph bbg = bbgMgr.getBasicBlockGraph(sm);
            
            
        }
    }

    /**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#reset()
	 */
	public void reset() {
		tagName = SLICING_TAG;
        taggedMethods.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#transform(soot.jimple.Stmt, soot.SootMethod)
	 */
	public void transform(final Stmt stmt, final SootMethod method) {
		tagStmt(stmt, method, tag, false);
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#transform(ValueBox, Stmt, SootMethod)
	 */
	public void transform(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		tagValueBox(vBox, stmt, method, tag);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.AbstractSliceResidualizer#transformSeed(soot.jimple.Stmt, soot.SootMethod)
	 */
	protected void transformSeed(final Stmt stmt, final SootMethod method) {
		tagStmt(stmt, method, seedTag, true);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.AbstractSliceResidualizer#transformSeed(soot.ValueBox, soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	protected void transformSeed(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		tagValueBox(vBox, stmt, method, seedTag);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 * @param theTag DOCUMENT ME!
	 */
	private void tagMethod(final SootMethod method, final SlicingTag theTag) {
		if (method.getTag(tagName) == null) {
			method.addTag(theTag);
            taggedMethods.add(method);
		}

		SootClass sc = method.getDeclaringClass();

		if (sc.getTag(tagName) == null) {
			sc.addTag(theTag);
            System.out.println("Tagged: "  + sc.getName());
		}

		if (sc.hasSuperclass()) {
			sc = sc.getSuperclass();

			if (sc.declaresMethod(method.getName(), method.getParameterTypes(), method.getReturnType())) {
				tagMethod(sc.getMethod(method.getName(), method.getParameterTypes(), method.getReturnType()), theTag);
			}
		}
	}
    
    /**
     * Marks the given criteria as included in the slice.  {@inheritDoc}
     *
     * @param seedcriteria DOCUMENT ME!
     *
     * @see edu.ksu.cis.indus.slicer.ISliceCollector#processSeedCriteria(java.util.Collection)
     */
    public void processSeedCriteria(final Collection seedcriteria) {
        for (Iterator i = seedcriteria.iterator(); i.hasNext();) {
            AbstractSliceCriterion crit = (AbstractSliceCriterion) i.next();

            if (crit instanceof SliceExpr) {
                SliceExpr expr = (SliceExpr) crit;
                transformSeed((ValueBox) expr.getCriterion(), expr.getOccurringStmt(), expr.getOccurringMethod());
            } else if (crit instanceof SliceStmt) {
                SliceStmt stmt = (SliceStmt) crit;
                transformSeed((Stmt) stmt.getCriterion(), stmt.getOccurringMethod());
            }
            crit.sliced();
        }
    }
    

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param theTag DOCUMENT ME!
	 * @param seed DOCUMENT ME!
	 */
	private void tagStmt(final Stmt stmt, final SootMethod method, final SlicingTag theTag, final boolean seed) {
		SlicingTag stmtTag = (SlicingTag) stmt.getTag(tagName);

		if (stmtTag == null || (!seed && stmtTag.isSeed())) {
			if (stmt instanceof IfStmt) {
				stmt.addTag(new BranchingSlicingTag(tag.getName(), 2, seed));
			} else if (stmt instanceof GotoStmt) {
				stmt.addTag(new BranchingSlicingTag(tag.getName(), 1, seed));
			} else if (stmt instanceof LookupSwitchStmt) {
				stmt.addTag(new BranchingSlicingTag(tag.getName(), ((LookupSwitchStmt) stmt).getTargetCount(), seed));
			} else if (stmt instanceof TableSwitchStmt) {
				TableSwitchStmt t = (TableSwitchStmt) stmt;
				stmt.addTag(new BranchingSlicingTag(tag.getName(), (t.getHighIndex() - t.getLowIndex()), seed));
			} else {
				stmt.addTag(theTag);
			}

			if (stmt.containsFieldRef()) {
				stmt.getFieldRef().getField().addTag(theTag);
			}
			tagMethod(method, theTag);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Tagged statement: " + stmt + "[" + stmt.hashCode() + "] | " + method.getSignature());
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param vBox DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param theTag DOCUMENT ME!
	 */
	private void tagValueBox(final ValueBox vBox, final Stmt stmt, final SootMethod method, final SlicingTag theTag) {
		SlicingTag valueTag = (SlicingTag) stmt.getTag(tagName);

		if (valueTag == null || (theTag != seedTag && valueTag.isSeed())) {
			vBox.addTag(theTag);

			if (vBox instanceof FieldRef) {
				((FieldRef) vBox).getField().addTag(theTag);
			}

			if (stmt.getTag(tagName) == null) {
				stmt.addTag(theTag);
				tagMethod(method, theTag);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Tagged value: " + vBox.getValue() + " | " + stmt + "[" + stmt.hashCode() + "] | "
					+ method.getSignature());
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/24 09:46:49  venku
   - moved ISliceCollector and TaggingBasedSliceCollector
     into slicer package.
   - The idea is to collect the slice based on annotation which
     can be as precise as we require and then layer on
     top of that the slicer residualization logic, either constructive or destructive.

   Revision 1.2  2003/11/24 07:31:03  venku
   - deleted method2locals, executable, and sliceType as they were not used.

   Revision 1.1  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.17  2003/11/17 01:39:42  venku
   - added slice XMLization support.
   Revision 1.16  2003/11/16 23:12:17  venku
   - coding convention.
   Revision 1.15  2003/11/16 22:55:31  venku
   - added new methods to support processing of seed criteria.
     This is not same as slicing seed criteria of which we do not
     make any distinction.
   Revision 1.14  2003/11/13 14:08:08  venku
   - added a new tag class for the purpose of recording branching information.
   - renamed fixReturnStmts() to makeExecutable() and raised it
     into ISliceCollector interface.
   - ripple effect.
   Revision 1.13  2003/11/05 09:05:28  venku
   - For strange reasons the StringTag does not fulfill our needs.
     So, we introduce a new class, SlicingTag.
   Revision 1.12  2003/11/05 08:32:50  venku
   - transformation are supported per entity basis.  This
     means each expression in a statement and needs to
     be tagged separately.  The containing statement
     should also be tagged likewise.
   Revision 1.11  2003/11/03 08:02:31  venku
   - ripple effect of changes to ITransformer.
   - added logging.
   - optimization.
   Revision 1.10  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.9  2003/10/13 01:00:09  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.8  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.7  2003/09/27 01:27:46  venku
   - documentation.
   Revision 1.6  2003/09/26 15:08:35  venku
   - ripple effect of changes in ITransformer.
   Revision 1.5  2003/09/15 07:52:08  venku
   - added a new transformer interface specifically targetted for slicing.
   - implemented the above interface.
   Revision 1.4  2003/08/25 07:17:38  venku
   Exposed initialize() as a public method.
   Removed SlicingTag class and used StringTag instead.
   Revision 1.3  2003/08/21 09:30:31  venku
    - added a new transform() method which can transform at the level of ValueBox.
    - CloningBasedSliceResidualizer does not do anything in this new method.
   Revision 1.2  2003/08/20 18:31:22  venku
   Documentation errors fixed.
   Revision 1.1  2003/08/19 12:55:50  venku
   This is a tag-based non-destructive slicing transformation implementation.
 */
