package com.thinkspace.common.satelite;

import com.thinkspace.opentalkon.data.TAMultiData;

public interface PLHttpErrorHandler {
	public void onHttpError(Exception ex, String data, String addr);
	public void onHttpError(Exception ex, TAMultiData data, String addr);
}
