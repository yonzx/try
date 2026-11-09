package swing;

import java.sql.Connection;
import java.sql.DriverManager;

// Opens a connection to the MySQL database.
// Every screen calls Db.getConnection() when it needs the database.
public class Db {

    // Change these if your MySQL settings are different
    static final String URL = "jdbc:mysql://localhost:3306/karinderya";
    static final String USER = "root";
    static final String PASSWORD = "";   // XAMPP default is empty

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
