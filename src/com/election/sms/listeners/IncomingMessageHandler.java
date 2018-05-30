package com.election.sms.listeners;


public interface IncomingMessageHandler {
	void onIncomingMessage(String gateway, String msisdn, String smsc, String msg);
}
