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
 
package edu.ksu.cis.indus.kaveri.dependence;

import java.util.ArrayList;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
            RightPaneTreeObject child = (RightPaneTreeObject) children
                    .get(i);
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
