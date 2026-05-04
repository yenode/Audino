package com.audino.controller;

import com.audino.model.AuditLog;
import com.audino.service.DataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class AuditLogController {

    @FXML private TableView<AuditLog> logTableView;
    @FXML private TableColumn<AuditLog, String> timestampCol;
    @FXML private TableColumn<AuditLog, String> userCol;
    @FXML private TableColumn<AuditLog, String> operationCol;
    @FXML private TableColumn<AuditLog, String> entityTypeCol;
    @FXML private TableColumn<AuditLog, String> entityIdCol;
    @FXML private TableColumn<AuditLog, String> detailsCol;
    @FXML private Label logCountLabel;

    private Stage dialogStage;
    private DataService dataService;

    @FXML
    public void initialize() {
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        operationCol.setCellValueFactory(new PropertyValueFactory<>("operation"));
        entityTypeCol.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityIdCol.setCellValueFactory(new PropertyValueFactory<>("entityId"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
        loadLogs();
    }

    private void loadLogs() {
        if (dataService == null) return;
        List<AuditLog> logs = dataService.getAuditLogs(500);
        logTableView.setItems(FXCollections.observableArrayList(logs));
        logCountLabel.setText(logs.size() + " entries");
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}
