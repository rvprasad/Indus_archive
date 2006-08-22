package edu.ksu.cis.indus.dom;

import static edu.ksu.cis.indus.kaveri.KaveriPlugin.getDefault;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbench;

import soot.SootMethod;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

public final class SlicerDOM {
    
    public IWorkbench getWorkbench() {
        return getWorkbench();
    }
    
    public IWorkspace getWorkSpace() {
        return getWorkspace();
    }
    
    public KaveriPlugin getKaveriPlugin() {
        return getDefault();
    }
    
    public IEscapeInfo getEscapeInfo() {
        return getSlicerTool().getECBA().getEscapeInfo();
    }
    
    public IReadWriteInfo getReadWriteInfo() {
        return getSlicerTool().getECBA().getReadWriteInfo();
    }
    
    public IEnvironment getScene() {
        return getSlicerTool().getSystem();
    }
    
    public SlicerTool getSlicerTool() {
        return getKaveriPlugin().getSlicerTool();
    }
    
    public BasicBlockGraphMgr getBasicBlockGraphManager() {
        return getSlicerTool().getBasicBlockGraphManager();
    }
    
    public ICallGraphInfo getCallGraph() {
        return getSlicerTool().getCallGraph(); 
    }
    
    public IMonitorInfo getMonitorInfo() {
        return getSlicerTool().getMonitorInfo();
    }
    
    public Collection<SootMethod> getRootMethods() {
        return getSlicerTool().getRootMethods();
    }
    
    public Collection<IDependencyAnalysis> getDA(final Comparable<?> id) {
        final Collection<IDependencyAnalysis> _r = new ArrayList<IDependencyAnalysis>();
        for (final Object _i : getSlicerTool().getDAs()) {
            final IDependencyAnalysis _da = (IDependencyAnalysis) _i;
            if (_da.getIds().contains(id)) {
                _r.add(_da);
            }
        }
        return _r;
    }
    
    public Collection<IDependencyAnalysis> getDAs() {
        return getSlicerTool().getDAs();
    }
    
    public IStmtGraphFactory<?> getStmtGraphFactory() {
        return getSlicerTool().getStmtGraphFactory();
    }
}
