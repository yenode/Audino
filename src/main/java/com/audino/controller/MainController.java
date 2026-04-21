package com.audino.controller;

import com.audino.model.*;
import com.audino.service.DataService;
import com.audino.service.InteractionEngine;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    @FXML private Label currentDateLabel;

    @FXML private TextField patientSearchField;
    @FXML private Button searchBtn;
    @FXML private ListView<Patient> patientListView;

    @FXML private Label selectedPatientLabel;
    @FXML private Label patientIdLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientGenderLabel;
    @FXML private Label patientAllergiesLabel;
    @FXML private Label patientConditionsLabel;
    @FXML private Label medicationCountLabel;
    @FXML private TableView<PrescribedDrug> prescriptionTableView;
    @FXML private TableColumn<PrescribedDrug, String> medicationColumn;
    @FXML private TableColumn<PrescribedDrug, String> dosageColumn;
    @FXML private TableColumn<PrescribedDrug, String> frequencyColumn;
    @FXML private TableColumn<PrescribedDrug, String> durationColumn;
    @FXML private TableColumn<PrescribedDrug, Void> actionColumn;
    @FXML private TextField medicationSearchField;
    @FXML private ComboBox<Medication> medicationComboBox;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private TextField durationField;
    @FXML private Button addMedicationBtn;
    @FXML private Button newPrescriptionBtn;
    @FXML private Button saveBtn;
    @FXML private Button addPatientBtn;
    @FXML private Button editPatientBtn;
    @FXML private Button deletePatientBtn;
    @FXML private Button refreshBtn;

    @FXML private Label criticalAlertsLabel;
    @FXML private Label warningAlertsLabel;
    @FXML private Label infoAlertsLabel;
    @FXML private ListView<InteractionAlert> alertsListView;
    @FXML private TextArea alertDetailsTextArea;
    @FXML private Button acknowledgeAlertBtn;

    @FXML private Label statusLabel;
    @FXML private Label prescriptionStatusLabel;
    @FXML private Label interactionStatusLabel;

    private DataService dataService;
    private InteractionEngine interactionEngine;
    private Patient selectedPatient;

    private Prescription currentPrescription;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private final ObservableList<Medication> medicationList = FXCollections.observableArrayList();
    private final ObservableList<Prescription> prescriptionList = FXCollections.observableArrayList();
    private final ObservableList<InteractionAlert> alertList = FXCollections.observableArrayList();
    private final ObservableList<PrescribedDrug> prescribedDrugList = FXCollections.observableArrayList();
    private final ContextMenu medicationSuggestionMenu = new ContextMenu();
    
    private boolean dataLoadedSuccessfully = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataService = new DataService();
        interactionEngine = new InteractionEngine();

        setupDate();
        loadData();
        setupPatientListView();
        setupMedicationComboBox();
        setupPrescriptionTable();
        setupAlertsListView();
        setupEventListeners();
        updateUIState();
    }
    
    private void setupDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDateLabel.setText(LocalDate.now().format(formatter));
    }

    private void loadData() {
        statusLabel.setText("Loading data from database...");
        try {
            dataService.loadAllData();
            patientList.setAll(dataService.getAllPatients());
            medicationList.setAll(dataService.getAllMedications());
            prescriptionList.setAll(dataService.getAllPrescriptions());
            dataLoadedSuccessfully = true;
            statusLabel.setText("Data loaded successfully.");
        } catch (Exception e) {
            dataLoadedSuccessfully = false;
            statusLabel.setText("Error loading data.");
            showErrorAlert("Data Loading Error", "Could not load application data from the database.", e.getMessage());
        }
    }

    @FXML
    private void handleAddPatient() {
        Patient newPatient = new Patient();
        boolean saved = showPatientDialog(newPatient, "Add New Patient");
        if (saved) {
            newPatient.setPatientId("PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            dataService.savePatient(newPatient);
            patientList.add(newPatient);
            patientListView.getSelectionModel().select(newPatient);
        }
    }

    @FXML
    private void handleEditPatient() {
        if (selectedPatient == null) {
            showWarningAlert("No Patient Selected", "Please select a patient to edit.");
            return;
        }
        boolean saved = showPatientDialog(selectedPatient, "Edit Patient");
        if (saved) {
            dataService.updatePatient(selectedPatient);
            patientListView.refresh();
            updatePatientInfoPanel();
        }
    }

    @FXML
    private void handleDeletePatient() {
        if (selectedPatient == null) {
            showWarningAlert("No Patient Selected", "Please select a patient to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Patient: " + selectedPatient.getFullName());
        confirmAlert.setContentText("Are you sure you want to permanently delete this patient and all associated records? This action cannot be undone.");

        final Patient patientToDelete = selectedPatient;

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String patientName = patientToDelete.getFullName();

                    dataService.deletePatient(patientToDelete);
                    patientList.remove(patientToDelete);
                    patientListView.getSelectionModel().clearSelection();
                    
                    statusLabel.setText("Patient " + patientName + " deleted.");
                } catch (Exception e) {
                    showErrorAlert("Deletion Error", "Could not delete the patient from the database.", e.getMessage());
                }
            }
        });
    }

    private boolean showPatientDialog(Patient patient, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PatientDialog.fxml"));
            VBox page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(patientListView.getScene().getWindow());
            Scene scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            dialogStage.setScene(scene);

            PatientDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPatient(patient);

            dialogStage.showAndWait();

            return controller.isSaved();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Dialog Error", "Could not load the patient dialog.", e.getMessage());
            return false;
        }
    }

    private void setupPatientListView() {
        patientListView.setItems(patientList);
        patientListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? null : patient.getFullName());
            }
        });
    }

    private void setupMedicationComboBox() {
        medicationComboBox.setItems(medicationList);
        Callback<ListView<Medication>, ListCell<Medication>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medication med, boolean empty) {
                super.updateItem(med, empty);
                setText(empty ? null : formatMedicationLabel(med));
            }
        };
        medicationComboBox.setCellFactory(cellFactory);
        medicationComboBox.setButtonCell(cellFactory.call(null));
    }

    private void setupPrescriptionTable() {
        medicationColumn.setCellValueFactory(cellData -> {
            PrescribedDrug drug = cellData.getValue();
            if (drug.getMedication() != null) {
                return new SimpleStringProperty(formatMedicationLabel(drug.getMedication()));
            }
            return new SimpleStringProperty("Unknown Medication");
        });
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        prescriptionTableView.setItems(prescribedDrugList);
        setupActionColumn();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.getStyleClass().add("danger-btn");
                removeBtn.setOnAction(event -> {
                    PrescribedDrug drug = getTableView().getItems().get(getIndex());
                    handleRemoveMedication(drug);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                    // Always enable remove button
                    removeBtn.setDisable(false);
                }
            }
        });
    }

    private void setupAlertsListView() {
        alertsListView.setItems(alertList);
        alertsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(InteractionAlert alert, boolean empty) {
                super.updateItem(alert, empty);
                if (empty || alert == null) {
                    setText(null);
                    getStyleClass().removeAll("alert-critical", "alert-warning", "alert-info");
                    setOpacity(1.0);
                } else {
                    setText(String.format("[%s] %s", alert.getAlertLevel(), alert.getAlertType()));
                    getStyleClass().removeAll("alert-critical", "alert-warning", "alert-info");
                    switch (alert.getAlertLevel()) {
                        case CRITICAL -> getStyleClass().add("alert-critical");
                        case WARNING -> getStyleClass().add("alert-warning");
                        case INFO -> getStyleClass().add("alert-info");
                    }
                    setOpacity(alert.isAcknowledged() ? 0.6 : 1.0);
                }
            }
        });
    }

    private void setupEventListeners() {
        patientListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handlePatientSelection(newVal));

        alertsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleAlertSelection(newVal));

        patientSearchField.textProperty().addListener(
            (obs, oldVal, newVal) -> handlePatientSearch());

        medicationSearchField.textProperty().addListener(
            (obs, oldVal, newVal) -> handleMedicationSearchInput(newVal));

        medicationSearchField.setOnAction(event -> applyMedicationAutoCorrection());
        medicationSearchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                medicationSuggestionMenu.hide();
            }
        });
    }

    private void handleMedicationSearchInput(String query) {
        if (query == null || query.isBlank()) {
            medicationComboBox.setItems(medicationList);
            medicationSuggestionMenu.hide();
            return;
        }

        List<Medication> suggestions = dataService.suggestMedications(query, 8);
        medicationComboBox.setItems(FXCollections.observableArrayList(suggestions));
        showMedicationSuggestions(query, suggestions);
    }

    private void showMedicationSuggestions(String query, List<Medication> suggestions) {
        medicationSuggestionMenu.getItems().clear();

        if (query == null || query.isBlank() || suggestions == null || suggestions.isEmpty()) {
            medicationSuggestionMenu.hide();
            return;
        }

        int menuLimit = Math.min(6, suggestions.size());
        for (int i = 0; i < menuLimit; i++) {
            Medication medication = suggestions.get(i);
            String genericName = medication.getGenericName() == null ? "" : medication.getGenericName().trim();

            MenuItem item = new MenuItem(formatMedicationLabel(medication));
            item.setOnAction(event -> {
                medicationSearchField.setText(genericName);
                medicationComboBox.getSelectionModel().select(medication);
                medicationSuggestionMenu.hide();
            });
            medicationSuggestionMenu.getItems().add(item);
        }

        if (!medicationSuggestionMenu.isShowing()) {
            medicationSuggestionMenu.show(medicationSearchField, Side.BOTTOM, 0, 0);
        }
    }

    private void applyMedicationAutoCorrection() {
        String current = medicationSearchField.getText();
        if (current == null || current.isBlank()) {
            medicationSuggestionMenu.hide();
            return;
        }

        String corrected = dataService.autoCorrectMedicationName(current);
        if (!corrected.equalsIgnoreCase(current.trim())) {
            medicationSearchField.setText(corrected);
            statusLabel.setText("Auto-corrected medication to: " + corrected);
        }

        List<Medication> results = dataService.suggestMedications(medicationSearchField.getText(), 8);
        if (!results.isEmpty()) {
            medicationComboBox.setItems(FXCollections.observableArrayList(results));
            medicationComboBox.getSelectionModel().select(results.get(0));
        }
        medicationSuggestionMenu.hide();
    }

    private String formatMedicationLabel(Medication medication) {
        if (medication == null) {
            return "Unknown Medication";
        }

        String generic = medication.getGenericName() == null ? "" : medication.getGenericName().trim();
        String brand = medication.getBrandName() == null ? "" : medication.getBrandName().trim();
        String rxNorm = medication.getRxNormCode() == null ? "" : medication.getRxNormCode().trim();

        String label = generic;
        if (!brand.isBlank() && !brand.equalsIgnoreCase(generic)) {
            label = brand + " (" + generic + ")";
        }

        label += " [RxNorm: " + (rxNorm.isBlank() ? "N/A" : rxNorm) + "]";

        return label;
    }

    @FXML
    private void handlePatientSelection(Patient patient) {
        selectedPatient = patient;
        updatePatientInfoPanel();
        
        if (patient != null) {
            // Automatically load existing prescription for this patient
            currentPrescription = dataService.getActivePrescriberionForPatient(patient.getPatientId());
            if (currentPrescription != null) {
                prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
                statusLabel.setText("Loaded prescription for " + patient.getFullName() + " (" + currentPrescription.getPrescribedDrugs().size() + " medications)");
                prescriptionStatusLabel.setText("Prescription Status: " + currentPrescription.getStatus());
                checkInteractions(); // Check interactions for existing medications
            } else {
                // No existing prescription - clear the view
                prescribedDrugList.clear();
                alertList.clear();
                statusLabel.setText("Selected " + patient.getFullName() + " - No existing prescription");
                prescriptionStatusLabel.setText("No active prescription");
            }
        } else {
            clearPrescription();
            statusLabel.setText("No patient selected");
        }
        updateUIState();
    }

    private void handleAlertSelection(InteractionAlert alert) {
        if (alert != null) {
            alertDetailsTextArea.setText(alert.getFormattedMessage());
            acknowledgeAlertBtn.setDisable(alert.isAcknowledged());
        } else {
            alertDetailsTextArea.clear();
            acknowledgeAlertBtn.setDisable(true);
        }
    }

    @FXML
    private void handleNewPrescription() {
        if (selectedPatient == null) {
            showWarningAlert("No Patient Selected", "Please select a patient before starting a new prescription.");
            return;
        }
        
        // Simply create new prescription (will automatically replace existing one when saved)
        currentPrescription = new Prescription(selectedPatient, "Dr. User");
        clearPrescriptionForm();
        prescribedDrugList.clear();
        alertList.clear();
        updateAlertsSummary();
        updateUIState();
        statusLabel.setText("New prescription started for " + selectedPatient.getFullName());
    }

    @FXML
    private void handleLoadExistingPrescription() {
        if (selectedPatient == null) {
            showWarningAlert("No Patient Selected", "Please select a patient to load prescription for.");
            return;
        }
        
        Prescription existingPrescription = dataService.getActivePrescriberionForPatient(selectedPatient.getPatientId());
        if (existingPrescription == null) {
            showWarningAlert("No Existing Prescription", "Patient " + selectedPatient.getFullName() + " has no active prescription to load.");
            return;
        }
        
        currentPrescription = existingPrescription;
        prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
        clearPrescriptionForm();
        alertList.clear();
        updateAlertsSummary();
        updateUIState();
        statusLabel.setText("Loaded existing prescription for " + selectedPatient.getFullName());
        prescriptionStatusLabel.setText("Prescription Status: " + currentPrescription.getStatus());
    }

    @FXML
    private void handleRefresh() {
        // Show confirmation if there are unsaved changes
        if (currentPrescription != null && currentPrescription.getStatus() == PrescriptionStatus.DRAFT && !currentPrescription.isEmpty()) {
            boolean confirmed = showConfirmationAlert("Unsaved Changes", 
                "You have unsaved changes to the current prescription. Refreshing will discard these changes. Are you sure?");
            if (!confirmed) {
                return;
            }
        }

        final String selectedPatientId = (selectedPatient != null) ? selectedPatient.getPatientId() : null;

        loadData();

        if (selectedPatientId != null) {
            patientList.stream()
                .filter(p -> selectedPatientId.equals(p.getPatientId()))
                .findFirst()
                .ifPresent(p -> {
                    patientListView.getSelectionModel().select(p);
                    patientListView.scrollTo(p);
                    // This will trigger handlePatientSelection and reload prescription from database
                });
        }
        
        statusLabel.setText("Data refreshed from database. Any unsaved changes have been discarded.");
    }

    @FXML
    private void handlePatientSearch() {
        String searchTerm = patientSearchField.getText();
        patientList.setAll(dataService.searchPatients(searchTerm));
    }

    @FXML
    private void handleAddMedication() {
        if (selectedPatient == null) {
            showWarningAlert("No Patient Selected", "Please select a patient before adding medications.");
            return;
        }

        // Automatically get or create prescription for this patient (no dialogs)
        if (currentPrescription == null) {
            // Try to get existing prescription
            currentPrescription = dataService.getActivePrescriberionForPatient(selectedPatient.getPatientId());
            if (currentPrescription != null) {
                // Load existing prescription
                prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
                statusLabel.setText("Loaded existing prescription for " + selectedPatient.getFullName());
                prescriptionStatusLabel.setText("Prescription Status: " + currentPrescription.getStatus());
            } else {
                // Create new prescription automatically but don't save yet
                currentPrescription = new Prescription(selectedPatient, "Dr. User");
                currentPrescription.setStatus(PrescriptionStatus.DRAFT); // Start as draft
                statusLabel.setText("Created new prescription for " + selectedPatient.getFullName() + " (not saved)");
                prescriptionStatusLabel.setText("Prescription Status: DRAFT (new prescription)");
            }
            updateUIState();
        }

        // Validate medication input
        Medication selectedMed = medicationComboBox.getValue();
        if (selectedMed == null && medicationSearchField.getText() != null && !medicationSearchField.getText().isBlank()) {
            String corrected = dataService.autoCorrectMedicationName(medicationSearchField.getText());
            medicationSearchField.setText(corrected);
            List<Medication> suggestions = dataService.suggestMedications(corrected, 1);
            if (!suggestions.isEmpty()) {
                selectedMed = suggestions.get(0);
                medicationComboBox.getSelectionModel().select(selectedMed);
            }
        }

        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        String duration = durationField.getText().trim();

        if (selectedMed == null || dosage.isEmpty() || frequency.isEmpty() || duration.isEmpty()) {
            showWarningAlert("Missing Information", "Please select a medication and fill in all dosage fields.");
            return;
        }

        if (!selectedMed.isValidDosage(dosage)) {
            showWarningAlert("Invalid Dosage", "The dosage format is not valid for this type of medication. Example: '1' for tablets, '5ml' for liquids.");
            return;
        }

        // Add medication directly to prescription (but don't save yet)
        PrescribedDrug newDrug = new PrescribedDrug(selectedMed, dosage, frequency, duration, "", "Dr. User");
        currentPrescription.addPrescribedDrug(newDrug);
        currentPrescription.setStatus(PrescriptionStatus.DRAFT); // Set as draft until saved
        
        // Update UI
        prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
        clearPrescriptionForm();
        checkInteractions();
        updateUIState();
        
        // Don't save immediately - user must click Save button
        statusLabel.setText(newDrug.getMedication().getDisplayName() + " added to prescription. Click 'Save' to save to database.");
        prescriptionStatusLabel.setText("Prescription Status: DRAFT (unsaved changes)");
    }

    private void handleRemoveMedication(PrescribedDrug drug) {
        if (currentPrescription != null) {
            currentPrescription.removePrescribedDrug(drug);
            currentPrescription.setStatus(PrescriptionStatus.DRAFT); // Mark as draft when modified
            prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
            checkInteractions();
            updateUIState();
            statusLabel.setText(drug.getMedication().getDisplayName() + " removed from prescription. Click 'Save' to save changes.");
            prescriptionStatusLabel.setText("Prescription Status: DRAFT (unsaved changes)");
        }
    }

    @FXML
    private void handleAcknowledgeAlert() {
        InteractionAlert selectedAlert = alertsListView.getSelectionModel().getSelectedItem();
        if (selectedAlert != null) {
            selectedAlert.acknowledge();
            alertsListView.refresh();
            acknowledgeAlertBtn.setDisable(true);
        }
    }

    @FXML
    private void handleSave() {
        if (currentPrescription != null && !currentPrescription.isEmpty()) {
            // Check for critical alerts but don't block saving
            boolean hasUnacknowledgedCritical = alertList.stream()
                .anyMatch(a -> a.getAlertLevel() == AlertLevel.CRITICAL && !a.isAcknowledged());

            if (hasUnacknowledgedCritical) {
                showWarningAlert("Critical Alerts", "There are unacknowledged critical alerts. Please review them.");
                // Don't return - allow saving anyway for simplified workflow
            }

            currentPrescription.setStatus(PrescriptionStatus.APPROVED);
            currentPrescription.setAlerts(alertList);
            dataService.savePrescription(currentPrescription);
            statusLabel.setText("Prescription saved successfully!");
            updateUIState();

        } else {
            showWarningAlert("Cannot Save", "There is no active or non-empty prescription to save.");
        }
    }

    private void checkInteractions() {
        if (selectedPatient == null || currentPrescription == null || currentPrescription.isEmpty()) {
            alertList.clear();
            updateAlertsSummary();
            interactionStatusLabel.setText("Interactions: N/A");
            return;
        }

        interactionStatusLabel.setText("Checking interactions...");
        CompletableFuture<List<InteractionAlert>> future = interactionEngine.checkAllInteractionsAsync(selectedPatient, currentPrescription, dataService.getInteractionRules(), medicationList);

        future.thenAccept(alerts -> Platform.runLater(() -> {
                alertList.setAll(alerts);
                updateAlertsSummary();
                interactionStatusLabel.setText("Interactions checked.");
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    interactionStatusLabel.setText("Error checking interactions.");
                    showErrorAlert("Interaction Engine Error", "An error occurred while checking for interactions.", ex.getMessage());
                });
                return null;
            });
    }

    private void updateUIState() {
        boolean patientSelected = selectedPatient != null;
        newPrescriptionBtn.setDisable(!patientSelected);
        editPatientBtn.setDisable(!patientSelected);
        deletePatientBtn.setDisable(!patientSelected);

        boolean prescriptionLoaded = currentPrescription != null;
        boolean isDraft = prescriptionLoaded && currentPrescription.getStatus() == PrescriptionStatus.DRAFT;
        boolean hasUnsavedChanges = prescriptionLoaded && !currentPrescription.isEmpty();
        
        addMedicationBtn.setDisable(!prescriptionLoaded);
        // Enable Save button when there are unsaved changes or prescription is in draft status
        saveBtn.setDisable(!hasUnsavedChanges || !isDraft);

        if (prescriptionLoaded) {
            if (isDraft && !currentPrescription.isEmpty()) {
                prescriptionStatusLabel.setText("Prescription Status: DRAFT (unsaved changes)");
            } else if (!prescriptionStatusLabel.getText().contains("modified")) {
                 prescriptionStatusLabel.setText("Prescription Status: " + currentPrescription.getStatus());
            }
        } else {
            prescriptionStatusLabel.setText("No active prescription");
        }
        
        medicationCountLabel.setText(prescribedDrugList.size() + " medications");
        prescriptionTableView.refresh();
    }

    private void updatePatientInfoPanel() {
        if (selectedPatient != null) {
            selectedPatientLabel.setText(selectedPatient.getFullName());
            patientIdLabel.setText(selectedPatient.getPatientId());
            patientAgeLabel.setText("Age: " + selectedPatient.getAge());
            patientGenderLabel.setText("Gender: " + (selectedPatient.getGender() != null ? selectedPatient.getGender() : "N/A"));
            patientAllergiesLabel.setText("Allergies: " + (!selectedPatient.getAllergies().isEmpty() ? String.join(", ", selectedPatient.getAllergies()) : "None"));
            patientConditionsLabel.setText("Conditions: " + (!selectedPatient.getChronicConditions().isEmpty() ? String.join(", ", selectedPatient.getChronicConditions()) : "None reported"));
        } else {
            selectedPatientLabel.setText("No Patient Selected");
            patientIdLabel.setText("");
            patientAgeLabel.setText("Age: N/A");
            patientGenderLabel.setText("Gender: N/A");
            patientAllergiesLabel.setText("Allergies: None");
            patientConditionsLabel.setText("Conditions: None reported");
        }
    }
    
    private void loadPrescriptionIntoView(Prescription prescription) {
        currentPrescription = prescription;
        
        for (PrescribedDrug drug : prescription.getPrescribedDrugs()) {
            medicationList.stream()
                .filter(m -> m.getMedicationId().equals(drug.getMedicationId()))
                .findFirst()
                .ifPresent(drug::setMedication);
        }

        prescribedDrugList.setAll(prescription.getPrescribedDrugs());
        alertList.setAll(prescription.getAlerts());
        updateAlertsSummary();
        statusLabel.setText("Loaded latest prescription for " + selectedPatient.getFullName());
        prescriptionStatusLabel.setText("Prescription Status: " + currentPrescription.getStatus());
    }

    private void updateAlertsSummary() {
        long criticalCount = alertList.stream().filter(a -> a.getAlertLevel() == AlertLevel.CRITICAL).count();
        long warningCount = alertList.stream().filter(a -> a.getAlertLevel() == AlertLevel.WARNING).count();
        long infoCount = alertList.stream().filter(a -> a.getAlertLevel() == AlertLevel.INFO).count();
        
        criticalAlertsLabel.setText(String.valueOf(criticalCount));
        warningAlertsLabel.setText(String.valueOf(warningCount));
        infoAlertsLabel.setText(String.valueOf(infoCount));
    }

    private void clearPrescription() {
        currentPrescription = null;
        prescribedDrugList.clear();
        alertList.clear();
        updateAlertsSummary();
        clearPrescriptionForm();
    }

    private void clearPrescriptionForm() {
        medicationComboBox.setValue(null);
        medicationSearchField.clear();
        medicationSuggestionMenu.hide();
        dosageField.clear();
        frequencyField.clear();
        durationField.clear();
    }

    public void shutdown() {
        // Save all data before shutting down - only if data was loaded successfully
        if (dataService != null && dataLoadedSuccessfully && !patientList.isEmpty()) {
            System.out.println("Saving data on shutdown...");
            dataService.saveAllData(new ArrayList<>(patientList), new ArrayList<>(prescriptionList));
        } else {
            System.out.println("Skipping save - data was not loaded successfully or is empty.");
        }
        
        if (interactionEngine != null) {
            interactionEngine.shutdown();
        }
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}