package com.audino.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConfigurationManager {

    private static final Object lock = new Object();
    private static ConfigurationManager instance;
    private final Properties applicationProperties;
    private ObjectMapper objectMapper;
    private HikariDataSource dataSource;

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
        initializeDataSource();
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
        applicationProperties.setProperty("db.url", "jdbc:postgresql://localhost:5432/postgres");
        applicationProperties.setProperty("db.username", "postgres");
        applicationProperties.setProperty("db.password", "postgres");
        applicationProperties.setProperty("db.pool.size", "10");
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        
        try {
            new java.net.Socket("localhost", 5432).close();
        } catch (Exception e) {
            System.out.println("Postgres not running on 5432. Starting Embedded Postgres...");
            try {
                io.zonky.test.db.postgres.embedded.EmbeddedPostgres.builder().setPort(5432).start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        config.setJdbcUrl(getProperty("db.url", "jdbc:postgresql://localhost:5432/postgres"));
        config.setUsername(getProperty("db.username", "postgres"));
        config.setPassword(getProperty("db.password", "postgres"));
        config.setMaximumPoolSize(Integer.parseInt(getProperty("db.pool.size", "10")));
        
        // Optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    private void initializeObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String getProperty(String key, String defaultValue) {
        return applicationProperties.getProperty(key, defaultValue);
    }

    public String getPatientsDataFile() {
        return getProperty("data.patients.file", "/data/patients.json");
    }

    public String getMedicationsDataFile() {
        return getProperty("data.medications.file", "/data/medications.json");
    }

    public String getInteractionRulesDataFile() {
        return getProperty("data.interactions.file", "/data/interaction-rules.json");
    }

    public String getPrescriptionsDataFile() {
        return getProperty("data.prescriptions.file", "/data/prescriptions.json");
    }
    
    public String getDbUrl() {
        return getProperty("db.url", "jdbc:postgresql://localhost:5432/audino");
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}