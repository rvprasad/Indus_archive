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
 
package edu.ksu.cis.indus.kaveri.rootmethodtrapper;

import edu.ksu.cis.indus.common.datastructures.Pair;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author ganeshan
 *
 * Provides the label for the root method view.
 */
public class RootMethodLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
        String _retString = "";
        if (obj instanceof Pair) {
            switch (index) {
            	case 0:
            	    break;
            	case 1:
            	    _retString = ((Pair) obj).getFirst().toString();
            	    break;
            	case 2:
            	    _retString = ((Pair) obj).getSecond().toString();
            	    break;
            	default:
            	    break;
            }
        } 
        return _retString;
    }

    public Image getColumnImage(Object obj, int index) {
        return null;
    }

    public Image getImage(Object obj) {
        return null;
    }
}

