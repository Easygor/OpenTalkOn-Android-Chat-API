package com.thinkspace.opentalkon.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkspace.opentalkon.R;

public class BaseTabButtonView extends FrameLayout {
	View viewMain;
	TextView newCount;
	ImageView imageTop;
	TextView textBottom;
	
	int clickImage_res;
	int noClickImage_res;
	String menuName;
	
	public BaseTabButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public BaseTabButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public BaseTabButtonView(Context context) {
		super(context);
		initView();
	}
	
	public void initView(){
		viewMain = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ot_tab_button, null);
		newCount = (TextView)viewMain.findViewById(R.id.oto_main_button_new_count);
		imageTop = (ImageView)viewMain.findViewById(R.id.oto_main_button_image_t);
		textBottom = (TextView)viewMain.findViewById(R.id.oto_main_button_image_b);
		
		addView(viewMain, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public int getClickImage_res() {
		return clickImage_res;
	}

	public void setClickImage_res(int clickImage_res) {
		this.clickImage_res = clickImage_res;
	}

	public int getNoClickImage_res() {
		return noClickImage_res;
	}

	public void setNoClickImage_res(int noClickImage_res) {
		this.noClickImage_res = noClickImage_res;
	}
	
	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public void setClick(boolean enable){
		viewMain.setBackgroundColor(Color.TRANSPARENT);
		if(enable){
			imageTop.setImageResource(clickImage_res);
			textBottom.setText(menuName);
			textBottom.setTextColor(Color.rgb(0xed, 0xd3, 0x76));
		}else{
			imageTop.setImageResource(noClickImage_res);
			textBottom.setText(menuName);
			textBottom.setTextColor(Color.rgb(0x9f, 0x9f, 0x9f));
		}
	}
	
	public void setNewCount(int count){
		if(count == 0){
			newCount.setVisibility(View.GONE);
		}else{
			newCount.setVisibility(View.VISIBLE);
			newCount.setText(String.valueOf(count));
		}
	}
}
