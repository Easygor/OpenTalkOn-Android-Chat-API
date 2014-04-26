package com.thinkspace.pushservice.appclient;

import java.nio.ByteBuffer;

public class BasePacket {
	public final static int headerSize = 4;
	public final static int defaultBodySize = 1028;
	int bodySize;
	byte[] header;
	byte[] body;
	
	public BasePacket(){
		bodySize = -1;
		header = new byte[headerSize];
	}
	
	public int readInt(byte[] data, int w){
		int ret  = 0;
		ret += ((int)(data[w+0]&0xff)) << 24;
		ret += ((int)(data[w+1]&0xff)) << 16;
		ret += ((int)(data[w+2]&0xff)) << 8;
		ret += ((int)(data[w+3]&0xff));
		return ret;
	}
	
	public void writeInt(byte[] data, int w, int val){
		data[w+3] = (byte)((val)&0xff);
		data[w+2] = (byte)((val>>8)&0xff);
		data[w+1] = (byte)((val>>16)&0xff);
		data[w+0] = (byte)((val>>24)&0xff);
	}
	
	public void decodeHeader(){
		bodySize = readInt(header, 0);
		body = new byte[bodySize];
	}
	public void encodeHeader(int bodySize){
		this.bodySize = bodySize;
		writeInt(header, 0, bodySize);
	}
	public byte[] getHeader() { return header; }
	public byte[] getBody() { return body; }
	
	public byte[] getHeaderAndBody(){
		ByteBuffer ret = ByteBuffer.allocate(bodySize + headerSize);
		ret.put(header);
		ret.put(body);
		return ret.array();
	}
	
	public void setBody(byte[] body){
		encodeHeader(body.length);
		this.body = body;
	}
	
	public int getHeaderAndBodySize(){
		return headerSize + bodySize;
	}
	public int getBodySize(){
		return bodySize;
	}
}
