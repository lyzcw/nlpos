# nlpos
nl pos cordova plugin

## 1、插件添加到cordova项目：
	ionic cordova plugin rm cordova-plugin-nlpos 若有先移除
	ionic cordova plugin add D:\Workspaces\nlpos  添加，指定插件项目的文件夹位置

## 2、andooid平台插件使用示例（仅ts文件代码）：
### 	-- 添加自定义事件监听：
	constructor(private platform: Platform, private events: Events, public navCtrl: NavController, public renderer: Renderer) {
    platform.ready().then((e) => {
      
      window.addEventListener('readcard', (data:any) => {
          //alert('刷卡信息：' + data.info);
          console.log('刷卡事件：' + data.info );
          let temp = this.homeModel.response;
          this.homeModel.response = "\r\n*******刷卡事件*******\r\n" + data.info; // + "Info:" + data;
        }, false );

      window.addEventListener('scancode', (data:any) => {
          //alert('扫码信息：' + data.info);
          console.log('扫码事件：' + data.info );
          let temp = this.homeModel.response;
          this.homeModel.response = "\r\n*******扫码事件*******\r\n" + data.info; // + "Info:" + data;
      }, false );
    });

###     --打开读卡器：
    openCardReader(): void {
	    let promise = new Promise((resolve, reject) => {
	      let readTimeout = 20;
	      cordova.plugins.nlpos.openCardReader((readTimeout), data => {
	      	// 打开读卡器没有同步消息返回，返回消息在readcard事件中监听
	        resolve(data);
	      }, error => {
	        reject(error);
	      });
	    });
	}

### 	-- 关闭读卡器：
	closeCardReader(): void {
    
    let promise = new Promise((resolve, reject) => {
	      cordova.plugins.nlpos.closeCardReader( data => {
	        this.homeModel.response = "\r\n*******刷卡同步返回消息*******\r\n" + data;
	        resolve(data);
	      }, error => {
	        reject(error);
	      });
	    });
	}

### 	-- 开始扫码：
	scan(): void {
	    let promise = new Promise((resolve, reject) => {
	      cordova.plugins.nlpos.scan( data => {
	        // 扫码没有同步消息返回，返回消息在scancode事件中监听
	        resolve(data);
	      }, error => {
	        reject(error);
	      });
	    });
	}

### 	-- 打印
	print(): void {
    let promise = new Promise((resolve, reject) => {
      let billTitle = "开联支付凭证";
      let billStr = "商户名称：开联支付\n";
      billStr += "操作员号(OPERATOR NO.)：001\n";
      billStr += "消费类型：消费 \n商户编号:123455432112345\n";
      billStr += "-----------------------------\n";
      billStr += "+++++++++++++++++++++++++++++\n";
      let billIco ="data:image/png;base64,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";
      let billArray = new Array();
      let jsonStr = '{"type":"title", "data":"'+ billTitle + '"}';
      billArray.push(jsonStr);
      jsonStr = '{"type":"image", "data":"'+ billIco + '"}';
      billArray.push(jsonStr);
      jsonStr = '{"type":"text", "data":"'+ billStr + '"}';
      billArray.push(jsonStr);
      let bill = "[" + billArray.toString()+ "]";
      console.log('打印请求：' + bill );
      cordova.plugins.nlpos.print((bill), data => {
        this.homeModel.response = "\r\n*******打印同步消息*******\r\n" + data;
        resolve(data);
      }, error => {
        reject(error);
      });
    });
  }

## 3、APP项目编译：ionic cordova build android

## 4、返回消息数据结构示例：
### 	-- 读卡readcard事件
#### 打开读卡器响应
	{
	    "timeout": "20秒", 					# 打开读卡器时传递的等待刷卡超时时间参数
	    "asyn": false,
	    "msg": "设备连接成功",
	    "status": "success", 				# 事件成功和失败的状态
	    "event": "readcard"
	}
####  刷卡失败响应
	{
	    "msg": "请先确定非接卡已上电或该数据块已写入数据",
	    "status": "failed",					# 事件成功和失败的状态
	    "event": "readcard",
	    "asyn": true
	}
####  刷卡成功响应：
	{
	    "asyn": true,
	    "msg": "读块数据完成",
	    "status": "success",				# 事件成功和失败的状态
	    "event": "readcard",
	    "data": {
	        "block": "2",					# 非接卡数据块号
	        "typeName": "非接S50",			# 非接卡类型名称
	        "cardType": "M1CARD",			# 非接卡类型代码
	        "data": "00000000000000000000000000000000" # 非接卡块数据
	    }
	}
####  磁条卡成功响应：
	{
	    "asyn": true,
	    "msg": "读取磁条卡数据成功",
	    "status": "success",
	    "event": "readcard",
	    "data": {
	    	"typeName": "磁条卡",			# 磁条卡类型名称
	        "cardType": "MSCARD",			# 磁条卡类型代码
	        "track-3": "null",				# 磁条卡磁道3数据
	        "track-2": "6660100029906598=99122015540000000000"	# 磁条卡磁道2数据
	    }
	}
####  扫码成功响应：
	{
	    "asyn": true,
	    "msg": "扫码成功",
	    "status": "success",				# 事件成功和失败的状态
	    "event": "scancode",
	    "data": {
	        "barcodes": "6918163075411"		# 扫码结果信息，包括二维码和条码
	    }
	}

## 5、TODO：
	-- 插件中读卡器仅支持S50非接卡和词条卡的数据读取
	-- 以上两种卡的数据块或磁道数据规范格式待定，当前仅读取个别块和磁道数据
	-- 打印接口仅支持文本参数传递，文本前打印一固定图片，图文格式待定

