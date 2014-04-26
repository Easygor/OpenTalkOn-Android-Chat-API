package com.thinkspace.opentalkon.satelite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpStatus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.os.Handler;

import com.thinkspace.common.satelite.PLHttpSatelite;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.util.encryptor.Hash;

public class TAImgDownloader {
	public final static int THREAD_POOL_SIZE = 5;
	
	downloadThread imgDownThread;
	Handler handler;
	
	Map<BitmapInfo, Bitmap> cacheMap = new ConcurrentHashMap<BitmapInfo, Bitmap>();
	Map<BitmapInfo, Boolean> pendingMap = new ConcurrentHashMap<BitmapInfo, Boolean>();
	
	ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	public static class BitmapInfo{
		public String uri;
		public boolean fullScreen;
		
		public BitmapInfo(String uri, boolean fullScreen){
			this.uri = uri;
			this.fullScreen = fullScreen;
		}
		
		@Override
		public int hashCode() {
			return 0;
		}
		@Override
		public boolean equals(Object o) {
			if(o instanceof BitmapInfo){
				BitmapInfo other = (BitmapInfo) o;
				return uri.equals(other.uri) && fullScreen == other.fullScreen;
			}
			return false;
		}
	}
	
	public TAImgDownloader(Handler handler){
		this.handler = handler;
		startImgDownThread();
	}
	
	public ExecutorService getThreadPool() {
		return threadPool;
	}
	
	public Map<BitmapInfo, Bitmap> getCacheMap() {
		return cacheMap;
	}

	public Map<BitmapInfo, Boolean> getPendingMap() {
		return pendingMap;
	}

	public void flushCache(){
		for(Entry<BitmapInfo, Bitmap> entry : cacheMap.entrySet()){
			entry.getValue().recycle();
		}
		cacheMap.clear();
		pendingMap.clear();
		System.gc();
	}

	public Handler getUIHandler(){
		return handler;
	}
	
	public void restartThreadPool(){
		threadPool.shutdownNow();
		imgDownThread.getpMsgs().clear();
		threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		
		startImgDownThread();
	}
	
	public void startImgDownThread(){
		if(imgDownThread != null){
			imgDownThread.interrupt();
		}
		imgDownThread = new downloadThread();
		imgDownThread.setName("ImageDownLoadThread");
		imgDownThread.start();
	}
	
	public void stopThread(){
		imgDownThread.interrupt();
		try {
			imgDownThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static Bitmap decodeBitmapProperly(String path, boolean fullScreen){
		try{
			File file = new File(path);
			long fileLength = file.length();
			Bitmap ret = null;
			if(fullScreen){
				if(fileLength <= 1024L * 3000L){
					ret = BitmapFactory.decodeFile(path);
				}else{
					Options op = new Options();
					op.inSampleSize = 2;
					ret = BitmapFactory.decodeFile(path, op);
				}
			}else{
				if(fileLength <= 1024L * 100L){
					ret = BitmapFactory.decodeFile(path);
				}else if(fileLength <= 1024L * 500L){
					Options op = new Options();
					op.inSampleSize = 2;
					ret = BitmapFactory.decodeFile(path, op);
				}else if(fileLength <= 1024L * 2000L){
					Options op = new Options();
					op.inSampleSize = 4;
					ret = BitmapFactory.decodeFile(path, op);
				}else{
					Options op = new Options();
					op.inSampleSize = 8;
					ret = BitmapFactory.decodeFile(path, op);
				}
			}
			return ret;
		}catch(OutOfMemoryError ex){
			return null;
		}
	}
	
	public static boolean canAllocateBitmapMem(int pendingMemory){
		Runtime rt = Runtime.getRuntime();
        if((int)((rt.maxMemory() - rt.totalMemory() + rt.freeMemory())/1048576) <= pendingMemory){
        	return false;
        }
        return true;
	}
	
	private static Bitmap decodeBitmap(String path, boolean fullScreen){
		try{
	        if(canAllocateBitmapMem(15) == false) return null;
			Bitmap ret = decodeBitmapProperly(path, fullScreen);
			return ret;
		}catch(OutOfMemoryError ex){
			return null;
		}
	}
	
	private synchronized static Bitmap decodeBitmap(InputStream is){
		try{
			if(canAllocateBitmapMem(15) == false) return null;
			Bitmap ret = BitmapFactory.decodeStream(is);
			return ret;
		}catch(OutOfMemoryError ex){
			return null;
		}
	}
	
	class ImgDownloadWork implements Runnable{
		doImgDownloadElem item;
		
		public ImgDownloadWork(doImgDownloadElem item){
			this.item = item;
		}

		@Override public void run() {
			try{
				if(item.getImg_url().startsWith("file://")){
					final Bitmap bitmap = decodeBitmapProperly(item.getImg_url().replace("file://", ""), item.fullScreen);
					getUIHandler().post(new Runnable() {
						@Override
						public void run() {
							if(bitmap != null){
								if(item.getHandler() != null){
									item.getHandler().onHttpImagePacketReceived(item.getImg_url(), bitmap);
								}
								if(item.getHandlerEx() != null){
									item.getHandlerEx().onHttpImagePacketReceived(item.getImg_url(), bitmap, item.getGiveData());
								}
							}else{
								flushCache();
								if(item.getHandler() != null){
									item.getHandler().onHttpImageException(new Exception("Image is Null"));
								}
							}
						}
					});
					return;
				}
				
				String realURL = item.getImg_url().replace(" ", "%20");
				boolean canSave = false;
				if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
					canSave = true;
				}
				if(canSave){
					String fileName = Hash.getHexHash(realURL).replace("/", "0").replace("\\", "1");
					String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.thinkspace.opentalkon/" + fileName;
					
					final File newFile = new File(path);
					PLEtcUtilMgr.makeDirectoryOnly(newFile);
					
					Bitmap existBitmap = null;
					boolean exist = newFile.exists();
					if(exist){
						if(newFile.length() == 0L){
							newFile.delete();
							exist = false;
						}else{
							existBitmap = decodeBitmap(newFile.getAbsolutePath(), item.fullScreen);
							if(existBitmap == null){
								flushCache();
								newFile.delete();
								exist = false;
							}
						}
					}
					
					if(exist){
						final Bitmap bitmap = existBitmap;
						getUIHandler().post(new Runnable() {
							@Override
							public void run() {
								if(bitmap != null){
									if(item.getHandler() != null){
										item.getHandler().onHttpImagePacketReceived(item.getImg_url(), bitmap);
									}
									if(item.getHandlerEx() != null){
										item.getHandlerEx().onHttpImagePacketReceived(item.getImg_url(), bitmap, item.getGiveData());
									}
								}else{
									flushCache();
									if(item.getHandler() != null){
										item.getHandler().onHttpImageException(new Exception("Image is Null"));
									}
								}
							}
						});
					}else{
						final Bitmap bitmap = downloadImage(realURL, newFile, item.fullScreen);
						getUIHandler().post(new Runnable() {
							@Override
							public void run() {
								if(bitmap != null){
									if(item.getHandler() != null){
										item.getHandler().onHttpImagePacketReceived(item.getImg_url(), bitmap);
									}
									if(item.getHandlerEx() != null){
										item.getHandlerEx().onHttpImagePacketReceived(item.getImg_url(), bitmap, item.getGiveData());
									}
								}else{
									flushCache();
									if(item.getHandler() != null){
										item.getHandler().onHttpImageException(new Exception("Image is Null"));
									}
								}
							}
						});
					}
				}else{
					final Bitmap bitmap = downloadImage(realURL, null, item.fullScreen);
					getUIHandler().post(new Runnable() {
						@Override
						public void run() {
							if(bitmap != null){
								if(item.getHandler() != null){
									item.getHandler().onHttpImagePacketReceived(item.getImg_url(), bitmap);
								}
								if(item.getHandlerEx() != null){
									item.getHandlerEx().onHttpImagePacketReceived(item.getImg_url(), bitmap, item.getGiveData());
								}
							}else{
								flushCache();
								if(item.getHandler() != null){
									item.getHandler().onHttpImageException(new Exception("Image is Null"));
								}
							}
						}
					});
				}
			}catch(final Exception ex){
				getUIHandler().post(new Runnable() {
					@Override
					public void run() {
						if(item.getHandler() != null){
							item.getHandler().onHttpImageException(ex);
						}
					}
				});
			}
		}
	};
	
	public static Bitmap downloadImage(String url, File saveTarget, boolean fullScreen) throws IOException{
		return downloadImage(url, saveTarget, fullScreen, 0);
	}
	
	public static Bitmap downloadImage(String url, File saveTarget, boolean fullScreen, int depth) throws IOException{
		if(depth >= 255) return null;
		HttpURLConnection conn = PLHttpSatelite.getHttpConnection(url);
		conn.setConnectTimeout(5000);
		conn.connect();
		
		int statusCode = conn.getResponseCode();
		if(statusCode != HttpStatus.SC_OK){
			String nextUrl = conn.getHeaderField("Location");
			if(nextUrl != null){
				return downloadImage(nextUrl, saveTarget, fullScreen, depth + 1);
			}else{
				return null;
			}
		}
		
		byte[] data = new byte[1024];
		InputStream is = conn.getInputStream();
		try{
			if(saveTarget != null){
				FileOutputStream fos = new FileOutputStream(saveTarget);
				while(true){
					int read = is.read(data, 0, 1024);
					if(read <= 0) break;
					fos.write(data, 0, read);
				}
				fos.close();
				return decodeBitmap(saveTarget.getAbsolutePath(), fullScreen);
			}else{
				return decodeBitmap(is);
			}
		}finally{
			is.close();
			conn.disconnect();
		}
	}
	
	class doImgDownloadElem{
		String img_url;
		TAImageDataHandler handler;
		TAImageDataHandlerEx handlerEx;
		Object giveData;
		boolean fullScreen;
		
		public doImgDownloadElem(String img_url, TAImageDataHandler handler) {
			this.img_url = img_url;
			this.handler = handler;
		}
		public doImgDownloadElem(String img_url, TAImageDataHandlerEx handlerEx, Object giveData) {
			this.img_url = img_url;
			this.handlerEx = handlerEx;
			this.giveData = giveData;
		}
		
		public boolean isFullScreen() {
			return fullScreen;
		}
		public void setFullScreen(boolean fullScreen) {
			this.fullScreen = fullScreen;
		}
		public TAImageDataHandlerEx getHandlerEx() {
			return handlerEx;
		}
		public void setHandlerEx(TAImageDataHandlerEx handlerEx) {
			this.handlerEx = handlerEx;
		}
		public Object getGiveData() {
			return giveData;
		}
		public void setGiveData(Object giveData) {
			this.giveData = giveData;
		}
		public String getImg_url() {
			return img_url;
		}
		public void setImg_url(String img_url) {
			this.img_url = img_url;
		}
		public TAImageDataHandler getHandler() {
			return handler;
		}
		public void setHandler(TAImageDataHandler handler) {
			this.handler = handler;
		}
	}
	
	abstract class BaseTask extends Thread{
		Queue<doImgDownloadElem> pMsgs;
		int sleepTime;
		
		public Queue<doImgDownloadElem> getpMsgs() { return pMsgs; }

		public BaseTask(int sleepTime) {
			this.sleepTime = sleepTime;
			this.pMsgs = new LinkedBlockingQueue<doImgDownloadElem>(); 
		}

		@Override
		public void run() {
			try{
				while(true){
					Thread.sleep(sleepTime);
					if(pMsgs.isEmpty()){
						synchronized (pMsgs) {
							pMsgs.wait();
						}
					}
					safeRun();
				}
			}catch(final Exception ex){
				ex.printStackTrace();
			}
		}
		
		public abstract void safeRun();
	}
	
	public class downloadThread extends BaseTask{
		public downloadThread() {
			super(1);
		}

		@Override
		public void safeRun() {
			while(pMsgs.isEmpty() == false){
				final doImgDownloadElem item = pMsgs.poll();
				threadPool.execute(new ImgDownloadWork(item));
			}
		}
	}
	
	public void requestImgDownload(String img_url, boolean fullScreen, TAImageDataHandler handler){
		try{
			Queue<doImgDownloadElem> msgs = imgDownThread.getpMsgs();
			doImgDownloadElem elem = new doImgDownloadElem(img_url, handler);
			elem.setFullScreen(fullScreen);
			msgs.add(elem);
			if(imgDownThread.getState() == State.WAITING){
				synchronized (msgs) {
					msgs.notify();
				}
			}
		}catch(final Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void requestImgDownload(String img_url, TAImageDataHandler handler){
		try{
			Queue<doImgDownloadElem> msgs = imgDownThread.getpMsgs();
			msgs.add(new doImgDownloadElem(img_url, handler));
			if(imgDownThread.getState() == State.WAITING){
				synchronized (msgs) {
					msgs.notify();
				}
			}
		}catch(final Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void requestImgDownload(String img_url, Object giveData, TAImageDataHandlerEx handler){
		try{
			Queue<doImgDownloadElem> msgs = imgDownThread.getpMsgs();
			msgs.add(new doImgDownloadElem(img_url, handler,giveData));
			if(imgDownThread.getState() == State.WAITING){
				synchronized (msgs) {
					msgs.notify();
				}
			}
		}catch(final Exception ex){
			ex.printStackTrace();
		}
	}
}
