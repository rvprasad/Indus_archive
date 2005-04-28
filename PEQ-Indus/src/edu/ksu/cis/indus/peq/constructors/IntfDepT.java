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
 
package edu.ksu.cis.indus.peq.constructors;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.peq.fsm.FSMToken;
import edu.ksu.cis.indus.peq.graph.Edge;
import edu.ksu.cis.indus.peq.graph.Node;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * @author ganeshan
 *
 * This represents a control dependee constructor.
 */
public class IntfDepT extends GeneralConstructor {

    /* (non-Javadoc)
     * @see edu.ksu.cis.indus.peq.constructors.GeneralConstructor#match(edu.ksu.cis.indus.peq.constructors.GeneralConstructor)
     */
    public IFSMToken match(GeneralConstructor cons, final Edge masterEdge) throws IllegalAccessException {
        final FSMToken _token = new FSMToken();
        
        if (cons instanceof IntfDepT) {
            if (this.isVariablePresent() && cons.isVariablePresent()) {
                throw new IllegalAccessException("Both the constructors can't have labels");
            }            
            
            final Pair _pair = (Pair) ((Node) masterEdge.getSrcNode()).getInformation();
            final Stmt _dsdt = (DefinitionStmt) _pair.getFirst();
            Object _val = null;
            if (_dsdt.containsArrayRef()) {
                _val = _dsdt.getArrayRef().getBase(); // TODO - Check for correctness of this thing.
            } else if (_dsdt.containsFieldRef()) {
                _val = _dsdt.getFieldRef().getField();
            } else {
                throw new IllegalArgumentException("Wierd value encoutered");
            }
            // Check for wildcards.
            if (!cons.getVariableName().equals("_")) {                            
                _token.getSubstituitionMap().put(cons.getVariableName(), _val);
            }
        } else {
            _token.setEmpty(true);
        }
        return _token;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof IntfDepT)) {
            return false;
        }
        IntfDepT rhs = (IntfDepT) object;
        return super.equals(rhs);
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
    	int _result = 17;
        _result = _result * 37 + 7; 
        _result = _result * 37 + super.hashCode();
        return _result;
    }
}
