package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.ProfessorDAO;
import com.db_quanlydiem.model.Professor;
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

public class AdminProfessorController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Professor> tableProfessor;
    @FXML private TableColumn<Professor, String> colID, colName, colTitle, colEmail, colPhone;

    // Form inputs
    @FXML private TextField txtID, txtName, txtEmail, txtPhone, txtHometown;
    @FXML private ComboBox<String> cbTitle;

    private ProfessorDAO professorDAO = new ProfessorDAO();
    private ObservableList<Professor> professorList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Setup Table Columns
        colID.setCellValueFactory(new PropertyValueFactory<>("professorID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("professorName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("professorTitle"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("professorEmail"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("professorPhone"));

        // 2. Setup ComboBox
        cbTitle.setItems(FXCollections.observableArrayList("Cử nhân", "Thạc sĩ", "Tiến sĩ", "Phó Giáo sư", "Giáo sư"));

        // 3. Load Data
        loadData();

        // 4. Selection Event
        tableProfessor.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        professorList.clear();
        professorList.addAll(professorDAO.getAllProfessors());
        tableProfessor.setItems(professorList);
    }

    private void fillForm(Professor p) {
        txtID.setText(p.getProfessorID());
        txtID.setDisable(true); // Khóa ID khi sửa
        txtName.setText(p.getProfessorName());
        txtEmail.setText(p.getProfessorEmail());
        txtPhone.setText(p.getProfessorPhone());
        txtHometown.setText(p.getProfessorHometown());
        cbTitle.setValue(p.getProfessorTitle());
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if (k.isEmpty()) loadData();
        else {
            professorList.clear();
            professorList.addAll(professorDAO.searchProfessor(k));
            tableProfessor.setItems(professorList);
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
        Professor p = new Professor(
                txtID.getText(), txtName.getText(), txtEmail.getText(),
                txtPhone.getText(), txtHometown.getText(), cbTitle.getValue()
        );
        if (professorDAO.addProfessor(p)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm giảng viên mới!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã GV có thể đã tồn tại.");
        }
    }

    @FXML
    public void handleUpdate() {
        Professor selected = tableProfessor.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Professor p = new Professor(
                txtID.getText(), txtName.getText(), txtEmail.getText(),
                txtPhone.getText(), txtHometown.getText(), cbTitle.getValue()
        );

        if (professorDAO.updateProfessor(p)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin giảng viên!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        Professor selected = tableProfessor.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Hãy chọn giảng viên cần xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa giảng viên: " + selected.getProfessorName() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (professorDAO.deleteProfessor(selected.getProfessorID())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa giảng viên.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa (có thể đang dạy lớp học phần).");
            }
        }
    }

    @FXML
    public void handleClear() {
        txtID.clear(); txtID.setDisable(false);
        txtName.clear(); txtEmail.clear(); txtPhone.clear(); txtHometown.clear();
        cbTitle.setValue(null);
        tableProfessor.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableProfessor.getScene().getWindow();

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
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Mã GV và Họ tên là bắt buộc.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}