package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TANotify extends TABigTableBase {
	public final static int NOTIFY_POST_REPLY = 0;
	public final static int NOTIFY_POST_SOMETHING = 1;
	public final static int NOTIFY_BROAD_TAG = 2;
	
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

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	int type;
	long post_id;
	String user_id;
	String title;
	String msg;
	String tag;
	
	public TANotify(int type){
		this.type = type;
	}
	
	public TANotify(Parcel source) {
		type = source.readInt();
		switch(type){
		case NOTIFY_POST_REPLY:
			post_id = source.readLong();
			user_id = source.readString();
			msg = source.readString();
			break;
		case NOTIFY_POST_SOMETHING:
			post_id = source.readLong();
			user_id = source.readString();
			title = source.readString();
			break;
		case NOTIFY_BROAD_TAG:
			post_id = source.readLong();
			tag = source.readString();
			break;
		}
	}

	@Override
	public void writeToParcel(Parcel d, int arg1) {
		d.writeInt(type);
		switch(type){
		case NOTIFY_POST_REPLY:
			d.writeLong(post_id);
			d.writeString(user_id);
			d.writeString(msg);
			break;
		case NOTIFY_POST_SOMETHING:
			d.writeLong(post_id);
			d.writeString(user_id);
			d.writeString(title);
			break;
		case NOTIFY_BROAD_TAG:
			d.writeLong(post_id);
			d.writeString(tag);
			break;
		}
	}

	@Override public int describeContents() { return 0; }
	public static final Parcelable.Creator<TANotify> CREATOR = new Creator<TANotify>() {
		@Override
		public TANotify[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TANotify createFromParcel(Parcel source) {
			return new TANotify(source);
		}
	};
}
