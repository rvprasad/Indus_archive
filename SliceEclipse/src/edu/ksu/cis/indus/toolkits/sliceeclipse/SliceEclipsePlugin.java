
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

package edu.ksu.cis.indus.toolkits.sliceeclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class SliceEclipsePlugin
  extends AbstractUIPlugin {
	/**
	 * The plugin instance.
	 */
	private static SliceEclipsePlugin plugin;

	/**
	 * The indusconfiguration instance.
	 */
	private  IndusConfiguration indusConfiguration;

	/**
	 * Comment for <code>resourceBundle.</code>
	 */
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 *
	 */
	public SliceEclipsePlugin() {
		super();
		plugin = this;
		indusConfiguration =  new IndusConfiguration();
		try {
			resourceBundle = ResourceBundle.getBundle("SliceEclipse.SliceEclipsePluginResources");
		} catch (MissingResourceException _x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return SliceEclipsePlugin The plugin
	 */
	public static SliceEclipsePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 *
	 * @param key The key to lookup
	 *
	 * @return String The string correspoding to the key
	 */
	public static String getResourceString(final String key) {
		final ResourceBundle _bundle = SliceEclipsePlugin.getDefault().getResourceBundle();
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
	 * Returns the Indus Configuration Instance.
	 *
	 * @return Returns the indusConfiguration.
	 */
	public IndusConfiguration getIndusConfiguration() {
		return indusConfiguration;
	}

	/**
	 * Returns the plugin's resource bundle.
	 *
	 * @return ResourceBundle The Resource bundle
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	
	/** Starts the plugin and initialized the default values.
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		loadDefaultConfigurations();		
	}

	/**
	 * Loads the defaults of the plugin.
	 */
	private void loadDefaultConfigurations() {
		// Load defaultConfiguration
		final StringBuffer _userConfiguration = new StringBuffer();
		URL _url =
			SliceEclipsePlugin.getDefault().getBundle().getEntry("data/default_config/default_slicer_configuration.xml");
		final IPreferenceStore _store = getPreferenceStore();
		
		try {
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(_url.openStream()));
			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
		} catch (IOException _ioe) {	
			_ioe.printStackTrace();
		}
		 
		_store.setDefault("defaultConfiguration", _userConfiguration.toString());
		
		
		// Load backwardConfiguration
		_userConfiguration.delete(0, _userConfiguration.length());		
		
		_url =
			SliceEclipsePlugin.getDefault().getBundle().getEntry("data/default_config/backward.xml");
		try {
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(_url.openStream()));
			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
		} catch (IOException _ioe) {	
			_ioe.printStackTrace();
		}
		_store.setDefault("backwardConfiguration", _userConfiguration.toString());
		
		// Load forwardConfiguration
		_userConfiguration.delete(0, _userConfiguration.length());		
		
		_url =
			SliceEclipsePlugin.getDefault().getBundle().getEntry("data/default_config/forward.xml");
		try {
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(_url.openStream()));
			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
		} catch (IOException _ioe) {	
			_ioe.printStackTrace();
		}
		_store.setDefault("forwardConfiguration", _userConfiguration.toString());
		
	}
}
