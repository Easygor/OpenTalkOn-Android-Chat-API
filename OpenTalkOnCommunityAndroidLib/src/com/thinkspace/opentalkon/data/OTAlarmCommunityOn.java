package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class OTAlarmCommunityOn extends TABigTableBase {
	Long community_id;

	public Long getCommunity_id() {
		return community_id;
	}

	public void setCommunity_id(Long community_id) {
		this.community_id = community_id;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(community_id);
	}
	
	public OTAlarmCommunityOn(Parcel src){
		community_id = src.readLong();
	}
	
	public OTAlarmCommunityOn(Long community_id){
		this.community_id = community_id; 
	}
	
	@Override public int describeContents() { return 0; }
	public static final Parcelable.Creator<OTAlarmCommunityOn> CREATOR = new Creator<OTAlarmCommunityOn>() {
		@Override
		public OTAlarmCommunityOn[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public OTAlarmCommunityOn createFromParcel(Parcel source) {
			return new OTAlarmCommunityOn(source);
		}
	};
}
