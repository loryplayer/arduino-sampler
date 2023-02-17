package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class create_database {

   static final String DB_URL = "jdbc:mysql:localhost:3306/";
   static final String USER = "root";
   static final String PASS = "root";

   public static void main(String[] args) throws ClassNotFoundException {

      // Open a connection
      try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306?user=root&password=root");
         Statement stmt = conn.createStatement();
      ) {		      
         String sql = "CREATE DATABASE STUDENTS";
         stmt.executeUpdate(sql);
         System.out.println("Database created successfully...");   	  
      } catch (SQLException e) {
         e.printStackTrace();
      } 
   }
}
