package com.election.sms.listeners;

import org.smslib.AGateway;

public interface OutgoingMessageHandler {
	void onMessageOut(AGateway gateway, String msg);
}
