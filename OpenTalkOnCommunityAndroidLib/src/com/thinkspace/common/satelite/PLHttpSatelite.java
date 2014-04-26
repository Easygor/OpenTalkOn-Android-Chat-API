package com.thinkspace.common.satelite;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.common.util.PLTaskMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.TAMultiData;


public class PLHttpSatelite {
	public final static int RETRY_CNT = 3;
	public final static int REQUEST_TYPE_POST = 0;
	public final static int REQUEST_TYPE_MULTIPART = 1;
	
	public final static int DEFAULT_HTTP_CONNECTION_TIME_OUT = 10000;
	
	Handler handler;
	PLHttpDataHandler dataHandler;
	PLTaskMgr taskMgr;
	Map<String, String> valueSet = new HashMap<String, String>();
	String addr;
	String lastRequestData = null;
	TAMultiData lastRequestMultiData = null;
	
	String lastResultData = null;
	String lastFailData = null;
	Exception lastException = null;
	
	public PLHttpSatelite(boolean makeHandler){
		if(makeHandler){
			handler = new Handler();
		}else{
			handler = null;
		}
	}
	
	public Handler getUIHandler() {
		return handler;
	}

	public String getLastRequestData() {
		return lastRequestData;
	}

	public void setLastRequestData(String lastRequestData) {
		this.lastRequestData = lastRequestData;
	}

	public TAMultiData getLastRequestMultiData() {
		return lastRequestMultiData;
	}

	public void setLastRequestMultiData(TAMultiData lastRequestMultiData) {
		this.lastRequestMultiData = lastRequestMultiData;
	}

	public PLHttpSatelite(){
		
	}
	
	public PLHttpDataHandler getHandler() {
		return dataHandler;
	}
	public void setHandler(PLHttpDataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}
	public PLTaskMgr getTaskMgr() {
		return taskMgr;
	}
	public void setTaskMgr(PLTaskMgr taskMgr) {
		this.taskMgr = taskMgr;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}

	public PLHttpSatelite(String addr, PLHttpDataHandler dataHandler, PLTaskMgr taskMgr){
		this.addr = addr;
		this.dataHandler = dataHandler;
		this.taskMgr = taskMgr;
	}
	
	public void sendMultiData(final TAMultiData data){
		final String targetAddr = getAddr();
		taskMgr.runTask(new Runnable() {
			@Override public void run() {
				sendViaHttpMultiPost( targetAddr, data, true);
			}
		});
	}
	
	public void sendData(final String data, final boolean uiTask){
		final String targetAddr = getAddr();
		taskMgr.runTask(new Runnable() {
			@Override public void run() {
				sendViaHttpPost(targetAddr, data, uiTask);
			}
		});
	}
	
	public static interface OnHttpGetDone{
		public void done(String url, String respose, int statusCode);
	}
	
	public static interface OnImageLoaded{
		public void done(String url, Bitmap bitmap);
	}
	
	public static void doHttpGet(final String url, final OnHttpGetDone handler){
		new Thread(new Runnable() {
			@Override public void run() {
				String response = null;
				int code = -1;
				try {
				    HttpClient client = new DefaultHttpClient();
				    HttpGet get = new HttpGet(url);
				    HttpResponse responseGet = client.execute(get);  
				    HttpEntity resEntityGet = responseGet.getEntity();
				    code = responseGet.getStatusLine().getStatusCode();
				    if (resEntityGet != null) {
				        response = EntityUtils.toString(resEntityGet);
				    }
				} catch (Exception e) {
					handler.done(url, null, 404);
				}
				handler.done(url, response, code);
			}
		}).start();
	}
	
	public static HttpURLConnection getHttpConnection(String urlText){
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlText);
			
			if(url.getProtocol().toLowerCase().equals("https")){
				PLHttpSatelite.trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection)url.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				conn = https;
			}else{
				conn = (HttpURLConnection)url.openConnection();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return conn;
	}
	
	public void writeToDOSWithString(DataOutputStream dos, String value) throws UnsupportedEncodingException, IOException{
		dos.write(value.getBytes("utf-8"));
	}
	
	public static boolean doDownloadWebFile(String url, String saveName, boolean saveToInternalStroage){
		try{
			
			String path = null;
			File newFile = null;
			if(saveToInternalStroage){
				File localPath = new File(OTOApp.getInstance().getContext().getFilesDir(), "files");
				if(localPath.exists() == false) localPath.mkdirs();
				newFile = new File(localPath, saveName);
			}else{
				path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/opentalkon/raw/" + saveName;
				if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false){
					return false;
				}
				newFile = new File(path);
				PLEtcUtilMgr.makeDirectoryOnly(newFile);
			}
			
			String utfFileName = URLEncoder.encode(url,"utf-8");
			
			HttpURLConnection conn = PLHttpSatelite.getHttpConnection(utfFileName);
			conn.setConnectTimeout(5000);
			conn.connect();
			
			byte[] data = new byte[1024];
			InputStream is = conn.getInputStream();
			
			FileOutputStream fos = new FileOutputStream(newFile);
			while(true){
				int read = is.read(data, 0, 1024);
				if(read <= 0) break;
				fos.write(data, 0, read);
			}
			fos.close();
			is.close();
			conn.disconnect();
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	public void sendViaHttpMultiPost(String addr, TAMultiData data, boolean uiTask){
		lastRequestMultiData = data;
		lastResultData = "";
		for(int tryCnt=1 ; tryCnt <= RETRY_CNT;++tryCnt){
			try{
				HttpURLConnection conn = getHttpConnection(addr); 
		
				conn.setDoInput(true);
				conn.setDoOutput(true);
		
				conn.setUseCaches(false); 
		
				conn.setRequestMethod("POST");
		
				conn.setRequestProperty("Connection","Keep-Alive"); 
				conn.setRequestProperty("Content-Type","multipart/form-data;boundary="+ data.generateBoundaryValue());
				conn.setRequestProperty("Content-Transfer-Encoding", "gzip");
		
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				Map<String, Object> dataMaps = data.getListOfItem();
				dataMaps.put("package_version", String.valueOf(OTOApp.getInstance().getVersionCode()));
				for(Entry<String, Object> entry : dataMaps.entrySet()){
					writeToDOSWithString(dos, "--" + data.getBoundaryValue() + "\r\n");
					Object value = entry.getValue();
					if(value instanceof File){
						File file = (File)value;
						writeToDOSWithString(dos, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\";filename=\""+ file.getAbsolutePath() +"\"" + "\r\n\r\n");
						FileInputStream fis = new FileInputStream(file);
						
						byte[] buffer = new byte[1024];
						while(true){
							int byteRead = fis.read(buffer, 0, 1024);
							if(byteRead <= 0) break;
							dos.write(buffer,0,byteRead);
						}
						fis.close();
					}
					if(value instanceof String){
						String val = (String)value;
						writeToDOSWithString(dos, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
						writeToDOSWithString(dos, val);
					}
					writeToDOSWithString(dos, "\r\n");
				}
				writeToDOSWithString(dos, "--" + data.getBoundaryValue() + "--" + "\r\n"); 
				dos.flush();
				dos.close();
				
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				lastResultData = sb.toString();
				break;
			}catch(IOException ex){
	        	ex.printStackTrace();
	        	lastException = ex;
	        	if(tryCnt == RETRY_CNT){
	        		handleException(lastException, data, uiTask, addr);
	        		return;
	        	}
	        	try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	continue;
			}
		}
		if(lastResultData.length() == 0){
			lastException = new Exception("Received Packet size is zero");
			handleException(lastException, data, uiTask, addr);
		}else{
			handleData(lastResultData, uiTask);
		}
	}
	
	 final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() { 
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			} 
	    }; 
	
	private static void trustAllHosts() { 
        // Create a trust manager that does not validate certificate chains 
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() { 
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                        return new java.security.cert.X509Certificate[] {}; 
                } 
 
                @Override 
                public void checkClientTrusted( 
                        java.security.cert.X509Certificate[] chain, 
                        String authType) 
                        throws java.security.cert.CertificateException { 
                    // TODO Auto-generated method stub 
                     
                } 
 
                @Override 
                public void checkServerTrusted( 
                        java.security.cert.X509Certificate[] chain, 
                        String authType) 
                        throws java.security.cert.CertificateException { 
                    // TODO Auto-generated method stub 
                     
                } 
        } }; 
 
        // Install the all-trusting trust manager 
        try { 
                SSLContext sc = SSLContext.getInstance("TLS"); 
                sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
                HttpsURLConnection 
                                .setDefaultSSLSocketFactory(sc.getSocketFactory()); 
        } catch (Exception e) { 
                e.printStackTrace(); 
        } 
    } 
	
	public void sendViaHttpPost(String addr, String data, boolean uiTask) {
		lastRequestData = data;
		lastResultData = "";
		for(int tryCnt=1 ; tryCnt <= RETRY_CNT;++tryCnt){
			try {
				URLConnection con = getHttpConnection(addr);
				
				con.setRequestProperty("content-type", "text/plain");
				con.setRequestProperty("charset", "utf-8");
				con.setDoOutput(true);
				con.setConnectTimeout(DEFAULT_HTTP_CONNECTION_TIME_OUT);
	
				OutputStreamWriter wr = new OutputStreamWriter(
						con.getOutputStream());
				
				wr.write(data);
				wr.flush();
	
				BufferedReader rd = null;
	
				rd = new BufferedReader(new InputStreamReader(con.getInputStream(),
						"UTF-8"));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				lastResultData = sb.toString();
				break;
			} catch (IOException ex) {
	        	ex.printStackTrace();
	        	lastException = ex;
	        	if(tryCnt == RETRY_CNT){
	        		handleException(lastException, data, uiTask, addr);
	        		return;
	        	}
	        	try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	continue;
			}
		}
		if(lastResultData.length() == 0){
			lastException = new Exception("Received Packet size is zero");
			handleException(lastException, data, uiTask, addr);
		}else{
			handleData(lastResultData, uiTask);
		}
	}
	
	public void handleData(final String data, boolean uiTask){
		if(uiTask && handler != null){
    		handler.post(new Runnable() {
				@Override public void run() {
					if(dataHandler != null){
						dataHandler.onHttpDataReceived(data);
					}
				}
			});
		}else{
			if(dataHandler != null){
				dataHandler.onHttpDataReceived(data);
			}
		}
	}
	
	public void handleException(final Exception ex, final String data ,boolean uiTask, final String addr){
		if(uiTask && handler != null){
    		handler.post(new Runnable() {
				@Override public void run() {
					if(dataHandler != null){
						dataHandler.onHttpError(ex, data, addr);
					}
				}
			});
		}else{
			if(dataHandler != null){
				dataHandler.onHttpError(ex, data, addr);
			}
		}		
	}
	public void handleException(final Exception ex, final TAMultiData data ,boolean uiTask, final String addr){
		if(uiTask && handler != null){
    		handler.post(new Runnable() {
				@Override public void run() {
					if(dataHandler != null){
						dataHandler.onHttpError(ex, data, addr);
					}
				}
			});
		}else{
			if(dataHandler != null){
				dataHandler.onHttpError(ex, data, addr);
			}
		}		
	}
}
