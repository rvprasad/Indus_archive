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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import soot.SootMethod;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dependence.filters.ControlFilter;
import edu.ksu.cis.indus.kaveri.dependence.filters.DataFilter;
import edu.ksu.cis.indus.kaveri.dependence.filters.DivergenceFilter;
import edu.ksu.cis.indus.kaveri.dependence.filters.InterferenceFilter;
import edu.ksu.cis.indus.kaveri.dependence.filters.ReadyFilter;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.kaveri.views.DependenceStackData;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;



/**
 * @author ganeshan
 *
 * This view keeps track of the dependencies.
 */
public class DependenceTrackingView extends ViewPart {
    
    /**
     * Used to display the Java and underlying Jimple.
     */
    private TreeViewer tvLeft;
    
    /**
     * Used to display the dependence information.
     */
    private TreeViewer tvRight;
    
    
    /**
     * Filter out Control dependence.
     */
    private ViewerFilter controlFilter;
    
    /**
     * Filter out Data dependence.
     */
    private ViewerFilter dataFilter;
    
    /**
     * Filter out Interference dependence.
     */
    private ViewerFilter interferenceFilter;
    
    /**
     * Filter out Ready dependence.
     */
    private ViewerFilter readyFilter;
    
    /**
     * Filter out Synchronization dependence.
     */
    private ViewerFilter synchFilter;
    
    /**
     * Filter out Divergence dependence.
     */
    private ViewerFilter divergenceFilter;
    
    
    private Action controlFilterAction;
    private Action dataFilterAction;
    private Action readyFilterAction;
    private Action interferenceFilterAction;
    private Action divergenceFilterAction;
    private Action synchFilterAction;
    /**
     * Whether this view is active.
     */
    private boolean isActive = false;
    
    
    private Map classNameToFileMap;
    
    /** The input to the right pane */
    private DependenceStmtData dsd;
    
    
    
    class StandarLabelProvider extends LabelProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {            
             return element.toString();            
        }
}
	
	
	        
    
    /**
     * Constructor.
     */
    public DependenceTrackingView() {
        dsd = new DependenceStmtData(); 
        classNameToFileMap = new HashMap();
    }
    
    /** Create the dialog areas.
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayout(new GridLayout(1, true));
        
        final SashForm _sForm = new SashForm(_comp, SWT.HORIZONTAL);
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _sForm.setLayoutData(_gd);
        
        final Composite _lComp = new Composite(_sForm, SWT.NONE);
        _lComp.setLayout(new GridLayout(1, true));
        
        final Label _lblLeft = new Label(_lComp, SWT.LEFT);
        _lblLeft.setText("Statement");
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _lblLeft.setLayoutData(_gd);
        _lblLeft.setFont(parent.getFont());
        
        tvLeft = new TreeViewer(_lComp, SWT.SINGLE | SWT.BORDER);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        tvLeft.getTree().setLayoutData(_gd);
        tvLeft.getTree().setFont(parent.getFont());
        
        
        final Composite _rComp = new Composite(_sForm, SWT.NONE);
        _rComp.setLayout(new GridLayout(1, true));
        
        final Label _lblRight = new Label(_rComp, SWT.LEFT);
        _lblRight.setText("Dependence");
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _lblRight.setLayoutData(_gd);
        _lblRight.setFont(parent.getFont());
        
        tvRight = new TreeViewer(_rComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        tvRight.getTree().setLayoutData(_gd);
        tvRight.getTree().setFont(parent.getFont());
        
        
        _sForm.setWeights(new int[] {4, 6});
        final IToolBarManager _manager = getViewSite().getActionBars()
        	.getToolBarManager();
        fillToolBar(_manager);                
                
        
        tvLeft.setContentProvider(new DepTrkStmtLstContentProvider());
        tvLeft.setLabelProvider(new StandarLabelProvider());
        tvLeft.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getStmtList());
        tvLeft.setAutoExpandLevel(1);
                
        tvRight.setContentProvider(new DepTrkDepLstContentProvider());
        tvRight.setLabelProvider(new StandarLabelProvider());
        tvRight.setInput(dsd);
        hookListeners();
        createFilters();
        hookFilters();
        createMenus();
        hookDoubleClickListeners();
    }
        

    /**
     * Handle double click in the right pane.
     */
    private void hookDoubleClickListeners() {
        tvRight.addDoubleClickListener(
                new IDoubleClickListener() {

                    public void doubleClick(DoubleClickEvent event) {
                        if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                            final IStructuredSelection _ssl = (IStructuredSelection) event.getSelection();
                            if (_ssl.getFirstElement() instanceof RightPaneTreeObject) {
                                final RightPaneTreeObject _rto = (RightPaneTreeObject) _ssl.getFirstElement();
                                final SootMethod _sm =  _rto.getSm();
                                if (_sm == null) return;
                                final String _className = _sm.getDeclaringClass().getName();
                                final int nLineNo = _rto.getLineNumber();
                                final IFile _file = getFileContainingClass(_sm, dsd.getJavaFile());
                                if (_file != null && nLineNo != -1) {
                                   final ICompilationUnit _unit =
                                       JavaCore.createCompilationUnitFrom(_file);
                                   if (_unit != null) {
                                       try {
                                        final IType _types[] = _unit.getAllTypes();
                                        for (int i = 0; i < _types.length; i++) {
                                            final IType _type = _types[i];
                                            if (_type.getFullyQualifiedName().equals(_className)) {
                                                final IMethod _methods[] =  _type.getMethods();
                                                for (int j = 0; j < _methods.length; j++) {
                                                    final IMethod _method = (IMethod) _methods[j];
                                                    if(SECommons.getProperMethodName(_method).equals(
                                                            SECommons.getSearchPattern(_sm))) {
                                                        final List _lst = SootConvertor.getStmtForLine(_file, _type, _method, nLineNo);
                                                        final IEditorPart _part =JavaUI.openInEditor(_method);
                                                        if (_part instanceof CompilationUnitEditor) {
                                                            final CompilationUnitEditor _editor = (CompilationUnitEditor) _part;
                                                            final IRegion _region =
                                        						_editor.getDocumentProvider().getDocument(_editor.getEditorInput()).getLineInformation(nLineNo - 1);
                                        					final String _text =
                                        						_editor.getDocumentProvider().getDocument(_editor.getEditorInput()).get(_region.getOffset(),
                                        							_region.getLength());
                                        					final String _trimmedString = _text.trim();
                                                            final PartialStmtData _psd = KaveriPlugin.getDefault().getIndusConfiguration().getStmtList();
                                                            if (KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory().getHistory().size() == 0) {
                                                                final DependenceStackData _dParent = new DependenceStackData();                                                                
                                                                _dParent.setFile(_psd.getJavaFile());
                                                                _dParent.setStatement(_psd.getSelectedStatement());
                                                                _dParent.setLineNo(_psd.getLineNo());
                                                                KaveriPlugin.getDefault().getIndusConfiguration().setDepHistory(new Pair(_dParent, "Starting Point"));
                                                            }
                                                            
                                                            _psd.setClassName(_type.getFullyQualifiedName());
                                                            _psd.setJavaFile(_file);
                                                            _psd.setLineNo(nLineNo);
                                                            _psd.setMethodName(PrettySignature.getMethodSignature(_method));
                                                            _psd.setSelectedStatement(_trimmedString);
                                                            _psd.setStmtList(_lst);
                                                            _editor.selectAndReveal(_region.getOffset(), _region.getLength());
                                                            final DependenceStackData _depS = new DependenceStackData();
                                                            _depS.setStatement(_trimmedString);
                                                            _depS.setFile(_file);
                                                            _depS.setLineNo(nLineNo);
                                                            final Pair _pair = new Pair(_depS, _rto.getParent().toString() + " " +
                                                                    _rto.getParent().getParent().toString());
                                                            KaveriPlugin.getDefault().getIndusConfiguration().setDepHistory(_pair);
                                                            
                                                             
                                                        }                                                        
                                                                                                                                                                        
                                                        break;  	
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    } catch (JavaModelException e) {
                                        SECommons.handleException(e);                                        
                                    } catch (PartInitException e) {
                                        SECommons.handleException(e);
                                    } catch (BadLocationException e) {
                                        SECommons.handleException(e);                                        
                                    }
                                   }
                                }
                            }
                        }
                        
                    }
                    
                }
                );
        
    }

    /**
     * Create the filters.
     */
    private void createFilters() {
        controlFilter = new ControlFilter();
        dataFilter = new DataFilter();
        interferenceFilter = new InterferenceFilter();
        readyFilter = new ReadyFilter();
        divergenceFilter = new DivergenceFilter();
    }

    /**
     * Add the filters,
     */
    private void hookFilters() {
        controlFilterAction = new Action("Control") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(controlFilter);
                } else {
                    tvRight.addFilter(controlFilter);
                }
            }
        };
        controlFilterAction.setChecked(true);       

        dataFilterAction = new Action("Data") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(dataFilter);
                } else {
                    tvRight.addFilter(dataFilter);
                }
            }
        };
        dataFilterAction.setChecked(true);

        
        interferenceFilterAction = new Action("Interference") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(interferenceFilter);
                } else {
                    tvRight.addFilter(interferenceFilter);
                }
            }
        };
        interferenceFilterAction.setChecked(true);

        
        readyFilterAction = new Action("Ready") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(readyFilter);
                } else {
                    tvRight.addFilter(readyFilter);
                }
            }
        };
        readyFilterAction.setChecked(true);

        
        synchFilterAction = new Action("Synchronization") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(synchFilter);
                } else {
                    tvRight.addFilter(synchFilter);
                }
            }
        };
        synchFilterAction.setChecked(true);
        
        divergenceFilterAction = new Action("Divergence") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(divergenceFilter);
                } else {
                    tvRight.addFilter(divergenceFilter);
                }
            }
        };
        divergenceFilterAction.setChecked(true);


        
    }

    /**
     * Create the filter menus.
     *
     */
	private void createMenus() {
		final IMenuManager _mainMnuManager = getViewSite().getActionBars().getMenuManager();
		_mainMnuManager.setRemoveAllWhenShown(true);
		_mainMnuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillMenu(mgr);
			}
		});
		fillMenu(_mainMnuManager);
	}


	/**
	 * Fill the menu.
	 * @param mnuMainMenu
	 */
	protected void fillMenu(IMenuManager mnuMainMenu) {
		final IMenuManager _filterMenu = new MenuManager("Filters");
		mnuMainMenu.add(_filterMenu);
		_filterMenu.add(controlFilterAction);
		_filterMenu.add(dataFilterAction);
		_filterMenu.add(interferenceFilterAction);
		_filterMenu.add(divergenceFilterAction);
		_filterMenu.add(readyFilterAction);
		_filterMenu.add(synchFilterAction);
		
	}
	

    /**
     * Add listeners to user actions.
     */
    private void hookListeners() {
        tvLeft.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                    final IStructuredSelection _ss = (IStructuredSelection) event.getSelection();
                    if (_ss.getFirstElement() instanceof LeftPaneTreeParent) {
                        final LeftPaneTreeParent _tp = (LeftPaneTreeParent) _ss.getFirstElement();
                        dsd.setupData(KaveriPlugin.getDefault().getIndusConfiguration().getStmtList(), -1);
                    } else if (_ss.getFirstElement() instanceof LeftPaneTreeObject) {
                        final LeftPaneTreeObject _to = (LeftPaneTreeObject) _ss.getFirstElement();
                        dsd.setupData(KaveriPlugin.getDefault().getIndusConfiguration().getStmtList(),  _to.getJimpleIndex());
                    }
                }
                
            }
            
        });
        
    }

    /**
     * Fill the toolbar.
     * @param manager The Toolbar manager.
     */
    private void fillToolBar(IToolBarManager _manager) {
        final Action _changeView = new Action() {
          public void run() {
              if (isActive) {
                  isActive = false;
                  final ImageDescriptor _desc = AbstractUIPlugin
                  .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                          "data/icons/trackView.gif");
                  this.setImageDescriptor(_desc);
                  ((DepTrkStmtLstContentProvider) tvLeft.getContentProvider()).setActive(isActive);
              } else {
                  isActive = true;                  
                  final ImageDescriptor _desc = AbstractUIPlugin
                  .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                          "data/icons/trackViewAct.gif");
                  this.setImageDescriptor(_desc);
                  ((DepTrkStmtLstContentProvider) tvLeft.getContentProvider()).setActive(isActive);
                  tvLeft.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getStmtList());
              }
          }
        };
        
        _changeView.setToolTipText("Change the view");        
        final ImageDescriptor _desc = AbstractUIPlugin
        .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                "data/icons/trackView.gif");
        _changeView.setImageDescriptor(_desc);
        _manager.add(_changeView);
    }
    
    /**
     * Returns the Java file containing the method whose soot equivalent 
     * is sootMethod
     * TODO Add fix for multiple projects.
     * @param sootMethod
     * @param sampleFile A Java file from the project. Used to obtain the project.
     * @return IFile The file containing the given method, or null if no such file is present.
     */
    private IFile getFileContainingClass(SootMethod sootMethod, final IFile sampleFile) {
       IFile _file = null;       
       
       final IProject _project = sampleFile.getProject();
       final IJavaProject _jProject = JavaCore.create(_project);
       final Object _mapVal = classNameToFileMap.get(sootMethod.getDeclaringClass().getName());
       if (_mapVal != null) {
           _file = (IFile) _mapVal;
       } else {
           final List _fileList = SECommons.processForFiles(_jProject);
           for (Iterator iter = _fileList.iterator(); iter.hasNext();) {
            final IFile _jfile = (IFile) iter.next();
            final List _classNameList = SECommons.getClassesInFile(_jfile);
            for (Iterator iterator = _classNameList.iterator(); iterator
                    .hasNext();) {
               final String _className = (String) iterator.next();
               if (_className.equals(sootMethod.getDeclaringClass().getName())) {
                   _file = _jfile;
                   classNameToFileMap.put(sootMethod.getDeclaringClass().getName(), _file);
               } else {
                   classNameToFileMap.put(_className, _jfile);
               }
                
            }
           }           
       }
       
       
       return _file;
    }

    /** 
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
       tvLeft.getControl().setFocus();

    }

}
