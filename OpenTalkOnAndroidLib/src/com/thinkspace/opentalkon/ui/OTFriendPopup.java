package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.common.util.PLUIUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAIgnore;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;

public class OTFriendPopup extends Activity implements TADataHandler{
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	TASatelite satelite;
	TAUserInfo userInfo;
	
	View btnLayout;
	TextView nickName;
	TextView introduce;
	ImageView imgView;
	ViewGroup [] btnViews = new ViewGroup[3];
	boolean isCreating;
	boolean authority;
	
	View adminEmblem;
	boolean admin;
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(TASatelite.CHECK_ADMIN.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData =data.getJSONObject("data");
					admin = realData.getBoolean("admin");
					drawLayout();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
		if(dData.hasLocation(TASatelite.getName(TASatelite.GET_USER_INFO_URL))){
			if(dData.isOK()){
				userInfo = (TAUserInfo)dData.getData();
				satelite.doCheckAdmin(OTOApp.getInstance().getToken(), userInfo.getId());
				drawLayout();
			}else{
				if(dData.getState().equals("user_id is not valid")){
					return;
				}
			}
		}else if(dData.hasLocation(TASatelite.getName(TASatelite.SET_BEST_FRIEND_FLAG))){
			if(dData.isOK()){
				Intent intent = new Intent();
				intent.putExtra("refresh_list", true);
				setResult(RESULT_OK, intent);
				finish();
			}else{
				
			}
		}else if(dData.hasLocation(TASatelite.getName(TASatelite.ADD_FRIEND_URL))){
			if(dData.isOK()){
				OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_add_friend_success), this);
				finish();
			}else{
				OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_add_friend_fail), this);
			}
		}
	}
	
	boolean finishGuard = false;
	
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
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	
	@Override
	protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
		super.onApplyThemeResource(theme, resid, first);
		theme.applyStyle(R.style.oto_YourCustomStyle, true);
	}
	
	class ChatBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if (OTOApp.getInstance().getToken().length() == 0)
				return;
			
			Intent intent = new Intent(OTFriendPopup.this, OTChatRoom.class);
			
			ArrayList<Long> users = new ArrayList<Long>();
			users.add(userInfo.getId());
			users.add(OTOApp.getInstance().getId());
			intent.putExtra("user_list", users);
			OTFriendPopup.this.startActivity(intent);
			finish();
		}
	}
	
	class BestFriendBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTFriendPopup.this);
			satelite.doSetBestFriendFlag(OTOApp.getInstance().getToken(), !userInfo.isFriend_best(), userInfo.getId());
		}
	}
	
	class BlockBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(OTOApp.getInstance().getCacheCtrl().hasIgnore(userInfo.getId())){
				OTOApp.getInstance().getCacheCtrl().removeIgnore(userInfo.getId());
			}else{
				TAIgnore ignore = new TAIgnore(userInfo.getId());
				OTOApp.getInstance().getCacheCtrl().addIgnore(ignore);
			}
			finish();
		}
	}
	
	class MyWordBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			
		}
	}
	
	class AddFriendBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTFriendPopup.this);
			satelite.doAddFriend(OTOApp.getInstance().getToken(), userInfo.getId());
		}
	}
	
	class ViewProfileBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(OTFriendPopup.this, OTSettingMyInfo.class);
			intent.putExtra("is_my_info", true);
			intent.putExtra("user_info", userInfo);
			OTFriendPopup.this.startActivity(intent);
			
			Intent retIntent = new Intent();
			retIntent.putExtra("refresh_list", true);
			setResult(RESULT_OK, retIntent);
		}
	}
	
	public TextView getTextInsideView(ViewGroup view){
		for(int i=0;i<view.getChildCount();++i){
			View rawView = view.getChildAt(i);
			if(rawView instanceof TextView){
				return (TextView)rawView;
			}
		}
		return null;
	}
	
	public ImageView getImageInsideView(ViewGroup view){
		for(int i=0;i<view.getChildCount();++i){
			View rawView = view.getChildAt(i);
			if(rawView instanceof ImageView){
				return (ImageView)rawView;
			}
		}
		return null;
	}
	
	public void drawLayout(){
		if(authority == false){
			btnLayout.setVisibility(View.GONE);
		}
		ChatBtnListener chatBtn = new ChatBtnListener();
		BestFriendBtnListener bestBtn = new BestFriendBtnListener();
		BlockBtnListener blockBtn = new BlockBtnListener();
		AddFriendBtnListener addFriendBtn = new AddFriendBtnListener();
		ViewProfileBtnListener viewProfileBtn = new ViewProfileBtnListener();
		
		if(admin){
			adminEmblem.setVisibility(View.VISIBLE);
			adminEmblem.setBackgroundResource(R.drawable.oto_admin_emblem_anim);
			final AnimationDrawable frameAnimation  = (AnimationDrawable) adminEmblem.getBackground();
			adminEmblem.post(new Runnable() {
				@Override public void run() {
					frameAnimation.start();
				}
			});
		}else{
			adminEmblem.setVisibility(View.GONE);
		}
		
		if(userInfo.getId() == OTOApp.getInstance().getId()){
			btnViews[0].setVisibility(View.GONE);
			btnViews[1].setOnClickListener(viewProfileBtn);
			btnViews[2].setVisibility(View.GONE);
			
			getTextInsideView(btnViews[1]).setText(getString(R.string.oto_friend_popup_profile));
			getImageInsideView(btnViews[1]).setImageResource(R.drawable.oto_set_profile);
		}else{
			if(userInfo.is_friend()){
				btnViews[1].setVisibility(View.GONE);
				btnViews[0].setOnClickListener(chatBtn);
				btnViews[2].setOnClickListener(bestBtn);
				
				getTextInsideView(btnViews[0]).setText(getString(R.string.oto_friend_popup_talk));
				getImageInsideView(btnViews[0]).setImageResource(R.drawable.oto_friend_top_menu_03_s);
				getImageInsideView(btnViews[2]).setImageResource(R.drawable.oto_friend_top_menu_01_s);
				if(userInfo.isFriend_best()){
					getTextInsideView(btnViews[2]).setText(getString(R.string.oto_friend_popup_del_bookmark));
				}else{
					getTextInsideView(btnViews[2]).setText(getString(R.string.oto_friend_popup_add_bookmark));
				}
			}else{
				btnViews[0].setOnClickListener(chatBtn);
				btnViews[1].setOnClickListener(addFriendBtn);
				btnViews[2].setOnClickListener(blockBtn);
				
				getTextInsideView(btnViews[0]).setText(getString(R.string.oto_friend_popup_talk));
				getImageInsideView(btnViews[0]).setImageResource(R.drawable.oto_friend_top_menu_03_s);
				
				getTextInsideView(btnViews[1]).setText(getString(R.string.oto_friend_popup_add_friend));
				getImageInsideView(btnViews[1]).setImageResource(R.drawable.oto_add_friend);
				
				if(OTOApp.getInstance().getCacheCtrl().hasIgnore(userInfo.getId())){
					getTextInsideView(btnViews[2]).setText(getString(R.string.oto_friend_popup_unblock));
					btnViews[0].setVisibility(View.GONE);
					btnViews[1].setVisibility(View.GONE);
				}else{
					getTextInsideView(btnViews[2]).setText(getString(R.string.oto_friend_popup_block));
					btnViews[0].setVisibility(View.VISIBLE);
					btnViews[1].setVisibility(View.VISIBLE);
				}
				getImageInsideView(btnViews[2]).setImageResource(R.drawable.oto_ban);
			}
		}
		
		if(userInfo != null){
			nickName.setText("(" + String.valueOf(userInfo.getId()) +")" + userInfo.getNickName());
			introduce.setText(userInfo.getIntroduce());
			
			if(userInfo.getImagePath().length() != 0){
				String imgUrl = TASatelite.makeImageUrl(userInfo.getImagePath());
				OTOApp.getInstance().getImageDownloader().requestImgDownload(imgUrl, true, new TAImageDataHandler() {
					@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
						displayer.display(bitmap, new ImageViewAware(imgView), null);
					}
					@Override public void onHttpImageException(Exception ex) {
					}
				});
				imgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(OTFriendPopup.this, OTEntireImageView.class);
						ArrayList<String> arr = new ArrayList<String>();
						arr.add(userInfo.getImagePath());
						intent.putExtra("img_path", arr);
						intent.putExtra("img_pos", 0);
						
						OTFriendPopup.this.startActivity(intent);
					}
				});
			}else{
				imgView.setImageResource(R.drawable.oto_user_background);
				imgView.setOnClickListener(null);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(userInfo != null && isCreating == false){
			satelite.doGetUserInfo(OTOApp.getInstance().getToken(), userInfo.getId());
		}
		if(isCreating)isCreating = false;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isCreating = true;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = PLUIUtilMgr.getDisplaySize(display);
		
		View parentView = getLayoutInflater().inflate(R.layout.ot_friend_popup, null);
		setContentView(parentView, new ViewGroup.LayoutParams(size.x - 80, LayoutParams.WRAP_CONTENT));
		View coverView = parentView.findViewById(R.id.oto_friend_detail_cover);
		btnLayout =  parentView.findViewById(R.id.oto_friend_detail_btn_layout);
		
		View fullImageView = parentView.findViewById(R.id.oto_friend_detail_full_image);
		fullImageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, size.x - 80));
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		adminEmblem = findViewById(R.id.oto_friend_detail_admin);
		adminEmblem.setVisibility(View.GONE);
		
		nickName = (TextView) findViewById(R.id.oto_friend_detail_nick_name);
		introduce = (TextView) findViewById(R.id.oto_friend_detail_introduce);
		imgView = (ImageView) findViewById(R.id.oto_friend_detail_image);
		btnViews[0] = (ViewGroup)findViewById(R.id.oto_friend_detail_btn1);
		btnViews[1] = (ViewGroup)findViewById(R.id.oto_friend_detail_btn2);
		btnViews[2] = (ViewGroup)findViewById(R.id.oto_friend_detail_btn3);
		
		satelite = new TASatelite(this);
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		
		if(intent.hasExtra("user_info") == false && intent.hasExtra("user_id") == false){
			finish();
			return;
		}
		
		authority = intent.getBooleanExtra("authority", true);
		TAUserInfo extraUser = intent.getParcelableExtra("user_info");
		
		if(authority == false){
			btnLayout.getLayoutParams().height = 0;
		}
		coverView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, btnLayout.getLayoutParams().height + size.x - 80));
		
		if(extraUser != null){
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
			satelite.doGetUserInfo(OTOApp.getInstance().getToken(), extraUser.getId());
		}else{
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
			long user_id = intent.getLongExtra("user_id", -1L);
			satelite.doGetUserInfo(OTOApp.getInstance().getToken(), user_id);
		}		
	}
}
