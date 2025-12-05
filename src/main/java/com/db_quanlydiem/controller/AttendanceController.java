package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.*;
import com.db_quanlydiem.model.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AttendanceController implements Initializable {

    @FXML private Label lblClassName;
    @FXML private ComboBox<String> cbSession; // ComboBox chứa danh sách các buổi học
    @FXML private TableView<AttendanceViewModel> tableAttendance;
    @FXML private TableColumn<AttendanceViewModel, String> colSTT, colStudentID, colStudentName, colNote;
    @FXML private TableColumn<AttendanceViewModel, Boolean> colStatus;

    // Các DAO cần thiết để lấy dữ liệu liên kết
    private GradeDAO gradeDAO = new GradeDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private CourseClassDAO courseClassDAO = new CourseClassDAO(); // Lấy thông tin lớp & học kỳ
    private SemesterDAO semesterDAO = new SemesterDAO();         // Lấy ngày bắt đầu/kết thúc
    private ClassScheduleDAO scheduleDAO = new ClassScheduleDAO(); // Lấy thứ/ca học
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    private ObservableList<AttendanceViewModel> attendanceList = FXCollections.observableArrayList();
    private String courseClassID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình bảng
        colSTT.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(tableAttendance.getItems().indexOf(cell.getValue()) + 1)));
        colStudentID.setCellValueFactory(cell -> cell.getValue().studentIDProperty());
        colStudentName.setCellValueFactory(cell -> cell.getValue().studentNameProperty());

        // Cấu hình ghi chú (cho phép sửa)
        colNote.setCellValueFactory(cell -> cell.getValue().noteProperty());
        colNote.setCellFactory(TextFieldTableCell.forTableColumn());

        // Cấu hình Checkbox trạng thái
        colStatus.setCellValueFactory(cell -> cell.getValue().isPresentProperty());
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));

        tableAttendance.setEditable(true);
        tableAttendance.setItems(attendanceList);
    }

    // Hàm này được gọi từ Dashboard để truyền dữ liệu sang
    public void setClassInfo(String classID, String className) {
        this.courseClassID = classID;
        lblClassName.setText(classID + " - " + className);

        loadStudentList();   // Load danh sách SV
        loadValidSessions(); // Tính toán và đổ lịch học vào ComboBox
    }

    // --- LOGIC TÍNH TOÁN NGÀY HỌC ---
    private void loadValidSessions() {
        // 1. Lấy thông tin Lớp để biết thuộc Học kỳ nào
        List<CourseClass> allC = courseClassDAO.getAllCourseClasses();
        CourseClass currentClass = allC.stream()
                .filter(c -> c.getCourseClassId().equals(courseClassID))
                .findFirst().orElse(null);

        if (currentClass == null) return;

        // 2. Lấy thông tin Học kỳ (Start Date, End Date)
        List<Semester> allSem = semesterDAO.getAllSemesters();
        Semester semester = allSem.stream()
                .filter(s -> s.getSemesterID().equals(currentClass.getSemesterID()))
                .findFirst().orElse(null);

        if (semester == null || semester.getStartDate() == null || semester.getEndDate() == null) {
            cbSession.setPromptText("Lỗi thông tin học kỳ");
            return;
        }

        // 3. Lấy Lịch học (Thứ mấy, Ca nào, Phòng nào)
        List<ClassSchedule> schedules = scheduleDAO.getScheduleByClass(courseClassID);
        if (schedules.isEmpty()) {
            cbSession.setPromptText("Chưa xếp lịch học!");
            return;
        }

        // 4. Thuật toán: Duyệt từng ngày từ Start -> End, nếu khớp thứ -> Thêm vào list
        List<String> validDates = new ArrayList<>();
        LocalDate start = semester.getStartDate().toLocalDate();
        LocalDate end = semester.getEndDate().toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek currentDayOfWeek = date.getDayOfWeek(); // Thứ của ngày đang duyệt

            // So sánh với từng lịch trong tuần
            for (ClassSchedule sch : schedules) {
                if (isDayMatch(currentDayOfWeek, sch.getDayOfWeek())) {
                    // Tạo chuỗi hiển thị: "20/11/2025 (Thứ 2 - Ca 1)"
                    String item = String.format("%s (%s - %s)",
                            date.format(formatter), sch.getDayOfWeek(), sch.getShift());
                    validDates.add(item);
                }
            }
        }

        cbSession.setItems(FXCollections.observableArrayList(validDates));

        // Tự động chọn ngày gần nhất (Hôm nay hoặc tương lai gần)
        String todayStr = LocalDate.now().format(formatter);
        for(String s : validDates) {
            // So sánh chuỗi ngày đơn giản (để chính xác hơn cần parse lại Date)
            if(s.compareTo(todayStr) >= 0) {
                cbSession.setValue(s);
                break;
            }
        }
    }

    // Helper: So sánh Thứ trong Java (Enum) với Thứ trong DB (String)
    private boolean isDayMatch(DayOfWeek javaDay, String dbDay) {
        // dbDay ví dụ: "Thứ 2", "Thứ 3", "CN", "Chủ Nhật"
        switch (javaDay) {
            case MONDAY: return dbDay.contains("2");
            case TUESDAY: return dbDay.contains("3");
            case WEDNESDAY: return dbDay.contains("4");
            case THURSDAY: return dbDay.contains("5");
            case FRIDAY: return dbDay.contains("6");
            case SATURDAY: return dbDay.contains("7");
            case SUNDAY: return dbDay.contains("CN") || dbDay.contains("Chủ") || dbDay.contains("8");
            default: return false;
        }
    }

    private void loadStudentList() {
        if (courseClassID == null) return;
        List<Grade> grades = gradeDAO.getGradesByClass(courseClassID);
        List<Student> students = studentDAO.getAllStudents();

        attendanceList.clear();
        for (Grade g : grades) {
            String name = students.stream()
                    .filter(s -> s.getStudentID().equals(g.getStudentID()))
                    .map(Student::getStudentName)
                    .findFirst().orElse("Unknown");
            attendanceList.add(new AttendanceViewModel(g.getStudentID(), name, true, ""));
        }
    }

    private void reloadAttendanceStatus() {
        String sessionStr = cbSession.getValue();
        if (sessionStr == null || courseClassID == null) return;

        // Tách lấy ngày từ chuỗi "dd/MM/yyyy (...)"
        String datePart = sessionStr.split(" ")[0];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(datePart, formatter);

        for (AttendanceViewModel item : attendanceList) {
            // Lấy trạng thái từ DB
            int status = attendanceDAO.getAttendanceStatus(item.getStudentID(), courseClassID, date);
            String note = attendanceDAO.getAttendanceNote(item.getStudentID(), courseClassID, date);

            if (status != -1) {
                // Nếu đã có dữ liệu -> Cập nhật lên giao diện
                item.setPresent(status == 1);
                item.setNote(note);
            } else {
                // Nếu chưa có -> Mặc định là Có mặt (true) và note rỗng
                item.setPresent(true);
                item.setNote("");
            }
        }
        tableAttendance.refresh();
    }
    @FXML
    public void saveAttendance() {
        String sessionStr = cbSession.getValue();
        if (sessionStr == null || sessionStr.isEmpty()) {
            showAlert("Vui lòng chọn buổi học trước khi lưu!");
            return;
        }

        // Tách ngày để lưu vào DB
        String datePart = sessionStr.split(" ")[0];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate saveDate = LocalDate.parse(datePart, formatter);

        // --- BIẾN ĐẾM MỚI ---
        int presentCount = 0;
        int absentCount = 0;
        // --------------------

        for (AttendanceViewModel item : attendanceList) {
            boolean success = attendanceDAO.saveOrUpdateAttendance(
                    item.getStudentID(),
                    courseClassID,
                    saveDate,
                    item.isPresent(),
                    item.getNote()
            );

            // --- LOGIC ĐẾM ---
            if (success) {
                if (item.isPresent()) {
                    presentCount++;
                } else {
                    absentCount++;
                }
            }
            // -----------------
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText("Đã lưu dữ liệu điểm danh!");

        // --- NỘI DUNG THÔNG BÁO MỚI ---
        alert.setContentText("Có mặt: " + presentCount + ", Vắng: " + absentCount + " sinh viên.");
        // ------------------------------

        alert.showAndWait();

        closeWindow();
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) lblClassName.getScene().getWindow();
        stage.close();
    }
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }
    // ViewModel cho TableView
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
        public String getNote() { return note.get(); }
        public SimpleStringProperty noteProperty() { return note; }
        public void setNote(String note) { this.note.set(note); }
    }
}