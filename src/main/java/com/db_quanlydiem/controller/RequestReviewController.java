package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.ReviewRequestDAO;
import com.db_quanlydiem.model.ReviewRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class RequestReviewController {

    @FXML private Label lblSubjectInfo;
    @FXML private TextArea txtReason;

    private ReviewRequestDAO reviewRequestDAO = new ReviewRequestDAO();
    private String studentID;
    private String courseClassID;

    public void setInfo(String studentID, String courseClassID, String subjectName) {
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        lblSubjectInfo.setText("Môn học: " + subjectName + "\n(Lớp: " + courseClassID + ")");
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập lý do phúc khảo.");
            return;
        }

        ReviewRequest req = new ReviewRequest(studentID, courseClassID, reason);
        if (reviewRequestDAO.addRequest(req)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu phúc khảo thành công!");
            handleCancel(event); // Đóng cửa sổ
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Gửi yêu cầu thất bại. Vui lòng thử lại sau.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) txtReason.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}