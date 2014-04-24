package com.thinkspace.opentalkon.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class OTHorizontalScrollView extends HorizontalScrollView {
	boolean blockVerticalScroll;
	
	public boolean isBlockVerticalScroll() {
		return blockVerticalScroll;
	}

	public void setBlockVerticalScroll(boolean blockVerticalScroll) {
		this.blockVerticalScroll = blockVerticalScroll;
	}

	public OTHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		blockVerticalScroll = true;
	}

	public OTHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		blockVerticalScroll = true;
	}

	public OTHorizontalScrollView(Context context) {
		super(context);
		blockVerticalScroll = true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(blockVerticalScroll){
	        switch (ev.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                // tell parent and it's ancestors to skip touch events
	                this.getParent().requestDisallowInterceptTouchEvent(true);
	                break;
	            case MotionEvent.ACTION_UP:
	                this.getParent().requestDisallowInterceptTouchEvent(false);
	                break;
	        }
		}
        return super.onInterceptTouchEvent(ev);
	}

}
