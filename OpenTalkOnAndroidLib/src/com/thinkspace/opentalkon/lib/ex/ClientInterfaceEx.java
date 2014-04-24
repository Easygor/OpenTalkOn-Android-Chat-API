package com.thinkspace.opentalkon.lib.ex;

import java.util.ArrayList;
import java.util.List;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.TAImgMsgData;
import com.thinkspace.opentalkon.data.TASetUserInfoData;
import com.thinkspace.opentalkon.lib.ClientInterface;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;

public class ClientInterfaceEx {
	
	/**
	 * @breif 메시지를 받기 위해 핸들러를 등록합니다.
	 * @param MessageHandler handler : 핸들러
	 */
	public static void setClientInterfaceMsgHandler(MessageHandler handler){
		OTOApp.getInstance().getPushClient().setClientInterfaceMsgHandler(handler);
	}
	
	/**
	 * @breif 친구를 추가 합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 반환 합니다.
	 * @param long userId : 친구 추가를 원하는 사용자의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : addfriend\n
	 * @return 성공 여부
	 */
	public static boolean doAddFriend(TADataHandler handler, long userId){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doAddFriend(OTOApp.getInstance().getToken(), userId);
		return true;
	}
	
	/**
	 * @breif 사용자의 친구를 가져 옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONArray\n
	 * 				JSONArray 각각의 요소\n
	 *              JSONObject {"id":사용자 ID,"nick_name":닉네임,"locale":언어,"introduce":자기소개,\n
	 * 				"image_path":소개 이미지,"app_code":앱코드,"friend_best":즐겨찾기여부,"friend":친구여부}\n
	 *              image_path의 경로의 경우 makeImageUrl(String addr) 를 통해 정상적인 경로를 얻을 수 있다.\n
	 *              location : getfriends\n
	 * @return 성공 여부
	 */
	public static boolean doGetFriends(TADataHandler handler){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetFriends(OTOApp.getInstance().getToken());
		return true;
	}
	
	/**
	 * @breif 사용자의 정보를 가져옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param long userId : 요청할 사용자 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject {"id":사용자 ID,"nick_name":닉네임,"locale":언어,"introduce":자기소개,\n
	 * 				"image_path":소개 이미지,"app_code":앱코드,"friend_best":즐겨찾기여부,"friend":친구여부}\n
	 *              image_path의 경로의 경우 makeImageUrl(String addr) 를 통해 정상적인 경로를 얻을 수 있다.\n
	 *              location : getuserinfo\n
	 * @return 성공 여부
	 */
	public static boolean doGetUserInfo(TADataHandler handler, long userId){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfo(OTOApp.getInstance().getToken(), userId);
		return true;
	}
	
	/**
	 * @breif 사용자들의 정보를 가져옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param List<Long> Ids : 요청할 사용자들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONArray\n
	 *             	JSONArray 각각의 요소\n
	 *              JSONObject {"id":사용자 ID,"nick_name":닉네임,"locale":언어,"introduce":자기소개,\n
	 *              "image_path":소개 이미지,"app_code":앱코드,"friend_best":즐겨찾기여부,"friend":친구여부}\n
	 *              image_path의 경로의 경우 makeImageUrl(String addr) 를 통해 정상적인 경로를 얻을 수 있다.\n
	 *              location : getuserinfos\n
	 * @return 성공 여부
	 */
	public static boolean doGetUserInfos(TADataHandler handler, List<Long> Ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfos(OTOApp.getInstance().getToken(), Ids);
		return true;
	}
	
	/**
	 * @breif 채팅방의 메시지들의 안읽은 사람 숫자가 바뀔경우의 변화 내용을 받을 것인지 설정합니다.\n
	 *           변화가 생길경우 setClientInterfaceMsgHandler에서 설정한 핸들러로 콜백됩니다.\n
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param long room_id : 알림설정할 채팅방의 ID
	 * @param boolean feed : 알림 여부
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : setmsgfeed\n
	 * @return 성공 여부
	 */
	public static boolean doSetMsgFeed(TADataHandler handler, long room_id, boolean feed){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetMsgFeed(OTOApp.getInstance().getToken(), room_id, feed);
		return true;
	}
	
	/**
	 * @breif 자신이 참가중인 채팅방에 사용자들을 초대 합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param long room_id : 사용자를 초대할 채팅방의 ID(초대를 하는 사용자는 이미 채팅방에 참여중이여야 합니다. 그렇지 않으면 요청이 실패됩니다.)
	 * @param List<Long> user_ids : 초대할 사용자들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : inviteroom\n
	 * @return 성공 여부
	 */
	public static boolean doInviteRoom(TADataHandler handler, long room_id, List<Long> user_ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doInviteRoom(OTOApp.getInstance().getToken(), room_id, user_ids);
		return true;
	}
	
	/**
	 * @breif 자신의 친구를 즐겨찾기에 추가/삭제 합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param boolean friend_flag : 즐겨찾기 여부(true : 추가, false : 삭제)
	 * @param long friend_id : 즐겨찾기 설정할 친구의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : setbestfriendflag\n
	 * @return 성공 여부
	 */
	public static boolean doSetBestFriendFlag(TADataHandler handler, boolean friend_flag, long friend_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetBestFriendFlag(OTOApp.getInstance().getToken(), friend_flag, friend_id);
		return true;	
	}
	
	/**
	 * @breif 자신이 참가 중인 채팅방을 나갑니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param long room_id : 채팅방 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : exitroom\n
	 * @return 성공 여부
	 */
	public static boolean doExitRoom(TADataHandler handler, long room_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doExitRoom(OTOApp.getInstance().getToken(), room_id);
		return true;	
	}
	
	/**
	 * @breif 메시지를 '읽음'으로 설정합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param Long msg_id : 1메시지 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : setreadmsg\n
	 * @return 성공 여부
	 */
	public static boolean doSetReceivedMsg(TADataHandler handler, Long msg_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetReceivedMsg(OTOApp.getInstance().getToken(), msg_id);
		return true;
	}
	
	/**
	 * @breif 못받았던 메시지들을 받았다고 알립니다. (doGetUnReceivedMsg호출후에 사용하는 용도 입니다.)
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param ArrayList<Long> msgs : 메시지들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : 없음\n
	 *              location : setreadmsg\n
	 * @return 성공 여부
	 */
	public static boolean doSetReceivedMsg(TADataHandler handler, ArrayList<Long> msgs){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetReceivedMsg(OTOApp.getInstance().getToken(), msgs);
		return true;
	}
	
	/**
	 * @breif 못받았던 메시지를 받습니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param ArrayList<Long> msgs : 메시지들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONArray\n
	 *             	JSONArray 각각의 요소\n
	 *             	JSONObject {"id":메시지ID,"sender_id":보낸 사용자 ID,"sender_nickname":보낸 사용자 닉네임, "msg":메시지, "send_time":보낸시간,"room_id":채팅방ID}\n
	 *              location : getunreadmsg\n
	 * @return 성공 여부
	 */
	public static boolean doGetUnReceivedMsg(TADataHandler handler){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUnReceivedMsg(OTOApp.getInstance().getToken());
		return true;
	}
	
	/**
	 * @breif 채팅방에 참가중인 유저들을 가져옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param Long room_id : 채팅방의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONArray\n
	 *             	JSONArray 각각의 요소\n
	 *              Long(사용자의 ID)\n
	 *              location : getroomusers\n
	 * @return 성공 여부
	 */
	public static boolean doGetRoomUsers(TADataHandler handler, Long room_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetRoomUsers(OTOApp.getInstance().getToken(), room_id);
		return true;
	}
	
	/**
	 * @breif 해당 사용자들로 이미 만들어진 채팅방을 가져오거나, 채팅방을 새로 만듭니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param List<Long> users : 사용자들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject{room_id: 채팅방의 ID, users: 채팅 참가자들}\n
	 *              location : getormakeroom\n
	 * @return 성공 여부
	 */
	public static boolean doGetOrMakeRoom(TADataHandler handler, List<Long> users){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetOrMakeRoom(OTOApp.getInstance().getToken(), users);
		return true;
	}
	
	/**
	 * @breif 사용자의 정보를 가져옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param Long id : 요청할 사용자의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject {"id":사용자 ID,"nick_name":닉네임,"locale":언어,"introduce":자기소개,\n
	 *             "image_path":소개 이미지,"app_code":앱코드,"friend_best":즐겨찾기여부,"friend":친구여부}\n
	 *              image_path의 경로의 경우 makeImageUrl(String addr) 를 통해 정상적인 경로를 얻을 수 있다.\n
	 *              location : getuserinfo\n
	 * @return 성공 여부
	 */
	public static boolean doGetUserInfo(TADataHandler handler, Long id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfo(OTOApp.getInstance().getToken(), id);
		return true;
	}
	
	/**
	 * @breif 사용자의 정보를 가져옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param String nick_name : 요청할 사용자의 닉네임
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject {"id":사용자 ID,"nick_name":닉네임,"locale":언어,"introduce":자기소개,\n
	 *             "image_path":소개 이미지,"app_code":앱코드,"friend_best":즐겨찾기여부,"friend":친구여부}\n
	 *              image_path의 경로의 경우 makeImageUrl(String addr) 를 통해 정상적인 경로를 얻을 수 있다.\n
	 *              location : getuserinfobynick\n
	 * @return 성공 여부
	 */
	public static boolean doGetUserInfoByNick(TADataHandler handler, String nick_name){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfoByNick(OTOApp.getInstance().getToken(), nick_name);
		return true;
	}
	
	/**
	 * @breif 친구를 삭제합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param long user_id : 삭제할 친구의 사용자 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : (없음)\n
	 *              location : delfriend\n
	 * @return 성공 여부
	 */
	public static boolean doDelFriend(TADataHandler handler, long user_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doDelFriend(OTOApp.getInstance().getToken(), user_id);
		return true;
	}
	
	/**
	 * @breif 메시지를 보냅니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param Long room_id : 메시지를 보낼 채팅방의 ID
	 * @param String msg : 메시지
	 * @param Long transact_id : 메시지 관리 숫자(서버에서 메시지 전송 성공 했을때, 응답 데이터에 이 숫자가 포함되서 옵니다.)
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject {msg_id: 메시지의 ID, transact_id: 요청당시 보낸 메시지 관리 숫자, send_time: 보낸 시간}\n
	 *              location : sendmessage\n
	 * @return 성공 여부
	 */
	public static boolean doSendMessage(TADataHandler handler, Long room_id, String msg, Long transact_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSendMessage(OTOApp.getInstance().getToken(), room_id, msg, transact_id);
		return true;
	}
	
	/**
	 * @breif 메시지를 '읽음'으로 설정 합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param ArrayList<Long> msg_Ids : '읽음' 으로 설정할 메시지들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONArray\n
	 * 				JSONArray 각각의 요소\n
	 * 				JSONObject { 메시지 ID, true } 반복\n
	 *              location : setreceivedmsgstate\n
	 * @return 성공 여부
	 */
	public static boolean doPostReadMsg(TADataHandler handler, ArrayList<Long> msg_Ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doPostReadMsg(OTOApp.getInstance().getToken(), msg_Ids);
		return true;
	}
	
	/**
	 * @breif 메시지들의 안읽은 사람수를 가져옵니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param ArrayList<Long> msg_Ids : 안읽은 사람수를 가져올 메시지들의 ID
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONArray\n
	 * 				JSONArray 각각의 요소\n
	 * 				JSONObject { 메시지 ID, 안읽은 사람 수 } 반복\n
	 *              location : getsendedmsgstate\n
	 * @return 성공 여부
	 */
	public static boolean doGetReadMsg(TADataHandler handler, ArrayList<Long> msg_Ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetReadMsg(OTOApp.getInstance().getToken(), msg_Ids);
		return true;
	}
	
	/**
	 * @breif 사용자 정보를 설정합니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param String nick_name : 사용자 닉네임
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject {"id":사용자 ID,"nick_name":닉네임,"locale":언어,"introduce":자기소개,\n
	 * 				"image_path":소개 이미지,"app_code":앱코드,"friend_best":즐겨찾기여부,"friend":친구여부}\n
	 *              image_path의 경로의 경우 makeImageUrl(String addr) 를 통해 정상적인 경로를 얻을 수 있다.\n
	 *              location : setuserinfo\n
	 * @return 성공 여부
	 */
	public static boolean doSetUserNickName(TADataHandler handler, String nick_name){
		if(ClientInterface.checkHasToken(true) == false) return false;
		TASetUserInfoData userData = new TASetUserInfoData();
		userData.token = OTOApp.getInstance().getToken();
		userData.nick_name = nick_name;
		new TASatelite(handler).doSetUserInfo(userData);
		return true;
	}
	
	/**
	 * @breif 이미지가 포함된 메시지를 보냅니다.
	 * @param TADataHandler handler : 친구를 추가한후 CallBack을 받기위한 핸들러 입니다. 성공이나 실패를 CallBack 합니다.
	 * @param Long room_id : 메시지를 보낼 채팅방의 ID
	 * @param String msg : 메시지
	 * @param List<String> imgPaths : 이미지 파일들의 경로
	 * @param Long transact_id : 메시지 관리 숫자(서버에서 메시지 전송 성공 했을때, 응답 데이터에 이 숫자가 포함되서 옵니다.)
	 * @remark (▼서비스 API 공통 사항)\n
	 * 				클라이언트의 요청이 정상적으로 전달되고, 서버에서 응답을 했을 경우,\n
	 *              TADataHandler의 onHttpPacketReceived(JSONObject data)가 호출 됩니다.\n
	 *              인자 설명\n
	 *              JSONObject data : \n
	 *              {\n
	 *              "state":상태 메시지 (요청 성공 일경우 "ok", 사용자 접속자수 제한에 걸렸을때 "limit_max_user", 그외에는 실패 이유),\n
	 *              "data":데이터,\n
	 *              "location": 호출한 서비스 API 이름\n
	 *              }\n
	 *              하지만, 요청이 실패 되고 예외가 발생되었을경우\n
	 *              TADataHandler의 onHttpException(Exception ex, JSONObject data, String addr)\n
	 *              or onHttpException(Exception ex, TAMultiData data, String addr)가 호출 됩니다.\n
	 *              인자 설명
	 *              Exception ex : 발생된 예외\n
	 *              JSONObject data or TAMultiData data : 요청했던 데이터\n
	 *              String addr : 호출한 서비스 API 이름\n
	 *              \n
	 *              (▼응답 데이터)\n
	 * @remark data : JSONObject {msg_id: 메시지의 ID, transact_id: 요청당시 보낸 메시지 관리 숫자, send_time: 보낸 시간, img_url: 메시지 경로, msg: 메시지}\n
	 *              location : sendimgmsg\n
	 * @return 성공 여부
	 */
	public static boolean doSendImgMsg(TADataHandler handler, Long room_id, String msg, List<String> imgPaths, Long transact_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		TAImgMsgData msgData = new TAImgMsgData();
		msgData.setToken(OTOApp.getInstance().getToken());
		msgData.setSender_id(OTOApp.getInstance().getId());
		msgData.setTransact_id(String.valueOf(transact_id));
		msgData.setRoom_id(room_id);
		msgData.setMsg(msg);
		msgData.setImagePaths(imgPaths);
		new TASatelite(handler).doSendImgMsg(msgData);
		return true;
	}
}
