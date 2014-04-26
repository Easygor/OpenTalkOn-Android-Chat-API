package com.thinkspace.opentalkon.ui;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;

public class OTAddFriend extends Activity implements TADataHandler {
	EditText idEditText;
	View searchButton;
	TextView caption;
	TASatelite satelite;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_add_friend);
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		idEditText = (EditText) findViewById(R.id.oto_add_friend_edittext);
		searchButton = findViewById(R.id.oto_add_friend_search);
		caption = (TextView) findViewById(R.id.oto_add_friend_caption);
		satelite = new TASatelite(this);
		
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = idEditText.getText().toString();
				if(text.length() == 0) return;
				satelite.doGetUserInfoByNick(OTOApp.getInstance().getToken(), text);
				OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTAddFriend.this);
			}
		});
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
		
		if(dData.hasLocation(TASatelite.getName(TASatelite.GET_USER_INFO_BY_NICK_URL))){
			if(dData.isOK()){
				TAUserInfo userInfo = (TAUserInfo)dData.getData();
				Intent intent = new Intent(this, OTFriendPopup.class);
				intent.putExtra("user_info", userInfo);
				startActivity(intent);
			}else{
				if(dData.getState().equals("user_id is not valid")){
					caption.setText(getString(R.string.oto_add_friend_alert2));
				}
			}
		}
	}

	@Override
	public void onTokenIsNotValid(JSONObject data) {
		finish();
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		finish();
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
}
