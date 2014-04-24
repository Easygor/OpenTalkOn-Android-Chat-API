package com.thinkspace.util.encryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PLEncryptor extends Encryptor {

	private SecretKeySpec skeySpec = null;

	public PLEncryptor(byte[] key) {
		initialize(key);
	}

	public PLEncryptor(String key) {
		initialize(key.getBytes());
	}

	@Override
	public byte[] encrypt(byte[] data) {
		// TODO Auto-generated method stub
		byte[] encrypted = null;

		if (skeySpec != null && data != null) {
			try {
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

				encrypted = cipher.doFinal(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return encrypted;
	}

	@Override
	public byte[] decrypt(byte[] data) {
		byte[] original = null;

		if (skeySpec != null && data != null) {
			try {
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);

				original = cipher.doFinal(data);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return original;
	}

	@Override
	public boolean initialize(byte[] key) {
		// TODO Auto-generated method stub
		skeySpec = new SecretKeySpec(key, "AES");

		return true;
	}
}
