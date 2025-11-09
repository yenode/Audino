package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.Patient;
import com.audino.model.Prescription;
import com.audino.util.ConfigurationManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataService {

    private final ObjectMapper objectMapper;
    private final ConfigurationManager config;
    private List<Patient> patients = new ArrayList<>();
    private List<Medication> medications = new ArrayList<>();
    private Map<String, Object> interactionRules;
    private final MongoService mongoService;

    public DataService() {
        this.config = ConfigurationManager.getInstance();
        this.objectMapper = config.getObjectMapper();
        this.mongoService = MongoService.getInstance();
    }

    public void loadAllData() {
        patients = mongoService.getAllPatients();
        medications = loadData(config.getMedicationsDataFile(), new TypeReference<>() {});
        interactionRules = loadData(config.getInteractionRulesDataFile(), new TypeReference<>() {});
        System.out.println("All data loaded.");
    }

    private <T> T loadData(String filePath, TypeReference<T> typeRef) {
        try (InputStream inputStream = DataService.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot find resource file: " + filePath);
            }
            return objectMapper.readValue(inputStream, typeRef);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load data from " + filePath, e);
        }
    }
    
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients);
    }

    public List<Medication> getAllMedications() {
        return new ArrayList<>(medications);
    }

    public Map<String, Object> getInteractionRules() {
        return interactionRules;
    }
    
    public List<Patient> searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllPatients();
        }
        String lowerCaseTerm = searchTerm.toLowerCase();
        return patients.stream()
                .filter(p -> p.getFullName().toLowerCase().contains(lowerCaseTerm))
                .collect(Collectors.toList());
    }

    public List<Medication> searchMedications(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllMedications();
        }
        String lowerCaseTerm = searchTerm.toLowerCase();
        return medications.stream()
                .filter(m -> m.getGenericName().toLowerCase().contains(lowerCaseTerm) ||
                             (m.getBrandName() != null && m.getBrandName().toLowerCase().contains(lowerCaseTerm)))
                .collect(Collectors.toList());
    }

    public void savePatient(Patient patient) {
        mongoService.addPatient(patient);
        patients.add(patient);
    }

    public void updatePatient(Patient patient) {
        mongoService.updatePatient(patient);
    }

    public void deletePatient(Patient patient) {
        mongoService.deletePrescriptionsForPatient(patient.getPatientId());
        mongoService.deletePatient(patient);
        patients.remove(patient);
    }

    public void savePrescription(Prescription prescription) {
        mongoService.addPrescription(prescription);
    }

    public List<Prescription> getPrescriptionsForPatient(Patient patient) {
        return mongoService.getPrescriptionsForPatient(patient.getPatientId());
    }
}