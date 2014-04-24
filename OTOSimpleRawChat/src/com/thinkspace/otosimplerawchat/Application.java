package com.thinkspace.otosimplerawchat;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.content.Intent;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.lib.ClientInterface;
import com.thinkspace.opentalkon.lib.ex.ClientInterfaceEx;
import com.thinkspace.opentalkon.lib.ex.Message;
import com.thinkspace.opentalkon.lib.ex.MessageHandler;
import com.thinkspace.opentalkon.lib.ex.MessageUnReadCount;

public class Application extends android.app.Application {
	static Application instance;
	List<MessageHandler> msgHandler;
	long lastRoomId;
	
	public static Application getInstance(){
		return instance;
	}
	
	public void registerMsgHandler(MessageHandler handler){
		msgHandler.add(handler);
	}
	
	public void unRegisterMsgHandler(MessageHandler handler){
		msgHandler.remove(handler);
	}
	
	public long getLastRoomId() {
		return lastRoomId;
	}

	public void setLastRoomId(long lastRoomId) {
		this.lastRoomId = lastRoomId;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		msgHandler = new ArrayList<MessageHandler>();
		ClientInterface.InitLibrary(this, "Y0uw7/HwBl50+/CS8OwWIHKSQDgkUE0rDlsGmJCXBeI=");
		ClientInterfaceEx.setClientInterfaceMsgHandler(new MessageHandler() {
			
			@Override
			public void onMessageUnReadCount(List<MessageUnReadCount> messageUnReadCount) {
				for(MessageHandler handler : msgHandler){
					handler.onMessageUnReadCount(messageUnReadCount);
				}
			}
			
			@Override
			public void onMessageReceived(Message message) {
				for(MessageHandler handler : msgHandler){
					handler.onMessageReceived(message);
				}
				
				if(OTOApp.getInstance().IsForeground(ChatActivity.class.getSimpleName()) &&
					lastRoomId == message.roomId) return;
					
				Intent intent = null;
				intent = new Intent(Application.this, ChatActivity.class);
				intent.putExtra("room_id", message.roomId);
				intent.putExtra("last_msg", message.senderId + " : " + message.textMessage);
				
				OTOApp.getInstance().getConvMgr().setupNotficationForMsgAnnounce(1, getString(R.string.oto_notification_message_popup),
						message.senderId + " : " + message.textMessage,
						Notification.FLAG_AUTO_CANCEL, intent, 0x123, true, true);
			}
		});
	}
}
