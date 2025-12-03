package com.db_quanlydiem.controller;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class AdminAssignStudentController implements Initializable {

    @FXML private Label lblClassInfo, lblCount;

    @FXML private TextField txtSearchAvailable;
    @FXML private TableView<Student> tableAvailable;
    @FXML private TableColumn<Student, String> colAvailID, colAvailName, colAvailMajor;

    @FXML private TextField txtSearchEnrolled;
    @FXML private TableView<Student> tableEnrolled;
    @FXML private TableColumn<Student, String> colEnrollID, colEnrollName, colEnrollMajor;

    private ObservableList<Student> availableList = FXCollections.observableArrayList();
    private ObservableList<Student> enrolledList = FXCollections.observableArrayList();

    private CourseClass currentClass;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable(tableAvailable, colAvailID, colAvailName, colAvailMajor);
        setupTable(tableEnrolled, colEnrollID, colEnrollName, colEnrollMajor);

        setupSearch(txtSearchAvailable, tableAvailable, availableList);
        setupSearch(txtSearchEnrolled, tableEnrolled, enrolledList);
    }

    private void setupTable(TableView<Student> table, TableColumn<Student, String> id, TableColumn<Student, String> name, TableColumn<Student, String> major) {
        id.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        name.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        major.setCellValueFactory(new PropertyValueFactory<>("studentMajor"));
    }

    public void setCourseClass(CourseClass courseClass) {
        this.currentClass = courseClass;
        lblClassInfo.setText("Lớp: " + courseClass.getCourseClassId() + " - " + courseClass.getClassName());
        loadData();
    }

    private void loadData() {
        if (currentClass == null) return;

        availableList.clear();
        enrolledList.clear();

        String sqlEnrolled = "SELECT s.* FROM Student s " +
                "JOIN Grade g ON s.StudentID = g.StudentID " +
                "WHERE g.CourseClassID = ?";

        String sqlAvailable = "SELECT * FROM Student WHERE StudentID NOT IN " +
                "(SELECT StudentID FROM Grade WHERE CourseClassID = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement psEnroll = conn.prepareStatement(sqlEnrolled);
            psEnroll.setString(1, currentClass.getCourseClassId());
            ResultSet rsEnroll = psEnroll.executeQuery();
            while(rsEnroll.next()) enrolledList.add(mapStudent(rsEnroll));

            PreparedStatement psAvail = conn.prepareStatement(sqlAvailable);
            psAvail.setString(1, currentClass.getCourseClassId());
            ResultSet rsAvail = psAvail.executeQuery();
            while(rsAvail.next()) availableList.add(mapStudent(rsAvail));

        } catch (Exception e) { e.printStackTrace(); }

        tableEnrolled.setItems(enrolledList);
        tableAvailable.setItems(availableList);
        updateCount();
    }

    // --- QUAN TRỌNG: Hàm này đã được sửa để khớp với Constructor 12 tham số ---
    private Student mapStudent(ResultSet rs) throws Exception {
        return new Student(
                rs.getString("StudentID"),
                rs.getString("StudentName"),
                rs.getDate("StudentDOB"),
                rs.getString("StudentGender"),
                rs.getString("StudentCohort"), // <-- Bổ sung tham số thứ 5: Khóa học
                rs.getString("StudentMajor"),
                rs.getString("StudentEmail"),
                rs.getString("StudentPhone"),
                rs.getString("StudentHometown"),
                rs.getString("ParentName"),
                rs.getString("ParentPhone"),
                rs.getString("StudentStatus")
        );
    }

    @FXML
    public void handleAddStudent() {
        Student selected = tableAvailable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn sinh viên ở bảng bên trái để thêm!");
            return;
        }

        String sql = "INSERT INTO Grade (StudentID, CourseClassID, SemesterID, GradeAssessment1, GradeAssessment2, GradeFinal, GradeAverage) VALUES (?, ?, ?, 0, 0, 0, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selected.getStudentID());
            ps.setString(2, currentClass.getCourseClassId());
            ps.setString(3, currentClass.getSemesterID());
            ps.executeUpdate();

            availableList.remove(selected);
            enrolledList.add(selected);
            updateCount();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi thêm sinh viên: " + e.getMessage());
        }
    }

    @FXML
    public void handleRemoveStudent() {
        Student selected = tableEnrolled.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn sinh viên ở bảng bên phải để xóa khỏi lớp!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa sinh viên " + selected.getStudentName() + " khỏi lớp?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        String sql = "DELETE FROM Grade WHERE StudentID = ? AND CourseClassID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selected.getStudentID());
            ps.setString(2, currentClass.getCourseClassId());
            ps.executeUpdate();

            enrolledList.remove(selected);
            availableList.add(selected);
            updateCount();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi xóa sinh viên (có thể đã có điểm): " + e.getMessage());
        }
    }

    private void updateCount() {
        lblCount.setText("(" + enrolledList.size() + "/" + currentClass.getCourseCapacity() + ")");
    }

    private void setupSearch(TextField txt, TableView<Student> table, ObservableList<Student> list) {
        FilteredList<Student> filteredData = new FilteredList<>(list, p -> true);
        txt.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return student.getStudentName().toLowerCase().contains(lowerCaseFilter)
                        || student.getStudentID().toLowerCase().contains(lowerCaseFilter);
            });
        });
        table.setItems(filteredData);
    }

    @FXML
    public void handleClose() {
        ((Stage) lblClassInfo.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }
}