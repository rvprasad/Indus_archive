package edu.ksu.cis.indus.dom;

import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public final class SlicerDOMFactory implements IMonkeyDOMFactory {

    public Object getDOMroot() {
        return new SlicerDOM();
    }

}
