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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {

    // --- 1. KHAI BÁO FXML (Phải trùng khớp 100% với fx:id trong file FXML) ---
    @FXML private Label lblStudentName, lblStudentID, lblCohort, lblMajor, lblPhone, lblEmail;

    // Toolbar
    @FXML private ComboBox<Semester> cbSemester;
    @FXML private ToggleGroup viewMode;
    @FXML private ToggleButton btnTabGrade, btnTabSchedule;

    // Views (StackPane)
    @FXML private VBox viewGrade;
    @FXML private VBox viewSchedule;

    // Bảng Điểm
    @FXML private TableView<GradeViewModel> tableGrade;
    @FXML private TableColumn<GradeViewModel, String> colSubjectName, colNote;
    @FXML private TableColumn<GradeViewModel, Integer> colCredit;
    @FXML private TableColumn<GradeViewModel, Double> colScore1, colScore2, colScoreFinal, colScoreAvg;

    // Bảng Lịch học
    @FXML private TableView<ScheduleViewModel> tableSchedule;
    @FXML private TableColumn<ScheduleViewModel, String> colDay, colShift, colSubjectSchedule, colRoom;

    // Footer Stats
    @FXML private Label lblGPA, lblCPA;

    // --- 2. DATA ---
    private SemesterDAO semesterDAO = new SemesterDAO();
    private ObservableList<GradeViewModel> gradeList = FXCollections.observableArrayList();
    private ObservableList<ScheduleViewModel> scheduleList = FXCollections.observableArrayList();

    // Biến tĩnh để nhận ID từ LoginController truyền sang
    public static String CURRENT_STUDENT_ID = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Kiểm tra nếu chưa có ID (chạy thử) thì gán mặc định để không lỗi
        if (CURRENT_STUDENT_ID == null || CURRENT_STUDENT_ID.isEmpty()) {
            CURRENT_STUDENT_ID = "69IT1001"; // ID mặc định để test
        }

        loadStudentInfo();

        // Load danh sách học kỳ
        cbSemester.setItems(FXCollections.observableArrayList(semesterDAO.getAllSemesters()));
        if (!cbSemester.getItems().isEmpty()) {
            cbSemester.getSelectionModel().selectFirst();
        }

        setupGradeTable();
        setupScheduleTable();

        // Xử lý chuyển tab Kết quả / Lịch học
        viewMode.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == btnTabGrade) {
                viewGrade.setVisible(true);
                viewSchedule.setVisible(false);
            } else if (newVal == btnTabSchedule) {
                viewGrade.setVisible(false);
                viewSchedule.setVisible(true);
            }
        });

        // Xử lý khi chọn kỳ học khác
        cbSemester.setOnAction(e -> {
            loadGrades();
            loadSchedule();
        });

        // Load dữ liệu lần đầu
        loadGrades();
        loadSchedule();
        calculateCPA();
    }

    // --- CÁC HÀM SỰ KIỆN (ACTIONS) ---

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

    // --- CÁC HÀM LOGIC LOAD DỮ LIỆU ---

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

                // Tự động suy ra Khóa từ 2 số đầu MSSV
                String id = rs.getString("StudentID");
                if (id.length() >= 2) {
                    lblCohort.setText("K" + id.substring(0, 2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupGradeTable() {
        colSubjectName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colCredit.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCredit()).asObject());
        colScore1.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScore1()).asObject());
        colScore2.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScore2()).asObject());
        colScoreFinal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScoreFinal()).asObject());
        colScoreAvg.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getScoreAvg()).asObject());
        colNote.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNote()));
        tableGrade.setItems(gradeList);
    }

    private void setupScheduleTable() {
        colDay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDay()));
        colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift()));
        colSubjectSchedule.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoom()));
        tableSchedule.setItems(scheduleList);
    }

    private void loadGrades() {
        if (cbSemester.getValue() == null) return;
        String semesterID = cbSemester.getValue().getSemesterID();
        gradeList.clear();

        String sql = "SELECT s.SubjectName, s.SubjectCredit, g.GradeAssessment1, g.GradeAssessment2, g.GradeFinal, g.GradeNote " +
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
                String subjName = rs.getString("SubjectName");
                int credit = rs.getInt("SubjectCredit");
                double s1 = rs.getDouble("GradeAssessment1");
                double s2 = rs.getDouble("GradeAssessment2");
                double sF = rs.getDouble("GradeFinal");
                String note = rs.getString("GradeNote");

                // TÍNH ĐIỂM TỰ ĐỘNG: (30% - 20% - 50%)
                double sAvg = (s1 * 0.3) + (s2 * 0.2) + (sF * 0.5);
                sAvg = Math.round(sAvg * 100.0) / 100.0; // Làm tròn 2 chữ số

                gradeList.add(new GradeViewModel(subjName, credit, s1, s2, sF, sAvg, note));

                // Chỉ tính GPA nếu môn đó đã có điểm tổng kết > 0
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void loadSchedule() {
        if (cbSemester.getValue() == null) return;
        String semesterID = cbSemester.getValue().getSemesterID();
        scheduleList.clear();

        String sql = "SELECT cs.DayOfWeek, cs.Shift, s.SubjectName, cs.Room " +
                "FROM ClassSchedule cs " +
                "JOIN CourseClass c ON cs.CourseClassID = c.CourseClassId " +
                "JOIN Subject s ON c.SubjectId = s.SubjectId " +
                "JOIN Grade g ON c.CourseClassId = g.CourseClassID " +
                "WHERE g.StudentID = ? AND c.SemesterID = ? " +
                "ORDER BY FIELD(cs.DayOfWeek, 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'CN'), cs.Shift";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CURRENT_STUDENT_ID);
            pstmt.setString(2, semesterID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                scheduleList.add(new ScheduleViewModel(
                        rs.getString("DayOfWeek"),
                        rs.getString("Shift"),
                        rs.getString("SubjectName"),
                        rs.getString("Room")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateCPA() {
        // Truy vấn lấy tất cả điểm thành phần của mọi kỳ
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

                // TÍNH ĐIỂM TỰ ĐỘNG CHO CPA
                double sAvg = (s1 * 0.3) + (s2 * 0.2) + (sF * 0.5);

                if (sAvg > 0) { // Chỉ tính nếu đã có điểm
                    totalScore += sAvg * rs.getInt("SubjectCredit");
                    totalCredits += rs.getInt("SubjectCredit");
                }
            }

            if (totalCredits > 0) {
                lblCPA.setText(String.format("%.2f", totalScore / totalCredits));
            } else {
                lblCPA.setText("0.00");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- INNER CLASSES (View Models cho TableView) ---

    public static class GradeViewModel {
        private final String subjectName;
        private final int credit;
        private final double score1, score2, scoreFinal, scoreAvg;
        private final String note;

        public GradeViewModel(String subjectName, int credit, double s1, double s2, double sF, double sAvg, String note) {
            this.subjectName = subjectName; this.credit = credit;
            this.score1 = s1; this.score2 = s2; this.scoreFinal = sF; this.scoreAvg = sAvg;
            this.note = note;
        }
        public String getSubjectName() { return subjectName; }
        public int getCredit() { return credit; }
        public double getScore1() { return score1; }
        public double getScore2() { return score2; }
        public double getScoreFinal() { return scoreFinal; }
        public double getScoreAvg() { return scoreAvg; }
        public String getNote() { return note; }
    }

    public static class ScheduleViewModel {
        private final String day, shift, subjectName, room;

        public ScheduleViewModel(String day, String shift, String subjectName, String room) {
            this.day = day; this.shift = shift; this.subjectName = subjectName; this.room = room;
        }
        public String getDay() { return day; }
        public String getShift() { return shift; }
        public String getSubjectName() { return subjectName; }
        public String getRoom() { return room; }
    }
}