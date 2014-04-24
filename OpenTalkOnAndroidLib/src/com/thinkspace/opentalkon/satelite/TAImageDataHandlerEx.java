package com.thinkspace.opentalkon.satelite;

import android.graphics.Bitmap;

public interface TAImageDataHandlerEx {
	public void onHttpImagePacketReceived(String url, Bitmap bitmap, Object giveData);
	public void onHttpImageException(Exception ex);
}
