package com.thinkspace.opentalkon.ui.helper;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class PLKeyBoardView extends LinearLayout {
	PLOnKeyBoardUpListener listener;
	public PLKeyBoardView(Context context, PLOnKeyBoardUpListener listener, int layout_id) {
		super(context);
		this.listener = listener;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layout_id, this);
	}

	public PLKeyBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedheight){
        	listener.onKeyBoardUp();
        } else {
        	listener.onKeyBoardDown();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
