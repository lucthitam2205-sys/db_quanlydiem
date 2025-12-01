package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.SubjectDAO;
import com.db_quanlydiem.model.Subject;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminSubjectController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Subject> tableSubject;
    @FXML private TableColumn<Subject, String> colID, colName;
    @FXML private TableColumn<Subject, Integer> colCredit;

    // Form Inputs
    @FXML private TextField txtID, txtName;
    @FXML private Spinner<Integer> spCredit;

    private SubjectDAO subjectDAO = new SubjectDAO();
    private ObservableList<Subject> subjectList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Cấu hình bảng
        colID.setCellValueFactory(new PropertyValueFactory<>("subjectId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("subjectCredit"));

        // 2. Cấu hình Spinner (Số tín chỉ từ 1 đến 10)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3);
        spCredit.setValueFactory(valueFactory);

        // 3. Load dữ liệu
        loadData();

        // 4. Sự kiện chọn dòng
        tableSubject.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        subjectList.clear();
        subjectList.addAll(subjectDAO.getAllSubjects());
        tableSubject.setItems(subjectList);
    }

    private void fillForm(Subject s) {
        txtID.setText(s.getSubjectId());
        txtID.setDisable(true); // Không cho sửa ID
        txtName.setText(s.getSubjectName());
        spCredit.getValueFactory().setValue(s.getSubjectCredit());
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if (k.isEmpty()) loadData();
        else {
            subjectList.clear();
            subjectList.addAll(subjectDAO.searchSubject(k));
            tableSubject.setItems(subjectList);
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

        Subject s = new Subject(
                txtID.getText(),
                txtName.getText(),
                spCredit.getValue()
        );

        if (subjectDAO.addSubject(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm môn học mới!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã môn học có thể đã tồn tại.");
        }
    }

    @FXML
    public void handleUpdate() {
        Subject selected = tableSubject.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Subject s = new Subject(
                txtID.getText(),
                txtName.getText(),
                spCredit.getValue()
        );

        if (subjectDAO.updateSubject(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật môn học!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        Subject selected = tableSubject.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Chọn môn học cần xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa môn học: " + selected.getSubjectName() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (subjectDAO.deleteSubject(selected.getSubjectId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa môn học.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa (Môn học đã có lớp mở).");
            }
        }
    }

    @FXML
    public void handleClear() {
        txtID.clear(); txtID.setDisable(false);
        txtName.clear();
        spCredit.getValueFactory().setValue(3);
        tableSubject.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableSubject.getScene().getWindow();

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
        if (txtID.getText().isEmpty() || txtName.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Mã và Tên môn học.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}