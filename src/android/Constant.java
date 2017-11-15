package com.openunion.cordova.plugins.nlpos;

import com.newland.mtype.module.common.rfcard.RFKeyMode;
import com.newland.mtype.util.ISOUtils;

import org.json.JSONArray;

/**
 * Created by lyzcw on 2017/9/6.
 */

public class Constant {
  //默认AES加密初始向量
  public static final String iv = "OpenUnionInitvec";
  //默认服务端keystore路径
  public static final String serverstorepath = "postrustks.bks";
  //默认服务端证书alias
  public static final String serverstorealias = "OpenUnionPosServer";
  //默认客户端keystore路径
  public static final String storepath = "posks.bks";
  //默认服务端keystore密码
  public static final String storepass = "OU@POS";
  //默认服务端keystore秘钥密码
  public static final String keypass = "POS@OU";
  //默认客户端证书subjectDN
  public static final String subjectDN = ",ou=OpenUnion Certificate Authority,o=OpenUnion Inc,c=cn";
  //默认客户端证书issuerDN
  public static final String issuerDN = "ou=OpenUnion Certificate Authority,o=OpenUnion Inc,c=cn";
  //默认证书有效期
  public static final int validity = 3650;
  //载入的卡标准json
  public static JSONArray cardrule = null;
  //默认外部秘钥
  public static final String key = "ffffffffffff";
  //默认的块号
  public static final int block = 2;
  //默认SNR序列号
  public static final String snr = "8B7A84EF";
  //key模式
  public static final RFKeyMode qpKeyMode = RFKeyMode.KEYA_0X60;
  //异步消息
  public static String asynMsg = "";
  //打印
  public static final String icon_path = "/res/drawable/printicon.png";
  public static final String merchant_label = "商户名称(MERCHANT NAME)：";
  public static final String merchant_name = "钓鱼岛";
  public static final String merchant_code_label = "商户编号:：";
  public static final String merchant_code = "123455432112345";

  public static final String operator_label = "操作员号(OPERATOR NO.)：";
  public static final String operator_no = "001";
  public static final String consume_type_label ="消费类型：";
  public static final String consume_type ="消费";
  public static final String signl_line = "-----------------------------";
  public static final String plus_line = "+++++++++++++++++++++++++++++";
}
