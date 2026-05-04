package com.audino.controller;

import com.audino.model.User;
import com.audino.service.DataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class UserManagerController {

    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> roleCol;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private DataService dataService;
    private Stage dialogStage;
    private ObservableList<User> userList = FXCollections.observableArrayList();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
        loadUsers();
    }

    @FXML
    private void initialize() {
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        roleComboBox.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void loadUsers() {
        if (dataService != null) {
            List<User> users = dataService.getAllUsers();
            userList.setAll(users);
            userTableView.setItems(userList);
        }
    }

    @FXML
    private void handleAddUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Validation Error", "All fields are required.");
            return;
        }

        if (userList.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            showAlert("Validation Error", "Username already exists.");
            return;
        }

        try {
            dataService.createUser(username, password, role);
            loadUsers();
            usernameField.clear();
            passwordField.clear();
            roleComboBox.getSelectionModel().selectFirst();
            showAlert("Success", "User created successfully.");
        } catch (Exception e) {
            showAlert("Error", "Could not create user: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
