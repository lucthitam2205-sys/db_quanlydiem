package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.SemesterDAO;
import com.db_quanlydiem.model.Semester;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.sql.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminSemesterController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Semester> tableSemester;
    @FXML private TableColumn<Semester, String> colID, colName;
    @FXML private TableColumn<Semester, Date> colStart, colEnd;

    // Form Inputs
    @FXML private TextField txtID, txtName;
    @FXML private DatePicker dpStart, dpEnd;

    private SemesterDAO semesterDAO = new SemesterDAO();
    private ObservableList<Semester> semesterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Cấu hình bảng
        colID.setCellValueFactory(new PropertyValueFactory<>("semesterID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("semesterName"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // 2. Load dữ liệu
        loadData();

        // 3. Sự kiện chọn dòng
        tableSemester.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        semesterList.clear();
        semesterList.addAll(semesterDAO.getAllSemesters());
        tableSemester.setItems(semesterList);
    }

    private void fillForm(Semester s) {
        txtID.setText(s.getSemesterID());
        txtID.setDisable(true); // Không cho sửa ID
        txtName.setText(s.getSemesterName());
        if (s.getStartDate() != null) dpStart.setValue(s.getStartDate().toLocalDate());
        if (s.getEndDate() != null) dpEnd.setValue(s.getEndDate().toLocalDate());
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if (k.isEmpty()) loadData();
        else {
            semesterList.clear();
            semesterList.addAll(semesterDAO.searchSemester(k));
            tableSemester.setItems(semesterList);
        }
    }

    @FXML
    public void handleRefresh() {
        loadData();
        handleClear();
    }

    @FXML
    public void handleAdd() {
        if (!validateForm()) return;

        Semester s = new Semester(
                txtID.getText(),
                txtName.getText(),
                Date.valueOf(dpStart.getValue()),
                Date.valueOf(dpEnd.getValue())
        );

        if (semesterDAO.addSemester(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm học kỳ mới!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã học kỳ có thể đã tồn tại.");
        }
    }

    @FXML
    public void handleUpdate() {
        Semester selected = tableSemester.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!validateForm()) return;

        Semester s = new Semester(
                txtID.getText(),
                txtName.getText(),
                Date.valueOf(dpStart.getValue()),
                Date.valueOf(dpEnd.getValue())
        );

        if (semesterDAO.updateSemester(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật học kỳ!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        Semester selected = tableSemester.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Chọn học kỳ cần xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa học kỳ: " + selected.getSemesterName() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (semesterDAO.deleteSemester(selected.getSemesterID())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa học kỳ.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa (Học kỳ đã có lớp học).");
            }
        }
    }

    @FXML
    public void handleClear() {
        txtID.clear(); txtID.setDisable(false);
        txtName.clear();
        dpStart.setValue(null);
        dpEnd.setValue(null);
        tableSemester.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableSemester.getScene().getWindow();

            // Chuyển cảnh
            stage.setScene(new Scene(root));
            stage.setTitle("Hệ thống Quản trị Đào tạo (Admin)");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi điều hướng", "Không thể quay về Dashboard: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (txtID.getText().isEmpty() || txtName.getText().isEmpty() || dpStart.getValue() == null || dpEnd.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ Mã, Tên và Thời gian.");
            return false;
        }
        if (dpEnd.getValue().isBefore(dpStart.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Lỗi thời gian", "Ngày kết thúc phải sau ngày bắt đầu.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}