package com.audino.controller;

import com.audino.model.Medication;
import com.audino.model.MedicationType;
import com.audino.model.TabletMedication;
import com.audino.model.LiquidMedication;
import com.audino.model.InjectionMedication;
import com.audino.service.DataService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class MedicationManagerController {

    @FXML private TableView<Medication> medTableView;
    @FXML private TableColumn<Medication, String> idCol;
    @FXML private TableColumn<Medication, String> nameCol;
    @FXML private TableColumn<Medication, String> brandCol;
    @FXML private TableColumn<Medication, String> rxNormCol;
    @FXML private TableColumn<Medication, String> typeCol;
    @FXML private TableColumn<Medication, Number> priceCol;

    @FXML private TextField idField;
    @FXML private TextField genericNameField;
    @FXML private TextField brandNameField;
    @FXML private TextField rxNormField;
    @FXML private ComboBox<MedicationType> typeComboBox;
    @FXML private TextField priceField;
    @FXML private TextArea interactionsArea;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private DataService dataService;
    private ObservableList<Medication> medList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMedicationId()));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenericName()));
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrandName()));
        rxNormCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRxNormCode()));
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClass().getSimpleName().replace("Medication", "")));
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPricePerUnit()));

        typeComboBox.setItems(FXCollections.observableArrayList(MedicationType.values()));
        typeComboBox.getSelectionModel().selectFirst();

        medTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMedicationDetails(newValue));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
        loadMedications();
    }

    private void loadMedications() {
        if (dataService != null) {
            medList.setAll(dataService.getAllMedications());
            medTableView.setItems(medList);
        }
    }

    private void showMedicationDetails(Medication med) {
        if (med != null) {
            idField.setText(med.getMedicationId());
            genericNameField.setText(med.getGenericName());
            brandNameField.setText(med.getBrandName());
            rxNormField.setText(med.getRxNormCode() != null ? med.getRxNormCode() : "");
            priceField.setText(String.format("%.2f", med.getPricePerUnit()));
            
            if (med instanceof TabletMedication) typeComboBox.setValue(MedicationType.TABLET);
            else if (med instanceof LiquidMedication) typeComboBox.setValue(MedicationType.LIQUID);
            else if (med instanceof InjectionMedication) typeComboBox.setValue(MedicationType.INJECTION);
            
            loadInteractionsFor(med.getGenericName());
        } else {
            handleClear();
        }
    }

    private void loadInteractionsFor(String drugName) {
        if (drugName == null || drugName.isEmpty() || dataService == null) {
            interactionsArea.setText("No drug selected.");
            return;
        }
        
        Map<String, Object> rules = dataService.getInteractionRules();
        if (rules == null || rules.isEmpty()) {
            interactionsArea.setText("No interaction rules loaded.");
            return;
        }

        StringBuilder info = new StringBuilder();
        
        // Drug-Drug
        Map<String, Object> drugDrug = (Map<String, Object>) rules.get("drugDrugInteractions");
        if (drugDrug != null) {
            for (Object ruleObj : drugDrug.values()) {
                Map<String, Object> rule = (Map<String, Object>) ruleObj;
                List<String> d1s = (List<String>) rule.get("drug1");
                List<String> d2s = (List<String>) rule.get("drug2");
                if ((d1s != null && d1s.stream().anyMatch(d -> d.equalsIgnoreCase(drugName))) || 
                    (d2s != null && d2s.stream().anyMatch(d -> d.equalsIgnoreCase(drugName)))) {
                    info.append("Drug-Drug (").append(rule.get("severity")).append("): ").append(d1s).append(" + ").append(d2s).append(" -> ").append(rule.get("description")).append("\n");
                }
            }
        }
        
        // Drug-Condition
        Map<String, Object> drugCond = (Map<String, Object>) rules.get("drugConditionInteractions");
        if (drugCond != null) {
            for (Object ruleObj : drugCond.values()) {
                Map<String, Object> rule = (Map<String, Object>) ruleObj;
                List<String> meds = (List<String>) rule.get("medicationClasses");
                if (meds != null && meds.stream().anyMatch(m -> m.equalsIgnoreCase(drugName))) {
                    info.append("Condition (").append(rule.get("severity")).append("): ").append(rule.get("conditionKeywords")).append(" -> ").append(rule.get("description")).append("\n");
                }
            }
        }
        
        // Drug-Allergy
        Map<String, Object> drugAllergy = (Map<String, Object>) rules.get("drugAllergyInteractions");
        if (drugAllergy != null) {
            for (Object ruleObj : drugAllergy.values()) {
                Map<String, Object> rule = (Map<String, Object>) ruleObj;
                List<String> meds = (List<String>) rule.get("medicationClasses");
                if (meds != null && meds.stream().anyMatch(m -> m.equalsIgnoreCase(drugName))) {
                    info.append("Allergy (").append(rule.get("severity")).append("): ").append(rule.get("allergyKeywords")).append(" -> ").append(rule.get("description")).append("\n");
                }
            }
        }

        if (info.length() == 0) {
            interactionsArea.setText("No known interactions for " + drugName);
        } else {
            interactionsArea.setText(info.toString());
        }
    }

    @FXML
    private void handleClear() {
        idField.clear();
        genericNameField.clear();
        brandNameField.clear();
        rxNormField.clear();
        priceField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        interactionsArea.setText("");
        errorLabel.setVisible(false);
        medTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);
        try {
            String id = idField.getText().trim();
            String genericName = genericNameField.getText().trim();
            String brandName = brandNameField.getText().trim();
            String rxNorm = rxNormField.getText().trim();
            MedicationType type = typeComboBox.getValue();
            String priceStr = priceField.getText().trim();

            if (id.isBlank() || genericName.isBlank() || priceStr.isBlank()) {
                showError("Please fill all required fields (ID, Generic Name, Price).");
                return;
            }

            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                showError("Price cannot be negative.");
                return;
            }

            Medication med;
            switch (type) {
                case LIQUID: med = new LiquidMedication(); break;
                case INJECTION: med = new InjectionMedication(); break;
                default: med = new TabletMedication(); break;
            }

            med.setMedicationId(id);
            med.setGenericName(genericName);
            med.setBrandName(brandName.isEmpty() ? genericName : brandName);
            med.setRxNormCode(rxNorm);
            med.setPricePerUnit(price);

            // check if update or save
            if (dataService.getAllMedications().stream().anyMatch(m -> m.getMedicationId().equals(id))) {
                dataService.updateMedication(med);
            } else {
                dataService.saveMedication(med);
            }

            errorLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            errorLabel.setText("✓ Medication '" + genericName + "' saved successfully!");
            errorLabel.setVisible(true);

            loadMedications();
        } catch (NumberFormatException e) {
            showError("Invalid price — please enter a valid number (e.g. 5.50).");
        } catch (RuntimeException e) {
            showError("Error saving: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
