/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.dependence;

import java.util.ArrayList;

/**
 * @author ganeshan
 *  
 */
public class LeftPaneTreeParent extends LeftPaneTreeObject {
    private ArrayList children;

    public LeftPaneTreeParent(String name) {
        super(name);
        children = new ArrayList();
    }

    public void addChild(LeftPaneTreeObject child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(LeftPaneTreeObject child) {
        children.remove(child);
        child.setParent(null);
    }

    public void removeAllChildren() {
        for (int i = 0; i < children.size(); i++) {
            LeftPaneTreeObject child = (LeftPaneTreeObject) children.get(i);
            child.setParent(null);
        }
        children.clear();
    }

    public LeftPaneTreeObject[] getChildren() {
        return (LeftPaneTreeObject[]) children
                .toArray(new LeftPaneTreeObject[children.size()]);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }
}
