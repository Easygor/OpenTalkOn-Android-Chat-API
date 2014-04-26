package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CommunityData implements Comparable<CommunityData>, Parcelable{
	public Long id = -1L;
	public String title;
	public String description;
	public String img_url;
	public String img_url2;
	public boolean admin_write_only;
	public boolean write_method_chat;
	public boolean need_picture;
	public boolean is_admin;
	public boolean is_ban;
	public boolean alarm;
	public Long sequence = -1L;
	public Long last_time = -1L;
	public boolean public_opentalk;
	public boolean community_group;
	public Long parent_community_id = -1L;
	
	@Override
	public int compareTo(CommunityData another) {
		return sequence.compareTo(another.sequence);
	}
	
	public int describeContents() {
		return 0;
	}
	
	public CommunityData(){}
	
	public CommunityData(Parcel src){
		id = src.readLong();
		title = src.readString();
		description = src.readString();
		img_url = src.readString();
		img_url2 = src.readString();
		admin_write_only = src.readInt()==1?true:false;
		write_method_chat = src.readInt()==1?true:false;
		need_picture = src.readInt()==1?true:false;
		is_admin = src.readInt()==1?true:false;
		is_ban = src.readInt()==1?true:false;
		alarm = src.readInt()==1?true:false;
		sequence = src.readLong();
		last_time = src.readLong();
		public_opentalk = src.readInt()==1?true:false;
		community_group = src.readInt()==1?true:false;
		parent_community_id = src.readLong();
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeLong(id);
		data.writeString(title);
		data.writeString(description);
		data.writeString(img_url);
		data.writeString(img_url2);
		data.writeInt(admin_write_only?1:0);
		data.writeInt(write_method_chat?1:0);
		data.writeInt(need_picture?1:0);
		data.writeInt(is_admin?1:0);
		data.writeInt(is_ban?1:0);
		data.writeInt(alarm?1:0);
		data.writeLong(sequence);
		data.writeLong(last_time);
		data.writeInt(public_opentalk?1:0);
		data.writeInt(community_group?1:0);
		data.writeLong(parent_community_id);
	}
	
	public static final Parcelable.Creator<CommunityData> CREATOR = new Creator<CommunityData>() {
		@Override
		public CommunityData[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public CommunityData createFromParcel(Parcel source) {
			return new CommunityData(source);
		}
	};
}
