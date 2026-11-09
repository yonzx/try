package swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JTabbedPane {

    private final Runnable onMenuChanged;

    private final DefaultTableModel ordersModel =
        new DefaultTableModel(new String[]{"Order #", "Date/Time", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    private final JTable ordersTable = new JTable(ordersModel);

    private final DefaultTableModel menuModel =
        new DefaultTableModel(new String[]{"ID", "Name", "Category", "Image", "Price"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    private final JTable menuTable = new JTable(menuModel);

    public AdminPanel(Runnable onMenuChanged) {
        this.onMenuChanged = onMenuChanged;
        addTab("Orders", buildOrdersTab());
        addTab("Manage Menu", buildMenuTab());
        loadOrders();
        loadMenu();
    }

    private JPanel buildOrdersTab() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton items = new JButton("View items");
        refresh.addActionListener(e -> loadOrders());
        items.addActionListener(e -> viewItems());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(refresh);
        bar.add(items);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildMenuTab() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        JButton add = new JButton("Add dish");
        JButton edit = new JButton("Edit selected");
        JButton del = new JButton("Delete selected");
        add.addActionListener(e -> addDish());
        edit.addActionListener(e -> editDish());
        del.addActionListener(e -> deleteDish());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(add);
        bar.add(edit);
        bar.add(del);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private void loadOrders() {
        ordersModel.setRowCount(0);
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, order_time, total FROM orders ORDER BY id DESC")) {
            while (rs.next()) {
                ordersModel.addRow(new Object[]{rs.getInt(1), rs.getTimestamp(2),
                    String.format("%.2f", rs.getDouble(3))});
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    private void loadMenu() {
        menuModel.setRowCount(0);
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, name, category, image, price FROM menu ORDER BY id")) {
            while (rs.next()) {
                menuModel.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                    rs.getString(3), rs.getString(4), rs.getDouble(5)});
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    private void viewItems() {
        int r = ordersTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }
        int orderId = (Integer) ordersModel.getValueAt(r, 0);
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT m.name, oi.qty, oi.price FROM order_items oi "
                   + "LEFT JOIN menu m ON m.id = oi.menu_id WHERE oi.order_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("  x").append(rs.getInt(2))
                      .append("  @ ₱").append(String.format("%.2f", rs.getDouble(3)))
                      .append("\n");
                }
            }
        } catch (SQLException e) {
            sb.append(e.getMessage());
        }
        JOptionPane.showMessageDialog(this, sb.toString(),
            "Order #" + orderId, JOptionPane.INFORMATION_MESSAGE);
    }

    private String[] showForm(String title, String[] init) {
        JTextField name = new JTextField(init[0], 18);
        JTextField cat = new JTextField(init[1], 18);
        JTextField img = new JTextField(init[2], 18);
        JTextField price = new JTextField(init[3], 18);
        JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
        f.add(new JLabel("Name")); f.add(name);
        f.add(new JLabel("Category (Meat, Veggies, Fried Dish, Soup)")); f.add(cat);
        f.add(new JLabel("Image file name")); f.add(img);
        f.add(new JLabel("Price")); f.add(price);
        int ok = JOptionPane.showConfirmDialog(this, f, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return null;
        return new String[]{name.getText().trim(), cat.getText().trim(),
                            img.getText().trim(), price.getText().trim()};
    }

    private void addDish() {
        String[] v = showForm("Add dish", new String[]{"", "", "", "0"});
        if (v == null) return;
        try {
            double price = Double.parseDouble(v[3]);
            if (exec("INSERT INTO menu(name, category, image, price) VALUES(?,?,?,?)",
                     v[0], v[1], v[2], price)) afterChange();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a number.");
        }
    }

    private void editDish() {
        int r = menuTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a dish first.");
            return;
        }
        int id = (Integer) menuModel.getValueAt(r, 0);
        String[] init = {
            String.valueOf(menuModel.getValueAt(r, 1)),
            String.valueOf(menuModel.getValueAt(r, 2)),
            String.valueOf(menuModel.getValueAt(r, 3)),
            String.valueOf(menuModel.getValueAt(r, 4))
        };
        String[] v = showForm("Edit dish #" + id, init);
        if (v == null) return;
        try {
            double price = Double.parseDouble(v[3]);
            if (exec("UPDATE menu SET name=?, category=?, image=?, price=? WHERE id=?",
                     v[0], v[1], v[2], price, id)) afterChange();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a number.");
        }
    }

    private void deleteDish() {
        int r = menuTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a dish first.");
            return;
        }
        int id = (Integer) menuModel.getValueAt(r, 0);
        int ok = JOptionPane.showConfirmDialog(this, "Delete this dish?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION && exec("DELETE FROM menu WHERE id=?", id)) {
            afterChange();
        }
    }

    private void afterChange() {
        loadMenu();
        onMenuChanged.run();
    }

    private boolean exec(String sql, Object... args) {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            error(e);
            return false;
        }
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
            "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
