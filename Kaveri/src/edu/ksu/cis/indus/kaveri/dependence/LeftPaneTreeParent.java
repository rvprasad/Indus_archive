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