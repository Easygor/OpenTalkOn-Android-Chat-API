package com.thinkspace.pushservice.appclient;

import com.thinkspace.pushservice.satelite.PLMsgHandler;
import com.thinkspace.pushservice.satelite.PLNotifyHandler;

public interface PushHandlerRegister {
	public void registerMsgHandler(PLMsgHandler handler);
	public void unRegisterMsgHandler(PLMsgHandler handler);
	public void registerNotifyHandler(PLNotifyHandler notifyHandler);
	public void unRegisterNotifyHandler(PLNotifyHandler notifyHandler);
}
