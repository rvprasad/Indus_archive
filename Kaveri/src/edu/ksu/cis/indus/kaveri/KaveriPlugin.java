
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

package edu.ksu.cis.indus.kaveri;

import edu.ksu.cis.indus.kaveri.common.SECommons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;


/**
 * The main plugin class.
 */
public class KaveriPlugin
  extends AbstractUIPlugin {
	/** 
	 * The plugin instance.
	 */
	private static KaveriPlugin plugin;

	/** 
	 * The indusconfiguration instance.
	 */
	private IndusConfiguration indusConfiguration;

	/** 
	 * Comment for <code>resourceBundle.</code>
	 */
	private ResourceBundle resourceBundle;

	/**
	 * Returns the shared instance.
	 *
	 * @return KaveriPlugin The plugin
	 */
	public static KaveriPlugin getDefault() {
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
		final ResourceBundle _bundle = KaveriPlugin.getDefault().getResourceBundle();
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

	/**
	 * Starts the plugin and initialized the default values.
	 *
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context)
	  throws Exception {
		super.start(context);
		plugin = this;
		indusConfiguration = new IndusConfiguration();

		try {
			resourceBundle = ResourceBundle.getBundle("Kaveri.KaveriPluginResources");
		} catch (MissingResourceException _x) {
			resourceBundle = null;
		}
		loadDefaultConfigurations();
	}

	/**
	 * Loads the defaults of the plugin.
	 */
	private void loadDefaultConfigurations() {
		// Load defaultConfiguration
		final StringBuffer _userConfiguration = new StringBuffer();
		URL _url =
			KaveriPlugin.getDefault().getBundle().getEntry("data/default_config/default_slicer_configuration.xml");
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

		_url = KaveriPlugin.getDefault().getBundle().getEntry("data/default_config/backward.xml");

		try {
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(_url.openStream()));

			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
		} catch (IOException _ioe) {
			SECommons.handleException(_ioe);
		}
		_store.setDefault("backwardConfiguration", _userConfiguration.toString());

		// Load forwardConfiguration
		_userConfiguration.delete(0, _userConfiguration.length());

		_url = KaveriPlugin.getDefault().getBundle().getEntry("data/default_config/forward.xml");

		try {
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(_url.openStream()));

			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
		} catch (IOException _ioe) {
			SECommons.handleException(_ioe);
		}
		_store.setDefault("forwardConfiguration", _userConfiguration.toString());
		setupDefaultColors();
	}

	/**
	 * Sets up default colors if none present.
	 */
	private void setupDefaultColors() {
		final IPreferenceStore _store = getPreferenceStore();
		final int _constMaxColor = 255;
		final int _const1 = 10;
		final int _const2 = 5;
		final int _const3 = 16;
		final int _const4 = 242;
		final int _const5 = 230;
		final RGB _redColor = new RGB(_constMaxColor, 0, 0);
		final RGB _blueColor = new RGB(0, 0, _constMaxColor);
		final RGB _greenColor = new RGB(_const1, _constMaxColor, _const1);
		final RGB _yellowColor = new RGB(_constMaxColor, _constMaxColor, _const2);
		//final RGB _purpleColor = new RGB(252, 58, 239);
		final RGB _paleBlue = new RGB(_const3, _const4, _const5);
		//final RGB lightRed = new RGB(214, 164, 180);

		PreferenceConverter.setDefault(_store, "controlColor", _redColor);
		PreferenceConverter.setDefault(_store, "dataColor", _greenColor);
		PreferenceConverter.setDefault(_store, "readyColor", _blueColor);
		PreferenceConverter.setDefault(_store, "syncColor", _yellowColor);
		PreferenceConverter.setDefault(_store, "inteferenceColor", _paleBlue);
	}
}
