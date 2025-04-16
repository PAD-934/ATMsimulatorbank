package main.java;

import java.time.LocalDateTime;

public class DeletedAccount {
    private String accountNumber;
    private String accountHolder;
    private double finalBalance;
    private LocalDateTime deletionTime;
    private String deletionReason;

    public DeletedAccount(String accountNumber, String accountHolder, double finalBalance, String deletionReason) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.finalBalance = finalBalance;
        this.deletionTime = LocalDateTime.now();
        this.deletionReason = deletionReason;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public double getFinalBalance() {
        return finalBalance;
    }

    public LocalDateTime getDeletionTime() {
        return deletionTime;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%.2f,%s,%s", 
            accountNumber, 
            accountHolder, 
            finalBalance, 
            deletionTime, 
            deletionReason);
    }

    public static DeletedAccount fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid deleted account data format");
        }
        
        DeletedAccount account = new DeletedAccount(
            parts[0], // accountNumber
            parts[1], // accountHolder
            Double.parseDouble(parts[2]), // finalBalance
            parts[4]  // deletionReason
        );
        account.deletionTime = LocalDateTime.parse(parts[3]);
        return account;
    }
}