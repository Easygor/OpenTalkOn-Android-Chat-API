package com.thinkspace.opentalkon.ui.helper;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.thinkspace.opentalkon.ui.OTImageLoadBase;

public class PLActivityGroupView extends OTImageLoadBase implements IPLActivityGroupView{
	boolean useMenu;
	
	@Override
	public Context getContext(){
		if(getParent() != null){
			return getMyActivityGroup().getContext();
		}else{
			return this;
		}
	}
	
	@Override
	public boolean isUseMenu() {
		return useMenu;
	}

	@Override
	public void setUseMenu(boolean useMenu) {
		this.useMenu = useMenu;
	}

	@Override
	public void onActivityResultGroup(int requestCode, int resultCode,Intent data) { }

	@Override
	public PLActivityGroup getMyActivityGroup(){
		return (PLActivityGroup)getParent();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	protected void onResume() {
		if(getMyActivityGroup() != null){
			getMyActivityGroup().setRecentView(this);
		}
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(getParent() != null){
			return getParent().onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onPageBack() { return true;}
	
	@Override
	public void onParentViewClick(int id) {}

	@Override
	public void onShowOptionMenu(Menu menu, MenuInflater inf){}
	
	@Override
	public void onSelectedOptionItem(MenuItem item){}

	@Override
	public void onTabDestoryed() {}
	
}
