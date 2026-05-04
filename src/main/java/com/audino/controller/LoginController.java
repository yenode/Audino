package com.audino.controller;

import com.audino.model.User;
import com.audino.service.DataService;
import com.audino.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private DataService dataService;

    @FXML
    public void initialize() {
        dataService = new DataService();
        // Load data so that the DB and admin user is seeded
        try {
            dataService.loadAllData();
        } catch (Exception e) {
            errorLabel.setText("Database connection error");
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Please enter username and password");
            errorLabel.setVisible(true);
            return;
        }

        User user = dataService.authenticate(username, password);
        if (user != null) {
            SessionManager.getInstance().login(user);
            openMainWindow();
        } else {
            errorLabel.setText("Invalid credentials");
            errorLabel.setVisible(true);
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
            BorderPane root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

            stage.setTitle("Audino: Intelligent Prescription Manager");
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(800);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
