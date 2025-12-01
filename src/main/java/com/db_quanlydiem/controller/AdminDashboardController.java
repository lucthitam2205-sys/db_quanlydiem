package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminDashboardController {

    // --- CÁC HÀM XỬ LÝ CHUYỂN CẢNH ---

    @FXML
    public void handleStudentManagement(ActionEvent event) {
        switchScene(event, "admin_student_management.fxml", "Quản lý Sinh viên");
    }

    @FXML
    public void handleProfessorManagement(ActionEvent event) {
        switchScene(event, "admin_professor_management.fxml", "Quản lý Giảng viên");
    }

    @FXML
    public void handleAccountManagement(ActionEvent event) {
        switchScene(event, "admin_account_management.fxml", "Quản lý Tài khoản");
    }

    @FXML
    public void handleSubjectManagement(ActionEvent event) {
        switchScene(event, "admin_subject_management.fxml", "Quản lý Môn học");
    }

    @FXML
    public void handleCourseClassManagement(ActionEvent event) {
        switchScene(event, "admin_course_class_management.fxml", "Quản lý Lớp học phần");
    }

    @FXML
    public void handleSemesterManagement(ActionEvent event) {
        switchScene(event, "admin_semester_management.fxml", "Quản lý Học kỳ");
    }

    @FXML
    public void handleScheduleManagement(ActionEvent event) {
        switchScene(event, "admin_schedule_management.fxml", "Quản lý Lịch học");
    }

    @FXML
    public void handleTuitionManagement(ActionEvent event) {
        switchScene(event, "admin_tuition_management.fxml", "Cấu hình Học phí");
    }

    // --- XỬ LÝ ĐĂNG XUẤT ---
    @FXML
    public void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            switchScene(event, "login.fxml", "Đăng nhập hệ thống");
        }
    }

    // --- HÀM CHUYỂN CẢNH CHUNG (Helper Method) ---
    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ nút bấm
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi hệ thống");
            alert.setContentText("Không thể tải màn hình: " + fxmlFile);
            alert.showAndWait();
        }
    }
}