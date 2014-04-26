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
import com.thinkspace.opentalkon.data.OTImgPostData;
import com.thinkspace.opentalkon.data.TAImgMsgData;
import com.thinkspace.opentalkon.data.TAMakePublicChatData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TASetUserInfoData;

public class TASatelite extends PLHttpSatelite implements PLHttpDataHandler{
	TADataHandler handler;
	
	public static String BASE_URL;
	public static String GET_FRIENDS_URL;
	public static String GET_SENDED_MSG_STATE_URL;
	public static String GET_USER_INFO_URL;
	public static String GET_USER_INFO_BY_NICK_URL;
	public static String GET_USER_INFOS_URL;
	public static String GET_OR_MAKE_ROOM_URL;
	public static String GET_ROOM_USERS_URL;
	public static String GET_UNRECEIVED_MSG_URL;
	public static String GET_COMMUNITY_POSTS;
	public static String GET_COMMUNITY_POST;
	public static String GET_COMMUNITY_LIST;
	public static String GET_COMMUNITY_DETAIL;
	public static String GET_COMMUNITY_MY_REPLY_URL;
	public static String GET_COMMUNITY_BEST;
	public static String GET_MORE_APP_LIST;
	public static String SET_COMMUNITY_DETAIL_FEED;
	public static String SET_RECEIVED_MSG_STATE_URL;
	public static String SET_USER_INFO_URL;
	public static String SET_RECEIVED_MSG_URL;
	public static String SET_MSG_FEED_URL;
	public static String SET_BEST_FRIEND_FLAG;
	public static String SET_COMMUNITY_FEED;
	public static String SET_COMMUNITY_ALARM;
	public static String SEND_IMG_MSG_URL;
	public static String SEND_MSG_URL;
	public static String POST_COMMUNITY;
	public static String POST_IMG_COMMUNITY;
	public static String ADD_FRIEND_URL;
	public static String DEL_FRIEND_URL;
	public static String DEL_COMMUNITY_REPLY;
	public static String DEL_COMMUNITY_POST;
	public static String REGISTER_URL;
	public static String EXIT_ROOM_URL;
	public static String INVITE_ROOM_URL;
	public static String REPLY_COMMUNITY;
	public static String LIKE_COMMUNITY;
	public static String GET_COMMUNITY_LIKE_PEOPLES;
	public static String LOGIN_FACEBOOK;
	public static String GET_FACEBOOK_PUBLIC;
	public static String SET_FACEBOOK_PUBLIC;
	public static String REPORT_ERROR;
	public static String MAKE_PUBLIC_CHAT;
	public static String GET_PUBLIC_CHATS;
	public static String ENTER_PUBLIC_CHAT;
	public static String GET_PUBLIC_ROOM_INFO;
	public static String GET_MY_PUBLIC_ROOMS;
	public static String SET_PUBLIC_ROOM_HIDDEN;
	public static String GIVE_PUBLIC_ROOM_BJ;
	public static String KICK_PUBLIC_ROOM_USER;
	public static String SET_UESR_OPTION;
	public static String CHECK_ADMIN;
	public static String SEARCH_PUBLIC_ROOM;
	public static String GET_SUB_COMMUNITY_LIST;
	public static String GET_COMMUNITY_RECENT_LIST;
	
	public static void setupSateliteHost(String BASE_URL){
		TASatelite.BASE_URL = BASE_URL;
		TASatelite.GET_FRIENDS_URL = BASE_URL + "getfriends";
		TASatelite.GET_SENDED_MSG_STATE_URL = BASE_URL + "getsendedmsgstate";
		TASatelite.GET_USER_INFO_URL = BASE_URL + "getuserinfo";
		TASatelite.GET_USER_INFO_BY_NICK_URL = BASE_URL + "getuserinfobynick";
		TASatelite.GET_USER_INFOS_URL = BASE_URL + "getuserinfos";
		TASatelite.GET_OR_MAKE_ROOM_URL = BASE_URL + "getormakeroom";
		TASatelite.GET_ROOM_USERS_URL = BASE_URL + "getroomusers";
		TASatelite.GET_UNRECEIVED_MSG_URL = BASE_URL + "getunreadmsg";
		TASatelite.GET_COMMUNITY_POSTS = BASE_URL + "getcommunityposts";
		TASatelite.GET_COMMUNITY_POST = BASE_URL + "getcommunitypost";
		TASatelite.GET_COMMUNITY_LIST = BASE_URL + "getcommunitylist";
		TASatelite.GET_COMMUNITY_DETAIL = BASE_URL + "getcommunitydetail";
		TASatelite.GET_COMMUNITY_MY_REPLY_URL = BASE_URL + "getcommunitymyreply";
		TASatelite.GET_COMMUNITY_BEST = BASE_URL + "getcommunitybest";
		TASatelite.GET_MORE_APP_LIST = BASE_URL + "getmoreapplist";
		TASatelite.SET_COMMUNITY_DETAIL_FEED = BASE_URL + "setcommunitydetailfeed";
		TASatelite.SET_RECEIVED_MSG_STATE_URL = BASE_URL + "setreceivedmsgstate";
		TASatelite.SET_USER_INFO_URL = BASE_URL + "setuserinfo";
		TASatelite.SET_RECEIVED_MSG_URL = BASE_URL + "setreadmsg";
		TASatelite.SET_MSG_FEED_URL = BASE_URL + "setmsgfeed";
		TASatelite.SET_BEST_FRIEND_FLAG = BASE_URL + "setbestfriendflag";
		TASatelite.SET_COMMUNITY_FEED = BASE_URL + "setcommunityfeed";
		TASatelite.SET_COMMUNITY_ALARM = BASE_URL + "setcommunityalarm";
		TASatelite.SEND_IMG_MSG_URL = BASE_URL + "sendimgmsg";
		TASatelite.SEND_MSG_URL = BASE_URL + "sendmessage";
		TASatelite.POST_COMMUNITY = BASE_URL + "postcommunity";
		TASatelite.POST_IMG_COMMUNITY = BASE_URL + "postimgcommunity";
		TASatelite.ADD_FRIEND_URL = BASE_URL + "addfriend";
		TASatelite.DEL_FRIEND_URL = BASE_URL + "delfriend";
		TASatelite.DEL_COMMUNITY_REPLY = BASE_URL + "delcommunityreply";
		TASatelite.DEL_COMMUNITY_POST = BASE_URL + "delcommunitypost";
		TASatelite.REGISTER_URL = BASE_URL + "register";
		TASatelite.EXIT_ROOM_URL = BASE_URL + "exitroom";
		TASatelite.INVITE_ROOM_URL = BASE_URL + "inviteroom";
		TASatelite.REPLY_COMMUNITY = BASE_URL + "replycommunity";
		TASatelite.LIKE_COMMUNITY = BASE_URL + "likecommunity";
		TASatelite.GET_COMMUNITY_LIKE_PEOPLES = BASE_URL + "getcommunityLikePeoples";
		TASatelite.LOGIN_FACEBOOK = BASE_URL + "loginfacebook";
		TASatelite.GET_FACEBOOK_PUBLIC = BASE_URL + "getfacebookpublic";
		TASatelite.SET_FACEBOOK_PUBLIC = BASE_URL + "setfacebookpublic";
		TASatelite.REPORT_ERROR = BASE_URL + "reporterror";
		TASatelite.MAKE_PUBLIC_CHAT = BASE_URL + "makepublicchat";
		TASatelite.GET_PUBLIC_CHATS = BASE_URL + "getpublicchats";
		TASatelite.ENTER_PUBLIC_CHAT = BASE_URL + "enterpublicchat";
		TASatelite.GET_PUBLIC_ROOM_INFO = BASE_URL + "getpublicroominfo";
		TASatelite.GET_MY_PUBLIC_ROOMS = BASE_URL + "getmypublicrooms";
		TASatelite.SET_PUBLIC_ROOM_HIDDEN = BASE_URL + "setpublicroomhidden";
		TASatelite.GIVE_PUBLIC_ROOM_BJ = BASE_URL + "givepublicroombj";
		TASatelite.KICK_PUBLIC_ROOM_USER = BASE_URL + "kickpublicroomuser";
		TASatelite.SET_UESR_OPTION = BASE_URL + "setuseroption";
		TASatelite.CHECK_ADMIN = BASE_URL + "checkadmin";
		TASatelite.SEARCH_PUBLIC_ROOM = BASE_URL + "searchpublicroom";
		TASatelite.GET_SUB_COMMUNITY_LIST = BASE_URL + "getsubcommunitylist";
		TASatelite.GET_COMMUNITY_RECENT_LIST = BASE_URL + "getcommunityrecentlist";
	}
	public void doGetCommunityRecentList(String token){
		setAddr(GET_COMMUNITY_RECENT_LIST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}		
	}
	
	public void doGetSubCommunityList(String token, long app_code, long community_group_id){
		setAddr(GET_SUB_COMMUNITY_LIST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("app_code", app_code);
			data.put("community_group_id", community_group_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}		
	}
	
	public void doSearchPublicRoom(String token, String keyword, long community_id){
		setAddr(SEARCH_PUBLIC_ROOM);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("keyword", keyword);
			data.put("community_id", community_id);
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
	public void doGivePublicRoomBj(String token, long room_id, long user_id){
		setAddr(GIVE_PUBLIC_ROOM_BJ);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			data.put("user_id", user_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	public void doKickPublicRoomUser(String token, long room_id, JSONArray users){
		setAddr(KICK_PUBLIC_ROOM_USER);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			data.put("users", users);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetPublicRoomHidden(String token, long room_id, boolean flag){
		setAddr(SET_PUBLIC_ROOM_HIDDEN);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			data.put("flag", flag);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetMyPublicRooms(String token){
		setAddr(GET_MY_PUBLIC_ROOMS);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetPublicRoomInfo(String token, long room_id){
		setAddr(GET_PUBLIC_ROOM_INFO);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doEnterPublicChat(String token, long room_id){
		setAddr(ENTER_PUBLIC_CHAT);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("room_id", room_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doMakePublicChat(TAMakePublicChatData data){
		setAddr(MAKE_PUBLIC_CHAT);
		sendMultiData(data);
	}
	
	public void doGetPublicChats(String token, int preCount, long community_id){
		setAddr(GET_PUBLIC_CHATS);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("preCount", preCount);
			data.put("community_id", community_id);
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
	
	public void doGetCommunityLikePeoples(String token, long post_id, long last_id){
		setAddr(GET_COMMUNITY_LIKE_PEOPLES);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("post_id", post_id);
			data.put("last_id", last_id);
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
	
	public void doSetCommunityDetailFeed(String token, long post_id, boolean community_detail_feed){
		setAddr(SET_COMMUNITY_DETAIL_FEED);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("post_id", post_id);
			data.put("community_detail_feed", community_detail_feed);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityBestDay(String token, long community_id){
		setAddr(GET_COMMUNITY_BEST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("stage", 0);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityBestWeek(String token, long community_id){
		setAddr(GET_COMMUNITY_BEST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("stage", 1);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityBestMonth(String token, long community_id){
		setAddr(GET_COMMUNITY_BEST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("stage", 2);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityMyReply(String token, long community_id, long last_id){
		setAddr(GET_COMMUNITY_MY_REPLY_URL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("last_id", last_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityDetail(String token, long community_id){
		setAddr(GET_COMMUNITY_DETAIL);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
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
	
	public void doDelCommunityPost(String token, long post_id){
		setAddr(DEL_COMMUNITY_POST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("post_id", post_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doDelCommunityReply(String token, long reply_id){
		setAddr(DEL_COMMUNITY_REPLY);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("reply_id", reply_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityPostsWithWord(String token, long community_id, long last_id, String word){
		JSONObject search_condition = new JSONObject();
		try {
			search_condition.put("search_type", "word");
			search_condition.put("word", word);
			doGetCommunityPosts(token, community_id, last_id, search_condition);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void doGetCommunityPostsWithNickName(String token, long community_id, long last_id, String nick_name){
		JSONObject search_condition = new JSONObject();
		try {
			search_condition.put("search_type", "nick_name");
			search_condition.put("nick_name", nick_name);
			doGetCommunityPosts(token, community_id, last_id, search_condition);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void doGetCommunityPost(String token, long post_id){
		setAddr(GET_COMMUNITY_POST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("post_id", post_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityPosts(String token, long community_id, long last_id){
		JSONObject search_condition = new JSONObject();
		try {
			search_condition.put("search_type", "none");
			doGetCommunityPosts(token, community_id, last_id, search_condition);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	void doGetCommunityPosts(String token, long community_id, long last_id, JSONObject search_condition){
		setAddr(GET_COMMUNITY_POSTS);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("last_id", last_id);
			data.put("search_condition", search_condition);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doLikeCommunity(String token, long post_id){
		setAddr(LIKE_COMMUNITY);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("post_id", post_id);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doReplyCommunity(String token, long post_id, String msg){
		setAddr(REPLY_COMMUNITY);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("post_id", post_id);
			data.put("msg", msg);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetCommunityAlarm(String token, long community_id, boolean community_alarm){
		setAddr(SET_COMMUNITY_ALARM);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("community_alarm", community_alarm);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doSetCommunityFeed(String token, long community_id, boolean community_feed){
		setAddr(SET_COMMUNITY_FEED);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("community_id", community_id);
			data.put("community_feed", community_feed);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	public void doGetCommunityList(String token, long app_code){
		setAddr(GET_COMMUNITY_LIST);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("app_code", app_code);
			sendData(data.toString());
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	public void doPostCommunity(String token, String msg, long community_id, long transact_id){
		setAddr(POST_COMMUNITY);
		JSONObject data = new JSONObject();
		try {
			data.put("token", token);
			data.put("msg", msg);
			data.put("community_id", community_id);
			data.put("transact_id", transact_id);
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
	
	public void doSendImgPost(OTImgPostData data){
		setAddr(POST_IMG_COMMUNITY);
		sendMultiData(data);
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
