
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

/*
 * Created on Jun 17, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.editorcontextmenu.dependence;

import java.util.List;

import org.eclipse.ui.IEditorActionDelegate;

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import soot.SootMethod;
import soot.jimple.Stmt;


/**
 * Tracks Backward control Dependence.
 *
 * @author Ganeshan 
 */
public class SynchronizationPredecessor extends DependenceBaseClass
  implements IEditorActionDelegate
	{
	/** (non-Javadoc)
	 * @see edu.ksu.cis.indus.kaveri.editorcontextmenu.dependence.DependenceBaseClass#handleDependence(soot.SootMethod, soot.jimple.Stmt)
	 */
	protected List handleDependence(SootMethod method, Stmt stmt) {
		return handleDependees(method, stmt, IDependencyAnalysis.SYNCHRONIZATION_DA);
	}

	/* (non-Javadoc)
	 * @see edu.ksu.cis.indus.kaveri.editorcontextmenu.dependence.DependenceBaseClass#getDependenceInfo()
	 */
	protected String getDependenceInfo() {
		return "Synchronization Dependee";
	}

	/* (non-Javadoc)
	 * @see edu.ksu.cis.indus.kaveri.editorcontextmenu.dependence.DependenceBaseClass#getDependenceAnnotationKey()
	 */
	protected String getDependenceAnnotationKey() {
		return "indus.slice.SynchronizationDependencehighlightAnnotation";
	}
}
