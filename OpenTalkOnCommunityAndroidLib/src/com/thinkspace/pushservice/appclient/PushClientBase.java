package com.thinkspace.pushservice.appclient;

import java.util.ArrayList;
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
import com.thinkspace.clientpackets.protobuf.ClientPackets.PostCommunity;
import com.thinkspace.clientpackets.protobuf.ClientPackets.RoomInfo;
import com.thinkspace.clientpackets.protobuf.ClientPackets.RoomUser;
import com.thinkspace.clientpackets.protobuf.ClientPackets.SendMessage;
import com.thinkspace.common.util.ConversationMgr.CommunityNotificationType;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.OTComMsg;
import com.thinkspace.opentalkon.data.OTTalkMsgV2;
import com.thinkspace.opentalkon.data.TAPublicRoomInfo;
import com.thinkspace.opentalkon.data.TARoomInfo;
import com.thinkspace.opentalkon.data.TAUserInfo;
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
				onSendMessageReceived(cPacket.getSendMessage());
				if(clientInterfaceMsgHandler != null){
					OTTalkMsgV2 talkMsg = parseTalkMsg(cPacket.getSendMessage().getClientMsgId(),
						cPacket.getSendMessage().getSenderId(),
						cPacket.getSendMessage().getSenderLevel(),
						cPacket.getSendMessage().getMsg(),
						cPacket.getSendMessage().getTime(),
						cPacket.getSendMessage().getRoomId(),
						cPacket.getSendMessage().getSenderNickName(),
						cPacket.getSendMessage().getPublicRoom());
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
					}else if(talkMsg.enterMsg || talkMsg.changeBjMsg || talkMsg.kickMsg || talkMsg.roomHiddenMsg){
						
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
			}
			
			if(cPacket.hasPostCommunity()){
				onPostCommunityReceived(cPacket.getPostCommunity());
			}
			
			if(cPacket.hasNotify()){
				onNotify(cPacket.getNotify());
				if(cPacket.getNotify().hasMsgRead()){
					if(clientInterfaceMsgHandler != null){
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
			OTOApp.getInstance().getCacheCtrl().addRoomUsers(roomInfo.getRoomId(), users, roomInfo.getPublicRoom());
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
		
		if(packet.hasLikeMine()){
			OTOApp.getInstance().getConvMgr().newMineCommunityNotification(CommunityNotificationType.LIKE,
					packet.getLikeMine().getCommunityId(),
					packet.getLikeMine().getPostId(),
					packet.getLikeMine().getNickName(),
					packet.getLikeMine().getUserId(),
					-1);
		}
		if(packet.hasCommentMine()){
			OTOApp.getInstance().getConvMgr().newMineCommunityNotification(CommunityNotificationType.REPLY,
					packet.getCommentMine().getCommunityId(),
					packet.getCommentMine().getPostId(),
					packet.getCommentMine().getNickName(),
					packet.getCommentMine().getUserId(),
					packet.getCommentMine().getReplyId());
		}
		
		if(packet.hasNewPost()){
			OTOApp.getInstance().getConvMgr().newCommunityPostNotification(packet.getNewPost().getCommunityId());
		}
		
		if(packet.hasNewReply()){
			OTOApp.getInstance().getConvMgr().newCommunityReplyNotification(packet.getNewReply().getCommunityId(),
					packet.getNewReply().getPostId(),
					packet.getNewReply().getNickName(),
					packet.getNewReply().getUserId(),
					packet.getNewReply().getReplyId());
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
		int sender_level = jsonData.getInt("sender_level");
		boolean public_room = jsonData.getBoolean("public_room");
		
		if(OTOApp.getInstance().getCacheCtrl().hasIgnore(sender_id)) return;
		if(OTOApp.getInstance().getCacheCtrl().getMsgMap().containsKey(id)) return;
		makeNewMsgAndProcessing(id,sender_id, msgData, send_time, room_id, sender_nickname, sender_level, public_room, true);
	}
	
	OTTalkMsgV2 parseTalkMsg(long id, long sender_id, int sender_level, 
			String msgData, long send_time, final long room_id, String sender_nickname, boolean public_room){
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
			}else if(name.equals("room_hidden_notify")){
				msg.setRoomHiddenMsg(true);
				msg.setHiddenFlag(obj.getBoolean("status"));
			}else if(name.equals("room_kick_notify")){
				msg.setKickMsg(true);
				msg.setKickUsers(obj.getJSONObject("user_list"));
			}else if(name.equals("change_room_bj_notify")){
				msg.setChangeBjMsg(true);
				msg.setChangedBjId(obj.getLong("bj_id"));
				msg.setChangedBjNickName(obj.getString("bj_nick_name"));
			}
			parsed = true;
		}catch(JSONException ex){
			msg.setImgMsg(false);
			msg.setExitMsg(false);
			msg.setInviteMsg(false);
			msg.setEnterMsg(false);
			msg.setRoomHiddenMsg(false);
			msg.setKickMsg(false);
			msg.setChangeBjMsg(false);
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
		msg.setPublicRoom(public_room);
		msg.setSender_level(sender_level);
		return msg;
	}
	
	public OTComMsg parseComMsg(JSONObject data) throws JSONException{
		OTComMsg msg = new OTComMsg();
		msg.setId(data.getLong("id"));
		msg.setSender_id(data.getLong("sender_id"));
		msg.setMsg(data.getString("msg"));
		msg.setTime(data.getLong("send_time"));
		msg.setReply_count(data.getInt("reply_count"));
		msg.setLike_count(data.getInt("like_count"));
		
		try{
			JSONObject obj = new JSONObject(msg.getMsg());
			String name = obj.getString("name");
			if(name.equals("image_message")){
				msg.setImg_url(obj.getJSONArray("img_url"));
				msg.setMsg(obj.getString("msg"));
				msg.setImgMsg(true);
			}
		}catch(JSONException ex){
			msg.setImgMsg(false);
		}
		
		if(msg.getSender_id() == OTOApp.getInstance().getId()){
			msg.setSendMsg(true);
		}
		
		return msg;
	}
	
	public void onPostCommunityReceived(PostCommunity packet){
		try {
			JSONObject data = new JSONObject();
			data.put("id", packet.getId());
			data.put("sender_id", packet.getSenderId());
			data.put("msg", packet.getMsg());
			data.put("send_time", packet.getSendTime());
			data.put("reply_count", packet.getReplyCount());
			data.put("like_count", packet.getLikeCount());
			
			final OTComMsg msg = parseComMsg(data);
			msg.setCommunity_id(packet.getMsgOwnerId());
			
			handler.post(new Runnable() {
				@Override public void run() {
					for(final PLMsgHandler callback : getMsgHandler()){
						if(callback != null){
							callback.onMsgReceived(msg);
						}
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void onSendMessageReceived(final SendMessage packet){
		if(OTOApp.getInstance().getCacheCtrl().hasIgnore(packet.getSenderId())) return;
		if(OTOApp.getInstance().getCacheCtrl().getMsgMap().containsKey(packet.getClientMsgId())) return;
		OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
		
		makeNewMsgAndProcessing(packet.getClientMsgId(), packet.getSenderId(), packet.getMsg(),
				packet.getTime(), packet.getRoomId(), packet.getSenderNickName(),packet.getSenderLevel(), packet.getPublicRoom(), false);
	}
	
	@SuppressWarnings("unchecked")
	public void makeNewMsgAndProcessing(long id, long sender_id, String msgData, long send_time,
			final long room_id, String sender_nickname, int sender_level, boolean public_room, final boolean doSetRead){
		final OTTalkMsgV2 msg = parseTalkMsg(id, sender_id, sender_level, msgData, send_time, room_id, sender_nickname, public_room);
		
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
		}else if(msg.isKickMsg()){
			refreshRoomInfo = true;
			msg.setRead_flag(true);
			try{
				JSONObject json = msg.getKickUsers();
				Iterator<String> iter = json.keys();
				OTOApp.getInstance().getDB().beginTransaction();
				while(iter.hasNext()){
					String key = iter.next();
					TAUserNick.getInstance().insertWithBeginTransaction(Long.valueOf(key), json.getString(key), null);
				}
				OTOApp.getInstance().getDB().endTransaction();
			}catch(Exception ex){}
		}else if(msg.isChangeBjMsg()){
			msg.setRead_flag(true);
			if(roomInfo != null && roomInfo instanceof TAPublicRoomInfo){
				TAPublicRoomInfo pRoom = (TAPublicRoomInfo) roomInfo;
				pRoom.setOwner(msg.getChangedBjId());
				TAUserInfo userInfo = new TAUserInfo();
				userInfo.setNickName(msg.getChangedBjNickName());
				pRoom.setOwnerInfo(userInfo);
			}
		}else if(msg.isRoomHiddenMsg()){
			msg.setRead_flag(true);
			if(roomInfo != null && roomInfo instanceof TAPublicRoomInfo){
				TAPublicRoomInfo pRoom = (TAPublicRoomInfo) roomInfo;
				pRoom.setHidden(msg.isHiddenFlag());
			}
		}else{
			makeNotification.set(true);
		}
		
		if(doSetRead){
			String token = OTOApp.getInstance().getToken();
			new TASatelite(null, false).doSetReceivedMsg(token, msg.getId());
		}
		
		msg.setUnread_cnt(-1L);
		if(roomInfo == null || roomInfo.getUsers().size() == 0 || refreshRoomInfo){
			OTOApp.getInstance().getCacheCtrl().getRoomUsersAsync(room_id, new OnRoomUserAsyncComplete() {
				@Override public void onComplete(ArrayList<Long> users) {
					TARoomInfo roomInfo = OTOApp.getInstance().getCacheCtrl().getRoomMap().get(room_id);
					if(msg.isKickMsg() == false && roomInfo.getUsers().contains(OTOApp.getInstance().getId()) == false) return;
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
			OTOApp.getInstance().getConvMgr().newMsgNotification(msg.getRoom_id(), msg.getSender_id(), msg.getMsg(), msg.isPublicRoom());
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
