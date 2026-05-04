package com.audino.controller;

import com.audino.model.Medication;
import com.audino.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PricingPromptController {

    @FXML private Label promptMessageLabel;
    @FXML private Label errorLabel;
    @FXML private TextField priceField;
    @FXML private PasswordField adminPasswordField;

    private Stage dialogStage;
    private Medication medication;
    private boolean saveClicked = false;
    private DataService dataService;

    @FXML
    private void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
        promptMessageLabel.setText("Missing pricing for '" + medication.getDisplayName() + "'. Enter price per unit to continue.");
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleSave() {
        errorLabel.setVisible(false);
        try {
            String priceText = priceField.getText().trim();
            if (priceText.isEmpty()) {
                showError("Please enter a price.");
                return;
            }
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Price must be greater than $0.00.");
                return;
            }

            String adminPwd = adminPasswordField.getText();
            if (adminPwd.isEmpty()) {
                showError("Admin password is required.");
                return;
            }
            if (dataService == null || !dataService.checkPassword("admin", adminPwd)) {
                showError("Invalid admin password. Access denied.");
                return;
            }

            medication.setPricePerUnit(price);
            saveClicked = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            showError("Invalid price — enter a number like 5.50.");
        }
    }

    @FXML
    private void handleCancel() {
        saveClicked = false;
        dialogStage.close();
    }
}
