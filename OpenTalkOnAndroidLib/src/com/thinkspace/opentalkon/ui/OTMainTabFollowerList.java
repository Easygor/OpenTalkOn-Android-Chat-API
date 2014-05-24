package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListElem.LabelType;
import com.thinkspace.opentalkon.ui.OTFriendListBase.SearchHandler;


public class OTMainTabFollowerList extends OTFriendListBase implements SearchHandler{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSearchLayout(this);
	}

	@Override
	public void onTabDestoryed() {
		super.onTabDestoryed();
		clearCacheFromThis();
	}
	
	@Override
	public void elemClicked(TAUserInfo info) {
		Intent intent = new Intent(getParent(), OTFriendPopup.class);
		intent.putExtra("user_info", info);
		getParent().startActivityForResult(intent, OT_CHECK_IF_RESUME);
	}

	@Override
	public void elemLongClicked(final TAUserInfo info) {
		
	}

	@Override
	public void onActivityResultGroup(int requestCode, int resultCode, Intent data) {
		super.onActivityResultGroup(requestCode, resultCode, data);
		if(resultCode != RESULT_OK) return;
		if(requestCode == OT_CHECK_IF_RESUME){
			boolean refresh = data.getBooleanExtra("refresh_list", false);
			if(refresh){
				setProg(true);
				doRequestList();
			}
		}
	}

	@Override
	public void listDataReceiveDone() {
		int reverseFriendCount = 0;
		
		FriendListElem me = null;
		for(FriendListElem elem : friendListElems){
			if(elem.info != null && OTOApp.getInstance().getId() == elem.info.getId()){
				me = elem;
			}else if(elem.info != null && elem.info.is_friend() == false){
				++reverseFriendCount;
			}
		}
		friendListElems.remove(me);

		if(reverseFriendCount > 0){
			FriendListElem friendLabel = new FriendListElem();
			friendLabel.setDivider(true);
			friendLabel.setFriendLabelType(LabelType.TYPE_REVERSE_FRIEND);
			friendLabel.setDivider_text(String.format(getString(R.string.oto_friend_label_3), reverseFriendCount));
			friendListElems.add(friendLabel);
		}
		
		Collections.sort(friendListElems);
		
		super.listDataReceiveDone();
	}

	@Override
	public void onSearch(String value) {
		if(friendListElems == null) return;
		
		friendListElems.clear();
		if(value.length() == 0){
			friendListElems.addAll(friendListElems_Original);
		}else{
			for(FriendListElem elem : friendListElems_Original){
				String nickName = elem.getInfo().getNickName();
				if(nickName != null && nickName.length() != 0){
					if(nickName.contains(value)){
						friendListElems.add(elem);
					}
				}
			}
		}
		listDataReceiveDone();
	}
	
	@Override
	public void doRequestList() {
		String token = OTOApp.getInstance().getToken();
		if(token.length() != 0){
			satelite.doGetReverseFriends(token);
		}else{
			
		}
	}

	@Override
	public String getEmptylistString() {
		return getString(R.string.oto_friend_not_exist);
	}

	@Override
	public void dispatchResponse(DispatchedData dispatchData) {
		if(dispatchData.getLocation().equals(TASatelite.getName(TASatelite.GET_REVERSE_FRIENDS_URL))){
			if(dispatchData.isOK()){
				@SuppressWarnings("unchecked")
				ArrayList<TAUserInfo> userInfos = (ArrayList<TAUserInfo>)dispatchData.getData();
				
				friendListElems_Original.clear();
				friendListElems.clear();
				for(TAUserInfo user : userInfos){
					final FriendListElem item = new FriendListElem();
					item.setInfo(user);
					friendListElems.add(item);
					friendListElems_Original.add(item);
				}
				listDataReceiveDone();
			}else{
				String state = dispatchData.getState();
				if(state.equals("user_id is not valid")){
					
				}else if(state.equals("can't add yourself")){
					
				}else if(state.equals("already added")){
					
				}else if(state.equals("token is not valid")){
					
				}
			}
		}
	}
}
