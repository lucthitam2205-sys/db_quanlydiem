package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.GradeDAO;
import com.db_quanlydiem.dao.StudentDAO;
import com.db_quanlydiem.model.Grade;
import com.db_quanlydiem.model.Student;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AttendanceController implements Initializable {

    @FXML private Label lblClassName, lblDate;
    @FXML private TableView<AttendanceViewModel> tableAttendance;
    @FXML private TableColumn<AttendanceViewModel, String> colSTT, colStudentID, colStudentName, colNote;
    @FXML private TableColumn<AttendanceViewModel, Boolean> colStatus;

    private GradeDAO gradeDAO = new GradeDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private ObservableList<AttendanceViewModel> attendanceList = FXCollections.observableArrayList();
    private String courseClassID;

    // Hàm nhận dữ liệu từ Dashboard truyền sang
    public void setClassInfo(String classID, String className) {
        this.courseClassID = classID;
        lblClassName.setText(classID + " - " + className);
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        loadStudentList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình cột
        colSTT.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(tableAttendance.getItems().indexOf(cell.getValue()) + 1)));
        colStudentID.setCellValueFactory(cell -> cell.getValue().studentIDProperty());
        colStudentName.setCellValueFactory(cell -> cell.getValue().studentNameProperty());
        colNote.setCellValueFactory(cell -> cell.getValue().noteProperty());

        // Cấu hình Checkbox
        colStatus.setCellValueFactory(cell -> cell.getValue().isPresentProperty());
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));
        tableAttendance.setEditable(true);

        tableAttendance.setItems(attendanceList);
    }

    private void loadStudentList() {
        if (courseClassID == null) return;

        // Lấy danh sách SV trong lớp từ bảng Grade (vì Grade lưu danh sách đăng ký)
        List<Grade> grades = gradeDAO.getGradesByClass(courseClassID);
        List<Student> students = studentDAO.getAllStudents(); // Nên tối ưu query này trong thực tế

        attendanceList.clear();
        for (Grade g : grades) {
            String name = students.stream()
                    .filter(s -> s.getStudentID().equals(g.getStudentID()))
                    .map(Student::getStudentName)
                    .findFirst().orElse("Unknown");

            // Mặc định là có mặt (true)
            attendanceList.add(new AttendanceViewModel(g.getStudentID(), name, true, ""));
        }
    }

    @FXML
    public void saveAttendance() {
        // Trong thực tế: Lưu vào bảng Attendance trong DB
        // Ở đây giả lập: In ra console và thông báo
        int presentCount = 0;
        for (AttendanceViewModel a : attendanceList) {
            if (a.isPresent()) presentCount++;
            // System.out.println("SV: " + a.getStudentID() + " - Có mặt: " + a.isPresent());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lưu thành công");
        alert.setHeaderText(null);
        alert.setContentText("Đã lưu điểm danh!\nSĩ số: " + attendanceList.size() + "\nCó mặt: " + presentCount);
        alert.showAndWait();

        closeWindow();
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) lblClassName.getScene().getWindow();
        stage.close();
    }

    // ViewModel cho bảng điểm danh
    public static class AttendanceViewModel {
        private final SimpleStringProperty studentID;
        private final SimpleStringProperty studentName;
        private final SimpleBooleanProperty isPresent;
        private final SimpleStringProperty note;

        public AttendanceViewModel(String id, String name, boolean present, String note) {
            this.studentID = new SimpleStringProperty(id);
            this.studentName = new SimpleStringProperty(name);
            this.isPresent = new SimpleBooleanProperty(present);
            this.note = new SimpleStringProperty(note);
        }

        public String getStudentID() { return studentID.get(); }
        public SimpleStringProperty studentIDProperty() { return studentID; }
        public String getStudentName() { return studentName.get(); }
        public SimpleStringProperty studentNameProperty() { return studentName; }
        public boolean isPresent() { return isPresent.get(); }
        public SimpleBooleanProperty isPresentProperty() { return isPresent; }
        public void setPresent(boolean present) { this.isPresent.set(present); }
        public SimpleStringProperty noteProperty() { return note; }
    }
}