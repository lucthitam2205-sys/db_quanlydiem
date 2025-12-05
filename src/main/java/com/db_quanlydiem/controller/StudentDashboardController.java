package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.dao.SemesterDAO;
import com.db_quanlydiem.model.Semester;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {

    // --- 1. KHAI BÁO FXML ---
    @FXML private Label lblStudentName, lblStudentID, lblCohort, lblMajor, lblPhone, lblEmail;
    @FXML private ImageView imgAvatar;

    @FXML private ComboBox<Semester> cbSemester;
    @FXML private ToggleGroup viewMode;
    @FXML private ToggleButton btnTabGrade, btnTabSchedule;

    @FXML private VBox viewGrade;
    @FXML private VBox viewSchedule;

    // Bảng điểm
    @FXML private TableView<GradeViewModel> tableGrade;
    @FXML private TableColumn<GradeViewModel, String> colSubjectName, colNote;
    @FXML private TableColumn<GradeViewModel, Integer> colCredit;
    @FXML private TableColumn<GradeViewModel, Double> colScore1, colScore2, colScoreFinal, colScoreAvg;

    // Bảng lịch học (CẬP NHẬT MỚI)
    @FXML private TableView<ScheduleViewModel> tableSchedule;
    @FXML private TableColumn<ScheduleViewModel, String> colDate, colDay, colShift, colSubjectSchedule, colRoom;
    @FXML private ComboBox<Integer> cbMonth; // Bộ lọc tháng
    @FXML private ComboBox<Integer> cbYear;  // Bộ lọc năm

    @FXML private Label lblGPA, lblCPA;

    // --- 2. DATA ---
    private SemesterDAO semesterDAO = new SemesterDAO();
    private ObservableList<GradeViewModel> gradeList = FXCollections.observableArrayList();
    private ObservableList<ScheduleViewModel> scheduleList = FXCollections.observableArrayList();

    public static String CURRENT_STUDENT_ID = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Fallback ID cho mục đích test
        if (CURRENT_STUDENT_ID == null || CURRENT_STUDENT_ID.isEmpty()) {
            CURRENT_STUDENT_ID = "69IT1234";
        }

        loadStudentInfo();

        // Load danh sách học kỳ
        cbSemester.setItems(FXCollections.observableArrayList(semesterDAO.getAllSemesters()));
        if (!cbSemester.getItems().isEmpty()) {
            cbSemester.getSelectionModel().selectFirst();
        }

        // Khởi tạo các bộ lọc thời gian
        initDateFilters();

        setupGradeTable();
        setupScheduleTable();

        // Sự kiện chuyển Tab
        viewMode.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == btnTabGrade) {
                viewGrade.setVisible(true);
                viewSchedule.setVisible(false);
                loadGrades(); // Load lại điểm khi chuyển tab
            } else if (newVal == btnTabSchedule) {
                viewGrade.setVisible(false);
                viewSchedule.setVisible(true);
                handleFilterSchedule(); // Load lịch theo tháng hiện tại khi chuyển tab
            }
        });

        // Sự kiện đổi học kỳ
        cbSemester.setOnAction(e -> {
            if (btnTabGrade.isSelected()) loadGrades();
            else handleFilterSchedule();
        });

        // Load dữ liệu mặc định ban đầu
        loadGrades();
        calculateCPA();
    }

    private void initDateFilters() {
        ObservableList<Integer> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) months.add(i);
        cbMonth.setItems(months);

        int currentYear = LocalDate.now().getYear();
        cbYear.setItems(FXCollections.observableArrayList(currentYear - 1, currentYear, currentYear + 1));

        // Mặc định chọn tháng năm hiện tại
        cbMonth.setValue(LocalDate.now().getMonthValue());
        cbYear.setValue(currentYear);
    }

    // --- CÁC HÀM SỰ KIỆN ---

    @FXML
    public void handleViewProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("student_details.fxml"));
            Parent root = loader.load();

            StudentDetailsController detailsController = loader.getController();
            detailsController.setStudentID(CURRENT_STUDENT_ID);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Hồ sơ sinh viên chi tiết");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình chi tiết: " + e.getMessage());
        }
    }

    @FXML
    public void handleRequestReview(ActionEvent event) {
        GradeViewModel selected = tableGrade.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Chưa chọn môn", "Vui lòng chọn một môn học trong bảng điểm để yêu cầu phúc khảo.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("request_review.fxml"));
            Parent root = loader.load();

            RequestReviewController controller = loader.getController();
            controller.setInfo(CURRENT_STUDENT_ID, selected.getCourseClassId(), selected.getSubjectName());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gửi yêu cầu phúc khảo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở form phúc khảo: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
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

    // --- LOGIC LỊCH HỌC (MỚI) ---

    @FXML
    public void handleFilterSchedule() {
        Integer m = cbMonth.getValue();
        Integer y = cbYear.getValue();
        if (m != null && y != null) loadScheduleDetailed(m, y);
    }

    @FXML
    public void handleViewAllSchedule() {
        loadScheduleDetailed(null, null);
    }

    private void loadScheduleDetailed(Integer filterMonth, Integer filterYear) {
        if (cbSemester.getValue() == null) return;
        Semester semester = cbSemester.getValue();

        // Kiểm tra xem dữ liệu học kỳ có ngày bắt đầu/kết thúc không
        if (semester.getStartDate() == null || semester.getEndDate() == null) {
            // Có thể cần query lại DB để lấy full info semester nếu ComboBox chỉ chứa ID/Name
            // Ở đây giả định SemesterDAO.getAllSemesters() đã trả về đủ ngày tháng
            return;
        }

        scheduleList.clear();
        String sql = "SELECT cs.DayOfWeek, cs.Shift, s.SubjectName, cs.Room FROM ClassSchedule cs " +
                "JOIN CourseClass c ON cs.CourseClassID = c.CourseClassId " +
                "JOIN Subject s ON c.SubjectId = s.SubjectId " +
                "JOIN Grade g ON c.CourseClassId = g.CourseClassID " +
                "WHERE g.StudentID = ? AND c.SemesterID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CURRENT_STUDENT_ID);
            pstmt.setString(2, semester.getSemesterID());
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = semester.getStartDate().toLocalDate();
            LocalDate end = semester.getEndDate().toLocalDate();

            while (rs.next()) {
                String dbDay = rs.getString("DayOfWeek");
                String shift = rs.getString("Shift");
                String subj = rs.getString("SubjectName");
                String room = rs.getString("Room");

                // THUẬT TOÁN BUNG LỊCH
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    // 1. Kiểm tra Thứ
                    if (!isDayMatch(date.getDayOfWeek(), dbDay)) continue;

                    // 2. Kiểm tra bộ lọc Tháng/Năm
                    if (filterMonth != null && filterYear != null) {
                        if (date.getMonthValue() != filterMonth || date.getYear() != filterYear) continue;
                    }

                    scheduleList.add(new ScheduleViewModel(
                            date.format(formatter), // Ngày
                            dbDay, shift, subj, room
                    ));
                }
            }

            // Sắp xếp theo ngày tăng dần
            scheduleList.sort(Comparator.comparing(ScheduleViewModel::getDate));
            tableSchedule.refresh();

        } catch (SQLException e) { e.printStackTrace(); }
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

    // --- LOGIC ĐIỂM SỐ & INFO ---

    private void loadGrades() {
        if (cbSemester.getValue() == null) return;
        String semesterID = cbSemester.getValue().getSemesterID();
        gradeList.clear();

        String sql = "SELECT c.CourseClassId, s.SubjectName, s.SubjectCredit, g.GradeAssessment1, g.GradeAssessment2, g.GradeFinal, g.GradeNote " +
                "FROM Grade g " +
                "JOIN CourseClass c ON g.CourseClassID = c.CourseClassId " +
                "JOIN Subject s ON c.SubjectId = s.SubjectId " +
                "WHERE g.StudentID = ? AND g.SemesterID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CURRENT_STUDENT_ID);
            pstmt.setString(2, semesterID);
            ResultSet rs = pstmt.executeQuery();

            double totalScore = 0;
            int totalCredits = 0;

            while (rs.next()) {
                String classID = rs.getString("CourseClassId");
                String subjName = rs.getString("SubjectName");
                int credit = rs.getInt("SubjectCredit");
                double s1 = rs.getDouble("GradeAssessment1");
                double s2 = rs.getDouble("GradeAssessment2");
                double sF = rs.getDouble("GradeFinal");
                String note = rs.getString("GradeNote");

                double sAvg = (s1 * 0.3) + (s2 * 0.2) + (sF * 0.5);
                sAvg = Math.round(sAvg * 100.0) / 100.0;

                gradeList.add(new GradeViewModel(classID, subjName, credit, s1, s2, sF, sAvg, note));

                if (sAvg > 0) {
                    totalScore += sAvg * credit;
                    totalCredits += credit;
                }
            }

            if (totalCredits > 0) {
                lblGPA.setText(String.format("%.2f", totalScore / totalCredits));
            } else {
                lblGPA.setText("0.00");
            }
            tableGrade.setItems(gradeList);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void calculateCPA() {
        String sql = "SELECT s.SubjectCredit, g.GradeAssessment1, g.GradeAssessment2, g.GradeFinal " +
                "FROM Grade g " +
                "JOIN CourseClass c ON g.CourseClassID = c.CourseClassId " +
                "JOIN Subject s ON c.SubjectId = s.SubjectId " +
                "WHERE g.StudentID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CURRENT_STUDENT_ID);
            ResultSet rs = pstmt.executeQuery();

            double totalScore = 0;
            int totalCredits = 0;

            while (rs.next()) {
                double s1 = rs.getDouble("GradeAssessment1");
                double s2 = rs.getDouble("GradeAssessment2");
                double sF = rs.getDouble("GradeFinal");
                double sAvg = (s1 * 0.3) + (s2 * 0.2) + (sF * 0.5);

                if (sAvg > 0) {
                    totalScore += sAvg * rs.getInt("SubjectCredit");
                    totalCredits += rs.getInt("SubjectCredit");
                }
            }

            if (totalCredits > 0) {
                lblCPA.setText(String.format("%.2f", totalScore / totalCredits));
            } else {
                lblCPA.setText("0.00");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadStudentInfo() {
        String sql = "SELECT * FROM Student WHERE StudentID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CURRENT_STUDENT_ID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                lblStudentName.setText(rs.getString("StudentName"));
                lblStudentID.setText("MSSV: " + rs.getString("StudentID"));
                lblMajor.setText(rs.getString("StudentMajor"));
                lblPhone.setText(rs.getString("StudentPhone"));
                lblEmail.setText(rs.getString("StudentEmail"));
                lblCohort.setText(rs.getString("StudentCohort"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupGradeTable() {
        colSubjectName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colCredit.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCredit()).asObject());
        colScore1.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScore1()).asObject());
        colScore2.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScore2()).asObject());
        colScoreFinal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScoreFinal()).asObject());
        colScoreAvg.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScoreAvg()).asObject());
        colNote.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNote()));

        colScoreAvg.setCellFactory(column -> new TableCell<GradeViewModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item));
                    if (item < 4.0 && item > 0) {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-background-color: #fadbd8;");
                        setTooltip(new Tooltip("CẢNH BÁO: Điểm dưới 4.0 - Không đạt"));
                    } else if (item >= 8.5) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (item >= 7.0) {
                        setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });
    }

    private void setupScheduleTable() {
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        colDay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDay()));
        colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift()));
        colSubjectSchedule.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoom()));
        tableSchedule.setItems(scheduleList);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- VIEW MODELS ---

    public static class GradeViewModel {
        private final String courseClassId;
        private final String subjectName;
        private final int credit;
        private final double score1, score2, scoreFinal, scoreAvg;
        private final String note;

        public GradeViewModel(String courseClassId, String subjectName, int credit, double s1, double s2, double sF, double sAvg, String note) {
            this.courseClassId = courseClassId; this.subjectName = subjectName; this.credit = credit;
            this.score1 = s1; this.score2 = s2; this.scoreFinal = sF; this.scoreAvg = sAvg; this.note = note;
        }
        public String getCourseClassId() { return courseClassId; }
        public String getSubjectName() { return subjectName; }
        public int getCredit() { return credit; }
        public double getScore1() { return score1; }
        public double getScore2() { return score2; }
        public double getScoreFinal() { return scoreFinal; }
        public double getScoreAvg() { return scoreAvg; }
        public String getNote() { return note; }
    }

    public static class ScheduleViewModel {
        private final String date, day, shift, subjectName, room;

        public ScheduleViewModel(String date, String day, String shift, String subjectName, String room) {
            this.date = date; this.day = day; this.shift = shift; this.subjectName = subjectName; this.room = room;
        }
        public String getDate() { return date; }
        public String getDay() { return day; }
        public String getShift() { return shift; }
        public String getSubjectName() { return subjectName; }
        public String getRoom() { return room; }
    }
}