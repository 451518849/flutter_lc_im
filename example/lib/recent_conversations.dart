import 'package:flutter/material.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:flutter_lc_im_example/conversation.dart';

class ConversationListPage extends StatefulWidget {
  Conversations conversations;
  ConversationListPage({this.conversations});
  @override
  _ConversationListState createState() => _ConversationListState();
}

class _ConversationListState extends State<ConversationListPage> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Scaffold(
      appBar: AppBar(
        title: Text('最近联系人列表'),
      ),
      body: ListView.builder(
          scrollDirection: Axis.vertical,
          itemCount: 1,
          itemBuilder: (BuildContext context, int index) {
            return InkWell(
                onTap: () {
                  Map user = {
                    'name': 'jason1',
                    'user_id': "6",
                    'avatar_url':
                        "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"
                  };
                  Map peer = {
                    'name': 'jason2',
                    'user_id': "1",
                    'avatar_url':
                        "http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100"
                  };
                  FlutterLcIm.pushToConversationView(user, peer);
                },
                child: _buildListItem(index));
          }),
    );
  }

  Widget _buildListItem(int index) {
    return Container(
      height: 60,
      // color: Colors.yellow,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: <Widget>[
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              Container(
                height: 60,
                child: Stack(
                  children: <Widget>[
                    Container(
                      margin: EdgeInsets.only(left: 14, top: 7, right: 6),
                      padding: EdgeInsets.all(10),
                      height: 46,
                      width: 46,
                      decoration: BoxDecoration(
                        color: Colors.black,
                        borderRadius: BorderRadius.circular(6.0),
                        // image url 去要到自己的服务器上去请求回来再赋值，这里使用一张默认值即可
                        image: DecorationImage(
                            image: NetworkImage(
                                'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100')),
                      ),
                    ),
                    widget.conversations.conversations[index]
                                .unreadMessagesCount ==
                            0
                        ? SizedBox()
                        : Container(
                            margin: EdgeInsets.only(left: 50, top: 2),
                            padding: EdgeInsets.only(left: 5, top: 1),
                            decoration: BoxDecoration(
                              color: Colors.red,
                              borderRadius: BorderRadius.circular(10.0),
                            ),
                            height: 16,
                            width: 16,
                            child: Text(
                              '${widget.conversations.conversations[index].unreadMessagesCount}',
                              style:
                                  TextStyle(fontSize: 10, color: Colors.white),
                            ),
                          )
                  ],
                ),
              ),
              Container(
                margin: EdgeInsets.only(left: 6, top: 4),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Container(
                      margin: EdgeInsets.only(top: 8),
                      child: Text(
                        widget.conversations.conversations[index].peerName,
                        style: TextStyle(fontWeight: FontWeight.w500),
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(bottom: 8),
                      child: Text(
                          widget.conversations.conversations[index]
                              .lastMessageContent,
                          style: TextStyle(color: Colors.grey, fontSize: 12)),
                    ),
                  ],
                ),
              ),
            ],
          ),
          Container(
            margin: EdgeInsets.only(right: 14, bottom: 18),
            child: Text(widget.conversations.conversations[index].lastMessageAt,
                style: TextStyle(color: Colors.grey, fontSize: 11)),
          )
        ],
      ),
    );
  }
}
