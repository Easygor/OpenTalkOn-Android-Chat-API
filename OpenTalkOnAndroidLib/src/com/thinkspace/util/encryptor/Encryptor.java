package com.thinkspace.util.encryptor;

public abstract class Encryptor {
	public abstract boolean initialize(byte[] key);
	public abstract byte[] encrypt(byte[] data);
	public abstract byte[] decrypt(byte[] data);
	
	public static String asHex(byte[] buf) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10) {
				strbuf.append("0");
			}

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}

	public static byte[] asBytes(String buf) {
		byte[] b = new byte[buf.length() / 2];
		int i;
		for (i = 0; i < buf.length() / 2; i++) {
			String str = buf.substring(i * 2, i * 2 + 2);
			b[i] = (byte) (Integer.parseInt(str, 16) & 0xff);
		}
		return b;
	}
}
