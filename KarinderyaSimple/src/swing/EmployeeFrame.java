package swing;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// Employee window: see orders and manage (add / edit / delete) the menu.
public class EmployeeFrame extends JFrame {

    private DefaultTableModel ordersModel =
        new DefaultTableModel(new String[] {"Order #", "Date/Time", "Total"}, 0);
    private JTable ordersTable = new JTable(ordersModel);

    private DefaultTableModel menuModel =
        new DefaultTableModel(new String[] {"ID", "Name", "Category", "Image", "Price"}, 0);
    private JTable menuTable = new JTable(menuModel);

    public EmployeeFrame(String username) {
        setTitle("Employee - " + username);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        ordersTable.setDefaultEditor(Object.class, null);   // read-only tables
        menuTable.setDefaultEditor(Object.class, null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Orders", makeOrdersTab());
        tabs.addTab("Manage Menu", makeMenuTab());

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logoutButton);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadOrders();
        loadMenu();
    }

    // ---------- Orders tab ----------
    private JPanel makeOrdersTab() {
        JButton refreshButton = new JButton("Refresh");
        JButton itemsButton = new JButton("View items");

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadOrders();
            }
        });
        itemsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewItems();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(refreshButton);
        buttons.add(itemsButton);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void loadOrders() {
        ordersModel.setRowCount(0);
        try {
            Connection con = Db.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT id, order_time, total FROM orders ORDER BY id DESC");
            while (rs.next()) {
                ordersModel.addRow(new Object[] {
                    rs.getInt("id"), rs.getTimestamp("order_time"), rs.getDouble("total")});
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void viewItems() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }
        int orderId = Integer.parseInt(ordersModel.getValueAt(row, 0).toString());
        String text = "";
        try {
            Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT menu.name, order_items.qty, order_items.price "
                + "FROM order_items LEFT JOIN menu ON menu.id = order_items.menu_id "
                + "WHERE order_items.order_id = ?");
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                text = text + rs.getString("name") + "  x" + rs.getInt("qty")
                    + "  @ " + String.format("%.2f", rs.getDouble("price")) + "\n";
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            text = ex.getMessage();
        }
        JOptionPane.showMessageDialog(this, text, "Order #" + orderId,
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- Manage Menu tab ----------
    private JPanel makeMenuTab() {
        JButton addButton = new JButton("Add dish");
        JButton editButton = new JButton("Edit selected");
        JButton deleteButton = new JButton("Delete selected");

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addDish();
            }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editDish();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteDish();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void loadMenu() {
        menuModel.setRowCount(0);
        try {
            Connection con = Db.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT id, name, category, image, price FROM menu ORDER BY id");
            while (rs.next()) {
                menuModel.addRow(new Object[] {
                    rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                    rs.getString("image"), rs.getDouble("price")});
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // Shows a small form. Returns the typed values, or null if cancelled.
    private String[] askDishDetails(String title, String name, String category,
                                    String image, String price) {
        JTextField nameField = new JTextField(name);
        JComboBox<String> categoryBox =
            new JComboBox<String>(new String[] {"Meat", "Veggies", "Fried Dish", "Soup"});
        categoryBox.setSelectedItem(category);
        JTextField imageField = new JTextField(image);
        JTextField priceField = new JTextField(price);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Category"));
        form.add(categoryBox);
        form.add(new JLabel("Image file name"));
        form.add(imageField);
        form.add(new JLabel("Price"));
        form.add(priceField);

        int answer = JOptionPane.showConfirmDialog(this, form, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String[] {nameField.getText().trim(),
            categoryBox.getSelectedItem().toString(),
            imageField.getText().trim(), priceField.getText().trim()};
    }

    private void addDish() {
        String[] v = askDishDetails("Add dish", "", "Meat", "", "0");
        if (v == null) {
            return;
        }
        try {
            double price = Double.parseDouble(v[3]);
            Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO menu (name, category, image, price) VALUES (?, ?, ?, ?)");
            ps.setString(1, v[0]);
            ps.setString(2, v[1]);
            ps.setString(3, v[2]);
            ps.setDouble(4, price);
            ps.executeUpdate();
            ps.close();
            con.close();
            loadMenu();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a number.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void editDish() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a dish first.");
            return;
        }
        int id = Integer.parseInt(menuModel.getValueAt(row, 0).toString());
        String[] v = askDishDetails("Edit dish #" + id,
            menuModel.getValueAt(row, 1).toString(),
            menuModel.getValueAt(row, 2).toString(),
            String.valueOf(menuModel.getValueAt(row, 3)),
            menuModel.getValueAt(row, 4).toString());
        if (v == null) {
            return;
        }
        try {
            double price = Double.parseDouble(v[3]);
            Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE menu SET name = ?, category = ?, image = ?, price = ? WHERE id = ?");
            ps.setString(1, v[0]);
            ps.setString(2, v[1]);
            ps.setString(3, v[2]);
            ps.setDouble(4, price);
            ps.setInt(5, id);
            ps.executeUpdate();
            ps.close();
            con.close();
            loadMenu();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a number.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteDish() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a dish first.");
            return;
        }
        int id = Integer.parseInt(menuModel.getValueAt(row, 0).toString());
        int answer = JOptionPane.showConfirmDialog(this, "Delete this dish?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM menu WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            con.close();
            loadMenu();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
            "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
