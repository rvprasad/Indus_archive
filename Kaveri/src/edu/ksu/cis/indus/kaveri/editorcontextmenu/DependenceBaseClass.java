/*
 * Created on Sep 26, 2004
 *
 * TODO Create a unified dependence class.
 */
package edu.ksu.cis.indus.kaveri.editorcontextmenu;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.SootMethod;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis;
import edu.ksu.cis.indus.tools.slicer.SlicerConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * Has the base functions used by the dependence classes.
 * @author ganeshan
 *
 */
public class DependenceBaseClass {	
	
	
	/** 
	 * Returns the list of statements linked by the dependence.
	 * @param _method
	 * @param _stmt
	 * @param dependenceType The dependecy
	 * @return List The list of dependees
	 */
	public List handleDependees(SootMethod _method, Stmt _stmt, final Object dependenceType) {		
		final EclipseIndusDriver _driver = KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver();
		final SlicerTool _stool = _driver.getSlicer();
		final SlicerConfiguration _config = (SlicerConfiguration) _stool.getActiveConfiguration();
		final List _lst = new LinkedList();
		// IDependencyAnalysis.CONTROL_DA
		Collection _coll = _config.getDependenceAnalyses(dependenceType);
		if (_coll != null) {
			Iterator it = _coll.iterator();
			while (it.hasNext()) {
			 AbstractDependencyAnalysis _crt = (AbstractDependencyAnalysis)	 it.next();		 
			 Collection ct =  _crt.getDependees(_stmt, _method);		 
			 Iterator stit = ct.iterator();
			 while(stit.hasNext()) {
			 	_lst.add(stit.next());
			 }
			}	
		}
		 	
		return _lst;
	}

	/** 
	 * Returns the list of statements linked by the dependence.
	 * @param _method
	 * @param _stmt
	 * @param dependenceType The dependecy
	 * @return List The list of dependent statements.
	 */
	public List handleDependents(SootMethod _method, Stmt _stmt, final Object dependenceType) {		
		final EclipseIndusDriver _driver = KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver();
		final SlicerTool _stool = _driver.getSlicer();
		final SlicerConfiguration _config = (SlicerConfiguration) _stool.getActiveConfiguration();
		final List _lst = new LinkedList();
		// IDependencyAnalysis.CONTROL_DA
		Collection _coll = _config.getDependenceAnalyses(dependenceType);
		if (_coll != null) {
			Iterator it = _coll.iterator();
			while (it.hasNext()) {
			 AbstractDependencyAnalysis _crt = (AbstractDependencyAnalysis)	 it.next();		 
			 Collection ct =  _crt.getDependents(_stmt, _method);		 
			 Iterator stit = ct.iterator();
			 while(stit.hasNext()) {
			 	_lst.add(stit.next());
			 }
			}	
		}
		return _lst;
	}

}
