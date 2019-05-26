import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterLcIm.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: _buildBtn(),
        ),
      ),
    );
  }

  Widget _buildBtn() {
    return RaisedButton(
      child: Text('跳转去聊天界面'),
      onPressed: () {
        FlutterLcIm.register("uAsHYp2qXIhJ6SYB88ehVXCr-gzGzoH1sz", "5HdoMlbpmaKDfs7H4blpd1LVf","1");
        Map user = {'name':'jason1','user_id':"1",'avatar_url':"http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"};
        Map peer = {'name':'jason2','user_id':"3",'avatar_url':"http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"};
         FlutterLcIm.pushToConversationView(user,peer);
         // FlutterLcIm.getConversationList();
          print('object');
      },
    );
  }
}
