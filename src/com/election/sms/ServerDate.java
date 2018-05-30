package com.election.sms;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class ServerDate {
	private String ts, date, time;
	private Connection con;
	
	public ServerDate(Connection con){
		this.con = con;
		loadDateAndTime();
	}
	
	private void loadDateAndTime(){
		
		try{
			Statement stmt = (Statement)con.createStatement();
			ResultSet result = stmt.executeQuery("select curdate() as date, curtime() as time");
			if(result != null){
				while(result.next()){
					date = result.getString("date");
					time = result.getString("time");
					ts = date+" "+time;
				}
			}
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}

	public String getTs() {
		return ts;
	}

	public String getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}
}
