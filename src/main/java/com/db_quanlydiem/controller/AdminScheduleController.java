package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.ClassScheduleDAO;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.model.ClassSchedule;
import com.db_quanlydiem.model.CourseClass;
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

public class AdminScheduleController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<ClassSchedule> tableSchedule;
    @FXML private TableColumn<ClassSchedule, Integer> colID;
    @FXML private TableColumn<ClassSchedule, String> colClassID, colDay, colShift, colRoom;

    // Form Inputs
    @FXML private ComboBox<CourseClass> cbClass;
    @FXML private ComboBox<String> cbDay;
    @FXML private TextField txtShift, txtRoom;

    // DAOs
    private ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private ObservableList<ClassSchedule> scheduleList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Setup Table Columns
        colID.setCellValueFactory(new PropertyValueFactory<>("scheduleID"));
        colClassID.setCellValueFactory(new PropertyValueFactory<>("courseClassID"));
        colDay.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        colShift.setCellValueFactory(new PropertyValueFactory<>("shift"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));

        // 2. Setup ComboBox
        cbDay.setItems(FXCollections.observableArrayList("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"));

        // Load danh sách lớp vào ComboBox
        cbClass.setItems(FXCollections.observableArrayList(courseClassDAO.getAllCourseClasses()));

        // 3. Load Table Data
        loadData();

        // 4. Selection Event
        tableSchedule.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        scheduleList.clear();
        scheduleList.addAll(scheduleDAO.getAllSchedules());
        tableSchedule.setItems(scheduleList);
    }

    private void fillForm(ClassSchedule cs) {
        // Chọn lớp trong combobox tương ứng với ID
        for(CourseClass cc : cbClass.getItems()) {
            if(cc.getCourseClassId().equals(cs.getCourseClassID())) {
                cbClass.setValue(cc);
                break;
            }
        }
        cbDay.setValue(cs.getDayOfWeek());
        txtShift.setText(cs.getShift());
        txtRoom.setText(cs.getRoom());
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if (k.isEmpty()) loadData();
        else {
            scheduleList.clear();
            scheduleList.addAll(scheduleDAO.searchSchedule(k));
            tableSchedule.setItems(scheduleList);
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

        ClassSchedule cs = new ClassSchedule(
                0, // ID tự tăng
                cbClass.getValue().getCourseClassId(),
                cbDay.getValue(),
                txtShift.getText(),
                txtRoom.getText()
        );

        if (scheduleDAO.addSchedule(cs)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm lịch học mới!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm lịch học.");
        }
    }

    @FXML
    public void handleUpdate() {
        ClassSchedule selected = tableSchedule.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!validateForm()) return;

        ClassSchedule cs = new ClassSchedule(
                selected.getScheduleID(),
                cbClass.getValue().getCourseClassId(),
                cbDay.getValue(),
                txtShift.getText(),
                txtRoom.getText()
        );

        if (scheduleDAO.updateSchedule(cs)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật lịch học!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        ClassSchedule selected = tableSchedule.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Chọn lịch cần xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa lịch học ID: " + selected.getScheduleID() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (scheduleDAO.deleteSchedule(selected.getScheduleID())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa lịch học.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa.");
            }
        }
    }

    @FXML
    public void handleClear() {
        cbClass.setValue(null);
        cbDay.setValue(null);
        txtShift.clear();
        txtRoom.clear();
        tableSchedule.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableSchedule.getScene().getWindow();

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
        if (cbClass.getValue() == null || cbDay.getValue() == null || txtShift.getText().isEmpty() || txtRoom.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ Lớp, Thứ, Ca và Phòng.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}