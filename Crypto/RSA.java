package Crypto;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSA {
	private static Map<Integer, String> keyMap = new HashMap<Integer, String>();
	public static void main(String[] args) throws Exception {
		genKeyPair();
		String message = "df7238200";
		long time1 = System.currentTimeMillis();
		for(int i = 0; i < 20000; i++) {
			encrypt(message,keyMap.get(0));
		}
		long time2 = System.currentTimeMillis();
		System.out.println(time2-time1);
	}

	public static void genKeyPair() throws NoSuchAlgorithmException {   
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");  
		keyPairGen.initialize(512,new SecureRandom());  
		KeyPair keyPair = keyPairGen.generateKeyPair();  
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); 
		String publicKeyString = new String(Base64.getEncoder().encodeToString(publicKey.getEncoded()));  
		String privateKeyString = new String(Base64.getEncoder().encodeToString((privateKey.getEncoded())));  
		keyMap.put(0,publicKeyString);  
		keyMap.put(1,privateKeyString);  
	}  

	public static String encrypt( String str, String publicKey ) throws Exception{
		byte[] decoded = Base64.getDecoder().decode(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	}

	public static String decrypt(String str, String privateKey) throws Exception{
		byte[] inputByte = Base64.getDecoder().decode(str.getBytes("UTF-8"));
		byte[] decoded = Base64.getDecoder().decode(privateKey);  
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));  
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}

}
