package com.thinkspace.opentalkon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GetValidTokenDoneReceiver extends BroadcastReceiver {
	List<BroadcastReceiver> receivers = Collections.synchronizedList(new ArrayList<BroadcastReceiver>());
	public GetValidTokenDoneReceiver() {}
	
	public void addDisposableReceiver(BroadcastReceiver receiver){
		if(receivers.contains(receiver) == false){
			receivers.add(receiver);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null) return;
		if(intent.hasExtra("packageName") == false) return;
		if(intent.hasExtra("result") == false) return;
		
		String result = intent.getStringExtra("result");
		if(intent.getStringExtra("packageName").equals(context.getPackageName())){
			if(result.equals("ok")){
				for(BroadcastReceiver receiver : receivers){
					receiver.onReceive(context, intent);
				}
				receivers.clear();
			}
		}
	}
}
