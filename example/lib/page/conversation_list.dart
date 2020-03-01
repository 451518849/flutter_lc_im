import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:flutter_lc_im_example/model/conversation.dart';
import 'package:badges/badges.dart';
import 'package:flutter_lc_im_example/model/user.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';

import 'conversation.dart';

const String CONVERSATION_CHANNEL = "flutter_lc_im/conversations";

class ImConversationListPage extends StatefulWidget {
  final String title;
  ImConversationListPage({this.title = '最近联系人列表'});
  @override
  _ConversationListState createState() => _ConversationListState();
}

class _ConversationListState extends State<ImConversationListPage> {
  List<ImConversation> _conversations = [];

  static const _conversationEventChannel = EventChannel(CONVERSATION_CHANNEL);

  RefreshController _refreshController =
      RefreshController(initialRefresh: false);

  int _offset = 0;
  int _limit = 20; //一次加载20条数据

  //只有下拉刷新，上拉加载leancloud有些问题
  void _onRefresh() async {
    _offset = 0;
    FlutterLcIm.queryHistoryConversations(_limit, _offset);
  }

  Widget imRefreshHeader() {
    return ClassicHeader(
      refreshingText: "加载中...",
      idleText: "加载最新会话",
      completeText: '加载完成',
      releaseText: '松开刷新数据',
      failedIcon: null,
      failedText: '刷新失败，请重试。',
    );
  }

  @override
  void initState() {
    super.initState();
    _setConversationChannel();
    FlutterLcIm.queryHistoryConversations(_limit, _offset);
  }

  //监听channel，接收会话列表
  void _setConversationChannel() {
    _conversationEventChannel
        .receiveBroadcastStream(CONVERSATION_CHANNEL)
        .listen((Object cons) {
      List<ImConversation> conversations =
          ImConversations.fromJson(cons).conversations;
      _loadData(conversations);
    }).onError((Object o) {});
  }

  void _loadData(List<ImConversation> conversations) {
    setState(() {
      _conversations = conversations;
      _refreshController.refreshCompleted();
    });

    //根据用户id到自己的服务器上获取头像，然后更新UI
    List peerIds = List();
    for (var item in _conversations) {
      peerIds.add(item.peerId);
      // http request
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: _conversations.length == 0
          ? SizedBox()
          : SmartRefresher(
              enablePullDown: true,
              enablePullUp: false,
              header: imRefreshHeader(),
              controller: _refreshController,
              onRefresh: _onRefresh,
              child: ListView.builder(
                  scrollDirection: Axis.vertical,
                  itemCount: _conversations.length,
                  itemBuilder: (BuildContext context, int index) {
                    return InkWell(
                        onTap: () {
                          Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (context) => ImConversationPage(
                                        currentUser: ImUser(
                                          uid: _conversations[index].clientId,
                                        ),
                                        toUser: ImUser(
                                          uid: _conversations[index].peerId,
                                          username:
                                              _conversations[index].peerName,
                                        ),
                                      )));
                        },
                        child: _buildListItem(_conversations[index]));
                  }),
            ),
    );
  }

  Widget _buildListItem(ImConversation conversation) {
    return Container(
      padding: const EdgeInsets.only(top: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: <Widget>[
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              conversation.unreadMessagesCount == 0
                  ? Container(
                      margin: EdgeInsets.only(left: 20, top: 7),
                      padding: EdgeInsets.all(10),
                      height: 46,
                      width: 46,
                      decoration: BoxDecoration(
                        color: Colors.black,
                        borderRadius: BorderRadius.circular(6.0),
                        // image url 去要到自己的服务器上去请求回来再赋值，这里使用一张默认值即可
                        image: DecorationImage(
                            image: NetworkImage(conversation.peerAvatarUrl)),
                      ),
                    )
                  : Badge(
                      badgeContent: Text(
                        '${conversation.unreadMessagesCount}',
                        style: TextStyle(color: Colors.white, fontSize: 12),
                      ),
                      child: Container(
                        margin: EdgeInsets.only(left: 20, top: 7),
                        padding: EdgeInsets.all(10),
                        height: 46,
                        width: 46,
                        decoration: BoxDecoration(
                          color: Colors.black,
                          borderRadius: BorderRadius.circular(6.0),
                          // image url 去要到自己的服务器上去请求回来再赋值，这里使用一张默认值即可
                          image: DecorationImage(
                              image: NetworkImage(conversation.peerAvatarUrl)),
                        ),
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
                        conversation.peerName,
                        style: TextStyle(fontWeight: FontWeight.w500),
                      ),
                    ),
                    Container(
                      constraints: BoxConstraints(maxWidth: 280),
                      margin: EdgeInsets.only(top: 8),
                      child: Text(conversation.lastMessage.text,
                          overflow: TextOverflow.ellipsis,
                          maxLines: 1,
                          style: TextStyle(color: Colors.grey, fontSize: 12)),
                    ),
                  ],
                ),
              ),
            ],
          ),
          Container(
            margin: EdgeInsets.only(right: 14, bottom: 18),
            child: Text(conversation.lastMessageAt,
                style: TextStyle(color: Colors.grey, fontSize: 11)),
          )
        ],
      ),
    );
  }
}
