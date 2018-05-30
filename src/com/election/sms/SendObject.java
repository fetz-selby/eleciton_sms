package com.election.sms;

import java.sql.SQLException;

import com.election.sms.db.DBConnection;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class SendObject {
	private Connection con = DBConnection.getConnection();
	private String msg, msisdn;
	
	public void sendMessage(){
		doSaving();
	}
	
	public void setMsisdn(String msisdn){
		this.msisdn = msisdn;
	}
	
	public void setMessage(String msg){
		this.msg = msg;
	}
	
	private void doSaving(){
		PreparedStatement prstmt = null;
		try{
			con.setAutoCommit(false);
			prstmt = (PreparedStatement) con.prepareStatement("insert into outbox (recipient_msisdn, msg) values (?, ?)");
			prstmt.setString(1, "+"+msisdn);
			prstmt.setString(2, msg);
			int status = prstmt.executeUpdate();
			if(status > 0){
				System.out.println("Msg saved to outbox successfully");
			}
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}
}
