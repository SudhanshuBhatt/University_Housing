package com.univhousing.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionUtils {
	
	private static Connection connection = null;
	
	static Constants constParameters = new Constants();
	
	public static Connection getConnection(){
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
			connection = DriverManager.getConnection(constParameters.JDBCURL,constParameters.USERNAME,constParameters.PASSWORD);
			
			Statement stmt = connection.createStatement();
			
			return connection;
			
		}catch(SQLException e1){
			System.out.println("SQLException: "+ e1.getMessage());
			System.out.println("VendorError: "+ e1.getErrorCode());
		}
		catch(ClassNotFoundException e2){
			System.out.println("Class Not Found!");
			e2.printStackTrace();
		}
		catch(Exception e3)
		{
			System.out.println("General Exception Case. Printing stack trace below:\n");
			e3.printStackTrace();
		}

		return null;
	}
	
	public static void closeConnection(Connection connection) throws SQLException
	{
		connection.close();
	}

}
