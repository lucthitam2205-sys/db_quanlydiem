package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.StudentDAO;
import com.db_quanlydiem.model.Student;
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

public class AdminStudentController implements Initializable {

    // --- 1. KHAI BÁO FXML ---

    @FXML private TextField txtSearch;

    @FXML private TableView<Student> tableStudent;
    @FXML private TableColumn<Student, String> colID;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colMajor;

    // Form Chi tiết - Nhóm 1: Cơ bản
    @FXML private TextField txtID;
    @FXML private TextField txtName;
    @FXML private TextField txtCohort; // MỚI: Trường nhập Khóa thủ công
    @FXML private DatePicker dpDOB;
    @FXML private ComboBox<String> cbGender;
    @FXML private TextField txtHometown;

    // Form Chi tiết - Nhóm 2: Đào tạo & Liên hệ
    @FXML private TextField txtMajor;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cbStatus;

    // Form Chi tiết - Nhóm 3: Gia đình
    @FXML private TextField txtParentName;
    @FXML private TextField txtParentPhone;

    // --- 2. DATA & DAO ---
    private StudentDAO studentDAO = new StudentDAO();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình bảng
        colID.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colMajor.setCellValueFactory(new PropertyValueFactory<>("studentMajor"));

        // Cấu hình ComboBox
        cbGender.setItems(FXCollections.observableArrayList("Nam", "Nữ", "Khác"));
        cbStatus.setItems(FXCollections.observableArrayList("Đang học", "Bảo lưu", "Đã tốt nghiệp", "Buộc thôi học"));

        // Load dữ liệu
        loadData();

        // Sự kiện chọn dòng
        tableStudent.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillForm(newSelection);
            }
        });
    }

    // --- 3. CÁC HÀM XỬ LÝ SỰ KIỆN ---

    @FXML
    public void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
        } else {
            studentList.clear();
            studentList.addAll(studentDAO.searchStudent(keyword));
            tableStudent.setItems(studentList);
        }
    }

    @FXML
    public void handleRefresh() {
        loadData();
        handleClear();
    }

    @FXML
    public void handleAdd() {
        if (!validateInput()) return;

        // Kiểm tra trùng mã
        if (studentDAO.isStudentIDExists(txtID.getText())) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã sinh viên '" + txtID.getText() + "' đã tồn tại!");
            return;
        }

        Student s = getStudentFromForm();
        if (studentDAO.addStudent(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm sinh viên mới!");
            loadData();
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm SV. Vui lòng kiểm tra lại dữ liệu.");
        }
    }

    @FXML
    public void handleUpdate() {
        Student selected = tableStudent.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sinh viên cần sửa.");
            return;
        }
        if (!validateInput()) return;

        Student s = getStudentFromForm();

        if (studentDAO.updateStudent(s)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin sinh viên!");
            loadData();
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        Student selected = tableStudent.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn sinh viên cần xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa sinh viên: " + selected.getStudentName() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (studentDAO.deleteStudent(selected.getStudentID())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa sinh viên.");
                loadData();
                handleClear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa sinh viên này (có thể do ràng buộc dữ liệu điểm).");
            }
        }
    }

    @FXML
    public void handleClear() {
        txtID.clear();
        txtID.setDisable(false); // Mở lại ô Mã SV để nhập mới
        txtName.clear();
        txtCohort.clear(); // Xóa ô Khóa
        dpDOB.setValue(null);
        cbGender.setValue(null);
        txtHometown.clear();

        txtMajor.clear();
        txtEmail.clear();
        txtPhone.clear();
        cbStatus.setValue(null);

        txtParentName.clear();
        txtParentPhone.clear();

        tableStudent.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtID.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Hệ thống Quản trị Đào tạo (Admin)");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi điều hướng", "Không thể quay về Dashboard: " + e.getMessage());
        }
    }

    // --- 4. HÀM PHỤ TRỢ ---

    private void loadData() {
        studentList.clear();
        studentList.addAll(studentDAO.getAllStudents());
        tableStudent.setItems(studentList);
    }

    private void fillForm(Student s) {
        txtID.setText(s.getStudentID());
        txtID.setDisable(true); // Không cho sửa Mã SV khi đang xem
        txtName.setText(s.getStudentName());
        txtCohort.setText(s.getStudentCohort()); // Đổ dữ liệu Khóa

        if (s.getStudentDOB() != null) dpDOB.setValue(s.getStudentDOB().toLocalDate());
        else dpDOB.setValue(null);

        cbGender.setValue(s.getStudentGender());
        txtHometown.setText(s.getStudentHometown());

        txtMajor.setText(s.getStudentMajor());
        txtEmail.setText(s.getStudentEmail());
        txtPhone.setText(s.getStudentPhone());
        cbStatus.setValue(s.getStudentStatus());

        txtParentName.setText(s.getParentName());
        txtParentPhone.setText(s.getParentPhone());
    }

    private Student getStudentFromForm() {
        Date dob = (dpDOB.getValue() != null) ? Date.valueOf(dpDOB.getValue()) : null;

        // Lấy Khóa trực tiếp từ ô nhập liệu
        String cohort = txtCohort.getText().trim();

        return new Student(
                txtID.getText(),
                txtName.getText(),
                dob,
                cbGender.getValue(),
                cohort, // Truyền khóa nhập tay
                txtMajor.getText(),
                txtEmail.getText(),
                txtPhone.getText(),
                txtHometown.getText(),
                txtParentName.getText(),
                txtParentPhone.getText(),
                cbStatus.getValue()
        );
    }

    private boolean validateInput() {
        // Thêm kiểm tra txtCohort không được rỗng
        if (txtID.getText().isEmpty() || txtName.getText().isEmpty() || txtMajor.getText().isEmpty() || txtCohort.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ít nhất: Mã SV, Họ tên, Khóa và Ngành học.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}