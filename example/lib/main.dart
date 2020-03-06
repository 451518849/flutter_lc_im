import 'package:flutter/material.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:flutter_lc_im_example/model/user.dart';
import 'package:flutter_lc_im_example/page/conversation.dart';

import 'page/conversation_list.dart';

void main() => runApp(MaterialApp(home: MyApp()));

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();

    FlutterLcIm.register("-gzGzoHsz",
        "xx", "https://leancloud.xx.com",false);
    FlutterLcIm.login("1");
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
        ImUser currentUser = ImUser(
            uid: '1',
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
