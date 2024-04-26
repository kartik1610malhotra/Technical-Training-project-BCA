
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Random;

public class BankingSystem extends JFrame {
    private Connection connection;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private int currentAccountNumber;

    public BankingSystem() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/technical", "root", "hr8178");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setTitle("Banking System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton submitButton = new JButton("Submit");
        JLabel createAccountLabel = new JLabel("New User? Click here to create an account:");
        JButton createAccountButton = new JButton("Create New Account");

        submitButton.addActionListener(e -> loginUser());
        createAccountButton.addActionListener(e -> createNewAccount()); // Attach action listener to create account button

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(submitButton);
        add(createAccountLabel);
        add(createAccountButton);

        setVisible(true);
    }


    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String dbPassword = resultSet.getString("password");
                if (password.equals(dbPassword)) {
                    // Successfully logged in
                    dispose();
                    currentAccountNumber = resultSet.getInt("account_number");
                    showMainMenu();
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showMainMenu() {
        JFrame mainMenuFrame = new JFrame("Main Menu");
        mainMenuFrame.setSize(300, 200);
        mainMenuFrame.setLayout(new GridLayout(4, 1));

        JButton withdrawButton = new JButton("Withdraw Money");
        JButton checkBalanceButton = new JButton("Check Balance");
        JButton changePasswordButton = new JButton("Change Password");
        JButton depositButton = new JButton("Deposit Money");

        withdrawButton.addActionListener(e -> withdrawMoney());
        checkBalanceButton.addActionListener(e -> checkBalance());
        changePasswordButton.addActionListener(e -> changePassword());
        depositButton.addActionListener(e -> depositMoney());

        mainMenuFrame.add(withdrawButton);
        mainMenuFrame.add(checkBalanceButton);
        mainMenuFrame.add(changePasswordButton);
        mainMenuFrame.add(depositButton);

        mainMenuFrame.setVisible(true);
    }

    private void createNewAccount() {
        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField depositField = new JTextField();

        Object[] message = {
                "Name:", nameField,
                "Username:", usernameField,
                "Password:", passwordField,
                "Initial Deposit:", depositField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Create New Account", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            double initialDeposit = Double.parseDouble(depositField.getText());

            int accountNumber = generateAccountNumber();
            saveNewAccount(name, username, password, initialDeposit, accountNumber);

            JOptionPane.showMessageDialog(null, "Account created successfully!\nAccount Number: " + accountNumber +
                    "\nUsername: " + username + "\nPassword: " + password + "\nBalance: " + initialDeposit);
        }
    }


    private int generateAccountNumber() {
        Random rand = new Random();
        return rand.nextInt(900000) + 100000; // 6-digit random number
    }

    private void saveNewAccount(String name, String username, String password, double balance, int accountNumber) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO user (name, username, password, balance, account_number) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.setDouble(4, balance);
            statement.setInt(5, accountNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void withdrawMoney() {
        String amountString = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
        if (amountString != null) {
            try {
                double amount = Double.parseDouble(amountString);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String password = JOptionPane.showInputDialog(this, "Enter your password:");
                if (password != null) {
                    if (checkPassword(password)) {
                        double currentBalance = getBalance();
                        if (amount <= currentBalance) {
                            updateBalance(currentBalance - amount);
                            JOptionPane.showMessageDialog(this, "Withdraw successful. Money left: " + (currentBalance - amount), "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkBalance() {
        String password = JOptionPane.showInputDialog(this, "Enter your password:");
        if (password != null) {
            if (checkPassword(password)) {
                double currentBalance = getBalance();
                JOptionPane.showMessageDialog(this, "Account number: " + currentAccountNumber + "\nBalance: " + currentBalance, "Balance", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changePassword() {
        String currentPassword = JOptionPane.showInputDialog(this, "Enter current password:");
        if (currentPassword != null) {
            if (checkPassword(currentPassword)) {
                String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
                if (newPassword != null) {
                    updatePassword(newPassword);
                    JOptionPane.showMessageDialog(this, "Password changed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void depositMoney() {
        String amountString = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
        if (amountString != null) {
            try {
                double amount = Double.parseDouble(amountString);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String password = JOptionPane.showInputDialog(this, "Enter your password:");
                if (password != null) {
                    if (checkPassword(password)) {
                        double currentBalance = getBalance();
                        updateBalance(currentBalance + amount);
                        JOptionPane.showMessageDialog(this, "Deposit successful. Updated balance: " + (currentBalance + amount), "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean checkPassword(String password) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user WHERE account_number = ? AND password = ?");
            statement.setInt(1, currentAccountNumber);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private double getBalance() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT balance FROM user WHERE account_number = ?");
            statement.setInt(1, currentAccountNumber);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getDouble("balance");
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateBalance(double newBalance) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE user SET balance = ? WHERE account_number = ?");
            statement.setDouble(1, newBalance);
            statement.setInt(2, currentAccountNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePassword(String newPassword) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE user SET password = ? WHERE account_number = ?");
            statement.setString(1, newPassword);
            statement.setInt(2, currentAccountNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingSystem::new);
    }
}
