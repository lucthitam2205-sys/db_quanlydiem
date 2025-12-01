package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main; // Import Main để lấy resource
import com.db_quanlydiem.dao.SemesterDAO;
import com.db_quanlydiem.dao.TuitionConfigDAO;
import com.db_quanlydiem.model.Semester;
import com.db_quanlydiem.model.TuitionConfig;
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

public class AdminTuitionController implements Initializable {

    @FXML private TableView<TuitionConfig> tableTuition;
    @FXML private TableColumn<TuitionConfig, String> colSemester, colDesc;
    @FXML private TableColumn<TuitionConfig, Double> colPrice;

    @FXML private ComboBox<Semester> cbSemester;
    @FXML private TextField txtPrice;
    @FXML private TextArea txtDesc;

    private TuitionConfigDAO tuitionDAO = new TuitionConfigDAO();
    private SemesterDAO semesterDAO = new SemesterDAO();
    private ObservableList<TuitionConfig> tuitionList = FXCollections.observableArrayList();

    private int selectedConfigID = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semesterID"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerCredit"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(String.format("%,.0f VNĐ", price));
            }
        });

        cbSemester.setItems(FXCollections.observableArrayList(semesterDAO.getAllSemesters()));
        loadData();

        tableTuition.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        tuitionList.clear();
        tuitionList.addAll(tuitionDAO.getAllConfigs());
        tableTuition.setItems(tuitionList);
    }

    private void fillForm(TuitionConfig tc) {
        selectedConfigID = tc.getConfigID();
        for (Semester s : cbSemester.getItems()) {
            if (s.getSemesterID().equals(tc.getSemesterID())) {
                cbSemester.setValue(s);
                break;
            }
        }
        cbSemester.setDisable(true);
        txtPrice.setText(String.valueOf((long)tc.getPricePerCredit()));
        txtDesc.setText(tc.getDescription());
    }

    @FXML
    public void handleAdd() {
        if (!validateForm()) return;
        if (tuitionDAO.getConfigBySemester(cbSemester.getValue().getSemesterID()) != null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Học kỳ này đã được cấu hình học phí rồi! Vui lòng chọn sửa.");
            return;
        }
        TuitionConfig tc = new TuitionConfig(0, cbSemester.getValue().getSemesterID(), Double.parseDouble(txtPrice.getText()), txtDesc.getText());
        if (tuitionDAO.addConfig(tc)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu cấu hình học phí!");
            loadData(); handleClear();
        } else showAlert(Alert.AlertType.ERROR, "Lỗi", "Lưu thất bại.");
    }

    @FXML
    public void handleUpdate() {
        if (selectedConfigID == -1) { showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn dòng cần sửa."); return; }
        if (!validateForm()) return;
        TuitionConfig tc = new TuitionConfig(selectedConfigID, cbSemester.getValue().getSemesterID(), Double.parseDouble(txtPrice.getText()), txtDesc.getText());
        if (tuitionDAO.updateConfig(tc)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật đơn giá!");
            loadData(); handleClear();
        } else showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
    }

    @FXML
    public void handleDelete() {
        if (selectedConfigID == -1) { showAlert(Alert.AlertType.WARNING, "Chú ý", "Chọn dòng cần xóa!"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa cấu hình giá của kỳ: " + cbSemester.getValue().getSemesterName() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (tuitionDAO.deleteConfig(selectedConfigID)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa cấu hình.");
                loadData(); handleClear();
            } else showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa thất bại.");
        }
    }

    @FXML
    public void handleClear() {
        selectedConfigID = -1;
        cbSemester.setValue(null);
        cbSemester.setDisable(false);
        txtPrice.clear();
        txtDesc.clear();
        tableTuition.getSelectionModel().clearSelection();
    }

    // --- SỬA LỖI NÚT BACK TẠI ĐÂY ---
    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableTuition.getScene().getWindow();

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
        if (cbSemester.getValue() == null || txtPrice.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Học kỳ và nhập Đơn giá.");
            return false;
        }
        try {
            double price = Double.parseDouble(txtPrice.getText());
            if (price < 0) { showAlert(Alert.AlertType.WARNING, "Lỗi giá trị", "Đơn giá không được âm."); return false; }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi định dạng", "Đơn giá phải là số.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}