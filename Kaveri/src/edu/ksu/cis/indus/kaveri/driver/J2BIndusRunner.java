
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

/*
 * Created on Apr 5, 2004
 *
 *
 *
 */
package edu.ksu.cis.indus.kaveri.driver;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;

import edu.ksu.cis.indus.tools.IToolProgressListener;

import edu.ksu.cis.j2b.J2BEclipsePlugin;

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import soot.G;


/**
 * This does the bulk of the call to the eclipse indus driver. The settings for the slice are currently stored in
 * IndusConfiguration
 *
 * @author Ganeshan
 */
public class J2BIndusRunner
  extends AbstractIndusRunner
  implements IRunnableWithProgress {
	/**
	 * Creates a new KaveriIndusRunner object.
	 *
	 * @param filesList The file pointing to the java file being sliced
	 * @param bar The slice progress bar to which to report the messages.
	 * @param cpSet The collection of classpaths. An empty set indicates recalcuate the class paths.
	 */
	public J2BIndusRunner(final List filesList, SliceProgressBar bar, final Set cpSet) {
		super(filesList, bar, cpSet);
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
	public void run(final IProgressMonitor monitor)
	  throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Running J2B slice", 100);  //$NON-NLS-1$

		final Collection _ctx = KaveriPlugin.getDefault().getIndusConfiguration().getChosenContext();

		if (_ctx.size() > 0) {
			processContexts(_ctx);
		}

		String _stag = "EclipseIndusTag";
		_stag = _stag + System.currentTimeMillis();

		final String _oldTag = driver.getNameOfSliceTag();
		driver.setNameOfSliceTag(_stag);

		driver.getSlicer().addToolProgressListener(new IToolProgressListener() {
				int _ctr = 1;
				long _currTime;
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

						//System.out.println(arg0.getMsg());
					} else {
						opCancelled = true;
					}
				}
			});

		driver.execute();

		// Residualize the scene
		driver.residualize();

		final Bundle _bundle = Platform.getBundle("edu.ksu.cis.j2b.J2BEclipse");

		if (_bundle != null) {
			int _state = _bundle.getState();

			if (_state == Bundle.UNINSTALLED || _state == Bundle.STOPPING) {
				try {
					_bundle.start();

					final J2BEclipsePlugin _j =
						(J2BEclipsePlugin) _bundle.loadClass("edu.ksu.cis.j2b.J2BEclipsePlugin").getConstructor(null)
													.newInstance(null);
					_j.generateAndWriteBIRSystem(driver.getScene(), ((IFile) fileList.get(0)).getProject(),
						driver.getSlicer().getAtomicityInfo());
				} catch (final BundleException _e) {
					log("The bundle couldnot be started.", _e);
				} catch (final IllegalArgumentException _e) {
					log("Zero-parameter constructor could not be found.", _e);
				} catch (final SecurityException _e) {
					log("Insufficient permission to access J2B bundle.", _e);
				} catch (final InstantiationException _e) {
					log("Could not instantiate J2B plugin.", _e);
				} catch (final IllegalAccessException _e) {
					log("Insufficient access permision to classes in J2B bundle.", _e);
				} catch (final NoSuchMethodException _e) {
					log("Zero-parameter constructor could not be found.", _e);
				} catch (final ClassNotFoundException _e) {
					log("edu.ksu.cis.j2b.J2BEclipsePlugin could not be found.", _e);
				}
			}
		}

		// Cancel pressed.
		if (opCancelled) {
			throw new InterruptedException("Slice was stopped");
		}

		returnCriteriaToPool();
		monitor.done();
		// Change the tag so that the view remain in a consistent state.
		_stag = "EclipseIndusTag";
		_stag = _stag + System.currentTimeMillis();
		driver.setNameOfSliceTag(_stag);

		resetSoot();
	}

	

	/**
	 * Logs the given message and throws a runtime exception.
	 *
	 * @param string is the message.
	 * @param e that is being logged.
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	private void log(final String string, final Throwable e) {
		final KaveriPlugin _default = KaveriPlugin.getDefault();
		_default.getLog().log(new Status(IStatus.ERROR, _default.getBundle().getSymbolicName(), IStatus.ERROR, string, e));
		throw new RuntimeException(e);
	}

	/**
	 * Reset the effects of the slice on the system.
	 */
	private void resetSoot() {
		G.reset();
		KaveriPlugin.getDefault().getIndusConfiguration().reset();
		KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
	}
}

// End of File
