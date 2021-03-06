
/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

/*
 * Created on Apr 5, 2004
 *
 *
 *
 */
package edu.ksu.cis.indus.kaveri.driver;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.tools.IToolProgressListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import soot.G;
//import edu.ksu.cis.j2b.J2BEclipsePlugin;


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

		final Bundle _bundle = Platform.getBundle("edu.ksu.cis.j2b");

		if (_bundle != null) {
			int _state = _bundle.getState();

			if (_state == Bundle.INSTALLED || _state == Bundle.RESOLVED) {
				try {
					_bundle.start();

					final Class _j2bClass = _bundle.loadClass("edu.ksu.cis.j2b.J2BEclipsePlugin");
					final Method _method = _j2bClass.getMethod("getDefault", null);
                    final Object _o = _method.invoke(null, null);
    //                final J2BEclipsePlugin _j = (J2BEclipsePlugin) _o;
      /*              Display.getDefault().asyncExec(new Runnable() {
                       public void run() {
       					_j.generateAndWriteBIRSystem(driver.getScene(), ((IFile) fileList.get(0)).getProject(),
       							driver.getSlicer().getAtomicityInfo());                           
                       }
                    });*/
				} catch (final BundleException _e) {
					log("The bundle couldnot be started.", _e);
				} catch (final IllegalArgumentException _e) {
					log("Zero-parameter constructor could not be found.", _e);
				} catch (final SecurityException _e) {
					log("Insufficient permission to access J2B bundle.", _e);
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
		KaveriPlugin.getDefault().getIndusConfiguration().resetAll();
		KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
	}
}

// End of File
