package com.openunion.cordova.plugins.nlpos;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

  public static String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

  public static void main(String[] args) {
    // TODO Auto-generated method stub

    //String content="我是加密明文abceefg";
    String content="{\"pwd\":\"123456\"}";
    String key="123456";

//    System.out.println("加密前（16）："+byteToHexString(content.getBytes()));
//    System.out.println("加密前："+ content );
//    byte[ ] encrypted=AES_CBC_Encrypt(content.getBytes(), key.getBytes(), Constant.iv.getBytes());
//    System.out.println("加密后："+byteToHexString(encrypted));
//    byte[ ] decrypted=AES_CBC_Decrypt(encrypted, key.getBytes(), Constant.iv.getBytes());
//    System.out.println("解密后（16）："+byteToHexString(decrypted));
//    System.out.println("解密后："+  new String(decrypted ));
//
    System.out.println("加密前："+ content );
    String encrypted="";
    try {
      encrypted = encrypt(content, key, Constant.iv );
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("加密后："+ encrypted );
    String decrypted="";
    try {
      decrypted = decrypt( encrypted, key, Constant.iv );
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("解密后："+  decrypted );
  }

  public static String encryptAndroid (String data, String key, String iv) throws Exception {
    SecureRandom sr = new SecureRandom();
    Key secureKey = getKeyAndroid(key);
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes("UTF-8")));
    byte[] bt = cipher.doFinal(data.getBytes("UTF-8"));
    String strS = Base64Utils.encode(bt);
    return strS;
  }

  public static String decryptAndroid(String message, String key, String iv) throws Exception {
    SecureRandom sr = new SecureRandom();
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    Key secureKey = getKeyAndroid(key);
    cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes("UTF-8")));
    byte[] res = Base64Utils.decode(message);
    res = cipher.doFinal(res);
    return new String(res);
  }

  public static String encrypt(String data, String key, String iv) throws Exception {
    //Key secureKey = getKey(key);
    key = get16Key( key, 16 );
    SecretKeySpec secureKey = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes("UTF-8")));
    byte[] bt = cipher.doFinal(data.getBytes("UTF-8"));
    String strS = Base64Utils.encode(bt);
    return strS;
  }


  public static String decrypt(String message, String key, String iv) throws Exception {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    //Key secureKey = getKey(key);
    key = get16Key( key, 16 );
    SecretKeySpec secureKey = new SecretKeySpec(key.getBytes(), "AES");
    cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes("UTF-8")));
    byte[] res = Base64Utils.decode(message);
    res = cipher.doFinal(res);
    return new String(res);
  }

  public static String get16Key (String strKey, int length ) {
    char c = strKey.length()>0?strKey.charAt(0):'O';
    StringBuffer sb = new StringBuffer( String.valueOf(c));
    for (int i = 0; i < length; ++i) {
      sb.append(c);
    }

    String result = strKey + sb.toString();
    int alllength = result.length();
    return result.substring( 0,length);
  }

  public static Key getKeyAndroid (String strKey) {
    try {
      if (strKey == null) {
        strKey = "";
      }
      KeyGenerator _generator = KeyGenerator.getInstance("AES");
      SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG",  "Crypto");
      secureRandom.setSeed(strKey.getBytes());
      _generator.init(128, secureRandom);
      return _generator.generateKey();
    } catch (Exception e) {
      throw new RuntimeException(" 初始化密钥出现异常 ");
    }
  }

  public static Key getKey (String strKey) {
    try {
      if (strKey == null) {
        strKey = "";
      }
      KeyGenerator _generator = KeyGenerator.getInstance("AES");
      SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
      secureRandom.setSeed(strKey.getBytes());
      _generator.init(128, secureRandom);
      return _generator.generateKey();
    } catch (Exception e) {
      throw new RuntimeException(" 初始化密钥出现异常 ");
    }
  }

  public static String byteToHexString(byte[] bytes) {
    StringBuffer sb = new StringBuffer(bytes.length);
    String sTemp;
    for (int i = 0; i < bytes.length; i++) {
      sTemp = Integer.toHexString(0xFF & bytes[i]);
      if (sTemp.length() < 2)
        sb.append(0);
      sb.append(sTemp.toUpperCase());
    }
    return sb.toString();
  }
}
