package com.liafinder.config;

import java.io.IOException;

public class ConfigManager {
    private static ConfigManager instance;
    private AppConfig config;

    private ConfigManager() {
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void loadConfig(String path) throws IOException {
        this.config = ConfigLoader.loadConfig(path);
    }

    public AppConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("Config not loaded. Call loadConfig() first.");
        }
        return config;
    }
}
