package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.common.util.PLDialogListener;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTMsgBase;
import com.thinkspace.opentalkon.data.OTTalkMsgV2;
import com.thinkspace.opentalkon.data.TAImgMsgData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TARoomInfo;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;

public class OTChatRoom extends OTRoomBase {
	public final static int OT_INVITE_USER = 81;
	class PreRoomInfo{
		public long room_id;
		public ArrayList<Long> userList;
	}
	TARoomInfo roomInfo;
	PreRoomInfo preRoomInfo;
	Map<Long, OTMsgBase> pendingMsgMap = new HashMap<Long, OTMsgBase>();
	Map<Long, Boolean> pendingReadCheckMap = new ConcurrentHashMap<Long, Boolean>();
	Map<Long, Boolean> pendingNotMyMsgReadCheckMap = new ConcurrentHashMap<Long, Boolean>();
	List<OTMsgBase> msgList;
	List<Long> roomUsers;
	boolean setupFeed = false;
	int nMsgPos;
	
	List<OTMsgBase> newMsgs = new ArrayList<OTMsgBase>();
	Map<Long, Boolean> dupChecker = new HashMap<Long, Boolean>();
	
	boolean doSetupFeed(boolean enable){
		if(roomInfo == null) return false;
		if(enable){
			if(setupFeed == false){
				satelite.doSetMsgFeed(OTOApp.getInstance().getToken(), roomInfo.getRoom_id(), true);
				setupFeed = true;
			}
		}else{
			if(setupFeed){
				satelite.doSetMsgFeed(OTOApp.getInstance().getToken(), roomInfo.getRoom_id(), false);
				setupFeed = false;
			}
		}
		
		return true;
	}
	
	public void toLayout(){
		msgListAdapter = new MsgListAdapater(this, -1, elemLists);
		convListView.setAdapter(msgListAdapter);
		convListView.setSelector(android.R.color.transparent);
		OnLoadNextMsg();
		convListScrollToBottom();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		nMsgPos = -1;
		super.onCreate(savedInstanceState);
		if(finishGuard)return;
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				if(OTOApp.getInstance().isHasFriendActivity() == false){
					Intent acintent = new Intent(OTChatRoom.this, OTMain.class);
					startActivity(acintent);
				}
				finish();
			}
		});
		toLayout();
		OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, true);
		
		adminOnlyView.setVisibility(View.GONE);
		
		if(roomInfo != null){
			doSetupFeed(true);
		}
	}
	@Override
	protected void onDestroy() {
		if(roomInfo != null){
			doSetupFeed(false);
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 0:
			if(roomInfo != null){
				satelite.doExitRoom(OTOApp.getInstance().getToken(), roomInfo.getRoom_id());
			}else{
				finish();
			}
			break;
		}		
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		
		if(requestCode == OT_INVITE_USER){
			ArrayList<Long> users = (ArrayList<Long>)data.getSerializableExtra("invite_users");
			if(users != null){
				if(roomInfo != null){
					new TASatelite(new TADataHandler() {
						@Override public void onHttpPacketReceived(JSONObject data) {
							try{
								String state = data.getString("state");
								if(state.equals("ok")){
									JSONObject realData = data.getJSONObject("data");
									boolean low_version_user_exist = realData.getBoolean("low_version_user_exist");
									boolean deny_invitation = realData.getBoolean("deny_invitation");
									
									if(low_version_user_exist && deny_invitation){
										OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTChatRoom.this, getString(R.string.oto_invite_label_2),
												getString(R.string.oto_invite_low_version));
									}else if(deny_invitation){
										OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTChatRoom.this, getString(R.string.oto_invite_label_2),
												getString(R.string.oto_deny_inviation_msg));
									}else if(low_version_user_exist){
										OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTChatRoom.this, getString(R.string.oto_invite_label_2),
												getString(R.string.oto_invite_low_version));
									}
								}else if(state.equals("low_version_user_exist")){
									OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTChatRoom.this, getString(R.string.oto_invite_label_2),
										getString(R.string.oto_invite_low_version));
								}else if(state.equals("deny invitation")){
									OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTChatRoom.this, getString(R.string.oto_invite_label_2),
											getString(R.string.oto_deny_inviation_msg));
								}else if(state.equals("low_version and deny invitation")){
									OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTChatRoom.this, getString(R.string.oto_invite_label_2),
											getString(R.string.oto_low_version_and_deny_inviation_msg));
								}
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
						@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}
						@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
						@Override public void onTokenIsNotValid(JSONObject data) {}
						@Override public void onLimitMaxUser(JSONObject data) {}
					}).doInviteRoom(OTOApp.getInstance().getToken(), roomInfo.getRoom_id(), users);
				}else{
					if(users.size() != 0){
						users.addAll(preRoomInfo.userList);
						startActivity(new Intent(this, OTChatRoom.class).putExtra("user_list", users));
						finish();
					}
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		processNotReadMsgs();
		if(roomInfo != null){
			if(OTOApp.getInstance().getConvMgr().getLastNotifiedRoomId() == roomInfo.getRoom_id()){
				OTOApp.getInstance().getConvMgr().cancelMsgNotification();
			}
		}
	}

	public void applyRoomInfo(TARoomInfo roomInfo){
		OTOApp.getInstance().getConvMgr().setLastJoinedRoomId(roomInfo.getRoom_id());
		this.roomInfo = roomInfo;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean parseIntentExtra() {
		Intent intent = getIntent();
		roomInfo = null;
		
		Map<Long, TARoomInfo> roomMap = OTOApp.getInstance().getCacheCtrl().getRoomMap();
		Long room_id = intent.getLongExtra("room_id", -1L);
		if(room_id == -1L){
			ArrayList userList = intent.getParcelableArrayListExtra("user_list");
			preRoomInfo = new PreRoomInfo();
			preRoomInfo.room_id = -1L;
			preRoomInfo.userList = (ArrayList<Long>)userList;
			room_id = OTOApp.getInstance().getCacheCtrl().getPrivateRoomId(preRoomInfo.userList);
			setTalkConvTitle(preRoomInfo.userList);
			if(roomMap.containsKey(room_id)){
				applyRoomInfo(roomMap.get(room_id));
			}
		}else{
			if(roomMap.containsKey(room_id)){
				applyRoomInfo(roomMap.get(room_id));
			}else{
				preRoomInfo = new PreRoomInfo();
				preRoomInfo.room_id = room_id;
				preRoomInfo.userList = null;
			}
		}
		return true;
	}
	
	public void onMsgCountSetting(OTTalkMsgV2 msg, MsgHolder holder){
		if(msg.read_flag){
			holder.setStatus(MsgHolder.STATUS_READ, msg.getUnread_cnt());
		}else{
			holder.setStatus(MsgHolder.STATUS_NOT_READ, msg.getUnread_cnt());
		}
	}

	@Override
	public void setupConvView(ListElem elem, MsgHolder holder, final ArrayAdapter<?> adapter) {
		if(elem.elemType == ListElemType.MSG){
			OTTalkMsgV2 msg = null;
			if(elem.msg instanceof OTTalkMsgV2){
				msg = (OTTalkMsgV2) elem.msg;
			}
			if(msg == null) return;
			if(msg.isExitMsg()) return;
			if(msg.isEnterMsg()) return;
			
			if(msg.isSendMsg() == false){
				String userNick = TAUserNick.getInstance().getUserInfo(msg.getSender_id());
				holder.user_name.setText(userNick);
				final ImageView view = holder.getUser_img();
				UserImageUrlHelper.loadUserImage(msg.getSender_id(), new OnLoadUserImageUrl() {
					@Override
					public void onLoad(final long user_id, String url, boolean fromCache) {
						loadImageOnList(url, view, R.drawable.oto_friend_img_01, adapter, true, false);
					}
				});
			}
			
			holder.setMsg(msg.getMsg());
			holder.setTime(msg.getTime());
			
			boolean layoutFail = false;
			if(msg.getId() == -1L){
				holder.setStatus(MsgHolder.STATUS_SENDING, -1);
				if(pendingMsgMap.containsKey((long)msg.getTableIdx()) == false){
					layoutFail = true;
				}
			}else{
				onMsgCountSetting(msg, holder);
			}
			
			holder.setLayoutFail(layoutFail);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setupListElem(OTMsgBase msg, ListElem elem) {
		if(msg instanceof OTTalkMsgV2){
			OTTalkMsgV2 talkMsg = (OTTalkMsgV2) msg;
			String userNick = TAUserNick.getInstance().getUserInfo(msg.getSender_id());
			if(talkMsg.isExitMsg()){
				elem.elemType = ListElemType.INFO;
				elem.infoString = String.format(getString(R.string.oto_exit_chat_room_msg), userNick);
			}else if(talkMsg.isInviteMsg()){
				elem.elemType = ListElemType.INFO;
				JSONObject json = talkMsg.getInviteUsers();
				
				try{
					Iterator<String> iter = json.keys();
					if(json.length() == 1){
						String invite_nick = json.getString(iter.next());
						elem.infoString = String.format(getString(R.string.oto_invite_msg_single), userNick, invite_nick);
					}else{
						String invite_nick = json.getString(iter.next());
						elem.infoString = String.format(getString(R.string.oto_invite_msg), userNick, invite_nick, json.length() - 1);
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}else if(talkMsg.isEnterMsg()){
				elem.elemType = ListElemType.INFO;
				elem.infoString = String.format(getString(R.string.oto_enter_chat_room_msg), userNick);
			}else if(talkMsg.isChangeBjMsg()){
				elem.elemType = ListElemType.INFO;
				elem.infoString = String.format(getString(R.string.oto_change_moderator_msg), talkMsg.getChangedBjNickName());
			}else if(talkMsg.isRoomHiddenMsg()){
				elem.elemType = ListElemType.INFO;
				if(talkMsg.isHiddenFlag()){
					elem.infoString = getString(R.string.oto_room_private_msg);
				}else{
					elem.infoString = getString(R.string.oto_room_public_msg);
				}
			}else if(talkMsg.isKickMsg()){
				elem.elemType = ListElemType.INFO;
				JSONObject json = talkMsg.getKickUsers();
				try{
					Iterator<String> iter = json.keys();
					String kickNick = json.getString(iter.next());
					if(json.length() == 1){
						elem.infoString = String.format(getString(R.string.oto_kick_single_msg), kickNick);
					}else{
						elem.infoString = String.format(getString(R.string.oto_kick_multiple_msg),  kickNick, json.length() - 1);
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}else{
				elem.elemType = ListElemType.MSG;
			}
		}
	}

	@Override
	public void onMsgReceived(OTMsgBase msg) {
		if(msg instanceof OTTalkMsgV2){
			OTTalkMsgV2 talkMsg = (OTTalkMsgV2) msg;
			
			if(roomInfo != null){
				if(roomInfo.getRoom_id().longValue() != talkMsg.getRoom_id().longValue())
					return;
			}else{
				if(preRoomInfo.room_id != talkMsg.getRoom_id())
					return;
			}
			
			if(roomInfo == null){
				applyRoomInfo(OTOApp.getInstance().getCacheCtrl().getRoomMap().get(talkMsg.room_id));
			}
			
			addListElem(talkMsg, false);
			if(OTOApp.getInstance().getConvMgr().isRoomScreenOff() == false){
				processNotReadMsgs();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void processNotReadMsgs(){
		if(roomInfo == null) return;
		ArrayList<Long> notReadMsgs = new ArrayList<Long>();
		ArrayList<Long> readCheckMsgs = new ArrayList<Long>();
		List<OTTalkMsgV2> lists = (List<OTTalkMsgV2>)roomInfo.getMsgs().clone();
		for(OTTalkMsgV2 msg : lists){
			if(msg.getId() == -1L) continue;
			
			if(msg.isSendMsg() == false && msg.isRead_flag() == false){
				if(pendingNotMyMsgReadCheckMap.containsKey(msg.getId()) == false){
					pendingNotMyMsgReadCheckMap.put(msg.getId(), true);
					notReadMsgs.add(msg.getId());
				}
			}
			
			if(msg.getUnread_cnt() != 0L){
				if(pendingReadCheckMap.containsKey(msg.getId()) == false){
					pendingReadCheckMap.put(msg.getId(), true);
					readCheckMsgs.add(msg.getId());
				}
			}
		}
		
		if(notReadMsgs.size() != 0){
			satelite.doPostReadMsg(OTOApp.getInstance().getToken(), notReadMsgs);
		}
		
		if(readCheckMsgs.size() != 0){
			satelite.doGetReadMsg(OTOApp.getInstance().getToken(), readCheckMsgs);
		}
	}
	
	@Override
	public void onNotify(Notify packet) {
		if(packet.hasMsgRead()){
			processNotReadMsgs();
		}
		convListView.invalidateViews();
	}
	
	public void setTalkConvTitle(List<Long> users){
		roomUsers = users;
		boolean getNickName = false;
		for(Long user_id : users){	
			String userNick = TAUserNick.getInstance().getUserInfo(user_id);
			if(userNick.length() == 0){
				getNickName = true;
				break;
			}
		}
		if(getNickName){
			satelite.doGetUserInfos(OTOApp.getInstance().getToken(), users);
		}
		
		int userSize =users.size();
		if(userSize == 2){
			long user = -1L;
			if(users.get(0) == OTOApp.getInstance().getId())
				user = users.get(1);
			else
				user = users.get(0);
			String userNick = TAUserNick.getInstance().getUserInfo(user);
			titleView.setText(userNick);
		}else if(userSize > 2){
			titleView.setText(getString(R.string.oto_group_chat) + " (" + String.valueOf(userSize) + ")");
		}
	}
	
	public void applyNewMsgs(){
		if(newMsgs.size() == 0) return;
		Map<Long, Boolean> timeMap = new HashMap<Long, Boolean>();
		ArrayList<ListElem> removeList = new ArrayList<ListElem>();
		
		for(ListElem elem : elemLists){
			if(elem.elemType == ListElemType.MSG_DATE){
				removeList.add(elem);
			}
		}
		for(ListElem elem : removeList){
			elemLists.remove(elem);
		}
		
		for(OTMsgBase msg : newMsgs){
			if(msg.getId() == -1L){
				elemLists.add(new ListElem(msg));
			}else{
				if(dupChecker.containsKey(msg.getId()) == false){
					dupChecker.put(msg.getId(), true);
					elemLists.add(new ListElem(msg));
				}
			}
		}
		
		for(int i=0;i<elemLists.size();++i){
			OTMsgBase msg = elemLists.get(i).msg;
			String timeDate = getBaseDatePartOne(msg.getTime());
			long time = getCalcBaseTime(msg.getTime());
			if(timeMap.containsKey(time) == false){
				timeMap.put(time, true);
				elemLists.add(i, new ListElem(timeDate, ListElemType.MSG_DATE, time));
				++i;
			}
		}
		
		newMsgs.clear();
		msgListAdapter.notifyDataSetChanged();
		convListScrollToBottom();
	}
	
	@Override
	public void onListViewScrollBottom() {
		applyNewMsgs();
	}

	public void addListElem(OTTalkMsgV2 newMsg, boolean sendMessage) {
		if(elemLists.size() == 0){
			toLayout();
			return;
		}
		
		if(newMsg.isExitMsg() || newMsg.isInviteMsg() || newMsg.isEnterMsg() || newMsg.isHiddenFlag() || newMsg.isKickMsg() || newMsg.isRoomHiddenMsg()){
			if(roomInfo != null){
				setTalkConvTitle(roomInfo.getUsers());
			}
		}
		
		boolean isBottom = false;
		if(convListView.getChildCount() > 1 && convListView.getLastVisiblePosition() >= convListView.getAdapter().getCount() - 2 &&
				convListView.getChildAt(convListView.getChildCount() - 2).getBottom() <= convListView.getHeight()){
			isBottom = true;
		}
		
		newMsgs.add(newMsg);
		
		if(isBottom || sendMessage){
			applyNewMsgs();
		}else{
			String textMsg = null;
			if(newMsg.getMsg().length() != 0){
				textMsg = newMsg.getMsg();
			}else{
				if(newMsg.isImgMsg()){
					textMsg = getString(R.string.oto_picture);
				}
			}
			setupConvListAppendLayout(newMsg.getSender_id(), textMsg);			
		}
	}

	@Override
	public void OnDeleteMsg(OTMsgBase msg) {
		if(msg instanceof OTTalkMsgV2){
			OTOApp.getInstance().getCacheCtrl().removeMsg((OTTalkMsgV2)msg);
		}
	}

	@Override
	public boolean OnSendMsg(String msg) {
		sendMsg(msg);
		return true;
	}

	@Override
	public boolean OnSendImgMsg(List<String> imagePaths, String msg) {
		sendImgMsg(imagePaths, msg);
		return true;
	}
	
	@Override
	public boolean hasPendingMsg(OTMsgBase msg) {
		return pendingMsgMap.containsKey((long)msg.getTableIdx());
	}

	class optionButtonListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			int pos = (Integer) v.getTag();
			OnOptionButtionClicked(pos);
			switchOption(false);
		}
	}
	
	public void OnOptionButtionClicked(int idx){
		switch(idx){
		case 0:
			doTakePhotoAction();
			break;
		case 1:
			if(imageBarView.getChildCount() < 10){
				doTakeAlbumAction();
			}else{
				OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTChatRoom.this);
			}
			break;
		case 2:
			if(imageBarView.getChildCount() < 10){
				doTakeAlbumActionMultiple(imageBarView.getChildCount());
			}else{
				OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTChatRoom.this);
			}
			break;
		case 3:
			if(roomInfo != null){
				startActivityForResult(
						new Intent(OTChatRoom.this, OTMakeChat.class).putExtra("invite", true).putExtra("users", roomInfo.getUsers()),
						OT_INVITE_USER);
			}else{
				if(preRoomInfo.userList != null){
					startActivityForResult(
							new Intent(OTChatRoom.this, OTMakeChat.class).putExtra("invite", true).putExtra("users", preRoomInfo.userList),
							OT_INVITE_USER);
				}
			}
			break;
		case 4:
			if(roomInfo != null){
				OTOApp.getInstance().getCacheCtrl().switchTalkAlarm(roomInfo.getRoom_id());
			}
			break;
		case 5:
			if(roomInfo != null){
				OTOApp.getInstance().getDialogMaker().makeYesNoDialog(getString(R.string.oto_lib_name),
					getString(R.string.oto_exit_room_caution), new PLDialogListener() {
						@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) { }
						@Override public void onDialogSelectedWithData(int dialogId, int pos, Object data) {}
						@Override
						public void onDialogSelected(int dialogId, int pos) {
							if(pos == DialogInterface.BUTTON_POSITIVE){
								satelite.doExitRoom(OTOApp.getInstance().getToken(), roomInfo.getRoom_id());
							}
						}
					}, OTChatRoom.this, 0);
			}else{
				finish();
			}
			break;
		}
	}
	public void OnSetupOptionButton(int idx, ImageView image, TextView text){
		switch(idx){
		case 0:
			image.setImageResource(R.drawable.oto_conv_camera);
			text.setText(getString(R.string.oto_context_menu_1));
			break;
		case 1:
			image.setImageResource(R.drawable.oto_conv_image);
			text.setText(getString(R.string.oto_context_menu_2));
			break;
		case 2:
			image.setImageResource(R.drawable.oto_conv_image_many);
			text.setText(getString(R.string.oto_context_menu_3));
			break;
		case 3:
			image.setImageResource(R.drawable.oto_conv_invite);
			text.setText(getString(R.string.oto_context_menu_4));
			break;
		case 4:
			if(roomInfo != null){
				if(OTOApp.getInstance().getCacheCtrl().getTalkAlarm(roomInfo.getRoom_id())){
					image.setImageResource(R.drawable.oto_conv_alaram_off);
					text.setText(getString(R.string.oto_context_menu_5_sub));
				}else{
					image.setImageResource(R.drawable.oto_conv_alaram);
					text.setText(getString(R.string.oto_context_menu_5));
				}
			}else{
				image.setImageResource(R.drawable.oto_conv_alaram_off);
				text.setText(getString(R.string.oto_context_menu_5_sub));
			}
			break;
		case 5:
			image.setImageResource(R.drawable.oto_conv_exit);
			text.setText(getString(R.string.oto_context_menu_6));
			break;
		case 6:
			break;
		case 7:
			break;
		}
	}

	@Override
	public boolean OnMakeOptionButton(ViewGroup contextMenuLayout) {
		LayoutInflater li = getLayoutInflater();
		ViewGroup topLayout = (ViewGroup) findViewById(R.id.oto_conv_detail_context_menu_top);
		ViewGroup bottomLayout = (ViewGroup) findViewById(R.id.oto_conv_detail_context_menu_bottom);
		topLayout.removeAllViews();
		bottomLayout.removeAllViews();
		OnClickListener listener = new optionButtonListener();
		for(int i=0;i<8;++i){
			View btnLayout = li.inflate(R.layout.ot_conv_inside_context_button, null);
			ImageView image = (ImageView)btnLayout.findViewById(R.id.oto_context_btn_image);
			TextView text = (TextView)btnLayout.findViewById(R.id.oto_context_btn_text);
			btnLayout.setTag(i);
			btnLayout.setOnClickListener(listener);
			OnSetupOptionButton(i,image,text);
			
			ViewGroup selLayout = null;
			if(i<4)selLayout = topLayout;
			else selLayout = bottomLayout;
			selLayout.addView(btnLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, 1.0f));
			if((i+1)%4 != 0){
				View line = new View(this);//#9b9c9d
				line.setBackgroundColor(Color.rgb(0x9b, 0x9c, 0x9d));
				selLayout.addView(line, new LinearLayout.LayoutParams(1, LayoutParams.MATCH_PARENT));
			}
		}
		return true;
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		Map<Long, OTTalkMsgV2> msgMap = OTOApp.getInstance().getCacheCtrl().getMsgMap();
		try {
			String location = data.getString("location");
			String state = data.getString("state");
			if(location.equals("getuserinfos")){
				JSONArray arrData = data.getJSONArray("data");
				if(OTOApp.getInstance().getDB().beginTransaction()){
					for(int i=0;i<arrData.length();++i){
						JSONObject userData = arrData.getJSONObject(i);
						TASateliteDispatcher.dispatchUserInfo(userData, true);
					}
					OTOApp.getInstance().getDB().endTransaction();
				}
				if(roomUsers != null){
					setTalkConvTitle(roomUsers);
				}
			}else if(location.equals("setreceivedmsgstate")){
				if(state.equals("ok")){
					JSONArray arr = data.getJSONArray("data");
					OTOApp.getInstance().getDB().beginTransaction();
					
					for(int i=0;i<arr.length();i+=2){
						Long msg_id = arr.getLong(i);
						pendingNotMyMsgReadCheckMap.remove(msg_id);
						boolean suc = arr.getBoolean(i+1);
						OTTalkMsgV2 msg = msgMap.get(msg_id);
						if(msg != null){
							if(suc){
								msg.read_flag = true;
								OTOApp.getInstance().getCacheCtrl().updateWIthBeginTransact(msg);
							}
						}
					}
					OTOApp.getInstance().getDB().endTransaction();
				}
			}else if(location.equals("getsendedmsgstate")){
				if(state.equals("ok")){
					JSONArray arr = data.getJSONArray("data");
					OTOApp.getInstance().getDB().beginTransaction();
					for(int i=0;i<arr.length();i+=2){
						Long msg_id = arr.getLong(i);
						pendingReadCheckMap.remove(msg_id);
						int count = arr.getInt(i+1);
						OTTalkMsgV2 msg = msgMap.get(msg_id);
						if(msg != null){
							if(msg.getUnread_cnt() > (long)count){
								msg.setUnread_cnt((long)count);
							}
							OTOApp.getInstance().getCacheCtrl().updateWIthBeginTransact(msg);
						}
					}
					OTOApp.getInstance().getDB().endTransaction();
					convListView.invalidateViews();
				}
			}else if(location.equals("sendmessage")){
				JSONObject realData = data.getJSONObject("data");
				Long transact_id = realData.getLong("transact_id");
				if(state.equals("ok")){
					Long msg_id = realData.getLong("msg_id");
					Long send_time = realData.getLong("send_time");
					OTTalkMsgV2 msg = OTOApp.getInstance().getCacheCtrl().getTableIDMsgMap().get(transact_id);
					msg.setId(msg_id);
					msg.setTime(send_time);
					OTOApp.getInstance().getCacheCtrl().updateMsg(msg);
				}
				
				if(pendingMsgMap.containsKey(transact_id)){
					pendingMsgMap.remove(transact_id);
				}
				msgListAdapter.notifyDataSetChanged();
				convListScrollToBottom();
			}else if(location.equals("sendimgmsg")){
				JSONObject realData = data.getJSONObject("data");
				Long transact_id = realData.getLong("transact_id");
				OTTalkMsgV2 msg = OTOApp.getInstance().getCacheCtrl().getTableIDMsgMap().get(transact_id);
				if(state.equals("ok")){
					Long msg_id = realData.getLong("msg_id");
					JSONArray img_url = realData.getJSONArray("img_url");
					Long send_time = realData.getLong("send_time");
					
					msg.setId(msg_id);
					msg.setImg_url(img_url);
					msg.setTime(send_time);
					
					OTOApp.getInstance().getCacheCtrl().updateMsg(msg);
				}
				
				if(pendingMsgMap.containsKey(transact_id)){
					pendingMsgMap.remove(transact_id);
				}
				msgListAdapter.notifyDataSetChanged();
				convListScrollToBottom();
			}else if(location.equals("getroomusers") || location.equals("getormakeroom")){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					Long room_id = realData.getLong("room_id");
					JSONArray arr = realData.getJSONArray("users");
					boolean public_room = realData.getBoolean("public_room");
					boolean deny_invitation = realData.getBoolean("deny_invitation");
					
					if(deny_invitation){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_deny_inviation_msg), this);
					}
					
					ArrayList<Long> users = new ArrayList<Long>();
					for(int i=0;i<arr.length();++i){
						users.add(arr.getLong(i));
					}
					OTOApp.getInstance().getCacheCtrl().addRoomUsers(room_id, users, public_room);
					applyRoomInfo(OTOApp.getInstance().getCacheCtrl().getRoomMap().get(room_id));
					OTOApp.getInstance().getUIMgr().dismissDialogProgress();
					
					if(lazyWorkQueue.isEmpty() == false){
						lazyWorkQueue.poll().run();
						doSetupFeed(true);
					}
				}else{
					if(state.equals("deny invitation")){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_deny_inviation_msg), this);
					}else{
						OTOApp.getInstance().getUIMgr().showToast(state, this);
					}
					lazyWorkQueue.clear();
					finishGuard = true;
					finish();
				}
			}else if(location.equals("exitroom")){
				if(state.equals("ok")){
					OTOApp.getInstance().getCacheCtrl().removeRoom(roomInfo);
					finish();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onTokenIsNotValid(JSONObject data) {
		if(finishGuard) return;
		finish();
		finishGuard = true;
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		if(finishGuard) return;
		startActivity(new Intent(this, OTLimit.class));
		finish();
		finishGuard = true;
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		if(addr.equals(TASatelite.SEND_MSG_URL)){
			try {
				Long transact_id = data.getLong("transact_id");
				if(pendingMsgMap.containsKey(transact_id)){
					pendingMsgMap.remove(transact_id);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(addr.equals(TASatelite.GET_ROOM_USERS_URL) || addr.equals(TASatelite.GET_OR_MAKE_ROOM_URL)){
			lazyWorkQueue.clear();
		}
		convListView.invalidateViews();
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		if(addr.equals(TASatelite.SEND_IMG_MSG_URL)){
			TAImgMsgData castData = (TAImgMsgData)data;
			Long transact_id = Long.valueOf(castData.getTransact_id());
			if(pendingMsgMap.containsKey(transact_id)){
				pendingMsgMap.remove(transact_id);
			}
		}
		convListView.invalidateViews();
	}

	@Override
	public int OnLoadNextMsg() {
		return loadNextMsg();
	}

	@Override
	public void OnReSendButtonPressed(long transact_id) {
		OTMsgBase msg = OTOApp.getInstance().getCacheCtrl().getTableIDMsgMap().get(transact_id);
		removeListElem(msg);
		if(msg.isImgMsg()){
			sendImgMsg(msg.getPreSendImg_url(), msg.getMsg());
		}else{
			sendMsg(msg.getMsg());
		}
	}

	@Override
	public void OnDelMsgButtonPressed(long transact_id) {
		OTMsgBase msg = OTOApp.getInstance().getCacheCtrl().getTableIDMsgMap().get(transact_id);
		removeListElem(msg);
	}

	@Override
	public void OnUserPressed(long user_id) {
		Intent intent = new Intent(OTChatRoom.this, OTFriendPopup.class);
		Bundle bundle = new Bundle();
		bundle.putLong("user_id", user_id);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@SuppressWarnings("unchecked")
	public int loadNextMsg(){
		if(roomInfo == null) return -1;
		Map<Long, Boolean> timeMap = new HashMap<Long, Boolean>();
		
		setTalkConvTitle(roomInfo.getUsers());
		msgList = (List<OTMsgBase>)roomInfo.getMsgs().clone();
		if(nMsgPos == -1) nMsgPos = msgList.size() - 1;
		elemLists.clear();
		dupChecker.clear();
		
		int nextScrollPos = nMsgPos - Math.max(nMsgPos - 20, 0);
		nMsgPos = Math.max(nMsgPos - 20, 0);
		for(int i=nMsgPos;i<msgList.size();++i){
			OTMsgBase msg = msgList.get(i);
			String timeDate = getBaseDatePartOne(msg.getTime());
			long time = getCalcBaseTime(msg.getTime());
			if(timeMap.containsKey(time) == false){
				timeMap.put(time, true);
				elemLists.add(new ListElem(timeDate, ListElemType.MSG_DATE, time));
			}
			if(msg.getId() == -1L){
				elemLists.add(new ListElem(msg));
			}else{
				if(dupChecker.containsKey(msg.getId()) == false){
					dupChecker.put(msg.getId(), true);
					elemLists.add(new ListElem(msg));
				}
			}
		}
		msgListAdapter.notifyDataSetChanged();
		//convListView.invalidateViews();
		return nextScrollPos;
	}
	
	void sendImgMsg(JSONArray imagePaths, String textMsg) {
		if(imagePaths == null) return;
		List<String> listImagePath = new ArrayList<String>();
		for(int i=0;i<imagePaths.length();++i){
			try{
				String path = imagePaths.getString(i);
				listImagePath.add(path);
			}catch(JSONException ex){}
		}
		sendImgMsg(listImagePath, textMsg);
	}
	
	public void sendImgMsg(final List<String> imagePaths, final String msg){
		_sendMsgPreWorkChecker(new Runnable() {
			@Override
			public void run() {
				_sendImgMsg(imagePaths, msg);
			}
		});
	}
	
	public void sendMsg(final String textMsg){
		_sendMsgPreWorkChecker(new Runnable() {
			@Override
			public void run() {
				_sendMsg(textMsg);
			}
		});
	}
	
	void _sendMsgPreWorkChecker(Runnable task){
		boolean raiseException = false;
		boolean preTaskExist = false;
		if(roomInfo == null && preRoomInfo != null){
			if(preRoomInfo.room_id != -1L){
				Map<Long, TARoomInfo> roomMap = OTOApp.getInstance().getCacheCtrl().getRoomMap();
				if(roomMap.containsKey(preRoomInfo.room_id)){
					applyRoomInfo(roomMap.get(preRoomInfo.room_id));
					if(roomInfo.getUsers().size() == 0){
						OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTChatRoom.this);
						satelite.doGetRoomUsers(OTOApp.getInstance().getToken(), preRoomInfo.room_id);
						preTaskExist = true;
					}
				}else{
					raiseException = true;
				}
			}
			
			if(preRoomInfo.userList != null){
				OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTChatRoom.this);
				satelite.doGetOrMakeRoom(OTOApp.getInstance().getToken(), preRoomInfo.userList);
				preTaskExist = true;
			}
		}else if(roomInfo == null && preRoomInfo == null){
			raiseException = true;
		}
		
		if(raiseException){
			try {
				throw new Exception("sendMsg Failed due to initialize failed");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		if(preTaskExist == false){
			task.run();
		}else{
			lazyWorkQueue.add(task);
		}		
	}
	
	void _sendMsg(){
		String body = sendBodyView.getEditableText().toString();
		if(body.length() == 0) return;
		sendBodyView.getText().clear();
		_sendMsg(body);
	}
	
	protected OTTalkMsgV2 makeSendMessage(String textMsg){
		Long myID = OTOApp.getInstance().getId();
		
		OTTalkMsgV2 msg = new OTTalkMsgV2();
		msg.setMsg(textMsg);
		msg.setId(-1L);		
		msg.setSender_id(myID);
		msg.setRoom_id(roomInfo.getRoom_id());
		msg.setTime(System.currentTimeMillis());
		msg.setRead_flag(false);
		msg.setImgMsg(false);
		msg.setSendMsg(true);
		msg.setUnread_cnt((long)roomInfo.getUsers().size() - 1);
		
		return msg;
	}
	
	protected OTTalkMsgV2 makeSendImgMessage(List<String> imagePaths, String textMsg){
		long myID = OTOApp.getInstance().getPref().getUser_id().getValue();
		
		OTTalkMsgV2 msg = new OTTalkMsgV2();
		msg.setMsg(getString(R.string.oto_picture));
		msg.setId(-1L);
		msg.setSender_id(myID);
		msg.setRoom_id(roomInfo.getRoom_id());
		msg.setTime(System.currentTimeMillis());
		msg.setRead_flag(false);
		msg.setImgMsg(true);
		msg.setSendMsg(true);
		msg.setUnread_cnt((long)roomInfo.getUsers().size() - 1);
		msg.setMsg(textMsg);
		
		JSONArray imgUrl = new JSONArray();
		for(String path : imagePaths){
			imgUrl.put(path);
		}
		msg.setPreSendImg_url(imgUrl);
		return msg;
	}
	
	void _sendMsg(String textMsg){
		String token = OTOApp.getInstance().getToken();
		OTTalkMsgV2 msg = makeSendMessage(textMsg);
		
		OTOApp.getInstance().getCacheCtrl().addUnSendMsg(msg);
		pendingMsgMap.put((long)msg.getTableIdx(), msg);
		addListElem(msg, true);
		
		satelite.doSendMessage(token, roomInfo.getRoom_id(), textMsg, (long)msg.getTableIdx());
	}
	
	 void _sendImgMsg(List<String> imagePaths, String textMsg){
		String token = OTOApp.getInstance().getToken();
		long myID = OTOApp.getInstance().getPref().getUser_id().getValue();
		
		OTTalkMsgV2 msg = makeSendImgMessage(imagePaths, textMsg);
		
		OTOApp.getInstance().getCacheCtrl().addUnSendMsg(msg);
		pendingMsgMap.put((long)msg.getTableIdx(), msg);
		addListElem(msg, true);
		
		TAImgMsgData data = new TAImgMsgData();
		data.setToken(token);
		data.setTransact_id(String.valueOf(msg.getTableIdx()));
		data.setImagePaths(imagePaths);
		data.setRoom_id(roomInfo.getRoom_id());
		data.setSender_id(myID);
		data.setMsg(textMsg);
		
		satelite.doSendImgMsg(data);		
	}
}
