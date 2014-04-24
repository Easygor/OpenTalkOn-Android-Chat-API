package com.thinkspace.pushservice.appclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.thinkspace.clientpackets.protobuf.ClientPackets.ClientPacket;
import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.clientpackets.protobuf.ClientPackets.RoomInfo;
import com.thinkspace.clientpackets.protobuf.ClientPackets.RoomUser;
import com.thinkspace.clientpackets.protobuf.ClientPackets.SendMessage;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.OTTalkMsgV2;
import com.thinkspace.opentalkon.data.TARoomInfo;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.PLCacheCtrl.OnRoomUserAsyncComplete;
import com.thinkspace.opentalkon.lib.ex.InvitedUser;
import com.thinkspace.opentalkon.lib.ex.Message;
import com.thinkspace.opentalkon.lib.ex.Message.MessageType;
import com.thinkspace.opentalkon.lib.ex.MessageHandler;
import com.thinkspace.opentalkon.lib.ex.MessageUnReadCount;
import com.thinkspace.opentalkon.push.pushClientHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.pushpackets.protobuf.Packets.PushService;
import com.thinkspace.pushpackets.protobuf.Packets.PushServiceClient;
import com.thinkspace.pushpackets.protobuf.Packets.SendData;
import com.thinkspace.pushpackets.protobuf.Packets.SendData_Resp;
import com.thinkspace.pushpackets.protobuf.Packets.SendData_Resp.Response;
import com.thinkspace.pushservice.satelite.PLMsgHandler;
import com.thinkspace.pushservice.satelite.PLNotifyHandler;

public abstract class PushClientBase implements pushClientHandler{
	protected ArrayList<PLMsgHandler> msgHandler = new ArrayList<PLMsgHandler>();
	protected ArrayList<PLNotifyHandler> notifyHandler = new ArrayList<PLNotifyHandler>();
	protected Map<Integer, PushPacketHandler> transactHandler;
	protected PushPacketHandler defaultHandler;
	
	protected Handler handler;
	protected Context ctx;
	protected boolean started;
	Map<Long, Boolean> receivedMsgMap = new HashMap<Long, Boolean>();
	
	MessageHandler clientInterfaceMsgHandler;
	
	public MessageHandler getClientInterfaceMsgHandler() {
		return clientInterfaceMsgHandler;
	}

	public void setClientInterfaceMsgHandler(MessageHandler clientInterfaceMsgHandler) {
		this.clientInterfaceMsgHandler = clientInterfaceMsgHandler;
	}

	public ArrayList<PLMsgHandler> getMsgHandler() {
		return msgHandler;
	}

	public ArrayList<PLNotifyHandler> getNotifyHandler(){
		return notifyHandler;
	}
	
	protected PushClientBase(Handler handler, Context context){
		this.handler = handler;
		this.ctx = context;
		this.started = false;
	}
	
	public void registerMsgHandler(PLMsgHandler handler) {
		if(this.msgHandler.contains(handler) == false){
			this.msgHandler.add(handler);
		}
	}

	public void unRegisterMsgHandler(PLMsgHandler handler){
		if(this.msgHandler.contains(handler)){
			this.msgHandler.remove(handler);
		}
	}

	public void registerNotifyHandler(PLNotifyHandler notifyHandler){
		if(this.notifyHandler.contains(notifyHandler) == false){
			this.notifyHandler.add(notifyHandler);
		}
	}

	public void unRegisterNotifyHandler(PLNotifyHandler notifyHandler){
		if(this.notifyHandler.contains(notifyHandler)){
			this.notifyHandler.remove(notifyHandler);
		}
	}
	
	@SuppressWarnings("unchecked")
	void processToClientHandler(OTTalkMsgV2 talkMsg){
			final Message message = new Message();
			if(talkMsg.isImgMsg()){
				message.messageType = MessageType.IMAGE_MESSAGE;
				message.imageUrl = new ArrayList<String>();
				try{
					for(int i=0;i<talkMsg.getImg_url().length();++i){
						String url = talkMsg.getImg_url().getString(i);
						message.imageUrl.add(url);
					}
				}catch(Exception ex){}
				message.textMessage = talkMsg.getMsg();
			}else if(talkMsg.isExitMsg()){
				message.messageType = MessageType.EXIT_MESSAGE;
			}else if(talkMsg.inviteMsg){
				message.messageType = MessageType.INVITE_MESSAGE;
				message.inviteUsers = new ArrayList<InvitedUser>();
				try{
					JSONObject json = talkMsg.getInviteUsers();
					Iterator<String> iter = json.keys();
					
					while(iter.hasNext()){
						String userId = iter.next();
						InvitedUser invitedUser = new InvitedUser();
						invitedUser.userId = Long.valueOf(userId);
						invitedUser.userNickName = json.getString(userId);
						message.inviteUsers.add(invitedUser);
					}
				}catch(Exception ex){}
			}else{
				message.messageType = MessageType.TEXT_MESSAGE;
				message.textMessage = talkMsg.getMsg();
			}
			message.messageId = talkMsg.getId();
			message.senderId = talkMsg.getSender_id();
			message.roomId = talkMsg.getRoom_id();
			message.time = talkMsg.getTime();
			
			handler.post(new Runnable() {
				@Override public void run() {
					clientInterfaceMsgHandler.onMessageReceived(message);
				}
			});
	}
	
	public void onReceived(PushService packet){
		if(packet.getClient().hasClientLoginResp()){
			//skip
		}else if(packet.getClient().hasSendData()){
			SendData sendData = packet.getClient().getSendData();
			PushService resp = PushService.newBuilder().setId(packet.getId())
					.setClient(PushServiceClient.newBuilder().setSendDataResp(SendData_Resp.newBuilder().setResp(Response.OK))).build();
			boolean res = sendPacket(resp);
			if(res == false){ }
			
			ClientPacket cPacket = null;
			try {
				cPacket = ClientPacket.parseFrom(sendData.getData());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
				return;
			}
			
			if(cPacket.hasRoomInfo()){
				onRoomInfo(cPacket.getRoomInfo());
			}
			
			if(cPacket.hasSendMessage()){
				long msgId = cPacket.getSendMessage().getClientMsgId();
				if(receivedMsgMap.containsKey(msgId)) return;
				receivedMsgMap.put(msgId, true);
				
				if(clientInterfaceMsgHandler == null){
					onSendMessageReceived(cPacket.getSendMessage());
				}else{
					OTTalkMsgV2 talkMsg = parseTalkMsg(cPacket.getSendMessage().getClientMsgId(),
						cPacket.getSendMessage().getSenderId(),
						cPacket.getSendMessage().getMsg(),
						cPacket.getSendMessage().getTime(),
						cPacket.getSendMessage().getRoomId(),
						cPacket.getSendMessage().getSenderNickName());
					processToClientHandler(talkMsg);
				}
			}
			
			if(cPacket.hasNotify()){
				if(clientInterfaceMsgHandler == null){
					onNotify(cPacket.getNotify());
				}else{
					if(cPacket.getNotify().hasMsgRead()){
						final ArrayList<MessageUnReadCount> ret = new ArrayList<MessageUnReadCount>();
						int count = cPacket.getNotify().getMsgRead().getMsgIdCount();
						for(int i=0;i<count;++i){
							MessageUnReadCount unReadCount = new MessageUnReadCount();
							unReadCount.msgId = cPacket.getNotify().getMsgRead().getMsgId(i);
							unReadCount.unReadCount = cPacket.getNotify().getMsgRead().getUnReadCount(i);
							ret.add(unReadCount);
						}
						handler.post(new Runnable() {
							@Override public void run() {
								clientInterfaceMsgHandler.onMessageUnReadCount(ret);
							}
						});
					}
				}
			}
		}
	}
	
	@Override
	public void onReceived(byte[] data) {
		PushService packet = null;
		try{
			packet = PushService.parseFrom(data);
		}catch(InvalidProtocolBufferException ex){
			ex.printStackTrace();
		}
		
		onReceived(packet);
	}
	

	public abstract boolean sendPacket(PushService packet);
	public abstract void stopTokenClient();
	public abstract void restartTokenClient();
	
	public void onRoomInfo(RoomInfo roomInfo){
		if(OTOApp.getInstance().getDB().beginTransaction()){
			ArrayList<Long> users = new ArrayList<Long>();
			for(int i=0; i<roomInfo.getUserCount();++i){
				RoomUser user = roomInfo.getUser(i);
				users.add(user.getUserId());
				TAUserNick.getInstance().insertWithBeginTransaction(user.getUserId(), user.getUserNickname(), null);
			}
			OTOApp.getInstance().getDB().endTransaction();
			OTOApp.getInstance().getCacheCtrl().addRoomUsers(roomInfo.getRoomId(), users);
		}
	}

	public void onNotify(final Notify packet){
		if(packet.hasMsgRead()){
			handler.post(new Runnable() {
				@Override public void run() {
					int size = packet.getMsgRead().getMsgIdList().size();
					if(OTOApp.getInstance().getDB().beginTransaction()){
						for(int i=0;i<size;++i){
							long msg_id = packet.getMsgRead().getMsgIdList().get(i);
							long count = packet.getMsgRead().getUnReadCountList().get(i);
							
							OTTalkMsgV2 msg = OTOApp.getInstance().getCacheCtrl().getMsgMap().get(msg_id);
							if(msg != null){
								msg.setUnread_cnt(Math.min((long)count, msg.getUnread_cnt()));
								OTOApp.getInstance().getCacheCtrl().updateWIthBeginTransact(msg);
							}
						}
						OTOApp.getInstance().getDB().endTransaction();
					}
				}
			});
		}
		
		handler.post(new Runnable() {
			@Override public void run() {
			for(final PLNotifyHandler nHandler : getNotifyHandler()){
					nHandler.onNotify(packet);
				}
			}
		});
	}
	
	public void parseTalkMsg(JSONObject jsonData) throws JSONException{
		long id = jsonData.getLong("id");
		long sender_id = jsonData.getLong("sender_id");
		String msgData = jsonData.getString("msg");
		long send_time = jsonData.getLong("send_time");
		long room_id = jsonData.getLong("room_id");
		String sender_nickname = jsonData.getString("sender_nickname");
		
		if(receivedMsgMap.containsKey(id)) return;
		receivedMsgMap.put(id, true);
		
		if(clientInterfaceMsgHandler == null){
			if(OTOApp.getInstance().getCacheCtrl().hasIgnore(sender_id)) return;
			if(OTOApp.getInstance().getCacheCtrl().getMsgMap().containsKey(id)) return;
			makeNewMsgAndProcessing(id,sender_id, msgData, send_time, room_id, sender_nickname, true, false);
		}else{
			OTTalkMsgV2 talkMsg = parseTalkMsg(id, sender_id, msgData, send_time, room_id, sender_nickname);
			processToClientHandler(talkMsg);
			String token = OTOApp.getInstance().getToken();
			new TASatelite(null, false).doSetReceivedMsg(token, id);
		}
	}
	
	OTTalkMsgV2 parseTalkMsg(long id, long sender_id, String msgData, long send_time, final long room_id, String sender_nickname){
		JSONArray img_url = null;
		String img_msg = null;
		OTTalkMsgV2 msg = new OTTalkMsgV2();
		boolean parsed = false;
		try{
			JSONObject obj = new JSONObject(msgData);
			String name = obj.getString("name");
			if(name.equals("image_message")){
				img_url = obj.getJSONArray("img_url");
				img_msg = obj.getString("msg");
				msg.setImgMsg(true);
			}else if(name.equals("exit_room_notify")){
				msg.setExitMsg(true);
			}else if(name.equals("join_room_notify")){
				msg.setInviteMsg(true);
				msg.setInviteUsers(obj.getJSONObject("user_list"));
			}else if(name.equals("enter_room_notify")){
				msg.setEnterMsg(true);
			}
			parsed = true;
		}catch(JSONException ex){
			msg.setImgMsg(false);
			msg.setExitMsg(false);
			msg.setInviteMsg(false);
			msg.setEnterMsg(false);
		}
		if(parsed){
			msg.setMsg("");
			if(msg.isImgMsg()){
				msg.setImg_url(img_url);
				msg.setMsg(img_msg);
			}
		}else{
			msg.setMsg(msgData);
		}
		
		msg.setId(id);
		msg.setSender_id(sender_id);
		msg.setRoom_id(room_id);
		msg.setTime(send_time);
		msg.setRead_flag(false);
		msg.setSendMsg(false);
		return msg;
	}
	
	public void onSendMessageReceived(final SendMessage packet){
		if(OTOApp.getInstance().getCacheCtrl().hasIgnore(packet.getSenderId())) return;
		if(OTOApp.getInstance().getCacheCtrl().getMsgMap().containsKey(packet.getClientMsgId())) return;
		OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
		
		makeNewMsgAndProcessing(packet.getClientMsgId(), packet.getSenderId(), packet.getMsg(),
				packet.getTime(), packet.getRoomId(), packet.getSenderNickName(), false,
				clientInterfaceMsgHandler != null);
	}
	
	@SuppressWarnings("unchecked")
	public void makeNewMsgAndProcessing(long id, long sender_id, String msgData, long send_time,
			final long room_id, String sender_nickname, final boolean doSetReceived, final boolean force_not_notify){
		final OTTalkMsgV2 msg = parseTalkMsg(id, sender_id, msgData, send_time, room_id, sender_nickname);
		
		if(OTOApp.getInstance().getDB().beginTransaction()){
			TAUserNick.getInstance().insertWithBeginTransaction(sender_id, sender_nickname, null);
			OTOApp.getInstance().getDB().endTransaction();
		}
		
		TARoomInfo roomInfo = OTOApp.getInstance().getCacheCtrl().getRoomMap().get(room_id);
		
		boolean refreshRoomInfo = false;
		final AtomicBoolean makeNotification = new AtomicBoolean(false);
		if(msg.isInviteMsg()){
			if(msg.getSender_id() != OTOApp.getInstance().getId()){
				makeNotification.set(true);
			}
			refreshRoomInfo = true;
			try{
				JSONObject json = msg.getInviteUsers();
				Iterator<String> iter = json.keys();
				
				OTOApp.getInstance().getDB().beginTransaction();
				while(iter.hasNext()){
					String key = iter.next();
					TAUserNick.getInstance().insertWithBeginTransaction(Long.valueOf(key), json.getString(key), null);
				}
				OTOApp.getInstance().getDB().endTransaction();
			}catch(Exception ex){}
		}else if(msg.isExitMsg()){
			refreshRoomInfo = true;
			msg.setRead_flag(true);
			OTOApp.getInstance().getCacheCtrl().exitRoomUser(msg.getRoom_id(), msg.getSender_id());
		}else if(msg.isEnterMsg()){
			refreshRoomInfo = true;
			msg.setRead_flag(true);
		}else{
			makeNotification.set(true);
		}
		
		if(doSetReceived){
			String token = OTOApp.getInstance().getToken();
			new TASatelite(null, false).doSetReceivedMsg(token, msg.getId());
		}
		
		msg.setUnread_cnt(-1L);
		if(roomInfo == null || roomInfo.getUsers().size() == 0 || refreshRoomInfo){
			OTOApp.getInstance().getCacheCtrl().getRoomUsersAsync(room_id, new OnRoomUserAsyncComplete() {
				@Override public void onComplete(ArrayList<Long> users) {
					TARoomInfo roomInfo = OTOApp.getInstance().getCacheCtrl().getRoomMap().get(room_id);
					if(roomInfo.getUsers().contains(OTOApp.getInstance().getId()) == false) return;
					doMsgProcessing(msg, roomInfo.getUsers().size(), makeNotification.get());
				}
				@Override public void onError() {
					doMsgProcessing(msg, 2, false);
				}
			}, false, true);
		}else{
			doMsgProcessing(msg, roomInfo.getUsers().size(), makeNotification.get());
		}
	}
	
	public void doMsgProcessing(final OTTalkMsgV2 msg, final int user_count, final boolean notify){
		msg.setUnread_cnt((long)user_count - 2);
		boolean res = OTOApp.getInstance().getCacheCtrl().addMsg(msg);
		
		if(notify && res){
			OTOApp.getInstance().getConvMgr().newMsgNotification(msg.getRoom_id(), msg.getSender_id(), msg.getMsg());
		}
		if(res){
			handler.post(new Runnable() {
				@Override public void run() {
					for(final PLMsgHandler callback : getMsgHandler()){
						if(callback != null){
							callback.onMsgReceived(msg);
						}
					}
				}
			});
		}
	}
}
