package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;


public class TAUserInfo implements Parcelable, Comparable<TAUserInfo>{
	long id;
	String nickName;
	String introduce;
	String locale;
	String imagePath;
	boolean friend_best;
	boolean friend;
	String facebook_id;
	boolean deny_invitation;
	boolean invite_from_friend;
	public boolean agree_term;
	public int level;
	
	public TAUserInfo(){}
	
	@Override
	public int compareTo(TAUserInfo another) {
		return compare(this, another);
	}
	
	public int compare(TAUserInfo lhs, TAUserInfo rhs) {
		if(lhs.isFriend_best()){
			if(rhs.isFriend_best()){
				return lhs.nickName.compareTo(rhs.nickName); 
			}else{
				return -1;
			}
		}
		
		if(rhs.isFriend_best()){
			if(lhs.isFriend_best()){
				return lhs.nickName.compareTo(rhs.nickName); 
			}else{
				return 1;
			}
		}
		
		return lhs.nickName.compareTo(rhs.nickName);
	}

	public TAUserInfo(Parcel data){		
		id = data.readLong();
		nickName = data.readString();
		introduce = data.readString();
		locale = data.readString();
		imagePath = data.readString();
		friend_best = data.readByte()==(byte)1?true:false;
		friend = data.readByte()==(byte)1?true:false;
		facebook_id = data.readString();
		deny_invitation = data.readByte()==(byte)1?true:false;
		invite_from_friend = data.readByte()==(byte)1?true:false;
		agree_term = data.readByte()==(byte)1?true:false;
		level = data.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeLong(id);
		data.writeString(nickName);
		data.writeString(introduce);
		data.writeString(locale);
		data.writeString(imagePath);
		data.writeByte(friend_best? (byte)1 : (byte)0);
		data.writeByte(friend? (byte)1 : (byte)0);
		data.writeString(facebook_id);
		data.writeByte(deny_invitation? (byte)1 : (byte)0);
		data.writeByte(invite_from_friend? (byte)1 : (byte)0);
		data.writeByte(agree_term? (byte)1 : (byte)0);
		data.writeInt(level);
	}
	
	public static final Parcelable.Creator<TAUserInfo> CREATOR = new Creator<TAUserInfo>() {
		@Override
		public TAUserInfo[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TAUserInfo createFromParcel(Parcel source) {
			return new TAUserInfo(source);
		}
	};
	
	public boolean isDeny_invitation() {
		return deny_invitation;
	}

	public void setDeny_invitation(boolean deny_invitation) {
		this.deny_invitation = deny_invitation;
	}

	public boolean isInvite_from_friend() {
		return invite_from_friend;
	}

	public void setInvite_from_friend(boolean invite_from_friend) {
		this.invite_from_friend = invite_from_friend;
	}

	public String getFacebook_id() {
		return facebook_id;
	}

	public void setFacebook_id(String facebook_id) {
		this.facebook_id = facebook_id;
	}

	public boolean isFriend_best() {
		return friend_best;
	}

	public void setFriend_best(boolean friend_best) {
		this.friend_best = friend_best;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public boolean is_friend() {
		return friend;
	}

	public void set_friend(boolean friend) {
		this.friend = friend;
	}
}
