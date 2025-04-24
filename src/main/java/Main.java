package main.java;

public class Main {
    public static void main(String[] args) {
        try {
            // Create ATM interface first
            ATMInterface atmInterface = new ATMInterface();
            
            // Initialize web interface with the same ATM interface instance
            WebInterface webInterface = new WebInterface(atmInterface, atmInterface.getAccounts());
            
            // Print access instructions
            System.out.println("\n=== BDA ATM System Started Successfully ===");
            System.out.println("Desktop Interface: Running in fullscreen mode");
            System.out.println("Web Interface: http://localhost:8080");
            System.out.println("\nAccess Options:");
            System.out.println("1. Use the ATM Interface directly on this computer");
            System.out.println("2. Access via web browser at http://localhost:8080");
            System.out.println("\nPress ESC in the ATM Interface to exit the system");
            System.out.println("=====================================\n");
            
            // Add shutdown hook to stop web server gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down ATM System...");
                webInterface.stop();
                System.out.println("Server stopped successfully.");
            }));
            
        } catch (Exception e) {
            System.err.println("Failed to start ATM System: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}