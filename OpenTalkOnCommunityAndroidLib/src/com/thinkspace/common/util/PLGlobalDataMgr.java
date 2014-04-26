package com.thinkspace.common.util;

import java.util.HashMap;
import java.util.Map;

public class PLGlobalDataMgr {
	Map<String, Object> globalData;
	public PLGlobalDataMgr(){
		globalData = new HashMap<String, Object>();
	}
	
	public Object getData(String key){
		if(globalData.containsKey(key)){
			return globalData.get(key);
		}
		return null;
	}
	public Object pollData(String key){
		if(globalData.containsKey(key)){
			Object ret = globalData.get(key);
			globalData.remove(key);
			return ret;
		}
		return null;	
	}
	
	public void pushData(String key, Object data){
		if(data != null){
			globalData.put(key, data);
		}
	}
}
