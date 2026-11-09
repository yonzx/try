package swing;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

// Login window for employees.
public class LoginFrame extends JFrame {

    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JLabel messageLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("Employee Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("Karinderya Employee Login", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 20));

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(10, 131, 10));
        loginButton.setForeground(Color.WHITE);

        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // When the button is clicked, run checkLogin()
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkLogin();
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        panel.setBackground(new Color(173, 216, 230));   // light blue
        panel.add(title);
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(messageLabel);

        add(panel);
        getRootPane().setDefaultButton(loginButton);   // Enter key = click Login
        setSize(340, 360);
        setLocationRelativeTo(null);
    }

    // Looks for the username and password in the "users" table
    private void checkLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT username FROM users WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // correct login: close this window, open the employee window
                dispose();
                new EmployeeFrame(username).setVisible(true);
            } else {
                messageLabel.setText("Wrong username or password.");
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
