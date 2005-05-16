package peqeclipse;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class PeqEclipsePlugin extends Plugin {
	//The shared instance.
	private static PeqEclipsePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public PeqEclipsePlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("peqeclipse.PeqEclipsePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static PeqEclipsePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PeqEclipsePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
