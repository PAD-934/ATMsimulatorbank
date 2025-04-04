package main.java;
// package src\main\java;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.Timer;

public class ATMInterface extends JFrame {
    private HashMap<String, Account> accounts;
    private Account currentAccount;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public ATMInterface() {
        accounts = new HashMap<>();
        loadAccounts();
        
        setTitle("ATM Simulation System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(100, 100, 100)); // Darker gray for ATM look

        // Adjust font sizes and colors for ATM look
        UIManager.put("Label.font", new Font("Consolas", Font.BOLD, 20));
        UIManager.put("Button.font", new Font("Consolas", Font.BOLD, 18));
        UIManager.put("TextField.font", new Font("Consolas", Font.BOLD, 18));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(100, 100, 100));
        
        // Add border to simulate ATM frame
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 20),
            BorderFactory.createBevelBorder(BevelBorder.RAISED)
        ));
        
        createLoginPanel();
        createSignUpPanel();
        createMainMenuPanel();
        
        // Add ESC key listener to exit
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
        setFocusable(true);
        
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHardwarePanel() {
        JPanel hardwarePanel = new JPanel(new GridLayout(3, 1, 15, 15));
        hardwarePanel.setBackground(new Color(100, 100, 100));
        hardwarePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create slots with 3D effect
        String[] slots = {"RECEIPT", "CARD", "CASH"};
        Color[] slotColors = {
            new Color(50, 50, 50),  // Receipt slot
            new Color(40, 40, 40),  // Card slot
            new Color(30, 30, 30)   // Cash dispenser
        };

        for (int i = 0; i < slots.length; i++) {
            JPanel slotPanel = new JPanel(new BorderLayout());
            slotPanel.setBackground(slotColors[i]);
            slotPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(15, 30, 15, 30)
            ));

            JLabel label = new JLabel(slots[i]);
            label.setForeground(new Color(0, 255, 0));
            label.setFont(new Font("Consolas", Font.BOLD, 16));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Add green LED indicator
            JPanel ledPanel = new JPanel();
            ledPanel.setBackground(new Color(0, 100, 0));
            ledPanel.setPreferredSize(new Dimension(10, 10));
            
            slotPanel.add(label, BorderLayout.CENTER);
            slotPanel.add(ledPanel, BorderLayout.EAST);
            
            hardwarePanel.add(slotPanel);
        }

        return hardwarePanel;
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(new Color(200, 200, 200));

        // Screen panel (center)
        JPanel screenPanel = new JPanel(new GridBagLayout());
        screenPanel.setBackground(Color.BLACK);
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 15),
            BorderFactory.createLineBorder(Color.BLACK, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title with ATM-style font
        JLabel titleLabel = new JLabel("Welcome to ATM System");
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Input fields with ATM-style
        JLabel accLabel = new JLabel("ACCOUNT NUMBER:");
        accLabel.setForeground(Color.GREEN);
        accLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        
        JTextField accField = new JTextField(15);
        styleTextField(accField);

        JLabel pinLabel = new JLabel("ENTER PIN:");
        pinLabel.setForeground(Color.GREEN);
        pinLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        
        JPasswordField pinField = new JPasswordField(15);
        styleTextField(pinField);

        // Add components to screen
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        screenPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        screenPanel.add(accLabel, gbc);
        
        gbc.gridy = 2;
        screenPanel.add(accField, gbc);

        gbc.gridy = 3;
        screenPanel.add(pinLabel, gbc);

        gbc.gridy = 4;
        screenPanel.add(pinField, gbc);

        // Side buttons panel (right)
        JPanel rightButtonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        rightButtonsPanel.setBackground(new Color(200, 200, 200));
        
        JButton loginButton = createATMButton("LOGIN", new Color(0, 100, 0));
        JButton signUpButton = createATMButton("NEW ACCOUNT", new Color(0, 0, 100));

        rightButtonsPanel.add(loginButton);
        rightButtonsPanel.add(signUpButton);

        // Keypad panel (bottom)
        JPanel keypadPanel = createKeypad();

        // Add all panels to main login panel
        loginPanel.add(screenPanel, BorderLayout.CENTER);
        loginPanel.add(rightButtonsPanel, BorderLayout.EAST);
        loginPanel.add(keypadPanel, BorderLayout.SOUTH);

        // Action listeners remain the same
        loginButton.addActionListener(_ -> {
            String accNum = accField.getText();
            String pin = new String(pinField.getPassword());
            
            if (validateLogin(accNum, pin)) {
                currentAccount = accounts.get(accNum);
                // Update welcome label in main menu
                updateWelcomeLabel();
                cardLayout.show(mainPanel, "mainMenu");
                // Clear fields after successful login
                accField.setText("");
                pinField.setText("");
            } else {
                JOptionPane.showMessageDialog(loginPanel, 
                    "Invalid Account Number or PIN", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        signUpButton.addActionListener(_ -> {
            cardLayout.show(mainPanel, "signup");
            accField.setText("");
            pinField.setText("");
        });

        mainPanel.add(loginPanel, "login");
    }

    // Helper methods for UI components
    private JButton createATMButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Consolas", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createBevelBorder(BevelBorder.RAISED)
        ));
        
        // Add hover and press effects
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
            public void mousePressed(MouseEvent e) {
                button.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
            public void mouseReleased(MouseEvent e) {
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                    BorderFactory.createBevelBorder(BevelBorder.RAISED)
                ));
            }
        });
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(Color.BLACK);
        field.setForeground(Color.GREEN);
        field.setFont(new Font("Consolas", Font.BOLD, 16));
        field.setCaretColor(Color.GREEN);
        field.setBorder(BorderFactory.createLineBorder(Color.GREEN));
    }

    private JPanel createKeypad() {
        JPanel keypad = new JPanel(new GridLayout(4, 3, 8, 8));
        keypad.setBackground(new Color(50, 50, 50));
        keypad.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] keys = {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "*", "0", "#"
        };

        for (String key : keys) {
            JButton button = new JButton(key);
            button.setPreferredSize(new Dimension(60, 60));
            button.setBackground(new Color(70, 70, 70));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Consolas", Font.BOLD, 20));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createBevelBorder(BevelBorder.RAISED)
            ));
            
            // Add click effect and sound
            button.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    button.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                }
                public void mouseReleased(MouseEvent e) {
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                        BorderFactory.createBevelBorder(BevelBorder.RAISED)
                    ));
                    playSound("button");
                }
            });
            
            keypad.add(button);
        }

        return keypad;
    }

    // Add this new method to update welcome label
    private void updateWelcomeLabel() {
        JPanel menuPanel = (JPanel) mainPanel.getComponent(2); // Get main menu panel
        Component[] components = menuPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setText("Welcome, " + currentAccount.getAccountHolder());
                break;
            }
        }
    }

    private void createSignUpPanel() {
        JPanel signUpPanel = new JPanel(new BorderLayout());
        signUpPanel.setBackground(new Color(200, 200, 200));

        // Create ATM screen panel
        JPanel screenPanel = new JPanel(new GridBagLayout());
        screenPanel.setBackground(Color.BLACK);
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY),
            BorderFactory.createLineBorder(Color.BLACK, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("CREATE NEW ACCOUNT");
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Input fields
        JTextField nameField = new JTextField(15);
        JTextField accNumField = new JTextField(15);
        JPasswordField pinField = new JPasswordField(15);
        JPasswordField confirmPinField = new JPasswordField(15);
        JTextField initialDepositField = new JTextField(15);

        // Style all text fields
        styleTextField(nameField);
        styleTextField(accNumField);
        styleTextField(pinField);
        styleTextField(confirmPinField);
        styleTextField(initialDepositField);

        // Labels
        JLabel[] labels = {
            new JLabel("FULL NAME:"),
            new JLabel("ACCOUNT NUMBER:"),
            new JLabel("PIN:"),
            new JLabel("CONFIRM PIN:"),
            new JLabel("INITIAL DEPOSIT:")
        };

        // Style all labels
        for (JLabel label : labels) {
            label.setForeground(Color.GREEN);
            label.setFont(new Font("Consolas", Font.BOLD, 16));
        }

        // Add components to screen
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        screenPanel.add(titleLabel, gbc);

        JComponent[] fields = {nameField, accNumField, pinField, confirmPinField, initialDepositField};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i * 2 + 1;
            screenPanel.add(labels[i], gbc);
            gbc.gridy = i * 2 + 2;
            screenPanel.add(fields[i], gbc);
        }

        // Side buttons panel
        JPanel rightButtonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        rightButtonsPanel.setBackground(new Color(200, 200, 200));
        
        JButton createButton = createATMButton("CREATE", new Color(0, 100, 0));
        JButton backButton = createATMButton("BACK", new Color(139, 0, 0));

        rightButtonsPanel.add(createButton);
        rightButtonsPanel.add(backButton);

        // Create keypad
        JPanel keypadPanel = createKeypad();

        // Add all panels to main signup panel
        signUpPanel.add(screenPanel, BorderLayout.CENTER);
        signUpPanel.add(rightButtonsPanel, BorderLayout.EAST);
        signUpPanel.add(keypadPanel, BorderLayout.SOUTH);

        // Action listeners
        createButton.addActionListener(_ -> createAccount(
            nameField.getText(),
            accNumField.getText(),
            new String(pinField.getPassword()),
            new String(confirmPinField.getPassword()),
            initialDepositField.getText()
        ));

        backButton.addActionListener(_ -> {
            cardLayout.show(mainPanel, "login");
            nameField.setText("");
            accNumField.setText("");
            pinField.setText("");
            confirmPinField.setText("");
            initialDepositField.setText("");
        });

        mainPanel.add(signUpPanel, "signup");
    }

    private void createAccount(String name, String accNum, String pin, String confirmPin, String initialDeposit) {
        if (name.isEmpty() || accNum.isEmpty() || pin.isEmpty() || confirmPin.isEmpty() || initialDeposit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (accounts.containsKey(accNum)) {
            JOptionPane.showMessageDialog(this, "Account number already exists!");
            return;
        }

        if (!pin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(this, "PINs do not match!");
            return;
        }

        try {
            double deposit = Double.parseDouble(initialDeposit);
            if (deposit < 500) {
                JOptionPane.showMessageDialog(this, "Initial deposit must be at least ₱500!");
                return;
            }

            Account newAccount = new Account(accNum, pin, deposit, name);
            accounts.put(accNum, newAccount);
            saveAccountToFile(newAccount);
            
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            cardLayout.show(mainPanel, "login");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount for initial deposit!");
        }
    }

    private void saveAccountToFile(Account account) {
        try {
            // First, read all accounts
            java.util.List<String> accounts = new ArrayList<>();  // Change this line
            File accountFile = new File("accounts.txt");
            if (accountFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(accountFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String accNum = line.split(",")[0];
                        if (!accNum.equals(account.getAccountNumber())) {
                            accounts.add(line);
                        }
                    }
                }
            }
            
            // Add the updated account
            accounts.add(String.format("%s,%s,%s,%.2f", 
                account.getAccountNumber(),
                account.getPin(),
                account.getAccountHolder(),
                account.getBalance()));
            
            // Write all accounts back to file
            try (PrintWriter out = new PrintWriter(new FileWriter(accountFile))) {
                for (String acc : accounts) {
                    out.println(acc);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playSound(String soundType) {
        try {
            String soundFile = switch (soundType) {
                case "button" -> "sounds/button.wav";
                case "card" -> "sounds/card.wav";
                case "printer" -> "sounds/printer.wav";
                default -> "sounds/beep.wav";
            };
            
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            // Silently fail if sound can't be played
        }
    }

    private boolean validateLogin(String accNum, String pin) {
        Account acc = accounts.get(accNum);
        return acc != null && acc.getPin().equals(pin);
    }

    private void loadAccounts() {
        File accountFile = new File("accounts.txt");
        if (accountFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(accountFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        String accNum = data[0];
                        String pin = data[1];
                        String name = data[2];
                        double balance = Double.parseDouble(data[3]);
                        accounts.put(accNum, new Account(accNum, pin, balance, name));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Default accounts if file doesn't exist
            accounts.put("1234", new Account("1234", "1234", 1000.0, "John Doe"));
            accounts.put("5678", new Account("5678", "5678", 2000.0, "Jane Smith"));
            // Save default accounts to file
            for (Account acc : accounts.values()) {
                saveAccountToFile(acc);
            }
        }
    }

    private void createMainMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(200, 200, 200));

        JLabel welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.GREEN);
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel optionsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        optionsPanel.setBackground(Color.BLACK);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] options = {
            "CHECK BALANCE",
            "WITHDRAW",
            "DEPOSIT",
            "TRANSFER",
            "CHANGE PIN",
            "EXIT"
        };
        
        for (String option : options) {
            JButton button = createATMButton(option, new Color(0, 100, 0));
            button.addActionListener(_ -> {
                playSound("button");
                switch (option) {
                    case "CHECK BALANCE" -> checkBalance();
                    case "WITHDRAW" -> showWithdrawDialog();
                    case "DEPOSIT" -> showDepositDialog();
                    case "TRANSFER" -> showTransferDialog();
                    case "CHANGE PIN" -> showChangePinDialog();
                    case "EXIT" -> logout();
                }
            });
            optionsPanel.add(button);
        }

        menuPanel.add(welcomeLabel, BorderLayout.NORTH);
        menuPanel.add(optionsPanel, BorderLayout.CENTER);
        menuPanel.add(createHardwarePanel(), BorderLayout.EAST);
        mainPanel.add(menuPanel, "mainMenu");
    }

    // Add new method for PIN change functionality
    private void showChangePinDialog() {
        JPanel changePinPanel = createATMScreen("CHANGE PIN");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Input fields
        JLabel currentPinLabel = new JLabel("Enter Current PIN:");
        JLabel newPinLabel = new JLabel("Enter New PIN:");
        JLabel confirmPinLabel = new JLabel("Confirm New PIN:");

        JPasswordField currentPinField = new JPasswordField(15);
        JPasswordField newPinField = new JPasswordField(15);
        JPasswordField confirmPinField = new JPasswordField(15);

        // Style components
        currentPinLabel.setForeground(Color.GREEN);
        newPinLabel.setForeground(Color.GREEN);
        confirmPinLabel.setForeground(Color.GREEN);
        currentPinLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        newPinLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        confirmPinLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        styleTextField(currentPinField);
        styleTextField(newPinField);
        styleTextField(confirmPinField);

        // Buttons
        JButton confirmButton = createATMButton("CONFIRM", new Color(0, 100, 0));
        JButton cancelButton = createATMButton("CANCEL", new Color(139, 0, 0));

        // Layout
        gbc.gridwidth = 2;
        gbc.gridy = 0; contentPanel.add(currentPinLabel, gbc);
        gbc.gridy = 1; contentPanel.add(currentPinField, gbc);
        gbc.gridy = 2; contentPanel.add(newPinLabel, gbc);
        gbc.gridy = 3; contentPanel.add(newPinField, gbc);
        gbc.gridy = 4; contentPanel.add(confirmPinLabel, gbc);
        gbc.gridy = 5; contentPanel.add(confirmPinField, gbc);
        
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        contentPanel.add(confirmButton, gbc);
        gbc.gridx = 1;
        contentPanel.add(cancelButton, gbc);

        changePinPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(changePinPanel, "changePin");
        cardLayout.show(mainPanel, "changePin");

        // Action listeners
        confirmButton.addActionListener(_ -> {
            String currentPin = new String(currentPinField.getPassword());
            String newPin = new String(newPinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword());

            if (!currentPin.equals(currentAccount.getPin())) {
                showErrorScreen("Current PIN is incorrect!");
                return;
            }

            if (!newPin.equals(confirmPin)) {
                showErrorScreen("New PINs do not match!");
                return;
            }

            if (!isValidPin(newPin)) {
                showErrorScreen("PIN must be 4 digits!");
                return;
            }

            // Update PIN
            currentAccount.setPin(newPin);
            saveAccountToFile(currentAccount);
            showSuccessScreen("PIN changed successfully!", "PIN CHANGE", 0.0);
        });

        cancelButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    private boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    // Update logout method to include card ejection
    private void logout() {
        if (currentAccount != null) {
            currentAccount.ejectCard();
            playSound("card");
            currentAccount = null;
        }
        cardLayout.show(mainPanel, "login");
    }

    private void generateReceipt(String transactionType, double amount) {
        JPanel receiptPanel = createATMScreen("TRANSACTION RECEIPT");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        // Receipt header
        addReceiptLine(contentPanel, "XYZ BANK ATM", gbc, 20);
        addReceiptLine(contentPanel, "------------------------", gbc, 16);
        addReceiptLine(contentPanel, "Date: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), gbc, 14);
        addReceiptLine(contentPanel, "------------------------", gbc, 16);
        
        // Transaction details
        addReceiptLine(contentPanel, "TRANSACTION TYPE:", gbc, 16);
        addReceiptLine(contentPanel, transactionType, gbc, 18);
        addReceiptLine(contentPanel, "AMOUNT: ₱" + String.format("%,.2f", amount), gbc, 16);
        addReceiptLine(contentPanel, "BALANCE: ₱" + String.format("%,.2f", currentAccount.getBalance()), gbc, 16);
        addReceiptLine(contentPanel, "------------------------", gbc, 16);
        
        // Account info (masked)
        addReceiptLine(contentPanel, "Account: ****" + 
            currentAccount.getAccountNumber().substring(
                Math.max(0, currentAccount.getAccountNumber().length() - 4)), gbc, 14);
        
        // Footer
        addReceiptLine(contentPanel, "Thank you for using our ATM", gbc, 14);
        addReceiptLine(contentPanel, "Please take your card", gbc, 14);

        JButton doneButton = createATMButton("DONE", new Color(0, 100, 0));
        gbc.insets = new Insets(20, 20, 10, 20);
        contentPanel.add(doneButton, gbc);

        receiptPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(receiptPanel, "receipt");
        cardLayout.show(mainPanel, "receipt");

        // Simulate printing sound
        playSound("printer");

        doneButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
        });
    }

    private void addReceiptLine(JPanel panel, String text, GridBagConstraints gbc, int fontSize) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.GREEN);
        label.setFont(new Font("Consolas", Font.BOLD, fontSize));
        gbc.gridy = GridBagConstraints.RELATIVE;
        panel.add(label, gbc);
    }

    private void showReceiptOptionScreen(String transactionType, double amount) {
        JPanel screenPanel = createATMScreen("PRINT RECEIPT");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Print receipt option
        JLabel printLabel = new JLabel("Would you like to print a receipt?");
        printLabel.setForeground(Color.GREEN);
        printLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        JButton yesButton = createATMButton("YES", new Color(0, 100, 0));
        JButton noButton = createATMButton("NO", new Color(139, 0, 0));
        
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        
        // Add components
        gbc.gridy = 0;
        contentPanel.add(printLabel, gbc);
        gbc.gridy = 1;
        contentPanel.add(buttonPanel, gbc);
        
        screenPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(screenPanel, "receiptOption");
        cardLayout.show(mainPanel, "receiptOption");
        
        // Action listeners
        yesButton.addActionListener(_ -> {
            showPrintingScreen();
            generateReceipt(transactionType, amount);
        });
        
        noButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    private void checkBalance() {
        generateReceipt("BALANCE INQUIRY", 0.0);
    }

    private void showWithdrawDialog() {
        JPanel withdrawPanel = createATMScreen("WITHDRAW");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Amount input
        JLabel amountLabel = new JLabel("Enter Amount to Withdraw:");
        amountLabel.setForeground(Color.GREEN);
        amountLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        JTextField amountField = new JTextField(15);
        styleTextField(amountField);

        // Quick amount buttons
        JPanel quickAmountPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickAmountPanel.setBackground(Color.BLACK);
        int[] quickAmounts = {1000, 2000, 3000, 5000, 10000, 20000};
        
        for (int amount : quickAmounts) {
            JButton amountButton = createATMButton("₱" + amount, new Color(0, 100, 0));
            amountButton.addActionListener(_ -> {
                amountField.setText(String.valueOf(amount));
                playSound("button");
            });
            quickAmountPanel.add(amountButton);
        }

        // Buttons
        JButton withdrawButton = createATMButton("WITHDRAW", new Color(0, 100, 0));
        JButton cancelButton = createATMButton("CANCEL", new Color(139, 0, 0));

        // Layout
        gbc.gridwidth = 2;
        gbc.gridy = 0; contentPanel.add(amountLabel, gbc);
        gbc.gridy = 1; contentPanel.add(amountField, gbc);
        gbc.gridy = 2; contentPanel.add(quickAmountPanel, gbc);
        
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        contentPanel.add(withdrawButton, gbc);
        gbc.gridx = 1;
        contentPanel.add(cancelButton, gbc);

        withdrawPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(withdrawPanel, "withdraw");
        cardLayout.show(mainPanel, "withdraw");

        // Action listeners
        withdrawButton.addActionListener(_ -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    showErrorScreen("Please enter a valid amount!");
                    return;
                }
                if (amount > currentAccount.getBalance()) {
                    showErrorScreen("Insufficient funds!");
                    return;
                }
                if (amount % 100 != 0) {
                    showErrorScreen("Amount must be in multiples of 100!");
                    return;
                }
                
                // Process withdrawal first
                currentAccount.withdraw(amount);
                saveAccountToFile(currentAccount);
                playSound("card");
                
                // Show success screen with receipt option
                showReceiptOptionScreen("WITHDRAWAL", amount);
                
            } catch (NumberFormatException ex) {
                showErrorScreen("Please enter a valid amount!");
            }
        });

        cancelButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    private void showDepositDialog() {
        JPanel depositPanel = createATMScreen("DEPOSIT");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Amount input
        JLabel amountLabel = new JLabel("Enter Amount to Deposit:");
        amountLabel.setForeground(Color.GREEN);
        amountLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        JTextField amountField = new JTextField(15);
        styleTextField(amountField);

        // Buttons
        JButton depositButton = createATMButton("DEPOSIT", new Color(0, 100, 0));
        JButton cancelButton = createATMButton("CANCEL", new Color(139, 0, 0));

        // Layout
        gbc.gridwidth = 2;
        gbc.gridy = 0; contentPanel.add(amountLabel, gbc);
        gbc.gridy = 1; contentPanel.add(amountField, gbc);
        
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        contentPanel.add(depositButton, gbc);
        gbc.gridx = 1;
        contentPanel.add(cancelButton, gbc);

        depositPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(depositPanel, "deposit");
        cardLayout.show(mainPanel, "deposit");

        // Action listeners
        depositButton.addActionListener(_ -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    showErrorScreen("Please enter a valid amount!");
                    return;
                }
                if (amount % 100 != 0) {
                    showErrorScreen("Amount must be in multiples of 100!");
                    return;
                }
                
                // Process deposit first
                currentAccount.deposit(amount);
                saveAccountToFile(currentAccount);
                playSound("card");
                
                // Show success screen with receipt option
                showReceiptOptionScreen("DEPOSIT", amount);
                
            } catch (NumberFormatException ex) {
                showErrorScreen("Please enter a valid amount!");
            }
        });

        cancelButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    private void showTransferDialog() {
        JPanel transferPanel = createATMScreen("TRANSFER");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Account number input
        JLabel accountLabel = new JLabel("Enter Recipient Account Number:");
        accountLabel.setForeground(Color.GREEN);
        accountLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        JTextField accountField = new JTextField(15);
        styleTextField(accountField);

        // Amount input
        JLabel amountLabel = new JLabel("Enter Amount to Transfer:");
        amountLabel.setForeground(Color.GREEN);
        amountLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        JTextField amountField = new JTextField(15);
        styleTextField(amountField);

        // Buttons
        JButton transferButton = createATMButton("TRANSFER", new Color(0, 100, 0));
        JButton cancelButton = createATMButton("CANCEL", new Color(139, 0, 0));

        // Layout
        gbc.gridwidth = 2;
        gbc.gridy = 0; contentPanel.add(accountLabel, gbc);
        gbc.gridy = 1; contentPanel.add(accountField, gbc);
        gbc.gridy = 2; contentPanel.add(amountLabel, gbc);
        gbc.gridy = 3; contentPanel.add(amountField, gbc);
        
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        contentPanel.add(transferButton, gbc);
        gbc.gridx = 1;
        contentPanel.add(cancelButton, gbc);

        transferPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(transferPanel, "transfer");
        cardLayout.show(mainPanel, "transfer");

        // Action listeners
        transferButton.addActionListener(_ -> {
            try {
                String recipientAccNum = accountField.getText();
                double amount = Double.parseDouble(amountField.getText());

                // Validate recipient account
                if (!accounts.containsKey(recipientAccNum)) {
                    showErrorScreen("Recipient account not found!");
                    return;
                }

                // Validate amount
                if (amount <= 0) {
                    showErrorScreen("Please enter a valid amount!");
                    return;
                }
                if (amount > currentAccount.getBalance()) {
                    showErrorScreen("Insufficient funds!");
                    return;
                }
                if (amount % 100 != 0) {
                    showErrorScreen("Amount must be in multiples of 100!");
                    return;
                }

                // Process transfer
                Account recipientAccount = accounts.get(recipientAccNum);
                currentAccount.withdraw(amount);
                recipientAccount.deposit(amount);
                saveAccountToFile(currentAccount);
                saveAccountToFile(recipientAccount);
                playSound("card");
                
                // Show success screen with receipt option
                showReceiptOptionScreen("TRANSFER", amount);
                
            } catch (NumberFormatException ex) {
                showErrorScreen("Please enter a valid amount!");
            }
        });

        cancelButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    private void showSuccessScreen(String message, String transactionType, double amount) {
        JPanel screenPanel = createATMScreen("TRANSACTION SUCCESSFUL");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Message display with amount
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + 
            message + "<br><br>" +
            "Amount: ₱" + String.format("%,.2f", amount) + "<br>" +
            "Balance: ₱" + String.format("%,.2f", currentAccount.getBalance()) +
            "</div></html>");
        messageLabel.setForeground(Color.GREEN);
        messageLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Print receipt option
        JLabel printLabel = new JLabel("Would you like to print a receipt?");
        printLabel.setForeground(Color.GREEN);
        printLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        JButton yesButton = createATMButton("YES", new Color(0, 100, 0));
        JButton noButton = createATMButton("NO", new Color(139, 0, 0));
        
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        
        // Add components
        gbc.gridy = 0;
        contentPanel.add(messageLabel, gbc);
        gbc.gridy = 1;
        contentPanel.add(printLabel, gbc);
        gbc.gridy = 2;
        contentPanel.add(buttonPanel, gbc);
        
        screenPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(screenPanel, "success");
        cardLayout.show(mainPanel, "success");
        
        // Action listeners
        yesButton.addActionListener(_ -> {
            showPrintingScreen();
            generateReceipt(transactionType, amount);
        });
        
        noButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    // Add this method before the main method
    private void showPrintingScreen() {
        JPanel printingPanel = createATMScreen("PRINTING RECEIPT");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        
        // Create loading animation
        JLabel loadingLabel = new JLabel("Printing Receipt...");
        loadingLabel.setForeground(Color.GREEN);
        loadingLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        
        // Add a progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 100, 0));
        progressBar.setBackground(Color.BLACK);
        progressBar.setFont(new Font("Consolas", Font.BOLD, 14));
        progressBar.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        contentPanel.add(loadingLabel, gbc);
        gbc.gridy = 1;
        contentPanel.add(progressBar, gbc);
        
        printingPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(printingPanel, "printing");
        cardLayout.show(mainPanel, "printing");
        
        // Simulate printing progress
        Timer timer = new Timer(50, null);
        timer.addActionListener(e -> {
            int value = progressBar.getValue();
            if (value < 100) {
                progressBar.setValue(value + 2);
            } else {
                ((Timer)e.getSource()).stop();
                playSound("printer");
                generateReceipt("BALANCE INQUIRY", 0.0);
            }
        });
        timer.start();
    }

    private JPanel createATMScreen(String title) {
        JPanel screenPanel = new JPanel(new BorderLayout(10, 10));
        screenPanel.setBackground(new Color(30, 30, 30));
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 15),
            BorderFactory.createLineBorder(Color.BLACK, 10)
        ));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.BLACK);
        titlePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 255, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        titlePanel.add(titleLabel);

        screenPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Add side panels for ATM look
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        leftPanel.setBackground(new Color(50, 50, 50));
        rightPanel.setBackground(new Color(50, 50, 50));
        leftPanel.setPreferredSize(new Dimension(50, 0));
        rightPanel.setPreferredSize(new Dimension(50, 0));
        
        screenPanel.add(leftPanel, BorderLayout.WEST);
        screenPanel.add(rightPanel, BorderLayout.EAST);

        return screenPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ATMInterface());
    }

    private void showErrorScreen(String message) {
        JPanel errorPanel = createATMScreen("ERROR");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        
        JButton okButton = createATMButton("OK", new Color(139, 0, 0));
        
        contentPanel.add(messageLabel, gbc);
        gbc.gridy = 1;
        contentPanel.add(okButton, gbc);
        
        errorPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(errorPanel, "error");
        cardLayout.show(mainPanel, "error");
        
        okButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }
} // End of class ATMInterface