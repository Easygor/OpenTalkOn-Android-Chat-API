package com.thinkspace.pushservice.satelite;

import com.thinkspace.opentalkon.data.OTMsgBase;

public interface PLMsgHandler {
	public void onMsgReceived(OTMsgBase msg);
}
