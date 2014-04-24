package com.thinkspace.pushservice.appclient;

import com.thinkspace.pushpackets.protobuf.Packets.PushService;

public interface PushPacketHandler {
	public void onDataReceived(PushService packet);
}
