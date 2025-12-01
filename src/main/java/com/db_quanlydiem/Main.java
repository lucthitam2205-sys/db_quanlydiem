package com.db_quanlydiem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Tải màn hình Đăng nhập (login.fxml) đầu tiên
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Thiết lập tiêu đề và hiển thị cửa sổ
        stage.setTitle("Hệ thống Quản lý Điểm - Đăng nhập");
        stage.setScene(scene);
        stage.setResizable(false); // Không cho phép thay đổi kích thước cửa sổ login
        stage.show();
    }

    // Hàm main chuẩn để chạy ứng dụng
    public static void main(String[] args) {
        launch();
    }
}