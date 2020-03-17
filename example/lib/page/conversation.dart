import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/model/user.dart';
import 'package:flutter_lc_im_example/utils/date.dart';
import 'package:flutter_lc_im_example/utils/permission.dart';
import 'package:flutter_lc_im_example/view/emoji/emoji_picker.dart';
import 'package:flutter_lc_im_example/view/message.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';
import 'package:flutter_lc_im_example/view/voice/voice.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';
import 'package:image_picker/image_picker.dart';
import 'package:video_player/video_player.dart';

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
  static const EventChannel _messageEventChannel =
      EventChannel(CONVERSATION_MESSAGE_CHANNEL);

  final TextEditingController _textController = TextEditingController();
  FocusNode _focusNode = FocusNode(); // 初始化一个FocusNode控件
  RefreshController _refreshController =
      RefreshController(initialRefresh: false);
  ScrollController _scrollController = ScrollController();

  //控制是否显示相册的工具栏
  bool _isShowExpaned = false;

  //控制是否显示emoji
  bool _isShowEmoji = false;

  bool _isShowVoice = true;

//弹起工具栏的高度
  double _expandedPanelHeight = 200;

//添加图片滑动的距离
  double _imageScrollHeight = 220;

  List<ImMessage> _messages = [];

  List _iconbuttons = [
    {'name': '相册', 'icon': Icons.photo_size_select_actual},
    {'name': '视频', 'icon': Icons.video_label},
    {'name': '拍摄', 'icon': Icons.camera_alt}
  ];

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

    //检测需要聊天的权限
    checkConversationPermission();

    //监听channel，接收消息
    _setConversationMessageChannel();
    //加入到会话中
    FlutterLcIm.createConversation(widget.toUser.uid);
    _focusNode.addListener(_focusNodeListener); // 初始化一个listener
  }

  @override
  void dispose() {
    super.dispose();
    _focusNode.removeListener(_focusNodeListener); // 页面消失时必须取消这个listener！！
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
          _scrollToBottom();
        } else {
          //刷新历史消息
          List<ImMessage> messages = [];
          messages.addAll(newMessages);
          messages.addAll(_messages);
          _messages = messages;
          _refreshController.refreshCompleted();

          //如果是第一次加载数据，需要滑动到底部
          if (_messages.length <= _limit) {
            _scrollToBottom();
          }
        }
      }
    });
  }

  Future<Null> _focusNodeListener() async {
    if (_focusNode.hasFocus) {
      Future.delayed(Duration(milliseconds: 100), () {
        setState(() {
          _isShowExpaned = false;
          _isShowEmoji = false;
          _scrollToBottom();
        });
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: true,
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
                  if (_focusNode.hasFocus) {
                    FocusScope.of(context).requestFocus(FocusNode());
                  } else {
                    setState(() {
                      _isShowExpaned = false;
                      _isShowEmoji = false;
                    });
                  }
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
                      return _buildMessageRow(_messages[i], i);
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
            Divider(height: 1.0),
            !_isShowEmoji
                ? SizedBox()
                : Container(
                    decoration: BoxDecoration(
                        color: Color.fromRGBO(241, 243, 244, 0.9)),
                    child: _buildEmojiPanelComposer()),
            !_isShowExpaned
                ? SizedBox()
                : Container(
                    height: _expandedPanelHeight,
                    decoration: BoxDecoration(
                        color: Color.fromRGBO(241, 243, 244, 0.9)),
                    child: _buildExpandedPanelComposer()),
          ]),
        );
      }),
    );
  }

/*
 * 文字输入框
 */
  Widget _buildInputTextComposer() {
    return IconTheme(
      data: IconThemeData(color: Color.fromRGBO(241, 243, 244, 0.9)),
      child: Container(
        alignment: Alignment.center,
        height: 40.0,
        margin: const EdgeInsets.only(top: 5, bottom: 5),
        child: Row(
          children: <Widget>[
            _isShowVoice
                ? GestureDetector(
                    onTap: () => _openVoiceAction(context),
                    child: Container(
                        margin: const EdgeInsets.only(left: 10, right: 10),
                        child: Image.asset(
                          'assets/images/voice.png',
                          height: 24,
                          width: 24,
                        )),
                  )
                : GestureDetector(
                    onTap: () => _openKeyboardAction(context),
                    child: Container(
                        margin: const EdgeInsets.only(left: 8, right: 8),
                        child: Image.asset(
                          'assets/images/keyboard.png',
                          height: 28,
                          width: 28,
                        )),
                  ),
            _isShowVoice
                ? Flexible(
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
                  )
                : Expanded(
                    child: VoiceWidget(
                      startRecord: () {},
                      stopRecord: (path, length) {
                        _submitVoiceMsg(path, length);
                      },
                    ),
                  ),
            GestureDetector(
              onTap: () => _openEmojiAction(context),
              child: Container(
                  margin: const EdgeInsets.only(left: 10),
                  child: Image.asset('assets/images/emoji.png',
                      height: 29, width: 29, color: Colors.black)),
            ),
            GestureDetector(
              onTap: () => _openExpandedAction(context),
              child: Container(
                  margin: const EdgeInsets.only(left: 10, right: 10),
                  child: Image.asset('assets/images/more.png',
                      height: 24, width: 24, color: Colors.black)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageRow(ImMessage message, int index) {
    bool isShowMessageTime = index % _limit == 0 ? true : false;
    return Column(
      children: <Widget>[
        isShowMessageTime
            ? _buildMessageTime(tranFormatTime(message.timestamp))
            : SizedBox(),
        ImMessageItemView(
          message: message,
          avatarUrl: message.ioType == ImMessageIOType.messageIOTypeOut
              ? widget.currentUser.avatarUrl
              : widget.toUser.avatarUrl,
          messageAlign: message.ioType == ImMessageIOType.messageIOTypeOut
              ? MessageRightAlign
              : MessageLeftAlign,
        )
      ],
    );
  }

  Widget _buildMessageTime(String time) {
    return Container(
      alignment: Alignment.center,
      width: 100.0,
      child: Text(
        time,
        style: TextStyle(fontSize: 10, color: Colors.grey[600]),
      ),
    );
  }

/*
 * 下方的弹出emoji
 */
  Widget _buildEmojiPanelComposer() {
    return EmojiPicker(
      rows: 3,
      columns: 7,
      recommendKeywords: ["racing", "horse"],
      numRecommended: 10,
      onEmojiSelected: (emoji, category) {
        _textController.text = _textController.text + emoji.emoji;
      },
    );
  }

/*
 * 下方的弹出工具栏 
 */
  Widget _buildExpandedPanelComposer() {
    return GridView.builder(
        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4,
            crossAxisSpacing: 10.0,
            mainAxisSpacing: 40.0,
            childAspectRatio: 0.8),
        padding: const EdgeInsets.symmetric(horizontal: 25.0, vertical: 20.0),
        scrollDirection: Axis.vertical,
        itemCount: _iconbuttons.length,
        itemBuilder: (BuildContext context, int index) {
          return _buildIconButton(
              _iconbuttons[index]['name'], _iconbuttons[index]['icon']);
        });
  }

  /*
   * 工具栏中的图标
   */
  Widget _buildIconButton(String buttonName, IconData icon) {
    return Column(
      children: <Widget>[
        GestureDetector(
          excludeFromSemantics: true,
          onTap: () {
            _openExpandedIcon(buttonName);
          },
          child: Container(
            width: 60.0,
            height: 60.0,
            alignment: Alignment.center,
            decoration: BoxDecoration(
                color: Colors.white, borderRadius: BorderRadius.circular(10.0)),
            child: Icon(
              icon,
              size: 28.0,
            ),
          ),
        ),
        Container(
            margin: EdgeInsets.only(top: 3.0),
            child: Text(buttonName,
                style: TextStyle(fontSize: 12.0, color: Colors.grey[600])))
      ],
    );
  }

  /*
   * 点击 语音 图标 
   */
  void _openVoiceAction(BuildContext context) {
    _isShowExpaned = false;
    _isShowEmoji = false;
    _isShowVoice = false;
    if (_focusNode.hasFocus) {
      FocusScope.of(context).requestFocus(FocusNode());
    }

    setState(() {});
  }

  /*
   * 点击 键盘 图标 
   */
  void _openKeyboardAction(BuildContext context) {
    _isShowExpaned = false;
    _isShowEmoji = false;
    _isShowVoice = true;

    setState(() {});

    Future.delayed(Duration(milliseconds: 100), () {
      if (!_focusNode.hasFocus) {
        FocusScope.of(context).requestFocus(_focusNode);
      }
    });
  }

  /*
   * 点击 emoji 图标 
   */
  void _openEmojiAction(BuildContext context) {
    if (_focusNode.hasFocus) {
      FocusScope.of(context).requestFocus(FocusNode());

      //Focus和setState冲突，延迟执行setState
      Future.delayed(Duration(milliseconds: 100), () {
        setState(() {
          _isShowEmoji = !_isShowEmoji;
          _isShowExpaned = false;
          _isShowVoice = true;
          _scrollToBottom();
        });
      });
    } else {
      setState(() {
        _isShowEmoji = !_isShowEmoji;
        _isShowExpaned = false;
        _isShowVoice = true;
        _scrollToBottom();
      });
    }
  }

  /*
   * 点击 + 图标 
   */
  void _openExpandedAction(BuildContext context) {
    if (_focusNode.hasFocus) {
      FocusScope.of(context).requestFocus(FocusNode());

      //Focus和setState冲突，延迟执行setState
      Future.delayed(Duration(milliseconds: 100), () {
        setState(() {
          _isShowExpaned = !_isShowExpaned;
          _isShowEmoji = false;
          _isShowVoice = false;
          _scrollToBottom();
        });
      });
    } else {
      setState(() {
        _isShowExpaned = !_isShowExpaned;
        _isShowEmoji = false;
        _isShowVoice = false;
        _scrollToBottom();
      });
    }
  }

  void _openExpandedIcon(String iconName) async {
    if (iconName == '相册') {
      var image = await ImagePicker.pickImage(source: ImageSource.gallery);
      _submitImageMsg(image.path);
    } else if (iconName == '视频') {
      var video = await ImagePicker.pickVideo(source: ImageSource.gallery);
      _submitVideoMsg(video);
    } else if (iconName == '拍摄') {
      var image = await ImagePicker.pickImage(source: ImageSource.camera);
      _submitImageMsg(image.path);
    }
  }

  /*
  * 发送文字消息 
  */
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

    // _scrollToBottom();

    //发送到服务器
    FlutterLcIm.sendTextMessage(text);
  }

  /*
  * 发送图片+文字消息 
  */
  void _submitImageMsg(String path) async {
    if (path == null) {
      return;
    }

    ImMessage message = ImMessage(
        fromUser: widget.currentUser,
        toUser: widget.toUser,
        url: path,
        ioType: ImMessageIOType.messageIOTypeOut,
        messageType: ImMessageType.image);
    setState(() {
      _messages.add(message);
    });

    _scrollToBottom(offset: _imageScrollHeight);
    //发送到服务器
    FlutterLcIm.sendImageMessage(path);
  }

  /*
  * 发送视频+文字消息 
  */
  void _submitVoiceMsg(String path, double duration) async {
    if (path == null) {
      return;
    }

    ImMessage message = ImMessage(
        fromUser: widget.currentUser,
        toUser: widget.toUser,
        url: path,
        messageId: DateTime.now().millisecondsSinceEpoch.toString(),
        duration: duration.ceil(),
        ioType: ImMessageIOType.messageIOTypeOut,
        messageType: ImMessageType.audio);
    setState(() {
      _messages.add(message);
    });

    _scrollToBottom();

    //发送到服务器
    FlutterLcIm.sendVoiceMessage(path, duration.ceil().toString());
  }

  /*
  * 发送视频 
  */
  void _submitVideoMsg(File video) async {
    if (video == null) {
      return;
    }

    var controller = VideoPlayerController.file(video);
    await controller.initialize();
    int seconds = controller.value.duration.inSeconds;

    ImMessage message = ImMessage(
        fromUser: widget.currentUser,
        toUser: widget.toUser,
        url: video.path,
        duration: seconds,
        ioType: ImMessageIOType.messageIOTypeOut,
        messageType: ImMessageType.video);
    setState(() {
      _messages.add(message);
    });

    _scrollToBottom();

    //发送到服务器
    FlutterLcIm.sendVideoMessage(video.path, duration: seconds.toString());

    controller.dispose();
  }

  void _scrollToBottom({double offset = 0, int milliseconds = 100}) {
    Future.delayed(Duration(milliseconds: 300), () {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent + offset,
        curve: Curves.easeOut,
        duration: const Duration(milliseconds: 300),
      );
    });
  }
}
