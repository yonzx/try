package swing;

import java.sql.*;

public class Db {
    private static final String URL =
        "jdbc:mysql://localhost:3306/karinderya?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; // XAMPP default is empty

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
