
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

import soot.ArrayType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;

import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * This class represents the central access point for the information calculated in an analysis.  The subclass should extend
 * this class with methods to access various information about the implmented analysis.  This class by itself provides the
 * interface to query generic, low-level analysis information.  These interfaces should be used by implemented components of
 * the framework to extract information during the analysis.
 * 
 * <p>
 * Created: Fri Jan 25 14:49:45 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractAnalyzer
  implements IValueAnalyzer {
	/**
	 * The context to be used when analysis information is requested and a context is not provided.
	 */
	protected Context context;

	/**
	 * The instance of the framework performing the analysis and is being represented by this analyzer object.
	 *
	 * @invariant fa != null
	 */
	protected FA fa;

	/**
	 * This field indicates if the analysis has stablized.  In this implementation we have equated, stability to be
	 * non-active.  This is consistent with the fact that when non-active, the information returned on different identical
	 * queries are identical although the information may be wrong.
	 */
	private boolean stable;

	/**
	 * Creates a new <code>AbstractAnalyzer</code> instance.
	 *
	 * @param theContext the context to be used by this analysis instance.
	 * @param tagName is the name of the tag used by the instance of the flow analysis framework associated with this
	 * 		  analysis instance to tag parts of the AST.   Refer to <code>FA.FA(AbstractAnalyzer, String)</code> for more
	 * 		  detail.
	 *
	 * @pre theContext != null and tagName != null
	 */
	protected AbstractAnalyzer(final Context theContext, final String tagName) {
		this.context = theContext;
		fa = new FA(this, tagName);
		stable = false;
	}

	/**
	 * Returns the environment in which the analysis occurred.
	 *
	 * @return the environment in which the analysis occurred.
	 *
	 * @post result != null
	 */
	public IEnvironment getEnvironment() {
		return (IEnvironment) fa;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * Returns the values associated with exception class for the given invocation expression and <code>this.context</code>.
	 *
	 * @param e is the method invoke expression.
	 * @param exception is the class of the exception thrown by this expression.
	 *
	 * @return the collection of values associated with given exception class and and invoke expression.
	 *
	 * @pre e != null and exception != null
	 * @post result != null
	 */
	public final Collection getThrowValues(final InvokeExpr e, final SootClass exception) {
		MethodVariant mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection temp = Collections.EMPTY_SET;

		if (mv != null) {
			InvocationVariant iv = (InvocationVariant) mv.getASTVariant(e, context);

			if (iv != null) {
				temp = iv.queryThrowNode(exception).getValues();
			}
		}
		return temp;
	}

	/**
	 * Returns the values associated with <code>astChunk</code> in the given <code>ctxt</code>.
	 *
	 * @param astChunk for which the values are requested.
	 * @param ctxt in which the values were associated to <code>astChunk</code>.
	 *
	 * @return a collection of <code>Object</code>s.  The actual instance of the analysis framework decides the static type
	 * 		   of the objects in this collection.
	 *
	 * @throws IllegalArgumentException when <code>astChunk</code> is not of type <code>Value</code>, <code>SootField</code>,
	 * 		   <code>ParameterRef</code>, or <code>ArrayType</code>.
	 *
	 * @pre astChunk != null and ctxt != null
	 * @post result != null
	 */
	public final Collection getValues(final Object astChunk, final Context ctxt) {
		Context tmpCtxt = context;
		context = ctxt;

		Collection result = Collections.EMPTY_LIST;

		if (astChunk instanceof Value) {
			result = getValues((Value) astChunk);
		} else if (astChunk instanceof SootField) {
			result = getValues((SootField) astChunk);
		} else if (astChunk instanceof ParameterRef) {
			result = getValues((ParameterRef) astChunk);
		} else if (astChunk instanceof ArrayType) {
			result = getValues((ArrayType) astChunk);
		} else {
			throw new IllegalArgumentException("v has to of type Value, SootField, ParameterRef, or ArrayType.");
		}
		context = tmpCtxt;
		return result;
	}

	/**
	 * Returns the set of values associated with <code>this</code> variable in the context given by <code>context</code>.
	 *
	 * @param ctxt in which the values were associated to <code>this</code> variable.  The instance method associated with
	 * 		  the interested <code>this</code> variable should be the current method in the call string of this context.
	 *
	 * @return the collection of values associated with <code>this</code> in <code>context</code>.
	 *
	 * @pre ctxt != null
	 * @post result != null
	 */
	public final Collection getValuesForThis(final Context ctxt) {
		Context tmpCtxt = context;
		context = ctxt;

		MethodVariant mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection temp = Collections.EMPTY_LIST;

		if (mv != null) {
			temp = mv.queryThisNode().getValues();
		}
		context = tmpCtxt;
		return temp;
	}

	/**
	 * Analyzes the given set of classes starting from the given method.
	 *
	 * @param scm a central repository of classes to be analysed.
	 * @param root the analysis is started from this method.
	 *
	 * @throws IllegalStateException when root == <code>null</code>
	 *
	 * @pre scm != null root != null
	 */
	public final void analyze(final Scene scm, final SootMethod root) {
		if (root == null) {
			throw new IllegalStateException("Root method cannot be null.");
		}
		stable = false;
		fa.analyze(scm, root);
		stable = true;
	}

	/**
	 * Analyzes the given set of classes repeatedly by considering the given set of methods as the starting point.  The
	 * collected information is the union of the information calculated by considering the same set of classes but starting
	 * from each of the given methods.
	 *
	 * @param scm a central repository of classes to be analysed.
	 * @param roots a collection of <code>SootMethod</code>s representing the various possible starting points for the
	 * 		  analysis.
	 *
	 * @throws IllegalStateException wen roots is <code>null</code> or roots is empty.
	 *
	 * @pre scm != null and roots != null and not roots.isEmpty()
	 */
	public final void analyze(final Scene scm, final Collection roots) {
		if (roots == null || roots.isEmpty()) {
			throw new IllegalStateException("There must be at least one root method to analyze.");
		}

		stable = false;

		for (Iterator i = roots.iterator(); i.hasNext();) {
			SootMethod root = (SootMethod) i.next();
			fa.analyze(scm, root);
		}
		stable = true;
	}

	/**
	 * Reset the analyzer so that fresh run of the analysis can occur.  This is intended to be called by the environment to
	 * reset the analysis.
	 */
	public final void reset() {
		resetAnalysis();
		fa.reset();
	}

	/**
	 * Returns the set of values associated with the given array type in the context given by <code>this.context</code>.
	 *
	 * @param a the array type for which the values are requested.
	 *
	 * @return the collection of values associated with <code>a</code> in <code>this.context</code>.
	 *
	 * @pre a != null
	 * @post result != null
	 */
	protected final Collection getValues(final ArrayType a) {
		ArrayVariant v = fa.queryArrayVariant(a);
		Collection temp = Collections.EMPTY_SET;

		if (v != null) {
			temp = v.getFGNode().getValues();
		}
		return temp;
	}

	/**
	 * Returns the set of values associated with the given parameter reference in the context given by
	 * <code>this.context</code>.
	 *
	 * @param p the parameter reference for which the values are requested.
	 *
	 * @return the collection of values associated with <code>p</code> in <code>this.context</code>.
	 *
	 * @pre p != null
	 * @post result != null
	 */
	protected final Collection getValues(final ParameterRef p) {
		MethodVariant mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection temp = Collections.EMPTY_SET;

		if (mv != null) {
			temp = mv.queryParameterNode(p.getIndex()).getValues();
		}
		return temp;
	}

	/**
	 * Returns the set of values associated with the given field in the context given by <code>this.context</code>.
	 *
	 * @param sf the field for which the values are requested.
	 *
	 * @return the collection of values associated with <code>sf</code> in <code>this.context</code>.
	 *
	 * @pre sf != null
	 * @post result != null
	 */
	protected final Collection getValues(final SootField sf) {
		FieldVariant fv = fa.queryFieldVariant(sf);
		Collection temp = Collections.EMPTY_SET;

		if (fv != null) {
			temp = fv.getValues();
		}
		return temp;
	}

	/**
	 * Returns the set of values associated with the given AST node in the context given by <code>this.context</code>.
	 *
	 * @param v the AST node for which the values are requested.
	 *
	 * @return the collection of values associted with <code>v</code> in <code>this.context</code>.
	 *
	 * @pre v != null
	 * @post result != null
	 */
	protected final Collection getValues(final Value v) {
		MethodVariant mv = fa.queryMethodVariant(context.getCurrentMethod());
		Collection temp = Collections.EMPTY_SET;

		if (mv != null) {
			ValuedVariant astv = mv.queryASTVariant(v, context);

			if (astv != null) {
				temp = astv.getFGNode().getValues();
			}
		}
		return temp;
	}

	/**
	 * Sets the mode factory on the underlaying framework object.  Refer to <code>ModeFactory</code> for more details.
	 *
	 * @param mf is the mode factory which provides the entities that dictate the mode of the analysis.
	 *
	 * @pre mf != null
	 */
	protected void setModeFactory(final ModeFactory mf) {
		fa.setModeFactory(mf);
	}

	/**
	 * Reset the analyzer so that a fresh run of the analysis can occur.  This is intended to be overridden by the subclasses
	 * to reset analysis specific data structures.  It shall be called before the framework data structures are reset.
	 */
	protected final void resetAnalysis() {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.7  2003/08/21 03:47:11  venku
   Ripple effect of adding IStatus.
   Documentation.
   Revision 1.6  2003/08/21 03:44:08  venku
   Ripple effect of adding IStatus.
   Revision 1.5  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.4  2003/08/17 10:37:08  venku
   Fixed holes in documentation.
   Removed addRooMethods in FA and added the equivalent logic into analyze() methods.
   Revision 1.3  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
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
