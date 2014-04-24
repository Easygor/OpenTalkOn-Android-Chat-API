package com.thinkspace.opentalkon.satelite;

public interface PLUploadImgMsgHandler {
	public void onComplete(String img_url, Long t_id);
	public void onError(Long t_id);
}
