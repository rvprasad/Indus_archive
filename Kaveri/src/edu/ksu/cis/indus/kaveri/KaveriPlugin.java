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


import edu.ksu.cis.indus.kaveri.driver.KaveriRootMethodTrapper;
import edu.ksu.cis.indus.kaveri.preferencedata.ExceptionListStore;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

import com.thoughtworks.xstream.XStream;


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
     * The root method trapper instance.
     */
    private KaveriRootMethodTrapper rmTrapper;
    
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
        final Collection _exceptionCollection = fetchExceptionNames();
        ExceptionFlowSensitiveStmtGraphFactory _factory = null;
        if (_exceptionCollection.isEmpty()) {
            _factory = new ExceptionFlowSensitiveStmtGraphFactory();
        } else {
            _factory = new ExceptionFlowSensitiveStmtGraphFactory(_exceptionCollection, true);
        }
            slicerTool = new SlicerTool(TokenUtil
                .getTokenManager(new SootValueTypeManager()),
                _factory);
        cacheMap = new HashMap();
        rmTrapper = new KaveriRootMethodTrapper();
        /*
         * final IWorkspace _workspace = ResourcesPlugin.getWorkspace();
         * listener = new JavaClassChangeListener();
         * _workspace.addResourceChangeListener(listener);
         */

    }

    /**
     * Fetch the collection of exception names.
     * @return Collection The collection of fqn exception names.
     */
    private Collection fetchExceptionNames() {
        final String _exceptionKey = "edu.ksu.cis.indus.kaveri.exceptionignorelist";
        Collection _coll = Collections.EMPTY_LIST;
        final IPreferenceStore _store = getPreferenceStore();
        final String _val = _store.getString(_exceptionKey);
        final XStream _xstream = new XStream();
        _xstream.alias("ExceptionListStore", ExceptionListStore.class);
        if (!_val.equals("")) {
            final ExceptionListStore _els = (ExceptionListStore) _xstream.fromXML(_val);
            _coll = _els.getExceptionCollection();
        }
        
        return _coll;
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
        loadConfigurations();
    }

    /**
     *  Create a new instance of the slicer.
     *  @param ignoreExceptionList The list of exceptions to ignore.
     */
    public void createNewSlicer(final Collection ignoreExceptionList) {
        ExceptionFlowSensitiveStmtGraphFactory _factory = null;
        if (ignoreExceptionList.isEmpty()) {
            _factory = new ExceptionFlowSensitiveStmtGraphFactory();
        } else {
            _factory = new ExceptionFlowSensitiveStmtGraphFactory(ignoreExceptionList,true);
        }
        
        slicerTool = new SlicerTool(TokenUtil            
                .getTokenManager(new SootValueTypeManager()),
                _factory);
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
    
    /**
     * 
     * Get the instance of the rootmethod trapper.
     * @return RootMethodTrapper The root method trapper instance.
     */
    public KaveriRootMethodTrapper getRmTrapper() {
        return rmTrapper;
    }
}