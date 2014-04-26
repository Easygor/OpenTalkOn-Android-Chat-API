package com.thinkspace.opentalkon.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thinkspace.common.util.EventDrivenTaskMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.OTAlarmTalkOff;
import com.thinkspace.opentalkon.data.OTTalkMsgV2;
import com.thinkspace.opentalkon.data.TABigTableTable;
import com.thinkspace.opentalkon.data.TABigTableTable.convertor;
import com.thinkspace.opentalkon.data.TAIgnore;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TANotification;
import com.thinkspace.opentalkon.data.TAPublicRoomInfo;
import com.thinkspace.opentalkon.data.TARoomInfo;
import com.thinkspace.opentalkon.data.TARoomUsers;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;

public class PLCacheCtrl{
	long transactID;
	EventDrivenTaskMgr dbCommiter;
	List<TARoomInfo> roomList = Collections.synchronizedList(new ArrayList<TARoomInfo>());
	List<TAIgnore> ignoreList = Collections.synchronizedList(new ArrayList<TAIgnore>());
	List<TANotification> notificationList = Collections.synchronizedList(new ArrayList<TANotification>());
	
	Map<Integer, TANotification> notificationMap = new ConcurrentHashMap<Integer, TANotification>();
	Map<Long, TANotification> notificationReplyMap = new ConcurrentHashMap<Long, TANotification>();
	Map<Long, TARoomInfo> roomMap = new ConcurrentHashMap<Long, TARoomInfo>();
	Map<Long, OTTalkMsgV2> msgMap = new ConcurrentHashMap<Long, OTTalkMsgV2>();
	Map<Long, OTTalkMsgV2> tableIDMsgMap = new ConcurrentHashMap<Long, OTTalkMsgV2>();
	Map<Long, TAIgnore> ignoreMap = new ConcurrentHashMap<Long, TAIgnore>();
	Map<Long, TARoomUsers> roomUserMap = new ConcurrentHashMap<Long, TARoomUsers>();
	Map<Long, OTAlarmTalkOff> talkAlarmOffMap = new ConcurrentHashMap<Long, OTAlarmTalkOff>();
	
	public List<TANotification> getNotificationList() {
		return notificationList;
	}

	public List<TARoomInfo> getRoomList() {
		return roomList;
	}

	public List<TAIgnore> getIgnoreList() {
		return ignoreList;
	}

	public Map<Long, TARoomInfo> getRoomMap() {
		return roomMap;
	}

	public Map<Long, OTTalkMsgV2> getMsgMap() {
		return msgMap;
	}
	
	public Map<Long, TAIgnore> getIgnoreMap() {
		return ignoreMap;
	}

	public Map<Long, OTTalkMsgV2> getTableIDMsgMap() {
		return tableIDMsgMap;
	}
	
	public EventDrivenTaskMgr getDbCommiter() {
		if(dbCommiter == null){
			dbCommiter = new EventDrivenTaskMgr();
			dbCommiter.start();
		}
		return dbCommiter;
	}

	public long getNextTransactID(){
		if(++transactID > 987654321L){
			transactID = 0x7;
		}
		return transactID;
	}
	
	public PLCacheCtrl(){
		transactID = 0x7;
		
		loadRoomUsers();
		loadMsgs();
		loadIgnores();
		loadAlarmInfo();
		loadNotifications();
	}
	
	void loadNotifications(){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TANotification.class.getSimpleName());
		convertor<TANotification> con = new convertor<TANotification>();
		notificationList = con.getData(table.getData());
		for(TANotification noti : notificationList){
			notificationMap.put(noti.getTableIdx(), noti);
			if(noti.getId() != -1L){
				notificationReplyMap.put(noti.getId(), noti);
			}
		}
		Collections.sort(notificationList);
	}
	
	public boolean hasLikeNotification(long user_id, long post_id){
		for(TANotification noti : notificationList){
			if(noti.getUser_id() == user_id && noti.getPost_id() == post_id && noti.getType() == TANotification.TYPE_LIKE_MINE){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasNewNotification(){
		for(TANotification noti : notificationList){
			if(noti.isCheck() == false) return true;
		}
		return false;
	}
	
	public int addNewNotification(String nick, long user_id, int type, long post_id, long id){
		if(id != -1L && notificationReplyMap.containsKey(id)){
			return -1;
		}
		
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TANotification.class.getSimpleName());
		TANotification noti = new TANotification();
		noti.setNick_name(nick);
		noti.setUser_id(user_id);
		noti.setType(type);
		noti.setPost_id(post_id);
		noti.setDate(System.currentTimeMillis());
		noti.setCheck(false);
		noti.setId(id);
		
		table.pushData(noti);
		notificationList.add(noti);
		notificationMap.put(noti.getTableIdx(), noti);
		if(noti.getId() != -1L){
			notificationReplyMap.put(noti.getId(), noti);
		}
		Collections.sort(notificationList);
		
		return noti.getTableIdx();
	}
	
	public void setCheckNotification(int tableIdx, boolean check){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TANotification.class.getSimpleName());
		TANotification noti = notificationMap.get(tableIdx);
		noti.setCheck(check);
		table.updateData(noti);
		Collections.sort(notificationList);
	}
	
	public void deleteNotification(int tableIdx){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TANotification.class.getSimpleName());
		TANotification noti = notificationMap.get(tableIdx);
		table.deleteData(noti);
		notificationMap.remove(noti);
		notificationList.remove(noti);
		if(noti.getId() != -1L){
			notificationReplyMap.remove(noti);
		}
	}
	
	public void deleteAllNotification(){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TANotification.class.getSimpleName());
		table.deleteAll();
		notificationMap.clear();
		notificationList.clear();
		notificationReplyMap.clear();
	}
	
	public int getAllUnReadMsg(){
		int ret = 0;
		for(TARoomInfo room : OTOApp.getInstance().getCacheCtrl().getRoomList()){
			ret += room.getUnread_msg_cnt();
		}
		return ret;
	}
	
	public void getUnReadMsg(final Runnable completeCallback, final Runnable errorCallback, final boolean notifyOff){
		final String token = OTOApp.getInstance().getToken();
		if(token.length() == 0){
			errorCallback.run();
			return;
		}
		
		new TASatelite(new TADataHandler() {
			@Override
			public void onHttpPacketReceived(JSONObject data) {
				TASateliteDispatcher.dispatchSateliteData(data);
				if(completeCallback != null){
					completeCallback.run();
				}
			}
			
			@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {
				if(errorCallback != null){
					errorCallback.run();
				}
			}
			@Override public void onHttpException(Exception ex, JSONObject data, String addr) {
				if(errorCallback != null){
					errorCallback.run();
				}
			}

			@Override
			public void onTokenIsNotValid(JSONObject data) {
				
			}

			@Override
			public void onLimitMaxUser(JSONObject data) {
				
			}
		}, false).doGetUnReceivedMsg(token);
	}
	
	public void loadAlarmInfo(){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTAlarmTalkOff.class.getSimpleName());
		convertor<OTAlarmTalkOff> con = new convertor<OTAlarmTalkOff>();
		for(OTAlarmTalkOff talkOffElem : con.getData(table.getData())){
			talkAlarmOffMap.put(talkOffElem.getRoom_id(), talkOffElem);
		}
	}
	
	public boolean getTalkAlarm(long room_id){
		if(talkAlarmOffMap.containsKey(room_id) == false){
			return true;
		}else{
			return false;
		}
	}
	
	public void switchTalkAlarm(long room_id){
		setTalkAlarm(room_id, !getTalkAlarm(room_id));
	}
	
	public void setTalkAlarm(long room_id, boolean enable){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTAlarmTalkOff.class.getSimpleName());
		if(enable){
			if(talkAlarmOffMap.containsKey(room_id)){
				table.deleteData(talkAlarmOffMap.get(room_id));
				talkAlarmOffMap.remove(room_id);
			}
		}else{
			if(talkAlarmOffMap.containsKey(room_id) == false){
				OTAlarmTalkOff talkOff = new OTAlarmTalkOff(room_id);
				talkAlarmOffMap.put(room_id, talkOff);
				table.pushData(talkOff);
			}
		}		
	}
	
	public void loadRoomUsers(){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TARoomUsers.class.getSimpleName());
		convertor<TARoomUsers> con = new convertor<TARoomUsers>();
		for(TARoomUsers roomUser : con.getData(table.getData())){
			roomUserMap.put(roomUser.getRoom_id(), roomUser);
		}
	}
	
	public Long getPrivateRoomId(ArrayList<Long> users){
		Map<Long, Boolean> existMap = new HashMap<Long, Boolean>();
		for(TARoomInfo roomUser : roomList){
			if(roomUser instanceof TAPublicRoomInfo) continue;
			if(roomUser.getUsers().size() != users.size()) continue;
			existMap.clear();
			for(Long userId : roomUser.getUsers()){
				existMap.put(userId, true);
			}
			
			boolean matched = true;
			for(Long userId : users){
				if(existMap.containsKey(userId) == false){
					matched = false;
					break;
				}
			}
			if(matched){
				return roomUser.getRoom_id();
			}
		}
		return -1L;
		
	}
	
	public void loadIgnores(){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TAIgnore.class.getSimpleName());
		convertor<TAIgnore> con = new convertor<TAIgnore>();
		ignoreList = con.getData(table.getData());
		for(TAIgnore ignore : ignoreList){
			ignoreMap.put(ignore.getUser_id(), ignore);
		}
	}
	
	public boolean hasIgnore(Long user_id){
		return ignoreMap.containsKey(user_id);
	}
	
	public void addIgnore(TAIgnore ignore){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TAIgnore.class.getSimpleName());
		table.pushData(ignore);
		synchronized (ignoreList) {
			ignoreList.add(ignore);
		}
		ignoreMap.put(ignore.getUser_id(), ignore);
	}
	
	public void removeIgnore(Long user_id){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TAIgnore.class.getSimpleName());
		
		TAIgnore ignore = ignoreMap.get(user_id);
		if(ignore == null) return;
		
		table.deleteData(ignore);
		ignoreList.remove(ignore);
		ignoreMap.remove(ignore.getUser_id());
	}
	
	public void loadMsgs(){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		convertor<OTTalkMsgV2> con = new convertor<OTTalkMsgV2>();
		for(OTTalkMsgV2 msg : con.getData(table.getData())){
			msgMap.put(msg.getId(), msg);
			tableIDMsgMap.put((long)msg.getTableIdx(), msg);
			addMsgToRoom(msg, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeRoom(TARoomInfo room){
		if(room == null) return;
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		
		if(OTOApp.getInstance().getDB().beginTransaction()){
			ArrayList<OTTalkMsgV2> msgs = (ArrayList<OTTalkMsgV2>)room.getMsgs().clone();
			for(OTTalkMsgV2 msg : msgs){
				table.deleteDataWithBeginTransaction(msg);
				msgMap.remove(msg.getId());
			}
			OTOApp.getInstance().getDB().endTransaction();
		}
		
		roomList.remove(room);
		roomMap.remove(room.getRoom_id());
	}
	
	public void exitRoomUser(long room_id, long user_id){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TARoomUsers.class.getSimpleName());
		TARoomUsers roomUsers = roomUserMap.get(room_id);
		if(roomUsers == null) return;
		if(roomUsers.getUsers() == null) return;
		roomUsers.getUsers().remove(user_id);
		table.updateData(roomUsers);
		
		TARoomInfo roomInfo = roomMap.get(room_id);
		if(roomInfo != null && roomInfo.getUsers() != null){
			roomInfo.getUsers().remove(user_id);
		}
	}
	
	public void addRoomUsers(Long room_id, ArrayList<Long> users, boolean public_room){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(TARoomUsers.class.getSimpleName());
		if(roomUserMap.containsKey(room_id) == false){
			TARoomUsers roomUsers = new TARoomUsers();
			roomUsers.setRoom_id(room_id);
			roomUsers.setUsers(users);
			table.pushData(roomUsers);
			roomUserMap.put(room_id, roomUsers);
		}else{
			roomUserMap.get(room_id).setUsers(users);
			table.updateData(roomUserMap.get(room_id));
		}
		
		if(roomMap.containsKey(room_id) == false){
			TARoomInfo addRoom = null;
			if(public_room){
				addRoom = new TAPublicRoomInfo();
			}else{
				addRoom = new TARoomInfo();
			}
			addRoom.setRoom_id(room_id);
			roomMap.put(room_id, addRoom);
			roomList.add(addRoom);
		}
		
		TARoomInfo room = roomMap.get(room_id);
		if(roomUserMap.containsKey(room_id)){
			room.setUsers(roomUserMap.get(room_id).getUsers());
		}
	}
	
	public static interface OnRoomUserAsyncComplete{
		public void onComplete(ArrayList<Long> users);
		public void onError();
	}
	
	public void getRoomUsersAsync(long room_id, final OnRoomUserAsyncComplete handler, boolean makeHandler, final boolean makeRoom){
		new TASatelite(new TADataHandler() {
			@Override public void onHttpPacketReceived(JSONObject data) {
				try{
					String state = data.getString("state");
					if(state.equals("ok")){
						JSONObject realData = data.getJSONObject("data");
						Long room_id = realData.getLong("room_id");
						JSONArray arr = realData.getJSONArray("users");
						boolean public_room = realData.getBoolean("public_room");
						ArrayList<Long> users = new ArrayList<Long>();
						for(int i=0;i<arr.length();++i){
							users.add(arr.getLong(i));
						}
						if(makeRoom){
							addRoomUsers(room_id, users, public_room);
						}
						if(handler != null){
							handler.onComplete(users);
						}
					}else{
						if(handler != null){
							handler.onError();
						}
					}
				}catch(JSONException ex){
					if(handler != null){
						handler.onError();
					}
				}
			}
			
			@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {
				if(handler != null){
					handler.onError();
				}
			}
			@Override public void onHttpException(Exception ex, JSONObject data, String addr) {
				if(handler != null){
					handler.onError();
				}
			}
			@Override
			public void onTokenIsNotValid(JSONObject data) {
				
			}

			@Override
			public void onLimitMaxUser(JSONObject data) {
				
			}
		}, makeHandler).doGetRoomUsers(OTOApp.getInstance().getToken(), room_id);
	}
	
	public void addNewRoomInfo(TARoomInfo roomInfo){
		if(roomMap.containsKey(roomInfo.getRoom_id())){
			TARoomInfo prevRoom = roomMap.get(roomInfo.getRoom_id());
			roomInfo.setMsgs(prevRoom.getMsgs());
			roomList.remove(prevRoom);
		}
		
		roomList.add(roomInfo);
		roomMap.put(roomInfo.getRoom_id(), roomInfo);
	}
	
	 void addMsgToRoom(OTTalkMsgV2 msg, boolean addLast){
		if(roomMap.containsKey(msg.room_id) == false){
			TARoomInfo room = null;
			if(msg.publicRoom){
				room = new TAPublicRoomInfo();
			}else{
				room = new TARoomInfo();
			}
			room.setRoom_id(msg.room_id);
			
			if(roomUserMap.containsKey(msg.room_id)){
				room.setUsers(roomUserMap.get(msg.room_id).getUsers());
			}else{
				getRoomUsersAsync(msg.room_id, null, false, true);
			}
			
			roomList.add(room);
			roomMap.put(msg.room_id, room);
		}
		
		TARoomInfo room = roomMap.get(msg.room_id);
		room.beginMsgProcess();
		if(addLast){
			room.addMsg(msg);
		}else{
			boolean inserted = false;		
			for(int i=0;i<room.getMsgs().size();++i){
				OTTalkMsgV2 preMsg = room.getMsgs().get(i);
				if(preMsg.getTime() > msg.getTime()){
					inserted = true;
					room.getMsgs().add(i, msg);
					break;
				}
			}
			if(inserted == false){
				room.addMsg(msg);
			}
		}
		room.endMsgProcess();
		
		if(msg.read_flag == false && msg.isSendMsg() == false){
			//room.incUnread_msg_cnt();
		}
		
		OTTalkMsgV2 lastMsg = room.getLastMsg();
		if(lastMsg == null || lastMsg.getTime() < msg.getTime()){
			room.setLastMsg(msg);
		}
	 }
	
	public void removeMsg(OTTalkMsgV2 msg){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		table.deleteData(msg);
		
		TARoomInfo roomInfo = getRoomMap().get(msg.room_id);
		roomInfo.beginMsgProcess();
		roomInfo.getMsgs().remove(msg);
		roomInfo.endMsgProcess();
	}
	
	public synchronized boolean addMsg(final OTTalkMsgV2 msg){
		if(msgMap.containsKey(msg.getId())) return false;
		msgMap.put(msg.getId(), msg);
		addMsgToRoom(msg, false);
		
		OTOApp.getInstance().getCacheCtrl().getDbCommiter().addTask(new Runnable() {
			@Override public void run() {
				TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
				table.pushData(msg);
			}
		});
		return true;
	}
	
	public void addUnSendMsg(OTTalkMsgV2 msg){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		table.pushData(msg);
		tableIDMsgMap.put((long)msg.getTableIdx(), msg);
		addMsgToRoom(msg, true);
	}
	
	public void addMsgWithBeginTransact(OTTalkMsgV2 msg){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		table.pushDataWithBeginTransaction(msg);
		msgMap.put(msg.getId(), msg);
		addMsgToRoom(msg, false);
	}
	
	public void updateMsg(OTTalkMsgV2 msg){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		table.updateData(msg);
		msgMap.put(msg.getId(), msg);
	}
	
	public void updateWIthBeginTransact(OTTalkMsgV2 msg){
		TABigTableTable table = OTOApp.getInstance().getDB().getTable(OTTalkMsgV2.class.getSimpleName());
		table.updateDataWithBeginTransaction(msg);
		msgMap.put(msg.getId(), msg);
	}	
}
