package main.java;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class AdminInterface extends JFrame {
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
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Admin Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");

        // Style components
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 100, 0));
        loginButton.setForeground(Color.WHITE);

        // Add components
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);
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

    private void createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
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
        JScrollPane accountScrollPane = new JScrollPane(accountTable);

        // Create transaction history table
        DefaultTableModel transactionModel = new DefaultTableModel(
            new String[]{"Timestamp", "Type", "Amount", "Balance", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable transactionTable = new JTable(transactionModel);
        JScrollPane transactionScrollPane = new JScrollPane(transactionTable);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            accountScrollPane,
            transactionScrollPane);
        splitPane.setDividerLocation(200);

        // Create CRUD buttons panel
        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Create Account");
        JButton updateButton = new JButton("Update Account");
        JButton deleteButton = new JButton("Delete Account");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");

        // Style buttons
        createButton.setBackground(new Color(0, 100, 0));
        updateButton.setBackground(new Color(0, 0, 139));
        deleteButton.setBackground(new Color(139, 0, 0));
        createButton.setForeground(Color.WHITE);
        updateButton.setForeground(Color.WHITE);
        deleteButton.setForeground(Color.WHITE);

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