# flutter_lc_im
![](https://img.shields.io/badge/build-passing-brightgreen)
![](https://img.shields.io/badge/version-1.1.0-orange)
![](https://img.shields.io/badge/platform-flutter-lightgrey)
![](https://img.shields.io/badge/license-MIT-blue)

### Introduction
超级简单、轻量的Flutter聊天插件,支持ios和android两个平台。关键是免费！！！强势推荐一波LeanCloud！！! 这可能就是你要找的免费又好用的 flutter im。LeanCloud的使用[传送门](https://leancloud.cn/)。

![](index.jpeg)
![](list.jpeg)
![](chat1.jpeg)
![](chat2.jpeg)
![](chat3.jpeg)

### Important
全面升级flutter_lc_im,彻底剥离原生界面，让界面定制更容易！在0.2.5版本以前聊天界面还是用的原生代码，使得维护起来非常麻烦，尤其是对UI的修改，更是繁琐。为此，在1.0.0以后的版中去除原生界面的代码，使用flutter编写界面，让用户可以根据自己的业务绘制界面。同时example中已经提供了一套聊天UI，可以直接使用。目前还处于Beta版，还在升级中，如需更多功能请Star支持。

### Support
 
- [x] 一对一聊天 
- [x] 获取聊天列表（可自定义列表UI）
- [x] 根据聊天情况刷新聊天列表 
- [x] 给出聊天列表上的未读消息数 
- [x] 发送图片消息
- [x] 发送语音消息
-  [x]   发送视频消息
- [x] 发送表情
- [ ] 团组聊天
- [x] 消息推送

- [ ] ...... 

### Flutter version
	v1.12.13+hotfix.8

### Install
Add this to your package's pubspec.yaml file:

	dependencies:
		flutter_lc_im: ^1.1.0
		  
	flutter packages get

### Use：

#### 注册
    FlutterLcIm.register("appId","appKey", "api",debug);
#### 登陆
    FlutterLcIm.login("clientId");
#### 获取聊天列表
    FlutterLcIm.queryHistoryConversations(_limit, _offset);  
#### 创建单聊
    FlutterLcIm.createConversation(peerId,limit);
#### 获取聊天记录
      FlutterLcIm.queryHistoryConversationMessages(）
#### 发送文字消息
       FlutterLcIm.sendTextMessage();   
#### 发送图片消息
       FlutterLcIm.sendImageMessage(); 
#### 发送语音消息
       FlutterLcIm.sendVoiceMessage(); 
#### 发送视频消息
       FlutterLcIm.sendVideoMessage(); 
#### 关于推送
##### ios推送
ios端的推送在AppDelegate.m文件中设置，已给参考代码。主要步骤为三步

1.  通过UNUserNotificationCenter注册
1.  通过AVInstallation设置deviceToken
1. 处理推送

##### android推送
android端目前只支持后台线程推送，暂不支持混合推送。推送代码很简单，只要在项目中的MainActivity中的resume函数中设置几行代码，如下所示：

 
	    @Override
	    protected void onResume() {
	    super.onResume();
	
	    // 获取推送消息数据
	    String message = this.getIntent().getStringExtra("com.avoscloud.Data");
	    String channel = this.getIntent().getStringExtra("com.avoscloud.Channel");
	
	    if (message != null && FlutterLcImPlugin.notificationCallback != null){
	      FlutterLcImPlugin.notificationCallback.success(message);
	    }
    }
   
### More
详细使用请看example中的代码
    
### QQ技术交流：
群聊号：853797155，欢迎交流问题和技术！

### 欢迎PR