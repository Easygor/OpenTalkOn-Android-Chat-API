package com.thinkspace.opentalkon;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;

import com.thinkspace.common.util.ConversationMgr;
import com.thinkspace.common.util.PLDialogMaker;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.common.util.PLTaskMgr;
import com.thinkspace.common.util.PLUIUtilMgr;
import com.thinkspace.opentalkon.data.TABigTableDB;
import com.thinkspace.opentalkon.data.TAPreference;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.PLCacheCtrl;
import com.thinkspace.opentalkon.javapush.JavaTokenClient;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.pushservice.appclient.PushClientBase;

public class OTOApp {
	public final static String ACTION_GET_VALID_TOKEN_IS_DONE = "com.thinkspace.opentalkon.intent.action.GET_VALID_TOKEN_IS_DONE";
	static OTOApp instance;
	public Context context;
	
	Handler handler;
	public TABigTableDB db;
	public PLTaskMgr taskMgr;
	public TAPreference pref;
	public PLUIUtilMgr uiMgr;
	public PLDialogMaker dialogMaker;
	public PLCacheCtrl cacheCtrl;
	public ConversationMgr convMgr;
	public TAImgDownloader imageDownloader;
	
	public TABigTableDB getDB() { return db; }
	public PLTaskMgr getTaskMgr() { return taskMgr; }
	public TAPreference getPref() { return pref;}
	public PLUIUtilMgr getUIMgr() {return uiMgr;}
	public PLDialogMaker getDialogMaker() { return dialogMaker; }
	public PLCacheCtrl getCacheCtrl() { return cacheCtrl; }
	public ConversationMgr getConvMgr(){ return convMgr; }
	public TAImgDownloader getImageDownloader() { return imageDownloader; }
	
	public boolean isPhoneVerify() {
		return phoneVerify;
	}
	public void setPhoneVerify(boolean phoneVerify) {
		this.phoneVerify = phoneVerify;
	}

	PushClientBase client;
	int mainActivityCount = 0;
	boolean mainFullScreen = false;
	GetValidTokenDoneReceiver getValidTokenDoneReceiver = null;
	boolean phoneVerify = false;
	
	public static OTOApp getInstance(){
		if(instance == null){
			instance = new OTOApp();
		}
		return instance;
	}
	
	public Context getContext() { return context; }

	public int getMainActivityCount() {
		return mainActivityCount;
	}
	public void IncMainActivityCount() {
		++this.mainActivityCount;
	}
	public void DecMainActivityCount() {
		--this.mainActivityCount;
		if(this.mainActivityCount < 0)
			this.mainActivityCount = 0;
	}
	
	public boolean isMainFullScreen() {
		return mainFullScreen;
	}
	public void setMainFullScreen(boolean mainFullScreen) {
		this.mainFullScreen = mainFullScreen;
	}
	public void deleteDatabase(){
		getDB().deleteDB();
		getDB().init();
	}
	
	public int getAppCode(){
		return pref.getAppCode().getValue();
	}
	
	public void changeUserDatabase(long prev_user_id, long user_id){
		if(prev_user_id == user_id) return;
		File dbFile = context.getDatabasePath(context.getString(R.string.oto_config_db_filename));
		File saveFile = context.getDatabasePath(String.valueOf(prev_user_id) + ".db");
		if(dbFile.exists()){
			if(PLEtcUtilMgr.copyFile(dbFile, saveFile) == false) return;
			dbFile.delete();
		}
		
		File userFile = context.getDatabasePath(String.valueOf(user_id) + ".db");
		if(userFile.exists()){
			if(PLEtcUtilMgr.copyFile(userFile, dbFile) == false) return;
			userFile.delete();
		}
		
		db = new TABigTableDB(context, context.getString(R.string.oto_config_db_filename));
		cacheCtrl = new PLCacheCtrl();
		TAUserNick.forceNewInstance();
	}
	
	public void InitializeAuth(boolean showToast){
		if(getPushClient() != null){
			getPushClient().stopTokenClient();
		}
		
		if(pref.getHas_backup().getValue()){
			changeUserDatabase(pref.getUser_id().getValue(), pref.getUser_id_backup().getValue());
			pref.getUser_id().setValue(pref.getUser_id_backup().getValue());
			pref.getToken().setValue(pref.getToken_backup().getValue());
			pref.getNickName().setValue(pref.getNickName_backup().getValue());
			
			pref.getHas_backup().setValue(false);
			pref.getUser_id_backup().setValue(-1L);
			pref.getToken_backup().setValue("");
			pref.getNickName_backup().setValue("");
			OTOApp.getInstance().startPushService(false);
		}else{
			pref.getToken().setValue("");
			pref.getUser_id().setValue(-1L);
			pref.getNickName().setValue("");
			pref.getAppCode().setValue(-1);
			pref.getVerifiedPhoneNumber().setValue("");
		}
		try{
			OTOApp.getInstance().getUIMgr().showToast(
					OTOApp.getInstance().getContext().getString(R.string.oto_token_is_not_valid_popup),
					OTOApp.getInstance().getContext());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void setupSateliteHost(){
		String BASE_URL = context.getString(R.string.oto_android_service_host);
		TASatelite.setupSateliteHost(BASE_URL);
	}
	
	public void InitOpenTalkLib(Context context, String appToken, BroadcastReceiver receiver, boolean phoneVerify) {
		instance = this;
		this.context = context;
		this.phoneVerify = phoneVerify;
		
		ErrorReporter errorRepoter = new ErrorReporter();
		errorRepoter.Init(context);
		Thread.setDefaultUncaughtExceptionHandler(errorRepoter);
		
		setupSateliteHost();
		handler = new Handler();
		convMgr = new ConversationMgr(context, handler);
		dialogMaker = new PLDialogMaker();
		db = new TABigTableDB(context, context.getString(R.string.oto_config_db_filename));
		pref = new TAPreference(context);
		taskMgr = new PLTaskMgr();
		uiMgr = new PLUIUtilMgr();
		cacheCtrl = new PLCacheCtrl();
		imageDownloader = new TAImgDownloader(handler);
		initPushClient(handler);
		
		//Library Logic
		TAUserNick.getInstance();
		
		pref.getAppToken().setValue(appToken);
		startPushService(false);

		IntentFilter iff = new IntentFilter("com.thinkspace.opentalkon.intent.action.GET_VALID_TOKEN_IS_DONE");
		context.registerReceiver(getValidTokenDoneReceiver = new GetValidTokenDoneReceiver(), iff);
		addValidTokenDoneReceiver(receiver);
		
		processCacheExpired();
	}
	
	public void addValidTokenDoneReceiver(BroadcastReceiver receiver){
		if(receiver != null){
			getValidTokenDoneReceiver.addDisposableReceiver(receiver);
		}
	}
	
	public void processCacheExpired(){
		if(System.currentTimeMillis() - pref.getLastCacheDeleted().getValue() >= 1000L * 60L * 60L * 24L * 7L * 3L){
			pref.getLastCacheDeleted().setValue(System.currentTimeMillis());
			new Thread(new Runnable() {
				@Override public void run() {
					try{
						File cacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.thinkspace.opentalkon/cached/");
						if(cacheDir.exists()){
							for(File file : cacheDir.listFiles()){
								file.delete();
							}
						}
					}catch(Exception ex){}
				}
			}).start();
		}
	}

	public int getVersionCode() {
		return context.getResources().getInteger(R.integer.oto_lib_version_code);
	}
	
	public long getId(){
		return pref.getUser_id().getValue();
	}
	
	public String getToken(){
		return pref.getToken().getValue();
	}
	
	public void startPushService(boolean userPress){
		Intent intent = new Intent(context, OTPushService.class);
		intent.putExtra("userPress", userPress);
		context.startService(intent);
    }	
	
	public PushClientBase getPushClient(){
		return client;
	}
	
	public void initPushClient(Handler handler){
		client = JavaTokenClient.getInstance();
		if (client == null) {
			client = JavaTokenClient.newInstance(handler, context);
		}
	}
	
	public boolean hasActivity(String activityName){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningTaskInfo info : am.getRunningTasks(100)){
			if(info.topActivity == null) return false;
			if(info.topActivity.getShortClassName().endsWith(activityName)){
				return true;
			}
		}
		return false;
	}
	
	public boolean IsForeground(String activityName){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> infos =  am.getRunningTasks(1);
		if(infos.size() != 0){
			RunningTaskInfo info = infos.get(0);
			if(info.topActivity == null) return false;
			if(info.topActivity.getShortClassName().endsWith(activityName)){
				return true;
			}
		}
		return false;
	}
}
