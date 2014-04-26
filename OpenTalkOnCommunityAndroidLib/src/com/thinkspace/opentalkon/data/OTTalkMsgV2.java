package com.thinkspace.opentalkon.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class OTTalkMsgV2 extends OTMsgBase{
	public Long room_id = 0L;
	public boolean read_flag;
	public boolean exitMsg;
	public boolean inviteMsg;
	public JSONObject inviteUsers = new JSONObject();
	public Long unread_cnt = 0L;
	public boolean publicRoom;
	public boolean enterMsg;
	public boolean kickMsg;
	public JSONObject kickUsers = new JSONObject();
	public boolean changeBjMsg;
	public Long changedBjId = 0L;
	public String changedBjNickName = "";
	public boolean roomHiddenMsg;
	public boolean hiddenFlag;
	
	public boolean isKickMsg() {
		return kickMsg;
	}
	public void setKickMsg(boolean kickMsg) {
		this.kickMsg = kickMsg;
	}
	public JSONObject getKickUsers() {
		return kickUsers;
	}
	public void setKickUsers(JSONObject kickUsers) {
		this.kickUsers = kickUsers;
	}
	public boolean isChangeBjMsg() {
		return changeBjMsg;
	}
	public void setChangeBjMsg(boolean changeBjMsg) {
		this.changeBjMsg = changeBjMsg;
	}
	public Long getChangedBjId() {
		return changedBjId;
	}
	public void setChangedBjId(Long changedBjId) {
		this.changedBjId = changedBjId;
	}
	public boolean isRoomHiddenMsg() {
		return roomHiddenMsg;
	}
	public void setRoomHiddenMsg(boolean roomHiddenMsg) {
		this.roomHiddenMsg = roomHiddenMsg;
	}
	public boolean isHiddenFlag() {
		return hiddenFlag;
	}
	public void setHiddenFlag(boolean hiddenFlag) {
		this.hiddenFlag = hiddenFlag;
	}
	public Long getRoom_id() {
		return room_id;
	}
	public void setRoom_id(Long room_id) {
		this.room_id = room_id;
	}
	public boolean isRead_flag() {
		return read_flag;
	}
	public void setRead_flag(boolean read_flag) {
		this.read_flag = read_flag;
	}
	public boolean isExitMsg() {
		return exitMsg;
	}
	public void setExitMsg(boolean exitMsg) {
		this.exitMsg = exitMsg;
	}
	public boolean isInviteMsg() {
		return inviteMsg;
	}
	public void setInviteMsg(boolean inviteMsg) {
		this.inviteMsg = inviteMsg;
	}
	public JSONObject getInviteUsers() {
		return inviteUsers;
	}
	public void setInviteUsers(JSONObject inviteUsers) {
		this.inviteUsers = inviteUsers;
	}
	public Long getUnread_cnt() {
		return unread_cnt;
	}
	public void setUnread_cnt(Long unread_cnt) {
		this.unread_cnt = unread_cnt;
	}
	public boolean isPublicRoom() {
		return publicRoom;
	}
	public void setPublicRoom(boolean publicRoom) {
		this.publicRoom = publicRoom;
	}
	public boolean isEnterMsg() {
		return enterMsg;
	}
	public void setEnterMsg(boolean enterMsg) {
		this.enterMsg = enterMsg;
	}
	public String getChangedBjNickName() {
		return changedBjNickName;
	}
	public void setChangedBjNickName(String changedBjNickName) {
		this.changedBjNickName = changedBjNickName;
	}
	public OTTalkMsgV2(){}
	public OTTalkMsgV2(Parcel data){
		version = data.readInt();
		id = data.readLong();
		sender_id = data.readLong();
		sender_level = data.readInt();
		room_id = data.readLong();
		msg = data.readString();
		time = data.readLong();
		read_flag = data.readInt() == 0 ? false : true;
		sendMsg = data.readInt() == 0 ? false : true;
		imgMsg = data.readInt() == 0 ? false : true;
		exitMsg = data.readInt() == 0 ? false : true;
		unread_cnt = data.readLong();
		inviteMsg = data.readInt() == 0 ? false : true;
		String inviteUserRawString = data.readString();
		String imgUrlRawString = data.readString();
		String preImgUrlRawString = data.readString();
		publicRoom = data.readInt() == 0 ? false : true;
		enterMsg = data.readInt() == 0 ? false : true;
		kickMsg = data.readInt() == 0 ? false : true;
		String kickUsersRawString = data.readString();
		changeBjMsg = data.readInt() == 0 ? false : true;
		changedBjId = data.readLong();
		changedBjNickName = data.readString();
		roomHiddenMsg = data.readInt() == 0 ? false : true;
		hiddenFlag = data.readInt() == 0 ? false : true;
		
		if(inviteUserRawString != null && inviteUserRawString.length() > 0){
			try {
				inviteUsers = new JSONObject(inviteUserRawString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(imgUrlRawString != null && imgUrlRawString.length() > 0){
			try {
				img_url = new JSONArray(imgUrlRawString);
			} catch (JSONException e) {
				e.printStackTrace();
			}			
		}
		if(preImgUrlRawString != null && preImgUrlRawString.length() > 0){
			try {
				preSendImg_url = new JSONArray(preImgUrlRawString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(kickUsersRawString != null && kickUsersRawString.length() > 0){
			try {
				kickUsers = new JSONObject(kickUsersRawString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeInt(version);
		data.writeLong(id);
		data.writeLong(sender_id);
		data.writeInt(sender_level);
		data.writeLong(room_id);
		data.writeString(msg);
		data.writeLong(time);
		data.writeInt(read_flag?1:0);
		data.writeInt(sendMsg?1:0);
		data.writeInt(imgMsg?1:0);
		data.writeInt(exitMsg?1:0);
		data.writeLong(unread_cnt);
		
		data.writeInt(inviteMsg?1:0);
		if(inviteUsers != null && inviteUsers.length() > 0){
			data.writeString(inviteUsers.toString());
		}else{
			data.writeString("");
		}
		
		if(img_url != null && img_url.length() > 0){
			data.writeString(img_url.toString());
		}else{
			data.writeString("");
		}
		if(preSendImg_url != null && preSendImg_url.length() > 0){
			data.writeString(preSendImg_url.toString());
		}else{
			data.writeString("");
		}
		data.writeInt(publicRoom?1:0);
		data.writeInt(enterMsg?1:0);
		
		data.writeInt(kickMsg?1:0);
		if(kickUsers != null && kickUsers.length() > 0){
			data.writeString(kickUsers.toString());
		}else{
			data.writeString("");
		}
		
		data.writeInt(changeBjMsg?1:0);
		data.writeLong(changedBjId);
		data.writeString(changedBjNickName);
		
		data.writeInt(roomHiddenMsg?1:0);
		data.writeInt(hiddenFlag?1:0);
	}
	
	public static final Parcelable.Creator<OTTalkMsgV2> CREATOR = new Creator<OTTalkMsgV2>() {
		@Override
		public OTTalkMsgV2[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public OTTalkMsgV2 createFromParcel(Parcel source) {
			return new OTTalkMsgV2(source);
		}
	};
}
