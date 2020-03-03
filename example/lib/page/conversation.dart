import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/model/user.dart';
import 'package:flutter_lc_im_example/view/message.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';

const String CONVERSATION_MESSAGE_CHANNEL = "flutter_lc_im/messages";

/*
 * 聊天界面 
 * 支持加载历史消息
 * 支持文本、图片、语音和视频消息的发送
 */
class ImConversationPage extends StatefulWidget {
  final ImUser currentUser;
  final ImUser toUser;
  final Color color;
  ImConversationPage(
      {Key key,
      this.currentUser,
      this.toUser,
      this.color = const Color(0xfffdd82c)})
      : super(key: key);

  @override
  _ImConversationPageState createState() => _ImConversationPageState();
}

class _ImConversationPageState extends State<ImConversationPage> {
  final TextEditingController _textController = TextEditingController();
  FocusNode _focusNode = FocusNode(); // 初始化一个FocusNode控件
  RefreshController _refreshController =
      RefreshController(initialRefresh: false);
  ScrollController _scrollController = ScrollController();

  List<ImMessage> _messages = [];

  static const _messageEventChannel =
      EventChannel(CONVERSATION_MESSAGE_CHANNEL);

  int _limit = 10; //一次加载10条数据

  //只有下拉加载更多数据
  void _onRefresh() async {
    //如果首次加载的message没有limit条表示没有历史数据，不能刷新
    if (_messages.length >= _limit) {
      ImMessage firstMessage = _messages[0];
      FlutterLcIm.queryHistoryConversationMessages(
          _limit, firstMessage.messageId, firstMessage.timestamp);
    } else {
      _refreshController.refreshCompleted();
    }
  }

  Widget imRefreshHeader() {
    return ClassicHeader(
      refreshingText: "加载中...",
      idleText: "加载聊天记录",
      completeText: '加载完成',
      releaseText: '松开刷新数据',
      failedIcon: null,
      failedText: '刷新失败，请重试。',
    );
  }

  @override
  void initState() {
    super.initState();

    //监听channel，接收消息
    _setConversationMessageChannel();
    //加入到会话中
    FlutterLcIm.createConversation(widget.currentUser.uid, widget.toUser.uid);
    _focusNode.addListener(_focusNodeListener); // 初始化一个listener
  }

  @override
  void dispose() {
    _focusNode.removeListener(_focusNodeListener); // 页面消失时必须取消这个listener！！
    super.dispose();
  }

  void _setConversationMessageChannel() {
    _messageEventChannel
        .receiveBroadcastStream(CONVERSATION_MESSAGE_CHANNEL)
        .listen((Object mess) {
      List<ImMessage> newMessages = ImMessages.fromJson(mess).messages;
      _loadData(newMessages);
    }).onError((Object o) {});
  }

  void _loadData(List<ImMessage> newMessages) {
    setState(() {
      if (newMessages.length == 0) {
        _refreshController.refreshCompleted();
      } else {
        //如果第一条数据相同表示是旧数据
        if (_messages.length != 0 &&
            newMessages[0].messageId == _messages[0].messageId) {
          _refreshController.refreshCompleted();
          return;
        } else if (newMessages.length == 1 &&
            newMessages[0].timestamp > _messages[0].timestamp) {
          //当消息只有一条时，需要判断是接收消息还是刷新的历史消息，接收消息的时间大于历史消息
          _messages.addAll(newMessages);
          //延迟执行滑动，等界面重新加载数据后再执行滑动
          Future.delayed(Duration(seconds: 1), () {
            _scrollToBottom();
          });
        } else {
          //刷新历史消息
          List<ImMessage> messages = [];
          messages.addAll(newMessages);
          messages.addAll(_messages);
          _messages = messages;
          _refreshController.refreshCompleted();
        }
      }
    });
  }

  Future<Null> _focusNodeListener() async {
    if (_focusNode.hasFocus && _messages.length > 4) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent + 300,
        curve: Curves.easeOut,
        duration: const Duration(milliseconds: 300),
      );
    } else {}
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: widget.color,
        title: Text(widget.toUser.username),
      ),
      body: Builder(builder: (BuildContext context) {
        return Container(
          margin: const EdgeInsets.only(top: 30),
          child: Column(children: <Widget>[
            Flexible(
              child: GestureDetector(
                behavior: HitTestBehavior.translucent,
                onTap: () {
                  FocusScope.of(context).requestFocus(FocusNode());
                },
                child: SmartRefresher(
                  enablePullDown: true,
                  enablePullUp: false,
                  header: imRefreshHeader(),
                  controller: _refreshController,
                  onRefresh: _onRefresh,
                  child: ListView.builder(
                    controller: _scrollController,
                    physics: BouncingScrollPhysics(),
                    itemCount: _messages.length,
                    itemBuilder: (context, i) {
                      return _buildMessageRow(_messages[i]);
                    },
                  ),
                ),
              ),
            ),
            Divider(height: 1.0),
            Container(
              child: _buildInputTextComposer(),
              decoration:
                  BoxDecoration(color: Color.fromRGBO(241, 243, 244, 0.9)),
            ),
          ]),
        );
      }),
    );
  }

  Widget _buildMessageRow(ImMessage message) {
    return ImMessageItemView(
      message: message,
      avatarUrl: message.ioType == ImMessageIOType.messageIOTypeOut
          ? widget.currentUser.avatarUrl
          : widget.toUser.avatarUrl,
      messageAlign: message.ioType == ImMessageIOType.messageIOTypeOut
          ? MessageRightAlign
          : MessageLeftAlign,
    );
  }

  Widget _buildInputTextComposer() {
    return IconTheme(
      data: IconThemeData(color: Color.fromRGBO(241, 243, 244, 0.9)),
      child: Container(
        alignment: Alignment.center,
        height: 40.0,
        margin: const EdgeInsets.only(top: 3, bottom: 3),
        child: Row(
          children: <Widget>[
            Container(
                margin: const EdgeInsets.only(left: 10, right: 10),
                child: Image.asset(
                  'assets/images/voice.png',
                  height: 24,
                  width: 24,
                )),
            Flexible(
              child: Container(
                margin: const EdgeInsets.only(top: 2, bottom: 2),
                child: TextField(
                  textInputAction: TextInputAction.send,
                  controller: _textController,
                  focusNode: _focusNode,
                  onSubmitted: _submitMsg,
                  decoration: InputDecoration(
                    contentPadding: EdgeInsets.all(10.0),
                    fillColor: Colors.white,
                    filled: true,
                    border: OutlineInputBorder(
                      borderSide: BorderSide.none,
                      borderRadius: BorderRadius.all(Radius.circular(4)),
                    ),
                  ),
                ),
              ),
            ),
            GestureDetector(
              onTap: () => openAction(),
              child: Container(
                  margin: const EdgeInsets.only(left: 10, right: 10),
                  child: Image.asset(
                    'assets/images/more.png',
                    height: 24,
                    width: 24,
                  )),
            ),
          ],
        ),
      ),
    );
  }

  Future<Null> openAction() {
    return showModalBottomSheet(
        context: context,
        builder: (BuildContext context) {
          return Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              ListTile(
                leading: Icon(Icons.photo_camera),
                title: Text("相机拍摄"),
                onTap: () async {
                  // var image = await ImagePicker.pickImage(source: ImageSource.camera);
                  Navigator.pop(context);
                },
              ),
              ListTile(
                leading: Icon(Icons.photo_library),
                title: Text("照片库"),
                onTap: () async {
                  // var image = await ImagePicker.pickImage(source: ImageSource.gallery);
                  Navigator.pop(context);
                },
              ),
            ],
          );
        });
  }

  void _submitMsg(String text) async {
    if (text == null || text == "") {
      return;
    }

    _textController.clear();

    ImMessage message = ImMessage(
        fromUser: widget.currentUser,
        toUser: widget.toUser,
        text: text,
        ioType: ImMessageIOType.messageIOTypeOut,
        messageType: ImMessageType.text);
    setState(() {
      _messages.add(message);
    });

    _scrollToBottom();

    //发送到服务器
    FlutterLcIm.sendMessage(text,"",ImMessageType.text);
  }

  void _scrollToBottom() {
    _scrollController.animateTo(
      _scrollController.position.maxScrollExtent,
      curve: Curves.easeOut,
      duration: const Duration(milliseconds: 300),
    );
  }
}
