package edu.ksu.cis.indus.kaveri;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/*
 * Created on Jan 3, 2005
 *
 * 
package edu.ksu.cis.indus.kaveri;

/**
 * 
 * Responsible for maintaining the system resources used by Kaveri.
 * Call dispose() when the plugin is shutting down to dispose of the resources.
 * @author ganeshan
 */
 
public class ResourceManager {
	/**
	 * Map between SWT.RGB and SWT.Color values used by the dependence history view.
	 */
	private Map colorMap;
	
	public ResourceManager() {
		colorMap = new HashMap();
	}
	
	private Color addColor(RGB rgbColor) {	
		final Color _color = new Color(null, rgbColor);
		colorMap.put(rgbColor, _color);
		return _color;
	}
	
	public boolean isColorPresent(RGB rgbColor) {
		return colorMap.containsKey(rgbColor);
	}
	
	public Color getColor(RGB rgbColor) {
		Color _c = (Color) colorMap.get(rgbColor);
		if (_c == null) {
			_c = addColor(rgbColor);			
		} 
		return _c;		
	}
	
	public void dispose() {
		disposeColors();
	}
	
	private void disposeColors() {		
		for (Iterator iter = colorMap.values().iterator(); iter.hasNext();) {
			final Color _element = (Color) iter.next();
			_element.dispose();
		}
	}
}
