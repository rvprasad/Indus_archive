
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

/*
 * Created on Aug 18, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


import soot.G;
import soot.Scene;
import soot.SootClass;

import edu.ksu.cis.indus.kaveri.common.SECommons;


/**
 * Listens to resource changes and updates the scene as required.
 *
 * @author ganeshan 
 */
public class JavaClassChangeListener
  implements IResourceChangeListener {
	/**
	 * A resource has changed. Process it. 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		System.out.println(event.getType());
		switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE:
				try {
				event.getDelta().accept(new JavaClassChangeVisitor());
				} catch (CoreException _ce) {
					SECommons.handleException(_ce);
				}
			default: return;	
		}
	}
}


class JavaClassChangeVisitor implements IResourceDeltaVisitor
{

	/** Visit the change.
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		final IResource _res = delta.getResource();		
		if (_res.getType() == IResource.FILE && ((IFile) _res).getFileExtension() != null) {
			if (_res.getFileExtension().equalsIgnoreCase("java")) {
				final IWorkspace _workspace = ResourcesPlugin.getWorkspace();
				switch (delta.getKind()) {
				 case IResourceDelta.ADDED :				 	
				 	Thread t = new Thread(new ResourceChangeProcessor((IFile) _res, delta));
				 	t.start();
				 	break;
				 case IResourceDelta.REMOVED:
				 	t = new Thread(new ResourceChangeProcessor((IFile) _res, delta));
				 	t.start();				 	
				 	break;
				 case IResourceDelta.CHANGED:				 	
				 	t = new Thread(new ResourceChangeProcessor((IFile) _res, delta));
				 	t.start();
				 	break;
				}
			}
			
		}
		return true;
	}
	
}


class ResourceChangeProcessor implements Runnable {
 private IFile file;
 private IResourceDelta delta;
	/**
	 * Constructor.
	 * @param jfile
	 * @param rdelta
	 */
	public ResourceChangeProcessor(IFile jfile, IResourceDelta rdelta) {
		file = jfile; delta = rdelta;
	}
	
	/** Run the operation.
	 * 
	 */
	public void run()  {
	 if (file != null && delta != null) {
	 	final IWorkspace _workspace = ResourcesPlugin.getWorkspace();
	 	final Scene _scene =  KaveriPlugin.getDefault().getIndusConfiguration().
		getEclipseIndusDriver().getScene();
	 	switch(delta.getKind()) {
	 	case IResourceDelta.ADDED :		 			 	
	 			if (_scene != null) {
	 				final List _lst = SECommons.getClassesInFile(file);
	 				if  (_lst != null && _lst.size() > 0) {
	 					//G.reset();
	 					handleClassesInFile(_lst, file, true, false);
	 				}
	 			}
		 	break;
		 case IResourceDelta.REMOVED:
 			if (_scene != null) {
 				if (_scene != null) {
 					//G.reset();
	 				final List _lst = SECommons.getClassesInFile(file);
	 				SECommons.loadupClasses(_lst, file);	 				
	 			}
 			}
		 	break;
		 case IResourceDelta.CHANGED:		 	
 			if (_scene != null) {
 				//G.reset(); 				
 				final List _lst = SECommons.getClassesInFile(file);
 				SECommons.loadupClasses(_lst, file);
 				handleClassesInFile(_lst, file, false, true);
 				
 			}
		 	break; 		 
	 	}
	 }
		
	}

	/**
	 * Updates the scene.
	 * @param lst
	 * @param jfile
	 * @param addFiles Indicates if files are to be added
	 * @param update Indicates that existing files are to be updated.
	 */
	private void handleClassesInFile(List lst, IFile jfile,
			 boolean addFiles, boolean update) {
	 	final Scene _scene =  KaveriPlugin.getDefault().getIndusConfiguration().
		getEclipseIndusDriver().getScene();
	 	if (update && _scene != null) {
	 		for (int _i = 0; _i < lst.size(); _i++) {
				final String _className = (String) lst.get(_i);
				try {
				final SootClass _sc = _scene.getSootClass(_className);
				if (_sc != null) {
					_scene.removeClass(_sc);
				}
				} catch (RuntimeException _rme) {
					
				}
			}
	 		SECommons.loadupClasses(lst, jfile);
	 	} else {
	 		if (addFiles) {
	 			SECommons.loadupClasses(lst, jfile);
	 		}
	 		else if (_scene != null){
	 			for (int _i = 0; _i < lst.size(); _i++) {
					final String _className = (String) lst.get(_i);
					try {
					final SootClass _sc = _scene.getSootClass(_className);
					if (_sc != null) {
						_scene.removeClass(_sc);
					}
					} catch (RuntimeException _rme) {
						
					}
				}
	 		}
	 	}
		
	}
	
}