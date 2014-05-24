package com.thinkspace.opentalkon.ui.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.thinkspace.opentalkon.R;


public class PrettyTextView extends FrameLayout {
	FrameLayout mainView;
	TextView textFront;
	TextView textBehind;
	
	public PrettyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		setupAttr(context, attrs);
	}

	public PrettyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		setupAttr(context, attrs);
	}

	public PrettyTextView(Context context) {
		super(context);
		init(context);
	}
	
	@SuppressLint("Recycle")
	void setupAttr(Context context, AttributeSet attrs){
		Resources res = getResources();
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PrettyTextView);
		String text = ta.getString(R.styleable.PrettyTextView_text);
		if(text != null) setText(text);
		setTextSize(ta.getDimension(R.styleable.PrettyTextView_textSize, res.getDimension(R.dimen.pretty_text_size)));
		setFrontTextColor(ta.getColor(R.styleable.PrettyTextView_mainTextColor, res.getColor(R.color.pretty_text_main_color)));
		setBehindTextColor(ta.getColor(R.styleable.PrettyTextView_shadowTextColor, res.getColor(R.color.pretty_text_shadow_color)));
		int style = ta.getInt(R.styleable.PrettyTextView_textStyle, 1);
		if(style == 0){
			textBehind.setTypeface(textBehind.getTypeface(), Typeface.NORMAL);
			textFront.setTypeface(textFront.getTypeface(), Typeface.NORMAL);
		}else{
			textBehind.setTypeface(textBehind.getTypeface(), Typeface.BOLD);
			textFront.setTypeface(textFront.getTypeface(), Typeface.BOLD);
		}
	}

	public void setText(String text){
		textFront.setText(text);
		textBehind.setText(text);
	}
	
	public void setTextSize(float size){
		textFront.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
		textBehind.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}
	
	public void setBehindTextColor(int color){
		textBehind.setTextColor(color);
	}
	
	public void setFrontTextColor(int color){
		textFront.setTextColor(color);
	}
	
	void init(Context context){
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainView = (FrameLayout) li.inflate(R.layout.ex_pretty_text_view, null);
		textFront = (TextView) mainView.findViewById(R.id.pretty_text_front);
		textBehind = (TextView) mainView.findViewById(R.id.pretty_text_behind);
		addView(mainView);
	}
}
