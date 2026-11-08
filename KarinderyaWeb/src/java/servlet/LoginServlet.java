package servlet;

import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String URL  = "jdbc:mysql://localhost:3306/karinderya";
    private static final String USER = "root";
    private static final String PASS = "";   // your MySQL password

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isValid(username, password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            response.sendRedirect("index.html");          // correct -> go to index
        } else {
            response.sendRedirect("login.html?error=1");  // wrong -> try again
        }
    }

    private boolean isValid(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();   // true if a matching row exists
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}