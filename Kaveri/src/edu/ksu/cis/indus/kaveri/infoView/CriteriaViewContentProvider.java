/*
 * Created on Jun 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ksu.cis.indus.kaveri.infoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;

import com.thoughtworks.xstream.alias.CannotResolveClassException;

import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;
import edu.ksu.cis.indus.kaveri.views.CriteriaListMaintainer;
import edu.ksu.cis.indus.kaveri.views.IDeltaListener;
import edu.ksu.cis.indus.kaveri.views.PartialSliceView;

/**
 * @author Ganeshan
 * 
 * Provides the contents for the criteria view.
 */
public class CriteriaViewContentProvider implements IStructuredContentProvider, IDeltaListener {

    private TableViewer crtViewer;

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        if (parent instanceof CriteriaListMaintainer && ((CriteriaListMaintainer) parent).getProject() != null) {
            final CriteriaListMaintainer _p = ((CriteriaListMaintainer) parent);
            final IProject _prj = _p.getProject();
            final List _retList = new ArrayList();
            try {
                final CriteriaData _data = PartialSliceView.retrieveCriteria(_prj);
                _retList.addAll(_data.getCriterias());            
                } catch (CannotResolveClassException _crce) {
                SECommons.handleException(_crce);
            } catch (CoreException _e) {
                SECommons.handleException(_e);
            } catch (IOException _e) {
                SECommons.handleException(_e);
            }
            return _retList.toArray();
        }
        return new Object[0];
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        crtViewer = (TableViewer) v;
        if (oldInput instanceof CriteriaListMaintainer) {
            ((CriteriaListMaintainer) oldInput).removeListener(this);
        }
        if (newInput instanceof CriteriaListMaintainer) {
            ((CriteriaListMaintainer) newInput).addListener(this);
        }
    }

    /**
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
     */
    public boolean isReady() {
        return true;
    }

    /**
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
}
