
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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;
import edu.ksu.cis.indus.staticanalyses.dependency.ControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DivergenceDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.SynchronizationDA;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerConfigurationView
  implements ToolConfigurationView,
	  ToolIconView {
	/**
	 * The collection of ids of the dependences to be considered for slicing.
     * @invariant dependencesToUse.oclIsKindOf(String)
	 */
	final Collection dependencesToUse = new HashSet();

	/**
	 * This maps IDs to dependency analyses.
	 *
	 * @invariant id2dependencyAnalysis.oclIsKindOf(Map(Object, DependencyAnalysis))
	 */
	Map id2dependencyAnalysis = new HashMap();

	/**
	 * This indicates if interprocedual divergence dependence should be used.
	 */
	boolean interProceduralDivergenceDA = false;

	/**
	 * This indicates if interprocedual ready dependence should be used.
	 */
	boolean interProceduralReadyDA = false;

	/**
	 * This indicates which rules should be used for ready dependence calculation.
	 */
	int readyDARules = ReadyDAv1.RULE_1 | ReadyDAv1.RULE_2 | ReadyDAv1.RULE_3 | ReadyDAv1.RULE_4;

	/**
	 * Creates a new SlicerConfigurationView object.
	 */
	SlicerConfigurationView() {
		id2dependencyAnalysis = new HashMap();
		id2dependencyAnalysis.put(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA, new IdentifierBasedDataDA());
		id2dependencyAnalysis.put(DependencyAnalysis.SYNCHRONIZATION_DA, new SynchronizationDA());
		id2dependencyAnalysis.put(DependencyAnalysis.READY_DA, new ReadyDAv1());
		id2dependencyAnalysis.put(DependencyAnalysis.INTERFERENCE_DA, new InterferenceDAv1());
		id2dependencyAnalysis.put(DependencyAnalysis.CONTROL_DA, new ControlDA());
		id2dependencyAnalysis.put(DependencyAnalysis.DIVERGENCE_DA, new DivergenceDA());
		id2dependencyAnalysis.put(DependencyAnalysis.REFERENCE_BASED_DATA_DA, new ReferenceBasedDataDA());
		dependencesToUse.addAll(id2dependencyAnalysis.keySet());
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getActiveIcon()
	 */
	public Object getActiveIcon() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getDisabledIcon()
	 */
	public Object getDisabledIcon() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getIcon()
	 */
	public Object getIcon() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getText()
	 */
	public String getText() {
		return "Configure Slicer";
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getToolTipText()
	 */
	public String getToolTipText() {
		return "Slicer config";
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolConfigurationView#configure()
	 */
	public void configure() {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	String get() {
        // use jibx or castor engines to implement this and set(String) as java object to xml document translation
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param configuration DOCUMENT ME!
	 */
	void set(final String configuration) {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/09/16 07:52:25  venku
   - coding convention.

   Revision 1.1  2003/09/15 08:55:23  venku
   - Well, the SlicerTool is still a mess in my opinion as it needs
     to be implemented as required by Bandera.  It needs to be
     much richer than it is to drive the slicer.
   - SlicerConfigurationView is supposed to bridge the above gap.
     I doubt it.

 */
