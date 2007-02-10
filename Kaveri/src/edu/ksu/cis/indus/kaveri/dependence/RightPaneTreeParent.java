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
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RightPaneTreeParent extends RightPaneTreeObject {
    private ArrayList children;

    public RightPaneTreeParent(String name) {
        super(name);
        children = new ArrayList();
    }

    public void addChild(RightPaneTreeObject child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(RightPaneTreeObject child) {
        children.remove(child);
        child.setParent(null);
    }

    public void removeAllChildren() {
        for (int i = 0; i < children.size(); i++) {
            RightPaneTreeObject child = (RightPaneTreeObject) children.get(i);
            child.setParent(null);
        }
        children.clear();
    }

    public RightPaneTreeObject[] getChildren() {
        return (RightPaneTreeObject[]) children
                .toArray(new RightPaneTreeObject[children.size()]);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }
}
