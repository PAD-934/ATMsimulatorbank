package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeletedAccountManager {
    private static final String DELETED_ACCOUNTS_FILE = "deleted_accounts.txt";
    private List<DeletedAccount> deletedAccounts;

    public DeletedAccountManager() {
        deletedAccounts = new ArrayList<>();
        loadDeletedAccounts();
    }

    public void addDeletedAccount(Account account, String reason) {
        DeletedAccount deletedAccount = new DeletedAccount(
            account.getAccountNumber(),
            account.getAccountHolder(),
            account.getBalance(),
            reason
        );
        deletedAccounts.add(deletedAccount);
        saveDeletedAccounts();
    }

    public List<DeletedAccount> getDeletedAccounts() {
        return new ArrayList<>(deletedAccounts);
    }

    public Optional<DeletedAccount> restoreAccount(String accountNumber) {
        Optional<DeletedAccount> accountToRestore = deletedAccounts.stream()
            .filter(acc -> acc.getAccountNumber().equals(accountNumber))
            .findFirst();
        
        accountToRestore.ifPresent(account -> {
            deletedAccounts.remove(account);
            saveDeletedAccounts();
        });
        
        return accountToRestore;
    }

    private void loadDeletedAccounts() {
        File file = new File(DELETED_ACCOUNTS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    DeletedAccount account = DeletedAccount.fromString(line);
                    deletedAccounts.add(account);
                } catch (Exception e) {
                    System.err.println("Error parsing deleted account: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading deleted accounts: " + e.getMessage());
        }
    }

    private void saveDeletedAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DELETED_ACCOUNTS_FILE))) {
            for (DeletedAccount account : deletedAccounts) {
                writer.println(account.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving deleted accounts: " + e.getMessage());
        }
    }
}