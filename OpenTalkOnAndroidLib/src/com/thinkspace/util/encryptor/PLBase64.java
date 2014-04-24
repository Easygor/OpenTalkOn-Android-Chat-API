package com.thinkspace.util.encryptor;

/**
 * @author TY 
 * @description This is implement of base64-encoding-decoding to speed up.  
 */
public class PLBase64 {
	static char[] numToCharMap = new char[64];
	static byte[] charToNumMap = new byte[128];
	static{
		int cnt = 0;
		for(char ch='A';ch<='Z';++ch){ numToCharMap[cnt++] = ch; }
		for(char ch='a';ch<='z';++ch){ numToCharMap[cnt++] = ch; }
		for(char ch='0';ch<='9';++ch){ numToCharMap[cnt++] = ch; }
		numToCharMap[cnt++] = '+';
		numToCharMap[cnt++] = '/';
		for(byte i=0;i<64;++i){ charToNumMap[numToCharMap[i]] = i; }
	}
	
	public static String encode(byte[] rawData){
		int len = rawData.length;
		StringBuilder sb = new StringBuilder(((len + 2) / 3) * 4);
		for(int i=0;i<len;i+=3){
			int t1 = rawData[i] & 0xff;
			int t2 = i+1 < len ? rawData[i+1] & 0xff : 0;
			int t3 = i+2 < len ? rawData[i+2] & 0xff : 0;
			
			char o1 = numToCharMap[(t1>>>2)];
			char o2 = numToCharMap[(((t1&0x3)<<4) + (t2>>>4))];
			char o3 = numToCharMap[(((t2&0xF)<<2) + (t3>>>6))];	
			char o4 = numToCharMap[(t3&0x3F)];
			
			sb.append(o1).append(o2);
			if(i+1 < len) {sb.append(o3);}
			else {sb.append('=');}
			
			if(i+2 < len) {sb.append(o4);}
			else {sb.append('=');}
		}
		return sb.toString();
	}
	
	
	public static byte[] decode(String encData){
		int len = encData.length();
		
		//Check length of encoded data
		if(len == 0)return new byte[0];
		if(len % 4 != 0){throw new IllegalArgumentException();}
		for(;len>=1&&encData.charAt(len-1)=='=';--len);
		if(len == 0){throw new IllegalArgumentException();}
		
		//Suppose that datas always are encoded correctly. If not so, that it might be taken unexpected crash.
		int cnt = 0;
		int retLen = len * 3 / 4; 
		byte[] ret = new byte[retLen];
		for(int i=0;i<len;i+=4){
			int t1 = charToNumMap[encData.charAt(i)];
			int t2 = charToNumMap[encData.charAt(i+1)];
			int t3 = i+2 < len ? charToNumMap[encData.charAt(i+2)] : 0;
			int t4 = i+3 < len ? charToNumMap[encData.charAt(i+3)] : 0;
			
			byte o1 = (byte)((t1<<2)|(t2>>>4));
			byte o2 = (byte)(((t2&0xf)<<4) | (t3 >>> 2));
			byte o3 = (byte)(((t3&0x3) << 6) | t4);
			
			ret[cnt++] = o1;
			if(cnt < retLen){ret[cnt++] = o2;}
			if(cnt < retLen){ret[cnt++] = o3;}
		}
		return ret;
	}
}
