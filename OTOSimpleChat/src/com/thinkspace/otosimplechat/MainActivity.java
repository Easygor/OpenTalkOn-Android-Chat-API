package com.thinkspace.otosimplechat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.lib.ClientInterface;

public class MainActivity extends Activity {
	TextView myId;
	EditText editId;
	Button chatButton;
	
	boolean registered;
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			myId.setText(getString(R.string.my_id) + " " + OTOApp.getInstance().getId());
		}
	};
	
	@Override
	protected void onDestroy() {
		if(registered){
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myId = (TextView) findViewById(R.id.my_id);
		editId = (EditText) findViewById(R.id.edittext_id);
		chatButton = (Button) findViewById(R.id.chat_button);
		
		if(OTOApp.getInstance().getId() == -1L){
			registered = true;
			IntentFilter iff = new IntentFilter(OTOApp.ACTION_GET_VALID_TOKEN_IS_DONE);
			registerReceiver(receiver, iff);
		}else{
			myId.setText(getString(R.string.my_id) + " " + OTOApp.getInstance().getId());
		}
		
		chatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String id = editId.getText().toString();
				if(id.length() > 0){
					ClientInterface.startChatRoom(MainActivity.this, Long.valueOf(id));
				}
			}
		});
	}
	
}
