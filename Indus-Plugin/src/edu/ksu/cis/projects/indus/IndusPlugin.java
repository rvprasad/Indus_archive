
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

package edu.ksu.cis.projects.indus;



import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class IndusPlugin
  extends AbstractUIPlugin {	

	/** 
	 * <p>The shared instance.</p>
	 */
	private static IndusPlugin plugin;
	

	/** 
	 * <p>Resource bundle. </p>
	 */
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public IndusPlugin() {
		super();
		plugin = this;

		try {
			resourceBundle = ResourceBundle.getBundle("edu.ksu.cis.projects.indus.Indus_PluginPluginResources");
		} catch (MissingResourceException _x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return IndusPlugin The plugin instance
	 */
	public static IndusPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 *
	 * @param key The key to the resource
	 * 
	 * @return String The resource name
	 */
	public static String getResourceString(final String key) {
		final ResourceBundle _bundle = IndusPlugin.getDefault().getResourceBundle();
		String _result = key;

		try {
			if (_bundle != null) {
				_result = _bundle.getString(key);
			}
		} catch (MissingResourceException _e) {
			_result = key;
		}
		return _result;
	}

	/**
	 * Returns the plugin's resource bundle.
	 *
	 * @return ResourceBundle The bundle.
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * This method is called upon plug-in activation.
	 *
	 * @param context The context of the plugin.
	 *
	 * @throws Exception The exception
	 */
	public void start(final BundleContext context)
	  throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped.
	 *
	 * @param context The context of the plugin
	 *
	 * @throws Exception The exception
	 */
	public void stop(final BundleContext context)
	  throws Exception {
		super.stop(context);
	}
}
