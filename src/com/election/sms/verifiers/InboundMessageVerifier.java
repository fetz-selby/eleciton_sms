package com.election.sms.verifiers;

import java.sql.SQLException;
import java.util.ArrayList;

import org.smslib.InboundMessage;

import com.election.sms.db.DBConnection;
import com.election.sms.listeners.IncomingNewMessage;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class InboundMessageVerifier {
	private static Connection con = DBConnection.getConnection();
	private ArrayList<String> inboxList;
	private IncomingNewMessage listener;
	
	public InboundMessageVerifier(IncomingNewMessage listener){
		this.listener = listener;
		//init();
	}
	
	/*private void init(){
		Statement stmt = null;
		
		try{
			stmt = (Statement) con.createStatement();
			ResultSet result = stmt.executeQuery("select date from inbox");
			if(result != null){
				inboxList = new ArrayList<String>();
				while(result.next()){
					String date = result.getString("date");
					inboxList.add(date);
				}
			}
		}catch(SQLException sqle){
			sqle.printStackTrace();
		}
	}*/
	
	/*public void doVerification(InboundMessage msg){
		if(msg != null){
			String msgDate = msg.getDate().toString();
			if(!(inboxList.contains(msgDate))){
				inboxList.add(msg.getDate().toString());
				listener.onNewMessageReceived(msg);
				storeNewMessage(msg);
			}
		}
	}*/
	
	public void storeNewMessage(InboundMessage newMsg){
		PreparedStatement prstmt = null;
		try{
			con.setAutoCommit(false);
			prstmt = (PreparedStatement) con.prepareStatement("insert into inbox (gateway, encoding, date, smsc, orig_msisdn, msg) values(?, ?, ?, ?, ?, ?)");
			prstmt.setString(1, newMsg.getGatewayId());
			prstmt.setString(2, newMsg.getEncoding().name());
			prstmt.setString(3, newMsg.getDate().toString());
			prstmt.setString(4, newMsg.getSmscNumber());
			prstmt.setString(5, newMsg.getOriginator());
			prstmt.setString(6, newMsg.getText());
			
			if(prstmt.executeUpdate() > 0){
				System.out.println("*** New Message stored successfully !!! ***");
			}
			
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}
}
