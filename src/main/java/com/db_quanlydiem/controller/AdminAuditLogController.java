package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.AuditLogDAO;
import com.db_quanlydiem.model.AuditLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminAuditLogController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<AuditLog> tableLog;
    @FXML private TableColumn<AuditLog, Integer> colID;
    @FXML private TableColumn<AuditLog, String> colTime, colUser, colAction, colDesc;

    private AuditLogDAO logDAO = new AuditLogDAO();
    private ObservableList<AuditLog> logList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colID.setCellValueFactory(new PropertyValueFactory<>("logID"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Format thời gian
        colTime.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getFormattedTime()));

        loadData();
    }

    private void loadData() {
        logList.clear();
        logList.addAll(logDAO.getAllLogs());
        tableLog.setItems(logList);
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if(k.isEmpty()) loadData();
        else {
            logList.clear();
            logList.addAll(logDAO.searchLogs(k));
            tableLog.setItems(logList);
        }
    }

    @FXML
    public void handleRefresh() {
        txtSearch.clear();
        loadData();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}