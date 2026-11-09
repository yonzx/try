package swing;

import javax.swing.*;
import java.awt.*;

public class EmployeeFrame extends JFrame {
    public EmployeeFrame(String user) {
        super("Employee - " + user);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> {
        dispose();
        new LoginFrame().setVisible(true);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logout);

        add(top, BorderLayout.NORTH);
        add(new AdminPanel(() -> { }), BorderLayout.CENTER);   // () -> { } means "do nothing"

        setSize(900, 600);
        setLocationRelativeTo(null);
    }
}