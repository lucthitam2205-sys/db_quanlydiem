package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.dao.ProfessorDAO;
import com.db_quanlydiem.dao.SemesterDAO;
import com.db_quanlydiem.dao.SubjectDAO;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Professor;
import com.db_quanlydiem.model.Semester;
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

public class AdminCourseClassController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<CourseClass> tableClass;
    @FXML private TableColumn<CourseClass, String> colClassID, colSubjectID, colClassName, colSemester, colProfessor;
    @FXML private TableColumn<CourseClass, Integer> colCredits, colCapacity;

    // Form inputs
    @FXML private TextField txtClassID, txtClassName, txtCredits;
    @FXML private ComboBox<Subject> cbSubject;
    @FXML private ComboBox<Semester> cbSemester;
    @FXML private ComboBox<Professor> cbProfessor;
    @FXML private Spinner<Integer> spCapacity;

    // DAOs
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();
    private SemesterDAO semesterDAO = new SemesterDAO();
    private ProfessorDAO professorDAO = new ProfessorDAO();

    private ObservableList<CourseClass> classList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Setup Table Columns
        colClassID.setCellValueFactory(new PropertyValueFactory<>("courseClassId"));
        colSubjectID.setCellValueFactory(new PropertyValueFactory<>("subjectId"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colCredits.setCellValueFactory(new PropertyValueFactory<>("subjectCredits"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semesterID"));
        colProfessor.setCellValueFactory(new PropertyValueFactory<>("professorID"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("courseCapacity"));

        // 2. Setup Spinner
        spCapacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 60));

        // 3. Load Data for ComboBoxes
        loadComboBoxData();

        // 4. Load Table Data
        loadData();

        // 5. Selection Listener
        tableClass.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadComboBoxData() {
        cbSubject.setItems(FXCollections.observableArrayList(subjectDAO.getAllSubjects()));
        cbSemester.setItems(FXCollections.observableArrayList(semesterDAO.getAllSemesters()));
        cbProfessor.setItems(FXCollections.observableArrayList(professorDAO.getAllProfessors()));
    }

    private void loadData() {
        classList.clear();
        classList.addAll(courseClassDAO.getAllCourseClasses());
        tableClass.setItems(classList);
    }

    private void fillForm(CourseClass c) {
        txtClassID.setText(c.getCourseClassId());
        txtClassID.setDisable(true);
        txtClassName.setText(c.getClassName());
        txtCredits.setText(String.valueOf(c.getSubjectCredits()));
        spCapacity.getValueFactory().setValue(c.getCourseCapacity());

        // Select items in ComboBox based on ID
        selectComboBoxItem(cbSubject, c.getSubjectId());
        selectComboBoxItem(cbSemester, c.getSemesterID());
        selectComboBoxItem(cbProfessor, c.getProfessorID());
    }

    // Helper to select item in ComboBox by ID (Using toString override or ID matching)
    // Note: This simple version assumes toString() or simple iteration matches.
    // In production, better to override equals() in Models or search by ID.
    private <T> void selectComboBoxItem(ComboBox<T> cb, String id) {
        if (id == null) return;
        for (T item : cb.getItems()) {
            // Đây là cách so sánh tạm thời, tốt nhất là Models nên có method getId() chung interface
            if (item instanceof Subject && ((Subject) item).getSubjectId().equals(id)) {
                cb.getSelectionModel().select(item); return;
            }
            if (item instanceof Semester && ((Semester) item).getSemesterID().equals(id)) {
                cb.getSelectionModel().select(item); return;
            }
            if (item instanceof Professor && ((Professor) item).getProfessorID().equals(id)) {
                cb.getSelectionModel().select(item); return;
            }
        }
    }

    @FXML
    public void handleSubjectSelection() {
        Subject selected = cbSubject.getValue();
        if (selected != null) {
            txtCredits.setText(String.valueOf(selected.getSubjectCredit()));
            // Auto-generate Class ID suggestion? (Optional)
        }
    }

    @FXML
    public void handleSearch() {
        String k = txtSearch.getText().trim();
        if (k.isEmpty()) loadData();
        else {
            classList.clear();
            classList.addAll(courseClassDAO.searchCourseClass(k));
            tableClass.setItems(classList);
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

        CourseClass cc = new CourseClass(
                txtClassID.getText(),
                cbSubject.getValue().getSubjectId(),
                txtClassName.getText(),
                Integer.parseInt(txtCredits.getText()),
                cbSemester.getValue().getSemesterID(),
                cbProfessor.getValue() != null ? cbProfessor.getValue().getProfessorID() : null,
                spCapacity.getValue()
        );

        if (courseClassDAO.addCourseClass(cc)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã mở lớp học phần mới!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã lớp có thể đã tồn tại.");
        }
    }

    @FXML
    public void handleUpdate() {
        if (tableClass.getSelectionModel().getSelectedItem() == null) return;
        if (!validateForm()) return;

        CourseClass cc = new CourseClass(
                txtClassID.getText(),
                cbSubject.getValue().getSubjectId(),
                txtClassName.getText(),
                Integer.parseInt(txtCredits.getText()),
                cbSemester.getValue().getSemesterID(),
                cbProfessor.getValue() != null ? cbProfessor.getValue().getProfessorID() : null,
                spCapacity.getValue()
        );

        if (courseClassDAO.updateCourseClass(cc)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin lớp!");
            handleRefresh();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại.");
        }
    }

    @FXML
    public void handleDelete() {
        CourseClass selected = tableClass.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Chọn lớp cần xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa lớp: " + selected.getCourseClassId() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (courseClassDAO.deleteCourseClass(selected.getCourseClassId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa lớp học phần.");
                handleRefresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa (Lớp đã có sinh viên đăng ký).");
            }
        }
    }

    @FXML
    public void handleClear() {
        txtClassID.clear(); txtClassID.setDisable(false);
        txtClassName.clear(); txtCredits.clear();
        cbSubject.setValue(null);
        cbSemester.setValue(null);
        cbProfessor.setValue(null);
        spCapacity.getValueFactory().setValue(60);
        tableClass.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBack() {
        try {
            // Load lại trang Admin Dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("admin_dashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại từ một node bất kỳ (ví dụ bảng)
            Stage stage = (Stage) tableClass.getScene().getWindow();

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
        if (txtClassID.getText().isEmpty() || cbSubject.getValue() == null || cbSemester.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Mã lớp, chọn Môn và Học kỳ.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}