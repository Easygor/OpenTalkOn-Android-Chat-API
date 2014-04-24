package com.thinkspace.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.util.encryptor.Hash;
import com.thinkspace.util.encryptor.PLBase64;

public class PLEtcUtilMgr {
	public static String getPhoneNumber(Context ctx){
		TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = tm.getLine1Number();
		if(phoneNumber == null || phoneNumber.length() == 0){
			return null;
		}
		phoneNumber = phoneNumber.replace("+82", "0");
		return phoneNumber;
	}
	public static boolean isLocaleKorea(){
		return Locale.getDefault().getCountry().equals(Locale.KOREA.getCountry());
	}
	
	public static String getLocale(){
		return Locale.getDefault().getCountry();
	}
	
	public static int getScreenHeight(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        return metrics.heightPixels;
	}
	
	public static String getDateFormat(long time){
		Date msgTime = new Date(time);
		long diff = System.currentTimeMillis() - time;
		
		diff += 3000L;
		if(diff < 0L) diff = 0L;
		
		if(diff < 1000L * 60L * 60L * 24L){
			if(diff < 1000L * 60L){
				return String.valueOf(diff/1000L) + OTOApp.getInstance().getContext().getString(R.string.oto_pre_seconds);
			}else if(diff < 1000L * 60L * 60L){
				return String.valueOf(diff/(1000L * 60L)) + OTOApp.getInstance().getContext().getString(R.string.oto_pre_minutes);
			}else{
				return String.valueOf(diff/(1000L * 60L * 60L)) + OTOApp.getInstance().getContext().getString(R.string.oto_pre_hours);
			}
		}else{
			return new SimpleDateFormat("yy. M. d").format(msgTime);
		}
	}
	
	public static String getDeviceId(){
		TelephonyManager TelephonyMgr = (TelephonyManager)OTOApp.getInstance().getContext().getSystemService(Context.TELEPHONY_SERVICE);
		if(TelephonyMgr == null) return null;
		String szImei = TelephonyMgr.getDeviceId();
		return szImei;
	}
	
	public static String getUniqueId(){
		String m_szDevIDShort = "35" + //we make this look like a valid IMEI
	        	Build.BOARD.length()%10+ Build.BRAND.length()%10 + 
	        	Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 + 
	        	Build.DISPLAY.length()%10 + Build.HOST.length()%10 + 
	        	Build.ID.length()%10 + Build.MANUFACTURER.length()%10 + 
	        	Build.MODEL.length()%10 + Build.PRODUCT.length()%10 + 
	        	Build.TAGS.length()%10 + Build.TYPE.length()%10 + 
	        	Build.USER.length()%10 ;
		return m_szDevIDShort;
	}
	
	public static String getMacAddress(){
		WifiManager wm = (WifiManager)OTOApp.getInstance().getContext().getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		return m_szWLANMAC;
	}
	
	public static String getAndroidId(){
		String m_szAndroidID = Secure.getString(OTOApp.getInstance().getContext().getContentResolver(), Secure.ANDROID_ID);
		return m_szAndroidID;
	}
	
	private static String getValidId(){
		try{
			String deviceId = getDeviceId();
			if(deviceId != null) return deviceId;
		}catch(Exception ex){}
		try{
			String macAddr = getMacAddress();
			if(macAddr != null) return macAddr;
		}catch(Exception ex){}
		try{
			String androidId = getAndroidId();
			if(androidId != null) return androidId;
		}catch(Exception ex){}
		try{
			String uniqueId = getUniqueId();
			if(uniqueId != null) return uniqueId;
		}catch(Exception ex){}
		return String.valueOf(System.currentTimeMillis()) + String.valueOf(new Random().nextLong());
	}
	
	public static String getNetworkType(){
		ConnectivityManager cm = (ConnectivityManager)OTOApp.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if(info != null && info.getTypeName() != null){
			String type = info.getTypeName();
			if(type.equalsIgnoreCase("mobile")){
				return "3G";
			}
			if(type.equalsIgnoreCase("wifi")){
				return "WIFI";
			}
		}
		return "unknown";
	}
	
	public static String getUniqueKeyWithSha(){
		String number = getPhoneNumber(OTOApp.getInstance().getContext());
		if(number == null){
			number = getValidId();
		}
		return PLBase64.encode(Hash.SHA256(number));
	}
	
	public static String getPhoneFormat(String number){
		if(number == null) return null;
		if(number.length() == 10){
			return number.substring(0,3) + "-" + number.substring(3,6) + "-" + number.substring(6);
		}
		if(number.length() == 11){
			return number.substring(0,3) + "-" + number.substring(3,7) + "-" + number.substring(7);
		}
		return number;
	}
	
	public static String getDefaultDateFormat(long time){
		return new SimpleDateFormat("a h:mm").format(new Date(time));
	}
	
	public static String getRecentDateFormat(long time){
		return new SimpleDateFormat("yyyy/MM/dd/a h:mm",Locale.US).format(new Date(time));
	}
	
	public static ArrayList<String> getHaveIntent(Intent intent, PackageManager pm) {
		ArrayList<String> ret = new ArrayList<String>();
		Iterator<ResolveInfo> obj = pm.queryIntentActivities(intent, 0).iterator();
		if (obj != null) {
			while (obj.hasNext()) {
				ResolveInfo resolveinfo = (ResolveInfo) obj.next();
				if (resolveinfo.activityInfo.packageName != null) {
					String packName = resolveinfo.activityInfo.packageName;
					ret.add(packName);
				}
			}
		}
		return ret;
	}
	
	public static String toNumberComma(String text){
		String ret = "";
		String number = "";
		for(int i=0;i<text.length();++i){
			char ch = text.charAt(i);
			if(ch >= '0' && ch <='9'){
				number += ch;
			}else{
				if(number.length() != 0){
					if(number.charAt(0) != '0'){
						ret += getPriceToWon(Long.valueOf(number));
					}else{
						ret += number;
					}
					number = "";
				}
				ret += ch;
			}
		}
		if(number.length() != 0){
			if(number.charAt(0) != '0'){
				ret += getPriceToWon(Long.valueOf(number));
			}else{
				ret += number;
			}
		}
		return ret;
	}
	
	public static String getPriceToWon(Long value){
		String ret = "";
		
		int cnt = 0;
		boolean resv = false;
		
		Long now = value;
		while(now != 0){
			if(resv){
				ret = String.valueOf(now % 10) + "," + ret;
				resv = false;
			}else{
				ret = String.valueOf(now % 10) + ret;
			}
			now /= 10;
			if(++cnt == 3){
				resv = true;
				cnt = 0;
			}
		}
		return ret;
	}
	
	public static void makeDirectoryOnly(File file){
		String[] partial = file.getAbsolutePath().split("/");
		String path = "/";
		for(int i = 0;i<partial.length-1;++i){
			path += partial[i] + '/';
		}
		new File(path).mkdirs();
	}
	
	public static float dipToPx(Resources res, float dip){
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
		return px;
	}
	
	public static float dpToPx(Resources res, float dp){
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
		return px;
	}
	
	public static void deleteAllFile(String path){
		File file = new File(path);
		File [] files = file.listFiles();
		if(files != null){
			for(File pFile : files){
				pFile.delete();
			}
		}
	}
	
	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try {
			InputStream in = new FileInputStream(srcFile);
			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Copy data from a source stream to destFile. Return true if succeed,
	 * return false if failed.
	 */
	
	private static boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			OutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
