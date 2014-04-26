package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class OTAlarmTalkOff extends TABigTableBase {
	Long room_id;

	public Long getRoom_id() {
		return room_id;
	}

	public void setRoom_id(Long room_id) {
		this.room_id = room_id;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(room_id);
	}
	
	public OTAlarmTalkOff(Parcel src){
		room_id = src.readLong();
	}
	
	public OTAlarmTalkOff(Long room_id){
		this.room_id = room_id; 
	}
	
	@Override public int describeContents() { return 0; }
	public static final Parcelable.Creator<OTAlarmTalkOff> CREATOR = new Creator<OTAlarmTalkOff>() {
		@Override
		public OTAlarmTalkOff[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public OTAlarmTalkOff createFromParcel(Parcel source) {
			return new OTAlarmTalkOff(source);
		}
	};
}
