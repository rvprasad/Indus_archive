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

package edu.ksu.cis.indus.kaveri.views;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.Messages;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * @author ganeshan
 * 
 * Displays the list of criteria for the project.
 */
public class CriteriaView extends ViewPart {

    TableViewer crtViewer;

    private IAction removeAll;
    private IAction remove;
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        _comp.setLayout(new GridLayout(1, true));

        crtViewer = new TableViewer(_comp, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        final Table _table = crtViewer.getTable();
        final GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _table.setLayoutData(_gd);
        setupTable(_table);
        crtViewer.setContentProvider(new ViewContentProvider());
        crtViewer.setLabelProvider(new CriteriaViewLabelProvider());
        crtViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer());
        for (int _i = 0; _i < _table.getColumnCount(); _i++) {
        	_table.getColumn(_i).pack();
        }
        createActions();
        createMenu();
        hookDoubleClick();
    }


    /**
	 * Hook double click.
	 */
	private void hookDoubleClick() {
		crtViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				final ISelection _sel = crtViewer.getSelection();
				if (!_sel.isEmpty() && _sel instanceof IStructuredSelection) {
					final Criteria _c = (Criteria) ((IStructuredSelection) _sel).getFirstElement();
					if (_c != null) {
						final CriteriaListMaintainer _clm = KaveriPlugin.getDefault()
						.getIndusConfiguration().getCrtMaintainer();
						final IFile _file = _clm.getJavaFile();
						if (_file != null) {
							final ICompilationUnit _unit = JavaCore.createCompilationUnitFrom(_file);
							if (_unit == null) {
								return;
							}
							try {
								final CompilationUnitEditor _editor = (CompilationUnitEditor) JavaUI.openInEditor(_unit);
								if (_editor != null) {
									final int _lineno = _c.getNLineNo();
									 final IRegion _region = _editor
                                     .getDocumentProvider()
                                     .getDocument(
                                             _editor
                                                     .getEditorInput())
                                     .getLineInformation(
                                             _lineno - 1);
                             _editor.selectAndReveal(_region.getOffset(), _region.getLength());
								}
							} catch (PartInitException e) {
								SECommons.handleException(e);
								KaveriErrorLog.logException("Par init exception", e);
							} catch (JavaModelException e) {
								SECommons.handleException(e);
								KaveriErrorLog.logException("Java model exception", e);								
							} catch (BadLocationException e) {
								SECommons.handleException(e);
								KaveriErrorLog.logException("Bad location", e);
							}
						}
					}
				}
				
			}
			
		});
		
	}


	/**
	 * Create the actions.
	 */
	private void createActions() {
		removeAll = new Action() {
			public void run() {
				final CriteriaData _cd = getCurrentCriteriaList();
				if (_cd != null) {
					crtViewer.getTable().selectAll();
					final List _lst = _cd.getCriterias();
					final IStructuredSelection _ssl= (IStructuredSelection) crtViewer.getSelection();
					for (final Iterator _t =_ssl.iterator(); _t.hasNext();) {
						final Criteria _c = (Criteria) _t.next();
						_lst.remove(_c);
					}
					_cd.setCriterias(_lst);
					final IProject _prj = KaveriPlugin.getDefault().
						getIndusConfiguration().getCrtMaintainer().getProject();
					saveNewCriteria(_prj, _cd);
					crtViewer.getTable().removeAll();
					crtViewer.refresh();
					KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
				}
			}
		};
		
		removeAll.setText("Remove All");
		
		remove = new Action() {
			public void run() {
				final CriteriaData _cd = getCurrentCriteriaList();
				if (_cd != null && !crtViewer.getSelection().isEmpty()) {
					final List _lst = _cd.getCriterias();
					final IStructuredSelection _ssl= (IStructuredSelection) crtViewer.getSelection();
					for (final Iterator _t =_ssl.iterator(); _t.hasNext();) {
						final Criteria _c = (Criteria) _t.next();
						_lst.remove(_c);
					}
					_cd.setCriterias(_lst);
					final IProject _prj = KaveriPlugin.getDefault().
						getIndusConfiguration().getCrtMaintainer().getProject();
					saveNewCriteria(_prj, _cd);
					crtViewer.getTable().removeAll();
					crtViewer.refresh();
					KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
				}
			}
		};
		remove.setText("Remove");
		
	}


	/**
	 * Save the new criteria.
	 * @param prj
	 * @param cd
	 */
	protected void saveNewCriteria(IProject prj, CriteriaData cd) {		               
        if (prj == null) {            
        }
        final IJavaProject _project = JavaCore.create(prj);
        IResource _resource;
                
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
                final String _propVal = _xstream.toXML(cd);
                _resource.setPersistentProperty(_name, _propVal);
                
            } catch (CannotResolveClassException _crce) {
                SECommons.handleException(_crce);
            } catch (CoreException _e) {
                SECommons.handleException(_e);
            }
        } catch (JavaModelException _e1) {
            SECommons.handleException(_e1);
        }
		
	}


	/**
	 * Returns the current set of criteria.
	 * @return
	 */
	protected CriteriaData getCurrentCriteriaList() {
		final CriteriaListMaintainer _m = KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer();
		
		final IProject _prj = _m.getProject();
        final IFile _file = _m.getJavaFile();                
        if (_prj == null || _file == null) {
            return null;
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
                    return _data;
                }
            } catch (CannotResolveClassException _crce) {
                SECommons.handleException(_crce);
            } catch (CoreException _e) {
                SECommons.handleException(_e);
            }
        } catch (JavaModelException _e1) {
            SECommons.handleException(_e1);
        }
		return null;
	}


	/**
	 * Create the popup menu.
	 */
	private void createMenu() {
		 final MenuManager _popMenuMangager = new MenuManager("#Popup");
	        _popMenuMangager.setRemoveAllWhenShown(true);
	        _popMenuMangager.addMenuListener(new IMenuListener() {
	            public void menuAboutToShow(IMenuManager manager) {	                
	                manager.add(remove);
	                manager.add(removeAll);
	                manager.add(new Separator(
	                        IWorkbenchActionConstants.MB_ADDITIONS));
	            }

	        });
	        final Menu _mnu = _popMenuMangager.createContextMenu(crtViewer
	                .getControl());
	        crtViewer.getControl().setMenu(_mnu);
		
	}


	/**
     * Setup the table.
     * 
     * @param table
     *            The table.
     */
    private void setupTable(Table criteriaTable) {
        final String[] _colnames = { "Function", "Line number", "Jimple index",
                "Consider Execution" };

        for (int _i = 0; _i < _colnames.length; _i++) {
            final TableColumn _ti = new TableColumn(criteriaTable, SWT.NULL);
            _ti.setText(_colnames[_i]);
        }

        criteriaTable.setHeaderVisible(true);
        criteriaTable.setLinesVisible(true);

        for (int _i = 0; _i < criteriaTable.getColumnCount(); _i++) {
            criteriaTable.getColumn(_i).pack();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

    class ViewContentProvider implements IStructuredContentProvider,
            IDeltaListener {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
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

}

class CriteriaViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
        String _retString = "";
        if (obj instanceof Criteria) {
            final Criteria _c = (Criteria) obj;
            switch (index) {
            case 0:
                _retString = _c.getStrMethodName();
                break;
            case 1:
                _retString = _c.getNLineNo() + "";
                break;
            case 2:
                _retString = _c.getNJimpleIndex() + "";
                break;
            case 3:
                _retString = _c.isBConsiderValue() + "";                
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