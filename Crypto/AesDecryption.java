package Crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class AesDecryption {

	public static byte[] decrypt(byte[] content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//kgen.init(128, new SecureRandom(password.getBytes())); // Attention: this
			// code may throws exception in Linux! change to the following three sentences
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] result = cipher.doFinal(content);
			return result; // Ã÷ÎÄ

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
			String ciphertext = "8Rn3YEgUUD7IYyWU9klCNyqCJiuW6yFCS5aOVrIxysjV8FZXZpF75zPkw22u2sG8=";
			String password = "123";
			String content = Base64.getEncoder().encodeToString(AesDecryption.decrypt(Base64.getDecoder().decode(ciphertext), password));
		System.out.println(content);
	}
}
