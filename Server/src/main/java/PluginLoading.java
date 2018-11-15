import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class PluginLoading {
    private static Logger serverLogger;

    static ArrayList<Plugin> getPlugins() {
        System.setProperty("log4j.configurationFile", "Server/src/main/resources/log4j.xml");
        serverLogger = LogManager.getLogger("Server.Server");

        ArrayList<Plugin> plugins = new ArrayList<>();
        File pluginDir = new File("Plugins");
        File[] jars = pluginDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        for (File jar : jars) {
            try {
                URL jarURL = jar.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL});
                JarFile jarFile = new JarFile(jar);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    String file = entries.nextElement().getName();
                    if (!file.endsWith(".class")) continue;
                    file = file.replaceAll(".class", "");
                    Class pluginClass = classLoader.loadClass(file);

                    if (isPluginImplemented(pluginClass.getInterfaces())) {
                        Class c = classLoader.loadClass(pluginClass.getName());
                        Object inst = c.newInstance();
                        plugins.add((Plugin) inst);
                    }
                }
            } catch (Exception e) {
                serverLogger.error("Exception during plugins loading " + e);
            }
        }
        return plugins;
    }

    private static boolean isPluginImplemented(Class[] interfaces) {
        for (Class cinterface : interfaces)
            if (cinterface.getName().contains("Plugin"))
                return true;
        return false;
    }


}