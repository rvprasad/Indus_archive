
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.NamedTag;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import soot.tagkit.Tag;


/**
 * The instance of the framework which controls and manages the analysis on execution.  It acts the central repository for
 * information pertaining to various components of the framework when the analysis is in progress.  It also serves as the
 * central repository for various instances of the framework at a given time.
 * 
 * <p>
 * Created: Tue Jan 22 00:45:10 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FA
  implements IEnvironment {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FA.class);

	/**
	 * The analyzer associated with this instance of the framework.
	 *
	 * @invariant _analyzer != null
	 */
	public final AbstractAnalyzer _analyzer;

	/**
	 * This is the collection of methods that serve as entry points into the system being analyzed.
	 *
	 * @invariant rootMethods != null
	 */
	protected final Collection rootMethods = new HashSet();

	/**
	 * The worklist associated with this instance of the framework.
	 *
	 * @invariant worklist != null
	 */
	final WorkList worklist;

	/**
	 * The manager of array variants.
	 */
	private ArrayVariantManager arrayVariantManager;

	/**
	 * The manager of class related primitive information and processing.
	 */
	private ClassManager classManager;

	/**
	 * The manager of instance field variants.
	 */
	private FieldVariantManager instanceFieldVariantManager;

	/**
	 * The manager of static field variants.
	 */
	private FieldVariantManager staticFieldVariantManager;

	/**
	 * The manager of method variants.
	 */
	private MethodVariantManager methodVariantManager;

	/**
	 * The factory that provides the components during the analysis performed by this instance of the framework.
	 */
	private ModeFactory modeFactory;

	/**
	 * The tag used to identify the elements of the AST touched by this framework instance.
	 */
	private NamedTag tag;

	/**
	 * The <code>Scene</code> which provides the set of class to be analyzed.
	 */
	private Scene scm;

	/**
	 * Creates a new <code>FA</code> instance.
	 *
	 * @param analyzer to be associated with this instance of the framework.
	 * @param tagName is the name of the tag that will be tacked onto parts of the AST processed by this framework instance.
	 * 		  The guarantee is that all elements so tagged were processed by the framework instance.  The inverse need not
	 * 		  be true.
	 *
	 * @pre analyzer != null and tagName != null
	 */
	FA(final AbstractAnalyzer analyzer, final String tagName) {
		worklist = new WorkList();
		this._analyzer = analyzer;
		this.tag = new NamedTag(tagName);
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.  If none
	 * exists, a new variant is created.
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 *
	 * @pre a != null
	 * @post result != null
	 */
	public final ArrayVariant getArrayVariant(final ArrayType a) {
		return getArrayVariant(a, _analyzer.context);
	}

	/**
	 * Returns the variant associated with the given array type in the given context.  If none exists, a new variant is
	 * created.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>.
	 *
	 * @pre a != null and context != null
	 * @post result != null
	 */
	public final ArrayVariant getArrayVariant(final ArrayType a, final Context context) {
		processType(a.baseType);
		return (ArrayVariant) arrayVariantManager.select(a, context);
	}

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 *
	 * @pre className != null
	 */
	public final SootClass getClass(final String className) {
		return scm.getSootClass(className);
	}

	/**
	 * Returns the classes of the system that were analyzed/accessed by this analysis.
	 *
	 * @return a collection of classes.
	 *
	 * @post result != null and result->forall(o | o.oclIsTypeOf(soot.SootClass))
	 */
	public Collection getClasses() {
		return Collections.unmodifiableCollection(classManager.classes);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.  If none exists,
	 * a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.
	 *
	 * @pre sf != null
	 * @post result != null
	 */
	public final FieldVariant getFieldVariant(final SootField sf) {
		return getFieldVariant(sf, _analyzer.context);
	}

	/**
	 * Returns the variant associated with the given field in the given context.  If none exists, a new variant is created.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant associated with the given field in the given context.
	 *
	 * @pre sf != null and context != null
	 * @post result != null
	 */
	public final FieldVariant getFieldVariant(final SootField sf, final Context context) {
		IVariant temp = null;
		processClass(sf.getDeclaringClass());
		processType(sf.getType());

		if (Modifier.isStatic(sf.getModifiers())) {
			temp = staticFieldVariantManager.select(sf, context);
		} else {
			temp = instanceFieldVariantManager.select(sf, context);
		}
		return (FieldVariant) temp;
	}

	/**
	 * Returns an LHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new LHS expression visitor.
	 *
	 * @pre e != null
	 */
	public final AbstractExprSwitch getLHSExpr(final AbstractStmtSwitch e) {
		return modeFactory.getLHSExprVisitor(e);
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.  If none
	 * exists, a new variant is created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>.
	 *
	 * @pre sm != null
	 * @post result != null
	 */
	public final MethodVariant getMethodVariant(final SootMethod sm) {
		return getMethodVariant(sm, _analyzer.context);
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.  If none exists, a new variant is
	 * created.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 *
	 * @return the variant corresonding to <code>sm</code> in the given context.
	 *
	 * @pre sm != null and context != null
	 * @post result != null
	 */
	public final MethodVariant getMethodVariant(final SootMethod sm, final Context context) {
		return (MethodVariant) methodVariantManager.select(sm, context);
	}

	/**
	 * Returns the flow graph node as created by the factory.
	 *
	 * @return a new flow graph node.
	 */
	public final IFGNode getNewFGNode() {
		return modeFactory.getFGNode(worklist);
	}

	/**
	 * Returns a RHS expression visitor as created by the factory.
	 *
	 * @param e the statement visitor which parameterizes the expression visitor.
	 *
	 * @return a new RHS expression visitor.
	 *
	 * @pre e != null
	 */
	public final AbstractExprSwitch getRHSExpr(final AbstractStmtSwitch e) {
		return modeFactory.getRHSExprVisitor(e);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getRoots()
	 */
	public Collection getRoots() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Returns the associated <code>Scene</code>.
	 *
	 * @return the associated <code>Scene</code>.
	 *
	 * @post result != null
	 */
	public final Scene getScene() {
		return scm;
	}

	/**
	 * Returns a statement visitor as created by the factory.
	 *
	 * @param e the method variant which parameterizes the statement visitor.
	 *
	 * @return a new statement visitor.
	 *
	 * @pre e != null
	 */
	public final AbstractStmtSwitch getStmt(final MethodVariant e) {
		return modeFactory.getStmtVisitor(e);
	}

	/**
	 * Performs type-based processing of the given class.
	 *
	 * @param clazz is the class to be processed.
	 *
	 * @pre clazz != null
	 */
	public void processClass(final SootClass clazz) {
		classManager.process(clazz);
	}

	/**
	 * Resets the framework.  The framework forgets all information allowing for a new session of analysis to executed.
	 */
	public void reset() {
		arrayVariantManager.reset();
		instanceFieldVariantManager.reset();
		methodVariantManager.reset();
		staticFieldVariantManager.reset();
		worklist.clear();
		rootMethods.clear();
		classManager.reset();
		scm = null;
	}

	/**
	 * Returns the variant associated with the given array type in the context captured by <code>analyzer</code>.
	 *
	 * @param a the array type whose variant is to be returned.
	 *
	 * @return the variant corresponding to <code>a</code> in the context captured by <code>analyzer</code>.
	 * 		   <code>null</code> if none exist.
	 *
	 * @pre a != null
	 * @post result != null
	 */
	final ArrayVariant queryArrayVariant(final ArrayType a) {
		return queryArrayVariant(a, _analyzer.context);
	}

	/**
	 * Returns the variant associated with the given array type in the given context.
	 *
	 * @param a the array type whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant corresponding to <code>a</code> in context <code>context</code>. <code>null</code> if none exist.
	 *
	 * @pre a != null and context != null
	 * @post result != null
	 */
	final ArrayVariant queryArrayVariant(final ArrayType a, final Context context) {
		return (ArrayVariant) arrayVariantManager.query(a, context);
	}

	/**
	 * Returns the variant associated with the given field in the context captured by <code>analyzer</code>.
	 *
	 * @param sf the field whose variant is to be returned.
	 *
	 * @return the variant associated with the given field in the context captured by <code>analyzer</code>.<code>null</code>
	 * 		   if none exists.
	 *
	 * @pre sf != null
	 * @post result != null
	 */
	final FieldVariant queryFieldVariant(final SootField sf) {
		return queryFieldVariant(sf, _analyzer.context);
	}

	/**
	 * Returns the variant associated with the given field in the given context.
	 *
	 * @param sf the field whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant associated with the given field in the given context.  <code>null</code> if none exists.
	 *
	 * @pre sf != null and context != null
	 * @post result != null
	 */
	final FieldVariant queryFieldVariant(final SootField sf, final Context context) {
		IVariant temp = null;

		if (Modifier.isStatic(sf.getModifiers())) {
			temp = staticFieldVariantManager.query(sf, context);
		} else {
			temp = instanceFieldVariantManager.query(sf, context);
		}
		return (FieldVariant) temp;
	}

	/**
	 * Returns a method variant correpsoding to the given method in the context <code>analyzer.context</code>.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 *
	 * @return a variant of <code>sm</code> in the context <code>analyzer.context</code>. <code>null</code> if none exist.
	 *
	 * @pre sm != null
	 * @post result != null
	 */
	final MethodVariant queryMethodVariant(final SootMethod sm) {
		return queryMethodVariant(sm, _analyzer.context);
	}

	/**
	 * Returns a method variant corresponding to the given method in the given context.
	 *
	 * @param sm the method corresponding to which the variant is requested.
	 * @param context the context of the requested variant.
	 *
	 * @return the variant corresonding to <code>sm</code> in the given context.  <code>null</code> if none exist.
	 *
	 * @pre sm != null and context != null
	 * @post result != null
	 */
	final MethodVariant queryMethodVariant(final SootMethod sm, final Context context) {
		return (MethodVariant) methodVariantManager.query(sm, context);
	}

	/**
	 * Sets the mode factory on this framework instance.
	 *
	 * @param mf is the factory object which provides the objects that dictate the mode of analysis.
	 */
	void setModeFactory(final ModeFactory mf) {
		modeFactory = mf;
		this.classManager = mf.getClassManager(this);
		arrayVariantManager = new ArrayVariantManager(this, mf.getArrayIndexManager());
		instanceFieldVariantManager = new FieldVariantManager(this, mf.getInstanceFieldIndexManager());
		methodVariantManager = new MethodVariantManager(this, mf.getMethodIndexManager(), modeFactory.getASTIndexManager());
		staticFieldVariantManager = new FieldVariantManager(this, mf.getStaticFieldIndexManager());
	}

	/**
	 * Retrieves the tag associated with the framework instance.
	 *
	 * @return the tag associated with the framework instance.
	 *
	 * @post result != null
	 */
	Tag getTag() {
		return tag;
	}

	/**
	 * Analyzes the given classes starting with <code>root</code> method.
	 *
	 * @param scene <code>Scene</code> object which contains the classes to be analyzed.
	 * @param root the method to start the analysis from.
	 *
	 * @pre scene != null and root != null
	 */
	void analyze(final Scene scene, final SootMethod root) {
		this.scm = scene;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting system processing...");
		}
		getMethodVariant(root);
		rootMethods.add(root);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting worklist processing...");
		}
		worklist.process();
	}

	/**
	 * Performs type-based processing.
	 *
	 * @param type to be processed.
	 *
	 * @pre type != null
	 */
	void processType(final Type type) {
		if (type instanceof RefType) {
			classManager.process(getClass(((RefType) type).getClassName()));
		} else if (type instanceof ArrayType && ((ArrayType) type).baseType instanceof RefType) {
			classManager.process(getClass(((RefType) ((ArrayType) type).baseType).getClassName()));
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.14  2003/12/05 15:29:44  venku
   - class manager was not being reset.  FIXED.
   Revision 1.13  2003/12/05 00:53:09  venku
   - removed unused method and restricted access to certain methods.
   Revision 1.12  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.11  2003/11/30 01:39:34  venku
   - changed access level of getTag.
   Revision 1.10  2003/11/30 01:09:42  venku
   - documentation.
   Revision 1.9  2003/11/30 01:07:57  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.8  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.4  2003/08/17 10:37:08  venku
   Fixed holes in documentation.
   Removed addRooMethods in FA and added the equivalent logic into analyze() methods.
   Revision 1.3  2003/08/16 21:50:51  venku
   Removed ASTVariant as it did not contain any data that was used.
   Concretized AbstractValuedVariant and renamed it to ValuedVariant.
   Ripple effect of the above change in some.
   Spruced up documentation and specification.
   Revision 1.2  2003/08/11 07:11:47  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Moved getRoots() into the environment.
   Added support to inject new roots in FA.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
