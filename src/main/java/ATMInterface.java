package main.java;
// package src\main\java;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
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
        
        setTitle("ATM Simulation Machine");
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
        JPanel loginPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 40, 40),
                        0, h, new Color(20, 20, 20));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };

        // Screen panel (center) with modern design
        JPanel screenPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 30),
                        w, h, new Color(15, 15, 15));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 30, 15, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Modern title with glowing effect
        JLabel titleLabel = new JLabel("Welcome to BDA ATM Machine") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                g2d.setColor(new Color(0, 255, 0, 50));
                g2d.setFont(getFont().deriveFont(Font.BOLD, 24));
                for (int i = 0; i < 5; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(0, 255, 0));
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 255, 0));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Modern input fields with glowing labels
        JLabel accLabel = createGlowingLabel("ACCOUNT NUMBER:");
        JTextField accField = new JTextField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!hasFocus() && getText().isEmpty()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setColor(new Color(0, 255, 0, 70));
                    g2d.setFont(getFont().deriveFont(Font.ITALIC));
                    g2d.drawString("Enter account number", 5, getHeight() - 8);
                }
            }
        };
        styleTextField(accField);

        JLabel pinLabel = createGlowingLabel("ENTER PIN:");
        JPasswordField pinField = new JPasswordField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!hasFocus() && getPassword().length == 0) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setColor(new Color(0, 255, 0, 70));
                    g2d.setFont(getFont().deriveFont(Font.ITALIC));
                    g2d.drawString("Enter PIN", 5, getHeight() - 8);
                }
            }
        };
        styleTextField(pinField);

        // Add focus listeners for glow effect
        addGlowEffect(accField);
        addGlowEffect(pinField);

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
        JButton adminButton = createATMButton("ADMIN", new Color(139, 0, 0));

        rightButtonsPanel.add(loginButton);
        rightButtonsPanel.add(signUpButton);
        rightButtonsPanel.add(adminButton);

        // Add admin button action listener
        adminButton.addActionListener(_ -> {
            AdminInterface adminInterface = new AdminInterface(accounts);
            adminInterface.setVisible(true);
        });

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
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, color,
                    0, getHeight(), color.darker());
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Add glow effect when mouse over
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 255, 255, 50));
                    g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }

                // Draw text with glow
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();

                // Draw glow
                g2d.setColor(new Color(255, 255, 255, 50));
                for (int i = 1; i <= 3; i++) {
                    g2d.drawString(getText(), x + i, y + i);
                }

                // Draw main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        button.setFont(new Font("Consolas", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add press effect
        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                button.setLocation(button.getX(), button.getY() + 1);
            }
            public void mouseReleased(MouseEvent e) {
                button.setLocation(button.getX(), button.getY() - 1);
            }
        });

        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(20, 20, 20));
        field.setForeground(new Color(0, 255, 0));
        field.setFont(new Font("Consolas", Font.BOLD, 16));
        field.setCaretColor(new Color(0, 255, 0));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setOpaque(true);
    }

    private JLabel createGlowingLabel(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                g2d.setColor(new Color(0, 255, 0, 30));
                g2d.setFont(getFont());
                for (int i = 0; i < 3; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(0, 255, 0));
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        label.setFont(new Font("Consolas", Font.BOLD, 16));
        return label;
    }

    private void addGlowEffect(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 255, 0), 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
    }

    private JPanel createKeypad() {
        JPanel keypad = new JPanel(new GridLayout(4, 3, 12, 12));
        keypad.setBackground(new Color(30, 30, 30));
        keypad.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] keys = {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "*", "0", "#"
        };

        for (String key : keys) {
            JButton button = new JButton(key) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Create metallic gradient background
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(80, 80, 80),
                        0, getHeight(), new Color(40, 40, 40));
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                    // Add subtle border
                    g2d.setColor(new Color(100, 100, 100));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                    // Add highlight effect when pressed
                    if (getModel().isPressed()) {
                        g2d.setColor(new Color(255, 255, 255, 30));
                        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    }

                    // Draw text with glow
                    FontMetrics fm = g2d.getFontMetrics();
                    Rectangle2D r = fm.getStringBounds(getText(), g2d);
                    int x = (getWidth() - (int) r.getWidth()) / 2;
                    int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();

                    // Draw glow effect
                    g2d.setColor(new Color(0, 255, 0, 40));
                    for (int i = 1; i <= 3; i++) {
                        g2d.drawString(getText(), x + i, y + i);
                    }

                    // Draw main text
                    g2d.setColor(new Color(0, 255, 0));
                    g2d.drawString(getText(), x, y);
                }
            };
            button.setPreferredSize(new Dimension(70, 70));
            button.setFont(new Font("Consolas", Font.BOLD, 22));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Add click effect and sound with smooth animation
            button.addMouseListener(new MouseAdapter() {
                Timer pressTimer;
                
                public void mousePressed(MouseEvent e) {
                    if (pressTimer != null && pressTimer.isRunning()) {
                        pressTimer.stop();
                    }
                    pressTimer = new Timer(50, evt -> {
                        button.setLocation(button.getX(), button.getY() + 1);
                        ((Timer)evt.getSource()).stop();
                    });
                    pressTimer.setRepeats(false);
                    pressTimer.start();
                }
                
                public void mouseReleased(MouseEvent e) {
                    if (pressTimer != null && pressTimer.isRunning()) {
                        pressTimer.stop();
                    }
                    pressTimer = new Timer(50, evt -> {
                        button.setLocation(button.getX(), button.getY() - 1);
                        playSound("button");
                        ((Timer)evt.getSource()).stop();
                    });
                    pressTimer.setRepeats(false);
                    pressTimer.start();
                }
            });
            
            keypad.add(button);
        }

        return keypad;
    }

    // Update welcome labels in main menu panel with user's name
    private void updateWelcomeLabel() {
        if (mainPanel == null || currentAccount == null) return;
        
        JPanel menuPanel = (JPanel) mainPanel.getComponent(2); // Get main menu panel
        if (menuPanel instanceof JPanel) {
            // Find the welcome panel in the center panel
            Component[] components = menuPanel.getComponents();
            for (Component c : components) {
                if (c instanceof JPanel && ((JPanel) c).getLayout() instanceof BorderLayout) {
                    Component[] centerComps = ((JPanel) c).getComponents();
                    for (Component centerComp : centerComps) {
                        if (centerComp instanceof JPanel && "welcomePanel".equals(centerComp.getName())) {
                            Component[] welcomeComps = ((JPanel) centerComp).getComponents();
                            for (Component welcomeComp : welcomeComps) {
                                if (welcomeComp instanceof JLabel) {
                                    JLabel label = (JLabel) welcomeComp;
                                    if (label.getText().startsWith("WELCOME")) {
                                        // Update welcome message
                                        label.setText("WELCOME BACK,");
                                    } else if (!label.getText().contains("ACCOUNT:")) {
                                        // Update user name
                                        label.setText(currentAccount.getAccountHolder().toUpperCase());
                                        label.setForeground(new Color(255, 255, 255));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
        }
    }
            }
// Remove extra closing brace
// Remove extra closing brace

    private void createSignUpPanel() {
        JPanel signUpPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 40, 40),
                        0, h, new Color(20, 20, 20));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };

        // Create modern ATM screen panel with gradient background
        JPanel screenPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 30),
                        w, h, new Color(15, 15, 15));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        screenPanel.setPreferredSize(new Dimension(600, 400));
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Modern title and instructions with glowing effect
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("CREATE NEW ACCOUNT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                g2d.setColor(new Color(0, 255, 0, 50));
                g2d.setFont(getFont().deriveFont(Font.BOLD, 24));
                for (int i = 0; i < 5; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(0, 255, 0));
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel instructionLabel = new JLabel("Please fill in all fields below") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                g2d.setColor(new Color(255, 255, 0, 30));
                g2d.setFont(getFont().deriveFont(Font.ITALIC, 14));
                for (int i = 0; i < 3; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Draw main text
                g2d.setColor(Color.YELLOW);
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        instructionLabel.setFont(new Font("Consolas", Font.ITALIC, 14));
        instructionLabel.setHorizontalAlignment(JLabel.CENTER);

        titlePanel.add(titleLabel);
        titlePanel.add(instructionLabel);

        // Input fields with hints
        JTextField nameField = new JTextField(15);
        nameField.setToolTipText("Enter your full legal name");
        JTextField accNumField = new JTextField(15);
        accNumField.setToolTipText("Choose a unique account number");
        JPasswordField pinField = new JPasswordField(15);
        pinField.setToolTipText("Choose a 4-digit PIN");
        JPasswordField confirmPinField = new JPasswordField(15);
        confirmPinField.setToolTipText("Re-enter your PIN");
        JTextField initialDepositField = new JTextField(15);
        initialDepositField.setToolTipText("Minimum ₱500");

        // Style all text fields
        styleTextField(nameField);
        styleTextField(accNumField);
        styleTextField(pinField);
        styleTextField(confirmPinField);
        styleTextField(initialDepositField);

        // Labels with requirements
        JLabel[] labels = {
            new JLabel("FULL NAME: (Required)"),
            new JLabel("ACCOUNT NUMBER: (Required)"),
            new JLabel("PIN: (4 digits)"),
            new JLabel("CONFIRM PIN:"),
            new JLabel("INITIAL DEPOSIT: (Min. ₱500)")
        };

        // Style all labels
        for (JLabel label : labels) {
            label.setForeground(Color.GREEN);
            label.setFont(new Font("Consolas", Font.BOLD, 16));
        }

        // Add components to screen
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        screenPanel.add(titlePanel, gbc);

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
        StringBuilder errorMessage = new StringBuilder();

        // Validate all fields
        if (name.isEmpty() || accNum.isEmpty() || pin.isEmpty() || confirmPin.isEmpty() || initialDeposit.isEmpty()) {
            errorMessage.append("All fields are required!\n");
        }

        // Validate name format
        if (!name.isEmpty() && !name.matches("^[a-zA-Z\\s]+$")) {
            errorMessage.append("Name should only contain letters and spaces\n");
        }

        // Validate account number format
        if (!accNum.isEmpty() && !accNum.matches("^\\d{4,}$")) {
            errorMessage.append("Account number should be at least 4 digits\n");
        }

        if (accounts.containsKey(accNum)) {
            errorMessage.append("Account number already exists!\n");
        }

        // Validate PIN format
        if (!pin.isEmpty() && !pin.matches("^\\d{4}$")) {
            errorMessage.append("PIN must be exactly 4 digits\n");
        }

        if (!pin.equals(confirmPin)) {
            errorMessage.append("PINs do not match!\n");
        }

        try {
            double deposit = Double.parseDouble(initialDeposit);
            if (deposit < 500) {
                errorMessage.append("Initial deposit must be at least ₱500!\n");
            }

            // If there are any validation errors, show them all at once
            if (errorMessage.length() > 0) {
                JOptionPane.showMessageDialog(this, errorMessage.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create account and save
            Account newAccount = new Account(accNum, pin, deposit, name);
            accounts.put(accNum, newAccount);
            saveAccountToFile(newAccount);
            
            // Show success message with account details
            String successMessage = String.format(
                "Account created successfully!\n\n" +
                "Account Details:\n" +
                "Name: %s\n" +
                "Account Number: %s\n" +
                "Initial Balance: ₱%.2f\n\n" +
                "Please remember your account number and PIN for login.",
                name, accNum, deposit
            );
            
            JOptionPane.showMessageDialog(this, successMessage, "Account Created", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "login");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount for initial deposit!", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
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
        menuPanel.setBackground(new Color(20, 20, 30)); // Darker futuristic background

        // Top panel with holographic-style welcome message and system info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 10, 20)); // Deep space blue
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2), // Neon blue border
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Enhanced clock panel with pulsing effect
        JPanel clockPanel = new JPanel(new GridLayout(2, 1));
        clockPanel.setBackground(new Color(0, 10, 20));
        JLabel clockLabel = new JLabel();
        JLabel dateLabel = new JLabel();
        clockLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        dateLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        
        // Create pulsing effect for clock
        Timer pulseTimer = new Timer(1000, e -> {
            clockLabel.setForeground(new Color(0, 255, 255)); // Cyan
            new Timer(500, e2 -> {
                clockLabel.setForeground(new Color(0, 200, 200));
            }).start();
        });
        pulseTimer.start();
        
        dateLabel.setForeground(new Color(0, 200, 255)); // Light blue
        clockLabel.setHorizontalAlignment(JLabel.RIGHT);
        dateLabel.setHorizontalAlignment(JLabel.RIGHT);
        clockPanel.add(clockLabel);
        clockPanel.add(dateLabel);

        // Update clock with milliseconds for futuristic feel
        Timer clockTimer = new Timer(50, e -> {
            Calendar cal = Calendar.getInstance();
            clockLabel.setText(String.format("%02d:%02d:%02d.%03d", 
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND)));
            dateLabel.setText(String.format("%02d/%02d/%04d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)));
        });
        clockTimer.start();

        // Holographic-style welcome panel with 3D effect
        JPanel welcomePanel = new JPanel(new GridLayout(3, 1, 15, 15));
        welcomePanel.setName("welcomePanel"); // Set name for component identification
        welcomePanel.setBackground(new Color(0, 20, 40));
        welcomePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 255, 255), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Dynamic welcome message with user's name
        String userName = currentAccount != null ? currentAccount.getAccountHolder() : "Guest";
        JLabel welcomeLabel = new JLabel("WELCOME BACK,");
        JLabel userLabel = new JLabel(userName.toUpperCase());
        welcomeLabel.setFont(new Font("Consolas", Font.BOLD, 42));
        userLabel.setFont(new Font("Consolas", Font.BOLD, 48));
        welcomeLabel.setForeground(new Color(0, 255, 255));
        userLabel.setForeground(new Color(255, 255, 255));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        userLabel.setHorizontalAlignment(JLabel.CENTER);

        // Animated account info with LED effect
        JLabel accountLabel = new JLabel(currentAccount != null ? 
            String.format("ACCOUNT: %s", currentAccount.getAccountNumber()) : "");
        accountLabel.setFont(new Font("Consolas", Font.BOLD, 32));
        accountLabel.setForeground(new Color(0, 255, 255));
        accountLabel.setHorizontalAlignment(JLabel.CENTER);

        // Add glow effect to welcome message
        Timer glowTimer = new Timer(1500, e -> {
            float[] hsb = Color.RGBtoHSB(0, 255, 255, null);
            userLabel.setForeground(Color.getHSBColor(hsb[0], hsb[1], 
                (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() / 500.0))));
        });
        glowTimer.start();

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(userLabel);
        welcomePanel.add(accountLabel);

        // System status panel with real-time updates
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        statusPanel.setBackground(new Color(0, 20, 40));

        // Animated system status indicators
        String[] statuses = {"SYSTEM", "NETWORK", "SECURITY"};
        for (String status : statuses) {
            JPanel indicator = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            indicator.setBackground(new Color(0, 10, 20));
            
            JLabel statusDot = new JLabel("●");
            statusDot.setFont(new Font("Dialog", Font.BOLD, 24));
            statusDot.setForeground(new Color(0, 255, 0));
            
            JLabel statusLabel = new JLabel(status + ": ONLINE");
            statusLabel.setFont(new Font("Consolas", Font.BOLD, 20));
            statusLabel.setForeground(new Color(0, 255, 255));
            
            // Add blinking effect to status dots
            Timer blinkTimer = new Timer(2000, e -> {
                statusDot.setForeground(new Color(0, 255, 0));
                new Timer(100, e2 -> {
                    statusDot.setForeground(new Color(0, 100, 0));
                }).start();
            });
            blinkTimer.start();
            
            indicator.add(statusDot);
            indicator.add(statusLabel);
            statusPanel.add(indicator);
        }

        // News ticker with modern styling
        JPanel tickerPanel = new JPanel(new BorderLayout());
        tickerPanel.setBackground(new Color(0, 20, 40));
        tickerPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 255), 1));
        
        JLabel tickerLabel = new JLabel("Welcome to BDA Bank • Your Trusted Banking Partner • Experience the Future of Banking •");
        tickerLabel.setFont(new Font("Consolas", Font.PLAIN, 16));
        tickerLabel.setForeground(new Color(0, 200, 255));
        
        Timer scrollTimer = new Timer(50, e -> {
            String text = tickerLabel.getText();
            tickerLabel.setText(text.substring(1) + text.charAt(0));
        });
        scrollTimer.start();
        
        tickerPanel.add(tickerLabel, BorderLayout.CENTER);

        // Combine all panels
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBackground(new Color(0, 10, 20));
        centerPanel.add(welcomePanel);
        centerPanel.add(statusPanel);
        centerPanel.add(tickerPanel);

        topPanel.add(centerPanel, BorderLayout.CENTER);
        topPanel.add(clockPanel, BorderLayout.EAST);

        // Main options panel with holographic display effect
        JPanel mainOptionsPanel = new JPanel(new BorderLayout());
        mainOptionsPanel.setBackground(new Color(0, 15, 30));
        mainOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            BorderFactory.createEmptyBorder(10, 50, 10, 50)
        ));

        // Create futuristic live screen display with holographic effect
        JPanel liveScreenPanel = new JPanel(new BorderLayout());
        liveScreenPanel.setBackground(new Color(0, 10, 30));
        liveScreenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255), 3),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Add outer glow effect
        liveScreenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255, 100), 5),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 255, 50), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            )
        ));

        // Holographic display content with modern grid layout and scanline effect
        final JPanel displayContent = new JPanel(new GridLayout(5, 1, 20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                
                int y = (int) (System.currentTimeMillis() / 50 % getHeight());
                g2d.setColor(new Color(0, 255, 255, 30));
                g2d.fillRect(0, y, getWidth(), 2);
            }
        };
        displayContent.setBackground(new Color(0, 15, 35));
        displayContent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255, 150), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Add scanline effect timer
        Timer scanlineTimer = new Timer(50, e -> displayContent.repaint());
        scanlineTimer.start();

        // Dynamic balance display with advanced holographic effect
        JPanel balancePanel = new JPanel(new BorderLayout(10, 10));
        balancePanel.setBackground(new Color(0, 15, 35));
        balancePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255, 180), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Create a layered balance display
        JPanel balanceDisplay = new JPanel(new BorderLayout(5, 10));
        balanceDisplay.setBackground(new Color(0, 20, 40));
        balanceDisplay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 255, 255, 100), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel balanceTitle = new JLabel("AVAILABLE BALANCE");
        balanceTitle.setFont(new Font("Consolas", Font.BOLD, 36));
        balanceTitle.setForeground(new Color(0, 255, 0));
        balanceTitle.setHorizontalAlignment(JLabel.CENTER);
        balanceTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // Create LED-style balance amount display with enhanced visibility and glow effect
        JLabel balanceAmount = new JLabel(String.format("₱%.2f", 
            currentAccount != null ? currentAccount.getBalance() : 0.0));
        balanceAmount.setFont(new Font("Consolas", Font.BOLD, 72)); // Increased font size
        balanceAmount.setForeground(new Color(0, 255, 0));
        balanceAmount.setHorizontalAlignment(JLabel.CENTER);
        balanceAmount.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(30, 0, 30, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 200, 0, 128), 4),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
            )
        ));
        
        // Add dynamic glow effect with enhanced visibility and pulsing background
        Timer balanceEffectTimer = new Timer(50, e -> {
            long time = System.currentTimeMillis();
            float pulse = (float) (0.9 + 0.1 * Math.sin(time / 800.0));
            balanceAmount.setForeground(new Color(0, (int)(255 * pulse), 0));
            Frame glowPanel = null;
            glowPanel.setBackground(new Color(0, (int)(40 * pulse), 0));
            
            // Update balance in real-time with full precision
            if (currentAccount != null) {
                balanceAmount.setText(String.format("₱%.2f", currentAccount.getBalance()));
            }
        });
        balanceEffectTimer.start();

        // Add enhanced visibility with multi-layer glow effect
        JPanel glowPanel = new JPanel(new BorderLayout());
        glowPanel.setBackground(new Color(0, 40, 0));
        glowPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 255, 0, 50), 5),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 200, 0, 100), 3),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
            )
        ));
        glowPanel.add(balanceAmount, BorderLayout.CENTER);

        
        balancePanel.add(balanceTitle, BorderLayout.NORTH);
        balancePanel.add(glowPanel, BorderLayout.CENTER);
        balancePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Interactive bank information display
        JPanel bankInfoPanel = new JPanel(new BorderLayout(10, 10));
        bankInfoPanel.setBackground(new Color(0, 20, 40));
        bankInfoPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 255, 128), 1));
        
        JLabel bankLogo = new JLabel("BDA BANK");
        bankLogo.setFont(new Font("Consolas", Font.BOLD, 28));
        bankLogo.setForeground(new Color(0, 200, 255));
        bankLogo.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel bankSlogan = new JLabel("<html><center>FUTURE OF BANKING<br>24/7 Support: 1-800-BDA-BANK</center></html>");
        bankSlogan.setFont(new Font("Consolas", Font.BOLD, 16));
        bankSlogan.setForeground(new Color(0, 180, 255));
        bankSlogan.setHorizontalAlignment(JLabel.CENTER);
        
        bankInfoPanel.add(bankLogo, BorderLayout.NORTH);
        bankInfoPanel.add(bankSlogan, BorderLayout.CENTER);

        // Dynamic promotional display with particle effect
        JPanel promoPanel = new JPanel(new BorderLayout());
        promoPanel.setBackground(new Color(0, 20, 40));
        promoPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 255, 128), 1));
        
        JLabel promoLabel = new JLabel("✧ SPECIAL OFFER: 5% CASHBACK ON ALL TRANSACTIONS ✧");
        promoLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        promoLabel.setForeground(new Color(255, 215, 0));
        promoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Add shimmer effect to promo
        Timer promoTimer = new Timer(100, e -> {
            String text = promoLabel.getText();
            if (text.startsWith("✧")) {
                promoLabel.setText("✦" + text.substring(1, text.length() - 1) + "✦");
            } else {
                promoLabel.setText("✧" + text.substring(1, text.length() - 1) + "✧");
            }
        });
        promoTimer.start();
        
        promoPanel.add(promoLabel, BorderLayout.CENTER);

        // Security status display with dynamic indicators
        JPanel securityPanel = new JPanel(new BorderLayout());
        securityPanel.setBackground(new Color(0, 20, 40));
        securityPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 255, 128), 1));
        
        JLabel securityIcon = new JLabel("🔒");
        securityIcon.setFont(new Font("Dialog", Font.PLAIN, 24));
        securityIcon.setForeground(new Color(0, 255, 0));
        securityIcon.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel securityMsg = new JLabel("<html><center>SECURE TRANSACTION ENVIRONMENT<br>Enhanced with Advanced Encryption</center></html>");
        securityMsg.setFont(new Font("Consolas", Font.BOLD, 16));
        securityMsg.setForeground(new Color(0, 255, 128));
        securityMsg.setHorizontalAlignment(JLabel.CENTER);
        
        securityPanel.add(securityIcon, BorderLayout.WEST);
        securityPanel.add(securityMsg, BorderLayout.CENTER);

        // Add all components to display content with spacing
        displayContent.add(balancePanel);
        displayContent.add(bankInfoPanel);
        displayContent.add(promoPanel);
        displayContent.add(securityPanel);

        liveScreenPanel.add(displayContent, BorderLayout.CENTER);
        mainOptionsPanel.add(liveScreenPanel, BorderLayout.CENTER);

        // Left side buttons with enhanced size
        JPanel leftButtons = new JPanel(new GridLayout(4, 1, 25, 25));
        leftButtons.setBackground(Color.BLACK);
        leftButtons.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Right side buttons with enhanced size
        JPanel rightButtons = new JPanel(new GridLayout(4, 1, 25, 25));
        rightButtons.setBackground(Color.BLACK);
        rightButtons.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Transaction options - Core banking features
        String[][] options = {
            {"WITHDRAW", "DEPOSIT"},
            {"TRANSFER FUNDS", "CHECK BALANCE"},
            {"TRANSACTION HISTORY", "CHANGE PIN"},
            {"CANCEL", "EXIT"}
        };

        for (String[] row : options) {
            JButton leftBtn = createTransactionButton(row[0]);
            JButton rightBtn = createTransactionButton(row[1]);

            leftBtn.addActionListener(_ -> handleTransaction(row[0]));
            rightBtn.addActionListener(_ -> handleTransaction(row[1]));

            leftButtons.add(leftBtn);
            rightButtons.add(rightBtn);
        }

        mainOptionsPanel.add(leftButtons, BorderLayout.WEST);
        mainOptionsPanel.add(rightButtons, BorderLayout.EAST);

        // Add all components to the main panel
        menuPanel.add(topPanel, BorderLayout.NORTH);
        menuPanel.add(mainOptionsPanel, BorderLayout.CENTER);
        menuPanel.add(createHardwarePanel(), BorderLayout.EAST);

        mainPanel.add(menuPanel, "mainMenu");
    }

    private JButton createTransactionButton(String text) {
        JButton button = new JButton(text.replace("\n", "<br>"));
        button.setPreferredSize(new Dimension(300, 100));
        button.setBackground(new Color(20, 30, 40));
        button.setForeground(new Color(0, 255, 255));
        button.setFont(new Font("Consolas", Font.BOLD, 24));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 3),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        // Add simple hover effect without animation
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(30, 40, 60));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 200, 255), 4),
                    BorderFactory.createEmptyBorder(20, 30, 20, 30)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(20, 30, 40));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 150, 255), 3),
                    BorderFactory.createEmptyBorder(20, 30, 20, 30)
                ));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(new Color(40, 50, 70));
                playSound("button");
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(new Color(30, 40, 60));
            }
        });
        
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setPreferredSize(new Dimension(250, 80));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setOpaque(true);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 255, 0), 3),
                    BorderFactory.createEmptyBorder(14, 24, 14, 24)
                ));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(40, 40, 40));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 255, 0), 2),
                    BorderFactory.createEmptyBorder(15, 25, 15, 25)
                ));
            }
            public void mousePressed(MouseEvent e) {
                button.setBackground(new Color(30, 30, 30));
            }
            public void mouseReleased(MouseEvent e) {
                button.setBackground(new Color(40, 40, 40));
            }
        });

        // HTML formatting for multi-line text
        if (text.contains("\n")) {
            button.setText("<html><center>" + text.replace("\n", "<br>") + "</center></html>");
        }

        return button;
    }

    private void handleTransaction(String option) {
        if (currentAccount == null) {
            showErrorScreen("Please login first!");
            cardLayout.show(mainPanel, "login");
            return;
        }

        playSound("button");
        switch (option) {
            case "WITHDRAW" -> showWithdrawDialog();
            case "DEPOSIT" -> showDepositDialog();
            case "TRANSFER FUNDS" -> showTransferDialog();
            case "CHECK BALANCE" -> {
                // Show loading animation
                JDialog loadingDialog = new JDialog(this, "Loading", true);
                loadingDialog.setLayout(new BorderLayout());
                loadingDialog.setSize(300, 150);
                loadingDialog.setLocationRelativeTo(this);
                
                JPanel loadingPanel = new JPanel(new BorderLayout(10, 10));
                loadingPanel.setBackground(Color.BLACK);
                loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                JLabel loadingLabel = new JLabel("Checking Balance");
                loadingLabel.setFont(new Font("Consolas", Font.BOLD, 18));
                loadingLabel.setForeground(new Color(0, 255, 0));
                loadingLabel.setHorizontalAlignment(JLabel.CENTER);
                
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                progressBar.setBackground(Color.BLACK);
                progressBar.setForeground(new Color(0, 255, 0));
                
                loadingPanel.add(loadingLabel, BorderLayout.NORTH);
                loadingPanel.add(progressBar, BorderLayout.CENTER);
                loadingDialog.add(loadingPanel);
                
                // Create timer to simulate loading and record transaction
                Timer loadingTimer = new Timer(1500, e -> {
                    loadingDialog.dispose();
                    checkBalance();
                    
                    // Record balance check in transaction history
                    currentAccount.addTransaction(new Transaction(
                        "Balance Inquiry",
                        0.0,
                        currentAccount.getBalance(),
                        "Balance checked at ATM"
                    ));
                    saveAccountToFile(currentAccount);
                });
                loadingTimer.setRepeats(false);
                loadingTimer.start();
                
                loadingDialog.setVisible(true);
            }
            case "TRANSACTION HISTORY" -> showMiniStatement();
            case "CHANGE PIN" -> showChangePinDialog();
            case "CANCEL", "EXIT" -> logout();
        }
    }

    private void showCashTransactionsMenu() {
        JPanel cashPanel = createATMScreen("CASH TRANSACTIONS");
        JPanel contentPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String[] options = {"WITHDRAW", "DEPOSIT", "QUICK ₱1000", "QUICK ₱5000", "QUICK ₱10000", "BACK"};
        
        for (String option : options) {
            JButton button = createTransactionButton(option);
            button.addActionListener(_ -> {
                switch (option) {
                    case "WITHDRAW" -> showWithdrawDialog();
                    case "DEPOSIT" -> showDepositDialog();
                    case "QUICK ₱1000" -> processWithdrawal(1000);
                    case "QUICK ₱5000" -> processWithdrawal(5000);
                    case "QUICK ₱10000" -> processWithdrawal(10000);
                    case "BACK" -> cardLayout.show(mainPanel, "mainMenu");
                }
            });
            contentPanel.add(button);
        }

        cashPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(cashPanel, "cashTransactions");
        cardLayout.show(mainPanel, "cashTransactions");
    }

    private void processWithdrawal(double amount) {
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
        
        // Process withdrawal
        currentAccount.withdraw(amount);
        saveAccountToFile(currentAccount);
        playSound("cash");
        
        // Show success screen with receipt option
        showReceiptOptionScreen("QUICK WITHDRAWAL", amount);
    }

    private void showOtherServicesMenu() {
        JPanel servicesPanel = createATMScreen("OTHER SERVICES");
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String[] options = {"CHANGE PIN", "MINI STATEMENT", "BALANCE INQUIRY", "BACK"};
        
        for (String option : options) {
            JButton button = createTransactionButton(option);
            button.addActionListener(_ -> {
                switch (option) {
                    case "CHANGE PIN" -> showChangePinDialog();
                    case "MINI STATEMENT" -> showMiniStatement();
                    case "BALANCE INQUIRY" -> checkBalance();
                    case "BACK" -> cardLayout.show(mainPanel, "mainMenu");
                }
            });
            contentPanel.add(button);
        }

        servicesPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(servicesPanel, "otherServices");
        cardLayout.show(mainPanel, "otherServices");
    }

    private void showMiniStatement() {
        JPanel statementPanel = createATMScreen("MINI STATEMENT");
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create text area for transactions
        JTextArea statementArea = new JTextArea();
        statementArea.setBackground(Color.BLACK);
        statementArea.setForeground(Color.GREEN);
        statementArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        statementArea.setEditable(false);

        // Add account details
        StringBuilder sb = new StringBuilder();
        sb.append("Account Number: ").append(currentAccount.getAccountNumber())
          .append("\nAccount Holder: ").append(currentAccount.getAccountHolder())
          .append("\nCurrent Balance: ₱").append(String.format("%,.2f", currentAccount.getBalance()))
          .append("\n\nRecent Transactions:\n");

        // Add recent transactions (last 5)
        java.util.List<Transaction> transactions = currentAccount.getTransactionHistory();
        int start = Math.max(0, transactions.size() - 5);
        for (int i = start; i < transactions.size(); i++) {
            sb.append("\n").append(transactions.get(i).toString());
        }

        statementArea.setText(sb.toString());

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(statementArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add back button
        JButton backButton = createATMButton("BACK", new Color(139, 0, 0));
        backButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(backButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        statementPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statementPanel, "miniStatement");
        cardLayout.show(mainPanel, "miniStatement");
        
        // Play printer sound
        playSound("printer");
    }

    private void showPaymentsMenu() {
        JPanel paymentsPanel = createATMScreen("PAYMENTS");
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String[] options = {"UTILITY BILLS", "CREDIT CARD", "LOAN PAYMENT", "BACK"};
        
        for (String option : options) {
            JButton button = createTransactionButton(option);
            button.addActionListener(_ -> {
                if (option.equals("BACK")) {
                    cardLayout.show(mainPanel, "mainMenu");
                } else {
                    showMessage("Service Currently Unavailable", 
                              option + " payment service is currently under maintenance.", 
                              JOptionPane.INFORMATION_MESSAGE);
                }
            });
            contentPanel.add(button);
        }

        paymentsPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(paymentsPanel, "payments");
        cardLayout.show(mainPanel, "payments");
    }

    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
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
        
        // Create a button panel at the top
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create larger buttons
        JButton backButton = createATMButton("BACK", new Color(139, 0, 0));
        JButton doneButton = createATMButton("DONE", new Color(0, 100, 0));
        
        // Set preferred size for larger buttons
        Dimension buttonSize = new Dimension(200, 60);
        backButton.setPreferredSize(buttonSize);
        doneButton.setPreferredSize(buttonSize);
        
        buttonPanel.add(backButton);
        buttonPanel.add(doneButton);
        
        // Add button panel to the top
        receiptPanel.add(buttonPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        // Receipt header
        addReceiptLine(contentPanel, "Banco De AU(BDA) ATM Machine", gbc, 20);
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

        receiptPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(receiptPanel, "receipt");
        cardLayout.show(mainPanel, "receipt");

        // Simulate printing sound
        playSound("printer");

        // Add action listeners
        backButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
        });
        
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
        // Create loading screen with futuristic design
        JPanel loadingPanel = createATMScreen("BALANCE INQUIRY");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Welcome message with glowing effect
        JLabel welcomeLabel = new JLabel("Welcome, " + currentAccount.getAccountHolder());
        welcomeLabel.setForeground(new Color(0, 255, 128));
        welcomeLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Modern processing message
        JLabel processingLabel = new JLabel("SCANNING");
        processingLabel.setForeground(new Color(0, 255, 255));
        processingLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        processingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create glowing panel for progress
        JPanel glowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create glowing effect
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, h/2, new Color(0, 255, 128, 50),
                    w, h/2, new Color(0, 128, 255, 50));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        glowPanel.setPreferredSize(new Dimension(300, 5));
        glowPanel.setBackground(Color.BLACK);

        // Modern progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 255, 128));
        progressBar.setBackground(new Color(0, 20, 20));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255)));
        progressBar.setFont(new Font("Consolas", Font.BOLD, 16));

        // Layout components
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0; contentPanel.add(welcomeLabel, gbc);
        gbc.gridy = 1; contentPanel.add(processingLabel, gbc);
        gbc.gridy = 2; contentPanel.add(glowPanel, gbc);
        gbc.gridy = 3; contentPanel.add(progressBar, gbc);

        loadingPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(loadingPanel, "loading");
        cardLayout.show(mainPanel, "loading");

        // Fast and smooth animation
        Timer dotTimer = new Timer(200, null);
        Timer progressTimer = new Timer(20, null);
        
        final int[] dots = {0};
        final int[] progress = {0};
        final float[] hue = {0f};

        dotTimer.addActionListener(e -> {
            dots[0] = (dots[0] + 1) % 4;
            StringBuilder text = new StringBuilder("SCANNING");
            for (int i = 0; i < dots[0]; i++) {
                text.append(">");
            }
            processingLabel.setText(text.toString());
            
            // Cycle through colors
            hue[0] = (hue[0] + 0.02f) % 1f;
            processingLabel.setForeground(Color.getHSBColor(hue[0], 1f, 1f));
        });

        progressTimer.addActionListener(e -> {
            progress[0] += 4;
            progressBar.setValue(progress[0]);
            progressBar.setString(progress[0] + "%");
            glowPanel.repaint();
            
            if (progress[0] >= 100) {
                progressTimer.stop();
                dotTimer.stop();
                // Show balance after loading completes
                generateReceipt("BALANCE INQUIRY", 0.0);
            }
        });

        dotTimer.start();
        progressTimer.start();
    }

    private void showWithdrawDialog() {
        JPanel withdrawPanel = createATMScreen("WITHDRAW");
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(0, 15, 30));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // Create holographic display panel with scanline effect
        JPanel displayPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create holographic background effect
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0, 30, 60),
                    getWidth(), getHeight(), new Color(0, 15, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add scanline effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                int y = (int) (System.currentTimeMillis() / 50 % getHeight());
                g2d.setColor(new Color(0, 255, 255, 30));
                g2d.fillRect(0, y, getWidth(), 2);
            }
        };
        displayPanel.setBackground(new Color(0, 15, 30));
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Create balance display with holographic effect
        JPanel balancePanel = new JPanel(new BorderLayout(10, 10));
        balancePanel.setOpaque(false);
        balancePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255, 100), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel balanceLabel = new JLabel(String.format("Available Balance: ₱%,.2f", currentAccount.getBalance()));
        balanceLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        balanceLabel.setForeground(new Color(0, 255, 255));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        balancePanel.add(balanceLabel, BorderLayout.CENTER);

        // Create amount input panel with modern styling
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Enhanced amount label with dynamic glow
        JLabel amountLabel = new JLabel("ENTER WITHDRAWAL AMOUNT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                g2d.setColor(new Color(0, 255, 255, 50));
                g2d.setFont(getFont().deriveFont(Font.BOLD, 24));
                for (int i = 0; i < 5; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(0, 255, 255));
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        amountLabel.setFont(new Font("Consolas", Font.BOLD, 24));

        // Modern amount input field with LED-style display
        JTextField amountField = new JTextField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(new Color(0, 20, 40));
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Border with glow
                g2d.setColor(new Color(0, 200, 255));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Draw text with currency symbol
                g2d.setFont(new Font("Consolas", Font.BOLD, 36));
                g2d.setColor(new Color(0, 255, 255));
                String text = getText().isEmpty() ? "₱0.00" : "₱" + getText();
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, 10, ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
            }
        };
        amountField.setPreferredSize(new Dimension(300, 60));
        amountField.setOpaque(false);
        amountField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        amountField.setForeground(new Color(0, 255, 255));
        amountField.setCaretColor(new Color(0, 255, 255));
        amountField.setFont(new Font("Consolas", Font.BOLD, 36));

        // Quick amount buttons with holographic effect
        JPanel quickAmountPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        quickAmountPanel.setOpaque(false);
        int[] quickAmounts = {1000, 2000, 5000, 10000, 15000, 20000};

        for (int amount : quickAmounts) {
            JButton quickButton = createTransactionButton(String.format("₱%,d", amount));
            quickButton.addActionListener(e -> {
                amountField.setText(String.valueOf(amount));
                playSound("button");
            });
            quickAmountPanel.add(quickButton);
        }

        // Action buttons with enhanced styling
        JButton withdrawButton = createTransactionButton("WITHDRAW");
        withdrawButton.setBackground(new Color(0, 100, 0));
        JButton cancelButton = createTransactionButton("CANCEL");
        cancelButton.setBackground(new Color(139, 0, 0));

        // Layout components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(balancePanel, BorderLayout.NORTH);

        gbc.gridwidth = 2;
        gbc.gridy = 0; inputPanel.add(amountLabel, gbc);
        gbc.gridy = 1; inputPanel.add(amountField, gbc);
        gbc.gridy = 2; inputPanel.add(quickAmountPanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(cancelButton);

        displayPanel.add(topPanel, BorderLayout.NORTH);
        displayPanel.add(inputPanel, BorderLayout.CENTER);
        displayPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(displayPanel, BorderLayout.CENTER);
        withdrawPanel.add(contentPanel, BorderLayout.CENTER);

        // Add scanline animation
        Timer scanlineTimer = new Timer(50, e -> displayPanel.repaint());
        scanlineTimer.start();

        mainPanel.add(withdrawPanel, "withdraw");
        cardLayout.show(mainPanel, "withdraw");

        // Enhanced action listeners with animations
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

                // Show processing animation
                JDialog processingDialog = new JDialog(this, "Processing", true);
                processingDialog.setUndecorated(true);
                processingDialog.setSize(300, 150);
                processingDialog.setLocationRelativeTo(this);

                JPanel processingPanel = new JPanel(new BorderLayout(10, 10));
                processingPanel.setBackground(new Color(0, 15, 30));
                processingPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));

                JLabel processingLabel = new JLabel("PROCESSING WITHDRAWAL");
                processingLabel.setFont(new Font("Consolas", Font.BOLD, 18));
                processingLabel.setForeground(new Color(0, 255, 255));
                processingLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                progressBar.setBackground(new Color(0, 20, 40));
                progressBar.setForeground(new Color(0, 255, 255));

                processingPanel.add(processingLabel, BorderLayout.CENTER);
                processingPanel.add(progressBar, BorderLayout.SOUTH);
                processingDialog.add(processingPanel);

                // Process withdrawal after brief animation
                Timer processTimer = new Timer(1500, e -> {
                    processingDialog.dispose();
                    currentAccount.withdraw(amount);
                    saveAccountToFile(currentAccount);
                    playSound("cash");
                    showReceiptOptionScreen("WITHDRAWAL", amount);
                });
                processTimer.setRepeats(false);
                processTimer.start();

                processingDialog.setVisible(true);
            } catch (NumberFormatException ex) {
                showErrorScreen("Please enter a valid amount!");
            }
        });

        cancelButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
        });
    }
// Remove extra closing brace as it was causing a syntax error

    private void showDepositDialog() {
        JPanel depositPanel = createATMScreen("DEPOSIT");
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(0, 15, 30));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // Create holographic display panel with scanline effect
        JPanel displayPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create holographic background effect
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0, 30, 60),
                    getWidth(), getHeight(), new Color(0, 15, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add scanline effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                int y = (int) (System.currentTimeMillis() / 50 % getHeight());
                g2d.setColor(new Color(0, 255, 255, 30));
                g2d.fillRect(0, y, getWidth(), 2);
            }
        };
        displayPanel.setBackground(new Color(0, 15, 30));
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Create balance display with holographic effect
        JPanel balancePanel = new JPanel(new BorderLayout(10, 10));
        balancePanel.setOpaque(false);
        balancePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255, 100), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel balanceLabel = new JLabel(String.format("Current Balance: ₱%,.2f", currentAccount.getBalance()));
        balanceLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        balanceLabel.setForeground(new Color(0, 255, 255));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        balancePanel.add(balanceLabel, BorderLayout.CENTER);

        // Create amount input panel with modern styling
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Enhanced amount label with dynamic glow
        JLabel amountLabel = new JLabel("ENTER DEPOSIT AMOUNT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                g2d.setColor(new Color(0, 255, 255, 50));
                g2d.setFont(getFont().deriveFont(Font.BOLD, 24));
                for (int i = 0; i < 5; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Draw main text
                g2d.setColor(new Color(0, 255, 255));
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        amountLabel.setFont(new Font("Consolas", Font.BOLD, 24));

        // Modern amount input field with LED-style display
        JTextField amountField = new JTextField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(new Color(0, 20, 40));
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Border with glow
                g2d.setColor(new Color(0, 200, 255));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Draw text with currency symbol
                g2d.setFont(new Font("Consolas", Font.BOLD, 36));
                g2d.setColor(new Color(0, 255, 255));
                String text = getText().isEmpty() ? "₱0.00" : "₱" + getText();
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, 10, ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
            }
        };
        amountField.setPreferredSize(new Dimension(300, 60));
        amountField.setOpaque(false);
        amountField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        amountField.setForeground(new Color(0, 255, 255));
        amountField.setCaretColor(new Color(0, 255, 255));
        amountField.setFont(new Font("Consolas", Font.BOLD, 36));

        // Action buttons with enhanced styling
        JButton depositButton = createTransactionButton("DEPOSIT");
        depositButton.setBackground(new Color(0, 100, 0));
        JButton cancelButton = createTransactionButton("CANCEL");
        cancelButton.setBackground(new Color(139, 0, 0));

        // Layout components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(balancePanel, BorderLayout.NORTH);

        gbc.gridwidth = 2;
        gbc.gridy = 0; inputPanel.add(amountLabel, gbc);
        gbc.gridy = 1; inputPanel.add(amountField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(depositButton);
        buttonPanel.add(cancelButton);

        displayPanel.add(topPanel, BorderLayout.NORTH);
        displayPanel.add(inputPanel, BorderLayout.CENTER);
        displayPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(displayPanel, BorderLayout.CENTER);
        depositPanel.add(contentPanel, BorderLayout.CENTER);

        // Add scanline animation
        Timer scanlineTimer = new Timer(50, e -> displayPanel.repaint());
        scanlineTimer.start();

        mainPanel.add(depositPanel, "deposit");
        cardLayout.show(mainPanel, "deposit");

        // Enhanced action listeners with animations
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

                // Process transfer with sender and receiver details
                Account recipientAccount = accounts.get(recipientAccNum);
                String description = String.format("Transfer between %s and %s", 
                    currentAccount.getAccountHolder(), recipientAccount.getAccountHolder());
                currentAccount.transfer(amount, recipientAccount, description);
                saveAccountToFile(currentAccount);
                saveAccountToFile(recipientAccount);
                playSound("card");
                
                // Show success screen with receipt option and transfer details
                showReceiptOptionScreen("TRANSFER", amount, 
                    String.format("To: %s (%s)", recipientAccount.getAccountHolder(), recipientAccNum));
                
            } catch (NumberFormatException ex) {
                showErrorScreen("Please enter a valid amount!");
            }
        });

        cancelButton.addActionListener(_ -> cardLayout.show(mainPanel, "mainMenu"));
    }

    private void showReceiptOptionScreen(String transactionType, double amount, String recipientInfo) {
        JPanel receiptPanel = createATMScreen("TRANSACTION RECEIPT");
        
        // Create a button panel at the top
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create larger buttons
        JButton backButton = createATMButton("BACK", new Color(139, 0, 0));
        JButton doneButton = createATMButton("DONE", new Color(0, 100, 0));
        
        // Set preferred size for larger buttons
        Dimension buttonSize = new Dimension(200, 60);
        backButton.setPreferredSize(buttonSize);
        doneButton.setPreferredSize(buttonSize);
        
        buttonPanel.add(backButton);
        buttonPanel.add(doneButton);
        
        // Add button panel to the top
        receiptPanel.add(buttonPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(0, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Header with holographic effect
        JLabel headerLabel = new JLabel("BDA ATM MACHINE");
        headerLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 255, 255));
        gbc.gridy = GridBagConstraints.RELATIVE;
        contentPanel.add(headerLabel, gbc);
        
        // Separator with LED effect
        addReceiptLine(contentPanel, "═══════════════════════", gbc, 16);
        
        // Transaction timestamp with futuristic format
        addReceiptLine(contentPanel, "DATE: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), gbc, 14);
        addReceiptLine(contentPanel, "═══════════════════════", gbc, 16);
        
        // Transaction details with neon glow
        addReceiptLine(contentPanel, "TRANSACTION TYPE:", gbc, 16);
        JLabel typeLabel = new JLabel(transactionType);
        typeLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        typeLabel.setForeground(new Color(0, 255, 128));
        contentPanel.add(typeLabel, gbc);
        
        addReceiptLine(contentPanel, "AMOUNT: ₱" + String.format("%,.2f", amount), gbc, 18);
        
        // Sender info with cyberpunk style
        addReceiptLine(contentPanel, "FROM:", gbc, 16);
        JLabel senderLabel = new JLabel(currentAccount.getAccountHolder());
        senderLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        senderLabel.setForeground(new Color(255, 255, 0));
        contentPanel.add(senderLabel, gbc);
        
        addReceiptLine(contentPanel, "ACC: ****" + 
            currentAccount.getAccountNumber().substring(
                Math.max(0, currentAccount.getAccountNumber().length() - 4)), gbc, 14);
        
        // Recipient info for transfers with matching style
        if (recipientInfo != null) {
            addReceiptLine(contentPanel, "TO:", gbc, 16);
            JLabel recipientLabel = new JLabel(recipientInfo);
            recipientLabel.setFont(new Font("Consolas", Font.BOLD, 18));
            recipientLabel.setForeground(new Color(255, 255, 0));
            contentPanel.add(recipientLabel, gbc);
        }
        
        addReceiptLine(contentPanel, "═══════════════════════", gbc, 16);
        addReceiptLine(contentPanel, "BALANCE: ₱" + String.format("%,.2f", currentAccount.getBalance()), gbc, 18);
        addReceiptLine(contentPanel, "═══════════════════════", gbc, 16);
        
        // Footer with system status
        addReceiptLine(contentPanel, "Transaction ID: " + generateTransactionId(), gbc, 14);
        addReceiptLine(contentPanel, "Status: APPROVED", gbc, 14);
        addReceiptLine(contentPanel, "Thank you for using BDA ATM", gbc, 14);

        // Add action listeners
        backButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
        });
        
        doneButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
        });

doneButton = new JButton("DONE") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 255, 255));
                int textX = (getWidth() - g2.getFontMetrics().stringWidth("DONE")) / 2;
                int textY = (getHeight() + g2.getFontMetrics().getHeight()) / 2;
                g2.drawString("DONE", textX, textY);
            }
        };
        doneButton.setPreferredSize(new Dimension(200, 50));
        doneButton.setContentAreaFilled(false);
        doneButton.setBorderPainted(false);
        doneButton.setFocusPainted(false);
        doneButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.insets = new Insets(20, 20, 10, 20);
        contentPanel.add(doneButton, gbc);

        receiptPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(receiptPanel, "receipt");
        cardLayout.show(mainPanel, "receipt");

        // Simulate printing sound with LED animation
        playSound("printer");

        doneButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
        });
    }

    private String generateTransactionId() {
        return String.format("%s%06d", 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
            (int)(Math.random() * 1000000));
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