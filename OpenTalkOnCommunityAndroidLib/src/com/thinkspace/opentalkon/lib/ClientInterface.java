package com.thinkspace.opentalkon.lib;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.OTChatRoom;
import com.thinkspace.opentalkon.ui.OTMain;
import com.thinkspace.opentalkon.ui.OTOpenTalkRoom;

public class ClientInterface {
	/**
	 * @breif Notification에 설정될 아이콘과 앱이름을 바꿉니다. \n
	 * 			  Notification은 메시지가 도착했을때, 누군가 내글을 좋아하거나 댓글을 달았을때 나타나게 됩니다. \n
	 * 			  따로 설정하지 않으면 OpenTalkOn의 기본 아이콘 및 이름으로 보여지게 됩니다.
	 * @param smallIcon : notification의 상태바에 보여지게될 Icon으로써, drawable의 ID를 넘기면 됩니다. 앱의 아이콘을 작게 만드신 후에 설정하면 가장 적당합니다.(38x38 사이즈 권장)
	 * @param bigIcon : notification의 내용에서 보여지게될 Icon으로써, drawable의 ID를 넘기면 됩니다. 앱의 아이콘을 설정하면 가장 적당합니다.
	 * @param appName : notification의 내용에서 보여지게될 사용하게될 앱이름 입니다.
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
	 * @breif 유저의 토큰을 가져옵니다.
	 * @remark 리턴값이 빈문자열("")일 경우 InitLibrary를 정상적으로 호출했는지 확인 주십시오.
	 * @return 유저 토큰 or 아직 정상적으로 토큰이 설정되지 않았을경우 빈문자열("")
	 */
	public static String getToken(){
		return OTOApp.getInstance().getToken();
	}
	
	/**
	 * @breif 유저의 ID를 가져옵니다.(대화나 유저정보를 가져올때 사용)
	 * @remark 리턴값이 -1일 경우 InitLibrary를 정상적으로 호출했는지 확인 주십시오.
	 * @return 유저 ID or 아직 정상적으로 아이디가 설정되지 않았을경우 -1
	 */
	public static long getId(){
		return OTOApp.getInstance().getId();
	}
	
	/**
	 * @breif 라이브러리를 초기화 합니다.
	 * @param Context context : 어플리케이션 컨텍스트
	 * @param String appToken : 오픈톡온 관리콘솔에서 확인한 appToken
	 */
	public static void InitLibrary(Context context, String appToken){
		OTOApp.getInstance().InitOpenTalkLib(context, appToken, null);
	}	
	
	/**
	 * @breif 라이브러리를 초기화 합니다.
	 * @param Context context : 어플리케이션 컨텍스트
	 * @param String appToken : 오픈톡온 관리콘솔에서 확인한 appToken
	 * @param BroadcastReceiver receiver : 토큰을 제대로 받아 왔는지 여부를 확인 할 수있는 Receiver
	 * @remark receiver로 전달되는 Intent는 getStringExtra("result")로 결과값을 확인 가능하며\n
	 * 				"ok"일 경우 정상인 것을 나타내며, 다른경우 에러 메시지를 가지고 있습니다.
	 */
	public static void InitLibrary(Context context, String appToken, BroadcastReceiver receiver){
		OTOApp.getInstance().InitOpenTalkLib(context, appToken, receiver);
	}
	
	/**
	 * @breif 사용자에게 토큰이 발급 되었는지 확인 합니다. 발급이 되어 있지 않은경우, 정상적으로 오픈톡온 API를 이용하실 수 없습니다. \n
	 * 			 발급이 되어 있지 않은경우, 자동으로 발급을 신청합니다.(몇초후 다시 시도했을경우 발급되어 있을 수 있음)
	 * @return 성공 여부
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
	 * @breif 사용자의 토큰을 가져옵니다.
	 * @return 사용자의 토큰
	 */
	public static String getUserToken(){
		return OTOApp.getInstance().getToken();
	}
	
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
	 * @breif 오픈톡온의 메인 Activity를 실행합니다.
	 * @param Context context : Activity를 실행할 안드로이드 Context
	 * @return 성공 여부
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
	 * @breif 오픈톡온의 오픈톡(커뮤니티) Activity를 실행합니다.
	 * @param Context context : Activity를 실행할 안드로이드 Context
	 * @param long community_id : 커뮤니티의 ID (이 값은 오픈톡 관리페이지 에서 확인 가능합니다.)
	 * @return 성공 여부
	 */
	public static boolean startOpenTalkOnCommunity(final Context context, long community_id){
		final Intent intent = new Intent(context, OTOpenTalkRoom.class);
		intent.putExtra("community_id", community_id);
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
	 * @breif 오픈톡온의 채팅방 Activity를 실행합니다.
	 * @param Context context : Activity를 실행할 안드로이드 Context
	 * @param ArrayList<Long> userIds : 대화를 나눌 상대들의 사용자 ID
	 * @remark 이미 해당 상대들과의 대화방이 열려 있으면 자동으로 이전의 대화방이 열린다.
	 * @return 성공 여부
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
	 * @breif 오픈톡온의 채팅방 Activity를 실행합니다.
	 * @param Context context : Activity를 실행할 안드로이드 Context
	 * @param Long userIds : 대화를 나눌 상대방의 사용자 ID
	 * @remark 이미 해당 상대와의 대화방이 열려 있으면 자동으로 이전의 대화방이 열린다.
	 * @return 성공 여부
	 */
	public static boolean startChatRoom(Context context, Long userId){
		ArrayList<Long> users = new ArrayList<Long>();
		users.add(userId);
		return startChatRoom(context, users);
	}
	
	/**
	 * @breif 새로운 대화 메시지가 도착 했을때 알릴지 여부를 설정합니다.
	 * @param boolean value : 설정 값
	 */
	public static void settingChatNotify(boolean value){
		OTOApp.getInstance().getPref().getSetting_chat_notifiy().setValue(value);
	}

	/**
	 * @breif 오픈톡에 새로운 글이 작성되었을때 알릴지 여부를 설정합니다.
	 * @boolean value : 설정 값
	 * @remark 사용자가 임의로 알림설정을 해놓은 오픈톡들의 알림을 전체 설정/해제 하는 용도로 사용됩니다.
	 */
	public static void settingOpenTalkNewNotify(boolean value){
		OTOApp.getInstance().getPref().getSetting_opentalk_new_post_notifiy().setValue(value);
	}
	
	/**
	 * @breif 내가작성한 오픈톡 글의 좋아요를 알릴지 여부를 설정합니다.
	 * @param boolean value : 설정 값
	 */
	public static void settingOpenTalkMyLikeNotify(boolean value){
		OTOApp.getInstance().getPref().getSetting_opentalk_my_like_notifiy().setValue(value);
	}
	
	/**
	 * @breif 내가작성한 오픈톡 글의 댓글을 알릴지 여부를 설정합니다.
	 * @param boolean value : 설정 값
	 */
	public static void settingOpenTalkMyReplyNotify(boolean value){
		OTOApp.getInstance().getPref().getSetting_opentalk_my_reply_notifiy().setValue(value);
	}
	
	/**
	 * @breif 내가 댓글을 작성한 글에 댓글이 달렸을때 알릴지 여부를 설정합니다.
	 * @param boolean value : 설정 값
	 */
	public static void settingOpenTalkReplyNotify(boolean value){
		OTOApp.getInstance().getPref().getSetting_opentalk_reply_notifiy().setValue(value);
	}
	
	/**
	 * @breif 정상적인 이미지 경로를 얻어 옵니다.
	 * @param String partialURL : 변환할 부분 이미지 URL
	 * @remark getFriends 등 사용자 정보에서의 imageURL을 정상적인 이미지 경로로 변환할때 사용합니다.
	 * @return 실패했을경우 null
	 * @return 성공했을경우 정상적인 이미지 경로
	 */
	public static String makeImageUrl(String partialURL){
		return TASatelite.makeImageUrl(partialURL);
	}
}
