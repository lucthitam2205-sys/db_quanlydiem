package com.db_quanlydiem.controller;

import com.db_quanlydiem.Main;
import com.db_quanlydiem.dao.*;
import com.db_quanlydiem.model.*;
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
    @FXML private Label lblProfName, lblProfID, lblTitle, lblPhone, lblEmail, lblHometown;
    @FXML private ComboBox<CourseClass> cbClass;
    @FXML private TextField txtSearchStudent; // Ô tìm kiếm

    // TableView và Columns
    @FXML private TableView<Grade> tableGrades;
    @FXML private TableColumn<Grade, String> colSTT, colStudentID, colStudentName, colNote;
    @FXML private TableColumn<Grade, Double> colScore1, colScore2, colScoreFinal, colScoreAvg;
    @FXML private TableColumn<Grade, Integer> colAbsent;
    // Thống kê
    @FXML private Label lblTotalStudents, lblRateExcellent, lblRateGood, lblRateFair, lblRateAverage;

    // --- 2. DAO & DATA ---
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private GradeDAO gradeDAO = new GradeDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private ProfessorDAO professorDAO = new ProfessorDAO();
    private ReviewRequestDAO reviewRequestDAO = new ReviewRequestDAO(); // DAO Phúc khảo
    private AuditLogDAO auditLogDAO = new AuditLogDAO(); // DAO Nhật ký hoạt động

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();

    // ID Giảng viên (Được set từ LoginController)
    public static String CURRENT_PROFESSOR_ID = "GV001";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Load thông tin giảng viên
        loadProfessorInfo();

        // 2. Load danh sách lớp giảng viên dạy vào ComboBox
        loadClasses();

        // 3. Cấu hình bảng điểm (Cho phép sửa & Cảnh báo điểm liệt)
        setupTable();

        // 4. Sự kiện khi chọn lớp -> Load điểm
        cbClass.setOnAction(e -> loadGrades());
    }

    // --- LOGIC LOAD DỮ LIỆU & CẤU HÌNH BẢNG ---

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
        tableGrades.setEditable(true);

        colStudentID.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        colAbsent.setCellValueFactory(new PropertyValueFactory<>("absentCount"));
        // Hiển thị tên sinh viên từ ID (Tối ưu: Nên cache list student thay vì gọi DAO mỗi dòng)

        colStudentName.setCellValueFactory(cellData -> {
            String sID = cellData.getValue().getStudentID();
            List<Student> students = studentDAO.getAllStudents();
            String name = students.stream()
                    .filter(s -> s.getStudentID().equals(sID))
                    .map(Student::getStudentName)
                    .findFirst().orElse("Unknown");
            return new SimpleStringProperty(name);
        });

        // Cấu hình các cột điểm cho phép sửa
        setupEditableColumn(colScore1, "gradeAssessment1");
        setupEditableColumn(colScore2, "gradeAssessment2");
        setupEditableColumn(colScoreFinal, "gradeFinal");

        colScoreAvg.setCellValueFactory(new PropertyValueFactory<>("gradeAverage"));

        // --- TÔ MÀU CẢNH BÁO ĐIỂM LIỆT ---
        colScoreAvg.setCellFactory(column -> new TableCell<Grade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item));
                    // Logic tô màu: < 4.0 (Trượt) -> Đỏ; >= 8.5 (Xuất sắc) -> Xanh
                    if (item < 4.0 && item > 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: #fadbd8;");
                        setTooltip(new Tooltip("Cảnh báo: Sinh viên trượt môn (F)"));
                    } else if (item >= 8.5) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

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
            if (newVal == null) newVal = 0.0;

            if (property.equals("gradeAssessment1")) g.setGradeAssessment1(newVal);
            else if (property.equals("gradeAssessment2")) g.setGradeAssessment2(newVal);
            else if (property.equals("gradeFinal")) g.setGradeFinal(newVal);

            // Tính lại điểm trung bình ngay lập tức (30-20-50)
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
        List<Grade> rawData = gradeDAO.getGradesByClass(selectedClass.getCourseClassId());

        // Tính toán lại điểm trung bình khi load (đề phòng DB lưu sai)
        for (Grade g : rawData) {
            double s1 = g.getGradeAssessment1();
            double s2 = g.getGradeAssessment2();
            double sF = g.getGradeFinal();

            double avg = (s1 * 0.3) + (s2 * 0.2) + (sF * 0.5);
            g.setGradeAverage(Math.round(avg * 100.0) / 100.0);

            gradeList.add(g);
        }

        tableGrades.setItems(gradeList);
        tableGrades.refresh();
        updateStatistics();
    }

    private void updateStatistics() {
        if (gradeList.isEmpty()) {
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

    // 1. Tìm kiếm sinh viên
    @FXML
    public void handleSearch() {
        String keyword = txtSearchStudent.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            tableGrades.setItems(gradeList); // Reset về danh sách đầy đủ
            updateStatistics();
            return;
        }

        List<Student> allStudents = studentDAO.getAllStudents();
        ObservableList<Grade> filteredList = FXCollections.observableArrayList();

        for (Grade g : gradeList) {
            String studentID = g.getStudentID().toLowerCase();
            String studentName = allStudents.stream()
                    .filter(s -> s.getStudentID().equals(g.getStudentID()))
                    .map(Student::getStudentName)
                    .findFirst()
                    .orElse("")
                    .toLowerCase();

            if (studentID.contains(keyword) || studentName.contains(keyword)) {
                filteredList.add(g);
            }
        }
        tableGrades.setItems(filteredList);
    }

    // 2. Xem Phúc khảo
    @FXML
    public void handleViewReviews(ActionEvent event) {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn lớp", "Vui lòng chọn lớp học phần để xem yêu cầu phúc khảo!");
            return;
        }

        List<ReviewRequest> requests = reviewRequestDAO.getRequestsByClass(selectedClass.getCourseClassId());
        System.out.println("Tìm thấy: " + requests.size() + " yêu cầu.");
        
        if (requests.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không có dữ liệu", "Lớp này hiện chưa có yêu cầu phúc khảo nào.");
            return;
        }

        StringBuilder content = new StringBuilder();
        content.append("DANH SÁCH YÊU CẦU PHÚC KHẢO\n");
        content.append("Lớp: ").append(selectedClass.getClassName()).append("\n");
        content.append("=========================================\n\n");

        for (ReviewRequest req : requests) {
            String studentName = "Unknown";
            try {
                studentName = studentDAO.getAllStudents().stream()
                        .filter(s -> s.getStudentID().equals(req.getStudentID()))
                        .findFirst().map(Student::getStudentName).orElse("Unknown");
            } catch (Exception e) {}

            content.append("• Sinh viên: ").append(studentName).append(" (").append(req.getStudentID()).append(")\n");
            content.append("  Lý do: ").append(req.getReason()).append("\n");
            content.append("  Ngày gửi: ").append(req.getRequestDate()).append("\n");
            content.append("-----------------------------------------\n");
        }
        System.out.println("Đang tìm phúc khảo cho lớp: " + selectedClass.getCourseClassId());

        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(400, 300);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Danh sách Phúc khảo");
        alert.setHeaderText("Yêu cầu từ sinh viên");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // 3. Lưu điểm (Kèm Audit Log)
    @FXML
    public void handleSaveGrades() {
        if (gradeList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Trống", "Không có dữ liệu để lưu.");
            return;
        }

        boolean allSuccess = true;
        int count = 0;

        for (Grade g : gradeList) {
            if (gradeDAO.updateGrade(g)) {
                count++;
            } else {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            // --- GHI LOG ---
            CourseClass currentClass = cbClass.getValue();
            String className = (currentClass != null) ? currentClass.getClassName() : "Unknown";

            auditLogDAO.addLog(new AuditLog(
                    CURRENT_PROFESSOR_ID,
                    "NHẬP ĐIỂM",
                    "Đã cập nhật điểm cho " + count + " sinh viên lớp " + className
            ));
            // ----------------

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu bảng điểm thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Cảnh báo", "Có lỗi khi lưu một số dòng điểm.");
        }
    }

    // 4. Các chức năng khác (Lịch, Điểm danh, Xuất file, Logout)
    @FXML
    public void handleViewSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("professor_schedule.fxml"));
            Parent root = loader.load();
            ProfessorScheduleController controller = loader.getController();
            controller.setProfessorID(CURRENT_PROFESSOR_ID);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Lịch giảng dạy cá nhân");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
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

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Điểm danh - " + selectedClass.getCourseClassId());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // --- ĐOẠN CODE QUAN TRỌNG CẦN THÊM ---
            // Khi cửa sổ điểm danh đóng lại, tự động gọi hàm loadGrades() để cập nhật số buổi vắng
            stage.setOnHiding(e -> {
                loadGrades(); // Hàm này sẽ query lại DB và update bảng điểm + cột vắng
            });
            // -------------------------------------

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ điểm danh: " + e.getMessage());
        }
    }

    @FXML
    public void handleExportList(ActionEvent event) {
        CourseClass selectedClass = cbClass.getValue();
        if (selectedClass == null || gradeList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Vui lòng chọn lớp và đảm bảo có dữ liệu.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu danh sách lớp");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Word Document", "*.doc"));
        fileChooser.setInitialFileName("DanhSach_" + selectedClass.getCourseClassId() + ".doc");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) exportToWord(file, selectedClass);
    }

    private void exportToWord(File file, CourseClass selectedClass) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("<html><head><meta charset='UTF-8'><style>body{font-family:'Times New Roman';} table{width:100%;border-collapse:collapse;} th,td{border:1px solid black;padding:5px;text-align:center;}</style></head><body>");
            writer.println("<h2 style='text-align:center;'>BẢNG ĐIỂM CHI TIẾT</h2>");
            writer.println("<p><b>Lớp:</b> " + selectedClass.getClassName() + "</p>");
            writer.println("<table><tr><th>STT</th><th>Mã SV</th><th>Họ tên</th><th>ĐG1</th><th>ĐG2</th><th>Cuối kỳ</th><th>Tổng kết</th></tr>");
            int stt = 1;
            for (Grade g : gradeList) {
                String name = colStudentName.getCellData(g);
                writer.println("<tr><td>" + (stt++) + "</td><td>" + g.getStudentID() + "</td><td style='text-align:left;'>" + name + "</td><td>" + g.getGradeAssessment1() + "</td><td>" + g.getGradeAssessment2() + "</td><td>" + g.getGradeFinal() + "</td><td><b>" + g.getGradeAverage() + "</b></td></tr>");
            }
            writer.println("</table></body></html>");
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất file!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}