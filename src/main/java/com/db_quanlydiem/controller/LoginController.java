package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.AccountDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private RadioButton rdStudent, rdProfessor, rdAdmin;
    @FXML private Button btnLogin;

    private AccountDAO accountDAO = new AccountDAO();

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // 1. Kiểm tra thông tin đăng nhập từ CSDL
        String roleFromDB = accountDAO.checkLogin(username, password);

        if (roleFromDB == null) {
            showAlert("Đăng nhập thất bại", "Sai tên đăng nhập hoặc mật khẩu!");
            return;
        }

        // 2. Kiểm tra vai trò (Fix lỗi: So sánh cả tiếng Việt và tiếng Anh)
        String selectedRole = getSelectedRole();
        boolean isRoleMatch = false;

        // Logic so sánh linh hoạt hơn
        if (selectedRole.equals("Sinh viên") && (roleFromDB.equalsIgnoreCase("Student") || roleFromDB.equalsIgnoreCase("Sinh viên"))) {
            isRoleMatch = true;
        }
        else if (selectedRole.equals("Giảng viên") && (roleFromDB.equalsIgnoreCase("Professor") || roleFromDB.equalsIgnoreCase("Giảng viên"))) {
            isRoleMatch = true;
        }
        else if (selectedRole.equals("Admin") && (roleFromDB.equalsIgnoreCase("Admin") || roleFromDB.equalsIgnoreCase("Quản trị viên"))) {
            isRoleMatch = true;
        }

        if (!isRoleMatch) {
            showAlert("Sai vai trò", "Tài khoản này có quyền '" + roleFromDB + "' nhưng bạn đang chọn '" + selectedRole + "'!");
            return;
        }

        // 3. Chuyển hướng (Fix lỗi: Switch case nhận cả tiếng Việt)
        String dashboardFXML = "";
        String title = "";

        // Chuyển roleFromDB về dạng chuẩn để switch
        if (roleFromDB.equalsIgnoreCase("Giảng viên")) roleFromDB = "Professor";
        if (roleFromDB.equalsIgnoreCase("Sinh viên")) roleFromDB = "Student";

        switch (roleFromDB) {
            case "Admin":
                dashboardFXML = "admin_dashboard.fxml";
                title = "Hệ thống Quản trị Đào tạo (Admin)";
                break;
            case "Professor":
                dashboardFXML = "professor_dashboard.fxml";
                title = "Cổng thông tin Giảng viên";
                break;
            case "Student":
                dashboardFXML = "student_dashboard.fxml";
                title = "Cổng thông tin Sinh viên";
                // Lưu ID sinh viên đăng nhập để dùng bên Dashboard
                StudentDashboardController.CURRENT_STUDENT_ID = username;
                break;
            default:
                showAlert("Lỗi", "Không xác định được giao diện cho quyền: " + roleFromDB);
                return;
        }

        if (!dashboardFXML.isEmpty()) {
            switchScene(event, dashboardFXML, title);
        }
    }

    private String getSelectedRole() {
        if (rdStudent.isSelected()) return "Sinh viên";
        if (rdProfessor.isSelected()) return "Giảng viên";
        if (rdAdmin.isSelected()) return "Admin";
        return "";
    }

    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể tải giao diện: " + fxmlFile + "\nLỗi: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}