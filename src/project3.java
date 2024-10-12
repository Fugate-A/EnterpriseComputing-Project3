import javax.swing.*;
import java.awt.*;

public class project3 extends JFrame {

    // Declaring buttons and other components
    private JButton connectButton, disconnectButton, executeButton, clearCommandButton, clearResultsButton, closeButton;
    private JComboBox<String> dbUrlProperties, userProperties;

    public project3() {
        // Set the title of the window
        setTitle("Database Client Application");

        // Set layout for the main window
        setLayout(new BorderLayout(10, 10));

        // Top panel for DB URL and User Properties dropdowns + Username and Password
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));  // 4 rows, 2 columns for dropdowns and text fields
        dbUrlProperties = new JComboBox<>(new String[]{"project3.properties", "bikedb.properties", "operationslog.properties"});
        userProperties = new JComboBox<>(new String[]{"root.properties", "client1.properties", "client2.properties"});
        JTextField userText = new JTextField();
        JPasswordField passwordText = new JPasswordField();

        // Add dropdowns and input fields to the top panel
        topPanel.add(new JLabel("DB URL Properties:"));
        topPanel.add(dbUrlProperties);
        topPanel.add(new JLabel("User Properties:"));
        topPanel.add(userProperties);
        topPanel.add(new JLabel("Username:"));
        topPanel.add(userText);
        topPanel.add(new JLabel("Password:"));
        topPanel.add(passwordText);

        // Center panel for SQL text and result text
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JTextArea sqlText = new JTextArea("Enter your SQL command here", 5, 40);
        JTextArea resultText = new JTextArea("Results will appear here", 10, 40);
        resultText.setEditable(false);
        centerPanel.add(new JScrollPane(sqlText));
        centerPanel.add(new JScrollPane(resultText));

        // Bottom panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 6, 10, 10));  // 1 row, 6 columns for buttons
        connectButton = new JButton("Connect to Database");
        disconnectButton = new JButton("Disconnect From Database");
        executeButton = new JButton("Execute SQL Command");
        clearCommandButton = new JButton("Clear SQL Command");
        clearResultsButton = new JButton("Clear Result Window");
        closeButton = new JButton("Close Application");

        // Add buttons to the panel
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        buttonPanel.add(executeButton);
        buttonPanel.add(clearCommandButton);
        buttonPanel.add(clearResultsButton);
        buttonPanel.add(closeButton);

        // Add the panels to the main frame
        add(topPanel, BorderLayout.NORTH);    // Add the top panel with dropdowns and text fields
        add(centerPanel, BorderLayout.CENTER);  // Add the center panel with SQL and result text areas
        add(buttonPanel, BorderLayout.SOUTH);   // Add the button panel at the bottom

        // Set the window size
        setSize(800, 600);

        // Ensure the application closes when clicking the close button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make sure the window is visible
        setVisible(true);
    }

    public static void main(String[] args) {
        // Run the application
        new project3();
    }
}
