package com.openunion.cordova.plugins.nlpos;


/**
 * Created by lyzcw on 2017/9/20.
 */

import android.util.Log;
import android.content.Context;
import android.content.res.AssetManager;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.util.List;
import java.util.Calendar;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.ContentSigner;

/**
 * 需要注意的就是加密是有长度限制的，过长的话会抛异常！！！需要做分段加解密
 */
public final class RSAUtils {
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  private String showMsg = "";
  private Map map = new HashMap();

  private static String RSA = "RSA";

  public static void main(String args[]) throws Exception{
    RSAUtils rsaUtils = new RSAUtils();
//    rsaUtils.export("testkey1", "123456".toCharArray(), "123456".toCharArray(),"BKS","E:\\项目管理\\智能POS\\test\\oudata\\clientkeystore1.bks", "E:\\项目管理\\智能POS\\test\\oudata\\clientkeystore1.cert");
//    rsaUtils.changeKeyPass("E:\\项目管理\\智能POS\\test\\oudata\\clientkeystore1.bks", "123456", "testkey1", "888888","123456");
//    rsaUtils.changeStorePass("E:\\项目管理\\智能POS\\test\\oudata\\clientkeystore1.bks", "111111", "123456");
//    rsaUtils.addFromKeyStore("E:\\项目管理\\智能POS\\test\\oudata\\oukeystore.bks", "123456", "E:\\项目管理\\智能POS\\test\\oudata\\clientkeystore1.bks", "111111", "testkey1", "888888");
//    rsaUtils.removeFromKeyStore("E:\\项目管理\\智能POS\\test\\oudata\\oukeystore.bks", "123456", "testkey1");
//    rsaUtils.listKeyStore( "E:\\项目管理\\智能POS\\test\\oudata\\oukeystore.bks", "123456");
//    rsaUtils.generateClientKeyStore("testkey1", "E:\\项目管理\\智能POS\\test\\oudata\\oukeystore.bks", "E:\\项目管理\\智能POS\\test\\oudata\\clientkeystore1.bks", "123456", "111111",  "888888", "klzf", "openunion", 360 );
    rsaUtils.encryptFile("ouposkey", "123456", "123456", "BKS", "E:\\项目管理\\智能POS\\test\\oudata\\oukeystore.bks" , "E:\\项目管理\\智能POS\\test\\oudata\\testrule.rule", "E:\\项目管理\\智能POS\\test\\oudata\\testrule-e.rule", 245 );
//    rsaUtils.decryptFile("ouposkey", "123456", "123456", "BKS", "E:\\项目管理\\智能POS\\test\\oudata\\oukeystore.bks" , "E:\\项目管理\\智能POS\\test\\oudata\\testrule-e.rule", "E:\\项目管理\\智能POS\\test\\oudata\\testrule-d.rule", 256 );
//    RSAUtils export=new ExportPrivateKey();
//    export.keystoreFile=new File(args[0]);
//    export.keyStoreType=args[1];
//    export.password=args[2].toCharArray();
//    export.alias=args[3];
//    export.exportedFile=new File(args[4]);
//    export.export();
  }
  /**
   * 随机生成android客户端RSA密钥对，并导入服务端keystore(默认密钥长度为2048)
   * @param alias 证书别名，服务端keystore中要唯一
   * @param storePath 服务端keystore路径
   * @param storePass 服务端keystore密码
   * @param keyPass 新生成私钥保护密码
   * @param subjectDN 公钥证书主体名
   * @param issuerDN 公钥证书签发者
   * @param validity 证书有效期（天数）
   *
   * @return
   */
  public static boolean genKeypairToKeyStore(String alias, String storePath, String storePass, String keyPass, String subjectDN, String issuerDN, int validity )
  {
    KeyPair pair = generateRSAKeyPair(2048);
    PrivateKey priv = pair.getPrivate();
    PublicKey pub = pair.getPublic();
    try {
      KeyStore ks = KeyStore.getInstance("BKS");

      X509Certificate certificate = generateCertificate( pair, subjectDN, issuerDN, validity);
      Certificate[] certChain = new Certificate[1];
      certChain[0] = certificate;

      File storeFile = new File(storePath);
      if(storeFile.exists() ){
        ks.load(new FileInputStream(storePath), storePass.toCharArray());
        ks.setKeyEntry(alias, priv, keyPass.toCharArray(), certChain );
      }
      FileOutputStream writeStream = new FileOutputStream( storePath );
      ks.store(writeStream, storePass.toCharArray());
      writeStream.close();

    } catch (Exception e) {
      System.out.println("生成密钥对并写入keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * 随机生成android客户端RSA密钥对和keystore，并导入服务端keystore(默认密钥长度为2048)
   * @param alias 证书别名，服务端keystore中要唯一
   * @param serverStorePath 服务端keystore路径
   * @param clientStorePath 生成的客户端keystore路径
   * @param serverStorePass 服务端keystore密码
   * @param clientStorePass 客户端keystore密码
   * @param keyPass 新生成私钥保护密码
   * @param subjectDN 公钥证书主体名
   * @param issuerDN 公钥证书签发者
   * @param validity 证书有效期（天数）
   *
   * @return
   */
  public static boolean generateClientKeyStore(String alias, String serverStorePath, String clientStorePath, String serverStorePass, String clientStorePass,  String keyPass, String subjectDN, String issuerDN, int validity )
  {
    KeyPair pair = generateRSAKeyPair(2048);
    PrivateKey priv = pair.getPrivate();
    PublicKey pub = pair.getPublic();
    try {
      KeyStore serverKs = KeyStore.getInstance("BKS");
      KeyStore clientKs = KeyStore.getInstance("BKS");
      clientKs.load(null, clientStorePass.toCharArray());

      X509Certificate certificate = generateCertificate( pair, subjectDN, issuerDN, validity);
      Certificate[] certChain = new Certificate[1];
      certChain[0] = certificate;
      clientKs.setKeyEntry(alias, priv, keyPass.toCharArray(), certChain );
      FileOutputStream writeStream = new FileOutputStream( clientStorePath );
      clientKs.store(writeStream, clientStorePass.toCharArray());
      writeStream.close();

      File serverStoreFile = new File(serverStorePath);
      if(serverStoreFile.exists() ){
        serverKs.load(new FileInputStream(serverStorePath), serverStorePass.toCharArray());
        serverKs.setKeyEntry(alias, priv, keyPass.toCharArray(), certChain );

      }else{
        serverKs = clientKs;
      }
      FileOutputStream writeStream1 = new FileOutputStream( serverStorePath );
      serverKs.store(writeStream1, serverStorePass.toCharArray());
      writeStream1.close();

    } catch (Exception e) {
      System.out.println("产生秘钥对并写入keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * 将android客户端RSA密钥对字符串，并导入客户端端keystore(默认密钥长度为2048)
   * @param alias 证书别名，服务端keystore中要唯一
   * @param pubKey 公钥字符串
   * @param privKey 私钥字符串
   * @param keyStorePath keystore路径
   * @param keyStorePass keystore密码
   * @param keyPass 新生成私钥保护密码
   * @param subjectDN 公钥证书主体名
   * @param issuerDN 公钥证书签发者
   * @param validity 证书有效期（天数）
   *
   * @return
   */
  public boolean writeKeyStore( String alias, byte[] pubKey, byte[] privKey, String keyStorePath, String keyStorePass,  String keyPass, String subjectDN, String issuerDN, int validity )
  {
    try {
      PrivateKey priv = getPrivateKey( privKey);
      PublicKey pub = getPublicKey( pubKey );
      KeyPair pair = new KeyPair( pub, priv);
      KeyStore clientKs = KeyStore.getInstance("BKS");

      X509Certificate certificate = generateCertificate( pair, subjectDN, issuerDN, validity);
      Certificate[] certChain = new Certificate[1];
      certChain[0] = certificate;

      //keyStorePath = context.getDir("ks", context.MODE_PRIVATE ).getAbsolutePath() + "/" + keyStorePath;
      File keyStoreFile = new File( keyStorePath );
      if(keyStoreFile.exists() ){
        clientKs.load(new FileInputStream(keyStorePath), keyStorePass.toCharArray());
      }else{
        clientKs.load(null, keyStorePass.toCharArray());
      }
      clientKs.setKeyEntry(alias, priv, keyPass.toCharArray(), certChain );
      FileOutputStream writeStream = new FileOutputStream( keyStorePath );
      clientKs.store(writeStream, keyStorePass.toCharArray());
      writeStream.close();

    } catch (Exception e) {
      System.out.println("写入keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * 将android客户端RSA密钥对字符串，并导入客户端端keystore(默认密钥长度为2048)
   * @param alias 证书别名，服务端keystore中要唯一
   * @param pubKey 公钥字符串
   * @param privKey 私钥字符串
   * @param keyStorePath keystore路径
   * @param keyStorePass keystore密码
   * @param keyPass 新生成私钥保护密码
   * @param subjectDN 公钥证书主体名
   * @param issuerDN 公钥证书签发者
   * @param validity 证书有效期（天数）
   *
   * @return
   */
  public static boolean writeKeyStore(Context context, String alias, byte[] pubKey, byte[] privKey, String keyStorePath, String keyStorePass,  String keyPass, String subjectDN, String issuerDN, int validity )
  {
    try {
      PrivateKey priv = getPrivateKey( privKey);
      PublicKey pub = getPublicKey( pubKey );
      KeyPair pair = new KeyPair( pub, priv);
      KeyStore clientKs = KeyStore.getInstance("BKS");

      X509Certificate certificate = generateCertificate( pair, subjectDN, issuerDN, validity);
      Certificate[] certChain = new Certificate[1];
      certChain[0] = certificate;

      keyStorePath = context.getDir("ks", context.MODE_PRIVATE ).getAbsolutePath() + "/" + keyStorePath;
      File keyStoreFile = new File( keyStorePath );
      if(keyStoreFile.exists() ){
        clientKs.load(new FileInputStream(keyStorePath), keyStorePass.toCharArray());
      }else{
        clientKs.load(null, keyStorePass.toCharArray());
      }
      clientKs.setKeyEntry(alias, priv, keyPass.toCharArray(), certChain );
      FileOutputStream writeStream = new FileOutputStream( keyStorePath );
      clientKs.store(writeStream, keyStorePass.toCharArray());
      writeStream.close();
    } catch (Exception e) {
      System.out.println("写入keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * 将android客户端RSA密钥对字符串，并导入客户端端keystore(默认密钥长度为2048)
   * @param alias 证书别名，服务端keystore中要唯一
   * @param certStr 证书字符串
   * @param privKey 私钥字符串
   * @param keyStorePath keystore路径
   * @param keyStorePass keystore密码
   * @param keyPass 新生成私钥保护密码
   *
   * @return
   */
  public static boolean writeKeyStore(Context context, String alias, String certStr, byte[] privKey, String keyStorePath, String keyStorePass,  String keyPass )
  {
    try {
      PrivateKey priv = getPrivateKey( privKey);
      KeyStore clientKs = KeyStore.getInstance("BKS");

      X509Certificate certificate = generateCertificate( certStr );
      Certificate[] certChain = new Certificate[1];
      certChain[0] = certificate;

      keyStorePath = context.getDir("ks", context.MODE_PRIVATE ).getAbsolutePath() + "/" + keyStorePath;
      File keyStoreFile = new File( keyStorePath );
      if(keyStoreFile.exists() ){
        clientKs.load(new FileInputStream(keyStorePath), keyStorePass.toCharArray());
      }else{
        clientKs.load(null, keyStorePass.toCharArray());
      }
      clientKs.setKeyEntry(alias, priv, keyPass.toCharArray(), certChain );
      FileOutputStream writeStream = new FileOutputStream( keyStorePath );
      clientKs.store(writeStream, keyStorePass.toCharArray());
      writeStream.close();
    } catch (Exception e) {
      System.out.println("写入keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * 将android客户端公钥证书字符串，并导入客户端信任keystore
   * @param alias 证书别名，服务端keystore中要唯一
   * @param certStr 公钥证书字符串
   * @param keyStorePath keystore路径
   * @param keyStorePass keystore密码
  *
   * @return
   */
  public static boolean writeKeyStore(Context context, String alias, String certStr, String keyStorePath, String keyStorePass )
  {
    try {
      byte[] byteCert = Base64Utils.decode( certStr );
      //转换成二进制流
      ByteArrayInputStream bain = new ByteArrayInputStream(byteCert);
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate oCert = (X509Certificate)cf.generateCertificate(bain);
      String info = oCert.getSubjectDN().getName();
      System.out.println("字符串证书拥有者:"+info);
      keyStorePath = context.getDir("ks", context.MODE_PRIVATE ).getAbsolutePath() + "/" + keyStorePath;
      File keyStoreFile = new File( keyStorePath );
      KeyStore ks = KeyStore.getInstance("BKS");
      if(keyStoreFile.exists() ){
        ks.load(new FileInputStream(keyStorePath), keyStorePass.toCharArray());
        ks.setCertificateEntry(alias,  oCert);
      }else{
        ks.load(null, keyStorePass.toCharArray());
        ks.setCertificateEntry(alias,  oCert);
      }
      FileOutputStream writeStream = new FileOutputStream( keyStorePath );
      ks.store(writeStream, keyStorePass.toCharArray());
      writeStream.close();

    } catch (Exception e) {
      System.out.println("导入客户端信任keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * 将公钥证书字符串，并导入客户端信任keystore
   * @param alias 证书别名，服务端keystore中要唯一
   * @param certStr 公钥证书字符串
   * @param keyStorePath keystore路径
   * @param keyStorePass keystore密码
   *
   * @return
   */
  public static boolean writeKeyStore( String alias, String certStr, String keyStorePath, String keyStorePass )
  {
    try {
      byte[] byteCert = Base64Utils.decode( certStr );
      //转换成二进制流
      ByteArrayInputStream bain = new ByteArrayInputStream(byteCert);
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate oCert = (X509Certificate)cf.generateCertificate(bain);
      String info = oCert.getSubjectDN().getName();
      System.out.println("字符串证书拥有者:"+info);
      File keyStoreFile = new File( keyStorePath );
      KeyStore ks = KeyStore.getInstance("BKS");
      if(keyStoreFile.exists() ){
        ks.load(new FileInputStream(keyStorePath), keyStorePass.toCharArray());
        ks.setCertificateEntry(alias,  oCert);
      }else{
        ks.load(null, keyStorePass.toCharArray());
        ks.setCertificateEntry(alias,  oCert);
      }
      FileOutputStream writeStream = new FileOutputStream( keyStorePath );
      ks.store(writeStream, keyStorePass.toCharArray());
      writeStream.close();

    } catch (Exception e) {
      System.out.println("导入信任证书到keystore失败:"+e.getMessage());
      return false;
    }
    return true;
  }
  // 导出证书 base64格式文件
  public static void exportCert(KeyStore keystore, String alias, String exportFile) throws Exception {
    Certificate cert = keystore.getCertificate(alias);
    String encoded = Base64Utils.encode(cert.getEncoded());
    FileWriter fw = new FileWriter(exportFile);
    fw.write("-----BEGIN CERTIFICATE-----\r\n");    //非必须
    fw.write(encoded);
    fw.write("\r\n-----END CERTIFICATE-----");  //非必须
    fw.close();
  }

  //导出证书 base64格式字符串
  public static String exportCert(String storePath, String storePass,  String alias) {
    KeyStore keystore;
    try {
      keystore = KeyStore.getInstance("BKS");
      File storeFile = new File(storePath);
      if(storeFile.exists() ){
        keystore.load(new FileInputStream(storePath), storePass.toCharArray());
        Certificate cert = keystore.getCertificate(alias);
        return Base64Utils.encode(cert.getEncoded());
      }else {
        return null;
      }
    } catch ( Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      System.out.println("导出证书 base64格式字符串失败:"+e.getMessage());
      return null;
    }

  }
  /**
   * 列出秘钥所有条目
   * @param storePath 秘钥库路径
   * @param storePass 秘钥库密码
   * @return 条目列表
   */
  public static List listKeyStore( String storePath, String storePass ){

    List itemList = new ArrayList();
    Map map = new HashMap();
    try {
      FileInputStream in=new FileInputStream( storePath );
      KeyStore ks=KeyStore.getInstance("BKS");
      ks.load(in,storePass.toCharArray());
      Enumeration e=ks.aliases();
      while(e.hasMoreElements()) {
        String aliase = (String)e.nextElement();
        java.security.cert.Certificate c = ks.getCertificate( aliase );
        Map map0 = new HashMap();
        map0.put("aliase", aliase );
        String pub =Base64Utils.encode(c.getPublicKey().getEncoded());
        map0.put("publickey", pub );
        if(c instanceof X509Certificate) {
          X509Certificate x509 = (X509Certificate) c;
          map0.put("Version", x509.getVersion());
          map0.put("SerialNumber", x509.getSerialNumber());
          map0.put("SubjectDN", x509.getSubjectDN());
          map0.put("IssuerDN", x509.getIssuerDN());
          map0.put("NotAfter", x509.getNotAfter());
          map0.put("NotBefore", x509.getNotBefore());
          map0.put("SigAlgName", x509.getSigAlgName());
        }
        itemList.add(map0);

      }
      in.close();
    }catch (Exception e){
      map.put("status", FAILED);
      map.put("msg", "秘钥库文件读取失败");
      itemList.add(map);
    }

    System.out.println( itemList.toString() );

    return itemList;
  }

  /**
   * 移除keystore条目
   * @param storePath 秘钥库路径
   * @param storePass 秘钥库密码
   * @param alias 秘钥别名
   * @return true 成功，false 失败
   */
  public static boolean removeFromKeyStore( String storePath, String storePass, String alias ){
    try {
      FileInputStream in = new FileInputStream( storePath );
      KeyStore ks=KeyStore.getInstance("BKS");
      ks.load(in,storePass.toCharArray());
      ks.deleteEntry( alias );
      FileOutputStream output = new  FileOutputStream( storePath );
      ks.store(output,storePass.toCharArray());//将keystore对象内容写入文件,条目删除成功
      in.close();
      output.close();
    }catch (Exception e){
      System.out.println( "从秘钥库移除别名" + alias + "的秘钥失败：" + e.getMessage() );
      return false;
    }
    System.out.println( "从秘钥库移除别名" + alias + "的秘钥成功" );
    return true;
  }

  /**
   * 从另一keystore添加keystore条目
   * @param serverStorePath 服务端秘钥库路径
   * @param serverStorePass 服务端秘钥库密码
   * @param clientStorePath 客户端秘钥库路径
   * @param clientStorePass 客户端秘钥库密码
   * @param alias 秘钥别名
   * @param keyPass 秘钥密码
   * @return true 成功，false 失败
   */
  public static boolean addFromKeyStore( String serverStorePath, String serverStorePass, String clientStorePath, String clientStorePass, String alias, String keyPass ){
    try {
      FileInputStream serverIn = new FileInputStream( serverStorePath );
      FileInputStream clientIn = new FileInputStream( clientStorePath );
      KeyStore serverKs=KeyStore.getInstance("BKS");
      KeyStore clientKs=KeyStore.getInstance("BKS");

      serverKs.load(serverIn,serverStorePass.toCharArray());
      clientKs.load(clientIn,clientStorePass.toCharArray());

      PrivateKey priv = (PrivateKey)clientKs.getKey( alias, keyPass.toCharArray());
      serverKs.setKeyEntry( alias, priv, serverStorePass.toCharArray(), clientKs.getCertificateChain( alias ));
      FileOutputStream writeStream = new FileOutputStream( serverStorePath );
      serverKs.store(writeStream, serverStorePass.toCharArray());
      serverIn.close();
      clientIn.close();
      writeStream.close();

    }catch (Exception e){
      System.out.println( "从秘钥库添加别名" + alias + "的秘钥失败：" + e.getMessage() );
      return false;
    }
    System.out.println( "从秘钥库添加别名" + alias + "的秘钥成功" );
    return true;
  }

  /**
   * 修改keystore口令
   * @param storePath 秘钥库路径
   * @param oldPass 秘钥库旧密码
   * @param newPass 秘钥库新密码
   * @return true 成功，false 失败
   */
  public static boolean changeStorePass( String storePath, String oldPass, String newPass ){
    try {
      FileInputStream in = new  FileInputStream(storePath );
      KeyStore ks = KeyStore.getInstance( "BKS" );
      ks.load(in,oldPass.toCharArray());
      in.close();
      FileOutputStream output = new  FileOutputStream( storePath );
      ks.store(output,newPass.toCharArray());
      output.close();
    }catch (Exception e){
      System.out.println( "修改秘钥库" + storePath + "的密码失败：" + e.getMessage() );
      return false;
    }
    System.out.println( "修改秘钥库" + storePath + "的密码成功");
    return true;
  }

  /**
   * 修改keystore的条目口令
   * @param storePath 秘钥库路径
   * @param storePass 秘钥库密码
   * @param alias 条目别名
   * @param oldPass 条目旧密码
   * @param newPass 条目新密码
   * @return true 成功，false 失败
   */
  public static boolean changeKeyPass( String storePath, String storePass, String alias, String oldPass, String newPass ){
    try {
      FileInputStream in = new  FileInputStream( storePath );
      KeyStore ks = KeyStore.getInstance( "BKS" );
      ks.load(in,storePass.toCharArray());
      Certificate[] cchain = ks.getCertificateChain(alias);//获取别名对应条目的证书链
      PrivateKey pk = (PrivateKey)ks.getKey(alias, oldPass.toCharArray());//获取别名对应条目的私钥
      ks.setKeyEntry(alias,pk, newPass.toCharArray(),cchain);//向密钥库中添加条目
      in.close();
      FileOutputStream output = new  FileOutputStream( storePath );
      ks.store(output,storePass.toCharArray());
      output.close();
    }catch (Exception e){
      System.out.println( "修改秘钥库" + storePath + "别名：" + alias + "的密码失败：" + e.getMessage() );
      return false;
    }
    System.out.println( "修改秘钥库" + storePath +  "别名：" + alias + "的密码成功");
    return true;
  }

  /**
   * 生成公钥证书
   * @param keyPair 密钥对
   * @param subjectDN 主体名
   * @param issuerDN 签发者
   * @param validity 有效期（天数）
   * @return 证书
   */
  public static X509Certificate generateCertificate( KeyPair keyPair, String subjectDN, String issuerDN, int validity) throws Exception {
    Date startDate=new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(startDate);
    cal.add(Calendar.DATE, validity);
    Date expiryDate = cal.getTime();
    BigInteger  serialNumber= BigInteger.valueOf(startDate.getTime()/1000); //以当前时间为序列号
    X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder( new X500Name( issuerDN), serialNumber, startDate, expiryDate, new X500Name(subjectDN), SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
    JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256withRSA");
    ContentSigner signer = builder.build(keyPair.getPrivate());

    byte[] certBytes = certBuilder.build(signer).getEncoded();
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));

    return certificate;
  }
  /**
   * 由证书字符串生成公钥证书
   * @param certStr 密钥对
   * @return 证书
   */
  public static X509Certificate generateCertificate( String certStr ) throws Exception {
    byte[] byteCert = Base64Utils.decode( certStr );
    //转换成二进制流
    ByteArrayInputStream bain = new ByteArrayInputStream(byteCert);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate oCert = (X509Certificate)cf.generateCertificate(bain);
    String info = oCert.getSubjectDN().getName();
    System.out.println("字符串证书拥有者:"+info);
    return oCert;
  }
  /**
   * 随机生成RSA密钥对(默认密钥长度为1024)
   *
   * @return
   */
  public static KeyPair generateRSAKeyPair()
  {
    return generateRSAKeyPair(2048);
  }

  /**
   * 随机生成RSA密钥对
   *
   * @param keyLength
   *            密钥长度，范围：512～2048<br>
   *            一般1024
   * @return
   */
  public static KeyPair generateRSAKeyPair(int keyLength)
  {
    try
    {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
      kpg.initialize(keyLength);
      return kpg.genKeyPair();
    } catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 从keystore提取密钥对
   * @param keystore 证书库
   * @param alias 证书别名
   * @password 私钥密码
   * @return 密钥对
   */
  public static KeyPair getKeyPair(KeyStore keystore, String alias, char[] password) {
    try {
      Key key=keystore.getKey(alias,password);
      if(key instanceof PrivateKey) {
        Certificate cert=keystore.getCertificate(alias);
        PublicKey publicKey=cert.getPublicKey();
        return new KeyPair(publicKey,(PrivateKey)key);
      }
    } catch (UnrecoverableKeyException e) {
    } catch (NoSuchAlgorithmException e) {
    } catch (KeyStoreException e) {
    }
    return null;
  }
  /**
   * 从keystore文件提取密钥对
   * @param storePath 证书库路径
   * @param storePass 证书库密码
   * @param alias 证书别名
   * @param keypass 私钥密码
   * @password 私钥密码
   * @return 密钥对
   */
  public static KeyPair getKeyPair(String storePath, String storePass, String alias, String keypass ) {
    try {
      File storeFile = new File(storePath);
      KeyStore keystore = KeyStore.getInstance("BKS");
      if(storeFile.exists() ){
        try {
          keystore.load(new FileInputStream(storePath), storePass.toCharArray());
        }catch (Exception e){
          return null;
        }
      }else {
        return null;
      }
      Key key=keystore.getKey(alias, keypass.toCharArray());
      if(key instanceof PrivateKey) {
        Certificate cert=keystore.getCertificate(alias);
        PublicKey publicKey=cert.getPublicKey();
        return new KeyPair(publicKey,(PrivateKey)key);
      }
    } catch (UnrecoverableKeyException e) {
    } catch (NoSuchAlgorithmException e) {
    } catch (KeyStoreException e) {
    }
    return null;
  }

  public void export( String alias, char[] storePass, char[] keyPass, String keyStoreType, String keystoreFile , String exportedFile ) throws Exception{
    KeyStore keystore=KeyStore.getInstance(keyStoreType);
    keystore.load(new FileInputStream(keystoreFile),storePass);
    KeyPair keyPair=getKeyPair(keystore,alias,keyPass);
    PrivateKey privateKey=keyPair.getPrivate();
    String encoded=Base64Utils.encode(privateKey.getEncoded());
    FileWriter fw=new FileWriter(exportedFile);
    fw.write("--BEGIN PRIVATE KEY--/n");
    fw.write(encoded);
    fw.write("/n");
    fw.write("--END PRIVATE KEY--");
    fw.close();

  }

  /**
   * 用公钥加密 <br>
   * 每次加密的字节数，不能超过密钥的长度值减去11
   *
   * @param data 需加密数据的byte数据
   * @param publicKey 公钥
   * @return 加密后的byte型数据
   */
  public static byte[] encryptData(byte[] data, PublicKey publicKey)
  {
    try
    {
      Cipher cipher = Cipher.getInstance(RSA);
      // 编码前设定编码方式及密钥
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      // 传入编码数据并返回编码结果
      return cipher.doFinal(data);
    } catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 用私钥解密
   *
   * @param encryptedData
   *            经过encryptedData()加密返回的byte数据
   * @param privateKey
   *            私钥
   * @return
   */
  public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey)
  {
    try
    {
      Cipher cipher = Cipher.getInstance(RSA);
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      return cipher.doFinal(encryptedData);
    } catch (Exception e)
    {
      return null;
    }
  }
  /**
   * 从keystore用公钥加密文件 <br>
   */
  public boolean encryptFile(String alias, String keypass, String storepass, String keyStoreType, String keystoreFile , String orgFile, String targetFile, int segmentSize )
  {
    try
    {
      //提取公钥
      KeyStore keystore=KeyStore.getInstance(keyStoreType);
      keystore.load(new FileInputStream(keystoreFile),storepass.toCharArray());
      KeyPair keyPair=getKeyPair(keystore,alias,keypass.toCharArray());
      //PrivateKey privateKey=keyPair.getPrivate();
      PublicKey publicKey = keyPair.getPublic();
      //byte[] publicBT = publicKey.getEncoded();
      //读取待加密文件明文
      String orgStr= readFile( orgFile );
      //orgStr = orgStr.replaceAll("\r", "").replaceAll("\n","").replaceAll("\t","").replaceAll("\\s","").replaceAll("　","").replaceAll(" ","");
      //分段RSA公钥加密
      //String targetStr = encipher( orgStr, Base64Utils.encode( publicBT ), segmentSize);
      String targetStr = encipher( orgStr,publicKey, segmentSize);

      return writeFile( targetStr, targetFile );
    } catch (Exception e){
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 从keystore用私钥解密文件存本地
   *
   */
  public boolean decryptFile(String alias, String keypass, String storepass, String keyStoreType, String keystoreFile , String orgFile, String targetFile, int segmentSize )
  {
    try
    {
      //提取私钥
      KeyStore keystore=KeyStore.getInstance(keyStoreType);
      keystore.load(new FileInputStream(keystoreFile),storepass.toCharArray());
      KeyPair keyPair=getKeyPair(keystore,alias,keypass.toCharArray());
      PrivateKey privateKey=keyPair.getPrivate();
      byte[] privateBT = privateKey.getEncoded();
      //读取待解密文件密文
      String orgStr= readFile( orgFile );
      //分段RSA私钥解密
      String targetStr = decipher( orgStr, Base64Utils.encode( privateBT ), segmentSize);
      //String targetStr = decipher( orgStr, privateKey, segmentSize);

      return writeFile( targetStr, targetFile );
    } catch (Exception e){
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 从keystore用私钥解密文件，返回明文
   *
   */
  public String decryptFile(String alias, String keypass, String storepass, String keyStoreType, String keystoreFile , String orgFileUrl, int segmentSize )
  {
    try
    {
      //提取私钥
      KeyStore keystore=KeyStore.getInstance(keyStoreType);
      keystore.load(new FileInputStream(keystoreFile),storepass.toCharArray());
      KeyPair keyPair=getKeyPair(keystore,alias,keypass.toCharArray());
      PrivateKey privateKey=keyPair.getPrivate();
      byte[] privateBT = privateKey.getEncoded();
      //读取待解密文件密文
      String orgStr= this.readUrl( orgFileUrl );
      if( null == orgStr ) return null;
      //分段RSA私钥解密
      String targetStr = decipher( orgStr, Base64Utils.encode( privateBT ), segmentSize);
      //String targetStr = decipher( orgStr, privateKey, segmentSize);
      return targetStr;
    } catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }
  /**
   * 从keystore用私钥解密文件，返回明文
   *
   */
  public String decryptFileAndroid(Context context, String alias, String keypass, String storepass, String keyStoreType, String keystoreFile, String orgFileUrl, int segmentSize )
  {
    try
    {
      //先从数据文件夹下找keystore
      File ks = context.getDir( "ks", context.MODE_PRIVATE );
      File ksFile = new File( ks.getAbsolutePath()+ "/" + keystoreFile );
      InputStream is = null;
      if( ksFile.exists() ){
        is = new FileInputStream( ksFile.getAbsolutePath());
      }else{
        //从初始assets下提取私钥
        AssetManager am = context.getResources().getAssets();
        is = am.open(keystoreFile);
      }

      KeyStore keystore=KeyStore.getInstance(keyStoreType);
      keystore.load( is,storepass.toCharArray());
      KeyPair keyPair=getKeyPair(keystore,alias,keypass.toCharArray());
      PrivateKey privateKey=keyPair.getPrivate();
      byte[] privateBT = privateKey.getEncoded();
      //读取待解密文件密文
      String orgStr= this.readUrl( orgFileUrl );
      if( null == orgStr ) return null;
      //分段RSA私钥解密
      String targetStr = decipherAndroid( orgStr, Base64Utils.encode( privateBT ), segmentSize);
      //String targetStr = decipher( orgStr, privateKey, segmentSize);
      return targetStr;
    } catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 从keystore用私钥解密字符串，返回明文
   *
   */
  public String decryptStr(Context context, String alias, String keypass, String storepass, String keyStoreType, String keystoreFile, String ciphertext, int segmentSize )
  {
    try
    {
      //提取私钥
      AssetManager am = context.getResources().getAssets();
      InputStream is = am.open(keystoreFile);

      KeyStore keystore=KeyStore.getInstance(keyStoreType);
      keystore.load( is,storepass.toCharArray());
      KeyPair keyPair=getKeyPair(keystore,alias,keypass.toCharArray());
      PrivateKey privateKey=keyPair.getPrivate();
      byte[] privateBT = privateKey.getEncoded();
      //读取待解密文件密文
      if( null == ciphertext ) return null;
      //分段RSA私钥解密
      String cleartext = decipherAndroid( ciphertext, Base64Utils.encode( privateBT ), segmentSize);
      //String targetStr = decipher( orgStr, privateKey, segmentSize);
      return cleartext;
    } catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }
  /**
   * 通过公钥byte[](publicKey.getEncoded())将公钥还原，适用于RSA算法
   *
   * @param keyBytes
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public static PublicKey getPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException,
    InvalidKeySpecException
  {
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);
    return publicKey;
  }

  /**
   * 通过私钥byte[]将公钥还原，适用于RSA算法
   *
   * @param keyBytes
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public static PrivateKey getPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException,
    InvalidKeySpecException
  {
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
    return privateKey;
  }

  /**
   * 使用N、e值还原公钥
   *
   * @param modulus
   * @param publicExponent
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public static PublicKey getPublicKey(String modulus, String publicExponent)
    throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    BigInteger bigIntModulus = new BigInteger(modulus);
    BigInteger bigIntPrivateExponent = new BigInteger(publicExponent);
    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);
    return publicKey;
  }

  /**
   * 使用N、d值还原私钥
   *
   * @param modulus
   * @param privateExponent
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public static PrivateKey getPrivateKey(String modulus, String privateExponent)
    throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    BigInteger bigIntModulus = new BigInteger(modulus);
    BigInteger bigIntPrivateExponent = new BigInteger(privateExponent);
    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
    return privateKey;
  }

  /**
   * 从字符串中加载公钥
   *
   * @param publicKeyStr
   *            公钥数据字符串
   * @throws Exception
   *             加载公钥时产生的异常
   */
  public static PublicKey loadPublicKey(String publicKeyStr) throws Exception
  {
    try
    {
      byte[] buffer = Base64Utils.decode(publicKeyStr);
      KeyFactory keyFactory = KeyFactory.getInstance(RSA);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
      return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException e)
    {
      throw new Exception("无此算法");
    } catch (InvalidKeySpecException e)
    {
      throw new Exception("公钥非法");
    } catch (NullPointerException e)
    {
      throw new Exception("公钥数据为空");
    }
  }

  /**
   * 从字符串中加载私钥<br>
   * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）。
   *
   * @param privateKeyStr
   * @return
   * @throws Exception
   */
  public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception
  {
    try
    {
      byte[] buffer = Base64Utils.decode(privateKeyStr);
      // X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
      KeyFactory keyFactory = KeyFactory.getInstance(RSA);
      return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException e)
    {
      throw new Exception("无此算法");
    } catch (InvalidKeySpecException e)
    {
      throw new Exception("私钥非法");
    } catch (NullPointerException e)
    {
      throw new Exception("私钥数据为空");
    }
  }

  /**
   * 从文件中输入流中加载公钥
   *
   * @param in
   *            公钥输入流
   * @throws Exception
   *             加载公钥时产生的异常
   */
  public static PublicKey loadPublicKey(InputStream in) throws Exception
  {
    try
    {
      return loadPublicKey(readKey(in));
    } catch (IOException e)
    {
      throw new Exception("公钥数据流读取错误");
    } catch (NullPointerException e)
    {
      throw new Exception("公钥输入流为空");
    }
  }

  /**
   * 从文件中加载私钥
   *
   * @param in 私钥文件流
   * @return 是否成功
   * @throws Exception
   */
  public static PrivateKey loadPrivateKey(InputStream in) throws Exception
  {
    try
    {
      return loadPrivateKey(readKey(in));
    } catch (IOException e)
    {
      throw new Exception("私钥数据读取错误");
    } catch (NullPointerException e)
    {
      throw new Exception("私钥输入流为空");
    }
  }

  /**
   * 读取密钥信息
   *
   * @param in
   * @return
   * @throws IOException
   */
  private static String readKey(InputStream in) throws IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String readLine = null;
    StringBuilder sb = new StringBuilder();
    while ((readLine = br.readLine()) != null)
    {
      if (readLine.charAt(0) == '-')
      {
        continue;
      } else
      {
        sb.append(readLine);
        sb.append('\r');
      }
    }

    return sb.toString();
  }

  /**
   * 打印公钥信息
   *
   * @param publicKey
   */
  public static void printPublicKeyInfo(PublicKey publicKey)
  {
    RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
    System.out.println("----------RSAPublicKey----------");
    System.out.println("Modulus.length=" + rsaPublicKey.getModulus().bitLength());
    System.out.println("Modulus=" + rsaPublicKey.getModulus().toString());
    System.out.println("PublicExponent.length=" + rsaPublicKey.getPublicExponent().bitLength());
    System.out.println("PublicExponent=" + rsaPublicKey.getPublicExponent().toString());
  }

  public static void printPrivateKeyInfo(PrivateKey privateKey)
  {
    RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
    System.out.println("----------RSAPrivateKey ----------");
    System.out.println("Modulus.length=" + rsaPrivateKey.getModulus().bitLength());
    System.out.println("Modulus=" + rsaPrivateKey.getModulus().toString());
    System.out.println("PrivateExponent.length=" + rsaPrivateKey.getPrivateExponent().bitLength());
    System.out.println("PrivatecExponent=" + rsaPrivateKey.getPrivateExponent().toString());

  }
  /**
   * 使用公钥加密
   * @param content 待加密内容
   * @param publicKeyBase64  公钥 base64 编码
   * @return 经过 base64 编码后的字符串
   */
  public String encipher(String content,String publicKeyBase64){
    return encipher(content,publicKeyBase64,-1);
  }
  /**
   * 使用公钥加密（分段加密）
   * @param content 待加密内容
   * @param publicKeyBase64  公钥 base64 编码
   * @param segmentSize 分段大小,一般小于 keySize/8（段小于等于0时，将不使用分段加密）
   * @return 经过 base64 编码后的字符串
   */
  public String encipher(String content,String publicKeyBase64,int segmentSize){
    try {
      PublicKey publicKey = getPublicKey(publicKeyBase64);
      return encipher(content,publicKey,segmentSize);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  /**
   * 分段加密
   * @param ciphertext 密文
   * @param key 加密秘钥
   * @param segmentSize 分段大小，<=0 不分段
   * @return
   */
  public String encipher(String ciphertext,java.security.Key key,int segmentSize){
    try {
      // 用公钥加密
      byte[] srcBytes = ciphertext.getBytes();

      // Cipher负责完成加密或解密工作，基于RSA
      Cipher cipher = Cipher.getInstance("RSA");
      // 根据公钥，对Cipher对象进行初始化
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] resultBytes = null;

      if(segmentSize>0)
        resultBytes = cipherDoFinal(cipher,srcBytes,segmentSize); //分段加密
      else
        resultBytes = cipher.doFinal(srcBytes);

      String base64Str =  Base64Utils.encode(resultBytes);
      return base64Str;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  /**
   * 分段大小
   * @param cipher
   * @param srcBytes
   * @param segmentSize
   * @return
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   * @throws IOException
   */
  private byte[] cipherDoFinal(Cipher cipher,byte[] srcBytes,int segmentSize)
    throws IllegalBlockSizeException, BadPaddingException, IOException{
    if(segmentSize<=0)
      throw new RuntimeException("分段大小必须大于0");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int inputLen = srcBytes.length;
    int offSet = 0;
    byte[] cache;
    int i = 0;
    // 对数据分段解密
    while (inputLen - offSet > 0) {
      if (inputLen - offSet > segmentSize) {
        cache = cipher.doFinal(srcBytes, offSet, segmentSize);
      } else {
        cache = cipher.doFinal(srcBytes, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * segmentSize;
    }
    byte[] data = out.toByteArray();
    out.close();
    return data;
  }
  /**
   * 使用私钥解密
   * @param contentBase64 待加密内容,base64 编码
   * @param privateKeyBase64  私钥 base64 编码
   * @segmentSize 分段大小
   * @return
   */
  public String decipher(String contentBase64,String privateKeyBase64){
    return decipher(contentBase64, privateKeyBase64,-1);
  }
  public String decipherAndroid(String contentBase64,String privateKeyBase64){
    return decipherAndroid(contentBase64, privateKeyBase64,-1);
  }
  /**
   * 使用私钥解密（分段解密）
   * @param contentBase64 待加密内容,base64 编码
   * @param privateKeyBase64  私钥 base64 编码
   * @segmentSize 分段大小
   * @return
   */
  public String decipher(String contentBase64,String privateKeyBase64,int segmentSize){
    try {
      PrivateKey privateKey = getPrivateKey(privateKeyBase64);
      return decipher(contentBase64, privateKey,segmentSize);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  public String decipherAndroid(String contentBase64,String privateKeyBase64,int segmentSize){
    try {
      PrivateKey privateKey = getPrivateKey(privateKeyBase64);
      return decipherAndroid(contentBase64, privateKey,segmentSize);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  /**
   * 分段解密
   * @param contentBase64 密文
   * @param key 解密秘钥
   * @param segmentSize 分段大小（小于等于0不分段）
   * @return
   */
  public String decipher(String contentBase64,java.security.Key key,int segmentSize){
    try {
      // 用私钥解密
      byte[] srcBytes = Base64Utils.decode(contentBase64);
      // Cipher负责完成加密或解密工作，基于RSA
      Cipher deCipher = Cipher.getInstance("RSA");
      // 根据公钥，对Cipher对象进行初始化
      deCipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decBytes = null;//deCipher.doFinal(srcBytes);
      if(segmentSize>0)
        decBytes = cipherDoFinal(deCipher,srcBytes,segmentSize); //分段解密
      else
        decBytes = deCipher.doFinal(srcBytes);

      String decrytStr=new String(decBytes);
      return decrytStr;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  /**
   * android端分段解密
   * @param contentBase64 密文
   * @param key 解密秘钥
   * @param segmentSize 分段大小（小于等于0不分段）
   * @return
   */
  public String decipherAndroid(String contentBase64,java.security.Key key,int segmentSize){
    try {
      // 用私钥解密
      byte[] srcBytes = Base64Utils.decode(contentBase64);
      // Cipher负责完成加密或解密工作，基于RSA
      Cipher deCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      // 根据公钥，对Cipher对象进行初始化
      deCipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decBytes = null;//deCipher.doFinal(srcBytes);
      if(segmentSize>0)
        decBytes = cipherDoFinal(deCipher,srcBytes,segmentSize); //分段解密
      else
        decBytes = deCipher.doFinal(srcBytes);

      String decrytStr=new String(decBytes);
      return decrytStr;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 获取公钥对象
   * @param publicKeyBase64
   * @return
   * @throws InvalidKeySpecException
   * @throws NoSuchAlgorithmException
   */
  public PublicKey getPublicKey(String publicKeyBase64)
    throws InvalidKeySpecException,NoSuchAlgorithmException{

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec publicpkcs8KeySpec =
      new X509EncodedKeySpec(Base64Utils.decode(publicKeyBase64));
    PublicKey publicKey = keyFactory.generatePublic(publicpkcs8KeySpec);
    return publicKey;
  }
  /**
   * 获取私钥对象
   * @param privateKeyBase64
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public PrivateKey getPrivateKey(String privateKeyBase64)
    throws NoSuchAlgorithmException, InvalidKeySpecException{
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec privatekcs8KeySpec =
      new PKCS8EncodedKeySpec(Base64Utils.decode(privateKeyBase64));
    PrivateKey privateKey = keyFactory.generatePrivate(privatekcs8KeySpec);
    return privateKey;
  }

  public String readUrl(String fileUrl) throws IOException {
    String read;
    String readStr ="";
    try{
      URL url =new URL(fileUrl);
      HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
      urlCon.setConnectTimeout(10000);
      urlCon.setReadTimeout(10000);
      BufferedReader br =new BufferedReader(new InputStreamReader( urlCon.getInputStream()));
      while ((read = br.readLine()) !=null) {
        readStr = readStr + read;
      }
      br.close();
    }catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    return readStr;
  }

  public static String readFile( String filePath){
    String returnStr="";
    //为了确保文件一定在之前是存在的，将字符串路径封装成File对象
    File file = new File( filePath );
    if(!file.exists()){
      throw new RuntimeException("要读取的文件不存在");
    }
    //创建文件字节读取流对象时，必须明确与之关联的数据源。
    try {
      FileInputStream fis = new FileInputStream(file);
      //创建一个字节数组，定义len记录长度
      int len = 0;
      byte[] buf = new byte[4096];
      while((len=fis.read(buf))!=-1){
        String tmp = new String(buf,0,len);
        System.out.println( tmp );
        returnStr += tmp;
      }
      //关资源
      fis.close();

    }catch ( Exception e){
      e.printStackTrace();
      System.out.println("读取文件错误" + e.getMessage());
      return null;
    }

    return returnStr;
  }

  public static boolean writeFile( String content, String fileUrl) {
    try {
      FileWriter fw = new FileWriter(fileUrl);
      fw.write(content);
      fw.close();
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("写文件错误" + e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * 秘钥对
   *
   */
  public class KeyPairInfo{
    public KeyPairInfo(int keySize){
      setKeySize(keySize);
    }
    public KeyPairInfo(String publicKey,String privateKey){
      setPrivateKey(privateKey);
      setPublicKey(publicKey);
    }
    String privateKey;
    String publicKey;
    int keySize=0;
    public String getPrivateKey() {
      return privateKey;
    }
    public void setPrivateKey(String privateKey) {
      this.privateKey = privateKey;
    }
    public String getPublicKey() {
      return publicKey;
    }
    public void setPublicKey(String publicKey) {
      this.publicKey = publicKey;
    }
    public int getKeySize() {
      return keySize;
    }
    public void setKeySize(int keySize) {
      this.keySize = keySize;
    }
  }
}
