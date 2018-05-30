package com.election.sms.listeners;

import org.smslib.AGateway;

public interface InBoundException {
	void onInBoundException(AGateway gateway);
}
