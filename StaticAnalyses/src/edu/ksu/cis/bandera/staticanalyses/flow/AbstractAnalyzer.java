package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.ParameterRef;
import ca.mcgill.sable.soot.jimple.ThisRef;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//AbstractAnalyzer.java
/**
 * <p>This class represents the central access point for the information calculated in an analysis.  The subclass should
 * extend this class with methods to access various information about the implmented analysis.  This class by itself provides
 * the interface to query generic, low-level analysis information.  These interfaces should be used by implemented components
 * of the framework to extract information during the analysis.</p>
 *
 * Created: Fri Jan 25 14:49:45 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractAnalyzer {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractAnalyzer.class);

	/**
	 * <p>The instance of the framework performing the analysis represented by this analyzer object.</p>
	 *
	 */
	protected BFA bfa;

	/**
	 * <p>The context to be used when analysis information is requested and a context is not provided.</p>
	 *
	 */
	public final Context context;

	/**
	 * <p>This field indicates if the analysis is progress or otherwise.</p>
	 *
	 */
	protected boolean active;

	private final static Collection unmodifiableEmptyCollection = Collections.unmodifiableCollection(new HashSet());

	/**
	 * <p>Creates a new <code>AbstractAnalyzer</code> instance.</p>
	 *
	 * @param name the name of the analysis to used to identify the corresponding instance of the framework.
	 * @param mf the factory to be used to create the components in the  framework during the analysis.
	 */
	protected AbstractAnalyzer (String name, ModeFactory mf){
		context = new Context();
		bfa = new BFA(name, this, mf);
		active = false;
	}

	/**
	 * <p>Analyzes the given set of classes starting from the given method.</p>
	 *
	 * @param scm a central repository of classes to be analysed.
	 * @param root the analysis is started from this method.
	 */
	public final void analyze(SootClassManager scm, SootMethod root) {
		if (root == null) {
			throw new IllegalStateException("Root method cannot be null.");
		} // end of if (root == null)
		active = true;
		bfa.analyze(scm, root);
		active = false;
	}

	/**
	 * <p>Analyzes the given set of classes repeatedly by considering the given set of methods as the starting point.  The
	 * collected information is the union of the information calculated by considering the same set of classes but starting
	 * from each of the given methods.</p>
	 *
	 * @param scm a central repository of classes to be analysed.
	 * @param roots a collection of <code>SootMethod</code>s representing the various possible starting points for the
	 * analysis.
	 */
	public final void analyze(SootClassManager scm, Collection roots) {
		if (roots == null || roots.size() == 0) {
			throw new IllegalStateException("There must be at least one root method to analyze.");
		} // end of if (root == null)
		active = true;
		for (Iterator i = roots.iterator(); i.hasNext();) {
			 bfa.analyze(scm, (SootMethod)i.next());
		} // end of for (Iterator i = roots.iterator(); i.hasNext();)
		active = false;
	}

	/**
	 * <p>Returns the analyser object corresponding to the given name.  Each instance of the framework will be identified by a
	 * name.  By doing so, various components in a system can access the same analysis by agreeing on a name for the
	 * analysis.</p>
	 *
	 * @param name the name of the instance of the analysis.
	 * @return the instance of the analyzer object corresonding to the given name.  If none exists, <code>null</code> is
	 * returned.
	 */
	public final static AbstractAnalyzer getAnalyzer(String name) {
		BFA temp = BFA.getBFA(name);
		AbstractAnalyzer ret = null;
		if (temp != null) {
			ret = temp.analyzer;
		} // end of if (temp != null)
		return ret;
	}

	/**
	 * <p>Returns the set of values associated with the given array type in the context given by
	 * <code>this.context</code>.</p>
	 *
	 * @param a the array type for which the values are requested.
	 * @return the collection of values associated with <code>a</code> in <code>this.context</code>.
	 */
	public final Collection getValues(ArrayType a) {
		ArrayVariant v = bfa.queryArrayVariant(a);
		Collection temp = unmodifiableEmptyCollection;
		if (v != null) {
			temp = v.getFGNode().getValues();
		} // end of if (v != null)
		return temp;
	}

	/**
	 * <p>Returns the set of values associated with the given parameter reference in the context given by
	 * <code>this.context</code>.</p>
	 *
	 * @param p the parameter reference for which the values are requested.
	 * @return the collection of values associated with <code>p</code> in <code>this.context</code>.
	 */
	public final Collection getValues(ParameterRef p) {
		MethodVariant mv = bfa.queryMethodVariant((SootMethod)context.getCurrentMethod());
		Collection temp = unmodifiableEmptyCollection;
		if (mv != null) {
			temp = mv.queryParameterNode(p.getIndex()).getValues();
		} // end of if (v != null)
		return temp;
	}

	/**
	 * <p>Returns the set of values associated with the given field in the context given by <code>this.context</code>.</p>
	 *
	 * @param sf the field for which the values are requested.
	 * @return the collection of values associated with <code>sf</code> in <code>this.context</code>.
	 */
	public final Collection getValues(SootField sf) {
		FieldVariant fv = bfa.queryFieldVariant(sf);
		Collection temp = unmodifiableEmptyCollection;
		if (fv != null) {
			temp = fv.getValues();
		} // end of if (v != null)
		return temp;
	}

	/**
	 * <p>Returns the set of values associated with the given <code>this</code> variable in the context given by
	 * <code>this.context</code>.</p>
	 *
	 * @param t the <code>this</code> variable for which the values are requested.
	 * @return the collection of values associated with <code>sf</code> in <code>this.context</code>.
	 */
	public final Collection getValues(ThisRef t) {
		MethodVariant mv = bfa.queryMethodVariant((SootMethod)context.getCurrentMethod());
		Collection temp = unmodifiableEmptyCollection;
		if (mv != null) {
			temp = mv.queryThisNode().getValues();
		} // end of if (v != null)
		return temp;
	}

	/**
	 * <p>Returns the set of values associated with the given AST node in the context given by <code>this.context</code>.</p>
	 *
	 * @param v the AST node for which the values are requested.
	 * @return the collection of values associted with <code>v</code> in <code>this.context</code>.
	 */
	public final Collection getValues(Value v) {
		logger.debug(context.getCurrentMethod() + "\n" + context);
		//ValueBox temp = context.setProgramPoint(null);
		MethodVariant mv = bfa.queryMethodVariant((SootMethod)context.getCurrentMethod());
		//context.setProgramPoint(temp);
		Collection temp = unmodifiableEmptyCollection;
		if (mv != null) {
			 ASTVariant astv = mv.getASTVariant(v, context);
			 if (astv != null) {
				  temp = astv.getFGNode().getValues();
			 } // end of if (astv != null)
		}
		return temp;
	}

	/**
	 * <p>Reset the analyzer so that fresh run of the analysis can occur.  This is intended to be called by the environment to
	 * reset the analysis.</p>
	 *
	 */
	public final void reset() {
		resetAnalysis();
		bfa.reset();
	}

	/**
	 * <p>Reset the analyzer so that a fresh run of the analysis can occur.  This is intended to be overridden by the
	 * subclasses to reset analysis specific data structures.  It shall be called before the framework data structures are
	 * initialized.</p>
	 *
	 */
	protected final void resetAnalysis() {
	}

}// AbstractAnalyzer
