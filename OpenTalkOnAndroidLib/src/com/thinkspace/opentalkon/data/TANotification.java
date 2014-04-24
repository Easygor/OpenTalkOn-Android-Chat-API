package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TANotification extends TABigTableBase implements Comparable<TANotification>{
	public final static int TYPE_LIKE_MINE = 0;
	public final static int TYPE_REPLY_MINE = 1;
	public final static int TYPE_REPLY = 2;
	
	String nick_name;
	long user_id;
	int type;
	long post_id;
	long date;
	boolean check;
	long id = -1L;
	
	@Override
	public int compareTo(TANotification another) {
		if(check && !another.check){
			return 1;
		}else if(!check && another.check){
			return -1;
		}else{
			return date < another.date?1:-1;
		}
	}

	public String getNick_name() {
		return nick_name;
	}

	public void setNick_name(String nick_name) {
		this.nick_name = nick_name;
	}

	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getPost_id() {
		return post_id;
	}

	public void setPost_id(long post_id) {
		this.post_id = post_id;
	}
	
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TANotification(){}

	public TANotification(Parcel data){
		nick_name = data.readString();
		user_id = data.readLong();
		type = data.readInt();
		post_id = data.readLong();
		date = data.readLong();
		check = data.readByte()==1?true:false;
		if(data.dataAvail() > 0){
			id = data.readLong();
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeString(nick_name);
		data.writeLong(user_id);
		data.writeInt(type);
		data.writeLong(post_id);
		data.writeLong(date);
		data.writeByte(check?(byte)1:(byte)0);
		if(id != -1){
			data.writeLong(id);
		}
	}
	
	public static final Parcelable.Creator<TANotification> CREATOR = new Creator<TANotification>() {
		@Override
		public TANotification[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TANotification createFromParcel(Parcel source) {
			return new TANotification(source);
		}
	};
}
