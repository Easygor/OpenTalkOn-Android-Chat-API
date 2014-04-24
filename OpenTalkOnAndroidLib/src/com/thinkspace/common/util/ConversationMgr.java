package com.thinkspace.common.util;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.ui.OTChatRoom;

public class ConversationMgr {
	public final static int NOTIFICATION_MSG_ID = 0x112;
	public final static int NOTIFICATION_COMMUNITY_ID = 0x113;
	
	long lastNotifiedCommunityId;
	long lastNotifiedRoomId;
	long lastJoinedRoomId;
	boolean roomScreenOff;
	int talkNotificationCnt;
	Context context;
	Handler handler;
	
	int smallIcon;
	int bigIcon;
	String appName;
	
	Map<Long, Boolean> hasComActivity = new HashMap<Long, Boolean>();
	
	public ConversationMgr(Context context, Handler handler){
		this.context = context;
		this.handler = handler;
		this.smallIcon = -1;
		this.bigIcon = -1;
		this.appName = null;
	}
	
	public void setSmallIcon(int smallIcon) {
		this.smallIcon = smallIcon;
	}



	public void setBigIcon(int bigIcon) {
		this.bigIcon = bigIcon;
	}



	public void setAppName(String appName) {
		this.appName = appName;
	}



	public void clearCommunity(){
		hasComActivity.clear();
	}
	
	public void joinCommunity(long community_id){
		hasComActivity.put(community_id, true);
	}
	public void exitCommunity(long community_id){
		if(hasComActivity.containsKey(community_id)){
			hasComActivity.remove(community_id);
		}
	}
	
	public boolean hasCommunity(long community_id){
		if(hasComActivity.containsKey(community_id)){
			return true;
		}
		return false;
	}

	public boolean IsTalkConvForeground(){
		return OTOApp.getInstance().IsForeground(OTChatRoom.class.getSimpleName());
	}
	
	public void newMsgNotification(Long room_id, Long sender_id, String msg){
		if(OTOApp.getInstance().getPref().getSetting_chat_notifiy().getValue() == false) return;
		if(IsTalkConvForeground() && room_id.equals(getLastJoinedRoomId()) && roomScreenOff == false) return;
		
		Intent intent = null;
		intent = new Intent(context, OTChatRoom.class);
		intent.putExtra("room_id", room_id);
		intent.putExtra("notificationJoin", true);
		
		String userNick = TAUserNick.getInstance().getUserInfo(sender_id);
		setLastNotifiedRoomId(room_id);
		setupNotficationForMsgAnnounce(++talkNotificationCnt, context.getString(R.string.oto_notification_message_popup), userNick + " : " + msg,
				Notification.FLAG_AUTO_CANCEL, intent, NOTIFICATION_MSG_ID, true, OTOApp.getInstance().getCacheCtrl().getTalkAlarm(room_id));
	}
	
	public void cancelMsgNotification(){
		talkNotificationCnt = 0;
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_MSG_ID);
	}
	
	public void cancelCommunityNotification(){
		lastNotifiedCommunityId = -1;
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_COMMUNITY_ID);
	}
	
	public void setupNotficationMsg(String title, String msg, int flags, Intent intent, int notiy_id , boolean preCancel){
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(preCancel){
			notificationManager.cancel(notiy_id);
		}
        Notification notification = new Notification(R.drawable.oto_noti_icon, title , System.currentTimeMillis());
        Uri alertDefaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.sound = alertDefaultSound;
        notification.vibrate = new long[]{400,400,400,400};
        notification.flags = flags;
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        notification.setLatestEventInfo(context, title, msg, pendingIntent);
        notificationManager.notify(notiy_id, notification);
	}
	
	ViewGroup group = null;
	
	public void setupNotficationForMsgAnnounce(final int numberOfmsg, final String notifiyMsg , final String body, final int flags,
			final Intent intent, final int notiy_id , final boolean preCancel, final boolean alarm){
		if(group == null){
			handler.post(new Runnable() {
				@Override
				public void run() {
					_setupNotficationForMsgAnnounce(numberOfmsg, notifiyMsg, body, flags, intent, notiy_id, preCancel, alarm);
				}
			});
		}else{
			_setupNotficationForMsgAnnounce(numberOfmsg, notifiyMsg, body, flags, intent, notiy_id, preCancel, alarm);
		}
	}
	
	void _setupNotficationForMsgAnnounce(int numberOfmsg, String notifiyMsg , String body, int flags,
			Intent intent, int notiy_id , boolean preCancel, boolean alarm){
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if(preCancel){
			notificationManager.cancel(notiy_id);
		}
		int notiIcon = R.drawable.oto_noti_icon;
		if(smallIcon != -1){
			notiIcon = smallIcon;
		}
        Notification notification = new Notification(notiIcon, notifiyMsg, System.currentTimeMillis());
        Uri alertDefaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        if(alarm){
	        notification.sound = alertDefaultSound;
	        notification.vibrate = new long[]{400,400,400,400};
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notification.flags = flags;
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_CANCEL_CURRENT );
        notification.setLatestEventInfo(context,"", "", pendingIntent);
        
        if(group == null){
        	group = (ViewGroup) View.inflate(context, notification.contentView.getLayoutId(), null);
        }
        Pair<Integer, Float> titleArrtib = getTextColorAndSize(group, notification, 0x1020016);
        Pair<Integer, Float> bodyArrtib = getTextColorAndSize(group, notification, 0x1020048);
        Pair<Integer, Float> timeArrtib = getTextColorAndSize(group, notification, 0x1020066);
        
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.ot_notification_msg);
        
        if(titleArrtib != null) notification.contentView.setTextColor(R.id.oto_notification_msg_title, titleArrtib.first);
        if(bodyArrtib != null) notification.contentView.setTextColor(R.id.oto_notification_msg_body, bodyArrtib.first);
        if(timeArrtib != null) notification.contentView.setTextColor(R.id.oto_notification_msg_time, timeArrtib.first);
        
        if(bigIcon != -1){
        	notification.contentView.setImageViewResource(R.id.oto_notification_msg_img, bigIcon);
        }
        
        if(smallIcon != -1){
        	notification.contentView.setImageViewResource(R.id.oto_notification_msg_small_icon, smallIcon);
        }
        
        if(appName != null){
        	notification.contentView.setTextViewText(R.id.oto_notification_msg_title, appName);
        }else{
        	notification.contentView.setTextViewText(R.id.oto_notification_msg_title, context.getString(R.string.oto_lib_name));
        }
        notification.contentView.setTextViewText(R.id.oto_notification_msg_body, body);
        notification.contentView.setTextViewText(R.id.oto_notification_msg_time, PLEtcUtilMgr.getDefaultDateFormat(System.currentTimeMillis()));
        if(numberOfmsg == 0){
        	notification.contentView.setViewVisibility(R.id.oto_notification_msg_cnt, View.GONE);
        }else{
        	 if(timeArrtib != null) notification.contentView.setTextColor(R.id.oto_notification_msg_cnt, timeArrtib.first);
        	notification.contentView.setTextViewText(R.id.oto_notification_msg_cnt, String.valueOf(numberOfmsg));
        	notification.contentView.setViewVisibility(R.id.oto_notification_msg_cnt, View.VISIBLE);
        }
	        
        notificationManager.notify(notiy_id, notification);
	}
	
	
	Pair<Integer, Float> getTextColorAndSize(View view, Notification notification, int res_id){
		if(view instanceof TextView){
			if(view.getId() == res_id){
				return Pair.create(((TextView) view).getTextColors().getDefaultColor() , ((TextView) view).getTextSize());
			}
		}
		if(view instanceof ViewGroup){
			ViewGroup vg = (ViewGroup) view;
			for(int i=0;i<vg.getChildCount();++i){
				Pair<Integer, Float> val = getTextColorAndSize(vg.getChildAt(i), notification, res_id); 
				if(val != null){
					return val;
				}
			}
		}
		return null;
	}
	
	public void setJoinWithNotification(){
		talkNotificationCnt = 0;
	}
	public long getLastNotifiedCommunityId() {
		return lastNotifiedCommunityId;
	}
	public void setLastNotifiedCommunityId(long lastNotifiedCommunityId) {
		this.lastNotifiedCommunityId = lastNotifiedCommunityId;
	}
	public long getLastNotifiedRoomId() {
		return lastNotifiedRoomId;
	}
	public void setLastNotifiedRoomId(long lastNotifiedRoomId) {
		this.lastNotifiedRoomId = lastNotifiedRoomId;
	}
	public long getLastJoinedRoomId() {
		return lastJoinedRoomId;
	}
	public void setLastJoinedRoomId(long lastJoinedRoomId) {
		this.lastJoinedRoomId = lastJoinedRoomId;
	}
	public boolean isRoomScreenOff() {
		return roomScreenOff;
	}
	public void setRoomScreenOff(boolean roomScreenOff) {
		this.roomScreenOff = roomScreenOff;
	}
	public int getTalkNotificationCnt() {
		return talkNotificationCnt;
	}
	public void setTalkNotificationCnt(int talkNotificationCnt) {
		this.talkNotificationCnt = talkNotificationCnt;
	}
		
}
