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

import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

/**
 * The main plugin class.
 */
public class KaveriPlugin extends AbstractUIPlugin {

    /**
     * The plugin instance.
     */
    private static KaveriPlugin plugin;

    /**
     * The slicer tool instance.
     */
    private SlicerTool slicerTool;

    /**
     * The resource change listener.
     */
    //private IResourceChangeListener listener;
    /**
     * This is the annotation cache map.
     */
    private Map cacheMap;

    /**
     * The indusconfiguration instance.
     */
    private IndusConfiguration indusConfiguration;

    /**
     * Comment for <code>resourceBundle.</code>
     */
    private ResourceBundle resourceBundle;

    /**
     * Constructor.
     */
    public KaveriPlugin() {
        super();
    }

    /**
     * Returns the shared instance.
     * 
     * @return KaveriPlugin The plugin
     */
    public static KaveriPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     * 
     * @param key
     *            The key to lookup
     * 
     * @return String The string correspoding to the key
     */
    public static String getResourceString(final String key) {
        final ResourceBundle _bundle = KaveriPlugin.getDefault()
                .getResourceBundle();
        String _result = key;

        try {
            if (_bundle != null) {
                _result = _bundle.getString(key);
            }
        } catch (MissingResourceException _e) {
            _result = key;
            KaveriErrorLog.logException("Missing Resource", _e);
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
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        indusConfiguration = new IndusConfiguration();
        try {
            resourceBundle = ResourceBundle
                    .getBundle("edu.ksu.cis.indus.kaveri");
        } catch (MissingResourceException _x) {
            resourceBundle = null;
            //	KaveriErrorLog.logInformation("Missing resource", _x);
        }
        slicerTool = new SlicerTool(TokenUtil
                .getTokenManager(new SootValueTypeManager()),
                new ExceptionFlowSensitiveStmtGraphFactory());
        cacheMap = new HashMap();
        /*
         * final IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         * listener = new JavaClassChangeListener();
         * _workspace.addResourceChangeListener(listener);
         */

    }

    /**
     * Loads the defaultConfiguration.xml into slicer tool.
     * 
     * @throws IllegalArgumentException
     *             When an valid configuration is used.
     */
    public void loadDefaultConfigurations() throws IllegalArgumentException {
        final IPreferenceStore _store = getPreferenceStore();
        final StringBuffer _userConfiguration = new StringBuffer();
        final URL _url = KaveriPlugin.getDefault().getBundle().getEntry(
                "data/default_config/default_slicer_configuration.xml");

        try {
            final BufferedReader _configReader = new BufferedReader(
                    new InputStreamReader(_url.openStream()));

            while (_configReader.ready()) {
                _userConfiguration.append(_configReader.readLine());
            }
            _configReader.close();
        } catch (IOException _ioe) {
            _ioe.printStackTrace();
            KaveriErrorLog.logException("Error reading default configuration",
                    _ioe);
        }

        final String _configuration = _userConfiguration.toString();
        final boolean _result = slicerTool
                .destringizeConfiguration(_configuration);
        if (!_result) {
            throw new IllegalArgumentException(
                    "Slicer Tool passed illegal configuration");
        }

    }

    /**
     * Loads the defaults of the plugin.
     * 
     * @throws IllegalArgumentException
     *             When an valid configuration is used.
     */
    public void loadConfigurations() throws IllegalArgumentException {
        final IPreferenceStore _store = getPreferenceStore();
        final String _config = _store.getString("defaultConfiguration");

        if (_config.equals("")) {
            loadDefaultConfigurations();
        } else {
            final boolean _result = slicerTool
                    .destringizeConfiguration(_config);
            if (!_result) {
                MessageDialog
                        .openError(
                                null,
                                "Configuration Reset",
                                "The stored configuration"
                                        + " is not compatible with Indus, resetting all the configurations");
                loadDefaultConfigurations();
                storeConfiguration();
            }

        }

    }

    /**
     * Resets the annotation cache map.
     *  
     */
    public void reset() {
        cacheMap.clear();
    }

    /**
     * Sets up default colors if none present.
     */
    public void setupDefaultColors() {
        final IPreferenceStore _store = getPreferenceStore();
        if (PreferenceConverter.getDefaultColor(_store, "controlColor") == PreferenceConverter.COLOR_DEFAULT_DEFAULT) {
            final int _constMaxColor = 255;
            final int _const1 = 10;
            final int _const2 = 5;
            final int _const3 = 16;
            final int _const4 = 242;
            final int _const5 = 230;
            final RGB _redColor = new RGB(_constMaxColor, 0, 0);
            final RGB _blueColor = new RGB(0, 0, _constMaxColor);
            final RGB _greenColor = new RGB(_const1, _constMaxColor, _const1);
            final RGB _yellowColor = new RGB(_constMaxColor, _constMaxColor,
                    _const2);
            //final RGB _purpleColor = new RGB(252, 58, 239);
            final RGB _paleBlue = new RGB(_const3, _const4, _const5);
            //final RGB lightRed = new RGB(214, 164, 180);

            PreferenceConverter.setDefault(_store, "controlColor", _redColor);
            PreferenceConverter.setDefault(_store, "dataColor", _greenColor);
            PreferenceConverter.setDefault(_store, "readyColor", _blueColor);
            PreferenceConverter.setDefault(_store, "syncColor", _yellowColor);
            PreferenceConverter.setDefault(_store, "interferenceColor",
                    _paleBlue);

        }
    }

    /**
     * Returns the slicer tool instance.
     * 
     * @return Returns the slicerTool.
     */
    public SlicerTool getSlicerTool() {
        return slicerTool;
    }

    /**
     * Stores the new configurations.
     */
    public void storeConfiguration() {
        final String _config = slicerTool.stringizeConfiguration();
        getPreferenceStore().setValue("defaultConfiguration", _config);
    }

    /**
     *  Create a new instance of the slicer.
     *  @param ignoreExceptionList The list of exceptions to ignore.
     */
    public void createNewSlicer(final Collection ignoreExceptionList) {
        slicerTool = new SlicerTool(TokenUtil
                .getTokenManager(new SootValueTypeManager()),
                new ExceptionFlowSensitiveStmtGraphFactory(ignoreExceptionList ,true));
        loadConfigurations();
    }

    /**
     * The plugin has stopped.
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(final BundleContext context) throws Exception {
        /*
         * if (listener != null) { final IWorkspace _workspace =
         * ResourcesPlugin.getWorkspace();
         * _workspace.removeResourceChangeListener(listener); }
         */
        getIndusConfiguration().getRManager().dispose();
        final IJobManager _manager = Platform.getJobManager();
        final String _myJobFamily = "edu.ksu.cis.indus.kaveri.soottagremover";
        _manager.cancel(_myJobFamily);

    }

    /**
     * @return Returns the cacheMap.
     */
    public Map getCacheMap() {
        return cacheMap;
    }

    /**
     * Adds the given object to the map.
     * 
     * @param key
     *            The key to the map
     * @param value
     *            The value to the map
     */
    public void addToCacheMap(final Object key, final Object value) {
        cacheMap.put(key, value);
    }
}