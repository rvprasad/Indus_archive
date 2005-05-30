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
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jibx.runtime.JiBXException;

/**
 * @author ganeshan
 *
 * Provides the content for the scope view.
 */
public class ScopeViewContentProvider implements IStructuredContentProvider {
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        final String _scopeKey = "edu.ksu.cis.indus.kaveri.scope";
        final String _scopeSpec = KaveriPlugin.getDefault()
                .getPreferenceStore().getString(_scopeKey);
        if (_scopeSpec.equals(""))
            return new Object[0];
        try {
            SpecificationBasedScopeDefinition _sbsd = SpecificationBasedScopeDefinition
                    .deserialize(_scopeSpec);
            final List _lstSpecs = new LinkedList();
            final Collection _collClassSpecs = _sbsd.getClassSpecs();
            for (Iterator iter = _collClassSpecs.iterator(); iter.hasNext();) {
                final ClassSpecification _cs = (ClassSpecification) iter.next();
                _lstSpecs.add(_cs);

            }
            final Collection _collMethodSpecs = _sbsd.getMethodSpecs();
            for (Iterator iter = _collMethodSpecs.iterator(); iter.hasNext();) {
                final MethodSpecification _ms = (MethodSpecification) iter
                        .next();
                _lstSpecs.add(_ms);

            }
            final Collection _collFieldSpecs = _sbsd.getFieldSpecs();
            for (Iterator iter = _collFieldSpecs.iterator(); iter.hasNext();) {
                final FieldSpecification _fs = (FieldSpecification) iter.next();
                _lstSpecs.add(_fs);
            }
            return _lstSpecs.toArray();

        } catch (JiBXException _jbe) {
            SECommons.handleException(_jbe);
            KaveriErrorLog.logException("Error deserializing scope spec", _jbe);
            return new Object[0];
        }

    }
}
