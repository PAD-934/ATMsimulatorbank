# BDA ATM System

## Overview
BDA ATM System is a sophisticated ATM simulation system that provides both desktop and web-based interfaces for banking operations. The system features a modern, user-friendly interface with advanced security measures and real-time transaction processing.

## System Features

### User Features
- Account Management
  - Create new accounts
  - Login with secure authentication
  - View account balance
  - Update account information

### Transaction Features
- Cash Withdrawal
- Cash Deposit
- Balance Inquiry
- Quick Cash Options
- Fund Transfer
- Transaction History

### Security Features
- Local network access only
- Account blocking after 3 failed login attempts
- 5-minute cooldown period for blocked accounts
- Secure transaction processing
- Card simulation with visual feedback

### Admin Features
- Admin dashboard for account management
- View and manage user accounts
- Track deleted accounts
- Monitor system activity

## System Requirements
- Java Runtime Environment (JRE) 8 or higher
- Modern web browser (Chrome recommended)
- Local network connection

## Accessing the System

### Desktop Application
1. Navigate to the ATMsimulatorbank directory
2. Run `compile.bat` to compile the application
3. Execute `run.bat` to start the ATM system

### Web Access (Local Network Only)
1. Ensure you are connected to the local network
2. Open Chrome browser
3. Navigate to `http://localhost:8080` when the system is running
4. The system will automatically validate your network access

### Security Note
For security reasons, the system is restricted to local network access only. Attempts to access from external networks will be blocked.

## User Guide

### Creating a New Account
1. Click "Sign Up" on the main screen
2. Enter required information:
   - Account number
   - PIN
   - Initial deposit
3. Follow the on-screen instructions

### Performing Transactions
1. Insert card (simulated)
2. Enter PIN
3. Select transaction type
4. Follow the prompts to complete the transaction

### Admin Access
1. Launch the admin interface
2. Login with admin credentials
3. Access dashboard for system management

## Sound Effects
The system includes realistic sound effects for:
- Button clicks
- Card insertion/ejection
- Cash dispensing
- Receipt printing
- Error notifications

## Support
For technical support or issues:
1. Check the system logs
2. Contact system administrator
3. Refer to documentation

## Development
Built using Java Swing for the desktop interface and modern web technologies for browser access. Features a responsive design and intuitive user interface with real-time feedback and animations.