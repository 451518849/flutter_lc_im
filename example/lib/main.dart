import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:flutter_lc_im_example/conversation.dart';
import 'package:flutter_lc_im_example/recent_conversations.dart';

void main() => runApp(MaterialApp(home: MyApp()));

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
    //dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz ye24iIK6ys8IvaISMC4Bs5WK
    FlutterLcIm.register(
        "dYRQ8YfHRiILshUnfFJu2eQM-gzGzoHsz", "ye24iIK6ys8IvaISMC4Bs5WK");
    FlutterLcIm.login("6");
    return Scaffold(
      appBar: AppBar(
        title: const Text('Lean Cloud Plugin example app'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            _buildChatViewBtn(),
            _buildConversationListView(context)
          ],
        ),
      ),
    );
  }

  Widget _buildChatViewBtn() {
    return RaisedButton(
      child: Text('跳转去聊天界面'),
      onPressed: () {
        Map user = {
          'name': 'jason1',
          'user_id': "6",
          'avatar_url':
              "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"
        };
        Map peer = {
          'name': 'jason2',
          'user_id': "4",
          'avatar_url':
              "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"
        };
        FlutterLcIm.pushToConversationView(user, peer);
      },
    );
  }

  Widget _buildConversationListView(BuildContext context) {
    return RaisedButton(
      child: Text('跳转去最近联系人列表'),
      onPressed: () async {
        FlutterLcIm.getRecentConversationUsers().then((res) {
          if (res != [] && res != null) {
            Conversations conversations = Conversations.fromJson(res);
                      print('conversations:${conversations.conversations}');

            Navigator.of(context).push(MaterialPageRoute(
                builder: (BuildContext context) => ConversationListPage(conversations: conversations,)));
          }
        });
      },
    );
  }
}
