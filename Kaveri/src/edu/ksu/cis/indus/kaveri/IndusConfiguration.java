
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

import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.PartialStmtData;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


//import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
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
	 * The set of criteria.
	 */
	private List criteria;

	/**
	 * The list of statements for partial slice view.
	 */	
	private PartialStmtData stmtList;

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
		eclipseIndusDriver = new EclipseIndusDriver();

		if (!additive) {
		//	System.out.println("Resetting");
			indusAnnotationManager.reset();
		}
		sliceFileList.clear();
		stmtList.setStmtList(null);
		//criteria.clear();
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
}
