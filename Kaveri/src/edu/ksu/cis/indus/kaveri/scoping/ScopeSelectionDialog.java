/*
 * Created on Jan 6, 2005
 *
 *
 */
package edu.ksu.cis.indus.kaveri.scoping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;


import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * @author ganeshan
 * 
 *  
 */
public class ScopeSelectionDialog extends CheckedTreeSelectionDialog {		

	
	/**
	 * @param parent
	 * @param labelProvider
	 * @param contentProvider
	 */
	public ScopeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
		super(parent, new ScopeLabelProvider(), new ScopeTreeContentProvider());
		this.setTitle("Scope Selection");
		this.setMessage("Select the methods to be used for scoping");
		this.setContainerMode(true);
		this.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getScopeMap());		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {		
		super.computeResult();
		List _lst = new LinkedList();
		final Object _obj[] = getResult();
		for (int i = 0; i < _obj.length; i++) {
			if (!(_obj[i] instanceof TreeParent)) {
				_lst.add(((TreeObject)_obj[i]).getElem());
			}
		}
		if (_lst.size() > 0) {
			setResult(_lst);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		KaveriPlugin.getDefault().getIndusConfiguration().setScopeSpecification("");
		super.cancelPressed();
	}
}


class ScopeLabelProvider  extends JavaElementLabelProvider {
	
	/** (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {		
		return super.getImage(((TreeObject) element).getElem());
	}
	public String getText(Object obj) {
		return super.getText(((TreeObject) obj).getElem());		
	}
}

class ScopeTreeContentProvider  implements ITreeContentProvider {
	private TreeParent invisibleRoot;
	
	public Object[] getChildren(Object parent) {
		if (parent instanceof TreeParent) {
			return ((TreeParent)parent).getChildren();
		}
		return new Object[0];
	}

	
	public Object getParent(Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject)child).getParent();
		}
		return null;
	}

	
	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent)
			return ((TreeParent)parent).hasChildren();
		return false;
	}

	
	public Object[] getElements(Object inputElement) {
		final Map _map = KaveriPlugin.getDefault().getIndusConfiguration().getScopeMap();
		if (inputElement.equals(_map)) {
			invisibleRoot = new TreeParent();			
			for (Iterator iter = _map.keySet().iterator(); iter.hasNext();) {
				final IJavaElement _elem = (IJavaElement) iter.next();
				final TreeParent _tp = new TreeParent(_elem);
				final Set _set = (Set) _map.get(_elem);
				for (Iterator iterator = _set.iterator(); iterator.hasNext();) {
					final IMethod _method = (IMethod) iterator.next();
					final TreeObject _to = new TreeObject(_method);
					_tp.addChild(_to);
				}
				invisibleRoot.addChild(_tp);
			}
			return invisibleRoot.getChildren();
		}
		return null;
	}

	
	public void dispose() {			
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
	}
	
}

class TreeObject implements IAdaptable {
	private IJavaElement elem;
	private TreeParent parent;
	private boolean isNotSpecial;
	public TreeObject(IJavaElement method) {
		isNotSpecial = false;
		this.elem = method;
	}
	
	public TreeObject() {
		isNotSpecial = true;
	}
	
	public String getName() {
		return elem.getElementName();
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
	 * @return Returns the elem.
	 */
	public IJavaElement getElem() {
		return elem;
	}
	/**
	 * @return Returns the isNotSpecial.
	 */
	public boolean isNotSpecial() {
		return isNotSpecial;
	}
}


class TreeParent extends TreeObject {
	private ArrayList children;
	private IJavaElement elem;
	
	public TreeParent(IJavaElement elem) {
		super(elem);
		children = new ArrayList();
	}
	
	public TreeParent() {
		super();
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
}
