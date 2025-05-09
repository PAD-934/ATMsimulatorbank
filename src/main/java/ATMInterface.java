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

    public HashMap<String, Account> getAccounts() {
        return accounts;
    }

    private JPanel splashScreen;
    private Timer splashTimer;
    private float glowIntensity = 0.0f;
    private boolean glowIncreasing = true;
    @SuppressWarnings("unused")
    private JLabel cardSlotLabel;
    private JPanel cardPanel;
    private boolean cardInserted = false;
    
    // Text fields for account and transaction input
    private JTextField accountField;
    private JTextField amountField;
    
    // Track login attempts and blocked accounts
    private HashMap<String, Integer> loginAttempts;
    private HashMap<String, Long> blockedAccounts;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long BLOCK_DURATION = 300000; // 5 minutes in milliseconds
    
    public ATMInterface() {
        accounts = new HashMap<>();
        loginAttempts = new HashMap<>();
        blockedAccounts = new HashMap<>();
        loadAccounts();
        
        // Create splash screen first
        createSplashScreen();
        
        setTitle("ATM Simulation Machine");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(220, 220, 220)); // Light gray for better visibility

        // Adjust font sizes and colors for better visibility
        UIManager.put("Label.font", new Font("Consolas", Font.BOLD, 20));
        UIManager.put("Button.font", new Font("Consolas", Font.BOLD, 18));
        UIManager.put("TextField.font", new Font("Consolas", Font.BOLD, 18));
        UIManager.put("Button.background", new Color(230, 230, 250)); // Light lavender for buttons
        UIManager.put("Button.foreground", new Color(0, 0, 0)); // Black text for contrast
        UIManager.put("TextField.background", new Color(255, 255, 255)); // White background for text fields
        UIManager.put("TextField.foreground", new Color(0, 0, 0)); // Black text for contrast

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(240, 240, 240)); // Lighter gray for main panel
        
        // Add border to simulate ATM frame with better visibility
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 20),
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
                    simulateCardEjection();
                    Timer exitTimer = new Timer(1000, event -> {
                        dispose();
                        System.exit(0);
                    });
                    exitTimer.setRepeats(false);
                    exitTimer.start();
                }
            }
        });
        setFocusable(true);
        
        setLayout(new CardLayout());
        add("splash", splashScreen);
        add("main", mainPanel);
        setVisible(true);
        
        // Request focus for splash screen and start animation
        splashScreen.requestFocusInWindow();
        startSplashAnimation();
    }
    
    private void createSplashScreen() {
        splashScreen = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create futuristic background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0, 20, 40),
                    getWidth(), getHeight(), new Color(0, 40, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add grid effect
                g2d.setColor(new Color(0, 100, 200, 30));
                int gridSize = 50;
                for (int i = 0; i < getWidth(); i += gridSize) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += gridSize) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        
        // Create center panel for content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        // Create glowing title
        JLabel titleLabel = new JLabel("BDA ATM SYSTEM") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow effect
                float alpha = Math.min(1.0f, Math.max(0.2f, glowIntensity));
                g2d.setColor(new Color(0, 1.0f, 1.0f, alpha * 0.5f));
                Font glowFont = getFont().deriveFont(Font.BOLD, 48);
                g2d.setFont(glowFont);
                
                // Multiple layers for glow effect
                for (int i = 0; i < 5; i++) {
                    g2d.drawString(getText(), i, getHeight() / 2 + i);
                }
                
                // Main text
                g2d.setColor(new Color(0, 1.0f, 1.0f, 1.0f));
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0, 255, 255));
        
        // Create press to start text
        JLabel pressStartLabel = new JLabel("PRESS ANY KEY TO START") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                float alpha = Math.min(1.0f, Math.max(0.2f, glowIntensity));
                g2d.setColor(new Color(0, 1.0f, 1.0f, alpha));
                g2d.setFont(getFont());
                g2d.drawString(getText(), 0, getHeight() / 2);
            }
        };
        pressStartLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        
        // Add components to center panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);
        centerPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        centerPanel.add(pressStartLabel, gbc);
        
        splashScreen.add(centerPanel, BorderLayout.CENTER);
        
        // Add key listener for any key press
        splashScreen.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (splashTimer != null && splashTimer.isRunning()) {
                    splashTimer.stop();
                }
                transitionToLogin();
            }
        });
        splashScreen.setFocusable(true);
    }
    
    private void startSplashAnimation() {
        splashTimer = new Timer(50, _ -> {
            if (glowIncreasing) {
                glowIntensity += 0.05f;
                if (glowIntensity >= 1.0f) {
                    glowIntensity = 1.0f;
                    glowIncreasing = false;
                }
            } else {
                glowIntensity -= 0.05f;
                if (glowIntensity <= 0.2f) {
                    glowIntensity = 0.2f;
                    glowIncreasing = true;
                }
            }
            splashScreen.repaint();
        });
        splashTimer.start();
    }
    
    private void transitionToLogin() {
        // Fade out splash screen
        Timer fadeTimer = new Timer(50, new ActionListener() {
            float alpha = 1.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.05f;
                if (alpha <= 0) {
                    ((Timer)e.getSource()).stop();
                    CardLayout layout = (CardLayout)getContentPane().getLayout();
                    layout.show(getContentPane(), "main");
                    cardLayout.show(mainPanel, "login");
                    mainPanel.requestFocusInWindow();
                } else {
                    splashScreen.setBackground(new Color(0, 0, 0, alpha));
                }
            }
        });
        fadeTimer.start();
    }
    
    private void simulateCardInsertion() {
        
        if (!cardInserted) {
            cardInserted = true;
            cardPanel.setVisible(true);
            
            // Get the LED indicator for card slot
            JPanel cardSlotPanel = (JPanel)((JPanel)cardPanel.getParent()).getParent();
            JPanel ledPanel = (JPanel)cardSlotPanel.getComponent(1);
            
            // Play card insertion sound
            playSound("card");
            
            // Start insertion animation
            Timer insertTimer = new Timer(20, new ActionListener() {
                int steps = 0;
                float progress = 0.0f;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    steps++;
                    progress = Math.min(1.0f, steps / 25.0f);
                    
                    // Smooth easing function with bounce
                    float easedProgress = (float)(1 - Math.pow(1 - progress, 4));
                    float bounce = progress > 0.8f ? (float)(Math.sin(progress * Math.PI * 8) * 0.1) : 0;
                    
                    if (progress < 1.0f) {
                        cardPanel.setBounds(
                            cardPanel.getX(),
                            cardPanel.getY() + (int)(3 * (1.0f - easedProgress + bounce)),
                            cardPanel.getWidth(),
                            cardPanel.getHeight()
                        );
                        
                        // Update LED color during insertion with pulsing effect
                        float pulse = (float)(Math.sin(progress * Math.PI * 4) * 0.3 + 0.7);
                        ledPanel.setBackground(new Color(
                            0,
                            (int)(100 + 155 * easedProgress * pulse),
                            0
                        ));
                    } else {
                        ((Timer)e.getSource()).stop();
                        
                        // Flash LED when card is fully inserted
                        Timer ledTimer = new Timer(100, new ActionListener() {
                            int flashes = 0;
                            boolean isOn = true;
                            
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                if (flashes < 3) {
                                    ledPanel.setBackground(new Color(0, isOn ? 255 : 100, 0));
                                    isOn = !isOn;
                                    flashes++;
                                } else {
                                    ledPanel.setBackground(new Color(0, 255, 0));
                                    ((Timer)evt.getSource()).stop();
                                }
                            }
                        });
                        ledTimer.start();
                    }
                }
            });
            insertTimer.start();
        }
    }
    
    private void simulateCardEjection() {
        if (cardInserted) {
            cardInserted = false;
            
            // Get the LED indicator for card slot
            JPanel cardSlotPanel = (JPanel)((JPanel)cardPanel.getParent()).getParent();
            JPanel ledPanel = (JPanel)cardSlotPanel.getComponent(1);
            
            // Start ejection animation
            // Play card ejection sound
            playSound("card");
            
            Timer ejectTimer = new Timer(20, new ActionListener() {
                int steps = 0;
                float progress = 0.0f;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    steps++;
                    progress = Math.min(1.0f, steps / 25.0f);
                    
                    // Smooth easing function with spring effect
                    float easedProgress = (float)(1 - Math.pow(1 - progress, 4));
                    float spring = progress < 0.7f ? (float)(Math.sin(progress * Math.PI * 6) * 0.15) : 0;
                    
                    if (progress < 1.0f) {
                        cardPanel.setBounds(
                            cardPanel.getX(),
                            cardPanel.getY() - (int)(3 * (easedProgress + spring)),
                            cardPanel.getWidth(),
                            cardPanel.getHeight()
                        );
                        
                        // Update LED color during ejection with pulsing effect
                        float pulse = (float)(Math.sin(progress * Math.PI * 3) * 0.3 + 0.7);
                        ledPanel.setBackground(new Color(
                            0,
                            (int)(255 * (1.0f - easedProgress) * pulse),
                            0
                        ));
                    } else {
                        ((Timer)e.getSource()).stop();
                        cardPanel.setVisible(false);
                        
                        // Reset LED to standby state with fade effect
                        Timer ledTimer = new Timer(50, new ActionListener() {
                            int steps = 0;
                            
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                steps++;
                                float fadeProgress = Math.min(1.0f, steps / 10.0f);
                                int green = (int)(255 * (1 - fadeProgress) + 100 * fadeProgress);
                                ledPanel.setBackground(new Color(0, green, 0));
                                
                                if (fadeProgress >= 1.0f) {
                                    ((Timer)evt.getSource()).stop();
                                }
                            }
                        });
                        ledTimer.start();
                    }
                }
            });

            ejectTimer.start();
        }
    }
// Remove extra closing brace

    private JPanel createHardwarePanel() {
        JPanel hardwarePanel = new JPanel(new GridLayout(3, 1, 15, 15));
        hardwarePanel.setBackground(new Color(220, 220, 220));
        hardwarePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create slots with 3D effect and better visibility
        String[] slots = {"RECEIPT", "CARD", "CASH"};
        Color[] slotColors = {
            new Color(200, 200, 200),  // Receipt slot - lighter gray
            new Color(190, 190, 190),  // Card slot - lighter gray
            new Color(180, 180, 180)   // Cash dispenser - lighter gray
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
        // Check if local access is allowed
        if (!Account.isLocalAccess()) {
            JOptionPane.showMessageDialog(null,
                "Access is restricted to local network only.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Create card animation components
        cardPanel = new JPanel() {
            private float shine = 0.0f;
            private boolean shineIncreasing = true;
            private Timer shineTimer;
            
            {
                shineTimer = new Timer(50, _ -> {
                    if (shineIncreasing) {
                        shine += 0.1f;
                        if (shine >= 1.0f) {
                            shine = 1.0f;
                            shineIncreasing = false;
                        }
                    } else {
                        shine -= 0.1f;
                        if (shine <= 0.0f) {
                            shine = 0.0f;
                            shineIncreasing = true;
                        }
                    }
                    repaint();
                });
                shineTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Draw card base with metallic gradient
                GradientPaint metallic = new GradientPaint(
                    0, 0, new Color(60, 60, 60),
                    getWidth(), getHeight(), new Color(40, 40, 40));
                g2d.setPaint(metallic);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Add shine effect
                if (shine > 0) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shine * 0.3f));
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(255, 255, 255, 100),
                        getWidth(), 0, new Color(255, 255, 255, 0)));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
                
                // Add chip with metallic effect
                GradientPaint chipMetallic = new GradientPaint(
                    10, 10, new Color(220, 200, 0),
                    40, 35, new Color(180, 160, 0));
                g2d.setPaint(chipMetallic);
                g2d.fillRoundRect(10, 10, 30, 25, 5, 5);
                
                // Add chip details
                g2d.setColor(new Color(160, 140, 0));
                g2d.setStroke(new BasicStroke(0.5f));
                for (int i = 0; i < 3; i++) {
                    g2d.drawLine(15, 15 + i * 8, 35, 15 + i * 8);
                }
                
                // Add hologram effect
                float hologramAlpha = (float) Math.abs(Math.sin(shine * Math.PI));
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hologramAlpha * 0.5f));
                g2d.setColor(new Color(200, 200, 255));
                g2d.fillOval(getWidth() - 25, 10, 15, 15);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
                // Add magnetic stripe with gradient
                GradientPaint stripePaint = new GradientPaint(
                    0, getHeight() - 20, new Color(30, 30, 30),
                    0, getHeight(), new Color(10, 10, 10));
                g2d.setPaint(stripePaint);
                g2d.fillRect(0, getHeight() - 20, getWidth(), 15);
            }
            
            @Override
            public void removeNotify() {
                super.removeNotify();
                if (shineTimer != null) {
                    shineTimer.stop();
                    shineTimer = null;
                }
            }
        };
        cardPanel.setPreferredSize(new Dimension(85, 54));
        cardPanel.setVisible(false);
        
        JPanel loginPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

        // Screen panel (center) with modern design
        JPanel screenPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create modern dark gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 40, 50),
                    getWidth(), getHeight(), new Color(50, 60, 80));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle highlight lines
                g2d.setColor(new Color(100, 150, 200, 20));
                for (int i = 0; i < getHeight(); i += 40) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        screenPanel.setOpaque(false);

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
        titleLabel.setForeground(new Color(0, 0, 139));
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

        // Add card slot to hardware panel
        JPanel hardwarePanel = createHardwarePanel();
        cardSlotLabel = (JLabel) ((JPanel)hardwarePanel.getComponent(1)).getComponent(0);
        JPanel cardSlotPanel = (JPanel)hardwarePanel.getComponent(1);
        cardSlotPanel.add(cardPanel, BorderLayout.SOUTH);
        
        // Side buttons panel (right)
        JPanel rightButtonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        rightButtonsPanel.setBackground(new Color(230, 230, 230));
        
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
            
            simulateCardInsertion();
            
            // Check if account is blocked
            if (blockedAccounts.containsKey(accNum)) {
                long blockTime = blockedAccounts.get(accNum);
                long currentTime = System.currentTimeMillis();
                if (currentTime - blockTime < BLOCK_DURATION) {
                    long remainingTime = (BLOCK_DURATION - (currentTime - blockTime)) / 1000;
                    simulateCardEjection();
                    JOptionPane.showMessageDialog(loginPanel,
                        String.format("Account is temporarily blocked. Please try again in %d seconds.", remainingTime),
                        "Account Blocked",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    // Unblock account if block duration has passed
                    blockedAccounts.remove(accNum);
                    loginAttempts.remove(accNum);
                }
            }
            
            if (validateLogin(accNum, pin)) {
                currentAccount = accounts.get(accNum);
                // Reset login attempts on successful login
                loginAttempts.remove(accNum);
                // Update welcome label in main menu
                updateWelcomeLabel();
                cardLayout.show(mainPanel, "mainMenu");
                // Clear fields after successful login
                accField.setText("");
                pinField.setText("");
            } else {
                // Increment login attempts
                int attempts = loginAttempts.getOrDefault(accNum, 0) + 1;
                loginAttempts.put(accNum, attempts);
                
                simulateCardEjection();
                if (attempts >= MAX_LOGIN_ATTEMPTS) {
                    // Block account
                    blockedAccounts.put(accNum, System.currentTimeMillis());
                    JOptionPane.showMessageDialog(loginPanel,
                        "Too many failed attempts. Account is blocked for 5 minutes.",
                        "Account Blocked",
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(loginPanel,
                        String.format("Invalid Account Number or PIN. %d attempts remaining.",
                            MAX_LOGIN_ATTEMPTS - attempts),
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                }
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
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create modern dark gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 40, 50),
                    getWidth(), getHeight(), new Color(50, 60, 80));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle highlight lines
                g2d.setColor(new Color(100, 150, 200, 20));
                for (int i = 0; i < getHeight(); i += 40) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        screenPanel.setPreferredSize(new Dimension(600, 400));
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        screenPanel.setOpaque(false);

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
        rightButtonsPanel.setBackground(new Color(230, 230, 230));
        
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

    private Map<String, Integer> failedAttempts = new HashMap<>();
    private Map<String, Long> lockoutTime = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION = 300000; // 5 minutes in milliseconds

    private boolean validateLogin(String accNum, String pin) {
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Account number cannot be empty!",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "PIN cannot be empty!",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if account exists
        Account acc = accounts.get(accNum);
        if (acc == null) {
            // Check if account was deleted
            DeletedAccountManager deletedManager = new DeletedAccountManager();
            Optional<DeletedAccount> deletedAccount = deletedManager.getDeletedAccounts().stream()
                .filter(a -> a.getAccountNumber().equals(accNum))
                .findFirst();
            
            if (deletedAccount.isPresent()) {
                JOptionPane.showMessageDialog(this,
                    "This account has been deleted by admin: " + deletedAccount.get().getDeletionReason(),
                    "Account Deleted",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            JOptionPane.showMessageDialog(this,
                "Account does not exist. Please check your account number.",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if account is deleted
        if (acc.isDeleted()) {
            JOptionPane.showMessageDialog(this,
                "This account has been deleted by admin: " + acc.getDeletionReason(),
                "Account Deleted",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if account is blocked by admin
        if (acc.isBlocked()) {
            JOptionPane.showMessageDialog(this,
                "This account has been blocked by the administrator.\nPlease contact customer service for assistance.",
                "Account Blocked",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if account is temporarily locked due to failed attempts
        Long lockTime = lockoutTime.get(accNum);
        if (lockTime != null) {
            long remainingTime = (lockTime + LOCKOUT_DURATION - System.currentTimeMillis()) / 1000;
            if (remainingTime > 0) {
                JOptionPane.showMessageDialog(this,
                    String.format("Account is temporarily locked. Please try again in %d seconds.", remainingTime),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                // Reset lockout if time has expired
                lockoutTime.remove(accNum);
                failedAttempts.remove(accNum);
            }
        }

        if (acc.getPin().equals(pin)) {
            // Reset failed attempts on successful login
            failedAttempts.remove(accNum);
            currentAccount = acc;
            return true;
        } else {
            // Increment failed attempts
            int attempts = failedAttempts.getOrDefault(accNum, 0) + 1;
            failedAttempts.put(accNum, attempts);

            if (attempts >= MAX_ATTEMPTS) {
                // Lock the account
                lockoutTime.put(accNum, System.currentTimeMillis());
                JOptionPane.showMessageDialog(this,
                    String.format("Account locked due to %d failed attempts. Please try again in 5 minutes.", MAX_ATTEMPTS),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    String.format("Invalid PIN. %d attempts remaining.", MAX_ATTEMPTS - attempts),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
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

    @SuppressWarnings({ })
    private void createMainMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(20, 20, 30)); // Darker futuristic background

        // Top panel with holographic-style welcome message and system info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 10, 20)); // Deep space blue
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2), // Neon blue border
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Enhanced clock panel with pulsing effect
        JPanel clockPanel = new JPanel(new GridLayout(2, 1));
        clockPanel.setBackground(new Color(0, 10, 20));
        JLabel clockLabel = new JLabel();
        JLabel dateLabel = new JLabel();
        clockLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        dateLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        
        // Create pulsing effect for clock
        Timer pulseTimer = new Timer(1000, _ -> {
            clockLabel.setForeground(new Color(0, 255, 255)); // Cyan
            new Timer(500, _ -> {
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
        Timer clockTimer = new Timer(50, _ -> {
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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
        Timer glowTimer = new Timer(1500, _ -> {
            float[] hsb = Color.RGBtoHSB(0, 255, 255, null);
            userLabel.setForeground(Color.getHSBColor(hsb[0], hsb[1], 
                (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() / 500.0))));
        });
        glowTimer.start();

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(userLabel);
        welcomePanel.add(accountLabel);

        // Spacer panel for better layout
        JPanel spacerPanel = new JPanel();
        spacerPanel.setBackground(new Color(0, 20, 40));
        spacerPanel.setPreferredSize(new Dimension(0, 30));

        // Status panel with modern styling
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(0, 20, 40));
        statusPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 255), 1));

        JLabel statusLabel = new JLabel("System Status: Ready");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 16));
        statusLabel.setForeground(new Color(0, 200, 255));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // News ticker with modern styling
        JPanel tickerPanel = new JPanel(new BorderLayout());
        tickerPanel.setBackground(new Color(0, 20, 40));
        tickerPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 255), 1));
        
        JLabel tickerLabel = new JLabel("Welcome to BDA Bank • Your Trusted Banking Partner • Experience the Future of Banking •");
        tickerLabel.setFont(new Font("Consolas", Font.PLAIN, 16));
        tickerLabel.setForeground(new Color(0, 200, 255));
        
        Timer scrollTimer = new Timer(50, _ -> {
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
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
        ));

        // Holographic display content with modern grid layout and scanline effect
        final JPanel displayContent = new JPanel(new GridLayout(5, 1, 10, 10)) {
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Add scanline effect timer
        Timer scanlineTimer = new Timer(50, _ -> displayContent.repaint());
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
        Timer balanceEffectTimer = new Timer(50, _ -> {
            long time = System.currentTimeMillis();
            float pulse = (float) (0.9 + 0.1 * Math.sin(time / 800.0));
            balanceAmount.setForeground(new Color(0, (int)(255 * pulse), 0));
            JPanel glowPanel = new JPanel();
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
        Timer promoTimer = new Timer(100, _ -> {
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
        JPanel leftButtons = new JPanel(new GridLayout(4, 1, 15, 15));
        leftButtons.setBackground(Color.BLACK);
        leftButtons.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Right side buttons with enhanced size
        JPanel rightButtons = new JPanel(new GridLayout(4, 1, 15, 15));
        rightButtons.setBackground(Color.BLACK);
        rightButtons.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Transaction options - Core banking features
        String[][] options = {
            {"WITHDRAW", "DEPOSIT"},
            {"TRANSFER FUNDS", "CHECK BALANCE"},
            {"TRANSACTION HISTORY", "CHANGE PIN"},
            {"LOG OUT", "EXIT"}
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
                Timer loadingTimer = new Timer(1500, _ -> {
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
            case "LOG OUT" -> showLogoutConfirmation();
            case "EXIT" -> showExitConfirmation();
            case "CANCEL" -> cardLayout.show(mainPanel, "mainMenu");
        }
    }

    @SuppressWarnings("unused")
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

    private void showLogoutConfirmation() {
        JPanel confirmPanel = new JPanel(new BorderLayout(10, 10));
        confirmPanel.setBackground(new Color(0, 20, 40));
        confirmPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel confirmLabel = new JLabel("Are you sure you want to log out?");
        confirmLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        confirmLabel.setForeground(new Color(0, 255, 255));
        confirmLabel.setHorizontalAlignment(JLabel.CENTER);
        confirmPanel.add(confirmLabel, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(
            this,
            confirmPanel,
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            logout();
            simulateCardEjection();
            cardLayout.show(mainPanel, "login");
        }
    }

    private void showExitConfirmation() {
        JPanel confirmPanel = new JPanel(new BorderLayout(10, 10));
        confirmPanel.setBackground(new Color(0, 20, 40));
        confirmPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel confirmLabel = new JLabel("Are you sure you want to exit?");
        confirmLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        confirmLabel.setForeground(new Color(0, 255, 255));
        confirmLabel.setHorizontalAlignment(JLabel.CENTER);
        confirmPanel.add(confirmLabel, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(
            this,
            confirmPanel,
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            simulateCardEjection();
            Timer exitTimer = new Timer(1000, _ -> System.exit(0));
            exitTimer.setRepeats(false);
            exitTimer.start();
        }
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

        cancelButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
            accountField.setText("");
            amountField.setText("");
        });
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
        
        // Create a button panel at the top with copy option for transfers
        JPanel buttonPanel = new JPanel(new GridLayout(1, transactionType.equals("TRANSFER") ? 3 : 2, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create larger buttons
        JButton backButton = createATMButton("BACK", new Color(139, 0, 0));
        JButton doneButton = createATMButton("DONE", new Color(0, 100, 0));
        JButton copyButton = null;
        if (transactionType.equals("TRANSFER")) {
            copyButton = createATMButton("COPY", new Color(0, 100, 130));
        }
        
        // Set preferred size for larger buttons
        Dimension buttonSize = new Dimension(150, 60);
        backButton.setPreferredSize(buttonSize);
        doneButton.setPreferredSize(buttonSize);
        if (copyButton != null) {
            copyButton.setPreferredSize(buttonSize);
        }
        
        buttonPanel.add(backButton);
        if (copyButton != null) {
            buttonPanel.add(copyButton);
        }
        buttonPanel.add(doneButton);
        
        // Add button panel to the top
        receiptPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Store receipt content for copy functionality
        StringBuilder receiptContent = new StringBuilder();
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        // Receipt header with increased spacing
        addReceiptLine(contentPanel, "\n", gbc, 14); // Add extra space at top
        receiptContent.append("\n");
        addReceiptLine(contentPanel, "Banco De AU(BDA) ATM Machine", gbc, 20);
        receiptContent.append("Banco De AU(BDA) ATM Machine\n");
        addReceiptLine(contentPanel, "\n", gbc, 14); // Add space after title
        receiptContent.append("\n");
        addReceiptLine(contentPanel, "================================", gbc, 16);
        receiptContent.append("================================\n");
        
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        addReceiptLine(contentPanel, "Date: " + timestamp, gbc, 14);
        receiptContent.append("Date: " + timestamp + "\n");
        addReceiptLine(contentPanel, "================================", gbc, 16);
        receiptContent.append("================================\n");
        addReceiptLine(contentPanel, "\n", gbc, 14);
        receiptContent.append("\n");
        
        // Transaction details with centered type and decorative borders
        addReceiptLine(contentPanel, "----------------------------------", gbc, 16);
        receiptContent.append("----------------------------------\n");
        addReceiptLine(contentPanel, String.format("%s%s%s", "      ", transactionType, "      "), gbc, 20);
        receiptContent.append(String.format("%s%s%s\n", "      ", transactionType, "      "));
        addReceiptLine(contentPanel, "----------------------------------", gbc, 16);
        receiptContent.append("----------------------------------\n");
        addReceiptLine(contentPanel, "\n", gbc, 14);
        receiptContent.append("\n");
        addReceiptLine(contentPanel, String.format("%-15s ₱%,16.2f", "AMOUNT:", amount), gbc, 16);
        receiptContent.append(String.format("%-15s ₱%,16.2f\n", "AMOUNT:", amount));
        addReceiptLine(contentPanel, String.format("%-15s ₱%,16.2f", "BALANCE:", currentAccount.getBalance()), gbc, 16);
        receiptContent.append(String.format("%-15s ₱%,16.2f\n", "BALANCE:", currentAccount.getBalance()));
        addReceiptLine(contentPanel, "\n", gbc, 14);
        receiptContent.append("\n");
        addReceiptLine(contentPanel, "================================", gbc, 16);
        receiptContent.append("================================\n");
        
        // Account info (masked) with consistent spacing
        addReceiptLine(contentPanel, "\n", gbc, 14);
        receiptContent.append("\n");
        String maskedAccount = String.format("Account: ****%s",
            currentAccount.getAccountNumber().substring(
                Math.max(0, currentAccount.getAccountNumber().length() - 4)));
        addReceiptLine(contentPanel, maskedAccount, gbc, 14);
        receiptContent.append(maskedAccount + "\n");
        addReceiptLine(contentPanel, "\n", gbc, 14);
        receiptContent.append("\n");
        
        // Footer with proper spacing
        addReceiptLine(contentPanel, "================================", gbc, 16);
        receiptContent.append("================================\n");
        addReceiptLine(contentPanel, "Thank you for using our ATM", gbc, 14);
        receiptContent.append("Thank you for using our ATM\n");
        addReceiptLine(contentPanel, "Please take your card", gbc, 14);
        receiptContent.append("Please take your card\n");
        addReceiptLine(contentPanel, "\n", gbc, 14);
        receiptContent.append("\n");

        // Add copy button functionality
        if (copyButton != null) {
            copyButton.addActionListener(_ -> {
                playSound("printer");
                JPanel copyPanel = new JPanel(new GridBagLayout());
                copyPanel.setBackground(Color.BLACK);
                GridBagConstraints copyGbc = new GridBagConstraints();
                copyGbc.insets = new Insets(10, 20, 10, 20);
                copyGbc.gridwidth = GridBagConstraints.REMAINDER;
                copyGbc.anchor = GridBagConstraints.CENTER;

                // Add copy watermark to each line
                String[] lines = receiptContent.toString().split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        addReceiptLine(copyPanel, line + " (COPY)", copyGbc, 14);
                    } else {
                        addReceiptLine(copyPanel, line, copyGbc, 14);
                    }
                }

                // Show copy in a new window
                JFrame copyFrame = new JFrame("Receipt Copy");
                copyFrame.setSize(400, 600);
                copyFrame.setLocationRelativeTo(null);
                copyFrame.add(new JScrollPane(copyPanel));
                copyFrame.setVisible(true);
            });
        }

        receiptPanel.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
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

        dotTimer.addActionListener(_ -> {
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

        progressTimer.addActionListener(_ -> {
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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
            quickButton.addActionListener(_ -> {
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
        Timer scanlineTimer = new Timer(50, _ -> displayPanel.repaint());
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
                Timer processTimer = new Timer(1500, _ -> {
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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
        Timer scanlineTimer = new Timer(50, _ -> displayPanel.repaint());
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

        cancelButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
            accountField.setText("");
            amountField.setText("");
        });
    }

    private void showTransferDialog() {
        JPanel transferPanel = createATMScreen("TRANSFER FUNDS");
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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

        // Create input panel with modern styling
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Enhanced account label with dynamic glow
        JLabel accountLabel = new JLabel("RECIPIENT ACCOUNT NUMBER") {
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
        accountLabel.setFont(new Font("Consolas", Font.BOLD, 24));

        // Enhanced amount label with dynamic glow
        JLabel amountLabel = new JLabel("TRANSFER AMOUNT") {
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

        // Modern account input field with LED-style display
        JTextField accountField = new JTextField(15) {
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

                // Draw text
                g2d.setFont(new Font("Consolas", Font.BOLD, 36));
                g2d.setColor(new Color(0, 255, 255));
                String text = getText();
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, 10, ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
            }
        };
        accountField.setPreferredSize(new Dimension(300, 60));
        accountField.setOpaque(false);
        accountField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        accountField.setForeground(new Color(0, 255, 255));
        accountField.setCaretColor(new Color(0, 255, 255));
        accountField.setFont(new Font("Consolas", Font.BOLD, 36));

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
        JButton transferButton = createTransactionButton("TRANSFER");
        transferButton.setBackground(new Color(0, 100, 0));
        JButton cancelButton = createTransactionButton("CANCEL");
        cancelButton.setBackground(new Color(139, 0, 0));

        // Layout components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(balancePanel, BorderLayout.NORTH);

        gbc.gridwidth = 2;
        gbc.gridy = 0; inputPanel.add(accountLabel, gbc);
        gbc.gridy = 1; inputPanel.add(accountField, gbc);
        gbc.gridy = 2; inputPanel.add(amountLabel, gbc);
        gbc.gridy = 3; inputPanel.add(amountField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(transferButton);
        buttonPanel.add(cancelButton);

        displayPanel.add(topPanel, BorderLayout.NORTH);
        displayPanel.add(inputPanel, BorderLayout.CENTER);
        displayPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(displayPanel, BorderLayout.CENTER);
        transferPanel.add(contentPanel, BorderLayout.CENTER);

        // Add scanline animation
        Timer scanlineTimer = new Timer(50, _ -> displayPanel.repaint());
        scanlineTimer.start();

        mainPanel.add(transferPanel, "transfer");
        cardLayout.show(mainPanel, "transfer");

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

        cancelButton.addActionListener(_ -> {
            playSound("button");
            cardLayout.show(mainPanel, "mainMenu");
            accountField.setText("");
            amountField.setText("");
        });
    }

    private void showReceiptOptionScreen(String transactionType, double amount, String recipientInfo) {
        JPanel receiptPanel = createATMScreen("TRANSACTION RECEIPT");        
        // Create a button panel at the top with 3 buttons for transfer receipts
        JPanel buttonPanel = new JPanel(new GridLayout(1, transactionType.equals("TRANSFER") ? 3 : 2, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create larger buttons
        JButton backButton = createATMButton("BACK", new Color(139, 0, 0));
        JButton doneButton = createATMButton("DONE", new Color(0, 100, 0));
        JButton copyButton = null;
        if (transactionType.equals("TRANSFER")) {
            copyButton = createATMButton("COPY RECEIPT", new Color(0, 100, 130));
        }
        
        // Set preferred size for larger buttons
        Dimension buttonSize = new Dimension(150, 60);
        backButton.setPreferredSize(buttonSize);
        doneButton.setPreferredSize(buttonSize);
        if (copyButton != null) {
            copyButton.setPreferredSize(buttonSize);
        }
        
        buttonPanel.add(backButton);
        buttonPanel.add(doneButton);
        
        // Add button panel to the top
        receiptPanel.add(buttonPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(0, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Header with professional design
        JLabel headerLabel = new JLabel("BDA ATM MACHINE");
        headerLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 120, 160));
        gbc.gridy = GridBagConstraints.RELATIVE;
        contentPanel.add(headerLabel, gbc);
        
        // Professional separator
        addReceiptLine(contentPanel, "══════════════════════════════", gbc, 16);
        
        // Transaction timestamp with clear format
        addReceiptLine(contentPanel, "DATE: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), gbc, 14);
        addReceiptLine(contentPanel, "══════════════════════════════", gbc, 16);
        
        // Transaction details with centered type and decorative borders
        addReceiptLine(contentPanel, "----------------------------------", gbc, 16);
        addReceiptLine(contentPanel, String.format("%s%s%s", "      ", transactionType, "      "), gbc, 20);
        addReceiptLine(contentPanel, "----------------------------------", gbc, 16);
        addReceiptLine(contentPanel, "\n", gbc, 14);
        
        // Amount with proper currency formatting
        addReceiptLine(contentPanel, String.format("%-22s ₱%,15.2f", "AMOUNT:", amount), gbc, 18);
        addReceiptLine(contentPanel, "----------------------------------", gbc, 16);
        
        // Sender information
        addReceiptLine(contentPanel, "FROM:", gbc, 16);
        addReceiptLine(contentPanel, String.format("  %s", currentAccount.getAccountHolder()), gbc, 16);
        addReceiptLine(contentPanel, String.format("  ACC: ****%s",
            currentAccount.getAccountNumber().substring(
                Math.max(0, currentAccount.getAccountNumber().length() - 4))), gbc, 14);
        
        // Recipient information
        if (recipientInfo != null) {
            addReceiptLine(contentPanel, "----------------------------------", gbc, 16);
            addReceiptLine(contentPanel, "TO:", gbc, 16);
            addReceiptLine(contentPanel, String.format("  %s", recipientInfo), gbc, 16);
        }
        
        // Balance and status information
        addReceiptLine(contentPanel, "══════════════════════════════", gbc, 16);
        addReceiptLine(contentPanel, String.format("%-22s ₱%,15.2f", "BALANCE:", currentAccount.getBalance()), gbc, 18);
        addReceiptLine(contentPanel, "══════════════════════════════", gbc, 16);
        
        // Transaction details
        addReceiptLine(contentPanel, String.format("TRANS ID: %s", generateTransactionId()), gbc, 14);
        addReceiptLine(contentPanel, "STATUS: APPROVED", gbc, 14);
        addReceiptLine(contentPanel, "", gbc, 14);
        addReceiptLine(contentPanel, "Thank you for banking with BDA ATM", gbc, 14);
        addReceiptLine(contentPanel, "Please keep this receipt for your records", gbc, 14);

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
        JPanel screenPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create lighter gradient background for better visibility
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(45, 50, 55),
                    getWidth(), getHeight(), new Color(65, 70, 75));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle grid pattern for depth
                g2d.setColor(new Color(75, 80, 85, 30));
                int gridSize = 20;
                for (int i = 0; i < getWidth(); i += gridSize) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += gridSize) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        screenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 65, 70), 15),
            BorderFactory.createLineBorder(new Color(40, 45, 50), 10)
        ));

        // Title panel with enhanced visibility
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background for title
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(30, 35, 40),
                    getWidth(), getHeight(), new Color(40, 45, 50));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titlePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0)));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 0, 139));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        titlePanel.add(titleLabel);

        screenPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Add side panels with improved contrast
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        Color sidePanelColor = new Color(55, 60, 65);
        leftPanel.setBackground(sidePanelColor);
        rightPanel.setBackground(sidePanelColor);
        leftPanel.setPreferredSize(new Dimension(60, 0));
        rightPanel.setPreferredSize(new Dimension(60, 0));
        
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