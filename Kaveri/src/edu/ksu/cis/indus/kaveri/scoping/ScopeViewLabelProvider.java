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
 
package edu.ksu.cis.indus.kaveri.scoping;

import edu.ksu.cis.indus.common.scoping.ClassSpecification;
import edu.ksu.cis.indus.common.scoping.FieldSpecification;
import edu.ksu.cis.indus.common.scoping.MethodSpecification;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author ganeshan
 *
 * Provides the label for the scope view.
 */
public class ScopeViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
        if (index == 0) {
            return "";
        }
        if (obj instanceof ClassSpecification) {
            final ClassSpecification _cs = (ClassSpecification) obj;
            switch (index) {
            case 1:
                return "Class";
            case 2:
                return _cs.getName();
            case 3:
                return _cs.getTypeSpec().getNamePattern();
            }
        } else if (obj instanceof MethodSpecification) {
            final MethodSpecification _ms = (MethodSpecification) obj;
            switch (index) {
            case 1:
                return "Method";
            case 2:
                return _ms.getName();
            case 3:
                return _ms.getMethodNameSpec();
            }

        } else if (obj instanceof FieldSpecification) {
            final FieldSpecification _fs = (FieldSpecification) obj;
            switch (index) {
            case 1:
                return "Field";
            case 2:
                return _fs.getName();
            case 3:
                return _fs.getFieldNameSpec();
            }

        } else {
            return "";
        }
        return getText(obj);
    }

    public Image getColumnImage(Object obj, int index) {
        return null;
    }

    public Image getImage(Object obj) {
        return null;
    }
}
