package main.java;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints;


public class Account {
    private String accountNumber;
    private String pin;
    private double balance;
    private String accountHolder;
    private List<Transaction> transactionHistory;

    public Account(String accountNumber, String pin, double balance, String accountHolder) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.accountHolder = accountHolder;
        this.transactionHistory = new ArrayList<>();
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

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance += amount;
        addTransaction("DEPOSIT", amount, this.balance, "Cash deposit");
        TransactionHistory.saveTransaction(accountNumber, "DEPOSIT", amount, accountHolder);
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance -= amount;
        addTransaction("WITHDRAWAL", amount, this.balance, "Cash withdrawal");
        TransactionHistory.saveTransaction(accountNumber, "WITHDRAWAL", amount, accountHolder);
    }

    public void transfer(double amount, Account recipient, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Insufficient funds for transfer");
        }
        this.balance -= amount;
        recipient.balance += amount;
        
        // Record transaction for sender with detailed description
        String senderDesc = String.format("Transfer to Account %s (%s): %s", 
            recipient.getAccountNumber(), 
            recipient.getAccountHolder(),
            description);
        addTransaction("TRANSFER_OUT", amount, this.balance, senderDesc);
        TransactionHistory.saveTransaction(accountNumber, "TRANSFER_OUT", amount, senderDesc);
        
        // Record transaction for recipient with detailed description
        String recipientDesc = String.format("Transfer from Account %s (%s): %s", 
            this.accountNumber,
            this.accountHolder,
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
        addTransaction("CARD_EJECTED", 0.0, balance, "Card ejected from ATM");
        TransactionHistory.saveTransaction(accountNumber, "CARD_EJECTED", 0.0, accountHolder);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    private void addTransaction(String type, double amount, double balanceAfter, String description) {
        Transaction transaction = new Transaction(type, amount, balanceAfter, description);
        transactionHistory.add(transaction);
    }

    public void addTransaction(Transaction transaction) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addTransaction'");
    }
}