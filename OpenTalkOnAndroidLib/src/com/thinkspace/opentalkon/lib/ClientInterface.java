package com.thinkspace.opentalkon.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.thinkspace.opentalkon.ui.OTChatRoom;
import com.thinkspace.opentalkon.ui.OTFriendListBase;
import com.thinkspace.opentalkon.ui.OTFriendPopup;
import com.thinkspace.opentalkon.ui.OTMain;
import com.thinkspace.opentalkon.ui.helper.PrettyTextView;
import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;
import com.thinkspace.pushservice.satelite.PLMsgHandler;

public class ClientInterface {
	public static Map<Activity, PLMsgHandler> msgHandlers = new HashMap<Activity, PLMsgHandler>();
	public static Map<Activity, SlidingMenu> menus = new HashMap<Activity, SlidingMenu>();
	
	private static void setMsgCount(int count, TextView msgCount){
		if(msgCount == null) return;
		if(count == 0){
			msgCount.setVisibility(View.GONE);
		}else{
			msgCount.setVisibility(View.VISIBLE);
			msgCount.setText(String.valueOf(count));
		}
	}
	
	private static class SlidingMenuListener implements OnClickListener{
		Activity activity;
		SlidingMenu menu;
		public SlidingMenuListener(Activity activity, SlidingMenu menu) {
			this.activity = activity;
			this.menu = menu;
		}

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.oto_menu_select_0){
				Intent intent = new Intent(activity,OTFriendPopup.class);
				intent.putExtra("user_id", OTOApp.getInstance().getId());
				activity.startActivityForResult(intent, OTFriendListBase.OT_CHECK_IF_RESUME);
				return;
			}
			
			Intent intent = new Intent(activity, OTMain.class);
			intent.putExtra("doInit", true);
			if(v.getId() == R.id.oto_menu_select_1){
				intent.putExtra("state", 1);
			}else if(v.getId() == R.id.oto_menu_select_2){
				intent.putExtra("state", 2);
			}else if(v.getId() == R.id.oto_menu_select_3){
				intent.putExtra("state", 3);
			}else if(v.getId() == R.id.oto_menu_select_4){
				intent.putExtra("state", 4);
			}else if(v.getId() == R.id.oto_menu_select_5){
				intent.putExtra("state", 5);
			}
			activity.startActivity(intent);
			menu.toggle(false);
		}
	}
	
	public static void onDestroySlidingMenu(final Activity activity){
		if(msgHandlers.containsKey(activity)){
			OTOApp.getInstance().getPushClient().unRegisterMsgHandler(msgHandlers.get(activity));
		}
	}
	
	public static void onResumeSlidingMenu(final Activity activity){
		
		if(OTOApp.getInstance().getId() == -1L){
			IntentFilter iff = new IntentFilter(OTOApp.ACTION_GET_VALID_TOKEN_IS_DONE);
			activity.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					activity.unregisterReceiver(this);
					onResumeSlidingMenu(activity);
				}
			}, iff);
			return;
		}
		
        final ImageView image = (ImageView) activity.findViewById(R.id.oto_menu_image);
        final TextView name = (TextView) activity.findViewById(R.id.oto_menu_name);
        final PrettyTextView followerCnt = (PrettyTextView) activity.findViewById(R.id.oto_menu_follower);
        final PrettyTextView followingCnt = (PrettyTextView) activity.findViewById(R.id.oto_menu_following);
        final RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
        final TextView msgCount = (TextView) activity.findViewById(R.id.oto_menu_select_1_count);
        
        setMsgCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg(), msgCount);
        
        new TASatelite(new TADataHandler() {
			@Override public void onTokenIsNotValid(JSONObject data) {}
			@Override public void onLimitMaxUser(JSONObject data) {}
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
			@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}
			@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
		}).doGetUserInfo(OTOApp.getInstance().getToken(), OTOApp.getInstance().getId());
	}
	
	public static void showSlidingMenu(final Activity activity){
		if(menus.containsKey(activity)){
			menus.get(activity).toggle(true);
		}
	}
	
	public static void onCreateSlidingMenu(final Activity activity){
		final SlidingMenu menu = new SlidingMenu(activity);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        menu.attachToActivity(activity, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.ex_main_menu_layout);
        menu.setShadowWidth(5);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setFadeEnabled(false);
		
        activity.findViewById(R.id.oto_menu_following_layout).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				activity.findViewById(R.id.oto_menu_select_2).performClick();
			}
		});
        activity.findViewById(R.id.oto_menu_follower_layout).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				activity.findViewById(R.id.oto_menu_select_3).performClick();
			}
		});
        activity.findViewById(R.id.oto_menu_user_layout).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(activity,OTFriendPopup.class);
				intent.putExtra("user_id", OTOApp.getInstance().getId());
				activity.startActivityForResult(intent, OTFriendListBase.OT_CHECK_IF_RESUME);
			}
		});
        
        SlidingMenuListener listener = new SlidingMenuListener(activity, menu);
        activity.findViewById(R.id.oto_menu_select_0).setOnClickListener(listener);
        activity.findViewById(R.id.oto_menu_select_1).setOnClickListener(listener);
        activity.findViewById(R.id.oto_menu_select_2).setOnClickListener(listener);
        activity.findViewById(R.id.oto_menu_select_3).setOnClickListener(listener);
        activity.findViewById(R.id.oto_menu_select_4).setOnClickListener(listener);
        activity.findViewById(R.id.oto_menu_select_5).setOnClickListener(listener);
        
        onResumeSlidingMenu(activity);
        
        final TextView msgCount = (TextView) activity.findViewById(R.id.oto_menu_select_1_count);
        menus.put(activity, menu);
        msgHandlers.put(activity, new PLMsgHandler() {
			@Override public void onMsgReceived(OTMsgBase msg) {
				setMsgCount(OTOApp.getInstance().getCacheCtrl().getAllUnReadMsg(), msgCount);
			}
		});
		
        OTOApp.getInstance().getPushClient().registerMsgHandler(msgHandlers.get(activity));
	}
	
	/**
	 * @breif Changes icon and App name to be set on Notification. \n
	 * 			  Notification is shown when message arrives, someone 'Likes' my post or when a comment is written on post. \n
	 * 			  If not additionally set, basic OpenTalkOn icon and name is shown.
	 * @param smallIcon : Icon to be shown on status bar of notification. Drawable ID should be passed. Most appropriate when App icon is made small.(38x38 size recommended)
	 * @param bigIcon : Icon to be shown on status bar of notification. Drawable ID should be passed. Most appropriate when your original App icon is set.
	 * @param appName : App name to be used and shown in notification.
	 */
	public static void changeNotificationContents(int smallIcon, int bigIcon, String appName){
		if(smallIcon != -1){
			OTOApp.getInstance().getConvMgr().setSmallIcon(smallIcon);
		}
		if(bigIcon != -1){
			OTOApp.getInstance().getConvMgr().setBigIcon(bigIcon);
		}
		if(appName != null){
			OTOApp.getInstance().getConvMgr().setAppName(appName);
		}
	}
	/**
	 * @breif Brings user token.
	 * @remark When return value is an empty string(""), check if InitLibrary was successfully called.
	 * @return User token or empty string("") when token is not normally set
	 */
	public static String getToken(){
		return OTOApp.getInstance().getToken();
	}
	
	/**
	 * @breif Brings User ID.(Used when calling chats or user information)
	 * @remark Check if InitLibrary was successfully called if return value is -1.
	 * @return User ID or -1 when ID is not successfully set yet
	 */
	public static long getId(){
		return OTOApp.getInstance().getId();
	}
	
	/**
	 * @breif Initialize Library.
	 * @param context : Application context
	 * @param appToken : AppToken checked at OpenTalkOn management console
	 */
	public static void InitLibrary(Context context, String appToken){
		OTOApp.getInstance().InitOpenTalkLib(context, appToken, null, false);
	}	
	
	/**
	 * @breif Initialize Library.
	 * @param context : Application context
	 * @param appToken : AppToken checked at OpenTalkOn management console
	 * @param receiver : Receiver that checks of the token was successfully received
	 * @remark Intent, which is sent to the receiver, checks the result by getStringExtra("result").\n
	 * 				"ok" shows normal condition and other cases have error messages.
	 */
	public static void InitLibrary(Context context, String appToken, BroadcastReceiver receiver){
		OTOApp.getInstance().InitOpenTalkLib(context, appToken, receiver, false);
	}
	
	public static void InitLibrary(Context context, String appToken, BroadcastReceiver receiver, boolean phoneVerify){
		OTOApp.getInstance().InitOpenTalkLib(context, appToken, receiver, phoneVerify);
	}
	
	/**
	 * @breif It is checked if a token is issued to the user. If the token is not issued, OpenTalkOn API cannot be successfully used. \n
	 * 			 If the token is not issued, the issue is automatically requested. (May be issued when trying after a few seconds)
	 * @return Success condition
	 */
	public static boolean checkHasToken(){
		if(OTOApp.getInstance().getToken().length() == 0){
			OTOApp.getInstance().startPushService(false);
			return false;
		}
		return true;
	}
	
	public static boolean checkHasToken(boolean userPress){
		if(OTOApp.getInstance().getToken().length() == 0){
			OTOApp.getInstance().startPushService(userPress);
			return false;
		}
		return true;
	}
	
	/**
	 * @breif Open OpenTalkOn main Activity.
	 * @param context : Android context to open activity
	 * @param fullScreen : Fullscreen condition
	 * @return Success condition
	 */
	public static boolean startOpenTalkOnMain(final Context context, boolean fullScreen){
		OTOApp.getInstance().setMainFullScreen(fullScreen);
		final Intent intent = new Intent(context, OTMain.class);
		if(checkHasToken(true) == false){
			OTOApp.getInstance().addValidTokenDoneReceiver(new BroadcastReceiver() {
				@Override public void onReceive(Context e, Intent i) {
					context.startActivity(intent);
				}
			});
			return false;
		}else{
			context.startActivity(intent);
			return true;
		}
	}
	
	/**
	 * @breif Open OpenTalkOn main Activity.
	 * @param context : Android context to open activity
	 * @return Success condition
	 */
	public static boolean startOpenTalkOnMain(final Context context){
		OTOApp.getInstance().setMainFullScreen(false);
		final Intent intent = new Intent(context, OTMain.class);
		if(checkHasToken(true) == false){
			OTOApp.getInstance().addValidTokenDoneReceiver(new BroadcastReceiver() {
				@Override public void onReceive(Context e, Intent i) {
					context.startActivity(intent);
				}
			});
			return false;
		}else{
			context.startActivity(intent);
			return true;
		}
	}
	
	/**
	 * @breif Open chatting room activity of OpenTalkOn.
	 * @param context : Android context to open activity
	 * @param userIds : User ID of users to chat with
	 * @remark When chatting again with a previous user, you enter the previous chatting room.
	 * @return Success condition
	 */
	public static boolean startChatRoom(final Context context, ArrayList<Long> userIds){
		final Intent intent = new Intent(context, OTChatRoom.class);
		intent.putExtra("user_list", userIds);
		if(checkHasToken(true) == false){
			OTOApp.getInstance().addValidTokenDoneReceiver(new BroadcastReceiver() {
				@Override public void onReceive(Context e, Intent i) {
					context.startActivity(intent);
				}
			});
			return false;
		}else{
			context.startActivity(intent);
			return true;
		}
	}
	
	/**
	 * @breif Open chatting room activity of OpenTalkOn.
	 * @param context : Android context to open activity
	 * @param userId : User ID of users to chat with
	 * @remark When chatting again with a previous user, you enter the previous chatting room.
	 * @return Success condition
	 */
	public static boolean startChatRoom(Context context, Long userId){
		ArrayList<Long> users = new ArrayList<Long>();
		users.add(userId);
		users.add(OTOApp.getInstance().getId());
		return startChatRoom(context, users);
	}
	
	/**
	 * @breif Sets alert of new chatting message arrivals.
	 * @param value : Setting value
	 */
	public static void settingChatNotify(boolean value){
		OTOApp.getInstance().getPref().getSetting_chat_notifiy().setValue(value);
	}
	
	/**
	 * @breif Get valid image path.
	 * @param partialURL : Partial image URL to change
	 * @remark Used when changing imageURL in user information into normal image path.
	 * @return null when failed
	 * @return Valid image path when successful
	 */
	public static String makeImageUrl(String partialURL){
		return TASatelite.makeImageUrl(partialURL);
	}
}
