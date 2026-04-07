package com.audino.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {

    private static final Object lock = new Object();
    private static ConfigurationManager instance;
    private final Properties applicationProperties;
    private ObjectMapper objectMapper;

    private ConfigurationManager() {
        applicationProperties = new Properties();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    public void initialize() {
        loadApplicationProperties();
        initializeObjectMapper();
        System.out.println("Configuration Manager initialized.");
    }

    private void loadApplicationProperties() {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input != null) {
                applicationProperties.load(input);
            } else {
                setDefaultProperties();
            }
        } catch (IOException ex) {
            System.err.println("Could not load application.properties, using defaults.");
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        applicationProperties.setProperty("sqlite.database.path", "data/audino.db");
    }

    private void initializeObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String getProperty(String key, String defaultValue) {
        return applicationProperties.getProperty(key, defaultValue);
    }
    
    public String getSqliteDatabasePath() {
        String override = System.getProperty("audino.sqlite.path");
        if (override != null && !override.trim().isEmpty()) {
            return override.trim();
        }
        return getProperty("sqlite.database.path", "data/audino.db");
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}