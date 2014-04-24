package com.thinkspace.opentalkon;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
 
public class ErrorReporter implements Thread.UncaughtExceptionHandler {
	
    String VersionName;
    String PackageName;
    String FilePath;
    String PhoneModel;
    String AndroidVersion;
    String Board;
    String Brand;
    // String CPU_ABI;
    String Device;
    String Display;
    String FingerPrint;
    String Host;
    String ID;
    String Manufacturer;
    String Model;
    String Product;
    String Tags;
    long Time;
    String Type;
    String User;
    private Thread.UncaughtExceptionHandler PreviousHandler;
    private static ErrorReporter S_mInstance;
    private Context CurContext;
 
    public void Init(Context context) {
        PreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        RecoltInformations(context);
        CurContext = context;
    }
 
    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
 
    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }
 
    void RecoltInformations(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi;
            // Version
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            VersionName = pi.versionName;
            // Package name
            PackageName = pi.packageName;
            // Files dir for storing the stack traces
            FilePath = context.getFilesDir().getAbsolutePath();
            // Device model
            PhoneModel = android.os.Build.MODEL;
            // Android version
            AndroidVersion = android.os.Build.VERSION.RELEASE;
            Board = android.os.Build.BOARD;
            Brand = android.os.Build.BRAND;
            // CPU_ABI = android.os.Build.;
            Device = android.os.Build.DEVICE;
            Display = android.os.Build.DISPLAY;
            FingerPrint = android.os.Build.FINGERPRINT;
            Host = android.os.Build.HOST;
            ID = android.os.Build.ID;
            // Manufacturer = android.os.Build.;
            Model = android.os.Build.MODEL;
            Product = android.os.Build.PRODUCT;
            Tags = android.os.Build.TAGS;
            Time = android.os.Build.TIME;
            Type = android.os.Build.TYPE;
            User = android.os.Build.USER;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
 
    public String CreateInformationString() {
        String ReturnVal = "";
        ReturnVal += "Version : " + VersionName;
        ReturnVal += "\n";
        ReturnVal += "Package : " + PackageName;
        ReturnVal += "\n";
        ReturnVal += "FilePath : " + FilePath;
        ReturnVal += "\n";
        ReturnVal += "Phone Model" + PhoneModel;
        ReturnVal += "\n";
        ReturnVal += "Android Version : " + AndroidVersion;
        ReturnVal += "\n";
        ReturnVal += "Board : " + Board;
        ReturnVal += "\n";
        ReturnVal += "Brand : " + Brand;
        ReturnVal += "\n";
        ReturnVal += "Device : " + Device;
        ReturnVal += "\n";
        ReturnVal += "Display : " + Display;
        ReturnVal += "\n";
        ReturnVal += "Finger Print : " + FingerPrint;
        ReturnVal += "\n";
        ReturnVal += "Host : " + Host;
        ReturnVal += "\n";
        ReturnVal += "ID : " + ID;
        ReturnVal += "\n";
        ReturnVal += "Model : " + Model;
        ReturnVal += "\n";
        ReturnVal += "Product : " + Product;
        ReturnVal += "\n";
        ReturnVal += "Tags : " + Tags;
        ReturnVal += "\n";
        ReturnVal += "Time : " + Time;
        ReturnVal += "\n";
        ReturnVal += "Type : " + Type;
        ReturnVal += "\n";
        ReturnVal += "User : " + User;
        ReturnVal += "\n";
        ReturnVal += "Total Internal memory : " + getTotalInternalMemorySize();
        ReturnVal += "\n";
        ReturnVal += "Available Internal memory : " + getAvailableInternalMemorySize();
        ReturnVal += "\n";
        return ReturnVal;
    }
 
    public void uncaughtException(Thread t, Throwable e) {
        String Report = "";
        Date CurDate = new Date();
        Report += "Error Report collected on : " + CurDate.toString();
        Report += "\n";
        Report += "\n";
        Report += "Informations :";
        Report += "\n";
        Report += "==============";
        Report += "\n";
        Report += "\n";
        Report += CreateInformationString();
        Report += "\n\n";
        Report += "Stack : \n";
        Report += "======= \n";
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        Report += stacktrace;
        Report += "\n";
        Report += "Cause : \n";
        Report += "======= \n";
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            Report += result.toString();
            cause = cause.getCause();
        }
        printWriter.close();
        Report += "****  End of current Report ***";
        SendErrorToServer(this.CurContext, Report);
        PreviousHandler.uncaughtException(t, e);
    }
 
    static ErrorReporter getInstance() {
        if (S_mInstance == null)
            S_mInstance = new ErrorReporter();
        return S_mInstance;
    }
 
    private void SendErrorToServer(Context _context, String ErrorContent) {
        String subject = ("Crash Report - Android ErrorReporter");
      
        String body = subject + "\n\n" + ErrorContent + "\n\n";
        TASatelite satelite = new TASatelite(new TADataHandler() {
			@Override public void onHttpPacketReceived(JSONObject data) {}
			@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}
			@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
			@Override public void onTokenIsNotValid(JSONObject data) {}
			@Override public void onLimitMaxUser(JSONObject data) {}
		});
        String unique_key = PLEtcUtilMgr.getUniqueKeyWithSha();
        satelite.doReportError(unique_key, body);
    }
}