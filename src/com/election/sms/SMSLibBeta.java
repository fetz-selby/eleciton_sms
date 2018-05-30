package com.election.sms;

import org.smslib.AGateway;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

import com.election.sms.db.CandidateRequest;
import com.election.sms.db.DBConnection;
import com.election.sms.db.DBEntry;
import com.election.sms.listeners.InBoundException;
import com.election.sms.listeners.IncomingMessageHandler;
import com.election.sms.listeners.OutBoundException;
import com.election.sms.listeners.OutgoingMessageHandler;
import com.election.sms.listeners.RequestTypeListener;
import com.election.sms.verifiers.RequestType;
import com.mysql.jdbc.Connection;

public class SMSLibBeta implements InBoundException, OutBoundException, IncomingMessageHandler, OutgoingMessageHandler {
	private Connection con = DBConnection.getConnection();
	private String modemName, port, model;
	private int baud;
	
	public SMSLibBeta(String modemName, String port, String model, int baud){
		this.modemName = modemName;
		this.port = port;
		this.model = model;
		this.baud = baud;
	}
	
	public void load(){
		doAction();
	}
	
	private void doAction(){
		//Initializing modem
		SerialModemGateway gateway = new SerialModemGateway(modemName, port, baud, "Huawei", model);

		//gateway.setSmscNumber("+233244500000");
		gateway.setInbound(true);
		gateway.setOutbound(true);
		
		//Starting the service
		Service service = Service.getInstance();

		try{
			service.addGateway(gateway);
			service.startService();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		ReadMessageBeta read = new ReadMessageBeta(service, gateway, this);
		read.addIncomingMessageHandler(this);
		
		SendMessageBeta send = new SendMessageBeta(con, gateway, service);
		
		//Initializing the send and receive threads
		Thread incoming = new Thread(read);
		Thread outgoing = new Thread(send);
		
		//Starting the threads
		incoming.start();
		outgoing.start();
	
	}
	

	@Override
	public void onMessageOut(AGateway gateway, String msg) {
		//System.out.println("Message sent is "+msg);
	}

	@Override
	public void onIncomingMessage(String gateway, final String originator, final String smsc, final String msg) {
		//Note originator number comes without the "+"
		//System.out.println("New msg is ["+msg+"] and come from gateway ["+gateway+"] and from ["+originator+"] and from smsc ["+smsc+"]");
		RequestType request = new RequestType(originator, msg);
		request.setRequestTypeListener(new RequestTypeListener() {
			
			@Override
			public void onDataRequest(String msisdn, String msg) {
				CandidateRequest request = new CandidateRequest(con, msisdn);
				request.fetchCandidates();
			}
			
			@Override
			public void onDataEntry(String msisdn, String msg, String type) {
				DBEntry entry = new DBEntry(con, msisdn, msg, type);
				entry.load();
			}
		});
		request.load();
	}

	@Override
	public void onOutBoundException(AGateway gateway, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInBoundException(AGateway gateway) {
		// TODO Auto-generated method stub
		
	}
}
