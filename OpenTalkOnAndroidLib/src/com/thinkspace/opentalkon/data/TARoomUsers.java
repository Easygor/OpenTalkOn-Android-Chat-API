package com.thinkspace.opentalkon.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class TARoomUsers extends TABigTableBase {
	Long room_id;
	ArrayList<Long> users;
	
	public Long getRoom_id() {
		return room_id;
	}

	public void setRoom_id(Long room_id) {
		this.room_id = room_id;
	}

	public ArrayList<Long> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<Long> users) {
		this.users = users;
	}

	public TARoomUsers(){}
	
	@SuppressWarnings("unchecked")
	public TARoomUsers(Parcel data){
		room_id = data.readLong();
		users = (ArrayList<Long>) data.readSerializable();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeLong(room_id);
		data.writeSerializable(users);
	}
	
	public static final Parcelable.Creator<TARoomUsers> CREATOR = new Creator<TARoomUsers>() {
		@Override
		public TARoomUsers[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TARoomUsers createFromParcel(Parcel source) {
			return new TARoomUsers(source);
		}
	};
}
