package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
	Connection con;
    private boolean conFree = true;
    
    // Database configuration
    public static String url = "jdbc:mysql://localhost:3306/money_manager";
    public static String dbdriver = "com.mysql.jdbc.Driver";
    public static String username = "root";
    public static String password = "Root@2021";
    
    public DatabaseConnector() throws Exception {
        try {
            /*
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/BookDB");
            con = ds.getConnection();
            */
            Class.forName(dbdriver);
            con = DriverManager.getConnection(url, username, password);
            //For the testing purpose can enable this
//            con = DriverManager.getConnection("jdbc:mysql://sql6.freemysqlhosting.net:3306/sql6444684", "sql6444684", "T9jqBmjlG1");
            System.out.println("Database connected successfully");
            
        } catch (Exception ex) {
            System.out.println("Exception in AccountDBAO: " + ex);
            throw new Exception("Couldn't open connection to database: " +
                    ex.getMessage());
        }
    }
    
    public void remove() {
        try {
            con.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
