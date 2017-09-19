package com.openunion.cordova.plugins.nlpos;

import android.util.Base64DataException;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterResult;
import com.newland.mtype.module.common.printer.PrinterStatus;
import com.newland.mtype.module.common.printer.WordStockType;
import com.newland.mtype.module.common.printer.ThrowType;

import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyzcw on 2017/9/7.
 */

public class Print {
  private WaitThreat waitThreat;
  private LiteralType literalType;
  private FontSettingScope fontSettingScope;
  private FontType fontType;
  public N900Device n900Device;
  private Printer printer;
  private PrinterManager printManager;
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  private String showMsg = "";
  private Map map = new HashMap();

  public Map print( String bill ){
    waitThreat = new WaitThreat();
    printManager=PrinterManager.getInstance();
    if(n900Device.isDeviceAlive()){
      printer = n900Device.getPrinter();
      // 缺纸
      if (printer.getStatus() == PrinterStatus.OUTOF_PAPER) {
        showMsg = "打印失败！打印机缺纸";
        map.put("status", FAILED);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      }
      if (printer.getStatus() != PrinterStatus.NORMAL) {
        showMsg = "打印失败！打印机状态不正常";
        map.put("status", FAILED);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      }

      printer.init();
      try {
        JSONArray billArray = new JSONArray(bill);
        for( int i=0; i<billArray.length(); i++){
          JSONObject billObject = billArray.getJSONObject( i );
          if( billObject.getString("type").equals("title")){
            printer.setDensity(10);
            printer.setLineSpace(Integer.parseInt("3"));
            printer.setWordStock(WordStockType.PIX_16);// 字库
            printer.setFontType(LiteralType.WESTERN, FontSettingScope.HEIGHT, FontType.DOUBLE);
            printer.setFontType(LiteralType.WESTERN, FontSettingScope.WIDTH, FontType.DOUBLE);
            PrinterResult printerResult0 = printer.print( billObject.getString("data"), 30, TimeUnit.SECONDS);
            showMsg += "标题打印结果：" + printerResult0.toString();
            LOG.d(LOG_TAG, showMsg);
          }else if( billObject.getString("type").equals("text")){
            printer.setDensity(10);
            printer.setLineSpace(Integer.parseInt("3"));
            printer.setWordStock(WordStockType.PIX_24);// 字库
            printer.setFontType(LiteralType.WESTERN, FontSettingScope.HEIGHT, FontType.NORMAL);
            printer.setFontType(LiteralType.WESTERN, FontSettingScope.WIDTH, FontType.NORMAL);
            PrinterResult printerResult0 = printer.print( billObject.getString("data"), 30, TimeUnit.SECONDS);
            showMsg += "内容打印结果：" + printerResult0.toString();
            LOG.d(LOG_TAG, showMsg);
          }if( billObject.getString("type").equals("image")){
            printer.setDensity(10);
            printer.setLineSpace(Integer.parseInt("0"));
            printer.setWordStock(WordStockType.PIX_16);// 字库
            printer.setFontType(LiteralType.WESTERN, FontSettingScope.HEIGHT, FontType.NORMAL);
            printer.setFontType(LiteralType.WESTERN, FontSettingScope.WIDTH, FontType.NORMAL);
            String imgStr = billObject.getString("data");
            int offset = imgStr.indexOf(";base64,") + 8;
            imgStr=imgStr.substring( offset );
            byte[] imgByte =Base64.decode(imgStr, Base64.NO_WRAP);//.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray( imgByte, 0, imgByte.length );
            //Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream(Constant.icon_path));
            PrinterResult printerResult0 = printer.print(0, bitmap, 30, TimeUnit.SECONDS);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.big_picture);
            // Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream(Constant.icon_path));
            showMsg += "图片打印结果：" + printerResult0.toString();
            LOG.d(LOG_TAG, showMsg);
          }
        }
        printer.paperThrow(ThrowType.BY_LINE,2);// 走紙

        map.put("status", SUCCESS);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      }catch (JSONException e ) {
        showMsg += "打印JSON参数传递错误" + e.getMessage();
        map.put("status", FAILED);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      }
    }
    return map;

  }

  private Map initPrinter(){
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          printer.init();
          showMsg = "打印机初始化成功" ;
          map.put("status", SUCCESS);
          map.put("msg", showMsg);
          LOG.d(LOG_TAG, showMsg);
        } catch (Exception e) {
          e.printStackTrace();
          showMsg = "打印机初始化异常：";
          map.put("status", FAILED);
          map.put("msg", showMsg);
          LOG.d(LOG_TAG, showMsg);
        }

      }
    }).start();

    return map;
  }
  private Map getPrinterState(){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          PrinterStatus printerStatus = printer.getStatus();
          showMsg = "打印机状态：" + printerStatus;
          map.put("status", SUCCESS);
          map.put("msg", showMsg);
          map.put("state", printerStatus.toString());
          LOG.d(LOG_TAG, showMsg);
        } catch (Exception e) {
          e.printStackTrace();
          showMsg = "获取打印机状态异常：" + e;
          map.put("status", FAILED);
          map.put("msg", showMsg);
          LOG.d(LOG_TAG, showMsg);
        }
      }
    }).start();
    return map;
  }

  /**
   * 线程等待、唤醒
   *
   */
  private class WaitThreat {
    Object syncObj = new Object();

    void waitForRslt() throws InterruptedException {
      synchronized (syncObj) {
        syncObj.wait();
      }
    }

    void notifyThread() {
      synchronized (syncObj) {
        syncObj.notify();
      }
    }
  }
}
