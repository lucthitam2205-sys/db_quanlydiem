package com.db_quanlydiem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

    public class DatabaseConnection {
        // THAY ĐỔI CÁC THÔNG SỐ NÀY CHO PHÙ HỢP

        // Tên database bạn đã tạo trong MySQL
        private static final String DB_NAME = "db_quanlydiem";
        // Tên đăng nhập MySQL
        private static final String USERNAME = "root";
        // Mật khẩu của MySQL của bạn
        private static final String PASSWORD = "Luctam@1504"; //
        // --- (Các thông số khác thường giữ nguyên) ---
        private static final String HOST = "localhost"; // 127.0.0.1
        private static final int PORT = 3306; // Cổng mặc định của MySQL
        private static final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;

        /**
         * Phương thức lấy kết nối CSDL (được gọi bởi các lớp DAO)
         * @return Đối tượng Connection, hoặc null nếu kết nối thất bại
         */
        public static Connection getConnection() {
            Connection connection = null;
            try {
                // Đăng ký driver (không cần thiết từ JDBC 4.0 trở lên nhưng vẫn nên làm)
                // Class.forName("com.mysql.cj.jdbc.Driver");

                connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                System.out.println("Kết nối CSDL thành công!");
                return connection;
            } catch (SQLException e) {
                System.err.println("Lỗi kết nối CSDL: Kiểm tra XAMPP/WAMP và mật khẩu.");
                e.printStackTrace();
                return null;
            }
            // Lưu ý: Các lớp DAO phải tự đóng Connection sau khi sử dụng
        }

        public static void main(String[] args) {
            getConnection(); // Dùng để chạy thử kết nối
        }
    }