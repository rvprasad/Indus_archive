/*
 *
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
 
package edu.ksu.cis.indus.kaveri.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.VoidType;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.RootMethodTrapper;
import edu.ksu.cis.indus.kaveri.common.SECommons;

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
