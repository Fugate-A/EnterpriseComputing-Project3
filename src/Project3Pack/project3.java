/*
Name: Andrew Fugate
Course: CNT 4714 Fall 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: November 3, 2024
Class: project3.java
*/
//------------------------------------------------------------------------------------------------
package Project3Pack;
//------------------------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
//------------------------------------------------------------------------------------------------
public class project3 extends JFrame
{
    private JTextArea commandArea;
    private JTextArea resultArea;
    private JTextField usernameField;
    private JTextField passwordField;
    
    private JComboBox < String > userSelector;
    private JComboBox < String > dbSelector;
    
    private JButton connectButton;
    private JButton executeButton;
    private JButton clearCommandButton;
    private JButton clearResultButton;
    private JButton closeButton;
    private JButton disconnectButton;
    
    private Connection connection;
    
    private JScrollPane resultScrollPane;
//------------------------------------------------------------------------------------------------
    public project3()
    {
        //init
        setupGUI();
        
        loadPropertiesOptions();
    }
//------------------------------------------------------------------------------------------------
    private void setupGUI()
    {
        //layout settings
        setTitle( "SQL Client Application" );
        setSize( 600, 400 );
        setLayout( new BorderLayout() );

        //input feild
        commandArea = new JTextArea( 3, 40 );
        resultArea = new JTextArea();
        resultArea.setEditable( false );

        usernameField = new JTextField( 10 );
        passwordField = new JTextField( 10 );
        dbSelector = new JComboBox <> ();
        userSelector = new JComboBox <> ();
        
        connectButton = new JButton( "Connect" );
        executeButton = new JButton( "Execute" );
        clearCommandButton = new JButton( "Clear Command" );
        clearResultButton = new JButton( "Clear Results" );
        disconnectButton = new JButton( "Disconnect" );
        closeButton = new JButton( "Close" );

        connectButton.addActionListener( new ConnectListener() );
        executeButton.addActionListener( new ExecuteListener() );
        clearCommandButton.addActionListener( e -> commandArea.setText( "" ) );
        clearResultButton.addActionListener( e -> resultArea.setText( "" ) );
        disconnectButton.addActionListener( new DisconnectListener() );
        closeButton.addActionListener( e -> System.exit( 0 ) );
        
        add( new JScrollPane( commandArea ), BorderLayout.NORTH );
        
        resultScrollPane = new JScrollPane( resultArea );
        resultScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        
        add( resultScrollPane, BorderLayout.CENTER );

        //contrls
        JPanel controlPanel = new JPanel();
        controlPanel.add( new JLabel( "Username:" ) );
        controlPanel.add( usernameField );
        controlPanel.add( new JLabel("Password:") );
        controlPanel.add( passwordField );
        controlPanel.add( dbSelector );
        controlPanel.add( userSelector );
        controlPanel.add( connectButton );
        controlPanel.add( executeButton );
        controlPanel.add( clearCommandButton );
        controlPanel.add( clearResultButton );
        controlPanel.add( disconnectButton );
        controlPanel.add( closeButton );

        add( controlPanel, BorderLayout.SOUTH );
    }
//------------------------------------------------------------------------------------------------
    private void loadPropertiesOptions()
    {
        dbSelector.addItem( "project3" );
        dbSelector.addItem( "bikedb" );

        userSelector.addItem( "root" );
        userSelector.addItem( "client1" );
        userSelector.addItem( "client2" );
        userSelector.addItem( "project3app" );
    }
//------------------------------------------------------------------------------------------------
    private void connectToDatabase( String username, String password, String dbName )
    {
        try
        {
            Properties props = new Properties();
 
            InputStream input = getClass().getResourceAsStream( "/Project3Pack/" + dbName + ".properties" );
            
            if( input == null )
            {
                resultArea.append( "Error: Properties file not found\n" );
      
                refreshResultArea();
                
                return;
            }
            
            props.load( input );
           
            String url = props.getProperty( "db.url" );
            String driver = props.getProperty( "db.driver" );
            
            Class.forName( driver );
            
            connection = DriverManager.getConnection( url, username, password );
            
            resultArea.append( "Connected to database: " + dbName + "\n" );
            
            refreshResultArea();
        }
        
        catch( Exception e )
        {
            resultArea.append( "Error: " + e.getMessage() + "\n" );
        
            refreshResultArea();
        }
    }
//------------------------------------------------------------------------------------------------
    private void executeSQL( String sql )
    {
        try
        {
            if( connection == null )
            {
                resultArea.append( "No active connection.\n" );
   
                refreshResultArea();
                
                return;
            }
            
            Statement stmt = connection.createStatement();
       
            sql = sql.trim();

            if( sql.toLowerCase().startsWith( "select" ) || sql.toLowerCase().startsWith( "show" ) || sql.toLowerCase().startsWith( "describe" ) )
            {
                ResultSet rs = stmt.executeQuery( sql );
       
                displayResultSet(rs);
            }
            
            else
            {
                int rows = stmt.executeUpdate( sql );

                resultArea.append( "Executed successfully, affected rows: " + rows + "\n" );
                
                logOperation( "update", usernameField.getText() );
            }
            
            commandArea.setText( "" );
       
            refreshResultArea();
        }
        
        catch( SQLException e )
        {
            resultArea.append( "Error executing SQL: " + e.getMessage() + "\n" );
       
            refreshResultArea();
        }
    }
//------------------------------------------------------------------------------------------------
    private void displayResultSet( ResultSet rs ) throws SQLException
    {
        ResultSetMetaData rsmd = rs.getMetaData();
   
        int columnsNumber = rsmd.getColumnCount();
        
        while( rs.next() )
        {
            for( int i = 1; i <= columnsNumber; i++ )
            {
                if( i > 1 )
            	{
            		resultArea.append( ", " );
            	}
                
                String columnValue = rs.getString( i );
                
                resultArea.append( rsmd.getColumnName( i ) + ": " + columnValue );
            }
            
            resultArea.append( "\n" );
        }
        
        refreshResultArea();
    }
//------------------------------------------------------------------------------------------------
    private void logOperation( String operationType, String username )
    {
        try( Connection logConn = DriverManager.getConnection( "jdbc:mysql://localhost:3306/operationslog", "project3app", "project3app" ) )
        {
            String logSQL = "INSERT INTO operationscount (operation_type, username) VALUES (?, ?)";
        
            PreparedStatement logStmt = logConn.prepareStatement( logSQL );
            
            logStmt.setString( 1, operationType );
            logStmt.setString( 2, username );
            logStmt.executeUpdate();
        }
        
        catch( SQLException e )
        {
            resultArea.append( "Error logging operation: " + e.getMessage() + "\n" );
            
            refreshResultArea();
        }
    }
//------------------------------------------------------------------------------------------------
    private void refreshResultArea()
    {
        resultArea.revalidate();
        resultArea.repaint();
        
        SwingUtilities.invokeLater( () ->
        {
            JScrollBar verticalBar = resultScrollPane.getVerticalScrollBar();
            
            verticalBar.setValue(verticalBar.getMaximum());
        }
        																		);
    }
//------------------------------------------------------------------------------------------------
    private class ConnectListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            connectToDatabase( usernameField.getText(), passwordField.getText(), dbSelector.getSelectedItem().toString() );
        }
    }
//-------------------------------------------------------------------------------------------------
    private class ExecuteListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            executeSQL( commandArea.getText() );
        }
    }
//------------------------------------------------------------------------------------------------
    private class DisconnectListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                if( connection != null && !connection.isClosed() )
                {
                    connection.close();
                    
                    resultArea.append( "Disconnected from database.\n" );
                    
                    refreshResultArea();
                }
            }
            
            catch( SQLException ex )
            {
                resultArea.append( "Error disconnecting: " + ex.getMessage() + "\n" );
     
                refreshResultArea();
            }
        }
    }
//------------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new project3().setVisible(true));
    }
}
//------------------------------------------------------------------------------------------------