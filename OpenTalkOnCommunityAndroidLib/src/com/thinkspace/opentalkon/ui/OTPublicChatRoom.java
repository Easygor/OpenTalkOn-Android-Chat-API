package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.common.util.PLDialogListener;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTTalkMsgV2;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAPublicRoomInfo;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;

public class OTPublicChatRoom extends OTChatRoom {
	public final static int OT_KICK_USER = 82;
	public final static int OT_CHANGE_BJ = 83;
	boolean admin;
	
	public TAPublicRoomInfo getRoomInfo(){
		return (TAPublicRoomInfo) roomInfo;
	}

	@Override
	protected boolean hasOwnBackPressedProcess() {
		if(roomInfo != null){
			OTOApp.getInstance().getDialogMaker().makeYesNoDialog(getString(R.string.oto_public_opentalk),
				getString(R.string.oto_publictalk_exit_msg),
				new PLDialogListener() {
					@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) {}
					@Override public void onDialogSelectedWithData(int dialogId, int pos, Object data) {}
					@Override public void onDialogSelected(int dialogId, int pos) {
						if(pos != DialogInterface.BUTTON_POSITIVE){
							satelite.doExitRoom(OTOApp.getInstance().getToken(), roomInfo.getRoom_id());
						}else{
							finish();
						}
					}
				}, this, 0);
			return true;
		}else{
			return super.hasOwnBackPressedProcess();
		}
	}

	@Override
	public void OnOptionButtionClicked(int idx) {
		super.OnOptionButtionClicked(idx);
		if(idx == 6){
			if(roomInfo != null && (getRoomInfo().getOwner() == OTOApp.getInstance().getId() || admin)){
				AlertDialog.Builder ab = new Builder(OTPublicChatRoom.this);
				ab.setTitle(getString(R.string.oto_management));
				if(getRoomInfo().isHidden()){
					ab.setItems(getResources().getStringArray(R.array.oto_public_room_management_public),
					new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							switch(which){
							case 0:{
								ArrayList<Long> cand = new ArrayList<Long>();
								for(Long user : roomInfo.getUsers()){
									if(user.equals(OTOApp.getInstance().getId())) continue;
									cand.add(user);
								}
								startActivityForResult(new Intent(OTPublicChatRoom.this, OTSelectUser.class).putExtra("users", cand), OT_KICK_USER);
								break;
							}case 1:{
								ArrayList<Long> cand = new ArrayList<Long>();
								for(Long user : roomInfo.getUsers()){
									if(user.equals(OTOApp.getInstance().getId())) continue;
									cand.add(user);
								}
								startActivityForResult(new Intent(OTPublicChatRoom.this, OTSelectUser.class)
									.putExtra("users", cand).putExtra("single", true), OT_CHANGE_BJ);
								break;
							}case 2:
								satelite.doSetPublicRoomHidden(OTOApp.getInstance().getToken(), getRoomInfo().getRoom_id(), false);
								break;
							}
						}
					});
				}else{
					ab.setItems(getResources().getStringArray(R.array.oto_public_room_management_private),
					new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							switch(which){
							case 0:{
								ArrayList<Long> cand = new ArrayList<Long>();
								for(Long user : roomInfo.getUsers()){
									if(user.equals(OTOApp.getInstance().getId())) continue;
									cand.add(user);
								}
								startActivityForResult(new Intent(OTPublicChatRoom.this, OTSelectUser.class).putExtra("users", cand), OT_KICK_USER);
								break;
							}case 1:{
								ArrayList<Long> cand = new ArrayList<Long>();
								for(Long user : roomInfo.getUsers()){
									if(user.equals(OTOApp.getInstance().getId())) continue;
									cand.add(user);
								}
								startActivityForResult(new Intent(OTPublicChatRoom.this, OTSelectUser.class)
									.putExtra("users", cand).putExtra("single", true), OT_CHANGE_BJ);
								break;
							}case 2:
								satelite.doSetPublicRoomHidden(OTOApp.getInstance().getToken(), getRoomInfo().getRoom_id(), true);
								break;
							}
						}
					});
				}
				ab.show();
			}
		}
	}

	@Override
	public void onNotify(Notify packet) {
		
	}

	@Override
	public void onMsgCountSetting(OTTalkMsgV2 msg, MsgHolder holder) {
		holder.setStatus(MsgHolder.STATUS_READ, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == OT_KICK_USER){
				ArrayList<Long> users = (ArrayList <Long>)data.getSerializableExtra("selected_users");
				if(users.size() > 0){
					JSONArray arr = new JSONArray();
					for(Long user : users) arr.put(user);
					satelite.doKickPublicRoomUser(OTOApp.getInstance().getToken(), roomInfo.getRoom_id(), arr);
				}
			}else if(requestCode == OT_CHANGE_BJ){
				ArrayList<Long> users = (ArrayList <Long>)data.getSerializableExtra("selected_users");
				users.size();
				if(users.size() > 0){
					satelite.doGivePublicRoomBj(OTOApp.getInstance().getToken(), roomInfo.getRoom_id(), users.get(0));
				}
			}
		}
	}

	@Override
	public void OnSetupOptionButton(int idx, ImageView image, TextView text) {
		super.OnSetupOptionButton(idx, image, text);
		if(idx == 6){
			if(roomInfo != null && (getRoomInfo().getOwner() == OTOApp.getInstance().getId() || admin)){
				image.setImageResource(R.drawable.oto_conv_setting);
				text.setText(getString(R.string.oto_management));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent == null || intent.hasExtra("room_id") == false){
			finish();
			return;
		}
		
		long room_id = intent.getLongExtra("room_id", -1L);
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
		satelite.doGetPublicRoomInfo(OTOApp.getInstance().getToken(), room_id);
		satelite.doCheckAdmin(OTOApp.getInstance().getToken(), OTOApp.getInstance().getId());
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		super.onHttpPacketReceived(data);
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(TASatelite.GET_PUBLIC_ROOM_INFO.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					TAPublicRoomInfo room = TASateliteDispatcher.dispatchPublicRoomInfo(realData);
					OTOApp.getInstance().getCacheCtrl().addNewRoomInfo(room);
					applyRoomInfo(room);
					doSetupFeed(true);
					
					setTalkConvTitle(room.getUsers());
				}
			}else if(TASatelite.SET_PUBLIC_ROOM_HIDDEN.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					TAPublicRoomInfo newRoom = TASateliteDispatcher.dispatchPublicRoomInfo(realData);
					OTOApp.getInstance().getCacheCtrl().addNewRoomInfo(newRoom);
					roomInfo = newRoom;
				}
			}else if(TASatelite.KICK_PUBLIC_ROOM_USER.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					boolean cannotKickAdmin = realData.getBoolean("cannotKickAdmin");
					if(cannotKickAdmin){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_cannot_kick_admin), this);
					}
				}else{
					if(state.equals("cannot kick admin")){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_cannot_kick_admin), this);
					}
				}
			}else if(TASatelite.CHECK_ADMIN.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					admin = realData.getBoolean("admin");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	protected OTTalkMsgV2 makeSendMessage(String textMsg) {
		OTTalkMsgV2 talkMsg = super.makeSendMessage(textMsg);
		talkMsg.setPublicRoom(true);
		return talkMsg;
	}

	@Override
	protected OTTalkMsgV2 makeSendImgMessage(List<String> imagePaths, String textMsg) {
		OTTalkMsgV2 talkMsg = super.makeSendImgMessage(imagePaths, textMsg);
		talkMsg.setPublicRoom(true);
		return talkMsg;
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		super.onHttpException(ex, data, addr);
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		super.onHttpException(ex, data, addr);
	}

	@Override
	public void setTalkConvTitle(List<Long> users) {
		if(getRoomInfo() != null){
			titleView.setText("(" + getRoomInfo().getUsers().size() +")"+getRoomInfo().getName());
			leftTopView.setVisibility(View.VISIBLE);
			if(getRoomInfo().isHidden()){
				leftTopView.setImageResource(R.drawable.oto_lock);
			}else{
				leftTopView.setImageResource(R.drawable.oto_unlock);
			}
		}else{
			leftTopView.setVisibility(View.GONE);
		}
	}
}
