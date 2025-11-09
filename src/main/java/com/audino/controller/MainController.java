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
import java.util.List;
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
    private final ObservableList<InteractionAlert> alertList = FXCollections.observableArrayList();
    private final ObservableList<PrescribedDrug> prescribedDrugList = FXCollections.observableArrayList();

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
            statusLabel.setText("Data loaded successfully.");
        } catch (Exception e) {
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
                setText(empty ? null : med.getDisplayName() + " (" + med.getGenericName() + ")");
            }
        };
        medicationComboBox.setCellFactory(cellFactory);
        medicationComboBox.setButtonCell(cellFactory.call(null));
    }

    private void setupPrescriptionTable() {
        medicationColumn.setCellValueFactory(cellData -> {
            PrescribedDrug drug = cellData.getValue();
            if (drug.getMedication() != null) {
                return new SimpleStringProperty(drug.getMedication().getDisplayName());
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
                    removeBtn.setDisable(currentPrescription != null && currentPrescription.getStatus() != PrescriptionStatus.DRAFT);
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
            (obs, oldVal, newVal) -> medicationComboBox.setItems(
                FXCollections.observableArrayList(dataService.searchMedications(newVal))));
    }

    @FXML
    private void handlePatientSelection(Patient patient) {
        selectedPatient = patient;
        updatePatientInfoPanel();
        
        if (patient != null) {
            List<Prescription> pastPrescriptions = dataService.getPrescriptionsForPatient(patient);
            if (!pastPrescriptions.isEmpty()) {
                loadPrescriptionIntoView(pastPrescriptions.get(0));
            } else {
                clearPrescription();
            }
        } else {
            clearPrescription();
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
        currentPrescription = new Prescription(selectedPatient, "Dr. User");
        clearPrescriptionForm();
        prescribedDrugList.clear();
        alertList.clear();
        updateAlertsSummary();
        updateUIState();
        statusLabel.setText("New prescription started for " + selectedPatient.getFullName());
    }

    @FXML
    private void handleRefresh() {
        final String selectedPatientId = (selectedPatient != null) ? selectedPatient.getPatientId() : null;

        loadData();

        if (selectedPatientId != null) {
            patientList.stream()
                .filter(p -> selectedPatientId.equals(p.getPatientId()))
                .findFirst()
                .ifPresent(p -> {
                    patientListView.getSelectionModel().select(p);
                    patientListView.scrollTo(p);
                });
        }
    }

    @FXML
    private void handlePatientSearch() {
        String searchTerm = patientSearchField.getText();
        patientList.setAll(dataService.searchPatients(searchTerm));
    }

    @FXML
    private void handleAddMedication() {
        if (currentPrescription == null) {
            showWarningAlert("No Active Prescription", "Please start a new prescription before adding medications.");
            return;
        }

        if (currentPrescription.getStatus() == PrescriptionStatus.APPROVED) {
            Prescription newDraft = new Prescription(selectedPatient, "Dr. User");
            for (PrescribedDrug drug : currentPrescription.getPrescribedDrugs()) {
                newDraft.addPrescribedDrug(drug);
            }
            currentPrescription = newDraft;
            statusLabel.setText("New draft created. Click 'Save' to approve changes.");
            prescriptionStatusLabel.setText("Prescription Status: DRAFT (modified)");
        }

        Medication selectedMed = medicationComboBox.getValue();
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

        PrescribedDrug newDrug = new PrescribedDrug(selectedMed, dosage, frequency, duration, "", "Dr. User");
        currentPrescription.addPrescribedDrug(newDrug);
        prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
        clearPrescriptionForm();
        checkInteractions();
        updateUIState();
        statusLabel.setText(newDrug.getMedication().getDisplayName() + " added to draft. Click 'Save' to approve.");
        prescriptionStatusLabel.setText("Prescription Status: DRAFT (modified)");
    }

    private void handleRemoveMedication(PrescribedDrug drug) {
        if (currentPrescription != null && currentPrescription.getStatus() == PrescriptionStatus.DRAFT) {
            currentPrescription.removePrescribedDrug(drug);
            prescribedDrugList.setAll(currentPrescription.getPrescribedDrugs());
            checkInteractions();
            updateUIState();
            statusLabel.setText(drug.getMedication().getDisplayName() + " removed from draft. Click 'Save' to approve.");
            prescriptionStatusLabel.setText("Prescription Status: DRAFT (modified)");
        } else {
             showWarningAlert("Prescription Not Editable", "This prescription is not a draft and cannot be modified.");
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
            boolean hasUnacknowledgedCritical = alertList.stream()
                .anyMatch(a -> a.getAlertLevel() == AlertLevel.CRITICAL && !a.isAcknowledged());

            if (hasUnacknowledgedCritical) {
                showWarningAlert("Unacknowledged Critical Alerts", "You must acknowledge all critical alerts before saving the prescription.");
                return;
            }

            currentPrescription.setStatus(PrescriptionStatus.APPROVED);
            currentPrescription.setAlerts(alertList);
            dataService.savePrescription(currentPrescription);
            statusLabel.setText("Prescription saved and approved.");
            showInfoAlert("Success", "Prescription has been saved to the database.");
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
        
        addMedicationBtn.setDisable(!prescriptionLoaded);
        saveBtn.setDisable(!isDraft || currentPrescription.isEmpty());

        if (prescriptionLoaded) {
            if (!prescriptionStatusLabel.getText().contains("modified")) {
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
        dosageField.clear();
        frequencyField.clear();
        durationField.clear();
    }

    public void shutdown() {
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
}