
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
 * Created on Aug 2, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.ksu.cis.indus.common.datastructures.Pair;


/**
 * This class maintains the set of partial jimple statements
 * for the chosen java statement. This acts as the domain model
 * for the partial slice view.
 *
 * @author ganeshan 
 */
public class DependenceHistoryData {
	/**
	 * The list of Jimple statements.
	 */
	private Stack dependenceHistory;
	
	/**
	 * The viewers listening to this model.
	 */
	private List listeners;
	
	/**
	 * Constructor.
	 *
	 */
	public DependenceHistoryData() {
		listeners = new ArrayList();
		dependenceHistory =  new Stack();
	}
	/**
	 * @return Returns the stmtList.
	 */
	public Stack getHistory() {
		return dependenceHistory;
	}
	/**
	 * @param history The current history.
	 */
	public void addHistory(final Pair history) {
		dependenceHistory.add(history);		
		if (! dependenceHistory.isEmpty()) {
			for (int _i = 0; _i < listeners.size(); _i++) {
				((IDeltaListener) listeners.get(_i)).propertyChanged();
			}	
		}				
	}
	
	/**
	 * Adds the listener to notify in case of change.
	 * @param listener The objects interested in viewing 
	 * the data
	 */
	public void addListener(final IDeltaListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Resets the stack.
	 *
	 */
	public void reset() {
	 dependenceHistory.clear();
	 for (int _i = 0; _i < listeners.size(); _i++) {
		((IDeltaListener) listeners.get(_i)).propertyChanged();
	 }	
	}
	/**
	 * Removes the listener.
	 * @param listener The listener to remove. 
	 * the data
	 */
	public void removeListener(final IDeltaListener listener) {
		listeners.remove(listener);
	}
}
