package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.AttendanceDAO;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.dao.GradeDAO;
import com.db_quanlydiem.dao.ProfessorDAO;
import com.db_quanlydiem.dao.StudentDAO;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Grade;
import com.db_quanlydiem.model.Professor;
import com.db_quanlydiem.model.Student;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // BỔ SUNG
import javafx.collections.transformation.SortedList;   // BỔ SUNG
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProfessorDashboardController implements Initializable, AttendanceController.AttendanceUpdateListener {

    // --- 1. KHAI BÁO FXML ---
    @FXML private Label lblProfName, lblProfID, lblTitle, lblPhone, lblEmail, lblHometown;
    @FXML private ComboBox<CourseClass> cbClass;
    @FXML private TextField txtSearchStudent; // Textfield tìm kiếm

    // TableView và Columns (SỬ DỤNG GradeViewModel)
    @FXML private TableView<GradeViewModel> tableGrades;
    @FXML private TableColumn<GradeViewModel, String> colSTT, colStudentID, colStudentName, colNote;
    @FXML private TableColumn<GradeViewModel, Double> colScore1, colScore2, colScoreFinal, colScoreAvg;
    @FXML private TableColumn<GradeViewModel, Integer> colAbsentCount;

    // Thống kê
    @FXML private Label lblTotalStudents, lblRateExcellent, lblRateGood, lblRateFair, lblRateAverage;

    // --- 2. DAO & DATA ---
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private GradeDAO gradeDAO = new GradeDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private ProfessorDAO professorDAO = new ProfessorDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    // SỬA ĐỔI: masterGradeList là danh sách gốc, filteredGradeList là danh sách được hiển thị
    private ObservableList<GradeViewModel> masterGradeList = FXCollections.observableArrayList();
    private FilteredList<GradeViewModel> filteredGradeList; // Danh sách đã lọc
    private List<Student> allStudents;

    private String currentProfessorID;

    public void setProfessorID(String professorID) {
        this.currentProfessorID = professorID;
        loadProfessorInfo();
        loadClasses();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allStudents = studentDAO.getAllStudents();

        // Khởi tạo FilteredList/SortedList ngay
        filteredGradeList = new FilteredList<>(masterGradeList, p -> true);
        SortedList<GradeViewModel> sortedData = new SortedList<>(filteredGradeList);
        sortedData.comparatorProperty().bind(tableGrades.comparatorProperty());
        tableGrades.setItems(sortedData);

        loadProfessorInfo();
        loadClasses();
        setupTable();
        cbClass.setOnAction(e -> loadGrades());

        // BƯỚC MỚI: Thêm listener cho ô tìm kiếm
        txtSearchStudent.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStudentList(newValue);
        });
    }

    // --- LOGIC CƠ BẢN ---

    private void loadProfessorInfo() {
        if (this.currentProfessorID == null) return;
        List<Professor> professors = professorDAO.getAllProfessors();
        Professor currentProf = professors.stream()
                .filter(p -> p.getProfessorID().equals(this.currentProfessorID))
                .findFirst()
                .orElse(null);

        if (currentProf != null) {
            lblProfName.setText(currentProf.getProfessorName());
            lblProfID.setText("Mã GV: " + currentProf.getProfessorID());
            lblTitle.setText(currentProf.getProfessorTitle());
            lblPhone.setText(currentProf.getProfessorPhone());
            lblEmail.setText(currentProf.getProfessorEmail());

            if (lblHometown != null) {
                lblHometown.setText(currentProf.getProfessorHometown());
            }
        }
    }

    private void loadClasses() {
        if (this.currentProfessorID == null) return;
        List<CourseClass> allClasses = courseClassDAO.getAllCourseClasses();
        List<CourseClass> myClasses = allClasses.stream()
                .filter(c -> this.currentProfessorID.equals(c.getProfessorID()))
                .collect(Collectors.toList());

        cbClass.setItems(FXCollections.observableArrayList(myClasses));
    }

    private void setupTable() { /* Giữ nguyên */
        tableGrades.setEditable(true);

        colStudentID.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        colStudentName.setCellValueFactory(cellData -> {
            String sID = cellData.getValue().getStudentID();
            String name = allStudents.stream()
                    .filter(s -> s.getStudentID().equals(sID))
                    .map(Student::getStudentName)
                    .findFirst().orElse("Unknown");
            return new SimpleStringProperty(name);
        });

        colAbsentCount.setCellValueFactory(new PropertyValueFactory<>("absentCount"));
        colAbsentCount.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        setupEditableColumn(colScore1, "gradeAssessment1");
        setupEditableColumn(colScore2, "gradeAssessment2");
        setupEditableColumn(colScoreFinal, "gradeFinal");

        colScoreAvg.setCellValueFactory(new PropertyValueFactory<>("gradeAverage"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("gradeNote"));
        colNote.setCellFactory(TextFieldTableCell.forTableColumn());
        colNote.setOnEditCommit(e -> e.getRowValue().setGradeNote(e.getNewValue()));

        colSTT.setCellFactory(column -> new TableCell<GradeViewModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else setText(String.valueOf(getIndex() + 1));
            }
        });
    }

    private void setupEditableColumn(TableColumn<GradeViewModel, Double> col, String property) {
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        col.setOnEditCommit(event -> {
            GradeViewModel g = event.getRowValue();
            Double newVal = event.getNewValue();

            if (newVal == null) newVal = 0.0;
            if (newVal < 0 || newVal > 10) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Điểm phải nằm trong khoảng 0.0 - 10.0.");
                tableGrades.refresh();
                return;
            }

            if (property.equals("gradeAssessment1")) g.setGradeAssessment1(newVal);
            else if (property.equals("gradeAssessment2")) g.setGradeAssessment2(newVal);
            else if (property.equals("gradeFinal")) g.setGradeFinal(newVal);

            double avg = (g.getGradeAssessment1() * 0.3) + (g.getGradeAssessment2() * 0.2) + (g.getGradeFinal() * 0.5);
            g.setGradeAverage(Math.round(avg * 100.0) / 100.0);

            tableGrades.refresh();
            updateStatistics();
        });
    }

    private void loadGrades() {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null || this.currentProfessorID == null) return;

        masterGradeList.clear(); // Xóa danh sách MASTER
        List<Grade> rawGrades = gradeDAO.getGradesByClass(selectedClass.getCourseClassId());

        // Lấy số buổi vắng
        Map<String, Integer> absentCounts = attendanceDAO.getAbsentCountsByClass(selectedClass.getCourseClassId());

        // Chuyển đổi Grade thành GradeViewModel và gán số buổi vắng
        for (Grade g : rawGrades) {
            double s1 = g.getGradeAssessment1();
            double s2 = g.getGradeAssessment2();
            double sf = g.getGradeFinal();
            double avg = (s1 * 0.3) + (s2 * 0.2) + (sf * 0.5);
            g.setGradeAverage(Math.round(avg * 100.0) / 100.0);

            int absentCount = absentCounts.getOrDefault(g.getStudentID(), 0);

            masterGradeList.add(new GradeViewModel(g, absentCount));
        }

        // Gắn danh sách gốc vào FilteredList
        filteredGradeList.setPredicate(p -> true);

        // Áp dụng lại bộ lọc nếu đang có từ khóa
        filterStudentList(txtSearchStudent.getText());
    }

    /**
     * Lọc danh sách sinh viên theo từ khóa tìm kiếm (Mã SV hoặc Tên SV).
     * @param keyword Từ khóa tìm kiếm.
     */
    private void filterStudentList(String keyword) {
        String lowerCaseFilter = keyword.toLowerCase();

        filteredGradeList.setPredicate(gradeViewModel -> {
            if (keyword == null || keyword.isEmpty()) {
                return true; // Hiển thị tất cả nếu không có từ khóa
            }

            // 1. Lấy tên SV từ ViewModel
            String studentID = gradeViewModel.getStudentID();
            String studentName = allStudents.stream()
                    .filter(s -> s.getStudentID().equals(studentID))
                    .map(Student::getStudentName)
                    .findFirst().orElse("");

            // 2. Kiểm tra khớp mã SV hoặc Tên SV
            if (studentID.toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (studentName.toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        });

        // Cập nhật lại thống kê sau khi lọc
        updateStatistics();
    }


    private void updateStatistics() {
        // Sử dụng items của TableView (đã lọc)
        ObservableList<GradeViewModel> currentList = tableGrades.getItems();

        if (currentList == null || currentList.isEmpty()) {
            lblTotalStudents.setText("0");
            lblRateExcellent.setText("0%");
            lblRateGood.setText("0%");
            lblRateFair.setText("0%");
            lblRateAverage.setText("0%");
            return;
        }

        int total = currentList.size();
        long excellent = currentList.stream().filter(g -> g.getGradeAverage() >= 8.5).count();
        long good = currentList.stream().filter(g -> g.getGradeAverage() >= 7.0 && g.getGradeAverage() < 8.5).count();
        long fair = currentList.stream().filter(g -> g.getGradeAverage() >= 5.5 && g.getGradeAverage() < 7.0).count();
        long average = currentList.stream().filter(g -> g.getGradeAverage() >= 4.0 && g.getGradeAverage() < 5.5).count();

        lblTotalStudents.setText(String.valueOf(total));
        lblRateExcellent.setText(String.format("%.1f%%", (double)excellent/total * 100));
        lblRateGood.setText(String.format("%.1f%%", (double)good/total * 100));
        lblRateFair.setText(String.format("%.1f%%", (double)fair/total * 100));
        lblRateAverage.setText(String.format("%.1f%%", (double)average/total * 100));
    }

    // HÀM CALLBACK KHI DỮ LIỆU ĐIỂM DANH THAY ĐỔI
    @Override
    public void refreshClassData(String courseClassID) {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass != null && selectedClass.getCourseClassId().equals(courseClassID)) {
            loadGrades();
        }
    }


    // --- CÁC HÀM SỰ KIỆN (BUTTON ACTIONS) ---

    @FXML
    public void handleViewSchedule(ActionEvent event) {
        if (this.currentProfessorID == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("professor_schedule.fxml"));
            Parent root = loader.load();

            ProfessorScheduleController controller = loader.getController();
            controller.setProfessorID(this.currentProfessorID);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Lịch giảng dạy cá nhân");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở lịch giảng dạy: " + e.getMessage());
        }
    }

    @FXML
    public void handleAttendance(ActionEvent event) {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn lớp", "Vui lòng chọn lớp học phần để điểm danh!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("attendance_check.fxml"));
            Parent root = loader.load();

            AttendanceController controller = loader.getController();
            controller.setClassInfo(selectedClass.getCourseClassId(), selectedClass.getClassName());
            controller.setUpdateListener(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Điểm danh - " + selectedClass.getCourseClassId());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ điểm danh: " + e.getMessage());
        }
    }

    @FXML
    public void handleExportList(ActionEvent event) {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null || masterGradeList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Vui lòng chọn lớp và đảm bảo có dữ liệu để xuất.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu danh sách lớp");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Word Document", "*.doc"));
        fileChooser.setInitialFileName("DanhSach_" + selectedClass.getCourseClassId() + ".doc");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            exportToWord(file, selectedClass);
        }
    }

    private void exportToWord(File file, CourseClass selectedClass) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("<html><head><meta charset='UTF-8'>");
            writer.println("<style>body { font-family: 'Times New Roman'; } table { width: 100%; border-collapse: collapse; } th, td { border: 1px solid black; padding: 5px; text-align: center; } th { background-color: #f2f2f2; }</style></head><body>");

            writer.println("<h2 style='text-align: center;'>BẢNG ĐIỂM CHI TIẾT</h2>");
            writer.println("<p><b>Lớp học phần:</b> " + selectedClass.getCourseClassId() + " - " + selectedClass.getClassName() + "</p>");
            writer.println("<p><b>Giảng viên:</b> " + lblProfName.getText() + "</p>");

            writer.println("<table>");
            writer.println("<tr><th>STT</th><th>Mã SV</th><th>Họ và Tên</th><th>ĐG 1</th><th>ĐG 2</th><th>Cuối kỳ</th><th>Tổng kết</th><th>Vắng</th><th>Ghi chú</th></tr>");

            int stt = 1;
            // Lặp qua danh sách GỐC để đảm bảo xuất đầy đủ (masterGradeList)
            for (GradeViewModel gv : masterGradeList) {
                String name = allStudents.stream()
                        .filter(s -> s.getStudentID().equals(gv.getStudentID()))
                        .map(Student::getStudentName)
                        .findFirst().orElse("");

                writer.println("<tr>");
                writer.println("<td>" + (stt++) + "</td>");
                writer.println("<td>" + gv.getStudentID() + "</td>");
                writer.println("<td style='text-align: left;'>" + name + "</td>");
                writer.println("<td>" + gv.getGradeAssessment1() + "</td>");
                writer.println("<td>" + gv.getGradeAssessment2() + "</td>");
                writer.println("<td>" + gv.getGradeFinal() + "</td>");
                writer.println("<td><b>" + gv.getGradeAverage() + "</b></td>");
                writer.println("<td>" + gv.getAbsentCount() + "</td>");
                writer.println("<td>" + (gv.getGradeNote() == null ? "" : gv.getGradeNote()) + "</td>");
                writer.println("</tr>");
            }
            writer.println("</table>");
            writer.println("</body></html>");

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất file thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu file: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveGrades() {
        if (masterGradeList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Trống", "Không có dữ liệu để lưu.");
            return;
        }

        boolean allSuccess = true;
        for (GradeViewModel gv : masterGradeList) {
            Grade g = new Grade(
                    gv.getStudentID(),
                    gv.getCourseClassID(),
                    gv.getSemesterID(),
                    gv.getGradeAssessment1(),
                    gv.getGradeAssessment2(),
                    gv.getGradeFinal(),
                    gv.getGradeAverage(),
                    gv.getGradeNote()
            );

            if (!gradeDAO.updateGrade(g)) {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu bảng điểm thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Cảnh báo", "Có lỗi khi lưu một số dòng điểm.");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) { /* Giữ nguyên logic gốc */
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Hệ thống Quản lý Điểm - Đăng nhập");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) { /* Giữ nguyên logic gốc */
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- VIEW MODEL CHO BẢNG ĐIỂM ---
    public static class GradeViewModel {
        private final String studentID;
        private final String courseClassID;
        private final String semesterID;

        private final SimpleDoubleProperty gradeAssessment1;
        private final SimpleDoubleProperty gradeAssessment2;
        private final SimpleDoubleProperty gradeFinal;
        private final SimpleDoubleProperty gradeAverage;
        private SimpleStringProperty gradeNote;

        private final SimpleIntegerProperty absentCount;

        public GradeViewModel(Grade grade, int absentCount) {
            this.studentID = grade.getStudentID();
            this.courseClassID = grade.getCourseClassID();
            this.semesterID = grade.getSemesterID();

            this.gradeAssessment1 = new SimpleDoubleProperty(grade.getGradeAssessment1());
            this.gradeAssessment2 = new SimpleDoubleProperty(grade.getGradeAssessment2());
            this.gradeFinal = new SimpleDoubleProperty(grade.getGradeFinal());
            this.gradeAverage = new SimpleDoubleProperty(grade.getGradeAverage());
            this.gradeNote = new SimpleStringProperty(grade.getGradeNote());
            this.absentCount = new SimpleIntegerProperty(absentCount);
        }

        public String getStudentID() { return studentID; }
        public String getCourseClassID() { return courseClassID; }
        public String getSemesterID() { return semesterID; }
        public int getAbsentCount() { return absentCount.get(); }
        public String getGradeNote() { return gradeNote.get(); }

        public double getGradeAssessment1() { return gradeAssessment1.get(); }
        public double getGradeAssessment2() { return gradeAssessment2.get(); }
        public double getGradeFinal() { return gradeFinal.get(); }
        public double getGradeAverage() { return gradeAverage.get(); }

        public SimpleDoubleProperty gradeAssessment1Property() { return gradeAssessment1; }
        public SimpleDoubleProperty gradeAssessment2Property() { return gradeAssessment2; }
        public SimpleDoubleProperty gradeFinalProperty() { return gradeFinal; }
        public SimpleDoubleProperty gradeAverageProperty() { return gradeAverage; }
        public SimpleStringProperty gradeNoteProperty() { return gradeNote; }
        public SimpleIntegerProperty absentCountProperty() { return absentCount; }

        public void setGradeAssessment1(double value) { gradeAssessment1.set(value); }
        public void setGradeAssessment2(double value) { gradeAssessment2.set(value); }
        public void setGradeFinal(double value) { gradeFinal.set(value); }
        public void setGradeAverage(double value) { gradeAverage.set(value); }
        public void setGradeNote(String value) { gradeNote.set(value); }
    }
}