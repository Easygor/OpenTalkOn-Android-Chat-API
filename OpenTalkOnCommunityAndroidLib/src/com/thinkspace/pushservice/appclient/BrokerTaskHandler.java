package com.thinkspace.pushservice.appclient;

public interface BrokerTaskHandler {
	public void onComplete(String ip, int port);
	public void onGetServerFail();
}
