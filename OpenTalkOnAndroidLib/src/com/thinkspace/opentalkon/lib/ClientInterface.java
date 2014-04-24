package com.thinkspace.opentalkon.lib;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.OTChatRoom;
import com.thinkspace.opentalkon.ui.OTMain;

public class ClientInterface {
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
