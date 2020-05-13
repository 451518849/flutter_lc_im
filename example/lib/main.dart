import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:flutter_lc_im_example/model/user.dart';
import 'package:flutter_lc_im_example/page/conversation.dart';
import 'page/conversation_list.dart';

void main() => runApp(MaterialApp(home: MyApp()));

const String FLUTTER_CHANNEL_CLIENT_STATUS = "flutter_lc_im/client/status";

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const _clientStatusEventChannel = EventChannel(FLUTTER_CHANNEL_CLIENT_STATUS);

  //监听channel，接收会话列表
  void _setClientStatusChannel() {
    print('开始监听IM状态');
    _clientStatusEventChannel
        .receiveBroadcastStream(FLUTTER_CHANNEL_CLIENT_STATUS)
        .listen((Object status) {
      print('IM 状态更变: $status');
    }).onError((Object o) {
      print('IM 状态监听错误: ${o.toString()}');
    });
  }

  @override
  void initState() {
    super.initState();

    FlutterLcIm.register("-gzGzoHsz",
        "xxx", "https://leancloud.xxxx.com",false);
    FlutterLcIm.login("1",notification: true);
    _setClientStatusChannel();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Lean Cloud Im Plugin example app'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            _buildChatViewBtn(),
            _buildConversationListView(context),
          ],
        ),
      ),
    );
  }

  Widget _buildChatViewBtn() {
    return RaisedButton(
      child: Text('跳转去聊天界面'),
      onPressed: () {
        ImUser currentUser = ImUser(
            uid: '6',
            username: '张三',
            avatarUrl:
                'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100');
        ImUser toUser = ImUser(
            uid: '1050',
            username: '莉丝',
            avatarUrl:
                'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100');

        Navigator.push(
          context,
          MaterialPageRoute(
              builder: (context) => ImConversationPage(
                    currentUser: currentUser,
                    toUser: toUser,
                  )),
        );
      },
    );
  }

  Widget _buildConversationListView(BuildContext context) {
    return RaisedButton(
      child: Text('跳转去最近联系人列表'),
      onPressed: () async {
        // List<ImConversation> conversations = List();
        // ImConversation conversation1 = ImConversation(
        //     clientId: "6",
        //     peerName: '测试1',
        //     peerId: '1050',
        //     lastMessage: ImMessage(text: '你好的方式发顺丰'),
        //     lastMessageAt: '2020-2-27',
        //     unreadMessagesCount: 1,
        //     avatarUrl:
        //         'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100');
        // ImConversation conversation2 = ImConversation(
        //     clientId: "6",
        //     peerName: '测试2',
        //     peerId: '1050',
        //     lastMessage: ImMessage(text: '你好的方式发顺丰'),
        //     lastMessageAt: '2020-2-27',
        //     unreadMessagesCount: 10,
        //     avatarUrl:
        //         'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100');
        // conversations.add(conversation1);
        // conversations.add(conversation2);

        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => ImConversationListPage()),
        );
      },
    );
  }
}
