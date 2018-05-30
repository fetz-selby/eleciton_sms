package com.election.sms;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.smslib.Service;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

public class SendMessageBeta implements Runnable{
	private Service service;
	private OutboundMessage outBoundMessage;
	private Connection con;

	
	public SendMessageBeta(Connection con, AGateway tmpgateway, Service tmpservice){
		this.con = con;
		
		try{
			service = tmpservice;			
			service.setOutboundMessageNotification(new IOutboundMessageNotification() {
				
				@Override
				public void process(AGateway arg0, OutboundMessage arg1) {
					//System.out.println("message sent is"+arg1.getText());
				}
			});			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void doInit(){
		sendMessages();
	}
	
	private void sendMessages(){
		try{
			Statement stmt = (Statement) con.createStatement();
			ResultSet result = stmt.executeQuery("select id, recipient_msisdn, msg from outbox");
			
			if(result != null){
				while(result.next()){
					String recipient = result.getString("recipient_msisdn");
					String msg = result.getString("msg");
					String id = ""+result.getInt("id");
					
					outBoundMessage = new OutboundMessage(recipient, msg);
					outBoundMessage.setStatusReport(true);
					service.sendMessage(outBoundMessage);
					
					System.out.println("Message to be sent "+msg+" and recipient num is "+recipient);
					
					storeToSent(id, msg, recipient);
				}
			}
		}catch(Exception sql){
			sql.printStackTrace();
		}
	}
	
	private void storeToSent(String id, String msg, String recipient_msisdn){
		PreparedStatement prstmt = null;
		try{
			con.setAutoCommit(false);
			prstmt = (PreparedStatement) con.prepareStatement("insert into sent (msg, recipient_msisdn) values (?, ?)");
			prstmt.setString(1, msg);
			prstmt.setString(2, recipient_msisdn);
			
			if(prstmt.executeUpdate() > 0){
				System.out.println("Saved to sent successfully");
				removeFromOutbox(id);
			}
			
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}
	
	//Do this after message is sent
	private void removeFromOutbox(String id){
		PreparedStatement prstmt = null;
		try{
			con.setAutoCommit(false);
			prstmt = (PreparedStatement) con.prepareStatement("delete from outbox where id = ?");
			prstmt.setInt(1, Integer.parseInt(id));
			if(prstmt.executeUpdate() > 0){
				System.out.println("Message removed successfully");
			}
		}catch(SQLException sql){
			sql.printStackTrace();
		}
	}

	@Override
	public void run() {
		final int SEC = 1000;
		
		try{
			while(true){
				doInit();
				System.gc();
				Thread.sleep(SEC);
			}
		}catch(InterruptedException ie){
			ie.printStackTrace();
		}
	}
}
