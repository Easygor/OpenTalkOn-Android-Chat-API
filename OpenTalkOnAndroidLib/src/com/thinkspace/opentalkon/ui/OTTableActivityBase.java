package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.helper.ExpandCollapseAnimation;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;

public abstract class OTTableActivityBase extends ImageCacheActivity implements OnClickListener, TADataHandler{
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	
	TASatelite satelite;
	View category;
	TextView categoryText;
	ViewGroup mainView;
	ScrollView mainScroll;
	List<TableDataInterface> dataList = new ArrayList<TableDataInterface>();
	Resources res;
	
	ArrayList<View> newLayout = new ArrayList<View>();
	ArrayList<View> dataSelectLayout = new ArrayList<View>();
	ArrayList<LinearLayout> rowLayouts = new ArrayList<LinearLayout>();
	ArrayList<FrameLayout> rowBottomLayouts = new ArrayList<FrameLayout>();
	int nowSelectedData = -1;
	
	int nowCategory = -1;
	CategoryInterface categoryHandler;
	
	public static interface TableDataInterface{
		public String getTitle();
		public String getDescription();
		public String getImagePath();
		public void onButtonClick(int state);
		public String getButtonString(int state);
	}
	
	public static interface CategoryInterface{
		public String[] getCategoryNames();
		public String getCategoryTitle();
		public void onCategoryClick(int state);
	}
	
	protected abstract void onRequestData();
	protected abstract void onProcessData(JSONObject data);
	protected abstract int getColCount();
	protected void setCategory(CategoryInterface categoryHandler, int nowCategory){
		if(categoryHandler == null){
			category.setVisibility(View.GONE);
		}else{
			category.setVisibility(View.VISIBLE);
			this.categoryHandler = categoryHandler; 
		}
		this.nowCategory = nowCategory;
		categoryText.setText(categoryHandler.getCategoryNames()[nowCategory]);
	}
	
	@Override
	public void onClick(View v) {
		for(LinearLayout rowBottom : rowLayouts){
			if(rowBottom.getTag() == null){
				rowBottom.setTag(rowBottom.getTop());
			}
		}
		
		int index = (Integer) v.getTag();
		View nowDataSelect = dataSelectLayout.get(index);
		
		for(FrameLayout rowBottom : rowBottomLayouts){
			if(rowBottom.getVisibility() == View.VISIBLE){
				rowBottom.setVisibility(View.GONE);
			}
		}
		for(View dataSelect : dataSelectLayout){
			dataSelect.setVisibility(View.GONE);
		}
		for(LinearLayout row : rowLayouts){
			int rowHeight = res.getDimensionPixelSize(R.dimen.ot_more_app_button_small_height);
			row.getLayoutParams().height = rowHeight;
		}
		
		LinearLayout nowRow = rowLayouts.get(index / getColCount());
		final FrameLayout nowRowBottom = rowBottomLayouts.get(index / getColCount());
		if(nowSelectedData == index){
			nowRowBottom.setVisibility(View.GONE);
			nowSelectedData = -1;
		}else{
			int rowHeight = res.getDimensionPixelSize(R.dimen.ot_more_app_button_height);
			nowRow.getLayoutParams().height = rowHeight;
			nowDataSelect.setVisibility(View.VISIBLE);
			int wantPos = (Integer) nowRow.getTag();
			
			final ImageView img = (ImageView) nowRowBottom.findViewById(R.id.oto_more_app_des_img);
			TextView name = (TextView) nowRowBottom.findViewById(R.id.oto_more_app_des_name);
			TextView body = (TextView) nowRowBottom.findViewById(R.id.oto_more_app_des_body);
			Button button1 = (Button) nowRowBottom.findViewById(R.id.oto_more_app_des_btn1);
			Button button2 = (Button) nowRowBottom.findViewById(R.id.oto_more_app_des_btn2);
			final TableDataInterface tableData = dataList.get(index);
			if(tableData.getButtonString(0) != null){
				button1.setText(tableData.getButtonString(0));
			}else{
				button1.setVisibility(View.GONE);
			}
			if(tableData.getButtonString(1) != null){
				button2.setText(tableData.getButtonString(1));
			}else{
				button2.setVisibility(View.GONE);
			}
			name.setText(tableData.getTitle());
			body.setText(tableData.getDescription());
			if(tableData.getImagePath().length() != 0){
				String uri = TASatelite.makeCommonImageUrl(tableData.getImagePath());
				loadImage(uri, img, R.drawable.oto_oto_logo, true, true);
			}else{
				img.setImageResource(R.drawable.oto_oto_logo);
			}
			
			button1.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					tableData.onButtonClick(0);
				}
			});
			button2.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					tableData.onButtonClick(1);
				}
			});
			
			nowRowBottom.setVisibility(View.VISIBLE);
			if(nowSelectedData == -1 || (nowSelectedData / getColCount()) != (index / getColCount()) ){
				ExpandCollapseAnimation anim = new ExpandCollapseAnimation(mainScroll, nowRowBottom, 200, wantPos, 100);
				nowRowBottom.startAnimation(anim);
			}
			nowSelectedData = index;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_more_app);
		res = getResources();
		
		categoryText = (TextView)findViewById(R.id.oto_more_app_category_text);
		category = findViewById(R.id.oto_more_app_select_category);
		mainView = (ViewGroup) findViewById(R.id.oto_more_app_main_layout);
		mainScroll = (ScrollView) findViewById(R.id.oto_more_app_main_scroll);
		mainScroll.setFadingEdgeLength(0);
		category.setVisibility(View.GONE);
		
		category.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View view) {
				if(categoryHandler == null) return;
				AlertDialog.Builder ab = new Builder(OTTableActivityBase.this);
				ab.setTitle(categoryHandler.getCategoryTitle());
				ab.setItems(categoryHandler.getCategoryNames(),
				new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						categoryHandler.onCategoryClick(which);
					}
				});
				ab.show();
			}
		});
		satelite = new TASatelite(this);
	}
	
	@Override public void onHttpPacketReceived(JSONObject data) {
		onProcessData(data);
	}
	
	boolean finishGuard = false;
	
	@Override
	public void onTokenIsNotValid(JSONObject data) {
		if(finishGuard) return;
		finish();
		finishGuard = true;
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		if(finishGuard) return;
		startActivity(new Intent(this, OTLimit.class));
		finish();
		finishGuard = true;
	}
	
	@Override public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	public void makeLayout(ViewGroup layout, List<TableDataInterface> dataList) {
		layout.removeAllViews();
		dataSelectLayout.clear();
		rowLayouts.clear();
		rowBottomLayouts.clear();
		nowSelectedData = -1;
		
		LayoutInflater li = getLayoutInflater();
		int remainCount;
		if(dataList.size() % getColCount() == 0)
			remainCount = 0;
		else
			remainCount = getColCount() - (dataList.size() % getColCount());
		
		LinearLayout recentRow = null;
		FrameLayout recentRowBottom = null;
		for(int i=0;i<dataList.size() + remainCount;++i){
			TableDataInterface data = null;
			if(i < dataList.size()) data = dataList.get(i);
			
			if(i % getColCount() == 0){
				
				if(i != 0){
					View horizontalLine = new View(this);
					horizontalLine.setBackgroundColor(Color.rgb(0x9b,0x9c,0x9d));
					layout.addView(horizontalLine, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 1));
				}
				
				recentRow = new LinearLayout(this);
				rowLayouts.add(recentRow);
				recentRow.setOrientation(LinearLayout.HORIZONTAL);
				int rowHeight = res.getDimensionPixelSize(R.dimen.ot_more_app_button_small_height);
				layout.addView(recentRow, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, rowHeight));
				
				recentRowBottom = (FrameLayout) li.inflate(R.layout.ot_more_app_description, null);
				recentRowBottom.setVisibility(View.GONE);
				rowBottomLayouts.add(recentRowBottom);
				int baseHeight = res.getDimensionPixelSize(R.dimen.ot_more_app_bottom_base_height);
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, baseHeight);
				layout.addView(recentRowBottom, param);
			}
			
			View appLayout = li.inflate(R.layout.ot_more_app_button, null);
			ViewGroup appBtnlayout = (ViewGroup)appLayout.findViewById(R.id.oto_more_app_btn_layout);
			ImageView appImage = (ImageView)appLayout.findViewById(R.id.oto_more_app_btn_image);
			TextView appText = (TextView)appLayout.findViewById(R.id.oto_more_app_btn_text);
			View newView = appLayout.findViewById(R.id.oto_more_app_btn_new);
			View appSelect = appLayout.findViewById(R.id.oto_more_app_btn_select);
			if(data != null){
				newLayout.add(newView);
				newView.setVisibility(View.GONE);
				dataSelectLayout.add(appSelect);
				if(data.getImagePath().length() != 0){
					String url = TASatelite.makeCommonImageUrl(data.getImagePath());
					loadImage(url, appImage, R.drawable.oto_oto_logo, true, false);
				}else{
					appImage.setImageResource(R.drawable.oto_oto_logo);
				}
				appText.setText(data.getTitle());
				
				appBtnlayout.setTag(i);
				appBtnlayout.setOnClickListener(this);
			}else{
				newView.setVisibility(View.GONE);
				appImage.setVisibility(View.GONE);
				appText.setVisibility(View.GONE);
				appSelect.setVisibility(View.GONE);
			}
			recentRow.addView(appLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, 1.0f));
			if((i+1) % getColCount() != 0){
				View veriticalLine = new View(this);
				veriticalLine.setBackgroundColor(Color.rgb(0x9b, 0x9c, 0x9d));
				int rowHeight = getResources().getDimensionPixelSize(R.dimen.ot_more_app_button_small_height);
				recentRow.addView(veriticalLine, new LinearLayout.LayoutParams(1, rowHeight));
			}
		}
	}
}
