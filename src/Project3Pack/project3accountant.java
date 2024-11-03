/*
Name: Andrew Fugate
Course: CNT 4714 Fall 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: November 3, 2024
Class: project3accountant.java
*/

package Project3Pack;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

public class project3accountant extends JFrame {
    private JTextArea commandArea;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JButton executeButton;
    private JButton clearCommandButton;
    private JButton clearResultButton;
    private JButton disconnectButton;
    private JButton closeButton;
    private JLabel connectionStatusLabel;
    private JTable resultsTable;
    private JScrollPane resultScrollPane;
    private Connection connection;

    public project3accountant() {
        setupGUI();
    }

    private void setupGUI() {
        setTitle("SQL Accountant Application");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel connectionLabel = new JLabel("Connection Details");
        connectionLabel.setForeground(Color.blue);
        connectionLabel.setBounds(20, 10, 200, 25);

        JLabel commandLabel = new JLabel("Enter SQL Command");
        commandLabel.setForeground(Color.blue);
        commandLabel.setBounds(350, 10, 200, 25);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(20, 40, 100, 25);
        usernameField = new JTextField("theaccountant");
        usernameField.setBounds(130, 40, 150, 25);
        usernameField.setEditable(false);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 80, 100, 25);
        passwordField = new JPasswordField("theaccountant");
        passwordField.setBounds(130, 80, 150, 25);
        passwordField.setEditable(false);

        JLabel dbSelectorLabel = new JLabel("DB URL Properties");
        dbSelectorLabel.setBounds(20, 120, 150, 25);
        JTextField dbField = new JTextField("operationslog.properties");
        dbField.setBounds(130, 120, 150, 25);
        dbField.setEditable(false);

        JLabel userSelectorLabel = new JLabel("User Properties");
        userSelectorLabel.setBounds(20, 160, 150, 25);
        JTextField userField = new JTextField("theaccountant.properties");
        userField.setBounds(130, 160, 150, 25);
        userField.setEditable(false);

        connectButton = new JButton("Connect");
        connectButton.setBounds(20, 200, 120, 35);
        connectButton.setBackground(Color.GREEN);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.setBounds(150, 200, 130, 35);
        disconnectButton.setBackground(Color.RED);

        clearCommandButton = new JButton("Clear Command");
        clearCommandButton.setBounds(350, 250, 150, 35);
        clearCommandButton.setBackground(Color.YELLOW);

        clearResultButton = new JButton("Clear Results");
        clearResultButton.setBounds(510, 250, 150, 35);
        clearResultButton.setBackground(Color.YELLOW);

        executeButton = new JButton("Execute");
        executeButton.setBounds(670, 250, 130, 35);
        executeButton.setBackground(Color.CYAN);

        closeButton = new JButton("Close");
        closeButton.setBounds(670, 290, 130, 35);
        closeButton.setBackground(Color.PINK);

        commandArea = new JTextArea(5, 40);
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        JScrollPane commandScrollPane = new JScrollPane(commandArea);
        commandScrollPane.setBounds(350, 40, 450, 180);

        connectionStatusLabel = new JLabel("NO CONNECTION ESTABLISHED");
        connectionStatusLabel.setOpaque(true);
        connectionStatusLabel.setBackground(Color.BLACK);
        connectionStatusLabel.setForeground(Color.RED);
        connectionStatusLabel.setBounds(20, 290, 640, 35);

        resultsTable = new JTable(new DefaultTableModel());
        resultsTable.setBorder(null);
        resultScrollPane = new JScrollPane(resultsTable);
        resultScrollPane.setBounds(20, 350, 780, 230);

        add(connectionLabel);
        add(commandLabel);
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(dbSelectorLabel);
        add(dbField);
        add(userSelectorLabel);
        add(userField);
        add(connectButton);
        add(disconnectButton);
        add(clearCommandButton);
        add(clearResultButton);
        add(executeButton);
        add(closeButton);
        add(commandScrollPane);
        add(connectionStatusLabel);
        add(resultScrollPane);

        connectButton.addActionListener(new ConnectListener());
        executeButton.addActionListener(new ExecuteListener());
        clearCommandButton.addActionListener(e -> commandArea.setText(""));
        clearResultButton.addActionListener(e -> ((DefaultTableModel) resultsTable.getModel()).setRowCount(0));
        disconnectButton.addActionListener(new DisconnectListener());
        closeButton.addActionListener(e -> System.exit(0));
    }

    private void connectToDatabase() {
        String enteredPassword = new String(passwordField.getPassword());
        try {
            Properties dbProps = new Properties();
            InputStream dbInput = getClass().getResourceAsStream("/Project3Pack/operationslog.properties");

            if (dbInput == null) {
                JOptionPane.showMessageDialog(this, "Error: Database properties file not found", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dbProps.load(dbInput);
            String dbUrl = dbProps.getProperty("db.url");
            String dbDriver = dbProps.getProperty("db.driver");

            Properties userProps = new Properties();
            InputStream userInput = getClass().getResourceAsStream("/Project3Pack/theaccountant.properties");

            if (userInput == null) {
                JOptionPane.showMessageDialog(this, "Error: User properties file not found", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            userProps.load(userInput);
            String storedPassword = userProps.getProperty("db.password");

            if (!enteredPassword.equals(storedPassword)) {
                JOptionPane.showMessageDialog(this, "Error: Incorrect password", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Class.forName(dbDriver);
            connection = DriverManager.getConnection(dbUrl, "theaccountant", enteredPassword);
            connectionStatusLabel.setText("CONNECTED TO: " + dbUrl);
            connectionStatusLabel.setBackground(Color.YELLOW);
            connectionStatusLabel.setForeground(Color.BLACK);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeSQL(String sql) {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "No active connection.", "Execution Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Modify SQL to handle the `@localhost` suffix
        sql = sql.replaceAll("where login_username = \"(\\w+)\"", "where login_username LIKE \"$1%\"");

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            boolean isResultSet = preparedStatement.execute();

            if (isResultSet) {
                ResultSet rs = preparedStatement.getResultSet();
                displayResultSet(rs);
            } else {
                JOptionPane.showMessageDialog(this, "Selection only permitted", "Execution Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error executing SQL: " + e.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResultSet(ResultSet rs) throws SQLException {
        DefaultTableModel tableModel = new DefaultTableModel();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            tableModel.addColumn(metaData.getColumnName(i));
        }

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }

        resultsTable.setModel(tableModel);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < resultsTable.getColumnModel().getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    private class ConnectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            connectToDatabase();
        }
    }

    private class ExecuteListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            executeSQL(commandArea.getText());
        }
    }

    private class DisconnectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    connectionStatusLabel.setText("NO CONNECTION ESTABLISHED");
                    connectionStatusLabel.setBackground(Color.BLACK);
                    connectionStatusLabel.setForeground(Color.RED);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(project3accountant.this, "Error disconnecting: " + ex.getMessage(), "Disconnection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new project3accountant().setVisible(true));
    }
}
