package com.election.sms.listeners;

public interface RequestTypeListener {
	void onDataEntry(String msisdn, String msg, String type);
	void onDataRequest(String msisdn, String msg);
}