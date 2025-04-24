package main.java;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class Account {
    private String accountNumber;
    private String pin;
    private double balance;
    private String accountHolder;
    private List<Transaction> transactionHistory;
    private boolean blocked;
    private boolean deleted;
    private String deletionReason;
    private int dailyTransactionCount;
    private long lastTransactionDate;
    private static final int MAX_DAILY_TRANSACTIONS = 10;
    private static final String LOCALHOST = "127.0.0.1";
    
    public static boolean isLocalAccess() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();
            return hostAddress.equals(LOCALHOST) || hostAddress.startsWith("192.168.") || hostAddress.startsWith("10.");
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    public static String validateAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return "Account number cannot be empty";
        }
        // Add more validation rules as needed
        return null; // null means validation passed
    }

    public Account(String accountNumber, String pin, double balance, String accountHolder) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.accountHolder = accountHolder;
        this.transactionHistory = new ArrayList<>();
        this.blocked = false;
        this.deleted = false;
        this.deletionReason = null;
        this.dailyTransactionCount = 0;
        this.lastTransactionDate = System.currentTimeMillis();
        // Add initial deposit as first transaction
        addTransaction("INITIAL_DEPOSIT", balance, balance, "Account opening deposit");
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
        addTransaction("PIN_CHANGE", 0.0, balance, "PIN changed");
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
        addTransaction("ACCOUNT_UPDATE", 0.0, balance, "Account holder updated");
    }

    public void setAccountNumber(String newAccountNumber) {
        String validationResult = validateAccount(newAccountNumber);
        if (validationResult != null) {
            throw new IllegalArgumentException(validationResult);
        }
        String oldAccountNumber = this.accountNumber;
        this.accountNumber = newAccountNumber;
        addTransaction("ACCOUNT_NUMBER_CHANGE", 0.0, balance, 
            String.format("Account number changed from %s to %s", oldAccountNumber, newAccountNumber));
        TransactionHistory.saveTransaction(newAccountNumber, "ACCOUNT_NUMBER_CHANGE", 0.0, accountHolder);
    }

    public static String validatePin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return "PIN cannot be empty";
        }
        if (!pin.matches("^\\d{4,6}$")) {
            return "PIN must be 4-6 digits";
        }
        return null; // validation passed
    }

    public void updatePin(String oldPin, String newPin) {
        if (!this.pin.equals(oldPin)) {
            throw new IllegalArgumentException("Current PIN is incorrect");
        }
        String validationResult = validatePin(newPin);
        if (validationResult != null) {
            throw new IllegalArgumentException(validationResult);
        }
        this.pin = newPin;
        addTransaction("PIN_CHANGE", 0.0, balance, "PIN updated successfully");
        TransactionHistory.saveTransaction(accountNumber, "PIN_CHANGE", 0.0, accountHolder);
    }

    public void updateAccount(String newAccountNumber, String oldPin, String newPin, String newAccountHolder, double depositAmount) {
        if (deleted) {
            throw new IllegalStateException("Account has been deleted: " + deletionReason);
        }
        if (blocked) {
            throw new IllegalStateException("Account is blocked");
        }
        
        // Verify current PIN
        if (!this.pin.equals(oldPin)) {
            throw new IllegalArgumentException("Current PIN is incorrect");
        }
        
        // Update account number if provided
        if (newAccountNumber != null && !newAccountNumber.equals(this.accountNumber)) {
            setAccountNumber(newAccountNumber);
        }
        
        // Update PIN if provided
        if (newPin != null && !newPin.equals(oldPin)) {
            updatePin(oldPin, newPin);
        }
        
        // Update account holder if provided
        if (newAccountHolder != null && !newAccountHolder.equals(this.accountHolder)) {
            setAccountHolder(newAccountHolder);
        }
        
        // Process deposit if amount is provided
        if (depositAmount > 0) {
            deposit(depositAmount);
        }
    }

    public void deposit(double amount) {
        if (deleted) {
            throw new IllegalStateException("Account has been deleted: " + deletionReason);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        checkTransactionLimit();
        this.balance += amount;
        addTransaction("DEPOSIT", amount, this.balance, "Cash deposit");
        TransactionHistory.saveTransaction(accountNumber, "DEPOSIT", amount, accountHolder);
        updateTransactionCount();
    }

    public void withdraw(double amount) {
        if (deleted) {
            throw new IllegalStateException("Account has been deleted: " + deletionReason);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        checkTransactionLimit();
        this.balance -= amount;
        addTransaction("WITHDRAWAL", amount, this.balance, "Cash withdrawal");
        TransactionHistory.saveTransaction(accountNumber, "WITHDRAWAL", amount, accountHolder);
        updateTransactionCount();
    }

    public void transfer(double amount, Account recipient, String description) {
        if (deleted) {
            throw new IllegalStateException("Account has been deleted: " + deletionReason);
        }
        if (recipient.isDeleted()) {
            throw new IllegalStateException("Recipient account has been deleted: " + recipient.getDeletionReason());
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Insufficient funds for transfer");
        }
        checkTransactionLimit();
        this.balance -= amount;
        recipient.balance += amount;
        
        // Record transaction for sender with standardized receipt format
        String senderDesc = String.format("TRANSFER TO:\n  Account: %s\n  Name: %s\n  Amount: ₱%.2f\n  Description: %s", 
            recipient.getAccountNumber(), 
            recipient.getAccountHolder(),
            amount,
            description);
        addTransaction("TRANSFER_OUT", amount, this.balance, senderDesc);
        TransactionHistory.saveTransaction(accountNumber, "TRANSFER_OUT", amount, senderDesc);
        
        // Record transaction for recipient with standardized receipt format
        String recipientDesc = String.format("TRANSFER FROM:\n  Account: %s\n  Name: %s\n  Amount: ₱%.2f\n  Description: %s", 
            this.accountNumber,
            this.accountHolder,
            amount,
            description);
        recipient.addTransaction("TRANSFER_IN", amount, recipient.balance, recipientDesc);
        TransactionHistory.saveTransaction(recipient.getAccountNumber(), "TRANSFER_IN", amount, recipientDesc);
    }

    public void checkBalance() {
        // Show loading animation
        JDialog loadingDialog = new JDialog();
        loadingDialog.setUndecorated(true);
        loadingDialog.setLayout(new BorderLayout(0, 0));
        
        JPanel animationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(getWidth(), getHeight()) / 4;
                
                g2d.setColor(new Color(0, 100, 0));
                g2d.setStroke(new BasicStroke(3));
                
                double angle = (System.currentTimeMillis() % 2000) * 2 * Math.PI / 2000;
                g2d.rotate(angle, centerX, centerY);
                
                for (int i = 0; i < 8; i++) {
                    int alpha = 255 - (i * 32);
                    g2d.setColor(new Color(0, 100, 0, alpha));
                    g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, i * 45, 45);
                }
                
                g2d.dispose();
            }
        };
        
        animationPanel.setPreferredSize(new Dimension(100, 100));
        loadingDialog.add(animationPanel, BorderLayout.CENTER);
        
        JLabel label = new JLabel("Checking Balance...");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        loadingDialog.add(label, BorderLayout.SOUTH);
        
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(null);
        
        // Create timer for animation
        Timer timer = new Timer(50, _ -> animationPanel.repaint());
        timer.start();
        
        // Show dialog for 2 seconds
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(2000);
                return null;
            }
            
            @Override
            protected void done() {
                timer.stop();
                loadingDialog.dispose();
                addTransaction("BALANCE_CHECK", 0.0, balance, "Balance inquiry");
                TransactionHistory.saveTransaction(accountNumber, "BALANCE_CHECK", 0.0, accountHolder);
                
                // Show balance in a new dialog
                JOptionPane.showMessageDialog(null,
                    String.format("Current Balance: ₱%.2f", balance),
                    "Balance Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }

    public void ejectCard() {
        if (!deleted) {
            addTransaction("CARD_EJECTED", 0.0, balance, "Card ejected from ATM");
            TransactionHistory.saveTransaction(accountNumber, "CARD_EJECTED", 0.0, accountHolder);
        }
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeleted(boolean deleted, String reason) {
        this.deleted = deleted;
        this.deletionReason = reason;
        if (deleted) {
            addTransaction("ACCOUNT_DELETED", 0.0, balance, "Account deleted: " + reason);
        }
    }

    private void checkTransactionLimit() {
        long currentTime = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;

        if (currentTime - lastTransactionDate > oneDayInMillis) {
            dailyTransactionCount = 0;
            lastTransactionDate = currentTime;
        }

        if (dailyTransactionCount >= MAX_DAILY_TRANSACTIONS) {
            throw new IllegalStateException("Daily transaction limit reached. Please try again tomorrow.");
        }
    }

    private void updateTransactionCount() {
        dailyTransactionCount++;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        String status = blocked ? "ACCOUNT_BLOCKED" : "ACCOUNT_UNBLOCKED";
        addTransaction(status, 0.0, balance, blocked ? "Account blocked by admin" : "Account unblocked by admin");
        TransactionHistory.saveTransaction(accountNumber, status, 0.0, accountHolder);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void addTransaction(String type, double amount, double newBalance, String description) {
        Transaction transaction = new Transaction(type, amount, newBalance, description);
        transactionHistory.add(transaction);
    }

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactionHistory.add(transaction);
        }
    }
}