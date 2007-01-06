/*
 * Created on Jun 3, 2005
 *
 * Generates the content for the scope tab.
 */
package edu.ksu.cis.indus.kaveri.infoView;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.ksu.cis.indus.common.scoping.ClassSpecification;
import edu.ksu.cis.indus.common.scoping.FieldSpecification;
import edu.ksu.cis.indus.common.scoping.MethodSpecification;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;

/**
 * @author Ganeshan
 * 
 * Generates the content for the scope tab of the slice information view.
 */
public class ScopeViewContentProvider implements IStructuredContentProvider {

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        if (parent != null && parent instanceof SpecificationBasedScopeDefinition) {
            final SpecificationBasedScopeDefinition _sbsd = (SpecificationBasedScopeDefinition) parent;
            final List _lstSpecs = new LinkedList();
            final Collection _collClassSpecs = _sbsd.getClassSpecs();
            for (Iterator iter = _collClassSpecs.iterator(); iter.hasNext();) {
                final ClassSpecification _cs = (ClassSpecification) iter.next();
                _lstSpecs.add(_cs);

            }
            final Collection _collMethodSpecs = _sbsd.getMethodSpecs();
            for (Iterator iter = _collMethodSpecs.iterator(); iter.hasNext();) {
                final MethodSpecification _ms = (MethodSpecification) iter.next();
                _lstSpecs.add(_ms);

            }
            final Collection _collFieldSpecs = _sbsd.getFieldSpecs();
            for (Iterator iter = _collFieldSpecs.iterator(); iter.hasNext();) {
                final FieldSpecification _fs = (FieldSpecification) iter.next();
                _lstSpecs.add(_fs);
            }
            return _lstSpecs.toArray();

        } else {
            return new Object[0];
        }

    }

    public void inputChanged(@SuppressWarnings("unused")
    Viewer v, @SuppressWarnings("unused")
    Object oldInput, @SuppressWarnings("unused")
    Object newInput) {
    }

}
