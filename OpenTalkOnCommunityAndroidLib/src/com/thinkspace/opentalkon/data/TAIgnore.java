package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TAIgnore extends TABigTableBase {
	Long user_id;
	
	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(user_id);
	}
	
	public TAIgnore(Parcel src){
		user_id = src.readLong();
	}
	
	public TAIgnore(Long user_id){
		this.user_id = user_id; 
	}
	
	@Override public int describeContents() { return 0; }
	public static final Parcelable.Creator<TAIgnore> CREATOR = new Creator<TAIgnore>() {
		@Override
		public TAIgnore[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TAIgnore createFromParcel(Parcel source) {
			return new TAIgnore(source);
		}
	};
}
