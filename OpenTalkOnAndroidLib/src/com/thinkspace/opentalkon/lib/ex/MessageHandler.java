package com.thinkspace.opentalkon.lib.ex;

import java.util.List;

public interface MessageHandler{

	public void onMessageReceived(Message message);

	public void onMessageUnReadCount(List<MessageUnReadCount> messageUnReadCount);
}
