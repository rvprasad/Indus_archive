/*
 * Symbol.java
 *
 * Created on April 17, 2005, 5:38 PM
 */

package edu.ksu.cis.automata.entities;

import edu.ksu.cis.automata.interfaces.ISymbol;

/**
 * The abstract symbol entity.
 * @author ganeshan
 */
public abstract class Symbol implements ISymbol {
    
    /**
     * The name for this symbol.
     */
    private String symName;
    
    /**
     * The value corresponding to this symbol.
     */
    private Object value;
    
    /** Creates a new instance of Symbol 
     * @param name The name of the symbol.
     * @param val The value attached to the symbol.
     */
    public Symbol(final String name, final Object val) {
        symName = name;
        this.value = val;
    }

    /**
     * Returns the value associated with this label.
     * @return Object The value attached to the label.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns a name for this symbol as a humanly identifiable string.
     * @return String The name for this symbol.
     */
    public String getName() {
        return symName;
    }
    
}
