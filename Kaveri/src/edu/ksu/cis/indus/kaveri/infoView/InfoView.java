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

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.internal.layout.LayoutCache;
import org.jibx.runtime.JiBXException;

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.callgraph.ContextContentProvider;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;
import edu.ksu.cis.indus.kaveri.views.CriteriaListMaintainer;
import edu.ksu.cis.indus.kaveri.views.CriteriaView;
import edu.ksu.cis.indus.kaveri.views.IDeltaListener;
import edu.ksu.cis.indus.kaveri.views.PartialSliceView;

/**
 * @author ganeshan
 * 
 * This view displays the updated information regarding the scope, contexts and
 * the slice statistics.
 */
public class InfoView extends CriteriaView implements IDeltaListener {

    /**
     * The viewer for the contexts.
     */
    private TableViewer ctxViewer;

    /**
     * The set of scope specifications.
     */
    private SpecificationBasedScopeDefinition sbsd;

    /**
     * The viewer for the scope information.
     * 
     */
    private TableViewer scopeViewer;

    /**
     * Creates the context display dialog.
     * 
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

        ctxViewer = new TableViewer(_comp, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
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
        ctxViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getCtxRepository());
        for (int _i = 0; _i < _colNames.length; _i++) {
            _table.getColumn(_i).pack();
        }

        return _comp;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        final GridLayout _layout = new GridLayout(1, true);
        _comp.setLayout(_layout);
        final TabFolder _folder = new TabFolder(_comp, SWT.NONE);
        _folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
        final TabItem _item1 = new TabItem(_folder, SWT.NONE);
        _item1.setText("Scope");
        _item1.setControl(createScopeTab(_folder));

        final TabItem _item2 = new TabItem(_folder, SWT.NONE);
        _item2.setText("Call Contexts");
        _item2.setControl(createContextTab(_folder));

        final TabItem _item3 = new TabItem(_folder, SWT.NONE);
        _item3.setText("Criteria List");
        _item3.setControl(layoutControls(_folder));

        KaveriPlugin.getDefault().getIndusConfiguration().getInfoBroadcaster().addListener(this);
    }

    /**
     * Create the scope tab.
     * 
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

        scopeViewer = new TableViewer(_comp, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);

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
            public void controlResized(@SuppressWarnings("unused")
            ControlEvent e) {
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
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        KaveriPlugin.getDefault().getIndusConfiguration().getInfoBroadcaster().removeListenere(this);
    }

    /**
     * Returns the current set of criteria.
     * 
     * @return
     */
    protected CriteriaData getCurrentCriteriaList() {
        final CriteriaListMaintainer _m = KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer();
        final IProject _prj = _m.getProject();
        if (_prj != null) {
            try {
                return PartialSliceView.retrieveCriteria(_prj);
            } catch (CoreException _e) {
                SECommons.handleException(_e);
            } catch (IOException _e) {
                SECommons.handleException(_e);
            }
        }
        return null;
    }

    /**
     * Initialize the variable sbsd with the specification stored in the
     * project.
     */
    private void initializeScopeSpecification() {
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        final String _scopeSpecKey = "edu.ksu.cis.indus.kaveri.scope";
        String _scopeSpec = _ps.getString(_scopeSpecKey);
        if (_scopeSpec.equals("")) {
            sbsd = new SpecificationBasedScopeDefinition();
        } else {
            try {
                sbsd = SpecificationBasedScopeDefinition.deserialize(_scopeSpec);
            } catch (JiBXException _jbe) {
                SECommons.handleException(_jbe);
                KaveriErrorLog.logException("JiBx Exception", _jbe);
                sbsd = null;
            }
        }

    }

    /**
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
     */
    public boolean isReady() {
        return false;
    }

    /**
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
     */
    public void propertyChanged() {
        initializeScopeSpecification();
        scopeViewer.setInput(sbsd);
        ctxViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getCtxRepository());
    }

    /**
     * Save the new criteria.
     * 
     * @param prj DOCUMENT ME!
     * @param cd DOCUMENT ME!
     */
    protected void saveNewCriteria(IProject prj, CriteriaData cd) {
        if (prj != null) {
            try {
                PartialSliceView.saveCriteria(prj, cd);
            } catch (IOException _e) {
                SECommons.handleException(_e);
            }
        }
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // TODO Auto-generated method stub
    }
}
