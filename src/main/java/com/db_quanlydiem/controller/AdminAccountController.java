package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.AccountDAO;
import com.db_quanlydiem.model.Account;
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

public class AdminAccountController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Account> tableAccount;
    @FXML private TableColumn<Account, String> colUsername, colRole, colCreatedDate;

    // Form Inputs
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;

    private AccountDAO accountDAO = new AccountDAO();
    private ObservableList<Account> accountList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Cấu hình bảng
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

        // 2. Cấu hình ComboBox
        cbRole.setItems(FXCollections.observableArrayList("Admin", "Professor", "Student"));

        // 3. Load dữ liệu
        loadData();

        // 4. Sự kiện click dòng
        tableAccount.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        accountList.clear();
        accountList.addAll(accountDAO.getAllAccounts());
        tableAccount.setItems(accountList);
    }

    private void fillForm(Account a) {
        txtUsername.setText(a.getUsername());
        txtUsername.setDisable(true); // Không cho sửa tên đăng nhập
        txtPassword.clear(); // Không hiện password cũ vì bảo mật (hoặc đã hash)
        txtPassword.setPromptText("Nhập nếu muốn đổi pass");
        cbRole.setValue(a.getRoleName());
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if (k.isEmpty()) loadData();
        else {
            accountList.clear();
            accountList.addAll(accountDAO.searchAccount(k));
            tableAccount.setItems(accountList);
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

        // Kiểm tra trùng username
        if (accountDAO.isUsernameExists(txtUsername.getText())) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập đã tồn tại!");
            return;
        }

        Account a = new Account(
                txtUsername.getText(),
                txtPassword.getText(), // Trong thực tế nên hash password ở đây
                cbRole.getValue(),
                null // CreatedDate sẽ do DB tự tạo
        );

        if (accountDAO.addAccount(a)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo tài khoản mới!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo tài khoản.");
        }
    }

    @FXML
    public void handleUpdate() {
        Account selected = tableAccount.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Nếu trường password trống -> Giữ nguyên pass cũ (Logic này cần DAO hỗ trợ hoặc xử lý ở đây)
        // Ở đây demo đơn giản: Bắt buộc nhập pass mới hoặc pass cũ để update
        if (txtPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng nhập mật khẩu (mới hoặc cũ) để xác nhận cập nhật.");
            return;
        }

        Account a = new Account(
                txtUsername.getText(),
                txtPassword.getText(),
                cbRole.getValue(),
                null
        );

        if (accountDAO.updateAccount(a)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật tài khoản!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        Account selected = tableAccount.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Chọn tài khoản cần xóa!");
            return;
        }

        if (selected.getUsername().equals("admin")) { // Bảo vệ tài khoản admin gốc
            showAlert(Alert.AlertType.ERROR, "Cấm", "Không thể xóa tài khoản Admin gốc!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa tài khoản: " + selected.getUsername() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (accountDAO.deleteAccount(selected.getUsername())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa tài khoản.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa thất bại.");
            }
        }
    }

    @FXML
    public void handleClear() {
        txtUsername.clear(); txtUsername.setDisable(false);
        txtPassword.clear(); txtPassword.setPromptText("Nhập mật khẩu");
        cbRole.setValue(null);
        tableAccount.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty() || cbRole.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ: Username, Password và Vai trò.");
            return false;
        }
        return true;
    }

    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableAccount.getScene().getWindow();

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
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}