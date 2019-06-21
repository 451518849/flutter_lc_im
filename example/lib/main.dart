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
    //dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz
    FlutterLcIm.register(
        "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz", "ye24iIK6ys8IvaISMC4Bs5WK");
    FlutterLcIm.login("1");
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
        Map user = {
          'name': 'jason1',
          'user_id': "1",
          'avatar_url':
              "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"
        };
        Map peer = {
          'name': 'jason2',
          'user_id': "3",
          'avatar_url':
              "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"
        };
        FlutterLcIm.pushToConversationView(user, peer);
        // FlutterLcIm.getConversationList();
        print('object');
      },
    );
  }
}
