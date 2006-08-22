package edu.ksu.cis.indus.dom;

import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;

public final class SlicerDOMFactory implements IMonkeyDOMFactory {

    public Object getDOMroot() {
        return new SlicerDOM();
    }

}
