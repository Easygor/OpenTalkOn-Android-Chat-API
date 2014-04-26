package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TAFreeRecent extends TABigTableBase {
	
	String name;
	String phoneNumber;
	String time;
	String shortName;
	String enName;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public TAFreeRecent(){}
	public TAFreeRecent(Parcel src){
		name = src.readString();
		phoneNumber = src.readString();
		time = src.readString();
		shortName = src.readString();
		enName = src.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeString(name);
		data.writeString(phoneNumber);
		data.writeString(time);
		data.writeString(shortName);
		data.writeString(enName);
	}
	
	public static final Parcelable.Creator<TAFreeRecent> CREATOR = new Creator<TAFreeRecent>() {
		@Override
		public TAFreeRecent[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TAFreeRecent createFromParcel(Parcel source) {
			return new TAFreeRecent(source);
		}
	};
}
