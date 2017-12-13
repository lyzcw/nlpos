package com.openunion.cordova.plugins.nlpos;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.newland.mtype.DeviceInfo;
import com.newland.mtype.ModuleType;
import com.newland.mtype.ProcessTimeoutException;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.K21CardReader;
import com.newland.mtype.module.common.cardreader.K21CardReaderEvent;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.TimeUnit;

/**
 * This class echoes a string called from JavaScript.
 */
public class nlpos extends CordovaPlugin {
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  protected static CallbackContext posCallbackContext;
  private String showMsg = "";
  public N900Device n900Device;
  private K21CardReader cardReader;
  private Map map = new HashMap();
  protected static long readTimeout=60L;

  /**
   * Constructor.
   */
  public nlpos() {

  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    Log.d(LOG_TAG, "Execute:" + action + " with :" + args.toString());
    posCallbackContext = callbackContext;

    if (action.equals("openCardReader")) {
      String params = (String) args.get(0);
      this.openCardReader( callbackContext, params );
      return true;
    }else if (action.equals("closeCardReader")) {
      this.closeCardReader( callbackContext );
      return true;
    }else if (action.equals("scan")) {
      this.scan( callbackContext );
      return true;
    }else if (action.equals("print")) {
      String bill = (String) args.get(0);
      this.print( callbackContext, bill );
      return true;
    }else if (action.equals("getAsynMsg")) {
      this.getAsynMsg( callbackContext );
      return true;
    }else if (action.equals("loadkey")) {
      String params = (String) args.get(0);
      this.loadkey( callbackContext, params );
      return true;
    }else if (action.equals("loadrule")) {
      String params = (String) args.get(0);
      this.loadrule( callbackContext, params );
      return true;
    }else if (action.equals("getDeviceInfo")) {
      this.getDeviceInfo( callbackContext);
      return true;
    }else if (action.equals("initDevice")) {
      this.initDevice( callbackContext);
      return true;
    }else if (action.equals("encrypt")) {
      String params = (String) args.get(0);
      this.encrypt( callbackContext, params );
      return true;
    }else if (action.equals("decrypt")) {
      String params = (String) args.get(0);
      this.decrypt( callbackContext, params );
      return true;
    }else if (action.equals("writeKeyStore")) {
      String params = (String) args.get(0);
      this.writeKeyStore( callbackContext, params );
      return true;
    }else if (action.equals("writeTrustStore")) {
      String params = (String) args.get(0);
      this.writeTrustStore( callbackContext, params );
      return true;
    }else if (action.equals("signDevice")) {
      String params = (String) args.get(0);
      this.signDevice( callbackContext, params );
      return true;
    }else if (action.equals("saveParm")) {
      String params = (String) args.get(0);
      this.saveParm( callbackContext, params );
      return true;
    }
    return false;
  }

  private void openCardReader( CallbackContext callbackContext, String params ) throws JSONException {
    map.clear();
    final JSONObject paramJson = new JSONObject( params );
    readTimeout = paramJson.getInt( "readTimeout");

    n900Device=N900Device.getInstance(this.cordova);
    n900Device.callbackContext = callbackContext;

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }

    if( n900Device.isDeviceAlive()) {

      new Thread(new Runnable() {
        @Override
        public void run() {
          Constant.asynMsg = "";
          try {
            cardReader=n900Device.getCardReaderModuleType();
            Log.d(LOG_TAG, "开始：打开读卡器");
            cardReader.openCardReader("请刷卡或者插入IC卡", new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARDREADER, ModuleType.COMMON_RFCARDREADER }, false, true, readTimeout, TimeUnit.SECONDS, new DeviceEventListener<K21CardReaderEvent>() {
              @Override
              public void onEvent(K21CardReaderEvent openCardReaderEvent, Handler handler) {
                Log.d(LOG_TAG, "监听到：刷卡事件");
                Map map1 = new HashMap();
                Map map0 = new HashMap();
                if (openCardReaderEvent.isSuccess()) {
                  Log.d(LOG_TAG, "监听到：刷卡成功");
                  switch (openCardReaderEvent.getOpenCardReaderResult().getResponseCardTypes()[0]) {
                    case MSCARD:
                      showMsg="读卡器识别到【磁条卡】";
                      boolean isCorrent = openCardReaderEvent.getOpenCardReaderResult().isMSDDataCorrectly();
                      if (!isCorrent) {
                        showMsg="刷卡姿势不对，获取的磁道数据不完整，请重刷！";
                        map0.put("status", FAILED);
                        map0.put("msg", showMsg );
                      }else{
                        SwipRead swipdRead = new SwipRead();
                        swipdRead.swiper = n900Device.getK21Swiper();
                        map0 = swipdRead.readExpress();
                      }
                      break;
                    case ICCARD:
                      showMsg="读卡器识别到【插卡】操作";
                      break;
                    case RFCARD:
                      switch (openCardReaderEvent.getOpenCardReaderResult().getResponseRFCardType()) {
                        case ACARD:
                        case BCARD:
                          showMsg="读卡器识别到非接CPU卡";
                          break;
                        case M1CARD:
                          byte sak = openCardReaderEvent.getOpenCardReaderResult().getSAK();
                          if (sak == 0x08) {
                            showMsg="读卡器识别到非接S50卡";
                            RFCardRead rfCardRead = new RFCardRead();
                            rfCardRead.rfCardModule = n900Device.getRFCardModule();
                            Map map2 =  rfCardRead.m1CardPowerOn();
                            if( map2.get("status").equals(SUCCESS)){
                              try {
                                JSONArray ruleidsArray = paramJson.getJSONArray("ruleids");
                                List blockDataList = new ArrayList();
                                Map map4 = getReadBlocks( ruleidsArray );
                                if( map4.get("status").equals(FAILED)){
                                  map0 = map4;
                                }else {
                                  JSONArray jsonArray = (JSONArray) map4.get("data");
                                  for (int n = 0; n < jsonArray.length(); n++) {
                                    //按找到的需读块列表读块
                                    JSONObject jsonObject = jsonArray.getJSONObject(n);
                                    Map map3 = readRfCard(rfCardRead, jsonObject.getInt("block"), jsonObject.getString("key"));
                                    if (map3.get("status").equals(SUCCESS)) {
                                      JSONObject dataJson = new JSONObject( ( HashMap ) map3.get("data") );
                                      String block = dataJson.getString("block");
                                      String blockData = dataJson.getString("data");
                                      Map blockDataMap = new HashMap();
                                      blockDataMap.put("block", block);
                                      blockDataMap.put("data", blockData);
                                      blockDataList.add(blockDataMap);
                                    } else {
                                      showMsg = "读数据块：" + jsonObject.getInt("block") + "失败，继续读下一块";
                                      Log.d(LOG_TAG, showMsg);

                                    }
                                  }
                                  if(null!=blockDataList && !blockDataList.isEmpty()) {
                                    showMsg = "读数据块完成";
                                    Log.d(LOG_TAG, showMsg);
                                    map0.put("status", SUCCESS);
                                    map0.put("msg", showMsg);
                                    map0.put("data", new JSONArray(blockDataList));
                                    Log.d(LOG_TAG, new JSONArray(blockDataList).toString());
                                  }else{
                                    showMsg = "读数据块失败，没有读到数据";
                                    Log.d(LOG_TAG, showMsg);
                                    map0.put("status", FAILED);
                                    map0.put("msg", showMsg);
                                  }
                                }
                              }catch (Exception e){
                                e.printStackTrace();
                                map0.put("status", FAILED);
                                showMsg="json错误：" + e.getMessage();
                              }

                            }else{
                              map0 = map2;
                            }

                          } else if (sak == 0x18) {
                            showMsg="读卡器识别到非接S70卡";
                          } else if (sak == 0x28) {
                            showMsg="读卡器识别到非接S50_pro卡";
                          } else if (sak == 0x38) {
                            showMsg="读卡器识别到非接S70_pro卡";
                          }else{
                            showMsg="sak="+sak;
                            showMsg=showMsg+";读卡器识别到未定义的非接卡";
                          }
                          break;
                        default:
                          showMsg="读卡器识别到未定义的非接卡";
                          break;
                      }

                      break;
                    default:
                      break;
                  }
                  Log.d(LOG_TAG, showMsg);
                  map0.put("event","readcard");
                  map0.put("asyn",true);
                  Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
                  Constant.asynMsg = (new JSONObject(map0)).toString();
                  sendUpdate( new JSONObject(map0), true );
                } else if (openCardReaderEvent.isUserCanceled()) {
                  showMsg = "取消开启读卡器";
                } else if (openCardReaderEvent.isFailed()   ) {
                  if(openCardReaderEvent.getException() instanceof ProcessTimeoutException){
                    showMsg = "超时";
                  }
                  if(openCardReaderEvent.getException().getCause()  instanceof ProcessTimeoutException){
                    showMsg = "超时";
                  }
                  Log.d(LOG_TAG, "读卡器：开启失败");
                  Log.d(LOG_TAG, showMsg);
                  showMsg = "读卡器开启失败";
                  map1.put("status", FAILED);
                  map1.put("msg",showMsg);
                  map1.put("event","readcard");
                  map1.put("asyn",false);
                  sendUpdate( new JSONObject(map1), true );
                  //asynMsg = new JSONObject(map1);
                }
              }

              @Override
              public Handler getUIHandler() {
                return null;
              }
            });
          }catch ( Exception e ) {
            e.printStackTrace();
            showMsg = "读卡器开启异常：";
            map.put("status", FAILED);
            map.put("msg", showMsg + "\r\n" + e.getMessage() );
            map.put("event","readcard");
            map.put("asyn",false);
            Log.d(LOG_TAG, (new JSONObject(map)).toString() );
            sendUpdate( new JSONObject(map), true );
          }
        }
      }).start();
      map.put("msg","读卡器开启完成");
      map.put("event","readcard");
      map.put("asyn",false);
      map.put("timeout", readTimeout + "秒");
      Log.d(LOG_TAG, new JSONObject(map).toString());
      sendUpdate( new JSONObject(map), true );

    }else {
      // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, (new JSONObject(map)).toString());
      // pluginResult.setKeepCallback(true);
      // callbackContext.sendPluginResult(pluginResult);
      Log.d(LOG_TAG, new JSONObject(map).toString());
      sendUpdate( new JSONObject(map), true );
    }
  }

  private void cancelCardReader( CallbackContext callbackContext) {
    Map map = new HashMap();
    // new Thread(new Runnable() {

    //  @Override
    //  public void run() {
    try {
      cardReader.cancelCardRead();
      showMsg = "撤销读卡操作：成功";
      map.put("status", SUCCESS);
      map.put("msg", showMsg );
    } catch (Exception e) {
      showMsg = "撤销读卡操作 异常：";
      map.put("status", FAILED);
      map.put("msg", showMsg );
    }
    //  }
    // }).start();
    callbackContext.success( (new JSONObject(map)).toString() );
  }

  private void getAsynMsg( CallbackContext callbackContext) {
    try {
      //if( asynMsg.getString("status").equals(SUCCESS)){
      callbackContext.success( Constant.asynMsg );
      Log.d(LOG_TAG, "Constant.asynMsg："+Constant.asynMsg );
      //}
    } catch (Exception e) {
      showMsg = "读取异步消息 异常";
      map.put("status", FAILED);
      map.put("msg", showMsg );
      callbackContext.success( (new JSONObject(map)).toString() );
      Log.d(LOG_TAG, showMsg );
    }
  }

  private void closeCardReader( CallbackContext callbackContext) {
    Map map = new HashMap();
    //  new Thread(new Runnable() {

    //  @Override
    //  public void run() {
    try {
      cardReader.closeCardReader();
      showMsg = "关闭读卡器：成功";
      map.put("status", SUCCESS);
      map.put("msg", showMsg );
    } catch (Exception e) {
      showMsg = "关闭读卡器 异常：";
      map.put("status", FAILED);
      map.put("msg", showMsg );
    }
    //  }
    //}).start();
    callbackContext.success( (new JSONObject(map)).toString() );

  }

  private void scan( CallbackContext callbackContext) {
    this.n900Device=N900Device.getInstance(this.cordova);

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }
    map.put("event","scancode");
    map.put("asyn",false);
    map.put("timeout", "10秒");
    Log.d(LOG_TAG, new JSONObject(map).toString());
    sendUpdate( new JSONObject(map), true );
    if( n900Device.isDeviceAlive()) {
      Scan scan = new Scan(this.n900Device, this.cordova.getActivity());
      scan.scan();
    }

  }

  private void print( CallbackContext callbackContext, String bill) {
    map.clear();
    n900Device=N900Device.getInstance(this.cordova);
    Print print = new Print();
    print.n900Device  = n900Device;

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }
    if( n900Device.isDeviceAlive()) {
      Map map0 = new HashMap();
      map0 = print.print( bill );
      if(map0.get("status").equals(FAILED)){
        callbackContext.success((new JSONObject(map0)).toString());
      }else{
        callbackContext.success((new JSONObject(map0)).toString());
      }
    }
  }

  private void getDeviceInfo( CallbackContext callbackContext ) {
    map.clear();
    n900Device=N900Device.getInstance(this.cordova);

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }
    if( n900Device.isDeviceAlive()) {
      Map map0 = new HashMap();
      Map map1 = new HashMap();
      map0.put("status", SUCCESS);
      DeviceInfo deviceInfo = n900Device.getDevice().getDeviceInfo();
      map1.put("sn",deviceInfo.getSN() );
      map1.put("appVer",deviceInfo.getAppVer() );
      map1.put("udid",deviceInfo.getUdid() );
      map1.put("csn",deviceInfo.getCSNData() );
      map1.put("ksn",deviceInfo.getKSNData());
      map1.put("pid",deviceInfo.getPIDNums() );
      map1.put("vid",deviceInfo.getVID() );
      map1.put("customSN",deviceInfo.getCustomSN() );
      map1.put("firmwareVer",deviceInfo.getFirmwareVer() );
      map1.put("isFactoryModel",deviceInfo.isFactoryModel() );
      map1.put("isMainkeyLoaded",deviceInfo.isMainkeyLoaded() );
      map1.put("isWorkingkeyLoaded",deviceInfo.isWorkingkeyLoaded() );
      map1.put("isDUKPTkeyLoadedfalse",deviceInfo.isDUKPTkeyLoaded() );
      map1.put("isSupportAudio",deviceInfo.isSupportAudio() );
      map1.put("isSupportBlueTooth",deviceInfo.isSupportBlueTooth());
      map1.put("isSupportUSB",deviceInfo.isSupportUSB());
      map1.put("isSupportMagCard",deviceInfo.isSupportMagCard() );
      map1.put("isSupportICCard",deviceInfo.isSupportICCard());
      map1.put("isSupportQuickPass",deviceInfo.isSupportQuickPass());
      map1.put("isSupportPrint",deviceInfo.isSupportPrint());
      map1.put("isSupportLCD",deviceInfo.isSupportLCD() );
      map1.put("isSupportOffLine",deviceInfo.isSupportOffLine() );

      Log.d(LOG_TAG, new JSONObject(map1).toString() );
      map0.put("msg", "获取设备信息成功");
      map0.put("data", map1);
      callbackContext.success((new JSONObject(map0)).toString());

    }
  }

  private void initDevice( CallbackContext callbackContext ) {
    map.clear();
    n900Device=N900Device.getInstance(this.cordova);

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }
    if( n900Device.isDeviceAlive()) {
      Map map0 = new HashMap();
      Map map1 = new HashMap();
      map0.put("status", SUCCESS);
      DeviceInfo deviceInfo = n900Device.getDevice().getDeviceInfo();
      map1.put("sn",deviceInfo.getSN() );

      Log.d(LOG_TAG, new JSONObject(map1).toString() );
      map0.put("msg", "初始化设备信息成功");
      map0.put("data", map1);
      callbackContext.success((new JSONObject(map0)).toString());

    }
  }

  private void loadkey( CallbackContext callbackContext, String params ) {
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String storepath = Environment.getExternalStorageDirectory() + paramsObject.getString("storepath");
        String password = paramsObject.getString("password");
//        EncryUtils encryUtils = new EncryUtils();
//        Map rtnmap = encryUtils.loadFromKeystoe(storepath, password);
//        Log.d(LOG_TAG, rtnmap.toString());
//        callbackContext.success((new JSONObject(rtnmap)).toString());
      } else {
        map.put("status", FAILED);
        map.put("msg", "参数不能为空");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void signDevice( CallbackContext callbackContext, String params ) {
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String deviceid = paramsObject.getString("deviceid");
        Context context = this.cordova.getActivity();
        String rtnStr = SSLClient.signDevice(context, deviceid );
        Log.d(LOG_TAG, rtnStr );
        // map.put("status", SUCCESS);
        // map.put("msg", "签到成功");
        // map.put("data", rtnStr);
        //map.put("clear", deStr);
        if( null == rtnStr ){
          map.put("status", FAILED);
          map.put("msg", "SSL连接失败");
          map.put("data", "");
          callbackContext.success((new JSONObject(map)).toString());
        }else {
          callbackContext.success(rtnStr);
        }
      } else {
        map.put("status", FAILED);
        map.put("msg", "参数不能为空");
        map.put("data", "");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }catch ( Exception e ) {
      showMsg += "签到参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void encrypt( CallbackContext callbackContext, String params ) {
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String key = paramsObject.getString("key");
        String content = paramsObject.getString("content");

        String rtnStr = AESUtils.encrypt( content, key, Constant.iv);
        //String deStr = AESUtils.decrypt( rtnStr, key, Constant.iv);
        Log.d(LOG_TAG, rtnStr );
        map.put("status", SUCCESS);
        map.put("msg", "加密成功");
        map.put("data", rtnStr);
        //map.put("clear", deStr);
        callbackContext.success((new JSONObject(map)).toString());

      } else {
        map.put("status", FAILED);
        map.put("msg", "参数不能为空");
        map.put("data", "");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }catch ( Exception e ) {
      showMsg += "加密参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void decrypt( CallbackContext callbackContext, String params ) {
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String key = paramsObject.getString("key");
        String content = paramsObject.getString("content");

        String rtnStr = AESUtils.decrypt( content, key, Constant.iv);
        //String deStr = AESUtils.decrypt( rtnStr, key, Constant.iv);
        Log.d(LOG_TAG, rtnStr );
        map.put("status", SUCCESS);
        map.put("msg", "解密成功");
        map.put("data", rtnStr);
        //map.put("clear", deStr);
        callbackContext.success((new JSONObject(map)).toString());

      } else {
        map.put("status", FAILED);
        map.put("msg", "参数不能为空");
        map.put("data", "");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }catch ( Exception e ) {
      showMsg += "解密参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void writeKeyStore( CallbackContext callbackContext, String params ) {
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String privKeyStr = paramsObject.getString("privKeyStr");
        String posAlias = paramsObject.getString("posAlias");
        String certStr = paramsObject.getString("certStr");
        Context context = this.cordova.getActivity();
        if (RSAUtils.writeKeyStore(context, posAlias, certStr, Base64Utils.decode(privKeyStr), Constant.storepath, Constant.storepass, Constant.keypass)){
          map.put("status", SUCCESS);
          map.put("msg", "将密钥对写入keystore成功");
          map.put("data", "");
          //map.put("clear", deStr);
          callbackContext.success((new JSONObject(map)).toString());
        } else {
          map.put("status", FAILED);
          map.put("msg", "将密钥对写入keystore失败");
          map.put("data", "");
          callbackContext.success((new JSONObject(map)).toString());
        }
      } else {
        map.put("status", FAILED);
        map.put("msg", "参数不能为空");
        map.put("data", "");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void writeTrustStore( CallbackContext callbackContext, String params ) {
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String serverCert = paramsObject.getString("serverCert");
        String caCertStr = paramsObject.getString("caCertStr");
        String serverAlias = paramsObject.getString("serverAlias");
        String caAlias = paramsObject.getString("caAlias");
        Context context = this.cordova.getActivity();
        if (RSAUtils.writeKeyStore(context,serverAlias, serverCert, Constant.serverstorepath, Constant.storepass) &&
          RSAUtils.writeKeyStore(context, caAlias, caCertStr, Constant.serverstorepath, Constant.storepass)){
          map.put("status", SUCCESS);
          map.put("msg", "将服务端和CA公钥证书写入信任keystore成功");
          map.put("data", "");
          //map.put("clear", deStr);
          callbackContext.success((new JSONObject(map)).toString());
        } else {
          map.put("status", FAILED);
          map.put("msg", "将公钥证书写入keystore失败");
          map.put("data", "");
          callbackContext.success((new JSONObject(map)).toString());
        }
      } else {
        map.put("status", FAILED);
        map.put("msg", "参数不能为空");
        map.put("data", "");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      map.put("data", "");
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void saveParm( CallbackContext callbackContext, String params ) {
    map.clear();
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);

        String rtnStr = paramsObject.getString("parms");
        if( null== rtnStr){
          Log.d(LOG_TAG, "解析卡标准失败，请检查" );
          map.put("status", FAILED);
          map.put("msg", "解析卡标准失败，请检查");
          callbackContext.success((new JSONObject(map)).toString());
        }else{
          rtnStr = rtnStr.replaceAll("\r", "").replaceAll("\n","").replaceAll("\t","").replaceAll("\\s","").replaceAll("　","").replaceAll(" ","");
          Constant.cardrule = new JSONArray( rtnStr );
          Log.d(LOG_TAG, rtnStr );
          map.put("status", SUCCESS);
          map.put("msg", "载入卡标准成功");
          map.put("data", rtnStr);
          callbackContext.success((new JSONObject(map)).toString());
        }

      } else {
        map.put("status", FAILED);
        map.put("msg", "json参数不能为空");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数传递错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  private void loadrule( CallbackContext callbackContext, String params ) {
    map.clear();
    try {
      if (null != params && !"".equals(params)) {
        JSONObject paramsObject = new JSONObject(params);
        String storepath = paramsObject.getString("storepath"); //Environment.getExternalStorageDirectory() +

        String rulepath = paramsObject.getString("rulepath");
        String alias = paramsObject.getString("alias");
        String storepass = paramsObject.getString("storepass");
        String keypass = paramsObject.getString("keypass");
        RSAUtils rsaUtils = new RSAUtils();
        Context context = this.cordova.getActivity();
        String rtnStr = rsaUtils.decryptFileAndroid(context, alias, keypass, storepass, "BKS",storepath, rulepath, 256 );
        if( null== rtnStr){
          Log.d(LOG_TAG, "读取远程卡标准失败，请检查网络连接或文件是否存在" );
          map.put("status", FAILED);
          map.put("msg", "读取远程卡标准失败，请检查网络连接或文件是否存在");
          callbackContext.success((new JSONObject(map)).toString());
        }else{
          rtnStr = rtnStr.replaceAll("\r", "").replaceAll("\n","").replaceAll("\t","").replaceAll("\\s","").replaceAll("　","").replaceAll(" ","");
          Constant.cardrule = new JSONArray( rtnStr );
          Log.d(LOG_TAG, rtnStr );
          map.put("status", SUCCESS);
          map.put("msg", "解密并载入卡标准成功");
          map.put("data", rtnStr);
          callbackContext.success((new JSONObject(map)).toString());
        }

      } else {
        map.put("status", FAILED);
        map.put("msg", "json参数不能为空");
        callbackContext.success((new JSONObject(map)).toString());
      }
    }catch (JSONException e ) {
      showMsg += "JSON参数传递错误" + e.getMessage();
      map.put("status", FAILED);
      map.put("msg", showMsg);
      LOG.d(LOG_TAG, showMsg);
      callbackContext.success((new JSONObject(map)).toString());
    }
  }

  public static Handler getScanEventHandler() {
    return scanEventHandler;
  }

  public static void setScanEventHandler(Handler scanEventHandler) {
    nlpos.scanEventHandler = scanEventHandler;
  }

  private static Handler scanEventHandler = new Handler(Looper.getMainLooper()) {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case Const.ScanResult.SCAN_FINISH: {
          Map map0 = new HashMap();
          map0.put("status", SUCCESS);
          map0.put("msg", "扫码结束");
          map0.put("event","scancode");
          map0.put("asyn",true);
          Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
          // Constant.asynMsg = (new JSONObject(map0)).toString();
          //sendUpdate( new JSONObject(map0), true );
          break;

        }
        case Const.ScanResult.SCAN_RESPONSE: {
          Bundle bundle = msg.getData();
          String[] barcodes = bundle.getStringArray("barcodes");
          //mainActivity.showMessage("-----扫码结果------"+ barcodes[0] + "\r\n", MessageTag.NORMAL);
          Map map0 = new HashMap();
          map0.put("status", SUCCESS);
          map0.put("msg", "扫码成功");
          Map map1 = new HashMap();
          map1.put("barcodes", barcodes[0]);
          map0.put("data", map1);
          map0.put("event","scancode");
          map0.put("asyn",true);
          Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
          Constant.asynMsg = (new JSONObject(map0)).toString();
          sendUpdate( new JSONObject(map0), true );
//        if (scanner != null) {
//          scanner.stopScan();
//        }
          break;
        }
        case Const.ScanResult.SCAN_ERROR: {
          Bundle bundle = msg.getData();
          int errorCode = bundle.getInt("errorCode");
          String errorMess = bundle.getString("errormessage");
          // mainActivity.showMessage("扫码异常，异常码：" + errorCode + ",异常信息：" + errorMess+ "\r\n", MessageTag.NORMAL);
          Map map0 = new HashMap();
          map0.put("status", FAILED);
          map0.put("msg", "扫码异常，异常码：" + errorCode + ",异常信息：" + errorMess);
          map0.put("event","scancode");
          map0.put("asyn",true);
          Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
          Constant.asynMsg = (new JSONObject(map0)).toString();
          sendUpdate( new JSONObject(map0), true );
          break;
        }

        default:
          break;
      }
    }

  };

  public Map getReadBlocks( JSONArray ruleids ) throws Exception {
    List readBlodks = new ArrayList();
    Map rtnMap = new HashMap();
    if( null == Constant.cardrule ){
      showMsg = "卡标准为空或没有预读取，不能读卡";
      Log.d(LOG_TAG, showMsg );
      rtnMap.put("status", FAILED);
      rtnMap.put("msg", showMsg);
      return rtnMap; //无卡标准，无法读卡
    }else if(  null==ruleids || ruleids.length()<=0 ){//所有标准全部读取
      for (int i=0; i<  Constant.cardrule.length(); i++){
        JSONObject jsonObject =  Constant.cardrule.getJSONObject(i);
        int startSection = jsonObject.getInt( "section");
        int sectionNum = jsonObject.getInt("num");
        for( int j=startSection; j<startSection + sectionNum; j++ ){
          for( int m = j * 4; m< j*4 +3; m++){
            Map blockMap = new HashMap();
            blockMap.put("block", m );
            blockMap.put("key", jsonObject.getString("key"));
            readBlodks.add( blockMap );
          }
        }
      }
    }else if(  null!=ruleids && ruleids.length()>0 ){//指定了读取标准，读取和标准的交集，否则无法读
      for(int n=0; n<ruleids.length(); n++) {
        for (int i = 0; i < Constant.cardrule.length(); i++) {
          JSONObject jsonObject = Constant.cardrule.getJSONObject(i);
          if( ruleids.getJSONObject(n).getString("ruleid").equals( jsonObject.getString("ruleid"))) {
            int startSection = jsonObject.getInt("section");
            int sectionNum = jsonObject.getInt("num");
            for (int j = startSection; j < startSection + sectionNum; j++) {
              for (int m = j * 4; m < j * 4 + 3; m++) {
                Map blockMap = new HashMap();
                blockMap.put("block", m + "");
                blockMap.put("key", jsonObject.getString("key"));
                readBlodks.add( blockMap );
              }
            }
          }
        }
      }

    }
    if( null==readBlodks || readBlodks.isEmpty()){
      showMsg = "卡标准中没有找到指定的标准，无法读卡";
      Log.d(LOG_TAG, showMsg );
      rtnMap.put("status", FAILED);
      rtnMap.put("msg", showMsg);
    }
    showMsg = "从卡标准中找到了要读的块信息";
    Log.d(LOG_TAG, showMsg );
    rtnMap.put("status", SUCCESS);
    rtnMap.put("msg", showMsg);
    rtnMap.put("data", new JSONArray(readBlodks));
    Log.d(LOG_TAG, readBlodks.toString() );
    return  rtnMap;
  }

  public CallbackContext getPosCallbackContext() {
    return posCallbackContext;
  }

  private Map readRfCard( RFCardRead rfCardRead, int block, String key ){
    Map map0 = new HashMap();
    Map map3 =  rfCardRead.authenticateByExtendKey( block, key );
    if( map3.get("status").equals(SUCCESS)){
      map0 = rfCardRead.readBlock( block );
    }else{
      map0 = map3;
    }
    return map0;
  }
  /**
   * Create a new plugin result and send it back to JavaScript
   *
   * @param*connection the network info to set as navigator.connection
   */
  private static void sendUpdate(JSONObject info, boolean keepCallback) {
    if (posCallbackContext != null) {
      Map map0 = new HashMap();
      map0.put("info", info.toString());
      PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(map0) );
      //PluginResult result = new PluginResult(PluginResult.Status.OK, info.toString() );
      result.setKeepCallback(keepCallback);
      posCallbackContext.sendPluginResult(result);
    }
  }
}
