package com.audino.controller;

import com.audino.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PatientDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField contactField;
    @FXML private TextArea allergiesArea;
    @FXML private TextArea conditionsArea;

    private Stage dialogStage;
    private Patient patient;
    private boolean saved = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            firstNameField.setText(patient.getFirstName());
            lastNameField.setText(patient.getLastName());
            dobPicker.setValue(patient.getDateOfBirth());
            genderComboBox.setValue(patient.getGender());
            contactField.setText(patient.getContactNumber());
            allergiesArea.setText(String.join("\n", patient.getAllergies()));
            conditionsArea.setText(String.join("\n", patient.getChronicConditions()));
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            patient.setFirstName(firstNameField.getText());
            patient.setLastName(lastNameField.getText());
            patient.setDateOfBirth(dobPicker.getValue());
            patient.setGender(genderComboBox.getValue());
            patient.setContactNumber(contactField.getText());

            List<String> allergies = Arrays.stream(allergiesArea.getText().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            patient.setAllergies(allergies);

            List<String> conditions = Arrays.stream(conditionsArea.getText().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            patient.setChronicConditions(conditions);

            saved = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (firstNameField.getText() == null || firstNameField.getText().isEmpty()) {
            errorMessage += "No valid first name!\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().isEmpty()) {
            errorMessage += "No valid last name!\n";
        }
        if (dobPicker.getValue() == null || dobPicker.getValue().isAfter(LocalDate.now())) {
            errorMessage += "No valid date of birth!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}