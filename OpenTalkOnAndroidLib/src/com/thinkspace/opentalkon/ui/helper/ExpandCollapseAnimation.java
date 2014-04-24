package com.thinkspace.opentalkon.ui.helper;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ScrollView;

/**
 * Class for handling collapse and expand animations.
 * @author Esben Gaarsmand
 *
 */
public class ExpandCollapseAnimation extends Animation {
    View mAnimatedView;
    int mEndHeight;
    
    ScrollView scrollView;
    int boundHeight;
    int initScollY;
    int initHeight;
    
    /**
     * Initializes expand collapse animation, has two types, collapse (1) and expand (0).
     * @param view The view to animate
     * @param duration
     * @param type The type of animation: 0 will expand from gone and 0 size to visible and layout size defined in xml. 
     * 1 will collapse view and set to gone
     */
    public ExpandCollapseAnimation(ScrollView scrollView, View view, int duration, int boundHeight, int initHeight) {
        setDuration(duration);
        this.scrollView = scrollView;
        this.boundHeight = boundHeight;
        this.initHeight = initHeight;
        
        initScollY = scrollView.getScrollY();
        mAnimatedView = view;
        mEndHeight = mAnimatedView.getLayoutParams().height;
        mAnimatedView.getLayoutParams().height = 100;
        mAnimatedView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(final float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
        	final int diff = (int) ((mEndHeight - initHeight) * interpolatedTime);
            mAnimatedView.getLayoutParams().height = diff + initHeight;
            mAnimatedView.requestLayout();
            scrollView.scrollTo(scrollView.getScrollX(), initScollY + (int)((boundHeight - initScollY) * interpolatedTime));
        } else {
            mAnimatedView.getLayoutParams().height = mEndHeight;
            mAnimatedView.requestLayout();
            scrollView.scrollTo(scrollView.getScrollX(), boundHeight);
        }
    }
}
