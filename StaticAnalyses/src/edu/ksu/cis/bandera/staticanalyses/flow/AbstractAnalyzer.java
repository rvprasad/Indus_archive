
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.ParameterRef;
import ca.mcgill.sable.soot.jimple.Value;

import edu.ksu.cis.bandera.staticanalyses.interfaces.Environment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;


//AbstractAnalyzer.java

/**
 * This class represents the central access point for the information calculated in an analysis.  The subclass should extend
 * this class with methods to access various information about the implmented analysis.  This class by itself provides the
 * interface to query generic, low-level analysis information.  These interfaces should be used by implemented components of
 * the framework to extract information during the analysis.  Created: Fri Jan 25 14:49:45 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractAnalyzer {
	/**
	 * The instance of the framework performing the analysis and is being represented by this analyzer object.
	 */
	protected BFA bfa;

	/**
	 * The collection of methods from which the analysis of the system starts.
	 *
	 * @invariant rootMethods->forall(o | o.isOclKind(SootMethod))
	 */
	protected Collection rootMethods;

	/**
	 * The context to be used when analysis information is requested and a context is not provided.
	 */
	protected Context context;

	/**
	 * This field indicates if the analysis is progress or otherwise.
	 */
	protected boolean active;

	/**
	 * Creates a new <code>AbstractAnalyzer</code> instance.
	 *
	 * @param name the name of the analysis to used to identify the corresponding instance of the framework.
	 * @param mf the factory to be used to create the components in the  framework during the analysis.
	 * @param context the context to be used by this analysis instance.
	 */
	protected AbstractAnalyzer(String name, ModeFactory mf, Context context) {
		this.context = context;
		bfa = new BFA(name, this, mf);
		active = false;
		rootMethods = new HashSet();
	}

	/**
	 * Returns the analyser object corresponding to the given name.  Each instance of the framework will be identified by a
	 * name.  By doing so, various components in a system can access the same analysis by agreeing on a name for the
	 * analysis.
	 *
	 * @param name the name of the instance of the analysis.
	 *
	 * @return the instance of the analyzer object corresonding to the given name.  If none exists, <code>null</code> is
	 * 		   returned.
	 */
	public static final AbstractAnalyzer getAnalyzer(String name) {
		BFA temp = BFA.getBFA(name);
		AbstractAnalyzer ret = null;

		if(temp != null) {
			ret = temp._ANALYZER;
		}

		// end of if (temp != null)
		return ret;
	}

	/**
	 * Returns the classes of the system that were analyzed/accessed by this analysis.
	 *
	 * @return a collection of classes.
	 *
	 * @post result->forall(o | o.oclType = ca.mcgill.sable.soot.SootClass)
	 */
	public Collection getClasses() {
		return bfa.getClasses();
	}

	/**
	 * Returns the environment in which the analysis occurred.
	 *
	 * @return the environment in which the analysis occurred.
	 */
	public Environment getEnvironment() {
		return (Environment) bfa;
	}

	/**
	 * Returns the methods from which the analysis started.
	 *
	 * @return a collection of <code>SootMethod</code>s.
	 */
	public final Collection getRoots() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Returns the values associated with exception class for the given invocation expression and <code>this.context</code>.
	 *
	 * @param e is the method invoke expression.
	 * @param exception is the class of the exception thrown by this expression.
	 *
	 * @return the collection of values associated with given exception class and and invoke expression.
	 */
	public final Collection getThrowValues(InvokeExpr e, SootClass exception) {
		MethodVariant mv = bfa.queryMethodVariant((SootMethod) context.getCurrentMethod());
		Collection temp = Collections.EMPTY_SET;

		if(mv != null) {
			InvocationVariant iv = (InvocationVariant) mv.getASTVariant(e, context);

			if(iv != null) {
				temp = iv.queryThrowNode(exception).getValues();
			}
		}
		return temp;
	}

	/**
	 * Returns the set of values associated with the given array type in the context given by <code>this.context</code>.
	 *
	 * @param a the array type for which the values are requested.
	 *
	 * @return the collection of values associated with <code>a</code> in <code>this.context</code>.
	 */
	public final Collection getValues(ArrayType a) {
		ArrayVariant v = bfa.queryArrayVariant(a);
		Collection temp = Collections.EMPTY_SET;

		if(v != null) {
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
	 */
	public final Collection getValues(ParameterRef p) {
		MethodVariant mv = bfa.queryMethodVariant((SootMethod) context.getCurrentMethod());
		Collection temp = Collections.EMPTY_SET;

		if(mv != null) {
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
	 */
	public final Collection getValues(SootField sf) {
		FieldVariant fv = bfa.queryFieldVariant(sf);
		Collection temp = Collections.EMPTY_SET;

		if(fv != null) {
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
	 */
	public final Collection getValues(Value v) {
		MethodVariant mv = bfa.queryMethodVariant((SootMethod) context.getCurrentMethod());
		Collection temp = Collections.EMPTY_SET;

		if(mv != null) {
			ASTVariant astv = mv.queryASTVariant(v, context);

			if(astv != null) {
				temp = astv.getFGNode().getValues();
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
	 */
	public final Collection getValues(Object astChunk, Context ctxt) {
		Context tmpCtxt = context;
		context = ctxt;

		Collection result = Collections.EMPTY_LIST;

		if(astChunk instanceof Value) {
			result = getValues((Value) astChunk);
		} else if(astChunk instanceof SootField) {
			result = getValues((SootField) astChunk);
		} else if(astChunk instanceof ParameterRef) {
			result = getValues((ParameterRef) astChunk);
		} else if(astChunk instanceof ArrayType) {
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
	 */
	public final Collection getValuesForThis(Context ctxt) {
		Context tmpCtxt = context;
		context = ctxt;

		MethodVariant mv = bfa.queryMethodVariant((SootMethod) context.getCurrentMethod());
		Collection temp = Collections.EMPTY_LIST;

		if(mv != null) {
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
	 * @pre root != null
	 */
	public final void analyze(SootClassManager scm, SootMethod root) {
		if(root == null) {
			throw new IllegalStateException("Root method cannot be null.");
		}
		active = true;
		bfa.analyze(scm, root);
		rootMethods.add(root);
		active = false;
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
	 * @pre roots != null and not roots.isEmpty()
	 */
	public final void analyze(SootClassManager scm, Collection roots) {
		if(roots == null || roots.isEmpty()) {
			throw new IllegalStateException("There must be at least one root method to analyze.");
		}

		active = true;

		for(Iterator i = roots.iterator(); i.hasNext();) {
			SootMethod root = (SootMethod) i.next();
			bfa.analyze(scm, root);
			rootMethods.add(root);
		}

		active = false;
	}

	/**
	 * Reset the analyzer so that fresh run of the analysis can occur.  This is intended to be called by the environment to
	 * reset the analysis.
	 */
	public final void reset() {
		resetAnalysis();
		bfa.reset();
		rootMethods.clear();
	}

	/**
	 * Reset the analyzer so that a fresh run of the analysis can occur.  This is intended to be overridden by the subclasses
	 * to reset analysis specific data structures.  It shall be called before the framework data structures are reset.
	 */
	protected final void resetAnalysis() {
	}
}

/*****
 ChangeLog:

$Log$

*****/
