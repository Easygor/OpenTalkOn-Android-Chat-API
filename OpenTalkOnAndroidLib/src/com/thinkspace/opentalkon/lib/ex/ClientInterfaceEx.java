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
	 * @breif Handler is registered to receive message.
	 * @param handler : Handler
	 */
	public static void setClientInterfaceMsgHandler(MessageHandler handler){
		OTOApp.getInstance().getPushClient().setClientInterfaceMsgHandler(handler);
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doAddFriend(TADataHandler handler, long userId){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doAddFriend(OTOApp.getInstance().getToken(), userId);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetFriends(TADataHandler handler){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetFriends(OTOApp.getInstance().getToken());
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetUserInfo(TADataHandler handler, long userId){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfo(OTOApp.getInstance().getToken(), userId);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetUserInfos(TADataHandler handler, List<Long> Ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfos(OTOApp.getInstance().getToken(), Ids);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doSetMsgFeed(TADataHandler handler, long room_id, boolean feed){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetMsgFeed(OTOApp.getInstance().getToken(), room_id, feed);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doInviteRoom(TADataHandler handler, long room_id, List<Long> user_ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doInviteRoom(OTOApp.getInstance().getToken(), room_id, user_ids);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doSetBestFriendFlag(TADataHandler handler, boolean friend_flag, long friend_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetBestFriendFlag(OTOApp.getInstance().getToken(), friend_flag, friend_id);
		return true;	
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doExitRoom(TADataHandler handler, long room_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doExitRoom(OTOApp.getInstance().getToken(), room_id);
		return true;	
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doSetReceivedMsg(TADataHandler handler, Long msg_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetReceivedMsg(OTOApp.getInstance().getToken(), msg_id);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doSetReceivedMsg(TADataHandler handler, ArrayList<Long> msgs){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSetReceivedMsg(OTOApp.getInstance().getToken(), msgs);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetUnReceivedMsg(TADataHandler handler){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUnReceivedMsg(OTOApp.getInstance().getToken());
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetRoomUsers(TADataHandler handler, Long room_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetRoomUsers(OTOApp.getInstance().getToken(), room_id);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetOrMakeRoom(TADataHandler handler, List<Long> users){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetOrMakeRoom(OTOApp.getInstance().getToken(), users);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetUserInfo(TADataHandler handler, Long id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfo(OTOApp.getInstance().getToken(), id);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetUserInfoByNick(TADataHandler handler, String nick_name){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetUserInfoByNick(OTOApp.getInstance().getToken(), nick_name);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doDelFriend(TADataHandler handler, long user_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doDelFriend(OTOApp.getInstance().getToken(), user_id);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doSendMessage(TADataHandler handler, Long room_id, String msg, Long transact_id){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doSendMessage(OTOApp.getInstance().getToken(), room_id, msg, transact_id);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doPostReadMsg(TADataHandler handler, ArrayList<Long> msg_Ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doPostReadMsg(OTOApp.getInstance().getToken(), msg_Ids);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
	 */
	public static boolean doGetReadMsg(TADataHandler handler, ArrayList<Long> msg_Ids){
		if(ClientInterface.checkHasToken(true) == false) return false;
		new TASatelite(handler).doGetReadMsg(OTOApp.getInstance().getToken(), msg_Ids);
		return true;
	}
	
	/**
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
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
	 * @breif http://www.opentalkon.com/assets/reference/classcom_1_1thinkspace_1_1opentalkon_1_1lib_1_1ex_1_1_client_interface_ex.html
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
