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

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.kaveri.views.IDeltaListener;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

/**
 * @author ganeshan
 * 
 * Provides the content for the left pane of the dependence tracking view.
 */
public class DepTrkStmtLstContentProvider implements ITreeContentProvider,
        IDeltaListener {

    private TreeViewer tvLeft;

    private boolean isActive = false;

    private PartialStmtData psd;

    private LeftPaneTreeParent invisibleRoot;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof LeftPaneTreeParent) {
            return ((LeftPaneTreeParent) parentElement).getChildren();
        }
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof LeftPaneTreeObject) {
            return ((LeftPaneTreeObject) element).getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof LeftPaneTreeParent) {
            return ((LeftPaneTreeParent) element).hasChildren();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (!isActive) {
            return new Object[0];
        }
        
        if (inputElement instanceof PartialStmtData) {
            if (invisibleRoot == null) {
                invisibleRoot = new LeftPaneTreeParent("");
                initialize();
            }

        }
        return getChildren(invisibleRoot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        if (psd != null) {
            psd.removeListener(this);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.tvLeft = (TreeViewer) viewer;
        if (oldInput != null) {
            ((PartialStmtData) oldInput).removeListener(this);
        }

        if (newInput != null) {
            this.psd = (PartialStmtData) newInput;
            ((PartialStmtData) newInput).addListener(this);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
     */
    public void propertyChanged() {
        if (invisibleRoot != null) {
            invisibleRoot.removeAllChildren();
        }
        if (tvLeft != null && isActive) {
            initialize();
            tvLeft.refresh();
            tvLeft.expandAll();
            tvLeft.setSelection(new StructuredSelection(invisibleRoot
                    .getChildren()[0]), true);
        }
    }

    /**
     * Initialize the tree model.
     */
    private void initialize() {
        if (invisibleRoot != null && psd.getSelectedStatement() != null && psd.getJavaFile() != null
                && psd.getStmtList() != null && psd.getStmtList().size() > 2) {            
            final String _mainHeading = psd.getSelectedStatement() + " ("
                    + psd.getJavaFile().getName() + ")";
            final LeftPaneTreeParent _tParent = new LeftPaneTreeParent(
                    _mainHeading);
            _tParent.setFile(psd.getJavaFile());
            _tParent.setJimpleIndex(-1);
            _tParent.setLineNumber(psd.getLineNo());

            final List _stmtList = psd.getStmtList().subList(2,
                    psd.getStmtList().size());
            for (int _i = 0; _i < _stmtList.size(); _i++) {
                final Stmt _stmt = (Stmt) _stmtList.get(_i);
                final LeftPaneTreeObject _tChild = new LeftPaneTreeObject(_stmt
                        .toString());
                _tChild.setFile(psd.getJavaFile());
                _tChild.setJimpleIndex(_i);
                _tChild.setLineNumber(psd.getLineNo());
                _tParent.addChild(_tChild);
            }
            invisibleRoot.addChild(_tParent);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
     */
    public boolean isReady() {
        return isActive;
    }

    /**
     * Set the active state of the content provider.
     * 
     * @param isActive
     *            The isActive to set.
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Select the top item if present.
     */
    public void selectTopItem() {
        if (invisibleRoot != null && invisibleRoot.getChildren() != null && invisibleRoot.getChildren().length > 0)
        tvLeft.setSelection(new StructuredSelection(invisibleRoot
                .getChildren()[0]), true);
    }
}