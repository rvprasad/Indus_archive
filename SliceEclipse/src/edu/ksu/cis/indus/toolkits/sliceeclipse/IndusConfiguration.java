
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
package edu.ksu.cis.indus.toolkits.sliceeclipse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.ksu.cis.indus.toolkits.eclipse.EclipseIndusDriver;
import edu.ksu.cis.indus.toolkits.sliceeclipse.presentation.AddIndusAnnotation;


/**
 * Holds the configuration for the current run for: Output Directory Selected Classes.
 *
 * @author Ganeshan
 */
public class IndusConfiguration {
	/**
	 * <p>
	 * The Indus Annotation manager
	 * </p>
	 * .
	 */
	private AddIndusAnnotation indusAnnotationManager;

//	/** 
//	 * <p>The current java editor.</p>
//	 */
//	private CompilationUnitEditor editor;

	/**
	 * The list of files requested for slicing.
	 */
	private List sliceFileList;
	
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
	 * Creates a new IndusConfiguration object.
	 */
	private HashMap lineNumbers;	

	/**
	 * <p>
	 * The Selected Classes
	 * </p>
	 * .
	 */
	private List selectedClasses;

	/**
	 * The criteria.
	 */
	private ArrayList criteria; 
	
	/**
	 * The current configuration for the slice. Set by IndusConfigurationDialog
	 */
	private String currentConfiguration;

	/**
	 * <p>
	 * The output directory
	 * </p>
	 * .
	 */
	private String outputDirectory = ".";

	/**
	 * Creates a new IndusConfiguration object.
	 */
	public IndusConfiguration() {
		eclipseIndusDriver = new EclipseIndusDriver();
		indusAnnotationManager = new AddIndusAnnotation();
		currentConfiguration = "";
		sliceDecorator = AbstractUIPlugin.imageDescriptorFromPlugin(
				"edu.ksu.cis.indus.toolkits.sliceEclipse",
				"data/icons/indus-decorator.gif");
		sliceFileList = new LinkedList();
		criteria = new ArrayList();
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
	 * Returns the eclipse indus driver
	 * </p>
	 * .
	 *
	 * @return Returns the eclipseIndusDriver.
	 */
	public EclipseIndusDriver getEclipseIndusDriver() {
		return eclipseIndusDriver;
	}

	/**
	 * <p>
	 * Returns the Indus annotation manager.
	 * </p>
	 *
	 * @return Returns the indusAnnotationManager.
	 */
	public final AddIndusAnnotation getIndusAnnotationManager() {
		return indusAnnotationManager;
	}

//	/**
//	 * Sets the editor to the given editor.
//	 *
//	 * @param javaeditor The editor to set.
//	 */
//	public void setEditor(final CompilationUnitEditor javaeditor) {
//		this.editor = javaeditor;
//	}
//
//	/**
//	 * Returns the current java editor.
//	 *
//	 * @return Returns the editor.
//	 */
//	public CompilationUnitEditor getEditor() {
//		return editor;
//	}

	/**
	 * Sets the line numbers.
	 *
	 * @param lineNumbersMap The map of classnames to line numbers.
	 */
	public void setLineNumbers(final HashMap lineNumbersMap) {
		this.lineNumbers = lineNumbersMap;
	}

	/**
	 * Returns the line numbers.
	 *
	 * @return Returns the lineNumbers.
	 */
	public HashMap getLineNumbers() {
		return lineNumbers;
	}

	/**
	 * <p>
	 * Sets the output directory to  the given directory
	 * </p>
	 * .
	 *
	 * @param outputdir The output directory
	 */
	public void setOutputDirectory(final String outputdir) {
		outputDirectory = outputdir;
	}

	/**
	 * <p>
	 * Returns the current output directory
	 * </p>
	 * .
	 *
	 * @return Returns the output directory.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * Sets the classes.
	 *
	 * @param classes The vector of classes to slice
	 *
	 * @deprecated <p>
	 */
	public void setSelectedClasses(final List classes) {
		this.selectedClasses = classes;
	}

	/**
	 * <p>
	 * Returns the list of slected classes
	 * </p>
	 * .
	 *
	 * @return Vector The vector of seleted classes
	 */
	public List getSelectedClasses() {
		return selectedClasses;
	}

	/**
	 * Resets the private variables.
	 * 
	 * <p>
	 * Reset the internal variables to defaults
	 * </p>
	 */
	public void reset() {
		selectedClasses = null;
		outputDirectory = ".";
		lineNumbers = null;
		eclipseIndusDriver = new EclipseIndusDriver();
		indusAnnotationManager.reset();
		sliceFileList.clear();
		//criteria.clear();
	}
	
	/**
	 * Returns the slice image decoration.
	 * @return Returns the sliceDecorator.
	 */
	public  ImageDescriptor getSliceDecorator() {
		return sliceDecorator;
	}
	
	/**
	 * Returns the list of files chosen for slicing.
	 * @return Returns the sliceFileList.
	 */
	public List getSliceFileList() {
		return sliceFileList;
	}
	
	/**
	 * Sets the list of files chosen for slicing.
	 * @param sliceList The sliceFileList to set.
	 */
	public void setSliceFileList(final List sliceList) {
		this.sliceFileList.addAll(sliceList);		
	}
	
	/**
	 * Returns the criteria chosen.
	 * @return Returns the criteria.
	 */
	public ArrayList getCriteria() {
		return criteria;
	}
	
	/**
	 * Sets the criteria.
	 * @param criterialist The criteria to set.
	 * Precondition: CriteriaList.firstElement.kindOf(String: classname)
	 * CriteriaList.secondElement.kindOf(String: methodname)
	 * CriteriaList.thirdElement.kindOf(int: linenumber)
	 * CriteriaList.fourthElement.kindOf(int: jimple index)
	 */
	public void setCriteria(final ArrayList criterialist) {
		this.criteria.add(criterialist);
	}
}
