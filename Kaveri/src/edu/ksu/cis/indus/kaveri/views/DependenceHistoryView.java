/*
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;

/**
 * REWRITE
 * <p>
 * Partial slice view. This class creates a view that shows the Jimple
 * statements for a given Java statement and indicates if they have a slice tag
 * or not. This view is only active after slicing has been performed.
 * </p>
 */
public class DependenceHistoryView extends ViewPart {
    /**
     * <p>
     * The table tvLeft for this view.
     * </p>
     */
    private TableTreeViewer viewer;

    private Action actionGotoSource;

    private Action actionFwd;

    private Action actionBck;

    private Action doubleClickAction;

    /**
     * The constructor.
     */
    public DependenceHistoryView() {
    }

    class TreeObject implements IAdaptable {
        private String name;

        private TreeParent parent;

        private String statement;

        private IFile file;

        private int lineNumber;

        private String dependencyTracked;

        private int index;

        private DependenceStackData dStack;

        public TreeObject(String stmt) {
            this.statement = stmt;
        }

        public void setParent(TreeParent parent) {
            this.parent = parent;
        }

        public TreeParent getParent() {
            return parent;
        }

        public String toString() {
            return getStatement();
        }

        public Object getAdapter(Class key) {
            return null;
        }

        /**
         * @return Returns the dependencyTracked.
         */
        public String getDependencyTracked() {
            return dependencyTracked;
        }

        /**
         * @param dependencyTracked
         *            The dependencyTracked to set.
         */
        public void setDependencyTracked(String dependencyTracked) {
            this.dependencyTracked = dependencyTracked;
        }

        /**
         * @return Returns the file.
         */
        public IFile getFile() {
            return file;
        }

        /**
         * @param file
         *            The file to set.
         */
        public void setFile(IFile file) {
            this.file = file;
        }

        /**
         * @return Returns the lineNumber.
         */
        public int getLineNumber() {
            return lineNumber;
        }

        /**
         * @param lineNumber
         *            The lineNumber to set.
         */
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        /**
         * @return Returns the statement.
         */
        public String getStatement() {
            return statement;
        }

        /**
         * @return Returns the Index of this node.
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index
         *            The index to set.
         */
        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * @return Returns the dStack.
         */
        public DependenceStackData getDStack() {
            return dStack;
        }

        /**
         * @param stack
         *            The dStack to set.
         */
        public void setDStack(DependenceStackData stack) {
            dStack = stack;
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

        public void removeAllChildren() {
            for (int i = 0; i < children.size(); i++) {
                TreeObject child = (TreeObject) children.get(i);
                child.setParent(null);
            }
            children.clear();
        }

        public TreeObject[] getChildren() {
            return (TreeObject[]) children.toArray(new TreeObject[children
                    .size()]);
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }
    }

    /**
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content
     *  
     */
    class DependenceHistoryViewContentProvider implements ITreeContentProvider,
            IDeltaListener {
        private TreeParent invisibleRoot;

        /**
         * Dispose any created resources.
         */
        public void dispose() {
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
            if (oldInput != null) {
                ((DependenceHistoryData) oldInput).removeListener(this);
            }

            if (newInput != null) {
                ((DependenceHistoryData) newInput).addListener(this);
            }
        }

        /**
         * The slice statement list has changed. Refresh the view.
         * 
         * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
         */
        public void propertyChanged() {
            if (viewer != null) {
                initialize();
                final DependenceHistoryData _dd = KaveriPlugin.getDefault()
                        .getIndusConfiguration().getDepHistory();
                actionBck.setEnabled(_dd.isBackNavPossible());
                actionFwd.setEnabled(_dd.isFwdNavPossible());

                viewer.refresh(false);
                final Object _obj[] = invisibleRoot.getChildren();
                if (_obj != null && _obj.length > 0) {
                    viewer.setSelection(new StructuredSelection(_obj[0]), true);
                }

                final TableColumn _cols[] = viewer.getTableTree().getTable()
                        .getColumns();
                for (int _i = 0; _i < _cols.length; _i++) {
                    _cols[_i].pack();
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parent) {
            if (parent instanceof TreeParent) {
                return ((TreeParent) parent).getChildren();
            }
            return new Object[0];
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object child) {
            if (child instanceof TreeObject) {
                return ((TreeObject) child).getParent();
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object parent) {
            if (parent instanceof TreeParent)
                return ((TreeParent) parent).hasChildren();
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object parent) {
            if (invisibleRoot == null) {
                invisibleRoot = new TreeParent("");
                initialize();
            }
            return getChildren(invisibleRoot);

        }

        private void initialize() {
            invisibleRoot.removeAllChildren();
            final List _lst = KaveriPlugin.getDefault().getIndusConfiguration()
                    .getDepHistory().getContents();
            final Object[] _retObj;
            if (!_lst.isEmpty()) {
                _retObj = _lst.toArray();

                for (int i = 0; i < _retObj.length; i++) {
                    final Pair _pair = (Pair) _retObj[i];
                    final DependenceStackData _data = (DependenceStackData) _pair
                            .getFirst();
                    final IFile _file = _data.getFile();
                    final String _stmt = _data.getStatement();
                    final int lineNo = _data.getLineNo();

                    final String _depLink = _pair.getSecond().toString();
                    final TreeParent _tp = new TreeParent(_stmt);

                    _tp.setDependencyTracked(_depLink);
                    _tp.setFile(_file);
                    _tp.setLineNumber(lineNo);
                    _tp.setIndex(_retObj.length - i - 1);
                    _tp.setDStack(_data);
                    //_to.setDepColor(_data.getDepColor());

                    invisibleRoot.addChild(_tp);
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

    /**
     * <p>
     * This class provides the labels for the elements shown in the view.
     * </p>
     * 
     *  
     */
    class DependenceHistoryViewLabelProvider extends LabelProvider implements
            ITableLabelProvider, IColorProvider {

        /**
         * Get the image label for the given column.
         * 
         * @param obj
         *            The object for which the image is needed
         * @param index
         *            The column
         * @return Image The image for the given column
         */
        public Image getColumnImage(final Object obj, final int index) {
            return null;
        }

        /**
         * Returns the textual representation of the element to be shown.
         * 
         * @param element
         *            The object whose value is to be shown
         * @param index
         *            The column number
         * 
         * @return String The textual representation of the object
         */
        public String getColumnText(final Object element, final int index) {
            String _retString = "";

            if (element instanceof TreeParent) {
                switch (index) {
                case 0:
                    _retString = element.toString();
                    break;
                case 1:
                    _retString = ((TreeParent) element).getFile().getName();
                    break;
                case 2:
                    _retString = ((TreeParent) element).getLineNumber() + "";
                    break;
                case 3:
                    _retString = ((TreeParent) element).getDependencyTracked();
                    break;
                default:
                    _retString = "";
                }

            } /*
               * else if (element instanceof TreeObject) { switch (index) { case
               * 1: break; case 2: break; case 3: _retString = ((TreeObject)
               * element).getDependencyTracked(); break; default: _retString =
               * ""; } }
               */

            return _retString;
        }

        /**
         * Returns the image label for the given object.
         * 
         * @param obj
         *            The object for which the image label is needed.
         * @return Image The image for the given object.
         */
        public Image getImage(final Object obj) {
            return null;
        }

        public Color getForeground(Object element) {
            Color _retColor = null;
            if (element instanceof TreeParent) {
                final TreeParent _tp = (TreeParent) element;
                if (_tp.getDependencyTracked().equals("Starting Program Point")) {
                    final RGB _rgb = new RGB(255, 0, 0);
                    if (_rgb != null) {
                        _retColor = KaveriPlugin.getDefault()
                                .getIndusConfiguration().getRManager()
                                .getColor(_rgb);
                    }
                }
            }
            return _retColor;
        }

        public Color getBackground(Object element) {
            return null;
        }

    }

    /**
     * Passing the focus request to the tvLeft's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * This is a callback that will allow us to create the tvLeft and initialize
     * it.
     * 
     * @param parent
     *            The parent control
     */
    public void createPartControl(final Composite parent) {
        viewer = new TableTreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION);
        viewer.getTableTree().getTable().setLinesVisible(true);
        viewer.getTableTree().getTable().setHeaderVisible(true);
        final Table _table = viewer.getTableTree().getTable();
        setUpTable(_table);
        viewer.setAutoExpandLevel(1);
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new DependenceHistoryViewContentProvider());
        viewer.setLabelProvider(new DependenceHistoryViewLabelProvider());
        viewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getDepHistory());

        parent.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                final TableColumn _columns[] = _table.getColumns();
                for (int i = 0; i < _columns.length; i++) {
                    _columns[i].pack();
                }

                /*
                 * TableColumn _col1 = _table.getColumn(0);
                 * _col1.setWidth(parent.getSize().x / 4 ); _col1 =
                 * _table.getColumn(1); _col1.setWidth(parent.getSize().x / 4);
                 * _col1 = _table.getColumn(2);
                 * _col1.setWidth(parent.getSize().x / 6); _col1 =
                 * _table.getColumn(3); _col1.setWidth(parent.getSize().x / 3);
                 */
            }
        });

        //tvLeft = new CheckboxTableViewer(_table);
        final IToolBarManager _manager = getViewSite().getActionBars()
                .getToolBarManager();
        fillToolBar(_manager);
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
    }

    /**
     * @param _table
     */
    /*
     * private void updateTable(Table _table) { _table.setLinesVisible(true);
     * _table.setHeaderVisible(true);
     * 
     * final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
     * _col1.setText("Statement");
     * 
     * final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
     * _col2.setText("Part of Slice"); _col1.pack(); _col2.pack(); }
     */

    /**
     * Fills the toolbar.
     * 
     * @param manager
     */
    private void fillToolBar(IToolBarManager manager) {
        actionBck = new Action() {
            public void run() {
                final DependenceHistoryData _dd = KaveriPlugin.getDefault()
                        .getIndusConfiguration().getDepHistory();
                _dd.navigateBack();
                highlightCurrentItem();
            }
        };
        actionBck.setToolTipText("Move Back");
        final ImageDescriptor _descB = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/viewBack.gif");

        actionBck.setImageDescriptor(_descB);
        manager.add(actionBck);
        final DependenceHistoryData _dd = KaveriPlugin.getDefault()
                .getIndusConfiguration().getDepHistory();
        actionBck.setEnabled(_dd.isBackNavPossible());

        actionFwd = new Action() {
            public void run() {
                final DependenceHistoryData _dd = KaveriPlugin.getDefault()
                        .getIndusConfiguration().getDepHistory();
                _dd.navigateForward();
                highlightCurrentItem();
            }
        };
        actionFwd.setToolTipText("Move Forward");
        final ImageDescriptor _desc = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/viewFront.gif");

        actionFwd.setImageDescriptor(_desc);
        actionFwd.setEnabled(_dd.isFwdNavPossible());
        manager.add(actionFwd);
    }

    /**
     * Highlight the current entry on the top of the stack in the Java editor.
     *  
     */
    private void highlightCurrentItem() {
        final DependenceHistoryData _dd = KaveriPlugin.getDefault()
                .getIndusConfiguration().getDepHistory();
        final DependenceStackData _ds = (DependenceStackData) _dd
                .getCurrentItem().getFirst();
        final IFile _file = _ds.getFile();
        final int _nLineNo = _ds.getLineNo() - 1;
        final ICompilationUnit _cunit = JavaCore
                .createCompilationUnitFrom(_file);
        try {
            final CompilationUnitEditor _cu = (CompilationUnitEditor) JavaUI
                    .openInEditor(_cunit);
            final IRegion _region = _cu.getDocumentProvider().getDocument(
                    _cu.getEditorInput()).getLineInformation(_nLineNo);
            _cu.selectAndReveal(_region.getOffset(), _region.getLength());
        } catch (PartInitException e) {
            SECommons.handleException(e);
            KaveriErrorLog.logException("Error opening Java Editor", e);
        } catch (JavaModelException e) {
            SECommons.handleException(e);
            KaveriErrorLog.logException("Java Model Exception", e);
        } catch (BadLocationException e) {
            SECommons.handleException(e);
            KaveriErrorLog.logException("Bad Location Exception", e);
        }
    }

    /**
     * Run the double click action
     */
    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    /**
     * Creates the popup menu actions.
     *  
     */
    private void makeActions() {
        actionGotoSource = new Action() {
            public void run() {
                highlightSource();
            }
        };
        actionGotoSource.setText("Goto Source");
        doubleClickAction = new Action() {
            public void run() {
                highlightSource();
            }
        };
    }

    private void highlightSource() {
        final ISelection _sel = viewer.getSelection();
        if (_sel != null && _sel instanceof IStructuredSelection) {
            IStructuredSelection _ssel = (IStructuredSelection) _sel;
            final Object _obj = _ssel.getFirstElement();
            IJavaElement _elem = null;
            int _navToIndex = -1;
            int nlineno = -1;
            DependenceStackData _dsd = null;
            if (_obj != null && _obj instanceof TreeParent) {
                final TreeParent _tp = (TreeParent) _obj;
                final IFile _file = _tp.getFile();
                _elem = JavaCore.create(_file);
                nlineno = _tp.getLineNumber();
                _navToIndex = _tp.getIndex();
                _dsd = _tp.getDStack();

            } else if (_obj != null && _obj instanceof TreeObject) {
                final TreeObject _to = (TreeObject) _obj;
                final IFile _file = _to.getFile();
                _elem = JavaCore.create(_file);
                nlineno = _to.getLineNumber();
                _navToIndex = _to.getIndex();
                _dsd = _to.getDStack();
            }
            if (_elem != null) {
                try {
                    final CompilationUnitEditor _editor = (CompilationUnitEditor) JavaUI
                            .openInEditor(_elem);
                    final IRegion _region = _editor.getDocumentProvider()
                            .getDocument(_editor.getEditorInput())
                            .getLineInformation(nlineno - 1);
                    _editor.selectAndReveal(_region.getOffset(), _region
                            .getLength());
                    KaveriPlugin.getDefault().getIndusConfiguration()
                            .getDepHistory().navigateTo(_navToIndex);
                } catch (PartInitException e) {
                    KaveriErrorLog.logException("Par Init Exception", e);
                    SECommons.handleException(e);
                } catch (JavaModelException e) {
                    KaveriErrorLog.logException("Java Model Exception", e);
                    SECommons.handleException(e);
                } catch (org.eclipse.jface.text.BadLocationException e) {
                    KaveriErrorLog.logException("Bad Location Exception", e);
                    SECommons.handleException(e);
                }
            }
        }
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                DependenceHistoryView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(actionGotoSource);
        //manager.add(actionFwd);
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Creates the table.
     * 
     * @param parent
     *            The parent composite
     * 
     * @return Table The table
     */
    private void setUpTable(final Table table) {
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        final TableColumn _col1 = new TableColumn(table, SWT.NONE);
        _col1.setText("Statement");

        final TableColumn _col2 = new TableColumn(table, SWT.NONE);
        _col2.setText("Filename");

        final TableColumn _col3 = new TableColumn(table, SWT.NONE);
        _col3.setText("Line number");

        final TableColumn _col4 = new TableColumn(table, SWT.NONE);
        _col4.setText("Relation with previous item");
    }
}