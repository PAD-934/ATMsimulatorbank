package main.java;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class TransactionHistory {
    private static final String TRANSACTION_FILE = "transactions.txt";
    
    public static class Transaction {
        String accountNumber;
        String type;
        double amount;
        long timestamp;
        String description;
        
        public Transaction(String accountNumber, String type, double amount, long timestamp, String description) {
            this.accountNumber = accountNumber;
            this.type = type;
            this.amount = amount;
            this.timestamp = timestamp;
            this.description = description;
        }
        
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("%s - %s: $%.2f on %s", 
                    type, accountNumber, amount, sdf.format(new Date(timestamp)));
        }
    }
    
    public static void saveTransaction(String accountNumber, String type, double amount, String description) {
        try (FileWriter fw = new FileWriter(TRANSACTION_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(String.format("%s,%s,%.2f,%d,%s", 
                accountNumber, type, amount, System.currentTimeMillis(), description));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error saving transaction: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void showTransactionHistory(String accountNumber) {
        List<Transaction> transactions = getTransactions(accountNumber);
        
        JDialog dialog = new JDialog((Frame)null, "Transaction History", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(50, 50, 50));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 30, 30), 15),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(30, 30, 30));
        JLabel titleLabel = new JLabel("TRANSACTION HISTORY");
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 255, 0));
        titlePanel.add(titleLabel);
        
        // Create table model with ATM-style formatting
        String[] columns = {"Date", "Type", "Amount", "Description"};
        Object[][] data = new Object[transactions.size()][4];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i][0] = sdf.format(new Date(t.timestamp));
            data[i][1] = t.type;
            data[i][2] = String.format("₱%.2f", t.amount);
            data[i][3] = t.description;
        }
        
        JTable table = new JTable(data, columns);
        table.setBackground(Color.BLACK);
        table.setForeground(new Color(0, 255, 0));
        table.setFont(new Font("Consolas", Font.PLAIN, 14));
        table.setGridColor(new Color(30, 30, 30));
        table.getTableHeader().setBackground(new Color(30, 30, 30));
        table.getTableHeader().setForeground(new Color(0, 255, 0));
        table.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 14));
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30)));
        
        // Create summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBackground(new Color(30, 30, 30));
        
        double total = transactions.stream()
            .mapToDouble(t -> t.type.startsWith("WITHDRAWAL") || t.type.equals("TRANSFER_OUT") 
                ? -t.amount : t.amount)
            .sum();
            
        JLabel totalLabel = new JLabel(String.format("CURRENT BALANCE: ₱%.2f", total));
        totalLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 255, 0));
        summaryPanel.add(totalLabel);
        
        // Add close button
        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(new Font("Consolas", Font.BOLD, 16));
        closeButton.setBackground(new Color(139, 0, 0));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(50, 50, 50));
        buttonPanel.add(closeButton);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    public static List<Transaction> getTransactions(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(accountNumber) && parts.length >= 5) {
                    transactions.add(new Transaction(
                            parts[0], parts[1], Double.parseDouble(parts[2]), 
                            Long.parseLong(parts[3]), parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    

    // Remove duplicate method since there's already a saveTransaction implementation above
    // This empty duplicate method can be deleted

    }
}