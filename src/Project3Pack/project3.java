/*
Name: Andrew Fugate
Course: CNT 4714 Fall 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: November 3, 2024
Class: project3.java
*/
//------------------------------------------------------------------------------------------------------------------------
package Project3Pack;
//------------------------------------------------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
//------------------------------------------------------------------------------------------------------------------------
public class project3 extends JFrame
{
    private JTextArea commandArea;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> dbSelector;
    private JComboBox<String> userSelector;
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
    private Connection logConnection;
  //------------------------------------------------------------------------------------------------------------------------
    public project3()
    {
        setupGUI();
        loadPropertiesOptions();
    }
  //------------------------------------------------------------------------------------------------------------------------
    private void setupGUI()
    {
        setTitle("SQL Client Application");
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
        usernameField = new JTextField(10);
        usernameField.setBounds(130, 40, 150, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 80, 100, 25);
        passwordField = new JPasswordField(10);
        passwordField.setBounds(130, 80, 150, 25);

        JLabel dbSelectorLabel = new JLabel("Properties File:");
        dbSelectorLabel.setBounds(20, 120, 100, 25);
        dbSelector = new JComboBox<>();
        dbSelector.setBounds(130, 120, 150, 25);

        JLabel userSelectorLabel = new JLabel("User:");
        userSelectorLabel.setBounds(20, 160, 100, 25);
        userSelector = new JComboBox<>();
        userSelector.setBounds(130, 160, 150, 25);

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
        connectionStatusLabel.setBounds(20, 250, 320, 35);

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
        add(connectionStatusLabel);
        add(resultScrollPane);

        connectButton.addActionListener(new ConnectListener());
        executeButton.addActionListener(new ExecuteListener());
        clearCommandButton.addActionListener(e -> commandArea.setText(""));
        clearResultButton.addActionListener(e -> ((DefaultTableModel) resultsTable.getModel()).setRowCount(0));
        disconnectButton.addActionListener(new DisconnectListener());
        closeButton.addActionListener(e -> System.exit(0));
    }
  //------------------------------------------------------------------------------------------------------------------------
    private void loadPropertiesOptions()
    {
        dbSelector.addItem("project3.properties");
        dbSelector.addItem("bikedb.properties");

        userSelector.addItem("root.properties");
        userSelector.addItem("client1.properties");
        userSelector.addItem("client2.properties");
    }
  //------------------------------------------------------------------------------------------------------------------------
    private void connectToDatabase(String enteredUsername, String enteredPassword, String dbPropertiesFile, String userPropertiesFile)
    {
        try
        {
            Properties dbProps = new Properties();
            InputStream dbInput = getClass().getResourceAsStream("/Project3Pack/" + dbPropertiesFile);

            if (dbInput == null)
            {
                JOptionPane.showMessageDialog(this, "Error: Database properties file not found", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dbProps.load(dbInput);
            String dbUrl = dbProps.getProperty("db.url");
            String dbDriver = dbProps.getProperty("db.driver");

            Properties userProps = new Properties();
            InputStream userInput = getClass().getResourceAsStream("/Project3Pack/" + userPropertiesFile);

            if (userInput == null)
            {
                JOptionPane.showMessageDialog(this, "Error: User properties file not found", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            userProps.load(userInput);
            String storedUsername = userProps.getProperty("db.username");
            String storedPassword = userProps.getProperty("db.password");

            if (!enteredUsername.equals(storedUsername) || !enteredPassword.equals(storedPassword))
            {
                JOptionPane.showMessageDialog(this, "Error: Incorrect username or password. Credentials Do Not Match Properties File!", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Class.forName(dbDriver);
            connection = DriverManager.getConnection(dbUrl, enteredUsername, enteredPassword);
            connectionStatusLabel.setText("CONNECTED TO: " + dbUrl);
            connectionStatusLabel.setBackground(Color.YELLOW);
            connectionStatusLabel.setForeground(Color.BLACK);

            // Establish log connection with project3app credentials
            Properties logProps = new Properties();
            InputStream logInput = getClass().getResourceAsStream("/Project3Pack/project3app.properties");

            if (logInput == null)
            {
                JOptionPane.showMessageDialog(this, "Error: Logging properties file not found", "Logging Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            logProps.load(logInput);
            String logUrl = logProps.getProperty("db.url");
            String logDriver = logProps.getProperty("db.driver");
            String logUser = logProps.getProperty("db.username");
            String logPassword = logProps.getProperty("db.password");

            Class.forName(logDriver);
            logConnection = DriverManager.getConnection(logUrl, logUser, logPassword);
        }
        
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    private void executeSQL(String sql)
    {
        if (connection == null)
        {
            JOptionPane.showMessageDialog(this, "No active connection.", "Execution Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            boolean isResultSet = preparedStatement.execute();

            if (isResultSet)
            {
                ResultSet rs = preparedStatement.getResultSet();
                displayResultSet(rs);
                logOperation("queries");
            }
            
            else
            {
                int updateCount = preparedStatement.getUpdateCount();
                JOptionPane.showMessageDialog(this, "Executed successfully, affected rows: " + updateCount, "Execution Success", JOptionPane.INFORMATION_MESSAGE);

                if (sql.trim().toUpperCase().startsWith("INSERT") || sql.trim().toUpperCase().startsWith("UPDATE") || sql.trim().toUpperCase().startsWith("DELETE"))
                {
                    logOperation("updates");
                }
            }

        }
        
        catch (SQLException e)
        {
            JOptionPane.showMessageDialog(this, "Error executing SQL: " + e.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    private void displayResultSet(ResultSet rs) throws SQLException
    {
        DefaultTableModel tableModel = new DefaultTableModel();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++)
        {
            tableModel.addColumn(metaData.getColumnName(i));
        }

        while (rs.next())
        {
            Object[] row = new Object[columnCount];
            
            for (int i = 1; i <= columnCount; i++)
            {
                row[i - 1] = rs.getObject(i);
            }
            
            tableModel.addRow(row);
        }

        resultsTable.setModel(tableModel);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < resultsTable.getColumnModel().getColumnCount(); i++)
        {
            resultsTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    private void logOperation(String operationType)
    {
        if (logConnection == null)
        {
            JOptionPane.showMessageDialog(this, "Logging connection not established.", "Logging Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
            logConnection.setAutoCommit(false);

            try (PreparedStatement stmt = logConnection.prepareStatement( "UPDATE operationscount SET num_" + operationType + " = num_" + operationType + " + 1 WHERE login_username = ?"))
            {
                stmt.setString(1, usernameField.getText());
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0)
                {
                    try (PreparedStatement insertStmt = logConnection.prepareStatement( "INSERT INTO operationscount (login_username, num_queries, num_updates) VALUES (?, ?, ?)"))
                    {
                        insertStmt.setString(1, usernameField.getText());
                        insertStmt.setInt(2, operationType.equals("queries") ? 1 : 0);
                        insertStmt.setInt(3, operationType.equals("updates") ? 1 : 0);
                        insertStmt.executeUpdate();
                    }
                }
            }

            logConnection.commit();
        }
        
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error logging operation: " + e.getMessage(), "Logging Error", JOptionPane.ERROR_MESSAGE);
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    private class ConnectListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            connectToDatabase(usernameField.getText(), new String(passwordField.getPassword()), dbSelector.getSelectedItem().toString(), userSelector.getSelectedItem().toString());
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    private class ExecuteListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            executeSQL(commandArea.getText());
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    private class DisconnectListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (connection != null && !connection.isClosed())
                {
                    connection.close();
                    connectionStatusLabel.setText("NO CONNECTION ESTABLISHED");
                    connectionStatusLabel.setBackground(Color.BLACK);
                    connectionStatusLabel.setForeground(Color.RED);
                }
                
                if (logConnection != null && !logConnection.isClosed())
                {
                    logConnection.close();
                }
            }
            
            catch (SQLException ex)
            {
                JOptionPane.showMessageDialog(project3.this, "Error disconnecting: " + ex.getMessage(), "Disconnection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
  //------------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new project3().setVisible(true));
    }
}
//------------------------------------------------------------------------------------------------------------------------