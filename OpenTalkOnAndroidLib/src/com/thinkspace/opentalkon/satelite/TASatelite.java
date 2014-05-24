package com.thinkspace.opentalkon.satelite;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thinkspace.common.satelite.PLHttpDataHandler;
import com.thinkspace.common.satelite.PLHttpSatelite;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.TAImgMsgData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TASetUserInfoData;

public class TASatelite extends PLHttpSatelite implements PLHttpDataHandler{
	TADataHandler handler;
	
	public static String BASE_URL;
	public static String GET_FRIENDS_URL;
	public static String GET_REVERSE_FRIENDS_URL;
	public static String GET_SENDED_MSG_STATE_URL;
	public static String GET_USER_INFO_URL;
	public static String GET_USER_INFO_BY_NICK_URL;
	public static String GET_USER_INFOS_URL;
	public static String GET_OR_MAKE_ROOM_URL;
	public static String GET_ROOM_USERS_URL;
	public static String GET_UNRECEIVED_MSG_URL;
	public static String GET_MORE_APP_LIST;
	public static String SET_RECEIVED_MSG_STATE_URL;
	public static String SET_USER_INFO_URL;
	public static String SET_RECEIVED_MSG_URL;
	public static String SET_MSG_FEED_URL;
	public static String SET_BEST_FRIEND_FLAG;
	public static String SEND_IMG_MSG_URL;
	public static String SEND_MSG_URL;
	public static String ADD_FRIEND_URL;
	public static String DEL_FRIEND_URL;
	public static String REGISTER_URL;
	public static String EXIT_ROOM_URL;
	public static String INVITE_ROOM_URL;
	public static String LOGIN_FACEBOOK;
	public static String REPORT_ERROR;
	public static String SET_UESR_OPTION;
	public static String CHECK_ADMIN;
	public static String GET_FACEBOOK_PUBLIC;
	public static String SET_FACEBOOK_PUBLIC;
	public static String APPLY_VERIFY_NUMBER;
	public static String SEND_VERIFY_NUMBER;
	public static String SET_USER_CONTACTS;
	
	public static void setupSateliteHost(String BASE_URL){
		TASatelite.BASE_URL = BASE_URL;
		TASatelite.GET_FRIENDS_URL = BASE_URL + "getfriends";
		TASatelite.GET_REVERSE_FRIENDS_URL = BASE_URL + "getreversefriends";
		TASatelite.GET_SENDED_MSG_STATE_URL = BASE_URL + "getsendedmsgstate";
		TASatelite.GET_USER_INFO_URL = BASE_URL + "getuserinfo";
		TASatelite.GET_USER_INFO_BY_NICK_URL = BASE_URL + "getuserinfobynick";
		TASatelite.GET_USER_INFOS_URL = BASE_URL + "getuserinfos";
		TASatelite.GET_OR_MAKE_ROOM_URL = BASE_URL + "getormakeroom";
		TASatelite.GET_ROOM_USERS_URL = BASE_URL + "getroomusers";
		TASatelite.GET_UNRECEIVED_MSG_URL = BASE_URL + "getunreadmsg";
		TASatelite.GET_MORE_APP_LIST = BASE_URL + "getmoreapplist";
		TASatelite.SET_RECEIVED_MSG_STATE_URL = BASE_URL + "setreceivedmsgstate";
		TASatelite.SET_USER_INFO_URL = BASE_URL + "setuserinfo";
		TASatelite.SET_RECEIVED_MSG_URL = BASE_URL + "setreadmsg";
		TASatelite.SET_MSG_FEED_URL = BASE_URL + "setmsgfeed";
		TASatelite.SET_BEST_FRIEND_FLAG = BASE_URL + "setbestfriendflag";
		TASatelite.SEND_IMG_MSG_URL = BASE_URL + "sendimgmsg";
		TASatelite.SEND_MSG_URL = BASE_URL + "sendmessage";
		TASatelite.ADD_FRIEND_URL = BASE_URL + "addfriend";
		TASatelite.DEL_FRIEND_URL = BASE_URL + "delfriend";
		TASatelite.REGISTER_URL = BASE_URL + "register";
		TASatelite.EXIT_ROOM_URL = BASE_URL + "exitroom";
		TASatelite.INVITE_ROOM_URL = BASE_URL + "inviteroom";
		TASatelite.LOGIN_FACEBOOK = BASE_URL + "loginfacebook";
		TASatelite.REPORT_ERROR = BASE_URL + "reporterror";
		TASatelite.SET_UESR_OPTION = BASE_URL + "setuseroption";
		TASatelite.CHECK_ADMIN = BASE_URL + "checkadmin";
		TASatelite.GET_FACEBOOK_PUBLIC = BASE_URL + "getfacebookpublic";
		TASatelite.SET_FACEBOOK_PUBLIC = BASE_URL + "setfacebookpublic";
		TASatelite.APPLY_VERIFY_NUMBER = BASE_URL + "applyverifynumber";
		TASatelite.SEND_VERIFY_NUMBER = BASE_URL + "sendverifynumber";
		TASatelite.SET_USER_CONTACTS = BASE_URL + "setusercontacts";
	}

	public void doApplyVerifyNumber(String token, String phone_number, String verify_number, JSONArray contacts){
		setAddr(APPLY_VERIFY_NUMBER);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("phone_number", phone_number);
			data.put("verify_number", verify_number);
			data.put("contacts", contacts);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSendVerifyNumber(String phone_number){
		setAddr(SEND_VERIFY_NUMBER);
		JSONObject data = new JSONObject();
		try {
			data.put("phone_number", phone_number);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetContacts(String token, JSONArray contacts){
		setAddr(SET_USER_CONTACTS);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("contacts", contacts);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetFacebookPublic(String token){
		setAddr(GET_FACEBOOK_PUBLIC);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetFacebookPublic(String token, boolean setting){
		setAddr(SET_FACEBOOK_PUBLIC);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("setting", setting);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}

	public void doCheckAdmin(String token, long user_id){
		setAddr(CHECK_ADMIN);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("user_id", user_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetUserOption(String token, String option, boolean flag){
		setAddr(SET_UESR_OPTION);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("option", option);
			data.put("flag", flag);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}

	
	public void doReportError(String unique_key, String errormsg){
		setAddr(REPORT_ERROR);
		JSONObject data = new JSONObject();
		try {
			data.put("unique_key", unique_key);
			data.put("errormsg", errormsg);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void loginFacebook(String token, String facebookId){
		setAddr(LOGIN_FACEBOOK);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("facebookId", facebookId);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetMoreAppList(String token, int category){
		setAddr(GET_MORE_APP_LIST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("category", category);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetUserInfos(String token, List<Long> Ids){
		setAddr(GET_USER_INFOS_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			JSONArray arr = new JSONArray();
			for(Long id : Ids){
				arr.put(id);
			}
			data.put("ids", arr);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetMsgFeed(String token, long room_id, boolean feed){
		setAddr(SET_MSG_FEED_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			data.put("feed", feed);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doInviteRoom(String token, long room_id, List<Long> user_ids){
		setAddr(INVITE_ROOM_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			JSONArray arr = new JSONArray();
			for(Long id : user_ids){
				arr.put(id);
			}
			data.put("user_ids", arr);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetBestFriendFlag(String token, boolean friend_flag, long friend_id){
		setAddr(SET_BEST_FRIEND_FLAG);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("friend_flag", friend_flag);
			data.put("friend_id", friend_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}		
	}
	
	public void doExitRoom(String token, long room_id){
		setAddr(EXIT_ROOM_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}		
	}
	
	public void doSetReceivedMsg(String token, Long msg_id){
		setAddr(SET_RECEIVED_MSG_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			JSONArray arr = new JSONArray();
			arr.put(msg_id);
			data.put("msgs", arr);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetReceivedMsg(String token, ArrayList<Long> msgs){
		setAddr(SET_RECEIVED_MSG_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			JSONArray arr = new JSONArray();
			for(Long msg_id : msgs)
				arr.put(msg_id);
			data.put("msgs", arr);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetUnReceivedMsg(String token){
		setAddr(GET_UNRECEIVED_MSG_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetRoomUsers(String token, Long room_id){
		setAddr(GET_ROOM_USERS_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetOrMakeRoom(String token, List<Long> users){
		setAddr(GET_OR_MAKE_ROOM_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			JSONArray arr = new JSONArray();
			for(Long id : users) arr.put(id);
			data.put("users", arr);
					
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetUserInfo(String token, Long id){
		setAddr(GET_USER_INFO_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("id", id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetUserInfoByNick(String token, String nick_name){
		setAddr(GET_USER_INFO_BY_NICK_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("nick_name", nick_name);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetUserInfo(TASetUserInfoData data){
		setAddr(SET_USER_INFO_URL);
		sendMultiData(data);
	}
	
	public void doDelFriend(String token, long user_id){
		setAddr(DEL_FRIEND_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("user_id", user_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doAddFriend(String token, long id){
		setAddr(ADD_FRIEND_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("id", id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetFriends(String token){
		setAddr(GET_FRIENDS_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetReverseFriends(String token){
		setAddr(GET_REVERSE_FRIENDS_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public boolean doRegister(String unique_key, String locale, Integer last_version, String app_token){
		setAddr(REGISTER_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("unique_key", unique_key);
			data.put("locale", locale);
			data.put("last_version", last_version);
			data.put("app_token", app_token);
			sendData(data.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void doSendImgMsg(TAImgMsgData data){
		setAddr(SEND_IMG_MSG_URL);
		sendMultiData(data);
	}
	
	public void doSendMessage(String token, Long room_id, String msg, Long transact_id){
		setAddr(SEND_MSG_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			data.put("msg", msg);
			data.put("transact_id", transact_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doPostReadMsg(String token, ArrayList<Long> msg_Ids){
		setAddr(SET_RECEIVED_MSG_STATE_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			JSONArray arr = new JSONArray();
			for(Long msg_id : msg_Ids){
				arr.put(msg_id);
			}
			data.put("msg_id", arr);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetReadMsg(String token, ArrayList<Long> msg_Ids){
		setAddr(GET_SENDED_MSG_STATE_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			JSONArray arr = new JSONArray();
			for(Long msg_id : msg_Ids){
				arr.put(msg_id);
			}
			data.put("msg_id", arr);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public static String getName(String url){
		for(int i=url.length()-1;i>=0;--i){
			if(url.charAt(i) == '/'){
				return url.substring(i+1);
			}
		}
		return "";
	}
	public static String dispatchURL(String url){
		return url.replace("%port%", "9000");
	}
	
	@Override
	public void setAddr(String addr) {
		super.setAddr(dispatchURL(addr));
	}
	
	public TASatelite(TADataHandler handler){
		super(true);
		this.handler = handler;
		setHandler(this);
		setTaskMgr(OTOApp.getInstance().getTaskMgr());
	}
	
	public TASatelite(TADataHandler handler, boolean makeHandler){
		super(makeHandler);
		this.handler = handler;
		setHandler(this);
		setTaskMgr(OTOApp.getInstance().getTaskMgr());
	}

	@Override
	public void onHttpError(Exception ex, String data, String addr) {
		try {
			if(handler != null){
				handler.onHttpException(ex, new JSONObject(data), addr);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onHttpError(Exception ex, TAMultiData data, String addr) {
		if(handler != null){
			handler.onHttpException(ex, data, addr);
		}
	}

	@Override
	public void onHttpDataReceived(String data) {
		try {
			if(handler != null){
				JSONObject json = new JSONObject(data);
				String state = json.getString("state");
				if(state.equals("token is not valid")){
					boolean hasHandler = getUIHandler() != null;
					OTOApp.getInstance().InitializeAuth(hasHandler);
					handler.onTokenIsNotValid(json);
				}else if(state.equals("limit_max_user")){
					handler.onLimitMaxUser(json);
				}else{
					handler.onHttpPacketReceived(json);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMultiData(TAMultiData data){
		super.sendMultiData(data);
	}
	
	public void sendData(String data) throws JSONException{
		JSONObject json = new JSONObject(data);
		json.put("package_version", OTOApp.getInstance().getVersionCode());
		super.sendData(json.toString(), true);
	}
	
	public static String makeImageUrl(String partialURL){
		try {
			String utfURL = URLEncoder.encode(partialURL,"utf-8");
			return TASatelite.dispatchURL(BASE_URL) + "img/public/" + utfURL;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static String makeCommonImageUrl(String partialURL){
		try {
			String utfURL = URLEncoder.encode(partialURL,"utf-8");
			return TASatelite.dispatchURL(BASE_URL) + "commonimg/" + utfURL;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}
