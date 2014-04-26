package com.thinkspace.opentalkon.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class UnPressableFrameLayout extends FrameLayout {
	
	public UnPressableFrameLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public UnPressableFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public UnPressableFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setPressed(boolean pressed) {
		
	}
}
