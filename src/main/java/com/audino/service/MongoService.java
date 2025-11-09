package com.audino.service;

import com.audino.model.Patient;
import com.audino.model.Prescription;
import com.audino.util.ConfigurationManager;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoService {
    private static MongoService instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoService() {}

    public static MongoService getInstance() {
        if (instance == null) {
            instance = new MongoService();
        }
        return instance;
    }

    public void initialize() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        String connectionString = config.getProperty("mongodb.uri", "mongodb://localhost:27017");
        String dbName = config.getProperty("mongodb.database", "audino");

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry customRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(new Jsr310CodecProvider(), pojoCodecProvider)
        );

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .codecRegistry(customRegistry)
                .build();

        mongoClient = MongoClients.create(clientSettings);
        database = mongoClient.getDatabase(dbName);
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public List<Patient> getAllPatients() {
        MongoCollection<Patient> collection = database.getCollection("patients", Patient.class);
        return collection.find().into(new ArrayList<>());
    }

    public void addPatient(Patient patient) {
        if (patient.getPatientId() == null || patient.getPatientId().trim().isEmpty()) {
             patient.setPatientId("PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        MongoCollection<Patient> collection = database.getCollection("patients", Patient.class);
        collection.insertOne(patient);
    }

    public void updatePatient(Patient patient) {
        MongoCollection<Patient> collection = database.getCollection("patients", Patient.class);
        collection.replaceOne(Filters.eq("_id", patient.getId()), patient);
    }
    
    public void deletePatient(Patient patient) {
        MongoCollection<Patient> collection = database.getCollection("patients", Patient.class);
        collection.deleteOne(Filters.eq("_id", patient.getId()));
    }

    public void addPrescription(Prescription prescription) {
        MongoCollection<Prescription> collection = database.getCollection("prescriptions", Prescription.class);
        collection.insertOne(prescription);
    }
    
    public List<Prescription> getPrescriptionsForPatient(String patientId) {
        MongoCollection<Prescription> collection = database.getCollection("prescriptions", Prescription.class);
        return collection.find(Filters.eq("patientId", patientId))
                         .sort(Sorts.descending("createdAt"))
                         .into(new ArrayList<>());
    }

    public void deletePrescriptionsForPatient(String patientId) {
        MongoCollection<Prescription> collection = database.getCollection("prescriptions", Prescription.class);
        collection.deleteMany(Filters.eq("patientId", patientId));
    }
}