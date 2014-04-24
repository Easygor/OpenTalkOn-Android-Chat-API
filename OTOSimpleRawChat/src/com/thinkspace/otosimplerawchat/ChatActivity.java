package com.thinkspace.otosimplerawchat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.lib.ex.ClientInterfaceEx;
import com.thinkspace.opentalkon.lib.ex.Message;
import com.thinkspace.opentalkon.lib.ex.MessageHandler;
import com.thinkspace.opentalkon.lib.ex.MessageUnReadCount;
import com.thinkspace.opentalkon.satelite.TADataHandler;

public class ChatActivity extends Activity implements TADataHandler, MessageHandler{
	ListView msgListView;
	ListAdapter msgAdapter;
	EditText msgEdit;
	Button sendButton;
	long room_id;
	long transact_id;
	Map<Long, String> msgMap = new HashMap<Long, String>();
	List<MessageInfo> msgList = new ArrayList<MessageInfo>(); 
	
	class MessageInfo{
		public boolean right;
		public String msg;
		public MessageInfo(boolean right, String msg){
			this.right = right;
			this.msg = msg;
		}
	}
	
	void addMsg(boolean right, String msg){
		msgList.add(new MessageInfo(right, msg));
		msgListView.setSelection(msgAdapter.getCount() - 1);
		msgListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				msgListView.setSelection(msgAdapter.getCount() - 1);
			}
		}, 30L);
		msgAdapter.notifyDataSetChanged();
	}
	
	class ListHolder{
		public MessageInfo msg;
		public TextView textView;
		public ViewGroup mainView;
		public ListHolder(){
			mainView = new FrameLayout(ChatActivity.this);
			textView = new TextView(ChatActivity.this);
			textView.setTextColor(Color.rgb(0, 0, 0));
			mainView.setTag(this);
			mainView.addView(textView);
		}
		
		public void setGravity(int gravity){
			FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			param.gravity = gravity;
			textView.setLayoutParams(param);
		}
	}
	
	class ListAdapter extends ArrayAdapter<MessageInfo>{
		public ListAdapter(Context context, int textViewResourceId, List<MessageInfo> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListHolder holder = null;
			if(convertView == null){
				holder = new ListHolder();
			}else{
				holder = (ListHolder) convertView.getTag();
			}
			MessageInfo now = msgList.get(position);
			holder.textView.setText(now.msg);
			if(now.right){
				holder.setGravity(Gravity.RIGHT);
			}else{
				holder.setGravity(Gravity.LEFT);
			}
			
			return holder.mainView;
		}
	}
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(location.equals("getormakeroom")){
				OTOApp.getInstance().getUIMgr().dismissDialogProgress();
				if(state.equals("ok")){
					JSONObject rData = data.getJSONObject("data");
					room_id = rData.getLong("room_id");
					Application.getInstance().setLastRoomId(room_id);
				}
			}else if(location.equals("sendmessage")){
				if(state.equals("ok")){
					JSONObject rData = data.getJSONObject("data");
					long transact_id = rData.getLong("transact_id");
					addMsg(true, OTOApp.getInstance().getId() + " : " + msgMap.get(transact_id));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override public void onTokenIsNotValid(JSONObject data) {}
	@Override public void onLimitMaxUser(JSONObject data) {}
	@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
	@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_layout);
		
		room_id = -1L;
		
		msgListView = (ListView) findViewById(R.id.msg_container);
		msgEdit = (EditText) findViewById(R.id.msg_edit);
		sendButton = (Button) findViewById(R.id.send_button);
		msgListView.setAdapter(msgAdapter = new ListAdapter(this, -1, msgList));
		
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		long user_id = intent.getLongExtra("user_id", -1L);
		if(user_id != -1L){
			List<Long> users = new ArrayList<Long>();
			users.add(user_id);
			users.add(OTOApp.getInstance().getId());
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
			ClientInterfaceEx.doGetOrMakeRoom(this, users);
		}else{
			room_id = intent.getLongExtra("room_id", -1L);
			Application.getInstance().setLastRoomId(room_id);
			String lastMsg = intent.getStringExtra("last_msg");
			addMsg(false, lastMsg);
		}
		
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(room_id == -1L) return;
				String msg = msgEdit.getText().toString();
				msgEdit.getText().clear();
				if(msg.length() > 0){
					long nowTransactid = transact_id++;
					msgMap.put(nowTransactid, msg);
					ClientInterfaceEx.doSendMessage(ChatActivity.this, room_id, msg, nowTransactid);
				}
			}
		});
		Application.getInstance().registerMsgHandler(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Application.getInstance().unRegisterMsgHandler(this);
	}

	@Override
	public void onMessageReceived(Message message) {
		addMsg(false, message.senderId + " : " + message.textMessage);
	}

	@Override
	public void onMessageUnReadCount(List<MessageUnReadCount> messageUnReadCount) {
		// TODO Auto-generated method stub
		
	}

}
