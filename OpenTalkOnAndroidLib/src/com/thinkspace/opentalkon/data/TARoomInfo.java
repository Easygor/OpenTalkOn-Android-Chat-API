package com.thinkspace.opentalkon.data;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class TARoomInfo implements Comparable<TARoomInfo>{
	OTTalkMsgV2 lastMsg;
	Long room_id;
	ArrayList<Long> users = new ArrayList<Long>();
	ArrayList<OTTalkMsgV2> msgs = new ArrayList<OTTalkMsgV2>();
	
	Semaphore sema = new Semaphore(1);
	
	@Override
	public int compareTo(TARoomInfo another) {
		if(another.lastMsg != null && lastMsg != null){
			return another.lastMsg.getTime().compareTo(lastMsg.getTime());
		}else if(lastMsg != null){
			return -1;
		}else if(another.lastMsg != null){
			return 1;
		}else{
			return 0;
		}
	}

	public TARoomInfo(){
		
	}
	
	public void setUsers(ArrayList<Long> users) {
		this.users = users;
	}

	public ArrayList<Long> getUsers() {
		return users;
	}

	@SuppressWarnings("unchecked")
	public int getUnread_msg_cnt() {
		int count = 0;
		ArrayList<OTTalkMsgV2> msgsClone = (ArrayList<OTTalkMsgV2>)msgs.clone();
		for(OTTalkMsgV2 msg : msgsClone){
			if(msg.isSendMsg()) continue;
			if(msg.isRead_flag() == false) ++count;
		}
		return count;
	}
	
	public Long getRoom_id() {
		return room_id;
	}

	public void setRoom_id(Long room_id) {
		this.room_id = room_id;
	}

	public void setMsgs(ArrayList<OTTalkMsgV2> msgs) {
		this.msgs = msgs;
	}

	public OTTalkMsgV2 getLastMsg() {
		return lastMsg;
	}
	public void setLastMsg(OTTalkMsgV2 lastMsg) {
		this.lastMsg = lastMsg;
	}
	
	public void beginMsgProcess(){
		try {
			sema.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void endMsgProcess(){
		sema.release();
	}

	public ArrayList<OTTalkMsgV2> getMsgs() {
		return msgs;
	}
	
	public void addMsg(OTTalkMsgV2 msg){
		msgs.add(msg);
	}
	public void addUser(Long user_id){
		users.add(user_id);
	}
}
