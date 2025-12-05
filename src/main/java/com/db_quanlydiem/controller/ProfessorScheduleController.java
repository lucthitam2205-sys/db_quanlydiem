package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.ClassScheduleDAO;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.dao.SemesterDAO;
import com.db_quanlydiem.dao.SubjectDAO;
import com.db_quanlydiem.model.ClassSchedule;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Semester;
import com.db_quanlydiem.model.Subject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox; // Import ComboBox
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ProfessorScheduleController implements Initializable {

    @FXML private TableView<ScheduleViewModel> tableSchedule;
    @FXML private TableColumn<ScheduleViewModel, String> colDate, colDay, colShift, colClassID, colSubjectName, colRoom;

    // --- THÊM KHAI BÁO COMBOBOX ---
    @FXML private ComboBox<Integer> cbMonth;
    @FXML private ComboBox<Integer> cbYear;

    private ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();
    private SemesterDAO semesterDAO = new SemesterDAO();

    private String professorID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        colDay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDay()));
        colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift()));
        colClassID.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getClassID()));
        colSubjectName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoom()));

        // --- KHỞI TẠO COMBOBOX THÁNG & NĂM ---
        ObservableList<Integer> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) months.add(i);
        cbMonth.setItems(months);

        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        years.addAll(currentYear - 1, currentYear, currentYear + 1);
        cbYear.setItems(years);

        // Mặc định chọn tháng năm hiện tại
        cbMonth.setValue(LocalDate.now().getMonthValue());
        cbYear.setValue(currentYear);
    }

    public void setProfessorID(String professorID) {
        this.professorID = professorID;
        // Load lịch tháng hiện tại mặc định
        handleFilter();
    }

    // --- HÀM XỬ LÝ LỌC LỊCH (GỌI KHI BẤM NÚT "XEM LỊCH") ---
    @FXML
    public void handleFilter() {
        Integer selectedMonth = cbMonth.getValue();
        Integer selectedYear = cbYear.getValue();

        if (selectedMonth == null || selectedYear == null) return;

        loadDetailedSchedule(selectedMonth, selectedYear);
    }

    @FXML
    public void handleViewAll() {
        // Load tất cả (truyền null để biết là không lọc)
        loadDetailedSchedule(null, null);
    }

    private void loadDetailedSchedule(Integer filterMonth, Integer filterYear) {
        if (professorID == null) return;

        List<ClassSchedule> schedules = scheduleDAO.getSchedulesByProfessor(professorID);
        List<CourseClass> allClasses = courseClassDAO.getAllCourseClasses();
        List<Subject> allSubjects = subjectDAO.getAllSubjects();
        List<Semester> allSemesters = semesterDAO.getAllSemesters();

        ObservableList<ScheduleViewModel> viewList = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ClassSchedule sch : schedules) {
            CourseClass courseClass = allClasses.stream()
                    .filter(c -> c.getCourseClassId().equals(sch.getCourseClassID()))
                    .findFirst().orElse(null);

            if (courseClass == null) continue;

            String subName = "N/A";
            Subject subject = allSubjects.stream()
                    .filter(s -> s.getSubjectId().equals(courseClass.getSubjectId()))
                    .findFirst().orElse(null);
            if (subject != null) subName = subject.getSubjectName();

            Semester semester = allSemesters.stream()
                    .filter(s -> s.getSemesterID().equals(courseClass.getSemesterID()))
                    .findFirst().orElse(null);

            if (semester != null && semester.getStartDate() != null && semester.getEndDate() != null) {
                LocalDate start = semester.getStartDate().toLocalDate();
                LocalDate end = semester.getEndDate().toLocalDate();

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    // 1. Kiểm tra Thứ
                    if (!isDayMatch(date.getDayOfWeek(), sch.getDayOfWeek())) {
                        continue;
                    }

                    // 2. Kiểm tra Tháng/Năm (Nếu có yêu cầu lọc)
                    if (filterMonth != null && filterYear != null) {
                        if (date.getMonthValue() != filterMonth || date.getYear() != filterYear) {
                            continue; // Bỏ qua nếu không đúng tháng/năm chọn
                        }
                    }

                    viewList.add(new ScheduleViewModel(
                            date.format(formatter),
                            sch.getDayOfWeek(),
                            sch.getShift(),
                            sch.getCourseClassID(),
                            subName,
                            sch.getRoom()
                    ));
                }
            }
        }

        viewList.sort(Comparator.comparing(ScheduleViewModel::getDate));
        tableSchedule.setItems(viewList);
    }

    private boolean isDayMatch(DayOfWeek javaDay, String dbDay) {
        switch (javaDay) {
            case MONDAY: return dbDay.contains("2");
            case TUESDAY: return dbDay.contains("3");
            case WEDNESDAY: return dbDay.contains("4");
            case THURSDAY: return dbDay.contains("5");
            case FRIDAY: return dbDay.contains("6");
            case SATURDAY: return dbDay.contains("7");
            case SUNDAY: return dbDay.contains("CN") || dbDay.contains("Chủ");
            default: return false;
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public static class ScheduleViewModel {
        private final String date, day, shift, classID, subjectName, room;
        public ScheduleViewModel(String date, String day, String shift, String classID, String subjectName, String room) {
            this.date = date; this.day = day; this.shift = shift;
            this.classID = classID; this.subjectName = subjectName; this.room = room;
        }
        public String getDate() { return date; }
        public String getDay() { return day; }
        public String getShift() { return shift; }
        public String getClassID() { return classID; }
        public String getSubjectName() { return subjectName; }
        public String getRoom() { return room; }
    }
}