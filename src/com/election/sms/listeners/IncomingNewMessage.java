package com.election.sms.listeners;

import org.smslib.InboundMessage;

public interface IncomingNewMessage {
	void onNewMessageReceived(InboundMessage newMsg);
}
