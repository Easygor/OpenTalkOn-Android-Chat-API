package com.thinkspace.common.util;

import java.lang.reflect.Method;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.widget.Toast;

public class PLUIUtilMgr {
	private ProgressDialog mUIProgressDialog; 
	
	public PLUIUtilMgr(){		
	}
	
	public PLUIUtilMgr makeWaitDialogProgress(Context context){
		return makeDialogProgress("Please Waiting...",context);
	}
	
	public synchronized PLUIUtilMgr makeDialogProgress(String Message,Context context)
	{
		if(mUIProgressDialog != null){
			try{
				if(mUIProgressDialog.isShowing()){
					mUIProgressDialog.dismiss();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		try{
			mUIProgressDialog = new ProgressDialog(context);
			mUIProgressDialog.setMessage(Message);
			mUIProgressDialog.setIndeterminate(true);
			mUIProgressDialog.setCancelable(true);
			mUIProgressDialog.show();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return this;
	}
	public synchronized PLUIUtilMgr dismissDialogProgress(){
		if(mUIProgressDialog == null){
			return this;
		}
		try{
			if(mUIProgressDialog.isShowing()){
				mUIProgressDialog.dismiss();
				mUIProgressDialog = null;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return this;
	}
	public PLUIUtilMgr setDialogProgress(String msg,Context context){
		if(mUIProgressDialog == null){
			makeDialogProgress(msg,context);
		}else{
			mUIProgressDialog.setMessage(msg);
		}
		return this;
	}
	public void showToast(String msg, Context context){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static int getStatusBarHeight(Resources res) {
	      int result = 0;
	      int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = res.getDimensionPixelSize(resourceId);
	      }
	      return result;
	}
	
	public static Point getDisplaySize(Display d)
	{
	    return getDisplaySizeLT11(d);
	}
	
	static Point getDisplaySizeLT11(Display d)
	{
	    try
	    {
	        Method getWidth = Display.class.getMethod("getWidth", new Class[] {});
	        Method getHeight = Display.class.getMethod("getHeight", new Class[] {});
	        return new Point(((Integer) getWidth.invoke(d, (Object[]) null)).intValue(), ((Integer) getHeight.invoke(d, (Object[]) null)).intValue());
	    }
	    catch (Exception ex){ // None of these exceptions should ever occur.
	    	return new Point(-1,-1);
	    }
	}
}
