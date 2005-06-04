/*
 * Created on Jun 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ksu.cis.indus.kaveri.infoView;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.LinkedList;
import java.util.List;

import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.Messages;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;
import edu.ksu.cis.indus.kaveri.views.CriteriaListMaintainer;
import edu.ksu.cis.indus.kaveri.views.IDeltaListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;

/**
 * @author Ganeshan
 *
 * Provides the contents for the criteria view.
 */
public class CriteriaViewContentProvider implements IStructuredContentProvider,
IDeltaListener {
    
    private TableViewer crtViewer;
    
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        crtViewer = (TableViewer) v;
        if (oldInput != null) {
            ((CriteriaListMaintainer) oldInput).removeListener(this);
        }
        if (newInput != null) {
            ((CriteriaListMaintainer) newInput).addListener(this);
        }
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        if (parent instanceof CriteriaListMaintainer) {
            final IProject _prj = ((CriteriaListMaintainer) parent)
                    .getProject();
            final IFile _file = ((CriteriaListMaintainer) parent).getJavaFile();                
            if (_prj == null) {
                return new Object[0];
            }
            final IJavaProject _project = JavaCore.create(_prj);
            IResource _resource;
            
            final List _classNameList = SECommons.getClassesInFile(_file);
            final List _retList = new LinkedList();
            final XStream _xstream = new XStream(new DomDriver());
            _xstream
                    .alias(
                            Messages
                                    .getString("IndusConfigurationDialog.17"), CriteriaData.class); //$NON-NLS-1$

            try {
                _resource = _project.getCorrespondingResource();

                final QualifiedName _name = new QualifiedName(Messages
                        .getString("IndusConfigurationDialog.18"), Messages
                        .getString("IndusConfigurationDialog.19"));

                try {
                    //				_resource.setPersistentProperty(_name, null); //
                    // Knocks
                    // out
                    // the stuff
                    final String _propVal = _resource
                            .getPersistentProperty(_name);

                    if (_propVal != null) {
                        final CriteriaData _data = (CriteriaData) _xstream
                                .fromXML(_propVal);
                        final java.util.List _lst = _data.getCriterias();

                        for (int _i = 0; _i < _lst.size(); _i++) {
                            final Criteria _c = (Criteria) _lst.get(_i);
                            if (_classNameList.contains(_c.getStrClassName())) {
                                _retList.add(_c);
                            }
                        }
                    }
                } catch (CannotResolveClassException _crce) {
                    SECommons.handleException(_crce);
                } catch (CoreException _e) {
                    SECommons.handleException(_e);
                }
            } catch (JavaModelException _e1) {
                SECommons.handleException(_e1);
            }
            return _retList.toArray();
        } else {
            return new Object[0];
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
     */
    public void propertyChanged() {
        if (crtViewer != null) {
            crtViewer.refresh();

            final Table _table = crtViewer.getTable();
            for (int _i = 0; _i < _table.getColumnCount(); _i++) {
                _table.getColumn(_i).pack();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
     */
    public boolean isReady() {
        return true;
    }
}
