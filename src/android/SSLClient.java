/**
 * lyzcw
*	2017年11月15日 上午9:35:48
 */
package com.openunion.cordova.plugins.nlpos;

/**
 * @author lyzcw
 *2017年11月15日 上午9:35:48
 */
import android.content.Context;

import java.net.*;
import java.security.KeyStore;

import javax.net.ssl.*;

import javax.net.*;
import java.io.*;

public class SSLClient
{
	static int DEFAULT_PORT=15820;  //系统将要监听的端口号15820
	static String CLIENT_STORE_PASSWORD = Constant.storepass;
	static String CLIENT_KEY_PASSWORD = Constant.keypass;
	static String CLIENT_TRUST_STORE_PASSWORD = Constant.storepass;
	static String DEFAULT_HOST= Constant.socketip;
	/*
	*构造函数
	*/

	public SSLClient()
	{

	}

	public static void main(String args[])
	{
    SSLSocket s = getSocket( DEFAULT_PORT );
		PrintWriter out;
		BufferedReader in;
		try {
			out = new PrintWriter(s.getOutputStream(),true);
			String req = "{" +
					"	\"deviceid\":\"N7NL00147829\"," +
					"	\"appid\":\"\"	," +
					"	\"userid \":\"\"," +
					"	\"token\":\"\"," +
					"	\"data\":{ " +
					"	 }" +
					"}";
			out.println(req);
		    in=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
		    String msg=in.readLine();
		    System.out.println("socket客户端收到的响应报文："+msg);
		    s.close();
		    out.close();
		    in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String signDevice(Context context, String deviceid )
	{
		String rtnStr = "";
		SSLSocket s = getSocket(context, DEFAULT_PORT );
		PrintWriter out;
		BufferedReader in;
		try {
			out = new PrintWriter(s.getOutputStream(),true);
			String req = "{" +
					"	\"deviceid\":\"" + deviceid + "\"," +
					"	\"appid\":\"\"	," +
					"	\"userid \":\"\"," +
					"	\"token\":\"\"," +
					"	\"data\":{ " +
					"	 }" +
					"}";
			out.println(req);
      in=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
      String msg=in.readLine();
			System.out.println("socket客户端收到的响应报文："+msg);
			rtnStr = msg;
      s.close();
      out.close();
      in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return rtnStr;

	}

	/*
	*@param port 监听的端口号
	*@return 返回一个SSLServerSocket对象
	*/

	private static SSLSocket getSocket( Context context, int thePort)
	{
		SSLSocket s=null;
		try
		{
			SSLContext ctx = SSLContext.getInstance("SSL");

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");

			KeyStore ks = KeyStore.getInstance("BKS");
			KeyStore tks = KeyStore.getInstance("BKS");
      String keyStorePath = context.getDir("ks", context.MODE_PRIVATE ).getAbsolutePath() + "/" + Constant.storepath;
      String trustStorePath = context.getDir("ks", context.MODE_PRIVATE ).getAbsolutePath() + "/" + Constant.serverstorepath;
			ks.load(new FileInputStream(keyStorePath), CLIENT_STORE_PASSWORD.toCharArray());
			tks.load(new FileInputStream( trustStorePath ), CLIENT_TRUST_STORE_PASSWORD.toCharArray());

			kmf.init(ks, CLIENT_KEY_PASSWORD.toCharArray());
			tmf.init(tks);

			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			s =  (SSLSocket) ctx.getSocketFactory().createSocket(DEFAULT_HOST, DEFAULT_PORT);

		}catch(Exception e)
		{
			System.out.println(e);
		}
		return s;
	}
	/*
	*@param port 监听的端口号
	*@return 返回一个SSLServerSocket对象
	*/

  private static SSLSocket getSocket(int thePort)
  {
    SSLSocket s=null;
    try
    {
      SSLContext ctx = SSLContext.getInstance("SSL");

      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

      KeyStore ks = KeyStore.getInstance("BKS");
      KeyStore tks = KeyStore.getInstance("BKS");

      ks.load(new FileInputStream("d:\\pos\\security\\posks.bks"), CLIENT_STORE_PASSWORD.toCharArray());
      tks.load(new FileInputStream("d:\\pos\\security\\postrustks.bks"), CLIENT_TRUST_STORE_PASSWORD.toCharArray());

      kmf.init(ks, CLIENT_KEY_PASSWORD.toCharArray());
      tmf.init(tks);

      ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

      s =  (SSLSocket) ctx.getSocketFactory().createSocket(DEFAULT_HOST, DEFAULT_PORT);

    }catch(Exception e)
    {
      System.out.println(e);
      return null;
    }
    return s;
  }


}
