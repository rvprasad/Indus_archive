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
package edu.ksu.cis.indus.toolkits.sliceeclipse.decorator;

import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
		final IDecoratorManager _manager = SliceEclipsePlugin.getDefault()
				.getWorkbench().getDecoratorManager();
		IndusDecorator _decorator = null;

		if (_manager.getEnabled("edu.ksu.cis.indus.sliceeclipse.decorator")) {
			_decorator = (IndusDecorator) SliceEclipsePlugin.getDefault()
					.getWorkbench().getDecoratorManager().getBaseLabelProvider(
							"edu.ksu.cis.indus.sliceeclipse.decorator");
		}
		return _decorator;
	}

	/**
	 * Decorates the files if they are included in the slice.
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 *      org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(final Object element, final IDecoration decoration) {
		//  System.out.println(element);
		//final IResource _resource = getResource(element);
		IResource _resource = null;

		if (element instanceof IFile) {
			_resource = (IFile) element;
		}

		final List _filelst = SliceEclipsePlugin.getDefault().getIndusConfiguration().getSliceFileList();

		if (_filelst.size() > 0) {
			boolean _shouldDecorate = false;
			final HashMap _map = SliceEclipsePlugin.getDefault().getIndusConfiguration().getLineNumbers();

			if (_map != null && _map.size() > 0) {
				_shouldDecorate = true;
			}

			if (_resource != null
				  && _resource.getType() == IResource.FILE
				  && _resource.getFileExtension().equalsIgnoreCase("java")
				  && _shouldDecorate) {
				if (_filelst.contains(_resource) && isFileOkToDecorate(_resource, _map)) {
					decoration.addOverlay(SliceEclipsePlugin.getDefault().getIndusConfiguration().getSliceDecorator());
				}
			}
		}
	}

	/**
	 * Determines if any of the classes in the Java file have a line number.
	 * @param resource The Java file
	 * @param map The map of classnames to line numbers
	 * @return boolean True if the file should be decorated.
	 */
	private boolean isFileOkToDecorate(final IResource resource,
			final HashMap map) {
		boolean _isFileOk = false;
		final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
				.create((IFile) resource);
		try {
			final IType[] _types = _icunit.getAllTypes();
			for (int _i = 0; _i < _types.length; _i++) {
				final String _classname = _types[_i].getFullyQualifiedName();
				if (map.containsKey(_classname)) {
					_isFileOk = true;
					break;
				}
			}
		} catch (JavaModelException _jme) {
			_isFileOk = false;
		}
		return _isFileOk;
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
	 * Returns the resource corressponding to the element.
	 *
	 * @param element The current element.
	 *
	 * @return IResource The resource coressponding to the element.
	 */
	private IResource getResource(final Object element) {
		IResource _resource = null;

		if (element instanceof IResource) {
			_resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			final IAdaptable _adaptable = (IAdaptable) element;
			_resource = (IResource) _adaptable.getAdapter(IResource.class);
		}

		return _resource;
	}

	/**
	 * Fires the label provider change event causing the decoration.
	 *
	 * @param event DOCUMENT ME!
	 */
	private void fireLabelEvent(final LabelProviderChangedEvent event) {
		Display.getDefault().syncExec(new Runnable() {
				public void run() {
					fireLabelProviderChanged(event);
				}
			});
	}
}
