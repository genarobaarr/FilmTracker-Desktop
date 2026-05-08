package com.src.filmtracker.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        this.properties = new Properties();
        cargarPropiedades();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        
        return instance;
    }

    private void cargarPropiedades() {
        try (InputStream input = ConfigManager.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception ex) {
            
        }
    }

    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        
        if (value != null) {
            if (!value.trim().isEmpty()) {
                return value.trim();
            }
        }
        
        return defaultValue;
    }
}