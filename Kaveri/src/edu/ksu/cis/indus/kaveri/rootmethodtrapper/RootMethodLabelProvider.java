/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
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

