package com.thinkspace.opentalkon.javapush;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.pushpackets.protobuf.Packets.Broker;
import com.thinkspace.pushpackets.protobuf.Packets.BrokerGetConnServer;
import com.thinkspace.pushpackets.protobuf.Packets.Pong;
import com.thinkspace.pushpackets.protobuf.Packets.PushClientLogin;
import com.thinkspace.pushpackets.protobuf.Packets.PushClientLogin_Resp.Response;
import com.thinkspace.pushpackets.protobuf.Packets.PushService;
import com.thinkspace.pushpackets.protobuf.Packets.PushServiceClient;
import com.thinkspace.pushpackets.protobuf.Packets.Server;
import com.thinkspace.pushservice.appclient.BasePacket;
import com.thinkspace.pushservice.appclient.PushClientBase;

public class JavaTokenClient extends PushClientBase{
	public final static int CONN_TIMEOUT = 10000;
	static JavaTokenClient instance;
	boolean started;
	
	Handler handler;
	TokenClientMain tokenClientMainThread = null;
	
	Object syncObject1 = new Object();
	Object syncObject2 = new Object();
	
	public static JavaTokenClient getInstance() {
		return instance;
	}
	
	public static JavaTokenClient newInstance(Handler handler, Context context){
		instance = new JavaTokenClient(handler, context);
		return instance;
	}
	
	public JavaTokenClient(Handler handler, Context context){
		super(handler, context);
	}
	
	@Override
	public void onConnected() {
		
	}

	@Override
	public void onStopped() {
		started = false;
	}

	@Override
	public boolean sendPacket(PushService packet) {
		if(tokenClientMainThread == null){
			return false;
		}
		if(tokenClientMainThread.getPushSocket() == null){
			return false;
		}
		
		try {
			sendPacket(tokenClientMainThread.getPushSocket(), packet);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void stopTokenClient(){
		synchronized(syncObject2){
			if(stopTokenClientAsync()){
				try {
					if(tokenClientMainThread != null){
						tokenClientMainThread.join(30000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			tokenClientMainThread = null;
		}
	}
	
	public void restart(String token){
		synchronized(syncObject1){
			if(started){
				stopTokenClient();
			}
			startTokenClientAsync();
		}
	}

	private boolean stopTokenClientAsync() {
		if(tokenClientMainThread == null) return false;
		
		tokenClientMainThread.setStopFlag(true);
		tokenClientMainThread.interrupt();
		if(tokenClientMainThread.getPushSocket() != null){
			Thread closeSocketThread = new Thread(new Runnable() {
				@Override public void run() {
					try {
						if(tokenClientMainThread.getPushSocket() != null){
							tokenClientMainThread.getPushSocket().close();
						}
					} catch (IOException e) {}
					tokenClientMainThread.setPushSocket(null);
				}
			});
			closeSocketThread.setName("TokenClientStopThread");
			closeSocketThread.start();
		}
		return true;
	}
	
	private void startTokenClientAsync(){
		started = true;
		tokenClientMainThread = new TokenClientMain();
		tokenClientMainThread.setPriority(Thread.MAX_PRIORITY);
		tokenClientMainThread.setName("TokenClientMainThread");
		tokenClientMainThread.start();
	}
	
	@Override
	public void restartTokenClient() {
		restart(OTOApp.getInstance().getToken());
	}
	
	public class TokenClientMain extends Thread{
		boolean stopFlag;
		SSLSocket pushSocket;
		int notOnlineCount;
		
		public boolean isStopFlag() {
			return stopFlag;
		}

		public void setStopFlag(boolean stopFlag) {
			this.stopFlag = stopFlag;
		}

		public SSLSocket getPushSocket() {
			return pushSocket;
		}

		public void setPushSocket(SSLSocket pushSocket) {
			this.pushSocket = pushSocket;
		}
		public int getNotOnlineCount() {
			return notOnlineCount;
		}

		public void setNotOnlineCount(int notOnlineCount) {
			this.notOnlineCount = notOnlineCount;
		}
		
		public TokenClientMain(){
			stopFlag = false;
			notOnlineCount = 0;
			pushSocket = null;
		}
		
		public Server processBroker(String brokerHost, int brokerPort) throws IOException{
			Socket socket = new Socket();
			InetSocketAddress isa = new InetSocketAddress(brokerHost, brokerPort);
			socket.connect(isa, CONN_TIMEOUT);
			socket.setReuseAddress(true);
			socket.setTcpNoDelay(true);
			
			sendPacket(socket, Broker.newBuilder().setGetConnServer(
					BrokerGetConnServer.newBuilder().setAppVersion(OTOApp.getInstance().getVersionCode())
							.setIsSsl(true)).build());
			BasePacket packet = readPacket(socket);
			Broker brokerPacket = Broker.parseFrom(packet.getBody());
			
			if(brokerPacket.hasGetConnServerResp() == false){
				throw new IOException("GetConnServer Failed");
			}
			socket.close();
			return brokerPacket.getGetConnServerResp().getServer();
		}
		
		public void processPush(Server server) throws Exception{
			boolean doConnect = true;
			
			while(stopFlag == false){
				if(doConnect){
		            KeyStore trustStore = KeyStore.getInstance("BKS");
		            InputStream trustStoreStream = OTOApp.getInstance().context.getResources().openRawResource(R.raw.oto_keystore);
		            trustStore.load(trustStoreStream, "thinkspace".toCharArray());
		     
		            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		            trustManagerFactory.init(trustStore);
		     
		            SSLContext sslContext = SSLContext.getInstance("TLS");
		            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
		            SSLSocketFactory factory = sslContext.getSocketFactory();
		            SSLSocket sslSocket = (SSLSocket) factory.createSocket(server.getIpAddr(), server.getPort());
					pushSocket = sslSocket;
					
					sendPacket(sslSocket, PushService.newBuilder().setClient(PushServiceClient.newBuilder().setClientLogin(
							PushClientLogin.newBuilder().setToken(OTOApp.getInstance().getToken()).setAppCode(OTOApp.getInstance().getAppCode()))).build());
					doConnect = false;
				}
				BasePacket packet = readPacket(pushSocket);
				PushService pushPacket = PushService.parseFrom(packet.getBody());
				if(pushPacket.hasPing()){
					sendPacket(pushSocket, PushService.newBuilder().setPong(Pong.newBuilder()).build());
				}else if(pushPacket.hasClient()){
					if(pushPacket.getClient().hasClientLoginResp()){
						if(pushPacket.getClient().getClientLoginResp().getResp().equals(Response.FAIL)){
							OTOApp.getInstance().getPref().getLastPushLoginFailTime().setValue(System.currentTimeMillis());
							stopTokenClientAsync();
							break;
						}else{
							
						}
					}else{
						onReceived(pushPacket);
					}
				}
			}
		}

		@Override public void run() {
			stopFlag = false;
			while(stopFlag == false && interrupted() == false){
				try{
					Server res = processBroker(OTOApp.getInstance().getContext().getString(R.string.oto_broker_appclient_host),
							Integer.valueOf(OTOApp.getInstance().getContext().getString(R.string.oto_broker_appclient_port)));
					notOnlineCount = 0;
					OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
					if(res.getPort() == -1) break;
					processPush(res);
				}catch(Exception ex){
					if(stopFlag) break;
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						break;
					}
					
					ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = cm.getActiveNetworkInfo();
					if(netInfo == null || cm.getActiveNetworkInfo().isConnected() == false){
						if(++notOnlineCount >= 10){
							notOnlineCount = 0;
							break;
						}
					}
				}
			}
			onStopped();
		}
	}
	
	public BasePacket readPacket(Socket socket) throws IOException{
		BasePacket basePacket = new BasePacket();
		InputStream is = socket.getInputStream();
		readData(is, basePacket.getHeader(), 0, BasePacket.headerSize);
		basePacket.decodeHeader();
		readData(is, basePacket.getBody(), 0, basePacket.getBodySize());
		return basePacket;
	}

	public void sendPacket(Socket socket, PushService packet) throws IOException{
		BasePacket basePacket = new BasePacket();
		basePacket.setBody(packet.toByteArray());
		byte[] rawData = basePacket.getHeaderAndBody();
		sendData(socket, rawData);
	}
	
	public void sendPacket(Socket socket, Broker packet) throws IOException{
		BasePacket basePacket = new BasePacket();
		basePacket.setBody(packet.toByteArray());
		byte[] rawData = basePacket.getHeaderAndBody();
		sendData(socket, rawData);
	}
	
	public void readData(InputStream is, byte[] data, int w, int length) throws IOException{
		int pos = w;
		while(true){
			int nowRead = is.read(data, pos, length - pos - w);
			if(nowRead == 0) return;
			if(nowRead == -1){
				throw new IOException("End of Stream");
			}
			pos += nowRead;
		}
	}
	
	public void sendData(Socket socket, byte[] data) throws IOException{
		socket.getOutputStream().write(data);
	}
}
