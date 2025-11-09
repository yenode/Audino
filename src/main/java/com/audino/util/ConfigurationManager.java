package com.audino.util;

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
        applicationProperties.setProperty("data.medications.file", "/data/medications.json");
        applicationProperties.setProperty("data.interactions.file", "/data/interaction-rules.json");
        applicationProperties.setProperty("mongodb.uri", "mongodb://localhost:27017");
        applicationProperties.setProperty("mongodb.database", "audino");
    }

    private void initializeObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public String getProperty(String key, String defaultValue) {
        return applicationProperties.getProperty(key, defaultValue);
    }
    
    public String getMedicationsDataFile() {
        return getProperty("data.medications.file", "/data/medications.json");
    }

    public String getInteractionRulesDataFile() {
        return getProperty("data.interactions.file", "/data/interaction-rules.json");
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}