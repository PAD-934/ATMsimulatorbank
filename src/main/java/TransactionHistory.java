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
        
        public Transaction(String accountNumber, String type, double amount, long timestamp) {
            this.accountNumber = accountNumber;
            this.type = type;
            this.amount = amount;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("%s - %s: $%.2f on %s", 
                    type, accountNumber, amount, sdf.format(new Date(timestamp)));
        }
    }
    
    public static void saveTransaction(String accountNumber, String type, double amount) {
        try (FileWriter fw = new FileWriter(TRANSACTION_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(String.format("%s,%s,%.2f,%d", 
                accountNumber, type, amount, System.currentTimeMillis()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error saving transaction: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void showTransactionHistory(String accountNumber) {
        List<Transaction> transactions = getTransactions(accountNumber);
        
        JFrame frame = new JFrame("Transaction History");
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table model
        String[] columns = {"Date", "Type", "Amount"};
        Object[][] data = new Object[transactions.size()][3];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i][0] = sdf.format(new Date(t.timestamp));
            data[i][1] = t.type;
            data[i][2] = String.format("$%.2f", t.amount);
        }
        
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add total balance
        double total = transactions.stream()
            .mapToDouble(t -> t.type.startsWith("Withdrawal") || t.type.startsWith("Transfer to") 
                ? -t.amount : t.amount)
            .sum();
        JLabel totalLabel = new JLabel(String.format("Total Balance: $%.2f", total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(totalLabel, BorderLayout.SOUTH);
        
        frame.add(panel);
        frame.setVisible(true);
    }
    
    public static List<Transaction> getTransactions(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(accountNumber)) {
                    transactions.add(new Transaction(
                            parts[0], parts[1], Double.parseDouble(parts[2]), Long.parseLong(parts[3])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}