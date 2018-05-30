package com.election.sms.verifiers;

import com.election.sms.listeners.RequestTypeListener;

public class RequestType {
	private RequestTypeListener listener;
	private String msisdn, msg;
	
	public RequestType(String msisdn, String msg){
		this.msisdn = msisdn;
		this.msg = msg;
	}
	
	public void load(){
		//Split msg
		String[] tokens = msg.split("[\\s]+");
		if(listener != null){
			if(tokens != null && tokens.length == 2){
				//Check if begins with digit
				if(Character.isDigit(tokens[0].charAt(0))){
					listener.onDataEntry(msisdn, msg, "M");
				}else{
					listener.onDataEntry(msisdn, msg, "P");
				}
			}else if(tokens != null && tokens.length == 1){
				if(tokens[0].equalsIgnoreCase("can") || tokens[0].equalsIgnoreCase("cans") || tokens[0].equalsIgnoreCase("candidates") || tokens[0].equalsIgnoreCase("candidate")){
					listener.onDataRequest(msisdn, msg);
				}
			}
		}
	}
	
	public void setRequestTypeListener(RequestTypeListener listener){
		this.listener = listener;
	}
}
