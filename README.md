# nlpos
nl pos cordova plugin

1、插件添加到cordova项目：
	ionic cordova plugin rm cordova-plugin-nlpos 若有先移除
	ionic cordova plugin add D:\Workspaces\nlpos  添加，指定插件项目的文件夹位置

2、andooid平台插件使用示例（仅ts文件代码）：
	-- 添加自定义事件监听：
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

    --打开读卡器：
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

	-- 关闭读卡器：
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

	-- 开始扫码：
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

	-- 打印
	print(): void {
	    let promise = new Promise((resolve, reject) => {
	      let bill = "商户名称：开联支付\n";
	      bill += "操作员号(OPERATOR NO.)：001\n";
	      bill += "消费类型：消费 \n商户编号:123455432112345\n";
	      bill += "-----------------------------\n";
	      bill += "+++++++++++++++++++++++++++++\n";
	      cordova.plugins.nlpos.print((bill), data => {
	        this.homeModel.response = "\r\n*******打印同步返回消息*******\r\n" + data; //temp + "\r\n******************************************\r\n" + data;
	        resolve(data);
	      }, error => {
	        reject(error);
	      });
	    });
	}

3、APP项目编译：ionic cordova build android

4、返回消息数据结构示例：
	-- 读卡readcard事件
	# 打开读卡器响应
	{
	    "timeout": "20秒", 					# 打开读卡器时传递的等待刷卡超时时间参数
	    "asyn": false,
	    "msg": "设备连接成功",
	    "status": "success", 				# 事件成功和失败的状态
	    "event": "readcard"
	}
	# 刷卡失败响应
	{
	    "msg": "请先确定非接卡已上电或该数据块已写入数据",
	    "status": "failed",					# 事件成功和失败的状态
	    "event": "readcard",
	    "asyn": true
	}
	# 刷卡成功响应：
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
	# 磁条卡成功响应：
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
	# 扫码成功响应：
	{
	    "asyn": true,
	    "msg": "扫码成功",
	    "status": "success",				# 事件成功和失败的状态
	    "event": "scancode",
	    "data": {
	        "barcodes": "6918163075411"		# 扫码结果信息，包括二维码和条码
	    }
	}

5、TODO：
	-- 插件中读卡器仅支持S50非接卡和词条卡的数据读取
	-- 以上两种卡的数据块或磁道数据规范格式待定，当前仅读取个别块和磁道数据
	-- 打印接口仅支持文本参数传递，文本前打印一固定图片，图文格式待定