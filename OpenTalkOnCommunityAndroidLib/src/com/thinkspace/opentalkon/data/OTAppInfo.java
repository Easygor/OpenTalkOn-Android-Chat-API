package com.thinkspace.opentalkon.data;

import android.os.Parcel;
import android.os.Parcelable;

public class OTAppInfo implements Parcelable{
	public long id;
	public long app_owner_id;
	public String app_name;
	public String package_name;
	public String img_path;
	public String description;
	
	public OTAppInfo(){}
	
	public OTAppInfo(Parcel data){
		id = data.readLong();
		app_owner_id = data.readLong();
		app_name = data.readString();
		package_name = data.readString();
		img_path = data.readString();
		description = data.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel data, int flag) {
		data.writeLong(id);
		data.writeLong(app_owner_id);
		data.writeString(app_name);
		data.writeString(package_name);
		data.writeString(img_path);
		data.writeString(description);
	}
	
	public static final Parcelable.Creator<OTAppInfo> CREATOR = new Creator<OTAppInfo>() {
		@Override
		public OTAppInfo[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public OTAppInfo createFromParcel(Parcel source) {
			return new OTAppInfo(source);
		}
	};
}
