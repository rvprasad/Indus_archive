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
 
package edu.ksu.cis.indus.kaveri.infoView;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.callgraph.ContextContentProvider;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jibx.runtime.JiBXException;

/**
 * @author ganeshan
 *
 * This view displays the updated information regarding
 * the scope, contexts and the slice statistics.
 */
public class InfoView extends ViewPart implements IDeltaListener {

    /**
     * The viewer for the scope information.
     * 
     */
    private TableViewer scopeViewer;
    
    /**
     * The viewer for the contexts.
     */
    private TableViewer ctxViewer;
    
    private IAction removeAll;
    private IAction remove;
    /**
     * The viewer for the criteria.
     */
    private TableViewer crtViewer;
    
    /**
     * The set of scope specifications.
     */
    private SpecificationBasedScopeDefinition sbsd;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        final GridLayout _layout = new GridLayout(1, true);
        _comp.setLayout(_layout);
        final TabFolder _folder = new TabFolder(_comp, SWT.NONE);
        _folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.FILL_VERTICAL));
        final TabItem _item1 = new TabItem(_folder, SWT.NONE);
        _item1.setText("Scope");
        _item1.setControl(createScopeTab(_folder));
        
        final TabItem _item2 = new TabItem(_folder, SWT.NONE);
        _item2.setText("Call Contexts");
        _item2.setControl(createContextTab(_folder));
        
        final TabItem _item3 = new TabItem(_folder, SWT.NONE);
        _item3.setText("Criteria List");
        _item3.setControl(createCriteriaList(_folder));
        
        KaveriPlugin.getDefault().getIndusConfiguration().getInfoBroadcaster().addListener(this);
    }

    /**
     * Create the criteria list.
     * @param folder
     * @return
     */
    private Control createCriteriaList(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
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
        crtViewer.setContentProvider(new CriteriaViewContentProvider());
        crtViewer.setLabelProvider(new CriteriaViewLabelProvider());
        crtViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer());
        for (int _i = 0; _i < _table.getColumnCount(); _i++) {
        	_table.getColumn(_i).pack();
        }
        createActions();
        createMenu();
        hookDoubleClick();
        return _comp;
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
	/**
     * Creates the context display dialog.
     * @param folder
     * @return
     */
    private Control createContextTab(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);        
        GridLayout _layout = new GridLayout(1, true);		
		_comp.setLayout(_layout);
		
		final GridData _gd1 = new GridData(GridData.FILL_BOTH);
		_gd1.horizontalSpan = 1;
		_gd1.grabExcessHorizontalSpace = true;
		_gd1.grabExcessVerticalSpace = true;
		_comp.setLayoutData(_gd1);
		        
        ctxViewer = new TableViewer(_comp, SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        final Table _table = ctxViewer.getTable();
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _table.setLayoutData(_gd);
        
        _table.setHeaderVisible(true);
        _table.setLinesVisible(true);

        final String[] _colNames = {"Call String Source", "Call String End"};
        for (int _i = 0; _i < _colNames.length; _i++) {
            final TableColumn _col = new TableColumn(_table, SWT.NONE);
            _col.setText(_colNames[_i]);
        }
        ctxViewer.setContentProvider(new ContextContentProvider(null));
        ctxViewer.setLabelProvider(new ContextLabelProvider());
        ctxViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getCtxRepository());
        for (int _i = 0; _i < _colNames.length; _i++) {
            _table.getColumn(_i).pack();
        }
                               
        return _comp;
    }

    /**
     * Create the scope tab.
     * @param folder
     * @return
     */
    private Control createScopeTab(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));

        initializeScopeSpecification();

        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 1;
        _comp.setLayout(_layout);

        scopeViewer = new TableViewer(_comp, SWT.SINGLE
                | SWT.V_SCROLL | SWT.FULL_SELECTION);

        final Table _table = scopeViewer.getTable();
        _table.setHeaderVisible(true);
        _table.setLinesVisible(true);


        final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
        _col1.setText("Type");

        final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
        _col2.setText("Scope Name");

        final TableColumn _col3 = new TableColumn(_table, SWT.NONE);
        _col3.setText("Element Name");

        scopeViewer.setContentProvider(new ScopeViewContentProvider());
        scopeViewer.setLabelProvider(new ScopeLabelProvider());

        folder.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                final TableColumn _cols[] = _table.getColumns();
                for (int i = 0; i < _cols.length; i++) {
                    _cols[i].pack();
                }
            }
        });

        GridData _gd = new GridData();
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _gd.horizontalAlignment = GridData.FILL;
        _gd.verticalAlignment = GridData.FILL;
        _table.setLayoutData(_gd);
        scopeViewer.setInput(sbsd);
        return _comp;
    }

    /**
     * Initialize the variable sbsd with the specification stored in the
     * project.
     */
    private void initializeScopeSpecification() {
        final IPreferenceStore _ps = KaveriPlugin.getDefault()
                .getPreferenceStore();
        final String _scopeSpecKey = "edu.ksu.cis.indus.kaveri.scope";
        String _scopeSpec = _ps.getString(_scopeSpecKey);
        if (_scopeSpec.equals("")) {
            _scopeSpec = "<indus:scopeSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + "xmlns:indus=\"http://indus.projects.cis.ksu.edu/indus\""
                    + "indus:specName=\"scope_spec\">";
            _scopeSpec += "\n</indus:scopeSpec>";
        }
        try {
            sbsd = SpecificationBasedScopeDefinition.deserialize(_scopeSpec);
        } catch (JiBXException _jbe) {
            SECommons.handleException(_jbe);
            KaveriErrorLog.logException("JiBx Exception", _jbe);
            sbsd = null;
        }

    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
       KaveriPlugin.getDefault().getIndusConfiguration().getInfoBroadcaster().removeListenere(this);
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
     */
    public void propertyChanged() {
     initializeScopeSpecification();
     scopeViewer.setInput(sbsd);
     ctxViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
             .getCtxRepository());    
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
     */
    public boolean isReady() {
        return false;
    }
}
