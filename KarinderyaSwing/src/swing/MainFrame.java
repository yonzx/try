package swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    static class Line {
        int id; String name; int qty; double price;
    }

    private final List<Line> cart = new ArrayList<>();
    private final DefaultTableModel cartModel =
        new DefaultTableModel(new String[]{"Dish", "Qty", "Price", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel totalLabel = new JLabel("Total: ₱0.00");
    private final JTabbedPane categoryTabs = new JTabbedPane();

    public MainFrame(String user) {
        super("Alexa's Karinderya - logged in as " + user);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.add(categoryTabs, BorderLayout.CENTER);
        menuPanel.add(buildOrderPanel(), BorderLayout.EAST);

        JTabbedPane main = new JTabbedPane();
        main.addTab("Menu & Order", menuPanel);
        main.addTab("Admin", new AdminPanel(this::reloadMenu));
        add(main);

        reloadMenu();
        setSize(1150, 720);
        setLocationRelativeTo(null);
    }

    private JPanel buildOrderPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.setPreferredSize(new Dimension(340, 0));

        JLabel head = new JLabel("Your Order");
        head.setFont(head.getFont().deriveFont(Font.BOLD, 16f));
        p.add(head, BorderLayout.NORTH);
        p.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JButton remove = new JButton("Remove selected");
        JButton clear = new JButton("Clear");
        JButton confirm = new JButton("Confirm Order");
        remove.addActionListener(e -> {
            int r = cartTable.getSelectedRow();
            if (r >= 0) { cart.remove(r); refreshCart(); }
        });
        clear.addActionListener(e -> { cart.clear(); refreshCart(); });
        confirm.addActionListener(e -> confirmOrder());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.add(remove);
        buttons.add(clear);
        JPanel south = new JPanel(new GridLayout(0, 1, 4, 6));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        south.add(totalLabel);
        south.add(buttons);
        south.add(confirm);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    void reloadMenu() {
        categoryTabs.removeAll();
        Map<String, JPanel> grids = new LinkedHashMap<>();
        for (String c : new String[]{"Meat", "Veggies", "Fried Dish", "Soup"}) {
            grids.put(c, newGrid());
        }
        String sql = "SELECT id, name, category, image, price FROM menu ORDER BY name";
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String cat = rs.getString("category");
                if (cat == null || cat.isBlank()) cat = "Other";
                grids.computeIfAbsent(cat, k -> newGrid()).add(
                    card(rs.getInt("id"), rs.getString("name"),
                         rs.getString("image"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            error(e);
        }
        for (Map.Entry<String, JPanel> e : grids.entrySet()) {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.add(e.getValue(), BorderLayout.NORTH);
            categoryTabs.addTab(e.getKey(), new JScrollPane(wrap));
        }
        categoryTabs.revalidate();
        categoryTabs.repaint();
    }

    private JPanel newGrid() {
        JPanel g = new JPanel(new GridLayout(0, 3, 12, 12));
        g.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return g;
    }

    private JPanel card(int id, String name, String image, double price) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel pic = new JLabel("No image", SwingConstants.CENTER);
        pic.setPreferredSize(new Dimension(160, 110));
        ImageIcon icon = loadIcon(image);
        if (icon != null) { pic.setText(null); pic.setIcon(icon); }

        JLabel info = new JLabel("<html><center><b>" + esc(name) + "</b><br>₱"
            + String.format("%.2f", price) + "</center></html>", SwingConstants.CENTER);

        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JButton add = new JButton("Add to order");
        add.addActionListener(e -> addToCart(id, name, (Integer) qty.getValue(), price));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        row.add(new JLabel("Qty:"));
        row.add(qty);
        row.add(add);

        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.add(info, BorderLayout.CENTER);
        bottom.add(row, BorderLayout.SOUTH);

        p.add(pic, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private ImageIcon loadIcon(String file) {
        if (file == null || file.isBlank()) return null;
        File f = new File("web", file);
        if (!f.exists()) f = new File("images", file);
        if (!f.exists()) return null;
        ImageIcon raw = new ImageIcon(f.getPath());
        if (raw.getIconWidth() <= 0) return null;
        return new ImageIcon(raw.getImage().getScaledInstance(160, 110, Image.SCALE_SMOOTH));
    }

    private void addToCart(int id, String name, int qty, double price) {
        for (Line l : cart) {
            if (l.id == id) { l.qty += qty; refreshCart(); return; }
        }
        Line l = new Line();
        l.id = id; l.name = name; l.qty = qty; l.price = price;
        cart.add(l);
        refreshCart();
    }

    private double total() {
        double t = 0;
        for (Line l : cart) t += l.qty * l.price;
        return t;
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (Line l : cart) {
            cartModel.addRow(new Object[]{l.name, l.qty,
                String.format("%.2f", l.price), String.format("%.2f", l.qty * l.price)});
        }
        totalLabel.setText(String.format("Total: ₱%.2f", total()));
    }

    private void confirmOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your order is empty.");
            return;
        }
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                int orderId = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orders(total) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDouble(1, total());
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (k.next()) orderId = k.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO order_items(order_id, menu_id, qty, price) VALUES(?,?,?,?)")) {
                    for (Line l : cart) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, l.id);
                        ps.setInt(3, l.qty);
                        ps.setDouble(4, l.price);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                JOptionPane.showMessageDialog(this,
                    "Order #" + orderId + " saved. Total: " + String.format("₱%.2f", total()));
                cart.clear();
                refreshCart();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
            "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
