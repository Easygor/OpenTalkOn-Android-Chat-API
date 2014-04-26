package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify_Community_CommentCount;
import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify_Community_LikeCount;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.CommunityData;
import com.thinkspace.opentalkon.data.CommunityLastTimeTable;
import com.thinkspace.opentalkon.data.OTComMsg;
import com.thinkspace.opentalkon.data.OTImgPostData;
import com.thinkspace.opentalkon.data.OTMsgBase;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;

public class OTOpenTalkRoom extends OTRoomBase {
	public final static int OT_CHECK_IS_DELETED = 100;
	public final static int OT_GET_WRITE_INFO = 200;
	long community_id = -1L;
	CommunityData data;
	
	AtomicLong noneLastId = new AtomicLong(-1L);
	AtomicBoolean noneAllLoaded = new AtomicBoolean(false);
	boolean noneLoading = false;
	
	boolean bestLoading = false;
	int bestStage;
	
	AtomicLong searchLastId = new AtomicLong(-1L);
	AtomicBoolean searchAllLoaded = new AtomicBoolean(false);
	boolean searchLoading = false;
	String searchNowBody;
	SearchCondition searchConditionValue;
	int mySearchStage;
	boolean authority;
	
	UpperBodyStage lastUpperBodyStage;
	BottomBodyStage lastBottomBodyStage;
	
	MsgListAdapater msgListAdapterToSearchList;
	protected List<ListElem> searchElemList = new ArrayList<OTRoomBase.ListElem>();
	
	Map<Long, OTMsgBase> pendingLikeMap = new HashMap<Long, OTMsgBase>();
	Map<Long, OTMsgBase> pendingMsgMap = new HashMap<Long, OTMsgBase>();
	Map<Long, OTMsgBase> saveSendingMsgMap = new HashMap<Long, OTMsgBase>();
	int pendingTransactId = 0; 
	
	synchronized int getNextTransactId(){
		return pendingTransactId++;
	}
	
	boolean hasWriteAuthority;
	
	class optionButtonListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(data == null) return;
			int pos = (Integer) v.getTag();
			switch(pos){
			case 0:
				if(hasWriteAuthority){
					if(data.write_method_chat == false){
						Intent intent = new Intent(OTOpenTalkRoom.this, OTWritePost.class);
						intent.putExtra("community_data", data);
						intent.putExtra("startAction", 0);
						startActivityForResult(intent, OT_GET_WRITE_INFO);
					}else{
						if(imageBarView.getChildCount() < 10){
							doTakePhotoAction();
						}else{
							OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTOpenTalkRoom.this);
						}
					}
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_not_authorize), OTOpenTalkRoom.this);
				}
				break;
			case 1:
				if(hasWriteAuthority){
					if(data.write_method_chat == false){
						Intent intent = new Intent(OTOpenTalkRoom.this, OTWritePost.class);
						intent.putExtra("community_data", data);
						intent.putExtra("startAction", 1);
						startActivityForResult(intent, OT_GET_WRITE_INFO);
					}else{
						if(imageBarView.getChildCount() < 10){
							doTakeAlbumAction();
						}else{
							OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTOpenTalkRoom.this);
						}
					}
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_not_authorize), OTOpenTalkRoom.this);
				}
				break;
			case 2:
				if(hasWriteAuthority){
					if(data.write_method_chat == false){
						Intent intent = new Intent(OTOpenTalkRoom.this, OTWritePost.class);
						intent.putExtra("community_data", data);
						intent.putExtra("startAction", 2);
						startActivityForResult(intent, OT_GET_WRITE_INFO);
					}else{
						if(imageBarView.getChildCount() < 10){
							doTakeAlbumActionMultiple(imageBarView.getChildCount());
						}else{
							OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTOpenTalkRoom.this);
						}
					}
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_not_authorize), OTOpenTalkRoom.this);
				}
				break;
			case 3:
				doSetSearchLayout(SearchCondition.WORD, "");
				break;
			case 4:
				doSetMySearchLayout(0);
				break;
			case 5:
				switchAlarm();
				break;
			case 6:
				doSetBestLayout(BestCondition.WEEK);
				break;
			case 7:
				finish();
				break;
			}
			switchOption(false);
		}
	}
	
	@Override
	public void onListViewScrollBottom() {
		// TODO Auto-generated method stub
		
	}

	public void switchAlarm(){
		//OTOApp.getInstance().getCacheCtrl().switchCommunityAlarm(data.id);
		new TASatelite(new TADataHandler() {
			@Override public void onHttpPacketReceived(JSONObject d) {
				try {
					String state = d.getString("state");
					if(state.equals("ok")){
						data.alarm = !data.alarm;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override public void onTokenIsNotValid(JSONObject data) {}
			@Override public void onLimitMaxUser(JSONObject data) {}
			@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}
			@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
		}).doSetCommunityAlarm(OTOApp.getInstance().getToken(), data.id, !data.alarm);
	}
	
	@Override
	public void onBackPressed() {
		if(lastBottomBodyStage != BottomBodyStage.CONV){
			doSetListUpper(UpperBodyStage.CONV_LIST);
			doSetListBottom(BottomBodyStage.CONV);
			return;
		}
		if(data != null){
			OTOApp.getInstance().getConvMgr().exitCommunity(data.id);
		}
		super.onBackPressed();
	}
	
	public static enum BestCondition{
		DAY,
		WEEK,
		MONTH
	}
	
	public static enum SearchCondition{
		NICK_NAME,
		WORD
	}
	
	public static enum UpperBodyStage{
		PROGRESS,
		CONV_LIST,
		SEARCH_LIST,
		SEARCH_EMPTY
	}
	public static enum BottomBodyStage{
		CONV,
		SEARCH,
		MY_SEARCH,
		BEST
	}
	
	void doSetListBottom(BottomBodyStage stage){
		switch(stage){
		case CONV:
			searchLayout.setVisibility(View.GONE);
			sendLayout.setVisibility(View.VISIBLE);
			mySearchLayout.setVisibility(View.GONE);
			bestLayout.setVisibility(View.GONE);
			break;
		case SEARCH:
			searchLayout.setVisibility(View.VISIBLE);
			sendLayout.setVisibility(View.GONE);
			mySearchLayout.setVisibility(View.GONE);
			bestLayout.setVisibility(View.GONE);
			break;
		case MY_SEARCH:
			searchLayout.setVisibility(View.GONE);
			sendLayout.setVisibility(View.GONE);
			mySearchLayout.setVisibility(View.VISIBLE);
			bestLayout.setVisibility(View.GONE);
			break;
		case BEST:
			searchLayout.setVisibility(View.GONE);
			sendLayout.setVisibility(View.GONE);
			mySearchLayout.setVisibility(View.GONE);
			bestLayout.setVisibility(View.VISIBLE);
			break;
		}
		lastBottomBodyStage = stage;
	}
	
	void doSetListUpper(UpperBodyStage stage){
		switch(stage){
		case PROGRESS:
			convListViewLayout.setVisibility(View.GONE);
			searchListView.setVisibility(View.GONE);
			progressView.setVisibility(View.VISIBLE);
			convEmptyView.setVisibility(View.GONE);
			break;
		case CONV_LIST:
			convListViewLayout.setVisibility(View.VISIBLE);
			searchListView.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);
			convEmptyView.setVisibility(View.GONE);
			break;
		case SEARCH_LIST:
			convListViewLayout.setVisibility(View.GONE);
			searchListView.setVisibility(View.VISIBLE);
			progressView.setVisibility(View.GONE);
			convEmptyView.setVisibility(View.GONE);
			break;
		case SEARCH_EMPTY:
			convListViewLayout.setVisibility(View.GONE);
			searchListView.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);
			convEmptyView.setVisibility(View.VISIBLE);
			break;
		}
		lastUpperBodyStage = stage;
	}
	
	void doSetSearchConditon(SearchCondition condition){
		searchConditionValue = condition;
		if(condition == SearchCondition.NICK_NAME){
			searchCondition.setText(getString(R.string.oto_nick_name));
		}else{
			searchCondition.setText(getString(R.string.oto_word));
		}
	}

	void doSetSearchLayout(SearchCondition condition, String body){
		doSetSearchConditon(condition);
		searchBody.setText(body);
		
		doSetListBottom(BottomBodyStage.SEARCH);
	}
	
	void doSetBestLayout(BestCondition condition){
		doSetListBottom(BottomBodyStage.BEST);
		doSetListUpper(UpperBodyStage.PROGRESS);
		
		bestLoading = true;
		bestStage = condition.ordinal();
		
		switch(bestStage){
		case 0:
			break;
		case 1:
			bestMonthBtn.setBackgroundResource(R.drawable.oto_button_gray);
			bestWeekBtn.setBackgroundResource(R.drawable.oto_button_gray_pressed);
			satelite.doGetCommunityBestWeek(OTOApp.getInstance().getToken(), data.id);
			break;
		case 2:
			bestWeekBtn.setBackgroundResource(R.drawable.oto_button_gray);
			bestMonthBtn.setBackgroundResource(R.drawable.oto_button_gray_pressed);
			satelite.doGetCommunityBestMonth(OTOApp.getInstance().getToken(), data.id);
			break;
		}
	}
	
	void doSetMySearchLayout(int stage){
		doSetListBottom(BottomBodyStage.MY_SEARCH);
		doSetListUpper(UpperBodyStage.PROGRESS);
		
		searchLastId.set(-1L);
		searchAllLoaded.set(false);
		searchLoading = true;
		mySearchStage = stage;
		
		switch(stage){
		case 0:
			searchNowBody = OTOApp.getInstance().getPref().getNickName().getValue();
			mySearchWordBtn.setBackgroundResource(R.drawable.oto_button_gray_pressed);
			mySearchReplyBtn.setBackgroundResource(R.drawable.oto_button_gray);
			satelite.doGetCommunityPostsWithNickName(OTOApp.getInstance().getToken(), data.id, searchLastId.get(), searchNowBody);
			break;
		case 1:
			mySearchWordBtn.setBackgroundResource(R.drawable.oto_button_gray);
			mySearchReplyBtn.setBackgroundResource(R.drawable.oto_button_gray_pressed);
			satelite.doGetCommunityMyReply(OTOApp.getInstance().getToken(), data.id, searchLastId.get());
			break;
		}
	}
	
	@Override
	public void OnLikeClick(OTMsgBase msg) {
		if(pendingLikeMap.containsKey(msg.getId()) == false){
			pendingLikeMap.put(msg.getId(), msg);
			satelite.doLikeCommunity(OTOApp.getInstance().getToken(), msg.getId());
		}
	}

	@Override
	public void OnCommentClick(OTMsgBase msg) {
		Intent intent = new Intent(this, OTOpenTalkDetail.class);
		intent.putExtra("post_id", msg.getId());
		intent.putExtra("click_reply", true);
		intent.putExtra("authority", authority);
		startActivityForResult(intent, OT_CHECK_IS_DELETED);
	}

	@Override
	public void OnBodyClick(OTMsgBase msg) {
		if(msg != null && msg.getId() != -1L){
			Intent intent = new Intent(this, OTOpenTalkDetail.class);
			intent.putExtra("post_id", msg.getId());
			intent.putExtra("authority", authority);
			startActivityForResult(intent, OT_CHECK_IS_DELETED);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == OT_CHECK_IS_DELETED){
			if(data != null && data.hasExtra("post_id")){
				long post_id = data.getLongExtra("post_id", -1L);
				removeListElem(post_id);
			}
		}else if(requestCode == OT_GET_WRITE_INFO){
			if(data != null && data.hasExtra("body")){
				String body = data.getStringExtra("body");
				ArrayList<String> imagePaths = data.getStringArrayListExtra("imagePaths");
				if(imagePaths == null){
					OnSendMsg(body);
				}else{
					OnSendImgMsg(imagePaths, body);
				}
			}
		}
	}

	@Override
	public boolean OnMakeOptionButton(ViewGroup contextMenuLayout) {
		if(authority == false){
			return false;
		}
		if(data == null){
			return false;
		}
		LayoutInflater li = getLayoutInflater();
		ViewGroup topLayout = (ViewGroup) findViewById(R.id.oto_conv_detail_context_menu_top);
		ViewGroup bottomLayout = (ViewGroup) findViewById(R.id.oto_conv_detail_context_menu_bottom);
		OnClickListener listener = new optionButtonListener();
		
		topLayout.removeAllViews();
		bottomLayout.removeAllViews();
		
		for(int i=0;i<8;++i){
			View btnLayout = li.inflate(R.layout.ot_conv_inside_context_button, null);
			ImageView image = (ImageView)btnLayout.findViewById(R.id.oto_context_btn_image);
			TextView text = (TextView)btnLayout.findViewById(R.id.oto_context_btn_text);
			btnLayout.setTag(i);
			btnLayout.setOnClickListener(listener);
			switch(i){
			case 0:
				image.setImageResource(R.drawable.oto_conv_camera);
				text.setText(getString(R.string.oto_context_menu_com_1));
				break;
			case 1:
				image.setImageResource(R.drawable.oto_conv_image);
				text.setText(getString(R.string.oto_context_menu_com_2));
				break;
			case 2:
				image.setImageResource(R.drawable.oto_conv_image_many);
				text.setText(getString(R.string.oto_context_menu_com_3));
				break;
			case 3:
				image.setImageResource(R.drawable.oto_conv_search);
				text.setText(getString(R.string.oto_context_menu_com_4));
				break;
			case 4:
				image.setImageResource(R.drawable.oto_conv_word);
				text.setText(getString(R.string.oto_context_menu_com_5));
				break;
			case 5:
				if(data.alarm){
					image.setImageResource(R.drawable.oto_conv_alaram_off);
					text.setText(getString(R.string.oto_context_menu_com_6_sub));
				}else{
					image.setImageResource(R.drawable.oto_conv_alaram);
					text.setText(getString(R.string.oto_context_menu_com_6));
				}
				break;
			case 6:
				image.setImageResource(R.drawable.oto_conv_like);
				text.setText(getString(R.string.oto_context_menu_com_7));
				break;
			case 7:
				image.setImageResource(R.drawable.oto_conv_exit);
				text.setText(getString(R.string.oto_context_menu_com_8));
				break;
			}
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
	public boolean parseIntentExtra() {
		Intent intent = getIntent();
		if(intent == null) return false;
		data = intent.getParcelableExtra("community_data");
		if(data == null){
			community_id = intent.getLongExtra("community_id", -1L);
		}
		if(data == null && community_id == -1L)return false;
		
		authority = intent.getBooleanExtra("authority", true);
		
		return true;
	}
	
	@Override
	protected void onDestroy() {
		if(data != null){
			satelite.doSetCommunityFeed(OTOApp.getInstance().getToken(), data.id, false);
		}
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(finishGuard)return;
		if(data == null && community_id == -1L) return;
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				if(OTOApp.getInstance().isHasFriendActivity() == false){
					Intent acintent = new Intent(OTOpenTalkRoom.this, OTMain.class);
					startActivity(acintent);
				}
				if(data != null){
					OTOApp.getInstance().getConvMgr().exitCommunity(data.id);
				}
				finish();
			}
		});
		
		findViewById(R.id.oto_conv_detail_send_btn_notice).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(OTOpenTalkRoom.this, OTWritePost.class);
				intent.putExtra("community_data", data);
				startActivityForResult(intent, OT_GET_WRITE_INFO);
			}
		});
		
		doSetListUpper(UpperBodyStage.CONV_LIST);
		doSetListBottom(BottomBodyStage.CONV);
		if(data != null){
			setupComConvs();
		}else{
			satelite.doGetCommunityDetail(OTOApp.getInstance().getToken(), community_id);
		}
	}
	
	public void setupComConvs(){
		OTOApp.getInstance().getConvMgr().joinCommunity(data.id);
		OnLoadNextMsg();
		
		if(data.write_method_chat == false){
			applyNoticeLayout();
		}
		
		if(data.admin_write_only){
			adminOnlyView.setVisibility(View.VISIBLE);
			if(data.is_admin == false){
				sendBtnView.setVisibility(View.INVISIBLE);
				sendBodyView.setVisibility(View.INVISIBLE);
				setVisibilitySendNoticeButton(View.INVISIBLE);
			}else{
				sendBtnView.setVisibility(View.VISIBLE);
				sendBodyView.setVisibility(View.VISIBLE);
				setVisibilitySendNoticeButton(View.VISIBLE);
				hasWriteAuthority = true;
			}
		}else{
			adminOnlyView.setVisibility(View.GONE);
			hasWriteAuthority = true;
		}
		
		if(data.is_ban){
			sendBtnView.setVisibility(View.INVISIBLE);
			sendBodyView.setVisibility(View.INVISIBLE);
			hasWriteAuthority = false;
		}
		
		if(authority == false){
			hasWriteAuthority = false;
			sendLayout.setVisibility(View.GONE);
		}
		
		if(OTOApp.getInstance().getDB().beginTransaction()){
			CommunityLastTimeTable.getInstance().insertWithBeginTransaction(data.id, data.last_time);
			OTOApp.getInstance().getDB().endTransaction();
		}
		
		if(OTOApp.getInstance().getConvMgr().getLastNotifiedCommunityId() == data.id){
			OTOApp.getInstance().getConvMgr().cancelCommunityNotification();
		}
		titleView.setText(data.title);
		satelite.doSetCommunityFeed(OTOApp.getInstance().getToken(), data.id, true);
		
		bestClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				doSetListUpper(UpperBodyStage.CONV_LIST);
				doSetListBottom(BottomBodyStage.CONV);
			}
		});
		
		bestWeekBtn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				doSetBestLayout(BestCondition.WEEK);
			}
		});
		
		bestMonthBtn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				doSetBestLayout(BestCondition.MONTH);
			}
		});
		
		mySearchWordBtn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				doSetMySearchLayout(0);
			}
		});
		
		mySearchReplyBtn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				doSetMySearchLayout(1);
			}
		});
		
		searchClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				doSetListUpper(UpperBodyStage.CONV_LIST);
				doSetListBottom(BottomBodyStage.CONV);
			}
		});
		mySearchClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				doSetListUpper(UpperBodyStage.CONV_LIST);
				doSetListBottom(BottomBodyStage.CONV);
			}
		});
		
		searchCondition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(searchConditionValue == SearchCondition.NICK_NAME){
					doSetSearchConditon(SearchCondition.WORD);
				}else{
					doSetSearchConditon(SearchCondition.NICK_NAME);
				}
			}
		});
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				searchNowBody = searchBody.getText().toString();
				if(searchNowBody.length() != 0){
					searchLastId.set(-1L);
					searchAllLoaded.set(false);
					searchLoading = true;
					
					doSetListUpper(UpperBodyStage.PROGRESS);
					if(searchConditionValue == SearchCondition.NICK_NAME){
						satelite.doGetCommunityPostsWithNickName(OTOApp.getInstance().getToken(), data.id, searchLastId.get(), searchNowBody);
					}else{
						satelite.doGetCommunityPostsWithWord(OTOApp.getInstance().getToken(), data.id, searchLastId.get(), searchNowBody);
					}
				}
			}
		});
		searchListView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int state) {
				if(lastBottomBodyStage == BottomBodyStage.BEST) return;
				if(state == OnScrollListener.SCROLL_STATE_IDLE){
					if(view.getFirstVisiblePosition() == 0 && view.getCount() != 0){
						if(searchAllLoaded.get() == false && searchLoading == false){
							OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTOpenTalkRoom.this);
							if(lastBottomBodyStage == BottomBodyStage.SEARCH){
								if(searchConditionValue == SearchCondition.NICK_NAME){
									satelite.doGetCommunityPostsWithNickName(OTOApp.getInstance().getToken(), data.id, searchLastId.get(), searchNowBody);
								}else{
									satelite.doGetCommunityPostsWithWord(OTOApp.getInstance().getToken(), data.id, searchLastId.get(), searchNowBody);
								}
								searchLoading = true;
							}else if(lastBottomBodyStage == BottomBodyStage.MY_SEARCH){
								if(mySearchStage == 0){
									satelite.doGetCommunityPostsWithNickName(OTOApp.getInstance().getToken(), data.id, searchLastId.get(), searchNowBody);
									searchLoading = true;
								}
								if(mySearchStage == 1){
									satelite.doGetCommunityMyReply(OTOApp.getInstance().getToken(), data.id, searchLastId.get());
									searchLoading = true;
								}
							}
						}
					}
				}
			}
			@Override public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {}
		});
	}

	@Override
	public void setupListElem(OTMsgBase msg, ListElem elem) {
		if(msg instanceof OTComMsg){
			elem.elemType = ListElemType.MSG;
		}
	}

	@Override
	public void setupConvView(ListElem elem, MsgHolder holder,final ArrayAdapter<?> adapter) {
		if(elem.elemType == ListElemType.MSG){
			OTComMsg msg = null;
			if(elem.msg instanceof OTComMsg){
				msg = (OTComMsg) elem.msg;
			}
			if(msg == null) return;
			
			if(holder.listElem.elemType == ListElemType.MSG){
				if(data.write_method_chat == false){
					try{
						//int textMaxWidth = (int)getResources().getDimension(R.dimen.ot_text_msg_maxwidth);
						holder.getNowConvResource().bottomLayout.setVisibility(View.VISIBLE);
						holder.nowConvResource.msgBody.setMinimumWidth((int)PLEtcUtilMgr.dpToPx(getResources(), 190));
						if(holder.listElem.msg.isSendMsg()){
							holder.nowConvResource.msgBody.setBackgroundResource(R.drawable.oto_conv_right_content);
						}else{
							holder.nowConvResource.msgBody.setBackgroundResource(R.drawable.oto_conv_left_content);
						}
						holder.nowConvResource.likeCnt.setText("" + msg.getLike_count());
						holder.nowConvResource.commentCnt.setText("" + msg.getReply_count());
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}else{
					holder.getNowConvResource().bottomLayout.setVisibility(View.GONE);
				}
			}
			
			if(msg.isSendMsg() == false){
				String nickName = TAUserNick.getInstance().getUserInfo(msg.getSender_id());
				holder.user_name.setText(nickName);
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
				if(pendingMsgMap.containsKey((long)msg.getTransact_id()) == false){
					layoutFail = true;
				}
			}else{
				holder.setStatus(MsgHolder.STATUS_READ, 0);
			}
			
			holder.setLayoutFail(layoutFail);
			if(layoutFail == false){
				if(data.write_method_chat == false){
					holder.nowConvResource.statusLayout.setVisibility(View.GONE);
					holder.nowConvResource.msgBodyParent.getLayoutParams().width = ViewGroup.LayoutParams.FILL_PARENT;
					holder.nowConvResource.body.setMaxWidth(9999999);
				}
			}
		}
	}

	@Override
	public void onMsgReceived(OTMsgBase msg) {
		if(msg instanceof OTComMsg){
			OTComMsg talkMsg = (OTComMsg) msg;
			if(data == null) return;
			if(talkMsg.getCommunity_id() != data.id) return;
			if(talkMsg.getSender_id() == OTOApp.getInstance().getId()) return;
			
			addMsgData(elemLists, talkMsg, false);
		}
	}

	@Override
	public void onNotify(Notify packet) {
		if(packet.hasCommentCount()){
			Notify_Community_CommentCount commentCount = packet.getCommentCount();
			if(commentCount.getCommunityId() != data.id) return;
			if(searchElemList != null && msgListAdapterToSearchList != null){
				for(ListElem elem : searchElemList){
					if(elem.elemType == ListElemType.MSG){
						OTComMsg msg = (OTComMsg) elem.msg;
						if(msg.getId() == commentCount.getPostId()){
							msg.setReply_count(commentCount.getCommentCnt());
							msgListAdapterToSearchList.notifyDataSetChanged();
							break;
						}
					}
				}
			}
			for(ListElem elem : elemLists){
				if(elem.elemType == ListElemType.MSG){
					OTComMsg msg = (OTComMsg) elem.msg;
					if(msg.getId() == commentCount.getPostId()){
						msg.setReply_count(commentCount.getCommentCnt());
						onMsgListNotifyDataSetChanged();
						break;
					}
				}
			}
		}
		if(packet.hasLikeCount()){
			Notify_Community_LikeCount likeCount = packet.getLikeCount();
			if(likeCount.getCommunityId() != data.id) return;
			if(searchElemList != null && msgListAdapterToSearchList != null){
				for(ListElem elem : searchElemList){
					if(elem.elemType == ListElemType.MSG){
						OTComMsg msg = (OTComMsg) elem.msg;
						if(msg.getId() == likeCount.getPostId()){
							msg.setLike_count(likeCount.getLikeCnt());
							msgListAdapterToSearchList.notifyDataSetChanged();
							break;
						}
					}
				}
			}
			for(ListElem elem : elemLists){
				if(elem.elemType == ListElemType.MSG){
					OTComMsg msg = (OTComMsg) elem.msg;
					if(msg.getId() == likeCount.getPostId()){
						msg.setLike_count(likeCount.getLikeCnt());
						onMsgListNotifyDataSetChanged();
						break;
					}
				}
			}
		}
	}

	@Override public void OnDeleteMsg(OTMsgBase msg) {}

	@Override
	public boolean hasPendingMsg(OTMsgBase msg) {
		OTComMsg comMsg = (OTComMsg) msg;
		return pendingMsgMap.containsKey(comMsg.getTransact_id());
	}

	@Override
	public boolean OnSendMsg(String textMsg) {
		if(data == null){
			return false;
		}
		if(data.need_picture){
			OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_need_pictrue), this);
			return false;
		}
		
		Long myID = OTOApp.getInstance().getId();
		String token = OTOApp.getInstance().getToken();
		long transactId = getNextTransactId();
		
		OTComMsg msg = new OTComMsg();
		msg.setTransact_id(transactId);
		msg.setMsg(textMsg);
		msg.setId(-1L);		
		msg.setSender_id(myID);
		msg.setTime(System.currentTimeMillis());
		msg.setImgMsg(false);
		msg.setSendMsg(true);
		msg.setTableIdx((int)transactId);
		
		pendingMsgMap.put(transactId, msg);
		saveSendingMsgMap.put(transactId, msg);
		addMsgData(elemLists, msg, true);
		
		satelite.doPostCommunity(token, textMsg, data.id, transactId);
		return true;
	}

	void OnSendImgMsg(JSONArray imagePaths, String textMsg) {
		if(imagePaths == null) return;
		List<String> listImagePath = new ArrayList<String>();
		for(int i=0;i<imagePaths.length();++i){
			try{
				String path = imagePaths.getString(i);
				listImagePath.add(path);
			}catch(JSONException ex){}
		}
		OnSendImgMsg(listImagePath, textMsg);
	}
	
	@Override
	public boolean OnSendImgMsg(List<String> imagePaths, String textMsg) {
		if(data == null){
			return false;
		}
		Long myID = OTOApp.getInstance().getId();
		String token = OTOApp.getInstance().getToken();
		long transactId = getNextTransactId();
		
		OTComMsg msg = new OTComMsg();
		msg.setTransact_id(transactId);
		msg.setMsg(textMsg);
		msg.setId(-1L);		
		msg.setSender_id(myID);
		msg.setTime(System.currentTimeMillis());
		msg.setImgMsg(true);
		msg.setSendMsg(true);
		msg.setTableIdx((int)transactId);
		
		JSONArray imgUrl = new JSONArray();
		for(String path : imagePaths){
			imgUrl.put(path);
		}
		msg.setPreSendImg_url(imgUrl);
		
		pendingMsgMap.put(transactId, msg);
		saveSendingMsgMap.put(transactId, msg);
		addMsgData(elemLists, msg, true);
		
		OTImgPostData imgData = new OTImgPostData();
		imgData.setToken(token);
		imgData.setCommunity_id(data.id);
		imgData.setImagePaths(imagePaths);
		imgData.setMsg(textMsg);
		imgData.setTransact_id(transactId);
		imgData.setSender_id(myID);
		
		satelite.doSendImgPost(imgData);
		return true;
	}

	@Override
	public void OnReSendButtonPressed(long transact_id) {
		OTMsgBase msg = saveSendingMsgMap.get(transact_id);
		removeListElem(msg);
		if(msg.isImgMsg()){
			OnSendImgMsg(msg.getPreSendImg_url(), msg.getMsg());
		}else{
			OnSendMsg(msg.getMsg());
		}
	}

	@Override
	public void OnDelMsgButtonPressed(long transact_id) {
		OTMsgBase msg = saveSendingMsgMap.get(transact_id);
		removeListElem(msg);
	}

	@Override
	public void OnUserPressed(long user_id) {
		Intent intent = new Intent(this, OTFriendPopup.class);
		Bundle bundle = new Bundle();
		bundle.putLong("user_id", user_id);
		bundle.putBoolean("authority", authority);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	@Override
	public int OnLoadNextMsg() {
		if(data != null){
			if(noneAllLoaded.get() == false && noneLoading == false){
				satelite.doGetCommunityPosts(OTOApp.getInstance().getToken(), data.id, noneLastId.get());
				noneLoading = true;
			}
		}
		return -1;
	}
	
	public static OTComMsg parseComMsg(JSONObject data) throws JSONException{
		OTComMsg msg = new OTComMsg();
		if(data.has("deleted")) return null;
		
		msg.setId(data.getLong("id"));
		msg.setSender_id(data.getLong("sender_id"));
		msg.setMsg(data.getString("msg"));
		msg.setTime(data.getLong("send_time"));
		msg.setReply_count(data.getInt("reply_count"));
		msg.setLike_count(data.getInt("like_count"));
		msg.setCommunity_id(data.getLong("msg_owner_id"));
		
		try{
			JSONObject obj = new JSONObject(msg.getMsg());
			String name = obj.getString("name");
			if(name.equals("image_message")){
				msg.setImg_url(obj.getJSONArray("img_url"));
				msg.setMsg(obj.getString("msg"));
				msg.setImgMsg(true);
			}
		}catch(JSONException ex){
			msg.setImgMsg(false);
		}
		
		if(msg.getSender_id() == OTOApp.getInstance().getId()){
			msg.setSendMsg(true);
		}
		
		TAUserInfo senderInfo = TASateliteDispatcher.dispatchUserInfo(data.getJSONObject("sender_info"), true);
		msg.setSenderInfo(senderInfo);
		
		return msg;
	}
	
	void addMsgData(List<ListElem> preElemlist, OTComMsg newMsg, boolean sendMessage){
		Map<Long, Boolean> timeMap = new HashMap<Long, Boolean>();
		ArrayList<ListElem> removeList = new ArrayList<ListElem>();
		
		boolean isBottom = false;
		if(convListView.getChildCount() > 0 && convListView.getLastVisiblePosition() == convListView.getAdapter().getCount() -1 &&
				convListView.getChildAt(convListView.getChildCount() - 1).getBottom() <= convListView.getHeight()){
			isBottom = true;
		}
		
		lastMsgTimeCheck(newMsg.getTime());
		
		for(ListElem elem : preElemlist){
			if(elem.elemType != ListElemType.MSG){
				removeList.add(elem);
			}
		}
		for(ListElem elem : removeList){
			preElemlist.remove(elem);
		}
		
		preElemlist.add(new ListElem(newMsg));
		
		for(int i=0;i<preElemlist.size();++i){
			OTMsgBase msg = preElemlist.get(i).msg;
			String timeDate = getBaseDatePartOne(msg.getTime());
			long time = getCalcBaseTime(msg.getTime());
			if(timeMap.containsKey(time) == false){
				timeMap.put(time, true);
				preElemlist.add(i, new ListElem(timeDate, ListElemType.MSG_DATE, time));
				++i;
			}
		}
		
		onMsgListNotifyDataSetChanged();
		
		if(sendMessage == false){
			if(isBottom){
				convListScrollToBottom();
			}else{
				String msg = null;
				if(newMsg.getMsg().length() != 0){
					msg = newMsg.getMsg();
				}else{
					if(newMsg.isImgMsg()){
						msg = getString(R.string.oto_picture);
					}
				}
				setupConvListAppendLayout(newMsg.getSender_id(), msg);
			}
		}else{
			convListScrollToBottom();
		}
	}
	
	public void lastMsgTimeCheck(Long time){
		if(OTOpenTalkRoom.this.data.last_time < time){
			if(OTOApp.getInstance().getDB().beginTransaction()){
				CommunityLastTimeTable.getInstance().insertWithBeginTransaction(OTOpenTalkRoom.this.data.id, time);
				OTOApp.getInstance().getDB().endTransaction();
			}
		}
	}
	
	public void lastMsgTimeCheckWithNoTransaction(Long time){
		if(OTOpenTalkRoom.this.data.last_time < time){
			CommunityLastTimeTable.getInstance().insertWithBeginTransaction(OTOpenTalkRoom.this.data.id, time);
		}
	}
	
	void applyMsgData(MsgListAdapater adapter,
			List<ListElem> preElemlist, JSONArray postList, ListView listView, AtomicBoolean allLoaded, AtomicLong lastMsgId) throws JSONException{
		Map<Long, Boolean> timeMap = new HashMap<Long, Boolean>();
		ArrayList<ListElem> removeList = new ArrayList<ListElem>();
		
		if(allLoaded != null){
			if(postList.length() == 0){
				allLoaded.set(true);
				return;
			}
		}
		
		for(ListElem elem : preElemlist){
			if(elem.elemType != ListElemType.MSG){
				removeList.add(elem);
			}
		}
		for(ListElem elem : removeList){
			preElemlist.remove(elem);
		}
		
		long nextSelectionMsgId = -1;
		if(OTOApp.getInstance().getDB().beginTransaction()){
			for(int i=0;i<postList.length();++i){
				OTComMsg msg = parseComMsg(postList.getJSONObject(i));
				lastMsgTimeCheckWithNoTransaction(msg.getTime());
				if(i == 0) nextSelectionMsgId = msg.getId();
				preElemlist.add(0, new ListElem(msg));
			}
			OTOApp.getInstance().getDB().endTransaction();
		}
		
		if(lastMsgId != null){
			ListElem elem = preElemlist.get(0);
			if(elem.msg != null){
				lastMsgId.set(elem.msg.getId());
			}
		}
		
		for(int i=0;i<preElemlist.size();++i){
			OTMsgBase msg = preElemlist.get(i).msg;
			String timeDate = getBaseDatePartOne(msg.getTime());
			long time = getCalcBaseTime(msg.getTime());
			if(timeMap.containsKey(time) == false){
				timeMap.put(time, true);
				preElemlist.add(i, new ListElem(timeDate, ListElemType.MSG_DATE, time));
				++i;
			}
		}
		
		int nextScrollPos = -1;
		for(int i=0;i<preElemlist.size();++i){
			OTMsgBase msg = preElemlist.get(i).msg;
			if(msg == null) continue;
			if(msg.getId() == nextSelectionMsgId){
				nextScrollPos = i;
				break;
			}
		}
		
		adapter.notifyDataSetChanged();
		if(nextScrollPos > 0){
			listView.setSelection(nextScrollPos);
		}
	}
	
	protected void onApplyNoneTypeMsgData(JSONArray postList, AtomicBoolean allLoaded, AtomicLong lastMsgId) throws JSONException{
		applyMsgData(msgListAdapter, elemLists, postList, convListView, allLoaded, lastMsgId);
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		try{
			String state = data.getString("state");
			String location = data.getString("location");
			if(location.equals(TASatelite.getName(TASatelite.GET_COMMUNITY_DETAIL))){
				JSONObject realData = data.getJSONObject("data");
				if(state.equals("ok")){
					OTOpenTalkRoom.this.data = TASateliteDispatcher.dispatchComData(realData);
					if(OTOpenTalkRoom.this.data.need_picture){
						Intent intent = new Intent(OTOpenTalkRoom.this, OTOpenTalkImageRoom.class);
						intent.putExtra("community_data", OTOpenTalkRoom.this.data);
						intent.putExtra("authority", authority);
						startActivity(intent);
						finish();						
					}else{
						setupComConvs();
					}
				}
			}else if(location.equals(TASatelite.getName(TASatelite.GET_COMMUNITY_POSTS))){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					JSONArray postList = realData.getJSONArray("data");
					JSONObject searchConditon = realData.getJSONObject("search_condition");
					
					if(searchConditon.getString("search_type").equals("none")){
						onApplyNoneTypeMsgData(postList, noneAllLoaded, noneLastId);
						noneLoading = false;
					}else{
						if(lastBottomBodyStage == BottomBodyStage.CONV) return;
						if(lastBottomBodyStage == BottomBodyStage.MY_SEARCH && mySearchStage != 0) return;
						
						boolean setupData = true;
						if(searchLastId.get() == -1L){
							if(postList.length() == 0){
								setupData = false;
								doSetListUpper(UpperBodyStage.SEARCH_EMPTY);
							}else{
								searchElemList.clear();
								searchListView.setAdapter(msgListAdapterToSearchList = new MsgListAdapater(this, -1, searchElemList));
								searchListView.setSelector(android.R.color.transparent);
								doSetListUpper(UpperBodyStage.SEARCH_LIST);
							}
						}
						if(setupData){
							if(searchConditon.getString("search_type").equals("nick_name")){
								applyMsgData(msgListAdapterToSearchList, searchElemList, postList, searchListView, searchAllLoaded, searchLastId);
							}
							if(searchConditon.getString("search_type").equals("word")){
								applyMsgData(msgListAdapterToSearchList, searchElemList, postList, searchListView, searchAllLoaded, searchLastId);
							}
						}
						searchLoading = false;
					}
				}else{
					if(state.equals("nick_name is not valid")){
						doSetListUpper(UpperBodyStage.SEARCH_EMPTY);
					}
				}
			}else if(location.equals(TASatelite.getName(TASatelite.GET_COMMUNITY_BEST))){
				JSONObject realData = data.getJSONObject("data");
				JSONArray postList = realData.getJSONArray("result");
				int stage = realData.getInt("stage");
				
				searchElemList.clear();
				searchListView.setAdapter(msgListAdapterToSearchList = new MsgListAdapater(this, -1, searchElemList));
				searchListView.setSelector(android.R.color.transparent);
				doSetListUpper(UpperBodyStage.SEARCH_LIST);
				
				if(bestStage == stage){
					if(postList.length() == 0){
						doSetListUpper(UpperBodyStage.SEARCH_EMPTY);
					}else{
						applyMsgData(msgListAdapterToSearchList, searchElemList, postList, searchListView, null, null);
					}
				}
				bestLoading = false;
			}else if(location.equals(TASatelite.getName(TASatelite.GET_COMMUNITY_MY_REPLY_URL))){
				if(lastBottomBodyStage != BottomBodyStage.MY_SEARCH || mySearchStage != 1) return;
				if(state.equals("ok")){
					JSONArray postList = data.getJSONArray("data");
					boolean setupData = true;
					if(searchLastId.get() == -1L){
						if(postList.length() == 0){
							setupData = false;
							doSetListUpper(UpperBodyStage.SEARCH_EMPTY);
						}else{
							searchElemList.clear();
							searchListView.setAdapter(msgListAdapterToSearchList = new MsgListAdapater(this, -1, searchElemList));
							searchListView.setSelector(android.R.color.transparent);
							doSetListUpper(UpperBodyStage.SEARCH_LIST);
						}
					}
					if(setupData){
						applyMsgData(msgListAdapterToSearchList, searchElemList, postList, searchListView, searchAllLoaded, searchLastId);
					}
					searchLoading = false;
				}else{
					doSetListUpper(UpperBodyStage.SEARCH_EMPTY);
				}
			}else if(location.equals(TASatelite.getName(TASatelite.LIKE_COMMUNITY))){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					int like_count = realData.getInt("like_count");
					long post_id = realData.getLong("post_id");
					if(pendingLikeMap.containsKey(post_id)){
						OTComMsg comMsg = (OTComMsg) pendingLikeMap.get(post_id);
						comMsg.setLike_count(Math.max(0, like_count));
						pendingLikeMap.remove(post_id);
					}
					onMsgListNotifyDataSetChanged();
				}else{
					if(state.equals("app_code is not valid")){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_app_code_is_not_valid), this);
					}
				}
			}else if(location.equals(TASatelite.getName(TASatelite.POST_COMMUNITY))){
				JSONObject realData = data.getJSONObject("data");
				long transact_id = realData.getLong("transact_id");
				
				if(state.equals("ok")){
					Long msg_id = realData.getLong("msg_id");
					Long send_time = realData.getLong("send_time");
					lastMsgTimeCheck(send_time);
					OTMsgBase msg = pendingMsgMap.get(transact_id);
					msg.setId(msg_id);
					msg.setTime(send_time);
					if(saveSendingMsgMap.containsKey(transact_id)){
						saveSendingMsgMap.put(transact_id, msg);
					}
				}else{
					if(state.equals("write_limit_error")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this,
							getString(R.string.oto_lib_name), getString(R.string.oto_write_limit_error));
					}
				}
				
				if(pendingMsgMap.containsKey(transact_id)){
					pendingMsgMap.remove(transact_id);
				}
				
				onMsgListNotifyDataSetChanged();
				convListScrollToBottom();
			}else if(location.equals(TASatelite.getName(TASatelite.POST_IMG_COMMUNITY))){
				JSONObject realData = data.getJSONObject("data");
				long transact_id = realData.getLong("transact_id");
				
				if(state.equals("ok")){
					Long msg_id = realData.getLong("msg_id");
					Long send_time = realData.getLong("send_time");
					JSONArray img_url = realData.getJSONArray("img_url");
					OTMsgBase msg = pendingMsgMap.get(transact_id);
					msg.setId(msg_id);
					msg.setTime(send_time);
					msg.setImg_url(img_url);
					if(saveSendingMsgMap.containsKey(transact_id)){
						saveSendingMsgMap.put(transact_id, msg);
					}
				}else{
					if(state.equals("write_limit_error")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this,
							getString(R.string.oto_lib_name), getString(R.string.oto_write_limit_error));
					}
				}
				
				if(pendingMsgMap.containsKey(transact_id)){
					pendingMsgMap.remove(transact_id);
				}
				
				onMsgListNotifyDataSetChanged();
				convListScrollToBottom();
			}
		}catch(Exception ex){
			ex.printStackTrace();
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
		if(addr.equals(TASatelite.POST_COMMUNITY)){
			try {
				Long transact_id = data.getLong("transact_id");
				if(pendingMsgMap.containsKey(transact_id)){
					pendingMsgMap.remove(transact_id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			convListView.invalidateViews();
		}
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		if(addr.equals(TASatelite.POST_IMG_COMMUNITY)){
			OTImgPostData castData = (OTImgPostData)data;
			Long transact_id = Long.valueOf(castData.getTransact_id());
			if(pendingMsgMap.containsKey(transact_id)){
				pendingMsgMap.remove(transact_id);
			}
			convListView.invalidateViews();
		}
	}
}
