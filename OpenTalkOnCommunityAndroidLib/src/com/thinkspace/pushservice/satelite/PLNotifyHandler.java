package com.thinkspace.pushservice.satelite;

import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;

public interface PLNotifyHandler {
	public void onNotify(Notify packet);
}
