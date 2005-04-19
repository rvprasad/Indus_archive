/*
 * Symbol1.java
 *
 * Created on April 17, 2005, 6:34 PM
 */

package edu.ksu.cis.automata.entities.test;

import edu.ksu.cis.automata.entities.Symbol;
import edu.ksu.cis.automata.interfaces.ISymbol;

/**
 * Symbol symbol2.
 * @author ganeshan
 */
public class Symbol2 extends Symbol {
    
    /** Creates a new instance of Symbol1 */
    public Symbol2(final String name, final Object val) {
        super(name, val);
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.automata.interfaces.ISymbol#match(edu.ksu.cis.automata.interfaces.ISymbol)
     */
    public boolean match(ISymbol sym) {
        return equals(sym);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object frnObject) {
       boolean result = true;
       if (frnObject instanceof Symbol2) {
           result = true;
       }
       return result;
    }
}
