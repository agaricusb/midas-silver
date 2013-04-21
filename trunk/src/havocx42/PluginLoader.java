package havocx42;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class PluginLoader {
	private File				pluginDirectory;
	ArrayList<ConverterPlugin>	classes;

	public PluginLoader() throws FileNotFoundException {
		pluginDirectory = new File("plugins/");
		if (!pluginDirectory.exists()) {
			throw new FileNotFoundException("Unable to find the plugins directory");
		}
		if (!pluginDirectory.isDirectory()) {
			throw new FileNotFoundException("Unable to find the plugins directory");
		}
	}

	public void loadPlugins() {
		URLClassLoader loader = null;
		Enumeration<URL> props = null;
		InputStream stream = null;
		Logger logger = Logger.getLogger("PluginLoader");
		classes = new ArrayList<ConverterPlugin>();

		loader = (URLClassLoader) ClassLoader.getSystemClassLoader();

		try {
			MyClassLoader myLoader = new MyClassLoader(loader.getURLs());
			ExtensionFilter filter = new ExtensionFilter("jar");
			File[] files = pluginDirectory.listFiles(filter);
			for (File file : files) {
				logger.info("Loading plugin: " + file.getName());
				myLoader.addURL(file.toURI().toURL());
			}

			props = myLoader.getResources("midasPlugin.properties");
			while (props.hasMoreElements()) {
				Properties prop = new Properties();
				stream = props.nextElement().openStream();
				prop.load(stream);
				logger.info("loading class: " + prop.getProperty("class"));
				String classLocation;
				if ((classLocation = prop.getProperty("class")) != null) {
					try {
						ConverterPlugin plugin = (ConverterPlugin) myLoader.loadClass(classLocation).newInstance();
						logger.info("Loaded Plugin: " + plugin.getPluginName());
						classes.add(plugin);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public ArrayList<ConverterPlugin> getPluginsOfType(PluginType type) {
		ArrayList<ConverterPlugin> plugins = new ArrayList<ConverterPlugin>();
		for (ConverterPlugin plugin : classes) {
			if (plugin.getPluginType() == type) {
				plugins.add(plugin);
			}
		}

		return plugins;
	}

	public ArrayList<ConverterPlugin> getPlugins() {
		return classes;
	}

}
