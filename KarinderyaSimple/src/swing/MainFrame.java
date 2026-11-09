package swing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// Customer window: pick dishes by category and place an order.
public class MainFrame extends JFrame {

    private JTabbedPane tabs = new JTabbedPane();          // Meat, Veggies, ...
    private DefaultTableModel cartModel;                   // rows in the order list
    private JTable cartTable;
    private JLabel totalLabel = new JLabel("Total: 0.00");

    public MainFrame() {
        setTitle("Alexa's Karinderya - Order");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(tabs, BorderLayout.CENTER);              // menu in the middle
        add(makeOrderPanel(), BorderLayout.EAST);    // order list on the right

        loadMenu();
    }

    // ---------- the order list on the right side ----------
    private JPanel makeOrderPanel() {
        cartModel = new DefaultTableModel(
            new String[] {"ID", "Dish", "Qty", "Price", "Subtotal"}, 0);
        cartTable = new JTable(cartModel);
        cartTable.setDefaultEditor(Object.class, null);   // user can't type in the table

        JLabel heading = new JLabel("Your Order");
        heading.setFont(new Font("Serif", Font.BOLD, 20));

        JButton removeButton = new JButton("Remove selected");
        JButton clearButton = new JButton("Clear");
        JButton confirmButton = new JButton("Confirm Order");
        confirmButton.setBackground(new Color(10, 131, 10));
        confirmButton.setForeground(Color.WHITE);

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelected();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cartModel.setRowCount(0);
                updateTotal();
            }
        });
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                confirmOrder();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(removeButton);
        buttons.add(clearButton);

        JPanel bottom = new JPanel(new GridLayout(0, 1, 4, 4));
        bottom.add(totalLabel);
        bottom.add(buttons);
        bottom.add(confirmButton);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- load the dishes from the "menu" table ----------
    private void loadMenu() {
        tabs.removeAll();
        String[] categories = {"Meat", "Veggies", "Fried Dish", "Soup"};

        try {
            Connection con = Db.getConnection();

            for (int i = 0; i < categories.length; i++) {
                JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
                grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                PreparedStatement ps = con.prepareStatement(
                    "SELECT id, name, image, price FROM menu WHERE category = ?");
                ps.setString(1, categories[i]);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String image = rs.getString("image");
                    double price = rs.getDouble("price");
                    grid.add(makeCard(id, name, image, price));
                }
                rs.close();
                ps.close();

                // put the grid at the top so the cards do not stretch
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.add(grid, BorderLayout.NORTH);
                tabs.addTab(categories[i], new JScrollPane(wrapper));
            }
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- one dish card (picture, name, price, Add button) ----------
    private JPanel makeCard(final int id, final String name, String image, final double price) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel picture = new JLabel("No image", SwingConstants.CENTER);
        picture.setPreferredSize(new Dimension(180, 120));
        File file = new File("images", image);
        if (image != null && file.exists()) {
            ImageIcon icon = new ImageIcon(file.getPath());
            Image small = icon.getImage().getScaledInstance(180, 120, Image.SCALE_SMOOTH);
            picture.setText("");
            picture.setIcon(new ImageIcon(small));
        }

        JLabel nameLabel = new JLabel(name + "  -  " + String.format("%.2f", price),
            SwingConstants.CENTER);

        final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JButton addButton = new JButton("Add to order");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int qty = (Integer) qtySpinner.getValue();
                addToCart(id, name, qty, price);
            }
        });

        JPanel row = new JPanel(new FlowLayout());
        row.add(new JLabel("Qty:"));
        row.add(qtySpinner);
        row.add(addButton);

        JPanel bottom = new JPanel(new GridLayout(0, 1));
        bottom.add(nameLabel);
        bottom.add(row);

        card.add(picture, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ---------- order list actions ----------
    private void addToCart(int id, String name, int qty, double price) {
        // if the dish is already in the list, just add to its quantity
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int rowId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            if (rowId == id) {
                int oldQty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                int newQty = oldQty + qty;
                cartModel.setValueAt(newQty, i, 2);
                cartModel.setValueAt(newQty * price, i, 4);
                updateTotal();
                return;
            }
        }
        // otherwise add a new row
        cartModel.addRow(new Object[] {id, name, qty, price, qty * price});
        updateTotal();
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cartModel.removeRow(row);
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total = total + Double.parseDouble(cartModel.getValueAt(i, 4).toString());
        }
        totalLabel.setText("Total: " + String.format("%.2f", total));
    }

    // ---------- save the order in MySQL ----------
    private void confirmOrder() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Your order is empty.");
            return;
        }

        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total = total + Double.parseDouble(cartModel.getValueAt(i, 4).toString());
        }

        try {
            Connection con = Db.getConnection();

            // 1) save the order itself and get its new id number
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO orders (total) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setDouble(1, total);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int orderId = keys.getInt(1);
            keys.close();
            ps.close();

            // 2) save each dish of the order
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                PreparedStatement item = con.prepareStatement(
                    "INSERT INTO order_items (order_id, menu_id, qty, price) VALUES (?, ?, ?, ?)");
                item.setInt(1, orderId);
                item.setInt(2, Integer.parseInt(cartModel.getValueAt(i, 0).toString()));
                item.setInt(3, Integer.parseInt(cartModel.getValueAt(i, 2).toString()));
                item.setDouble(4, Double.parseDouble(cartModel.getValueAt(i, 3).toString()));
                item.executeUpdate();
                item.close();
            }
            con.close();

            JOptionPane.showMessageDialog(this,
                "Order #" + orderId + " saved.\nTotal: " + String.format("%.2f", total));
            cartModel.setRowCount(0);
            updateTotal();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
