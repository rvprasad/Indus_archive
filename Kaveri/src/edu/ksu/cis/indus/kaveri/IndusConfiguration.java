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
import edu.ksu.cis.indus.kaveri.callgraph.ContextRepository;
import edu.ksu.cis.indus.kaveri.callgraph.MethodCallContext;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;
import edu.ksu.cis.indus.kaveri.views.DependenceHistoryData;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This class is used to temporarily store the various parameters to be used for
 * the slice. It acts as a global slice settings holder.
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
     * The collection of chosen contexts.
     */
    private Collection chosenContext;

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
     * The context repository.
     */
    private ContextRepository ctxRepository;

    /**
     * Residualize the scene.
     */
    private boolean doResidualize;
    
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
    private Set depLinkSet = new HashSet();

    /**
     * <p>
     * The number of times a slice has been run.
     * </p>
     */
    private int nNoOfSlicesRun;

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
        sliceDecorator = AbstractUIPlugin.imageDescriptorFromPlugin(
                "edu.ksu.cis.indus.kaveri", "data/icons/indus-decorator.gif");
        sliceFileList = new LinkedList();
        criteria = new ArrayList();
        stmtList = new PartialStmtData();
        depHistory = new DependenceHistoryData();
        selectedStatement = "            ";
        rManager = new ResourceManager();
        scopeMap = new HashMap();
        scopeSpecification = "";
        ctxRepository = new ContextRepository();
        chosenContext = new ArrayList();
        nNoOfSlicesRun = 0;
    }

    /**
     * set / reset additive slicing.
     * 
     * @param badditive
     *            The additive to set.
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
     * @param criterialist
     *            The criteria to set. Precondition:
     *            CriteriaList.firstElement.kindOf(String: classname)
     *            CriteriaList.secondElement.kindOf(String: methodname)
     *            CriteriaList.thirdElement.kindOf(int: linenumber)
     *            CriteriaList.fourthElement.kindOf(int: jimple index)
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
     * @param cConfiguration
     *            The currentConfiguration to set.
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
     * @param lineNumbersMap
     *            The map of classnames to line numbers.
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
     * Returns the slice image decoration.
     * 
     * @return Returns the sliceDecorator.
     */
    public ImageDescriptor getSliceDecorator() {
        return sliceDecorator;
    }

    /**
     * Sets the list of files chosen for slicing. Used by the decorator to tag
     * the files in the display.
     * 
     * @param sliceList
     *            The sliceFileList to set.
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
        lineNumbers = null;
        selectedStatement = "         ";

        if (!additive) {
            //	System.out.println("Resetting");
            indusAnnotationManager.reset();
        }
        sliceFileList.clear();
        sliceProject = null;
        stmtList.setStmtList(null);
        //criteria.clear();
        KaveriPlugin.getDefault().reset();
        depHistory.reset();
        //ctxRepository.reset();
        //chosenContext.clear();
    }

    /**
     * Reset the chosen contexts.
     *  
     */
    public void resetChosenContext() {
        chosenContext.clear();
    }

    /**
     * Returns the set of statements.
     * 
     * @return Returns the stmtList.
     */
    public PartialStmtData getStmtList() {
        return stmtList;
    }

    /**
     * Sets the list of statements.
     * 
     * @param stmtsList
     *            The stmtList to set.
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
     * @param project
     *            The sliceProject to set.
     */
    public void setSliceProject(final IProject project) {
        this.sliceProject = project;
    }

    /**
     * @return Returns the selectedStatement.
     */
    public String getSelectedStatement() {
        return selectedStatement;
    }

    /**
     * @param statement
     *            The selectedStatement to set.
     */
    public void setSelectedStatement(final String statement) {
        this.selectedStatement = statement;
    }

    /**
     * @return Returns the depHistory.
     */
    public DependenceHistoryData getDepHistory() {
        return depHistory;
    }

    /**
     * @param history
     *            The depHistory to set.
     */
    public void setDepHistory(final Pair history) {
        this.depHistory.addHistory(history);
    }

    /**
     * @return Returns the depLinkSet.
     */
    public Set getDepLinkSet() {
        return depLinkSet;
    }

    /**
     * @param line
     *            The line number to add.
     */
    public void addToDepLinkSet(final Object line) {
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
     * 
     * @param method
     *            The java method to add to the scope.
     */
    public void addToScopeMap(final IMethod method) {
        final IJavaElement _class = method.getParent();
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
     * @param specification
     *            The scopeSpecification to set.
     */
    public void setScopeSpecification(final String specification) {
        this.scopeSpecification = specification;
    }

    /**
     * Reset the slice count.
     *  
     */
    public void resetSliceCount() {
        nNoOfSlicesRun = 0;
    }

    /**
     * Adds the given context to the repository.
     * 
     * @param context
     *            The context
     */
    public void addContext(final IJavaProject project,  final MethodCallContext context) {
        ctxRepository.addCallStack(project, context);
    }

    /**
     * Returns the repository instance.
     * 
     * @return Returns the ctxRepository.
     */
    public ContextRepository getCtxRepository() {
        return ctxRepository;
    }

    /**
     * Get the chosen context.
     * 
     * @return Collection The set of contexts.
     */
    public Collection getChosenContext() {
        return chosenContext;
    }

    /**
     * Adds the given contexts to the chosen list.
     * 
     * @pre mychosenContext.oclIsKindOf(Collection(MethodContext))
     * @param myChosenContext
     *            The collection of contexts.
     */
    public void addToChosenContext(final Collection myChosenContext) {
        chosenContext.addAll(myChosenContext);
    }
    /**
     * @return Returns the doResidualize.
     */
    public boolean isDoResidualize() {
        return doResidualize;
    }
    /**
     * @param doResidualize The doResidualize to set.
     */
    public void setDoResidualize(boolean doResidualize) {
        this.doResidualize = doResidualize;
    }
}