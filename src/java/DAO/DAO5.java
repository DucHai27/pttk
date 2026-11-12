package DAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Đọc cấu hình JDBC và mở kết nối MySQL cho các DAO con. */
public class DAO5 {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream input = DAO5.class.getResourceAsStream("/db.properties")) {
            if (input != null) {
                InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
                CONFIG.load(reader);
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** DAO gọi hàm này trong try-with-resources để tự đóng kết nối khi dùng xong. */
    protected Connection getConnection() throws SQLException {
        String url = setting("DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/restman5?characterEncoding=UTF-8");

        Properties properties = new Properties();
        properties.setProperty("user", setting("DB_USER", "db.user", "root"));
        properties.setProperty("password", setting("DB_PASSWORD", "db.password", ""));
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "10000");

        return DriverManager.getConnection(url, properties);
    }

    /** Ưu tiên biến môi trường; nếu chưa đặt thì đọc từ db.properties. */
    private static String setting(String env, String key, String fallback) {
        String value = System.getenv(env);
        if (value != null) {
            return value;
        }
        return CONFIG.getProperty(key, fallback);
    }
}
