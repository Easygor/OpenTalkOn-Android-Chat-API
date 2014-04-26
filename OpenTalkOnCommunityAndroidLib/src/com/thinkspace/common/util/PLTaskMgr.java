package com.thinkspace.common.util;

public class PLTaskMgr {
	public PLTaskMgr(){}
	public void runTask(Runnable runnable){
		new Thread(runnable).start();
	}
}
