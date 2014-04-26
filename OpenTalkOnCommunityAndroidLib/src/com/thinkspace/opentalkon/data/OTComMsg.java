package com.thinkspace.opentalkon.data;

import android.os.Parcel;

public class OTComMsg extends OTMsgBase {
	long transact_id;
	int reply_count;
	int like_count;
	long community_id;
	TAUserInfo senderInfo;
	
	public long getCommunity_id() {
		return community_id;
	}
	public void setCommunity_id(long community_id) {
		this.community_id = community_id;
	}
	public int getReply_count() {
		return reply_count;
	}
	public void setReply_count(int reply_count) {
		this.reply_count = reply_count;
	}
	public int getLike_count() {
		return like_count;
	}
	public void setLike_count(int like_count) {
		this.like_count = like_count;
	}
	public long getTransact_id() {
		return transact_id;
	}
	public void setTransact_id(long transact_id) {
		this.transact_id = transact_id;
	}
	public TAUserInfo getSenderInfo() {
		return senderInfo;
	}
	public void setSenderInfo(TAUserInfo senderInfo) {
		this.senderInfo = senderInfo;
	}
	@Override
	public int getSender_level() {
		if(senderInfo != null){
			return senderInfo.level;
		}
		return 1;
	}
	@Override public int describeContents() { return 0; }
	@Override public void writeToParcel(Parcel arg0, int arg1) {}
}
