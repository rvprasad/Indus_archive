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
 
package edu.ksu.cis.indus.kaveri.peq;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;

import soot.SootMethod;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;
import edu.ksu.cis.indus.peq.customengine.IndusExistentialQueryEngine;
import edu.ksu.cis.indus.peq.customengine.IndusMatcher;
import edu.ksu.cis.indus.peq.fsm.FSMBuilder;
import edu.ksu.cis.indus.peq.graph.GraphBuilder;
import edu.ksu.cis.indus.peq.indusinterface.IndusInterface;
import edu.ksu.cis.indus.peq.queryglue.QueryConvertor;
import edu.ksu.cis.indus.peq.queryglue.QueryObject;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.queryengine.IQueryProgressListener;

/**
 * @author ganeshan
 *
 * This is the view that enables PEQ related stuff in Kaveri.
 * 
 */
public class PEQView extends ViewPart {
    /**
     * Shows the statement that is the entry point.
     */
    Text txtStatement;
    
    /**
     * The text box for entering the query.
     */
    Combo cmbQuery;

    /**
     * The Tree tvLeft for showing the results.
     */
    TreeViewer tvLeft;

    /**
     * The table viewer for showing the mappings.
     */
    TableViewer tvRight;
    /** Query results */
    private Collection queryResults = Collections.EMPTY_LIST;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayout(new GridLayout(2, false));
        
        final Label _lblStatement = new Label(_comp, SWT.NONE);
        _lblStatement.setText("Statement:");
        GridData _gd = new GridData();
        _gd.horizontalSpan = 1;
        _lblStatement.setLayoutData(_gd);
        
        txtStatement = new Text(_comp, SWT.BORDER);
        txtStatement.setEditable(false);
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan = 1;        
        txtStatement.setLayoutData(_gd);
        
        
        final Label _lblQuery = new Label(_comp, SWT.NONE);
        _lblQuery.setText("Query:");
        _gd = new GridData();
        _gd.horizontalSpan = 1;
        _lblQuery.setLayoutData(_gd);
        
        cmbQuery = new Combo(_comp, SWT.DROP_DOWN);        
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        cmbQuery.setLayoutData(_gd);
        
        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Results");
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 2;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _grp.setLayoutData(_gd);
        _grp.setLayout(new GridLayout(1, true));
        
        final SashForm _sForm = new SashForm(_grp, SWT.HORIZONTAL);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _sForm.setLayoutData(_gd);
        
        tvLeft = new TreeViewer(_sForm, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        final Tree _tree = tvLeft.getTree();
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;        
        _gd.grabExcessVerticalSpace = true;
        _gd.grabExcessHorizontalSpace = true;         
        _tree.setLayoutData(_gd);
        tvLeft.setContentProvider(new ViewContentProvider());
        tvLeft.setLabelProvider(new LabelProvider());
        tvLeft.setInput(queryResults);
        
        
        tvRight = new TableViewer(_sForm, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        final Table _table = tvRight.getTable();
        setupTable(_table);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;        
        _gd.grabExcessVerticalSpace = true;
        _gd.grabExcessHorizontalSpace = true;
        _table.setLayoutData(_gd);
        tvRight.setContentProvider(new TableContentProvider());
        tvRight.setLabelProvider(new TableLabelProvider());
        _sForm.setWeights(new int[] { 4, 6 });
        
        tvLeft.addSelectionChangedListener(
                new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                            final IStructuredSelection _ss = (IStructuredSelection) event
                                    .getSelection();
                            final Object _selObject = _ss.getFirstElement();
                            if (_selObject instanceof TreeObject && !(_selObject instanceof TreeParent)) {
                                final TreeObject _to = (TreeObject) _selObject;
                                tvRight.setInput(_to.getMapping());
                            } else {
                                tvRight.setInput("");
                            }
                        }
                        
                    }
                    
                }
                );
        
        
        final IToolBarManager _manager = getViewSite().getActionBars()
        .getToolBarManager();
        fillToolBar(_manager);
    }

    /**
     * Fill the toolbar.
     * @param manager
     */
    private void fillToolBar(IToolBarManager manager) {
        final IAction _actQuery = new Action() {            
            public void run() {
                final Shell _parentShell = getViewSite().getShell();
                if (cmbQuery.getText().equals("")) {                    
                    MessageDialog.openError(_parentShell, "Query Missing", "Please write a query before running the engine");
                    return;
                }
                final String _queryString = cmbQuery.getText();
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                try {
                    progressService.busyCursorWhile(new IRunnableWithProgress(){
                       public void run(final IProgressMonitor monitor) {
                          monitor.beginTask("Processing Query", 100);
                          final PartialStmtData _psd = KaveriPlugin.getDefault()
                      	.getIndusConfiguration().getStmtList();
                          if (_psd.getStmtList() != null && _psd.getSelectedStatement() != null
                              && _psd.getStmtList().size() > 2 ) {
                              final List _jimpleList = _psd.getStmtList().subList(2, _psd.getStmtList().size());
                              final Stmt _stmt = (Stmt) _jimpleList.get(_jimpleList.size()-1);
                              final SootMethod _sm = (SootMethod) _psd.getStmtList().get(1);
                              monitor.worked(5);
                              final QueryConvertor _qc = new QueryConvertor();
                              final QueryObject _qo = _qc.getQueryObject(_queryString);
                              monitor.worked(15);
                              if (_qo == null) {
                                  Display.getDefault().asyncExec(new Runnable() {
                                      public void run() {
                                          MessageDialog.openError(_parentShell, "Query Error", _qc.getErrorString());        
                                      }
                                  });
                                  
                              } else {
                                  FSMBuilder _fbuilder = new FSMBuilder(_qo);                                  
                                  final Collection _rootCollection = new LinkedList();
                                  final Pair _initPair = new Pair(_stmt, _sm);
                                  _rootCollection.add(_initPair);
                                  final GraphBuilder _gbuilder = new GraphBuilder(_rootCollection);
                                  final IndusMatcher _matcher = new IndusMatcher();
                                  final IQueryProgressListener _listener = new IQueryProgressListener() {

                                    public void queryProgress(QueryProgressEvent arg0) {
                                        monitor.worked(10);
                                        monitor.setTaskName(arg0.getMessage());
                                        
                                    }
                                      
                                  };
                                  IndusInterface.getInstance().setSlicer(KaveriPlugin.getDefault().getSlicerTool());
                                  final IndusExistentialQueryEngine _ieeq = new IndusExistentialQueryEngine(_gbuilder, _fbuilder, _matcher);
                                  _ieeq.addListener(_listener);
                                  _ieeq.execute();                                  
                                  queryResults =  _ieeq.getResults();
                                  Display.getDefault().asyncExec(new Runnable() {
                                      public void run() {
                                          tvLeft.setInput(queryResults);
                                          tvRight.setInput("");
                                      }
                                  });
                              }
                              
                          }
                          monitor.done();
                       }
                    });
                } catch (InvocationTargetException e) {
                   SECommons.handleException(e);
                } catch (InterruptedException e) {
                    SECommons.handleException(e);
                }
            }
        };
        final ImageDescriptor _desc = AbstractUIPlugin
        .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                "data/icons/runQuery.gif");
        _actQuery.setImageDescriptor(_desc);
        manager.add(_actQuery);
        _actQuery.setToolTipText("Run the Query");
        
        final IAction _actUpdate = new Action() {            
            public void run() {
                final PartialStmtData _psd = KaveriPlugin.getDefault()
                	.getIndusConfiguration().getStmtList();
                if (_psd.getStmtList() != null && _psd.getSelectedStatement() != null
                        && _psd.getStmtList().size() > 2 ) {
                    txtStatement.setText(_psd.getSelectedStatement());
                    if (cmbQuery.indexOf("") != -1) {
                        cmbQuery.select(cmbQuery.indexOf(""));
                    } else {
                        cmbQuery.add("");
                    }
                    // Reset the table.
                }
                queryResults = Collections.EMPTY_LIST;
                tvLeft.setInput(queryResults);
            }
        };
        final ImageDescriptor _descUpd = AbstractUIPlugin
        .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                "data/icons/update.gif");
        _actUpdate.setImageDescriptor(_descUpd);
        manager.add(_actUpdate);
        _actUpdate.setToolTipText("Update the statement");
        
    }
    
    
    /**
     * Setup the table.
     * @param table The table.
     */
    private void setupTable(Table table) {
        final TableColumn _col1 = new TableColumn(table, SWT.NONE);
        _col1.setText("Variable name");
        
        final TableColumn _col2 = new TableColumn(table, SWT.NONE);
        _col2.setText("Substituition");
        
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (int _i = 0; _i < table.getColumnCount(); _i++) {
            table.getColumn(_i).pack();
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
       cmbQuery.setFocus();
    }
    
	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		private Map mapping;
		
		public TreeObject(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}
		/**
		 * @return Returns the statement.
		 */
		public Map getMapping() {
			return mapping;
		}
		
		public void setMapping(final Map mapping) {
		    this.mapping = mapping;
		}
	}
	
	class TreeParent extends TreeObject {
		private ArrayList children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
        /**
         * Removes all the children. 
         */
        public void removeAllChildren() {
            for (int i = 0; i < children.size(); i++) {
                ((TreeObject) children.get(i)).setParent(null);
            }
            children.clear();                       
        }
	}

    /**
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content
     *  
     */
    class ViewContentProvider implements ITreeContentProvider {
        private TreeParent invisibleRoot;
        /**
         * Returns the elements to show in the view.
         * 
         * @param parent
         *            The parent.
         * 
         * @return Object[] The list of statements if any present.
         */
        public Object[] getElements(final Object parent) {
            if (invisibleRoot == null) {
                invisibleRoot = new TreeParent("");                
            }
            initialize();
            return getChildren(invisibleRoot);
        }
        
        public void initialize() {
            int _ctr = 1;
            invisibleRoot.removeAllChildren();
            for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
              final Collection _oneResult = (Collection) iter.next();                        
              final TreeParent _parent = new TreeParent("Result " + _ctr);
              for (Iterator iterator = _oneResult.iterator(); iterator.hasNext();) {
                  final IFSMToken _token = (IFSMToken) iterator.next();
                  final TreeObject _object = new TreeObject(_token.getGraphEdge().getSrcNode().toString());                  
                  _object.setMapping(_token.getSubstituitionMap());
                  _parent.addChild(_object);                
              }      
              invisibleRoot.addChild(_parent);
              _ctr++;
            }            
        }



        /**
         * Dispose any created resources.
         */
        public void dispose() {
            System.gc();
        }

        /**
         * The input has changed. Register for receiving any changes.
         * 
         * @param v
         *            The current tvLeft
         * @param oldInput
         *            The old input to the view.
         * @param newInput
         *            The new input to the view.
         */
        public void inputChanged(final Viewer v, final Object oldInput,
                final Object newInput) {
        }



        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TreeParent) {
                return ((TreeParent) parentElement).getChildren();
            }
            return new Object[0];
        }



        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
           if (element instanceof TreeObject) {
               return ((TreeObject) element).getParent();
           }
           return null;
        }



        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            if (element instanceof TreeParent) {
                return ((TreeParent) element).hasChildren();
            } else 
                return false;            
        }
    }

    
    class TableContentProvider implements IStructuredContentProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
           if (inputElement instanceof Map) {
               final Set _entrySet = ((Map) inputElement).entrySet();
               return _entrySet.toArray();
           }
           return new Object[0];
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {         
            
        }
        
    }

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex) {
            String _retString = "";
            if (element instanceof Map.Entry) {
                final Map.Entry _entry = (Map.Entry) element;
                switch (columnIndex) {
                	case 0:
                	    _retString = _entry.getKey().toString();
                	    break;
                	case 1:
                	    _retString = _entry.getValue().toString();
                	    break;
                }
            }
            return _retString;
        }
        
    }
}
