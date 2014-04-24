package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.Map;

import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.common.util.PLUIUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTMsgBase;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroup;
import com.thinkspace.opentalkon.view.BaseTabButtonView;
import com.thinkspace.pushservice.satelite.PLMsgHandler;

public class OTMain extends PLActivityGroup implements OnClickListener, PLMsgHandler {
	ArrayList<BaseTabButtonView> menuButtons = new ArrayList<BaseTabButtonView>();
	ArrayList<Pair<Integer, String>> menuButtonInfos = new ArrayList<Pair<Integer,String>>();
	int selectedButton;
	PL_TAB_STATE tabState = new PL_TAB_STATE();
	
	public class PL_TAB_STATE {
		public ArrayList<PageSet> Pages;
		public Map<String, PageSet> notTabPages;
		
		public void settingAll(){
			Pages = new ArrayList<PL_TAB_STATE.PageSet>();
			
			Pages.add(new PageSet(OTMainTabFriendList.class, R.drawable.oto_friend_top_menu_02_n, R.drawable.oto_friend_top_menu_02_s,
					getString(R.string.oto_friend_tab_2)));
			Pages.add(new PageSet(OTMainTabChatList.class, R.drawable.oto_friend_top_menu_03_n, R.drawable.oto_friend_top_menu_03_s,
					getString(R.string.oto_friend_tab_3)));
			Pages.add(new PageSet(OTMainTabMore.class, R.drawable.oto_friend_top_menu_04_n, R.drawable.oto_friend_top_menu_04_s,
					getString(R.string.oto_friend_tab_4)));
		}
		
		public int findActivityState(Class<?> cls){
			for(int i=0;i<Pages.size();++i){
				PageSet now = Pages.get(i);
				if(now.cls.equals(cls)){
					return i;
				}
			}
			return -1;
		}
		
		public class PageSet{
			Class<?> cls;
			int noClick_res_id;
			int click_res_id;
			String menuName;
			
			public PageSet(Class<?> cls, int noClick_res_id, int click_res_id, String menuName) {
				this.cls = cls;
				this.noClick_res_id = noClick_res_id;
				this.click_res_id = click_res_id;
				this.menuName = menuName;
			}
		}
	}
	
	@Override
	public void onMsgReceived(OTMsgBase msg) {
		menuButtons.get(1).setNewCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterMsgHandler(this);
		}
	}

	@Override
	protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
		super.onApplyThemeResource(theme, resid, first);
		theme.applyStyle(R.style.oto_YourCustomStyle, true);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(OTOApp.getInstance().getPref().getVerifiedPhoneNumber().getValue().length() == 0 &&
				OTOApp.getInstance().isPhoneVerify()){
			startActivity(new Intent(this, OTVerifyPhone.class));
			finish();
			return;
		}
		
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().registerMsgHandler(this);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = PLUIUtilMgr.getDisplaySize(display);
		
		View parentView = getLayoutInflater().inflate(R.layout.ot_tab_main, null);
		if(OTOApp.getInstance().isMainFullScreen()){
			setContentView(parentView, new ViewGroup.LayoutParams(size.x, size.y - PLUIUtilMgr.getStatusBarHeight(getResources())));
		}else{
			setContentView(parentView, new ViewGroup.LayoutParams(size.x - 40, size.y - 60));
		}
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		tabState.settingAll();
		
		setupMenuButton();
		selectedButton = -1;
		clickButton(0);
		OTOApp.getInstance().getPref().getLastPushLoginFailTime().setValue(-1L);
		OTOApp.getInstance().setHasFriendActivity(true);
		OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
		OTOApp.getInstance().startPushService(false);
		
		OTOApp.getInstance().getImageDownloader().flushCache();
	}

	@Override
	public void onBackPressed() {
		OTOApp.getInstance().setHasFriendActivity(false);
		OTOApp.getInstance().getConvMgr().clearCommunity();
		PLEtcUtilMgr.deleteAllFile(Environment.getExternalStorageDirectory().getAbsolutePath() + OTImageLoadBase.BASE_SEND_IMG_PATH);
		super.onBackPressed();
	}

	public void setupMenuButton(){
		menuButtons.add((BaseTabButtonView)findViewById(R.id.oto_base_tab_btn1));
		menuButtons.add((BaseTabButtonView)findViewById(R.id.oto_base_tab_btn2));
		menuButtons.add((BaseTabButtonView)findViewById(R.id.oto_base_tab_btn3));
		
		for(int i=0;i<menuButtons.size();++i){
			BaseTabButtonView nowButton = menuButtons.get(i);
			nowButton.setOnClickListener(this);
			nowButton.setNoClickImage_res(tabState.Pages.get(i).noClick_res_id);
			nowButton.setClickImage_res(tabState.Pages.get(i).click_res_id);
			nowButton.setMenuName(tabState.Pages.get(i).menuName);
			nowButton.setNewCount(0);
		}
	}
	
	@Override
	public void onClick(View nowButton) {
		for(int i=0;i<menuButtons.size();++i){
			if(nowButton == menuButtons.get(i)){
				menuButtons.get(i).setClick(true);
				selectedButton = i;
				applyPage(tabState.Pages.get(i).cls);
			}else{
				menuButtons.get(i).setClick(false);
			}
		}
	}
	
	void clickButton(int nowButton){
		menuButtons.get(nowButton).performClick();
	}

	@Override
	public void onChangeView(String viewName, Object data) {
		if(viewName.equals("exit_room")){
			menuButtons.get(1).setNewCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg());
		}
	}

	@Override
	public FrameLayout getFrameLayOut() {
		return (FrameLayout) findViewById(R.id.oto_base_tab_main_layout);
	}

	@Override
	protected void onResume() {
		super.onResume();
		menuButtons.get(1).setNewCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg());
	}
}
