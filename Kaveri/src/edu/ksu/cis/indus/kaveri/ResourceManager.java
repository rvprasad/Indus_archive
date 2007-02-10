/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
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
 * package edu.ksu.cis.indus.kaveri;
 * 
 * /**
 * 
 * Responsible for maintaining the system resources used by Kaveri. Call
 * dispose() when the plugin is shutting down to dispose of the resources.
 * @author ganeshan
 */

/**
 * The resource manager.
 * @author ganeshan
 *
 * Manages all the gui resources used in Kaveri.
 */
public class ResourceManager {
    /**
     * Map between SWT.RGB and SWT.Color values used by the dependence history
     * view.
     */
    private Map colorMap;

    /**
     * Constructor.
     *
     */
    public ResourceManager() {
        colorMap = new HashMap();
    }

    /**
     * Adds the given color to the map.
     * @param rgbColor The color to add.
     * @return Color The color corresponding to the rgbColor.
     */
    private Color addColor(final RGB rgbColor) {
        final Color _color = new Color(null, rgbColor);
        colorMap.put(rgbColor, _color);
        return _color;
    }

    /**
     * Indicates if the color is present in the map.
     * @param rgbColor The color to check
     * @return boolean The presence of the color.
     */
    public boolean isColorPresent(final RGB rgbColor) {
        return colorMap.containsKey(rgbColor);
    }

    /**
     * Get the color corresponding to the color, create if none present.
     * @param rgbColor The color of the Color.
     * @return Color The color for the given rgbColor.
     */
    public Color getColor(final RGB rgbColor) {
        Color _c = (Color) colorMap.get(rgbColor);
        if (_c == null) {
            _c = addColor(rgbColor);
        }
        return _c;
    }

    /**
     * Dispose the resources.
     *
     */
    public void dispose() {
        disposeColors();
    }

    /**
     * Dispose of the colors.
     *
     */
    private void disposeColors() {
        for (final Iterator _iter = colorMap.values().iterator(); _iter.hasNext();) {
            final Color _element = (Color) _iter.next();
            _element.dispose();
        }
    }
}