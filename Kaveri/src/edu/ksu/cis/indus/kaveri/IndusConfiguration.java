
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
 * Created on Apr 1, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;
import edu.ksu.cis.indus.kaveri.views.DependenceHistoryData;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


//import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * This class is used to temporarily store the various parameters to be used 
 * for the slice. It acts as a global slice settings holder.
 *
 * @author Ganeshan
 */
public class IndusConfiguration {
	/** 
	 * <p>
	 * The Indus Annotation manager instance.
	 * </p>
	 * 
	 */
	private AddIndusAnnotation indusAnnotationManager;

	/** 
	 * <p>
	 * The eclipse indus driver.
	 * </p>
	 */
	private EclipseIndusDriver eclipseIndusDriver;

	/** 
	 * The slice decorator.
	 */
	private ImageDescriptor sliceDecorator;

	/**
	 * The selected line for viewing the slice.
	 * 	
	 */
	private String selectedStatement;
	
	/**
	 * The project which has been recently sliced.
	 */
	private IProject sliceProject;
	/** 
	 * The set of criteria.
	 */
	private List criteria;

	/**
	 * Maintains a cache of the scoped elements.
	 */
	private Map scopeMap;
	
	/**
	 * Resource Manager. Used to cache system resources.
	 */
	private ResourceManager rManager;
	
	
	/**
	 * Holds the xml representation of the scope.
	 */
	private String scopeSpecification;
	
	/**
	 * The list of statements for partial slice view.
	 */	
	private PartialStmtData stmtList;

	/**
	 * The dependence history.
	 */
	private DependenceHistoryData depHistory;
	/** 
	 * The list of files requested for slicing.
	 */
	private List sliceFileList;

	/** 
	 * The map of classname to line numbers.
	 */
	private Map lineNumbers;

	/** 
	 * The current configuration for the slice. Set by IndusConfigurationDialog.
	 */
	private String currentConfiguration;

	/**
	 * Holds the list of current dependencies tracked.
	 */
	private HashSet depLinkSet = new HashSet();
	/** 
	 * <p>
	 * The output directory.
	 * </p>	 
	 */
	private String outputDirectory = ".";

	/** 
	 * Indicates if additive slicing is to be performed.
	 */
	private boolean additive;

	/**
	 * Creates a new IndusConfiguration object.
	 */
	public IndusConfiguration() {
		eclipseIndusDriver = new EclipseIndusDriver();
		indusAnnotationManager = new AddIndusAnnotation();
		currentConfiguration = "";
		sliceDecorator =
			AbstractUIPlugin.imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
				"data/icons/indus-decorator.gif");
		sliceFileList = new LinkedList();
		criteria = new ArrayList();
		stmtList = new PartialStmtData();
		depHistory = new DependenceHistoryData();
		selectedStatement = "            ";
		rManager = new ResourceManager();
		scopeMap = new HashMap();
		scopeSpecification = "";
	}

	/**
	 * set / reset additive slicing.
	 *
	 * @param badditive The additive to set.
	 */
	public void setAdditive(final boolean badditive) {
		this.additive = badditive;
	}

	/**
	 * Indicates if additive slicing is to be performed.
	 *
	 * @return Returns the additive.
	 */
	public boolean isAdditive() {
		return additive;
	}

	/**
	 * Sets the criteria.
	 *
	 * @param criterialist The criteria to set. 
	 * Precondition: CriteriaList.firstElement.kindOf(String: classname)
	 * 		  CriteriaList.secondElement.kindOf(String: methodname) CriteriaList.thirdElement.kindOf(int: linenumber)
	 * 		  CriteriaList.fourthElement.kindOf(int: jimple index)
	 */
	public void setCriteria(final Criteria criterialist) {
		this.criteria.add(criterialist);
	}

	/**
	 * Returns the set of chosen criteria.
	 *
	 * @return Returns the criteria.
	 */
	public List getCriteria() {
		return criteria;
	}

	/**
	 * Sets the current configuration to be used.
	 *
	 * @param cConfiguration The currentConfiguration to set.
	 */
	public void setCurrentConfiguration(final String cConfiguration) {
		this.currentConfiguration = cConfiguration;
	}

	/**
	 * Returns the current configuration.
	 *
	 * @return Returns the currentConfiguration.
	 */
	public String getCurrentConfiguration() {
		return currentConfiguration;
	}

	/**
	 * <p>
	 * Returns the eclipse indus driver instance.
	 * </p>	 
	 *
	 * @return Returns the eclipseIndusDriver.
	 */
	public EclipseIndusDriver getEclipseIndusDriver() {
		return eclipseIndusDriver;
	}

	/**
	 * <p>
	 * Returns the Indus annotation manager instance.
	 * </p>
	 *
	 * @return Returns the indusAnnotationManager.
	 */
	public final AddIndusAnnotation getIndusAnnotationManager() {
		return indusAnnotationManager;
	}

	
	/**
	 * Sets the line number map.
	 *
	 * @param lineNumbersMap The map of classnames to line numbers.
	 */
	public void setLineNumbers(final Map lineNumbersMap) {
		this.lineNumbers = lineNumbersMap;
	}

	/**
	 * Returns the line number map.
	 *
	 * @return Returns the lineNumbers.
	 */
	public Map getLineNumbers() {
		return lineNumbers;
	}

	/**
	 * <p>
	 * Sets the output directory to  the given directory.
	 * </p>	 
	 *
	 * @param outputdir The output directory
	 */
	public void setOutputDirectory(final String outputdir) {
		outputDirectory = outputdir;
	}

	/**
	 * <p>
	 * Returns the current output directory.
	 * </p>	 
	 *
	 * @return Returns the output directory.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

	

	/**
	 * Returns the slice image decoration.
	 *
	 * @return Returns the sliceDecorator.
	 */
	public ImageDescriptor getSliceDecorator() {
		return sliceDecorator;
	}

	/**
	 * Sets the list of files chosen for slicing.
	 * Used by the decorator to tag the files in the display.
	 * @param sliceList The sliceFileList to set.
	 */
	public void setSliceFileList(final List sliceList) {
		this.sliceFileList.addAll(sliceList);
	}

	/**
	 * Returns the list of files chosen for slicing.
	 *
	 * @return Returns the sliceFileList.
	 */
	public List getSliceFileList() {
		return sliceFileList;
	}

	/**
	 * <p>
	 * Reset the internal variables to their defaults.
	 * </p>
	 */
	public void reset() {
		//selectedClasses = null;
		outputDirectory = ".";
		lineNumbers = null;
		selectedStatement = "         ";

		if (!additive) {
		//	System.out.println("Resetting");
			indusAnnotationManager.reset();
		}
		sliceFileList.clear();
		sliceProject = null;
		stmtList.setStmtList(null);
		criteria.clear();	
		KaveriPlugin.getDefault().reset();
		depHistory.reset();		
	}
	/**
	 * Returns the set of statements.
	 * @return Returns the stmtList.
	 */
	public PartialStmtData getStmtList() {
		return stmtList;
	}
	/**
	 * Sets the list of statements.
	 * @param stmtsList The stmtList to set.
	 */
	public void setStmtList(final List stmtsList) {
		this.stmtList.setStmtList(stmtsList);
	}
	/**
	 * @return Returns the sliceProject.
	 */
	public IProject getSliceProject() {
		return sliceProject;
	}
	/**
	 * @param sliceProject The sliceProject to set.
	 */
	public void setSliceProject(IProject sliceProject) {
		this.sliceProject = sliceProject;
	}
	/**
	 * @return Returns the selectedStatement.
	 */
	public String getSelectedStatement() {
		return selectedStatement;
	}
	/**
	 * @param selectedStatement The selectedStatement to set.
	 */
	public void setSelectedStatement(String selectedStatement) {
		this.selectedStatement = selectedStatement;
	}
	/**
	 * @return Returns the depHistory.
	 */
	public DependenceHistoryData getDepHistory() {
		return depHistory;
	}
	/**
	 * @param history The depHistory to set.
	 */
	public void setDepHistory(Pair history) {
		this.depHistory.addHistory(history);
	}
	/**
	 * @return Returns the depLinkSet.
	 */
	public HashSet getDepLinkSet() {
		return depLinkSet;
	}
	/**
	 * @param line The line number to add.
	 */
	public void addToDepLinkSet(Object line) {
		depLinkSet.add(line);
	}
	/**
	 * @return Returns the rManager.
	 */
	public ResourceManager getRManager() {
		return rManager;
	}
	/**
	 * @return Returns the scopeMap.
	 */
	public Map getScopeMap() {
		return scopeMap;
	}
	/**
	 * Adds the given method to the scope cache.
	 * @param method The java method to add to the scope.
	 */
	public void addToScopeMap(IMethod method) {
		final IJavaElement _class =  method.getParent();
	    if (_class != null) {
	    	if (scopeMap.containsKey(_class)) {
	    		final Set _set = (Set) scopeMap.get(_class);
	    		_set.add(method);
	    	} else {
	    		final Set _set = new HashSet();
	    		_set.add(method);
	    		scopeMap.put(_class, _set);
	    	}
	    }
	}
	/**
	 * @return Returns the scopeSpecification.
	 */
	public String getScopeSpecification() {
		return scopeSpecification;
	}
	/**
	 * @param scopeSpecification The scopeSpecification to set.
	 */
	public void setScopeSpecification(String scopeSpecification) {
		this.scopeSpecification = scopeSpecification;
	}
}
