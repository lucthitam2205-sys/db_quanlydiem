package com.db_quanlydiem.controller;

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
import javafx.scene.Parent; // Import thêm
import javafx.scene.Scene;  // Import thêm
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality; // Import thêm
import javafx.stage.Stage;    // Import thêm

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

    private <T> void selectComboBoxItem(ComboBox<T> cb, String id) {
        if (id == null) return;
        for (T item : cb.getItems()) {
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

    // --- HÀM MỚI: Mở cửa sổ Gán Sinh viên ---
    @FXML
    public void handleAssignStudent() {
        CourseClass selected = tableClass.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn lớp", "Vui lòng chọn lớp học phần cần gán sinh viên!");
            return;
        }

        try {
            // Load file FXML của giao diện gán sinh viên
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/db_quanlydiem/admin_assign_student.fxml"));
            Parent root = loader.load();

            // Lấy controller của giao diện mới và truyền dữ liệu lớp học sang
            AdminAssignStudentController controller = loader.getController();
            controller.setCourseClass(selected);

            // Tạo Stage mới (cửa sổ pop-up)
            Stage stage = new Stage();
            stage.setTitle("Quản lý danh sách lớp: " + selected.getCourseClassId());
            stage.setScene(new Scene(root));

            // Chặn tương tác với cửa sổ cha (Dashboard) cho đến khi đóng cửa sổ con
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không mở được cửa sổ gán sinh viên: " + e.getMessage());
        }
    }
    // ----------------------------------------

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
        Optional<ButtonType> result = alert.showAndWait(); // Sửa lỗi Optional

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/db_quanlydiem/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtClassID.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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