package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.*;
import com.db_quanlydiem.model.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AttendanceController implements Initializable {

    // --- INTERFACE GIAO TIẾP VỚI DASHBOARD (CALLBACK) ---
    public interface AttendanceUpdateListener {
        void refreshClassData(String courseClassID);
    }
    // ----------------------------------------------------

    @FXML private Label lblClassName;
    @FXML private ComboBox<String> cbSession;
    @FXML private TableView<AttendanceViewModel> tableAttendance;
    @FXML private TableColumn<AttendanceViewModel, String> colSTT, colStudentID, colStudentName, colNote;
    @FXML private TableColumn<AttendanceViewModel, Boolean> colStatus;

    // Các DAO
    private GradeDAO gradeDAO = new GradeDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private SemesterDAO semesterDAO = new SemesterDAO();
    private ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    private AttendanceUpdateListener updateListener;
    private ObservableList<AttendanceViewModel> attendanceList = FXCollections.observableArrayList();
    private String courseClassID;
    private String selectedSessionDate = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình bảng (Giữ nguyên)
        colSTT.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(tableAttendance.getItems().indexOf(cell.getValue()) + 1)));
        colStudentID.setCellValueFactory(cell -> cell.getValue().studentIDProperty());
        colStudentName.setCellValueFactory(cell -> cell.getValue().studentNameProperty());

        // Cấu hình cột Ghi chú (Cho phép sửa)
        colNote.setCellValueFactory(cell -> cell.getValue().noteProperty());
        colNote.setCellFactory(TextFieldTableCell.forTableColumn());

        // Listener cho việc sửa ghi chú (Lưu trực tiếp khi commit)
        colNote.setOnEditCommit(event -> {
            AttendanceViewModel viewModel = event.getRowValue();
            viewModel.setNote(event.getNewValue());
            saveSingleAttendanceEntry(viewModel); // Lưu DB
        });

        // Cấu hình Checkbox trạng thái (Cho phép sửa)
        colStatus.setCellValueFactory(cell -> cell.getValue().isPresentProperty());
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));

        // Listener cho Checkbox (Lưu trực tiếp khi click)
        colStatus.setOnEditCommit(event -> {
            AttendanceViewModel viewModel = event.getRowValue();
            viewModel.setPresent(event.getNewValue());

            // Tự động lưu/cập nhật DB và thông báo cho Dashboard
            saveSingleAttendanceEntry(viewModel);

            // Yêu cầu Dashboard tải lại dữ liệu (để cập nhật cột "Vắng")
            if (updateListener != null) {
                updateListener.refreshClassData(courseClassID);
            }
        });

        tableAttendance.setEditable(true);
        tableAttendance.setItems(attendanceList);

        // Listener cho ComboBox Session
        cbSession.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSessionDate = newVal.substring(0, 10);
                loadStudentList();
            }
        });
    }

    // Setter để nhận callback từ Dashboard (Giữ nguyên)
    public void setUpdateListener(AttendanceUpdateListener listener) {
        this.updateListener = listener;
    }

    // Hàm setClassInfo (Giữ nguyên)
    public void setClassInfo(String classID, String className) {
        this.courseClassID = classID;
        lblClassName.setText(classID + " - " + className);
        loadValidSessions();
    }

    // --- LOGIC TÍNH TOÁN NGÀY HỌC (Giữ nguyên) ---
    private void loadValidSessions() {
        List<CourseClass> allC = courseClassDAO.getAllCourseClasses();
        CourseClass currentClass = allC.stream()
                .filter(c -> c.getCourseClassId().equals(courseClassID))
                .findFirst().orElse(null);

        if (currentClass == null) return;

        List<Semester> allSem = semesterDAO.getAllSemesters();
        Semester semester = allSem.stream()
                .filter(s -> s.getSemesterID().equals(currentClass.getSemesterID()))
                .findFirst().orElse(null);

        if (semester == null || semester.getStartDate() == null || semester.getEndDate() == null) {
            cbSession.setPromptText("Lỗi thông tin học kỳ");
            return;
        }

        List<ClassSchedule> schedules = scheduleDAO.getScheduleByClass(courseClassID);
        if (schedules.isEmpty()) {
            cbSession.setPromptText("Chưa xếp lịch học!");
            return;
        }

        List<String> validDates = new ArrayList<>();
        LocalDate start = semester.getStartDate().toLocalDate();
        LocalDate end = semester.getEndDate().toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek currentDayOfWeek = date.getDayOfWeek();

            for (ClassSchedule sch : schedules) {
                if (isDayMatch(currentDayOfWeek, sch.getDayOfWeek())) {
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
            if(s.substring(0, 10).compareTo(todayStr) >= 0) {
                cbSession.setValue(s);
                selectedSessionDate = s.substring(0, 10);
                break;
            }
        }

        if(selectedSessionDate != null) {
            loadStudentList();
        }
    }

    // Helper: So sánh Thứ (Giữ nguyên)
    private boolean isDayMatch(DayOfWeek javaDay, String dbDay) {
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

    // --- LOGIC TẢI DANH SÁCH SINH VIÊN VÀ TRẠNG THÁI ĐIỂM DANH (Giữ nguyên) ---
    private void loadStudentList() {
        if (courseClassID == null || selectedSessionDate == null) return;

        List<Grade> grades = gradeDAO.getGradesByClass(courseClassID);
        List<Student> students = studentDAO.getAllStudents();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate attendanceDate = LocalDate.parse(selectedSessionDate, formatter);
        Date sqlDate = Date.valueOf(attendanceDate);

        List<Attendance> savedAttendance = attendanceDAO.getAttendanceByClassAndDate(courseClassID, sqlDate);

        attendanceList.clear();
        for (Grade g : grades) {
            String studentID = g.getStudentID();

            String name = students.stream()
                    .filter(s -> s.getStudentID().equals(studentID))
                    .map(Student::getStudentName)
                    .findFirst().orElse("Unknown");

            Optional<Attendance> savedEntry = savedAttendance.stream()
                    .filter(a -> a.getStudentID().equals(studentID))
                    .findFirst();

            boolean isPresent = savedEntry.map(Attendance::isPresent).orElse(true);
            String note = savedEntry.map(Attendance::getNote).orElse("");
            // Lấy ID nếu tồn tại
            int attendanceID = savedEntry.map(Attendance::getAttendanceID).orElse(0);

            attendanceList.add(new AttendanceViewModel(attendanceID, studentID, name, isPresent, note));
        }
    }

    // --- LOGIC LƯU VÀ CẬP NHẬT TỪ DB (ĐÃ SỬA LỖI CỐ ĐỊNH ID) ---
    private void saveSingleAttendanceEntry(AttendanceViewModel a) {
        if (selectedSessionDate == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(selectedSessionDate, formatter);
        Date sqlDate = Date.valueOf(date);

        Attendance attendance = new Attendance(
                a.getAttendanceID(),
                a.getStudentID(),
                courseClassID,
                sqlDate,
                a.isPresent(),
                a.getNote()
        );

        // 1. Nếu đã có AttendanceID (bản ghi đã tồn tại) -> Update
        if (a.getAttendanceID() > 0) {
            attendanceDAO.updateAttendance(attendance);
        } else {
            // 2. Bản ghi chưa có -> Add mới, và cần CẬP NHẬT ID MỚI (Khắc phục lỗi UNIQUE)
            // GIẢ ĐỊNH: AttendanceDAO có phương thức trả về ID sau khi INSERT
            int newId = attendanceDAO.addAttendanceAndGetId(attendance);

            if (newId > 0) {
                // CẬP NHẬT ID MỚI VÀO VIEWMODEL
                a.setAttendanceID(newId);
            }
        }
    }

    @FXML
    public void saveAttendance() {
        String session = cbSession.getValue();
        if (session == null || session.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn buổi");
            alert.setContentText("Vui lòng chọn buổi học trong danh sách để điểm danh!");
            alert.showAndWait();
            return;
        }

        // Chức năng saveAttendance tổng thể: Duyệt qua tất cả và lưu
        // Mặc dù Listener đã tự động lưu, ta vẫn cần hàm này cho nút "Lưu" (nếu có)

        boolean allSuccess = true;
        for (AttendanceViewModel a : attendanceList) {
            // Cố gắng lưu từng entry (sẽ gọi logic update/add bên trong)
            saveSingleAttendanceEntry(a);
        }

        if (updateListener != null) {
            updateListener.refreshClassData(courseClassID);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lưu thành công");
        alert.setHeaderText("Đã hoàn thành lưu/cập nhật dữ liệu điểm danh.");
        alert.showAndWait();

        closeWindow();
    }

    @FXML
    public void closeWindow() {
        if (updateListener != null) {
            updateListener.refreshClassData(courseClassID);
        }
        Stage stage = (Stage) lblClassName.getScene().getWindow();
        stage.close();
    }

    // ViewModel cho TableView
    public static class AttendanceViewModel {
        private final SimpleStringProperty studentID;
        private final SimpleStringProperty studentName;
        private final SimpleBooleanProperty isPresent;
        private final SimpleStringProperty note;
        private int attendanceID; // ĐÃ SỬA: Không phải final để cho phép set ID mới

        public AttendanceViewModel(int attendanceID, String id, String name, boolean present, String note) {
            this.attendanceID = attendanceID;
            this.studentID = new SimpleStringProperty(id);
            this.studentName = new SimpleStringProperty(name);
            this.isPresent = new SimpleBooleanProperty(present);
            this.note = new SimpleStringProperty(note);
        }

        // Getter/Setter MỚI cho ID
        public int getAttendanceID() { return attendanceID; }
        public void setAttendanceID(int attendanceID) { this.attendanceID = attendanceID; }

        // Giữ nguyên các getters/setters cũ
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