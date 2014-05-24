package com.thinkspace.opentalkon.ui;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTMsgBase;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroup;
import com.thinkspace.opentalkon.ui.helper.PrettyTextView;
import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;
import com.thinkspace.pushservice.satelite.PLMsgHandler;

public class OTMain extends PLActivityGroup implements PLMsgHandler, OnClickListener {
	SlidingMenu menu;
	int nowState;
	TASatelite satelite;
	
	TextView msgCount;
	ImageView image;
	TextView name;
	PrettyTextView followerCnt;
	PrettyTextView followingCnt;
	RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	
	@Override
	public void onMsgReceived(OTMsgBase msg) {
		setMsgCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterMsgHandler(this);
		}
	}
	
	void setMsgCount(int count){
		if(msgCount == null) return;
		if(count == 0){
			msgCount.setVisibility(View.GONE);
		}else{
			msgCount.setVisibility(View.VISIBLE);
			msgCount.setText(String.valueOf(count));
		}
	}
	
	void setupMenu(){
		menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.ex_main_menu_layout);
        menu.setShadowWidth(5);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setFadeEnabled(false);
		findViewById(R.id.oto_main_menu).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				menu.toggle(true);
			}
		});
		
		msgCount = (TextView) findViewById(R.id.oto_menu_select_1_count);
		image = (ImageView) findViewById(R.id.oto_menu_image);
		name = (TextView) findViewById(R.id.oto_menu_name);
		followerCnt = (PrettyTextView) findViewById(R.id.oto_menu_follower);
		followingCnt = (PrettyTextView) findViewById(R.id.oto_menu_following);
		
		findViewById(R.id.oto_menu_following_layout).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				findViewById(R.id.oto_menu_select_2).performClick();
			}
		});
		findViewById(R.id.oto_menu_follower_layout).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				findViewById(R.id.oto_menu_select_3).performClick();
			}
		});
		findViewById(R.id.oto_menu_user_layout).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(OTMain.this,OTFriendPopup.class);
				intent.putExtra("user_id", OTOApp.getInstance().getId());
				startActivityForResult(intent, OTFriendListBase.OT_CHECK_IF_RESUME);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setMsgCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg());
		if(satelite == null){
			satelite = new TASatelite(new TADataHandler() {
				
				@Override
				public void onTokenIsNotValid(JSONObject data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLimitMaxUser(JSONObject data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onHttpPacketReceived(JSONObject data) {
					try{					
					DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
					JSONObject realData = data.getJSONObject("data");
					if(dData.isOK()){
						if(TASatelite.GET_USER_INFO_URL.endsWith(dData.getLocation())){
							TAUserInfo userInfo = (TAUserInfo) dData.getData();
							int follower = realData.getInt("follower");
							int following = realData.getInt("following");
							followerCnt.setText(String.valueOf(follower));
							followingCnt.setText(String.valueOf(following));
							name.setText(userInfo.getNickName());
							if(userInfo.getImagePath().length() != 0){
								String img_path = TASatelite.makeImageUrl(userInfo.getImagePath());
								image.setImageResource(R.drawable.oto_friend_img_01);
								OTOApp.getInstance().getImageDownloader().requestImgDownload(img_path, new TAImageDataHandler() {
									@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
										roundDisplayer.display(bitmap, new ImageViewAware(image), null);
									}
									@Override public void onHttpImageException(Exception ex) {
										image.setImageResource(R.drawable.oto_friend_img_01);
									}
								});
							}else{
								image.setImageResource(R.drawable.oto_friend_img_01);
							}
						}
					}
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
				
				@Override
				public void onHttpException(Exception ex, TAMultiData data, String addr) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onHttpException(Exception ex, JSONObject data, String addr) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		satelite.doGetUserInfo(OTOApp.getInstance().getToken(), OTOApp.getInstance().getId());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().registerMsgHandler(this);
		}
		
		setContentView(R.layout.ex_main_layout);
		/*
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = PLUIUtilMgr.getDisplaySize(display);
		
		View parentView = getLayoutInflater().inflate(R.layout.ex_main_layout, null);
		if(OTOApp.getInstance().isMainFullScreen()){
			setContentView(parentView, new ViewGroup.LayoutParams(size.x, size.y - PLUIUtilMgr.getStatusBarHeight(getResources())));
		}else{
			setContentView(parentView, new ViewGroup.LayoutParams(size.x - 40, size.y - 60));
		}*/
		setupMenu();
		setupActivity();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.anim_rightin, R.anim.anim_rightout);
	}
	
	void startActivityWithAnim(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_leftin, R.anim.anim_leftout);
	}
	
	void setupActivity(){
		Intent intent = getIntent();
		boolean doInit = intent.getBooleanExtra("doInit", true);
		if(doInit){
			OTOApp.getInstance().getPref().getLastPushLoginFailTime().setValue(-1L);
			OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
			OTOApp.getInstance().startPushService(false);
			OTOApp.getInstance().getImageDownloader().flushCache();
		}
		
		OTOApp.getInstance().IncMainActivityCount();
		
		nowState = intent.getIntExtra("state", 1);
		Class<?> loadClass = null;
		if(nowState == 1){
			loadClass = OTMainTabChatList.class;
			findViewById(R.id.oto_menu_select_1).setBackgroundResource(R.drawable.sh_white_button_pressed);
		}else if(nowState == 2){
			loadClass = OTMainTabFollowingList.class;
			findViewById(R.id.oto_menu_select_2).setBackgroundResource(R.drawable.sh_white_button_pressed);
		}else if(nowState == 3){
			loadClass = OTMainTabFollowerList.class;
			findViewById(R.id.oto_menu_select_3).setBackgroundResource(R.drawable.sh_white_button_pressed);
		}else if(nowState == 4){
			loadClass = OTAddFriend.class;
			findViewById(R.id.oto_menu_select_4).setBackgroundResource(R.drawable.sh_white_button_pressed);
		}else if(nowState == 5){
			loadClass = OTMainTabMore.class;
			findViewById(R.id.oto_menu_select_5).setBackgroundResource(R.drawable.sh_white_button_pressed);
		}
		applyPage(loadClass);
		
		findViewById(R.id.oto_menu_select_0).setOnClickListener(this);
		findViewById(R.id.oto_menu_select_1).setOnClickListener(this);
		findViewById(R.id.oto_menu_select_2).setOnClickListener(this);
		findViewById(R.id.oto_menu_select_3).setOnClickListener(this);
		findViewById(R.id.oto_menu_select_4).setOnClickListener(this);
		findViewById(R.id.oto_menu_select_5).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.oto_menu_select_0){
			Intent intent = new Intent(this,OTFriendPopup.class);
			intent.putExtra("user_id", OTOApp.getInstance().getId());
			startActivityForResult(intent, OTFriendListBase.OT_CHECK_IF_RESUME);
			return;
		}
		
		Intent intent = new Intent(this, OTMain.class);
		intent.putExtra("doInit", false);
		if(v.getId() == R.id.oto_menu_select_1){
			if(nowState == 1)return;
			intent.putExtra("state", 1);
		}else if(v.getId() == R.id.oto_menu_select_2){
			if(nowState == 2)return;
			intent.putExtra("state", 2);
		}else if(v.getId() == R.id.oto_menu_select_3){
			if(nowState == 3)return;
			intent.putExtra("state", 3);
		}else if(v.getId() == R.id.oto_menu_select_4){
			if(nowState == 4)return;
			intent.putExtra("state", 4);
		}else if(v.getId() == R.id.oto_menu_select_5){
			if(nowState == 5)return;
			intent.putExtra("state", 5);
		}
		startActivityWithAnim(intent);
		menu.toggle(false);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		OTOApp.getInstance().DecMainActivityCount();
	}

	@Override
	public FrameLayout getFrameLayOut() {
		return (FrameLayout) findViewById(R.id.oto_main_layout);
	}

	@Override
	public void onChangeView(String viewName, Object data) {
		// TODO Auto-generated method stub
		
	}
}
