package com.election.sms.listeners;

import org.smslib.AGateway;

public interface OutBoundException {
	void onOutBoundException(AGateway gateway, String msg);
}
