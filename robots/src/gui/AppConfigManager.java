package gui;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;

public class AppConfigManager {
    private static final String CONFIG_FILE = Paths.get(
            System.getProperty("user.home"), "window_config.ser").toString();

    public static void saveConfig(Map<String, WindowState> config) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE));
        oos.writeObject(config);
        oos.close();
    }

    public static Map<String, WindowState> loadConfig() throws IOException, ClassNotFoundException {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return new HashMap<>();
        }

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CONFIG_FILE));
        Map<String, WindowState> config = (Map<String, WindowState>) ois.readObject();
        ois.close();
        return config;
    }
}
