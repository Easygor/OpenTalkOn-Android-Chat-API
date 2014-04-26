package com.thinkspace.opentalkon.satelite;

import android.graphics.Bitmap;

public interface TAImageDataHandler {
	public void onHttpImagePacketReceived(String url, Bitmap bitmap);
	public void onHttpImageException(Exception ex);
}
