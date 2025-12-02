package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.dao.GradeDAO;
import com.db_quanlydiem.dao.ProfessorDAO;
import com.db_quanlydiem.dao.StudentDAO;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Grade;
import com.db_quanlydiem.model.Professor;
import com.db_quanlydiem.model.Student;
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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProfessorDashboardController implements Initializable {

    // --- 1. KHAI BÁO FXML ---
    @FXML private Label lblProfName, lblProfID, lblTitle, lblPhone, lblEmail, lblHometown; // Đã thêm lblHometown
    @FXML private ComboBox<CourseClass> cbClass;
    @FXML private TextField txtSearchStudent;

    // TableView và Columns
    @FXML private TableView<Grade> tableGrades;
    @FXML private TableColumn<Grade, String> colSTT, colStudentID, colStudentName, colNote;
    @FXML private TableColumn<Grade, Double> colScore1, colScore2, colScoreFinal, colScoreAvg;

    // Thống kê
    @FXML private Label lblTotalStudents, lblRateExcellent, lblRateGood, lblRateFair, lblRateAverage;

    // --- 2. DAO & DATA ---
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private GradeDAO gradeDAO = new GradeDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private ProfessorDAO professorDAO = new ProfessorDAO();

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();

    // ID Giảng viên (Giả lập - Trong thực tế sẽ set từ LoginController)
    public static String CURRENT_PROFESSOR_ID = "GV001";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Load thông tin giảng viên
        loadProfessorInfo();

        // 2. Load danh sách lớp giảng viên dạy vào ComboBox
        loadClasses();

        // 3. Cấu hình bảng điểm (Cho phép sửa)
        setupTable();

        // 4. Sự kiện khi chọn lớp -> Load điểm
        cbClass.setOnAction(e -> loadGrades());
    }

    // --- LOGIC CHÍNH ---

    private void loadProfessorInfo() {
        List<Professor> professors = professorDAO.getAllProfessors();
        Professor currentProf = professors.stream()
                .filter(p -> p.getProfessorID().equals(CURRENT_PROFESSOR_ID))
                .findFirst()
                .orElse(null);

        if (currentProf != null) {
            lblProfName.setText(currentProf.getProfessorName());
            lblProfID.setText("Mã GV: " + currentProf.getProfessorID());
            lblTitle.setText(currentProf.getProfessorTitle());
            lblPhone.setText(currentProf.getProfessorPhone());
            lblEmail.setText(currentProf.getProfessorEmail());

            // Set quê quán nếu label tồn tại trong FXML
            if (lblHometown != null) {
                lblHometown.setText(currentProf.getProfessorHometown());
            }
        }
    }

    private void loadClasses() {
        List<CourseClass> allClasses = courseClassDAO.getAllCourseClasses();
        List<CourseClass> myClasses = allClasses.stream()
                .filter(c -> CURRENT_PROFESSOR_ID.equals(c.getProfessorID()))
                .collect(Collectors.toList());

        cbClass.setItems(FXCollections.observableArrayList(myClasses));
    }

    private void setupTable() {
        tableGrades.setEditable(true); // Quan trọng: Cho phép sửa bảng

        colStudentID.setCellValueFactory(new PropertyValueFactory<>("studentID"));

        // Hiển thị tên sinh viên (Lấy từ StudentDAO)
        colStudentName.setCellValueFactory(cellData -> {
            String sID = cellData.getValue().getStudentID();
            List<Student> students = studentDAO.getAllStudents();
            String name = students.stream()
                    .filter(s -> s.getStudentID().equals(sID))
                    .map(Student::getStudentName)
                    .findFirst().orElse("Unknown");
            return new SimpleStringProperty(name);
        });

        // Cấu hình các cột điểm cho phép sửa (Edit)
        setupEditableColumn(colScore1, "gradeAssessment1");
        setupEditableColumn(colScore2, "gradeAssessment2");
        setupEditableColumn(colScoreFinal, "gradeFinal");

        colScoreAvg.setCellValueFactory(new PropertyValueFactory<>("gradeAverage"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("gradeNote"));
        colNote.setCellFactory(TextFieldTableCell.forTableColumn());
        colNote.setOnEditCommit(e -> e.getRowValue().setGradeNote(e.getNewValue()));

        // Cột STT tự động
        colSTT.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else setText(String.valueOf(getIndex() + 1));
            }
        });
    }

    private void setupEditableColumn(TableColumn<Grade, Double> col, String property) {
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        col.setOnEditCommit(event -> {
            Grade g = event.getRowValue();
            Double newVal = event.getNewValue();

            // Xử lý null
            if (newVal == null) newVal = 0.0;

            if (property.equals("gradeAssessment1")) g.setGradeAssessment1(newVal);
            else if (property.equals("gradeAssessment2")) g.setGradeAssessment2(newVal);
            else if (property.equals("gradeFinal")) g.setGradeFinal(newVal);

            // Tự động tính lại điểm trung bình
            double avg = (g.getGradeAssessment1() * 0.3) + (g.getGradeAssessment2() * 0.2) + (g.getGradeFinal() * 0.5);
            g.setGradeAverage(Math.round(avg * 100.0) / 100.0);

            tableGrades.refresh();
            updateStatistics();
        });
    }

    private void loadGrades() {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null) return;

        gradeList.clear();
        gradeList.addAll(gradeDAO.getGradesByClass(selectedClass.getCourseClassId()));
        tableGrades.setItems(gradeList);

        updateStatistics();
    }

    private void updateStatistics() {
        if (gradeList.isEmpty()) {
            // Reset text về 0 nếu list trống
            lblTotalStudents.setText("0");
            lblRateExcellent.setText("0%");
            lblRateGood.setText("0%");
            lblRateFair.setText("0%");
            lblRateAverage.setText("0%");
            return;
        }

        int total = gradeList.size();
        long excellent = gradeList.stream().filter(g -> g.getGradeAverage() >= 8.5).count();
        long good = gradeList.stream().filter(g -> g.getGradeAverage() >= 7.0 && g.getGradeAverage() < 8.5).count();
        long fair = gradeList.stream().filter(g -> g.getGradeAverage() >= 5.5 && g.getGradeAverage() < 7.0).count();
        long average = gradeList.stream().filter(g -> g.getGradeAverage() >= 4.0 && g.getGradeAverage() < 5.5).count();

        lblTotalStudents.setText(String.valueOf(total));
        lblRateExcellent.setText(String.format("%.1f%%", (double)excellent/total * 100));
        lblRateGood.setText(String.format("%.1f%%", (double)good/total * 100));
        lblRateFair.setText(String.format("%.1f%%", (double)fair/total * 100));
        lblRateAverage.setText(String.format("%.1f%%", (double)average/total * 100));
    }

    // --- CÁC HÀM SỰ KIỆN (BUTTON ACTIONS) ---

    // 1. MỞ CỬA SỔ XEM LỊCH DẠY
    @FXML
    public void handleViewSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("professor_schedule.fxml"));
            Parent root = loader.load();

            // Truyền ID giảng viên sang để load lịch đúng
            ProfessorScheduleController controller = loader.getController();
            controller.setProfessorID(CURRENT_PROFESSOR_ID);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Lịch giảng dạy cá nhân");
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn cửa sổ cha
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở lịch giảng dạy: " + e.getMessage());
        }
    }

    // 2. MỞ CỬA SỔ ĐIỂM DANH
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

    // 3. XUẤT FILE WORD
    @FXML
    public void handleExportList(ActionEvent event) {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null || gradeList.isEmpty()) {
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
            writer.println("<tr><th>STT</th><th>Mã SV</th><th>Họ và Tên</th><th>ĐG 1</th><th>ĐG 2</th><th>Cuối kỳ</th><th>Tổng kết</th><th>Ghi chú</th></tr>");

            int stt = 1;
            for (Grade g : gradeList) {
                String name = colStudentName.getCellData(g);
                writer.println("<tr>");
                writer.println("<td>" + (stt++) + "</td>");
                writer.println("<td>" + g.getStudentID() + "</td>");
                writer.println("<td style='text-align: left;'>" + name + "</td>");
                writer.println("<td>" + g.getGradeAssessment1() + "</td>");
                writer.println("<td>" + g.getGradeAssessment2() + "</td>");
                writer.println("<td>" + g.getGradeFinal() + "</td>");
                writer.println("<td><b>" + g.getGradeAverage() + "</b></td>");
                writer.println("<td>" + (g.getGradeNote() == null ? "" : g.getGradeNote()) + "</td>");
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

    // 4. LƯU ĐIỂM XUỐNG DB
    @FXML
    public void handleSaveGrades() {
        if (gradeList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Trống", "Không có dữ liệu để lưu.");
            return;
        }

        boolean allSuccess = true;
        for (Grade g : gradeList) {
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}