package com.thinkspace.opentalkon.data;


public class TAPublicRoomInfo extends TARoomInfo{
	String name;
	String img_url;
	long last_msg_time;
	long owner;
	boolean hidden;
	TAUserInfo ownerInfo;

	public TAPublicRoomInfo(){}
	
	@Override
	public int compareTo(TARoomInfo another) {
		if(another instanceof TAPublicRoomInfo == false) return -1;
		TAPublicRoomInfo other = (TAPublicRoomInfo) another;
		if(last_msg_time == other.last_msg_time) return 0;
		return last_msg_time < other.last_msg_time?1:-1;
	}

	@Override
	public void setLastMsg(OTTalkMsgV2 lastMsg) {
		super.setLastMsg(lastMsg);
		last_msg_time = lastMsg.getTime();
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public TAUserInfo getOwnerInfo() {
		return ownerInfo;
	}

	public void setOwnerInfo(TAUserInfo ownerInfo) {
		this.ownerInfo = ownerInfo;
	}

	public long getOwner() {
		return owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	public long getLast_msg_time() {
		return last_msg_time;
	}

	public void setLast_msg_time(long last_msg_time) {
		this.last_msg_time = last_msg_time;
	}

	public String getImg_url() {
		return img_url;
	}

	public void setImg_url(String img_url) {
		this.img_url = img_url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
