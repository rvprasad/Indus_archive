/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
package edu.ksu.cis.indus.kaveri.driver;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.RootMethodTrapper;
import edu.ksu.cis.indus.kaveri.common.SECommons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.VoidType;

/**
 * @author ganeshan
 *
 * The Kaveri Root Method Trapper can trap additional methods than Indus does.
 */
public class KaveriRootMethodTrapper extends RootMethodTrapper {
    
    private Collection rootMethodCollection;
    
    /**
     * Constructor.
     *
     */
    public KaveriRootMethodTrapper() {
        this.rootMethodCollection = new ArrayList();                
    }
    
    /**
     * Checks if the method is a main method.
     * @param sm The soot method
     * @return boolean Whether the method is a main method.
     */
    private boolean isMainMethod(final SootMethod sm) {
        final boolean _result;
		if (sm.getName().equals("main")
			  && sm.isPublic()
			  && sm.isStatic()
			  && sm.getParameterCount() == 1
			  && sm.getReturnType().equals(VoidType.v())
			  && sm.getParameterType(0).equals(ArrayType.v(RefType.v("java.lang.String"), 1))) {
			_result = true;
		} else {
			_result = false;
		}
		return _result;
    }
    
    public void reset() {
        rootMethodCollection.clear();
    }

    /**
     * Indicates if the given method is a root method.
     */
    protected boolean isThisARootMethod(SootMethod sm) {
        boolean _result = isMainMethod(sm);
        if (!_result && rootMethodCollection.size() > 0) {
            final String _className = sm.getDeclaringClass().getName();
            final String _signature = _className + "." + SECommons.getSearchPattern(sm);
            if (rootMethodCollection.contains(_signature)) {
                _result = true;
            }
        }
        return _result;
    }
    
    /**
     * Adds the given set of root methods as signatures.
     * @param rootMCollection
     */
    public void addRootMethodSignatures(final Collection rootMCollection) {
        for (Iterator iter = rootMCollection.iterator(); iter.hasNext();) {
            final Pair _pair = (Pair) iter.next();
            final String _signature = _pair.getFirst().toString() +  "." + _pair.getSecond().toString();
            rootMethodCollection.add(_signature);
        }
    }
    
}
