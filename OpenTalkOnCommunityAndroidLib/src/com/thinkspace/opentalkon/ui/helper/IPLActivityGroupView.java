package com.thinkspace.opentalkon.ui.helper;


import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public interface IPLActivityGroupView {
	public Context getContext();
	public PLActivityGroup getMyActivityGroup();
	
	public boolean onPageBack();
	public void onParentViewClick(int id);
	public void onShowOptionMenu(Menu menu, MenuInflater inf);
	public void onSelectedOptionItem(MenuItem item);
	
	public void onActivityResultGroup(int requestCode, int resultCode, Intent data);
	
	public void onTabDestoryed();
	
	public boolean isUseMenu();
	public void setUseMenu(boolean useMenu);
}
