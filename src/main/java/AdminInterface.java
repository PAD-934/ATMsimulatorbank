package main.java;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class AdminInterface extends JFrame {
    // Custom colors for futuristic theme
    private static final Color NEON_CYAN = new Color(0, 255, 255);
    @SuppressWarnings("unused")
    private static final Color DARK_BG = new Color(20, 30, 40);
    private static final Color FIELD_BG = new Color(30, 40, 50);
    private static final Color BORDER_COLOR = new Color(0, 200, 255);
    
    private JTextField createFuturisticTextField() {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(FIELD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Border with glow
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setForeground(NEON_CYAN);
        field.setCaretColor(NEON_CYAN);
        field.setFont(new Font("Consolas", Font.PLAIN, 14));
        return field;
    }
    
    private JPasswordField createFuturisticPasswordField() {
        JPasswordField field = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(FIELD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Border with glow
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setForeground(NEON_CYAN);
        field.setCaretColor(NEON_CYAN);
        field.setFont(new Font("Consolas", Font.PLAIN, 14));
        return field;
    }
    
    private JButton createFuturisticButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button background with gradient based on button type
                Color startColor, endColor, glowColor;
                if (getText().contains("Create")) {
                    startColor = new Color(0, 100, 0);
                    endColor = new Color(0, 150, 0);
                    glowColor = new Color(0, 255, 0, 100);
                } else if (getText().contains("Update")) {
                    startColor = new Color(0, 100, 130);
                    endColor = new Color(0, 150, 200);
                    glowColor = new Color(0, 200, 255, 100);
                } else if (getText().contains("Delete")) {
                    startColor = new Color(130, 0, 0);
                    endColor = new Color(200, 0, 0);
                    glowColor = new Color(255, 0, 0, 100);
                } else if (getText().contains("Refresh")) {
                    startColor = new Color(100, 100, 0);
                    endColor = new Color(150, 150, 0);
                    glowColor = new Color(255, 255, 0, 100);
                } else {
                    startColor = new Color(50, 50, 50);
                    endColor = new Color(100, 100, 100);
                    glowColor = new Color(200, 200, 200, 100);
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Glow effect
                if (getModel().isPressed()) {
                    g2d.setColor(glowColor);
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 150));
                } else {
                    g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 50));
                }
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Text
                g2d.setFont(new Font("Consolas", Font.BOLD, 16));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        button.setPreferredSize(new Dimension(150, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private HashMap<String, Account> accounts;
    private DefaultTableModel accountModel;

    public AdminInterface(HashMap<String, Account> accounts) {
        this.accounts = accounts;
        
        setTitle("ATM Admin Interface");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createLoginPanel();
        createDashboardPanel();

        add(mainPanel);
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create futuristic gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 30, 40),
                    getWidth(), getHeight(), new Color(40, 50, 70));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle grid effect
                g2d.setColor(new Color(60, 70, 90, 30));
                int gridSize = 20;
                for (int i = 0; i < getWidth(); i += gridSize) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += gridSize) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        loginPanel.setBackground(new Color(20, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create glowing title
        JLabel titleLabel = new JLabel("ADMIN LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create glow effect
                g2d.setColor(new Color(0, 255, 255, 50));
                g2d.setFont(getFont().deriveFont(Font.BOLD, 26));
                g2d.drawString(getText(), 1, 26);
                g2d.drawString(getText(), -1, 26);
                
                // Main text
                g2d.setColor(new Color(0, 255, 255));
                g2d.drawString(getText(), 0, 25);
            }
        };
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 255, 255));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Create custom text fields
        JTextField usernameField = createFuturisticTextField();
        JPasswordField passwordField = createFuturisticPasswordField();
        JButton loginButton = createFuturisticButton("LOGIN");

        // Add components
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setForeground(new Color(0, 255, 255));
        userLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        loginPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setForeground(new Color(0, 255, 255));
        passLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        loginPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(_ -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (AdminAccount.authenticate(username, password)) {
                cardLayout.show(mainPanel, "dashboard");
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(loginPanel, "login");
    }

    @SuppressWarnings("unused")
    private void createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create futuristic gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 30, 40),
                    getWidth(), getHeight(), new Color(40, 50, 70));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle grid effect
                g2d.setColor(new Color(60, 70, 90, 30));
                int gridSize = 30;
                for (int i = 0; i < getWidth(); i += gridSize) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += gridSize) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        dashboardPanel.setBackground(new Color(20, 30, 40));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model for user accounts with non-editable cells
        DefaultTableModel accountModel = new DefaultTableModel(
            new String[]{"Account Number", "Account Holder", "Balance", "PIN"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable accountTable = new JTable(accountModel);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setBackground(new Color(30, 40, 50));
        accountTable.setForeground(Color.WHITE);
        accountTable.setGridColor(new Color(0, 200, 255, 100));
        accountTable.setFont(new Font("Consolas", Font.PLAIN, 14));
        accountTable.getTableHeader().setBackground(new Color(20, 30, 40));
        accountTable.getTableHeader().setForeground(NEON_CYAN);
        accountTable.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 14));
        
        JScrollPane accountScrollPane = new JScrollPane(accountTable) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        accountScrollPane.setBorder(BorderFactory.createEmptyBorder());
        accountScrollPane.getViewport().setBackground(new Color(30, 40, 50));

        // Create transaction history table
        DefaultTableModel transactionModel = new DefaultTableModel(
            new String[]{"Timestamp", "Type", "Amount", "Balance", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable transactionTable = new JTable(transactionModel);
        transactionTable.setBackground(new Color(30, 40, 50));
        transactionTable.setForeground(Color.WHITE);
        transactionTable.setGridColor(new Color(0, 200, 255, 100));
        transactionTable.setFont(new Font("Consolas", Font.PLAIN, 14));
        transactionTable.getTableHeader().setBackground(new Color(20, 30, 40));
        transactionTable.getTableHeader().setForeground(NEON_CYAN);
        transactionTable.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 14));
        
        JScrollPane transactionScrollPane = new JScrollPane(transactionTable) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        transactionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        transactionScrollPane.getViewport().setBackground(new Color(30, 40, 50));

        // Create split pane
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            accountScrollPane,
            transactionScrollPane);
        splitPane.setDividerLocation(200);

        // Create CRUD buttons panel with futuristic style
        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent background
                g2d.setColor(new Color(30, 40, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // Glowing border
                g2d.setColor(new Color(0, 200, 255, 100));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        crudPanel.setOpaque(false);
        
        JButton createButton = createFuturisticButton("Create Account");
        JButton updateButton = createFuturisticButton("Update Account");
        JButton deleteButton = createFuturisticButton("Delete Account");
        JButton refreshButton = createFuturisticButton("Refresh");
        JButton logoutButton = createFuturisticButton("Logout");

        // Add action listeners
        createButton.addActionListener(e -> showCreateAccountDialog());
        updateButton.addActionListener(e -> {
            int row = accountTable.getSelectedRow();
            if (row >= 0) {
                String accNum = (String) accountTable.getValueAt(row, 0);
                Account account = accounts.get(accNum);
                if (account != null) {
                    showUpdateAccountDialog(account);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account to update");
            }
        });

        deleteButton.addActionListener(e -> {
            int row = accountTable.getSelectedRow();
            if (row >= 0) {
                String accNum = (String) accountTable.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete account " + accNum + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    accounts.remove(accNum);
                    refreshData(accountModel, transactionModel);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account to delete");
            }
        });

        refreshButton.addActionListener(_ -> refreshData(accountModel, transactionModel));
        logoutButton.addActionListener(_ -> {
            cardLayout.show(mainPanel, "login");
            accountModel.setRowCount(0);
            transactionModel.setRowCount(0);
        });

        // Add buttons to panels
        crudPanel.add(createButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);
        crudPanel.add(refreshButton);
        crudPanel.add(logoutButton);

        // Create top panel for buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(crudPanel, BorderLayout.CENTER);

        dashboardPanel.add(topPanel, BorderLayout.NORTH);
        dashboardPanel.add(splitPane, BorderLayout.CENTER);

        // Add selection listener to the account table
        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = accountTable.getSelectedRow();
                if (row >= 0) {
                    String accNum = (String) accountTable.getValueAt(row, 0);
                    Account account = accounts.get(accNum);
                    if (account != null) {
                        displayTransactionHistory(account, transactionModel);
                    }
                }
            }
        });

        mainPanel.add(dashboardPanel, "dashboard");
        
        // Initial data load
        refreshData(accountModel, transactionModel);
    }

    private void refreshData(DefaultTableModel accountModel, DefaultTableModel transactionModel) {
        accountModel.setRowCount(0);
        for (Account account : accounts.values()) {
            accountModel.addRow(new Object[]{
                account.getAccountNumber(),
                account.getAccountHolder(),
                String.format("₱%.2f", account.getBalance()),
                account.getPin()
            });
        }
        transactionModel.setRowCount(0);
    }

    private void displayTransactionHistory(Account account, DefaultTableModel transactionModel) {
        transactionModel.setRowCount(0);
        List<Transaction> transactions = account.getTransactionHistory();
        for (Transaction transaction : transactions) {
            transactionModel.addRow(new Object[]{
                transaction.getFormattedTimestamp(),
                transaction.getType(),
                String.format("₱%.2f", transaction.getAmount()),
                String.format("₱%.2f", transaction.getBalanceAfter()),
                transaction.getDescription()
            });
        }
    }

    @SuppressWarnings("unused")
    private void showCreateAccountDialog() {
        JDialog dialog = new JDialog(this, "Create New Account", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create input fields
        JTextField nameField = new JTextField(20);
        JTextField accNumField = new JTextField(20);
        JPasswordField pinField = new JPasswordField(20);
        JTextField initialDepositField = new JTextField(20);

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Account Holder:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Account Number:"), gbc);
        gbc.gridx = 1;
        dialog.add(accNumField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("PIN:"), gbc);
        gbc.gridx = 1;
        dialog.add(pinField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Initial Deposit:"), gbc);
        gbc.gridx = 1;
        dialog.add(initialDepositField, gbc);

        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            try {
                String accNum = accNumField.getText();
                if (accounts.containsKey(accNum)) {
                    JOptionPane.showMessageDialog(dialog, "Account number already exists!");
                    return;
                }

                String name = nameField.getText();
                String pin = new String(pinField.getPassword());
                double initialDeposit = Double.parseDouble(initialDepositField.getText());

                if (name.isEmpty() || accNum.isEmpty() || pin.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required!");
                    return;
                }

                if (initialDeposit < 500) {
                    JOptionPane.showMessageDialog(dialog, "Initial deposit must be at least ₱500!");
                    return;
                }

                Account newAccount = new Account(accNum, pin, initialDeposit, name);
                accounts.put(accNum, newAccount);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Account created successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount for initial deposit!");
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(createButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    @SuppressWarnings("unused")
    private void showUpdateAccountDialog(Account account) {
        JDialog dialog = new JDialog(this, "Update Account", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create input fields
        JTextField nameField = new JTextField(account.getAccountHolder(), 20);
        JPasswordField pinField = new JPasswordField(20);
        pinField.setText(account.getPin());

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Account Holder:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("New PIN:"), gbc);
        gbc.gridx = 1;
        dialog.add(pinField, gbc);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String name = nameField.getText();
            String pin = new String(pinField.getPassword());

            if (name.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!");
                return;
            }

            // Update account details
            account.setPin(pin);
            account.setAccountHolder(name);
            dialog.dispose();
            DefaultTableModel transactionModel = null;
            refreshData(accountModel, transactionModel);
            JOptionPane.showMessageDialog(this, "Account updated successfully!");
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        dialog.add(updateButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}