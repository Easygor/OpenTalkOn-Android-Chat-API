package com.thinkspace.opentalkon.ui.helper;

import com.thinkspace.opentalkon.R;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NonThrowsImageView extends ImageView {

	public NonThrowsImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NonThrowsImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonThrowsImageView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try{
			super.onDraw(canvas);
		}catch(Exception ex){
			setImageResource(R.drawable.oto_friend_img_01);
		}
	}
}
