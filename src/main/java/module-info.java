module com.db_quanlydiem.db_quanlydiem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.graphics;
    // Thư viện MySQL

    // Mở quyền cho JavaFX nạp FXML
    opens com.db_quanlydiem to javafx.fxml;

    // QUAN TRỌNG: Xuất package chứa Main để JavaFX có thể chạy được
    exports com.db_quanlydiem;

    // Xuất các package khác để dùng trong ứng dụng
    exports com.db_quanlydiem.controller;
    opens com.db_quanlydiem.controller to javafx.fxml;

    exports com.db_quanlydiem.model;
    exports com.db_quanlydiem.dao;
}