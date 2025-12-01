package com.db_quanlydiem.controller;

import com.db_quanlydiem.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class StudentDetailsController {

    @FXML private Label lblName, lblID, lblDOB, lblGender, lblHometown;
    @FXML private Label lblMajor, lblStatus, lblEmail, lblPhone;
    @FXML private Label lblParentName, lblParentPhone;

    private String studentID;

    // Hàm này sẽ được gọi từ DashboardController để truyền ID sang
    public void setStudentID(String studentID) {
        this.studentID = studentID;
        loadDetails();
    }

    private void loadDetails() {
        String sql = "SELECT * FROM Student WHERE StudentID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                lblName.setText(rs.getString("StudentName"));
                lblID.setText(rs.getString("StudentID"));

                // Format ngày sinh
                if (rs.getDate("StudentDOB") != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    lblDOB.setText(sdf.format(rs.getDate("StudentDOB")));
                } else {
                    lblDOB.setText("N/A");
                }

                lblGender.setText(rs.getString("StudentGender"));
                lblHometown.setText(rs.getString("StudentHometown"));
                lblMajor.setText(rs.getString("StudentMajor"));
                lblStatus.setText(rs.getString("StudentStatus"));
                lblEmail.setText(rs.getString("StudentEmail"));
                lblPhone.setText(rs.getString("StudentPhone"));
                lblParentName.setText(rs.getString("ParentName"));
                lblParentPhone.setText(rs.getString("ParentPhone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // --- CHỨC NĂNG IN HỒ SƠ RA FILE DOC ---
    @FXML
    public void printProfile(ActionEvent event) {
        // Tạo hộp thoại chọn nơi lưu file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu hồ sơ sinh viên");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Word Document", "*.doc"));

        // Tên file mặc định: HoSo_MSSV.doc
        String defaultFileName = "HoSo_" + (lblID.getText().isEmpty() ? "SinhVien" : lblID.getText()) + ".doc";
        fileChooser.setInitialFileName(defaultFileName);

        // Lấy Stage hiện tại để hiển thị hộp thoại
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveToFile(file);
        }
    }

    private void saveToFile(File file) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            // Sử dụng HTML cơ bản để tạo định dạng cho file .doc
            // Word có thể đọc và hiển thị HTML rất tốt
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<style>");
            writer.println("body { font-family: 'Times New Roman', serif; font-size: 12pt; }");
            writer.println("h1 { text-align: center; color: #2c3e50; }");
            writer.println("h3 { color: #2980b9; border-bottom: 1px solid #bdc3c7; padding-bottom: 5px; }");
            writer.println(".info-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            writer.println(".info-table td { padding: 8px; vertical-align: top; }");
            writer.println(".label { font-weight: bold; width: 150px; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");

            // Tiêu đề
            writer.println("<h1>HỒ SƠ SINH VIÊN</h1>");
            writer.println("<p style='text-align: center;'>Trường Đại học Xây dựng Hà Nội</p>");
            writer.println("<br/>");

            // Phần 1
            writer.println("<h3>I. THÔNG TIN CÁ NHÂN</h3>");
            writer.println("<table class='info-table'>");
            writer.println("<tr><td class='label'>Họ và tên:</td><td>" + lblName.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Mã sinh viên:</td><td>" + lblID.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Ngày sinh:</td><td>" + lblDOB.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Giới tính:</td><td>" + lblGender.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Quê quán:</td><td>" + lblHometown.getText() + "</td></tr>");
            writer.println("</table>");

            // Phần 2
            writer.println("<h3>II. TÌNH TRẠNG HỌC TẬP & LIÊN HỆ</h3>");
            writer.println("<table class='info-table'>");
            writer.println("<tr><td class='label'>Chuyên ngành:</td><td>" + lblMajor.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Trạng thái:</td><td>" + lblStatus.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Email:</td><td>" + lblEmail.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>Số điện thoại:</td><td>" + lblPhone.getText() + "</td></tr>");
            writer.println("</table>");

            // Phần 3
            writer.println("<h3>III. THÔNG TIN GIA ĐÌNH</h3>");
            writer.println("<table class='info-table'>");
            writer.println("<tr><td class='label'>Họ tên phụ huynh:</td><td>" + lblParentName.getText() + "</td></tr>");
            writer.println("<tr><td class='label'>SĐT liên hệ:</td><td>" + lblParentPhone.getText() + "</td></tr>");
            writer.println("</table>");

            writer.println("<br/><br/>");
            writer.println("<p style='text-align: right; font-style: italic;'>Hà Nội, ngày ... tháng ... năm ......</p>");
            writer.println("<p style='text-align: right; font-weight: bold;'>Xác nhận của nhà trường&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>");

            writer.println("</body>");
            writer.println("</html>");

            // Thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã xuất hồ sơ thành công tới:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi xuất file");
            alert.setContentText("Không thể lưu file: " + e.getMessage());
            alert.showAndWait();
        }
    }
}