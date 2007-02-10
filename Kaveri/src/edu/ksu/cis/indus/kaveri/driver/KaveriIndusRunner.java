/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.driver;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.decorator.IndusDecorator;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.kaveri.soot.SootIndusTagCleaner;
import edu.ksu.cis.indus.tools.IToolProgressListener;

/**
 * This does the bulk of the call to the eclipse indus driver. The settings for the slice are currently stored in
 * IndusConfiguration
 * 
 * @author Ganeshan
 */
public class KaveriIndusRunner
		extends AbstractIndusRunner
		implements IRunnableWithProgress {

	/**
	 * Creates a new KaveriIndusRunner object.
	 * 
	 * @param filesList The file pointing to the java file being sliced
	 * @param progressBar The slice progress bar to which to report the messages.
	 * @param cpSet The collection of classpath entries.
	 */
	public KaveriIndusRunner(final List filesList, SliceProgressBar progressBar, final Set cpSet) {
		super(filesList, progressBar, cpSet);
	}

	/**
	 * Sets the current editor. Used to show the highlighting in case of backward and forward slicing.
	 * 
	 * @param ceditor The editor to set.
	 */
	public void setEditor(final CompilationUnitEditor ceditor) {
		this.editor = ceditor;
	}

	/**
	 * Sets up the slice parameters.
	 * 
	 * @return boolean True if the slicer was set up properly.
	 */
	public boolean doWork() {
		return setUp();
	}

	/**
	 * Runs the slice using the driver.
	 * 
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IProgressMonitor monitor) throws InterruptedException {
		monitor.beginTask(Messages.getString("AbstractIndusRunner.1"), 100); //$NON-NLS-1$
		final Collection _ctx = KaveriPlugin.getDefault().getIndusConfiguration().getChosenContext();
		if (_ctx.size() > 0) {
			processContexts(_ctx);
		}
		String _stag = "EclipseIndusTag";
		_stag = _stag + System.currentTimeMillis();
		final String _oldTag = driver.getNameOfSliceTag();
		driver.setNameOfSliceTag(_stag);

		IToolProgressListener _tpl = new IToolProgressListener() {

			int _ctr = 1;

			long _currTime = 0;

			String _newMsg = null;

			public void toolProgess(final ToolProgressEvent arg0) {
				_ctr++;
				if (!monitor.isCanceled()) {
					monitor.worked(_ctr);
					if (_newMsg == null) {
						_newMsg = arg0.getMsg();
						_currTime = System.currentTimeMillis();
					} else {
						final long _newTimeDelta = System.currentTimeMillis() - _currTime;
						_currTime = _newTimeDelta + _currTime;
						Display.getDefault().asyncExec(new Runnable() {

							public void run() {
								bar.addSliceMessage(_newMsg + " Time: " + _newTimeDelta + " ms");
							}
						});
						_newMsg = arg0.getMsg();
					}
					// System.out.println(arg0.getMsg());
				} else {
					opCancelled = true;
				}
			}
		};

		driver.getSlicer().addToolProgressListener(_tpl);
		driver.execute();
		driver.getSlicer().removeToolProgressListener(_tpl);
		if (opCancelled) {
			throw new InterruptedException("Slice was stopped");
		}

		final IndusDecorator _decorator = IndusDecorator.getIndusDecorator();
		if (fileList.size() > 0) {
			final IFile _file = (IFile) fileList.get(0);
			final IProject _pr = _file.getProject();
			KaveriPlugin.getDefault().getIndusConfiguration().setSliceProject(_pr);
			final List _flist = completeFileList;
			if (_flist != null) {
				KaveriPlugin.getDefault().getIndusConfiguration().setSliceFileList(_flist);
			}
			completeFileList = _flist;
		}

		if (_decorator != null) {
			_decorator.refesh();
		}

		if (editor != null) {
			highlightEditor();
		}

		returnCriteriaToPool();
		monitor.done();
		KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
		if (!_oldTag.equals("")) {
			final SootIndusTagCleaner _job = new SootIndusTagCleaner("Clean Soot Indus tags", _oldTag);
			_job.schedule(10000);
		}
	}

}
