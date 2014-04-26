package com.thinkspace.opentalkon.ui.helper;

import java.util.Random;


import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

public abstract class PLActivityGroup extends ActivityGroup {
	IPLActivityGroupView recentView;
	MenuInflater menuInflator = new MenuInflater(this);
	Random random = new Random(System.currentTimeMillis());
	
	public void setRecentView(IPLActivityGroupView view){
		this.recentView = view;
	}
	
	public Context getContext(){
		return this;
	}
	
	public MenuInflater getMenuInflator() {
		return menuInflator;
	}

	public void setMenuInflator(MenuInflater menuInflator) {
		this.menuInflator = menuInflator;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(recentView != null){
			recentView.onActivityResultGroup(requestCode, resultCode, data);
		}
	}

	public abstract void onChangeView(String viewName, Object data);
	
	public boolean applyPage(Class<?> changeView, Bundle bundle){
		if(recentView != null){
			recentView.onTabDestoryed();
		}
		Intent intent = new Intent(this, changeView);
		intent.setAction(Intent.ACTION_VIEW);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtras(bundle);
		
		Window w = getLocalActivityManager().startActivity(String.valueOf(random.nextLong() * random.nextInt()), intent);
		
		View wv = w != null ? w.getDecorView() : null;
		if(wv == null) return false;
		FrameLayout frameLayout = getFrameLayOut();
		frameLayout.removeAllViews();
		frameLayout.addView(wv);
		wv.setVisibility(View.INVISIBLE);
		
		wv.setVisibility(View.VISIBLE);
		wv.setFocusable(true);
		wv.bringToFront();
		return true;
	}
	
	public boolean applyPage(Class<?> changeView){
		if(recentView != null){
			recentView.onTabDestoryed();
		}
		Intent intent = new Intent(this, changeView);
		intent.setAction(Intent.ACTION_VIEW);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("click", true);
		
		Window w = getLocalActivityManager().startActivity(String.valueOf(random.nextLong() * random.nextInt()), intent);
		
		View wv = w != null ? w.getDecorView() : null;
		if(wv == null) return false;
		FrameLayout frameLayout = getFrameLayOut();
		frameLayout.removeAllViews();
		frameLayout.addView(wv);
		wv.setVisibility(View.INVISIBLE);
		
		wv.setVisibility(View.VISIBLE);
		wv.setFocusable(true);
		wv.bringToFront();
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	public IPLActivityGroupView getRecentView() {
		return recentView;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(recentView != null && recentView.isUseMenu()){
			menu.clear();
			recentView.onShowOptionMenu(menu,menuInflator);
			return super.onPrepareOptionsMenu(menu);
		}
		return false;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(recentView != null && recentView.isUseMenu()){
			recentView.onSelectedOptionItem(item);
			return super.onOptionsItemSelected(item);
		}
		return false;
	}
	public abstract FrameLayout getFrameLayOut();
}

