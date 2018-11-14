import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class PluginLoading {
     static ArrayList<Plugin> getPlugins() {
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
                e.printStackTrace();
            }
        }
        return plugins;
    }

    private static boolean isPluginImplemented(Class[] interfaces) {
        for (Class item : interfaces)
            if (item.getName().contains("Plugin"))
                return true;
        return false;
    }


}