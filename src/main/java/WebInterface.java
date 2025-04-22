package main.java;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class WebInterface {
    private static final int PORT = 8080;
    private HttpServer server;
    @SuppressWarnings("unused")
    private ATMInterface atmInterface;
    public WebInterface(ATMInterface atmInterface, HashMap<String, Account> accounts) {
        this.atmInterface = atmInterface;
        initializeServer();
    }

    private void initializeServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newCachedThreadPool());
            
            // Configure routes
            server.createContext("/", new MainHandler());
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/account", new AccountHandler());
            server.createContext("/api/transaction", new TransactionHandler());
            server.createContext("/admin-dashboard", new AdminDashboardHandler());
            
            server.start();
            System.out.println("Web server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start web server: " + e.getMessage());
        }
    }

    private class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isLocalRequest(exchange)) {
                sendResponse(exchange, 403, "Access restricted to local network");
                return;
            }

            String response = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>BDA ATM System</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Consolas', monospace; background: #142030; color: #00ffff; }\n" +
                "        .container { max-width: 800px; margin: 50px auto; padding: 20px; }\n" +
                "        .title { text-align: center; font-size: 2.5em; margin-bottom: 30px; }\n" +
                "        .form-group { margin-bottom: 20px; }\n" +
                "        input { background: #1a2835; border: 2px solid #00ffff; color: #00ffff; padding: 10px; width: 100%; }\n" +
                "        button { background: #00ffff; color: #142030; border: none; padding: 10px 20px; cursor: pointer; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <h1 class='title'>BDA ATM System</h1>\n" +
                "        <div class='form-group'>\n" +
                "            <input type='text' id='accountNumber' placeholder='Account Number'>\n" +
                "        </div>\n" +
                "        <div class='form-group'>\n" +
                "            <input type='password' id='pin' placeholder='PIN'>\n" +
                "        </div>\n" +
                "        <button onclick='login()'>Login</button>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        async function login() {\n" +
                "            const accountNumber = document.getElementById('accountNumber').value;\n" +
                "            const pin = document.getElementById('pin').value;\n" +
                "            try {\n" +
                "                const response = await fetch('/api/login', {\n" +
                "                    method: 'POST',\n" +
                "                    headers: { 'Content-Type': 'application/json' },\n" +
                "                    body: JSON.stringify({ accountNumber, pin })\n" +
                "                });\n" +
                "                const data = await response.json();\n" +
                "                if (data.success) {\n" +
                "                    window.location.href = data.redirectUrl || '/dashboard';\n" +
                "                } else {\n" +
                "                    alert(data.message);\n" +
                "                }\n" +
                "            } catch (error) {\n" +
                "                alert('Login failed. Please try again.');\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

            sendResponse(exchange, 200, response);
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isLocalRequest(exchange)) {
                sendResponse(exchange, 403, "{\"success\": false, \"message\": \"Access restricted to local network\"}");
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                // Parse JSON request
                String username = "";
                String password = "";
                try {
                    // Simple JSON parsing (you might want to use a proper JSON library)
                    username = requestBody.replaceAll(".*\"accountNumber\":\s*\"([^\"]*)\".*", "$1");
                    password = requestBody.replaceAll(".*\"pin\":\s*\"([^\"]*)\".*", "$1");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid request format\"}");
                    return;
                }

                // Authenticate using AdminAccount
                if (AdminAccount.authenticate(username, password)) {
                    String response = "{\"success\": true, \"message\": \"Login successful\", \"redirectUrl\": \"/admin-dashboard\"}"; 
                    sendResponse(exchange, 200, response);
                } else {
                    String response = "{\"success\": false, \"message\": \"Invalid username or password\"}"; 
                    sendResponse(exchange, 401, response);
                }
            } else {
                sendResponse(exchange, 405, "{\"success\": false, \"message\": \"Method not allowed\"}");
            }
        }
    }

    private class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isLocalRequest(exchange)) {
                sendResponse(exchange, 403, "{\"success\": false, \"message\": \"Access restricted to local network\"}");
                return;
            }

            // Handle account-related operations
            String response = "{\"success\": true, \"data\": {\"balance\": 1000}}"; 
            sendResponse(exchange, 200, response);
        }
    }

    private class AdminDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isLocalRequest(exchange)) {
                sendResponse(exchange, 403, "Access restricted to local network");
                return;
            }

            String response = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Admin Dashboard - BDA ATM System</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Consolas', monospace; background: #142030; color: #00ffff; }\n" +
                "        .container { max-width: 1200px; margin: 50px auto; padding: 20px; }\n" +
                "        .title { text-align: center; font-size: 2.5em; margin-bottom: 30px; }\n" +
                "        .dashboard-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }\n" +
                "        .dashboard-card { background: #1a2835; padding: 20px; border: 2px solid #00ffff; }\n" +
                "        .dashboard-card h2 { margin-top: 0; color: #00ffff; }\n" +
                "        .button { background: #00ffff; color: #142030; border: none; padding: 10px 20px; cursor: pointer; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <h1 class='title'>Admin Dashboard</h1>\n" +
                "        <div class='dashboard-grid'>\n" +
                "            <div class='dashboard-card'>\n" +
                "                <h2>System Status</h2>\n" +
                "                <p>ATM System is running normally</p>\n" +
                "            </div>\n" +
                "            <div class='dashboard-card'>\n" +
                "                <h2>Account Management</h2>\n" +
                "                <button class='button'>Manage Accounts</button>\n" +
                "            </div>\n" +
                "            <div class='dashboard-card'>\n" +
                "                <h2>Transaction History</h2>\n" +
                "                <button class='button'>View Transactions</button>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

            sendResponse(exchange, 200, response);
        }
    }

    private class TransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isLocalRequest(exchange)) {
                sendResponse(exchange, 403, "{\"success\": false, \"message\": \"Access restricted to local network\"}");
                return;
            }

            // Handle transaction operations
            String response = "{\"success\": true, \"message\": \"Transaction processed\"}"; 
            sendResponse(exchange, 200, response);
        }
    }

    private boolean isLocalRequest(HttpExchange exchange) {
        String remoteAddress = exchange.getRemoteAddress().getAddress().getHostAddress();
        return remoteAddress.equals("127.0.0.1") || remoteAddress.startsWith("192.168.") || 
               remoteAddress.startsWith("10.") || remoteAddress.startsWith("172.16.");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", 
            response.startsWith("<!DOCTYPE html>") ? "text/html" : "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}