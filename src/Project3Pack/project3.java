/*
Name: Andrew Fugate
Course: CNT 4714 Fall 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: November 3, 2024
Class: project3.java
*/

package Project3Pack;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class project3 extends JFrame {
    private JTextArea commandArea;
    private JTextArea resultArea;
    private JTextField usernameField;
    private JTextField passwordField;
    
    private JComboBox<String> userSelector;
    private JComboBox<String> dbSelector;
    
    private JButton connectButton;
    private JButton executeButton;
    private JButton clearCommandButton;
    private JButton clearResultButton;
    private JButton closeButton;
    private JButton disconnectButton;
    
    private Connection connection;
    private JScrollPane resultScrollPane;

    public project3() {
        setupGUI();
        loadPropertiesOptions();
    }

    private void setupGUI() {
        setTitle("SQL Client Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Using null layout for precise control

        // Labels
        JLabel connectionLabel = new JLabel("Connection Details");
        connectionLabel.setForeground(Color.blue);
        connectionLabel.setBounds(10, 10, 200, 20);
        
        JLabel commandLabel = new JLabel("Enter SQL Command");
        commandLabel.setForeground(Color.blue);
        commandLabel.setBounds(300, 10, 200, 20);

        // Username and Password Fields with Labels
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(10, 40, 100, 25);
        usernameField = new JTextField(10);
        usernameField.setBounds(110, 40, 150, 25);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 80, 100, 25);
        passwordField = new JTextField(10);
        passwordField.setBounds(110, 80, 150, 25);

        // Database and User Selection with Labels
        JLabel dbSelectorLabel = new JLabel("Database:");
        dbSelectorLabel.setBounds(10, 120, 100, 25);
        dbSelector = new JComboBox<>();
        dbSelector.setBounds(110, 120, 150, 25);

        JLabel userSelectorLabel = new JLabel("User:");
        userSelectorLabel.setBounds(10, 160, 100, 25);
        userSelector = new JComboBox<>();
        userSelector.setBounds(110, 160, 150, 25);

        // Buttons for connection and execution
        connectButton = new JButton("Connect");
        connectButton.setBounds(10, 200, 150, 30);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.setBounds(10, 240, 150, 30);

        clearCommandButton = new JButton("Clear Command");
        clearCommandButton.setBounds(300, 220, 150, 30);

        clearResultButton = new JButton("Clear Results");
        clearResultButton.setBounds(460, 220, 150, 30);

        executeButton = new JButton("Execute");
        executeButton.setBounds(620, 220, 150, 30);

        closeButton = new JButton("Close");
        closeButton.setBounds(620, 260, 150, 30);

        // SQL Command Area
        commandArea = new JTextArea(5, 40);
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        JScrollPane commandScrollPane = new JScrollPane(commandArea);
        commandScrollPane.setBounds(300, 40, 470, 160);

        // Result Area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBounds(10, 300, 760, 250);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Adding components to JFrame
        add(connectionLabel);
        add(commandLabel);
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(dbSelectorLabel);
        add(dbSelector);
        add(userSelectorLabel);
        add(userSelector);
        add(connectButton);
        add(disconnectButton);
        add(clearCommandButton);
        add(clearResultButton);
        add(executeButton);
        add(closeButton);
        add(commandScrollPane);
        add(resultScrollPane);

        // Adding ActionListeners
        connectButton.addActionListener(new ConnectListener());
        executeButton.addActionListener(new ExecuteListener());
        clearCommandButton.addActionListener(e -> commandArea.setText(""));
        clearResultButton.addActionListener(e -> resultArea.setText(""));
        disconnectButton.addActionListener(new DisconnectListener());
        closeButton.addActionListener(e -> System.exit(0));
    }

    private void loadPropertiesOptions() {
        dbSelector.addItem("project3");
        dbSelector.addItem("bikedb");

        userSelector.addItem("root");
        userSelector.addItem("client1");
        userSelector.addItem("client2");
        userSelector.addItem("project3app");
    }

    private void connectToDatabase(String username, String password, String dbName) {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getResourceAsStream("/Project3Pack/" + dbName + ".properties");
            if (input == null) {
                resultArea.append("Error: Properties file not found\n");
                refreshResultArea();
                return;
            }
            props.load(input);
            String url = props.getProperty("db.url");
            String driver = props.getProperty("db.driver");

            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            resultArea.append("Connected to database: " + dbName + "\n");
            refreshResultArea();
        } catch (Exception e) {
            resultArea.append("Error: " + e.getMessage() + "\n");
            refreshResultArea();
        }
    }

    private void executeSQL(String sql) {
        try {
            if (connection == null) {
                resultArea.append("No active connection.\n");
                refreshResultArea();
                return;
            }
            
            sql = sql.trim();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            if (sql.toLowerCase().startsWith("select") || sql.toLowerCase().startsWith("show") || sql.toLowerCase().startsWith("describe")) {
                ResultSet rs = preparedStatement.executeQuery();
                displayResultSet(rs);
            } else {
                int rows = preparedStatement.executeUpdate();
                resultArea.append("Executed successfully, affected rows: " + rows + "\n");
                logOperation("update", usernameField.getText());
            }
            commandArea.setText("");
            refreshResultArea();
        } catch (SQLException e) {
            resultArea.append("Error executing SQL: " + e.getMessage() + "\n");
            refreshResultArea();
        }
    }

    private void displayResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) resultArea.append(", ");
                String columnValue = rs.getString(i);
                resultArea.append(rsmd.getColumnName(i) + ": " + columnValue);
            }
            resultArea.append("\n");
        }
        refreshResultArea();
    }

    private void logOperation(String operationType, String username) {
        try (Connection logConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/operationslog", "project3app", "project3app")) {
            String logSQL = "INSERT INTO operationscount (operation_type, username) VALUES (?, ?)";
            PreparedStatement logStmt = logConn.prepareStatement(logSQL);
            logStmt.setString(1, operationType);
            logStmt.setString(2, username);
            logStmt.executeUpdate();
        } catch (SQLException e) {
            resultArea.append("Error logging operation: " + e.getMessage() + "\n");
            refreshResultArea();
        }
    }

    private void refreshResultArea() {
        resultArea.revalidate();
        resultArea.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = resultScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    private class ConnectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            connectToDatabase(usernameField.getText(), passwordField.getText(), dbSelector.getSelectedItem().toString());
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
                    resultArea.append("Disconnected from database.\n");
                    refreshResultArea();
                }
            } catch (SQLException ex) {
                resultArea.append("Error disconnecting: " + ex.getMessage() + "\n");
                refreshResultArea();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new project3().setVisible(true));
    }
}
