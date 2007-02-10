/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.dom;

import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public final class SlicerDOMFactory implements IMonkeyDOMFactory {

    public Object getDOMroot() {
        return new SlicerDOM();
    }

}
