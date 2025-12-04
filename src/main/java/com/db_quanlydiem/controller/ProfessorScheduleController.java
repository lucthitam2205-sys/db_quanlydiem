package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.ClassScheduleDAO;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.dao.SubjectDAO;
import com.db_quanlydiem.model.ClassSchedule;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Subject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProfessorScheduleController implements Initializable {

    @FXML private TableView<ScheduleViewModel> tableSchedule;
    @FXML private TableColumn<ScheduleViewModel, String> colDate, colDay, colShift, colClassID, colSubjectName, colRoom;
    @FXML private ComboBox<Integer> cmbMonth; // Thêm ComboBox Tháng
    @FXML private ComboBox<Integer> cmbYear;  // Thêm ComboBox Năm

    private ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();

    private String professorID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo ComboBox Tháng và Năm
        cmbMonth.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));
        cmbYear.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(2023, LocalDate.now().getYear() + 2).boxed().collect(Collectors.toList())));

        // Đặt giá trị mặc định (Tùy chọn)
        cmbMonth.setValue(LocalDate.now().getMonthValue());
        cmbYear.setValue(LocalDate.now().getYear());

        // Cấu hình Cell Value Factories
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate())); // Cột mới
        colDay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDay()));
        colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift()));
        colClassID.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getClassID()));
        colSubjectName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoom()));
    }

    public void setProfessorID(String professorID) {
        this.professorID = professorID;
        // Tự động tải khi khởi động với tháng/năm mặc định
        handleLoadSchedule();
    }

    @FXML
    public void handleLoadSchedule() {
        if (professorID == null) return;

        Integer selectedMonth = cmbMonth.getValue();
        Integer selectedYear = cmbYear.getValue();

        if (selectedMonth == null || selectedYear == null) {
            showAlert("Lỗi", "Vui lòng chọn Tháng và Năm.");
            return;
        }

        loadScheduleData(selectedYear, selectedMonth);
    }

    private void loadScheduleData(int year, int month) {
        if (professorID == null) return;

        List<ClassSchedule> dailySchedules = scheduleDAO.getSchedulesByProfessor(professorID);

        // Lấy tất cả thông tin lớp học và môn học (Giả định chúng ít thay đổi)
        List<CourseClass> allClasses = courseClassDAO.getAllCourseClasses();
        List<Subject> allSubjects = subjectDAO.getAllSubjects();

        ObservableList<ScheduleViewModel> viewList = FXCollections.observableArrayList();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 1. Lặp qua từng ngày trong tháng đã chọn
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final String dayOfWeekName = convertDayOfWeekToVietnamese(date.getDayOfWeek());

            // 2. Tìm lịch học cố định cho ngày trong tuần đó
            List<ClassSchedule> daySchedules = dailySchedules.stream()
                    .filter(sch -> sch.getDayOfWeek().equals(dayOfWeekName))
                    .collect(Collectors.toList());

            for (ClassSchedule sch : daySchedules) {
                // Tìm thông tin Lớp
                CourseClass courseClass = allClasses.stream()
                        .filter(c -> c.getCourseClassId().equals(sch.getCourseClassID()))
                        .findFirst().orElse(null);

                String subName = "N/A";
                if (courseClass != null) {
                    // Tìm thông tin Môn
                    Subject subject = allSubjects.stream()
                            .filter(s -> s.getSubjectId().equals(courseClass.getSubjectId()))
                            .findFirst().orElse(null);
                    if (subject != null) subName = subject.getSubjectName();

                    // CHỈ HIỂN THỊ LỚP TRONG KỲ HỌC CỦA LỚP ĐÓ (Tối ưu hóa hơn)

                }

                viewList.add(new ScheduleViewModel(
                        date.toString(), // Ngày/Tháng/Năm
                        dayOfWeekName,   // Thứ
                        sch.getShift(),
                        sch.getCourseClassID(),
                        subName,
                        sch.getRoom()
                ));
            }
        }
        tableSchedule.setItems(viewList);
    }

    // Phương thức trợ giúp để chuyển đổi DayOfWeek sang Tiếng Việt
    private String convertDayOfWeekToVietnamese(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "Chủ Nhật";
            default: return "";
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // ViewModel
    public static class ScheduleViewModel {
        private final String date, day, shift, classID, subjectName, room;

        public ScheduleViewModel(String date, String day, String shift, String classID, String subjectName, String room) {
            this.date = date; this.day = day; this.shift = shift; this.classID = classID;
            this.subjectName = subjectName; this.room = room;
        }

        public String getDate() { return date; } // Getter mới
        public String getDay() { return day; }
        public String getShift() { return shift; }
        public String getClassID() { return classID; }
        public String getSubjectName() { return subjectName; }
        public String getRoom() { return room; }
    }
}