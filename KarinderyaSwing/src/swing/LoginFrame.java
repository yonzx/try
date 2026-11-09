package swing;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        super("Karinderya Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTextField user = new JTextField(16);
        JPasswordField pass = new JPasswordField(16);
        JButton login = new JButton("Login");
        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setForeground(Color.RED);

        JLabel title = new JLabel("Alexa's Karinderya", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
form.setBackground(new Color(173, 216, 230));
form.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(10, 131, 10), 5),
    BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        form.add(title);
        form.add(new JLabel("Username"));
        form.add(user);
        form.add(new JLabel("Password"));
        form.add(pass);
        form.add(login);
        form.add(msg);
        add(form);

        Runnable attempt = () -> {
            String sql = "SELECT username FROM users WHERE username=? AND password=?";
            try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user.getText().trim());
                ps.setString(2, new String(pass.getPassword()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString(1);
                        dispose();
                        new MainFrame(name).setVisible(true);
                    } else {
                        msg.setText("Wrong username or password.");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);
            }
        };
        login.addActionListener(e -> attempt.run());
        getRootPane().setDefaultButton(login);

        pack();
        setLocationRelativeTo(null);
    }
}
