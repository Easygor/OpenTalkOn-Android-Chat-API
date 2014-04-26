package com.thinkspace.opentalkon.push;

public interface pushClientHandler {
	public void onConnected();
	public void onStopped();
	public void onReceived(byte[] data);
}
