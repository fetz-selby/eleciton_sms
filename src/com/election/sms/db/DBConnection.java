package com.election.sms.db;

import java.sql.DriverManager;

import com.mysql.jdbc.Connection;

public class DBConnection {
	private static Connection con;
	private String url = "jdbc:mysql://localhost:3306/election";

	private static DBConnection dbc = new DBConnection();
	
	private DBConnection(){}
	
	public static Connection getConnection(){
		dbc.establishConnection();
		return con;
	}
	
	private void establishConnection(){
		try{
            Class.forName("com.mysql.jdbc.Driver");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			con = (Connection)DriverManager.getConnection(url, "root", "");

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
