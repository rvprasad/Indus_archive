
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
 * Created on Jun 4, 2004
 *
 */
package edu.ksu.cis.indus.kaveri.decorator;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.presentation.TagToAnnotationMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IDecoratorManager;


/**
 * Decorates the resources if the slice includes the file.
 *
 * @author Ganeshan
 */
public class IndusDecorator
  extends LabelProvider
  implements ILightweightLabelDecorator {
		
	
	/**
	 * Returns the static instance of the decorator.
	 *
	 * @return IndusDecorator The decorator.
	 */
	public static IndusDecorator getIndusDecorator() {
		final IDecoratorManager _manager = KaveriPlugin.getDefault().getWorkbench().getDecoratorManager();
		IndusDecorator _decorator = null;

		if (_manager.getEnabled("edu.ksu.cis.indus.kaveri.decorator")) {
			_decorator =
				(IndusDecorator) KaveriPlugin.getDefault()
					.getWorkbench().getDecoratorManager().getBaseLabelProvider(
							"edu.ksu.cis.indus.kaveri.decorator");
		}
		return _decorator;
	}

	/**
	 * Decorates the java files in Eclipse if they are included in the slice.
	 *
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * 		org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(final Object element, final IDecoration decoration) {
		//  System.out.println(element);
		//final IResource _resource = getResource(element);
		IResource _resource = null;

		if (element instanceof IFile) {
			_resource = (IFile) element;
		}

		final List _filelst = KaveriPlugin.getDefault().getIndusConfiguration().getSliceFileList();

		if (_filelst.size() > 0) {			
			
			if (_resource != null
				  && _resource.getType() == IResource.FILE
				  && _resource.getFileExtension().equalsIgnoreCase("java")
			) {
				if (_filelst.contains(_resource) && isFileOkToDecorate(_resource)) {
					decoration.addOverlay(KaveriPlugin.getDefault().getIndusConfiguration().getSliceDecorator());
				}
			}
		}
	}


	/**
	 * Refresh.
	 */
	public void refesh() {
		final IndusDecorator _decorator = getIndusDecorator();

		if (_decorator != null) {
			_decorator.fireLabelEvent(new LabelProviderChangedEvent(_decorator));
		}
	}

	/**
	 * Determines if any of the classes in the Java file have a slice associated so that the file can be annotated.
	 *
	 * @param resource The Java file
	 * 
	 *
	 * @return boolean True if the file should be decorated.
	 */
	private boolean isFileOkToDecorate(final IResource resource) {
		boolean _isFileOk = false;
		final IFile _file  = (IFile) resource;
		final IProject _project = KaveriPlugin.getDefault().getIndusConfiguration()
		.getSliceProject();
		final IProject _sliceProject = _file.getProject();
		if (_project != null && _project == _sliceProject) {
			final Map decorateMap = KaveriPlugin.getDefault().getCacheMap();
			final List _lst = SECommons.getClassesInFile(_file);
			for (int _i = 0; _i < _lst.size(); _i++) {
				final String _classname = (String) _lst.get(_i);
				
				if (decorateMap.get(_classname) != null) {
					_isFileOk = true; break;
				}
			}
			final Map _map = TagToAnnotationMapper.getAnnotationLinesForFile(_file);
			if (_map.size() > 0) {
				final Iterator _it = _map.keySet().iterator();
				while (_it.hasNext()) {
					final Object _key = _it.next();
					final Map _value = (Map) _map.get(_key);
					if (_value.size() > 0) {
						decorateMap.put(_key, _value);
						_isFileOk = true;
					}					
				}				
			}
		}
		return _isFileOk;
	}

	/**
	 * Fires the label provider change event causing the decoration.
	 *
	 * @param event The label provider changed event
	 */
	private void fireLabelEvent(final LabelProviderChangedEvent event) {
		Display.getDefault().syncExec(new Runnable() {
				public void run() {
					fireLabelProviderChanged(event);
				}
			});
	}
}
