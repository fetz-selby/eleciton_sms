package com.election.sms;

import java.io.IOException;

import org.smslib.AGateway;
import org.smslib.AGateway.Protocols;
import org.smslib.GatewayException;
import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.modem.SerialModemGateway;

import com.election.sms.listeners.InBoundException;
import com.election.sms.listeners.IncomingMessageHandler;
import com.election.sms.listeners.IncomingNewMessage;
import com.election.sms.verifiers.InboundMessageVerifier;

public class ReadMessageBeta implements Runnable, IncomingNewMessage{
	private Inbox inbox;
	private Service service;
	private SerialModemGateway gateway;
	private IncomingMessageHandler incomingListener;
	private InBoundException inboundException;
	private InboundMessageVerifier readVerifier;
	
	public ReadMessageBeta(Service service, SerialModemGateway gateway, InBoundException onError){
		this.service = service;
		this.gateway = gateway;
		readVerifier = new InboundMessageVerifier(this);
		inbox = new Inbox();
		
		inboundException = onError;	
		
		try{
			this.gateway.setProtocol(Protocols.PDU);			
			this.service.setInboundMessageNotification(inbox);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void doInit(){
		try{
			System.in.read();
		}catch(Exception e){
			e.printStackTrace();
			inboundException.onInBoundException(gateway);
		}
	}
	
	public void addIncomingMessageHandler(IncomingMessageHandler listener){
		incomingListener = listener;
	}
	
	private class Inbox implements IInboundMessageNotification{
		@Override
		public void process(AGateway arg0, MessageTypes arg1, InboundMessage arg2) {
			if(arg1 == MessageTypes.INBOUND){
				if(incomingListener != null){
					incomingListener.onIncomingMessage(arg2.getGatewayId(), arg2.getOriginator(), arg2.getSmscNumber(), arg2.getText());
					readVerifier.storeNewMessage(arg2);
						try {
							//Message being deleted from the SIM
							gateway.deleteMessage(arg2);
						} catch (TimeoutException e) {
							e.printStackTrace();
						} catch (GatewayException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}					
				}
			}
		}
		
	}

	@Override
	public void run() {
		while(true){
			doInit();
			/*try{
				Thread.sleep(1000);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}*/
		}
	}
	
	@Override
	public void onNewMessageReceived(InboundMessage newMsg) {
		incomingListener.onIncomingMessage(newMsg.getGatewayId(), newMsg.getOriginator(), newMsg.getSmscNumber(), newMsg.getText());
	}
}
