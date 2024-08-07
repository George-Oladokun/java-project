import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class InventoryManagementApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField registerFirstNameField, registerLastNameField, registerUsernameField, registerPasswordField;
    private JTextField loginUsernameField, loginPasswordField;
    private JTextField productNameField, productQuantityField;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private Connection connection;
    private int loggedInUserId;

    public InventoryManagementApp() {
        setTitle("Inventory Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        connectToDatabase();
        initComponents();
        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "Login");

        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_db", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        initNavbar();
        initRegisterPanel();
        initLoginPanel();
        initDashboardPanel();
    }

    private void initNavbar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem registerMenuItem = new JMenuItem("Register");
        registerMenuItem.addActionListener(e -> cardLayout.show(mainPanel, "Register"));
        JMenuItem loginMenuItem = new JMenuItem("Login");
        loginMenuItem.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        menu.add(registerMenuItem);
        menu.add(loginMenuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void initRegisterPanel() {
        JPanel registerPanel = new JPanel(new GridLayout(5, 1));
        registerPanel.add(new JLabel("First Name:"));
        registerFirstNameField = new JTextField();
        registerPanel.add(registerFirstNameField);
        registerPanel.add(new JLabel("Last Name:"));
        registerLastNameField = new JTextField();
        registerPanel.add(registerLastNameField);
        registerPanel.add(new JLabel("Username:"));
        registerUsernameField = new JTextField();
        registerPanel.add(registerUsernameField);
        registerPanel.add(new JLabel("Password:"));
        registerPasswordField = new JPasswordField();
        registerPanel.add(registerPasswordField);
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegisterAction());
        registerPanel.add(registerButton);
        mainPanel.add(registerPanel, "Register");
    }

    private void initLoginPanel() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        loginUsernameField = new JTextField();
        loginPanel.add(loginUsernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPasswordField = new JPasswordField();
        loginPanel.add(loginPasswordField);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginAction());
        loginPanel.add(loginButton);
        mainPanel.add(loginPanel, "Login");
    }

    private void initDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
    
        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(new AddProductAction());
    
        JPanel inputPanel = new JPanel(new GridLayout(1, 4));
        productNameField = new JTextField();
        productQuantityField = new JTextField();
        inputPanel.add(new JLabel("Product Name:"));
        inputPanel.add(productNameField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(productQuantityField);
    
        tableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Quantity", "Edit", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; 
            }
        };
    
        productTable = new JTable(tableModel);
        productTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        productTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), true));
        productTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        productTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), false));
    
        JScrollPane tableScrollPane = new JScrollPane(productTable);
    
        dashboardPanel.add(inputPanel, BorderLayout.NORTH);
        dashboardPanel.add(addButton, BorderLayout.CENTER);
        dashboardPanel.add(tableScrollPane, BorderLayout.SOUTH);
    
        mainPanel.add(dashboardPanel, "Dashboard");
    }
class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String label;
    private boolean isPushed;
    private boolean isEditAction;

    public ButtonEditor(JCheckBox checkBox, boolean isEditAction) {
        super(checkBox);
        this.isEditAction = isEditAction;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            if (isEditAction) {
                editProductAction(productTable.getSelectedRow());
            } else {
                deleteProductAction(productTable.getSelectedRow());
            }
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
private void editProductAction(int row) {
    int productId = (int) tableModel.getValueAt(row, 0);
    String name = (String) tableModel.getValueAt(row, 1);
    int quantity = (int) tableModel.getValueAt(row, 2);

    productNameField.setText(name);
    productQuantityField.setText(String.valueOf(quantity));

    int result = JOptionPane.showConfirmDialog(null, new Object[]{new JLabel("Edit Product"), productNameField, productQuantityField}, "Edit Product", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String newName = productNameField.getText().trim();
        String newQuantityText = productQuantityField.getText().trim();
        if (newName.isEmpty() || newQuantityText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Product name and quantity cannot be empty.");
            return;
        }
        int newQuantity;
        try {
            newQuantity = Integer.parseInt(newQuantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number for quantity.");
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE products SET name = ?, quantity = ? WHERE id = ?");
            ps.setString(1, newName);
            ps.setInt(2, newQuantity);
            ps.setInt(3, productId);
            ps.executeUpdate();
            loadProducts(); // Refresh the product list
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }
}

private void deleteProductAction(int row) {
    int productId = (int) tableModel.getValueAt(row, 0);

    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this product?", "Delete Product", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM products WHERE id = ?");
            ps.setInt(1, productId);
            ps.executeUpdate();
            loadProducts(); // Refresh the product list
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }
}
    

    private void loadProducts() {
        try {
            tableModel.setRowCount(0); 
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM products WHERE user_id = ?");
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] row = {rs.getInt("id"), rs.getString("name"), rs.getInt("quantity"), "Edit", "Delete"};
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private class RegisterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String firstName = registerFirstNameField.getText();
            String lastName = registerLastNameField.getText();
            String username = registerUsernameField.getText();
            String password = new String(((JPasswordField) registerPasswordField).getPassword());
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO users (first_name, last_name, username, password) VALUES (?, ?, ?, ?)");
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, username);
                ps.setString(4, password);
                ps.executeUpdate();
                cardLayout.show(mainPanel, "Login");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class LoginAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = loginUsernameField.getText();
            String password = new String(((JPasswordField) loginPasswordField).getPassword());
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?");
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    loggedInUserId = rs.getInt("id");
                    loadProducts();
                    cardLayout.show(mainPanel, "Dashboard");
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid login credentials.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class AddProductAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String name = productNameField.getText().trim();
            String quantityText = productQuantityField.getText().trim();
    
            // Validate inputs
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Product name cannot be empty.");
                return;
            }
            if (quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Product quantity cannot be empty.");
                return;
            }
    
            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number for quantity.");
                return;
            }
    
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO products (name, quantity, user_id) VALUES (?, ?, ?)");
                ps.setString(1, name);
                ps.setInt(2, quantity);
                ps.setInt(3, loggedInUserId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Product added successfully!");
                loadProducts(); // Refresh the product list
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        }
    }
    

    public static void main(String[] args) {
        new InventoryManagementApp();
    }
}
