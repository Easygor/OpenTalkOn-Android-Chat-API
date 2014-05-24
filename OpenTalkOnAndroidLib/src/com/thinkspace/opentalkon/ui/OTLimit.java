package com.thinkspace.opentalkon.ui;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.thinkspace.common.util.PLUIUtilMgr;
import com.thinkspace.opentalkon.R;

public class OTLimit extends Activity {

	
	@Override
	protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
		super.onApplyThemeResource(theme, resid, first);
		theme.applyStyle(R.style.oto_YourCustomStyle, true);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = PLUIUtilMgr.getDisplaySize(display);
		
		View parentView = getLayoutInflater().inflate(R.layout.ot_user_limit, null);
		setContentView(parentView, new ViewGroup.LayoutParams(size.x - 40, ViewGroup.LayoutParams.WRAP_CONTENT));
		
	}

}
