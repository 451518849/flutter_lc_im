import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/flutter_plugin_record.dart';

import 'animation.dart';
import 'loading.dart';

typedef startRecord = Future Function();
typedef stopRecord = Future Function();

class VoiceWidget extends StatefulWidget {
  final Function startRecord;
  final Function stopRecord;

  /// startRecord 开始录制回调  stopRecord回调
  const VoiceWidget({Key key, this.startRecord, this.stopRecord})
      : super(key: key);

  @override
  _VoiceWidgetState createState() => _VoiceWidgetState();
}

class _VoiceWidgetState extends State<VoiceWidget> {
  double starty = 0.0;
  double offset = 0.0;
  bool isUp = false;
  String textShow = "按住 说话";
  String toastShow = "手指上滑,取消发送";
  String voiceIco = "assets/images/voice_volume_1.png";

  ///默认隐藏状态
  bool voiceState = true;
  OverlayEntry overlayEntry;
  FlutterPluginRecord recordPlugin;

  @override
  void initState() {
    super.initState();
    recordPlugin = FlutterPluginRecord();
    _init();

    ///初始化方法的监听
    recordPlugin.responseFromInit.listen((data) {
      if (data) {
        print("初始化成功");
      } else {
        print("初始化失败");
      }
    });

    /// 开始录制或结束录制的监听
    recordPlugin.response.listen((data) {
      if (data.msg == "onStop") {
        ///结束录制时会返回录制文件的地址方便上传服务器
        print("onStop  " + data.path);
        widget.stopRecord(data.path, data.audioTimeLength);
      } else if (data.msg == "onStart") {
        widget.startRecord();
      }
    });

  }

  ///显示录音悬浮布局
  buildOverLayView(BuildContext context) {
    if (overlayEntry == null) {
      overlayEntry = OverlayEntry(builder: (content) {
        return Positioned(
          top: MediaQuery.of(context).size.height * 0.5,
          left: MediaQuery.of(context).size.width * 0.5 - 80,
          child: Material(
            type: MaterialType.transparency,
            child: Center(
              child: Opacity(
                opacity: 0.8,
                child: Container(
                  width: 160,
                  height: 80,
                  decoration: BoxDecoration(
                    color: Color(0xff77797A),
                    borderRadius: BorderRadius.all(Radius.circular(20.0)),
                  ),
                  child: Column(
                    children: <Widget>[
                      Container(
                        margin: EdgeInsets.only(top: 10),
                        child: Text(
                          toastShow,
                          style: TextStyle(
                            fontStyle: FontStyle.normal,
                            color: Colors.white,
                            fontSize: 14,
                          ),
                        ),
                      ),
                      Container(
                        margin: EdgeInsets.only(top: 10,left: 40),
                        alignment: Alignment.centerLeft,
                        child: Loading(
                          indicator: PulseOutIndicator(),
                          size: 20.0,
                          color: Colors.white,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        );
      });
      Overlay.of(context).insert(overlayEntry);
    }
  }

  showVoiceView() {
    setState(() {
      textShow = "松开结束";
      voiceState = false;
    });
    buildOverLayView(context);
    start();
  }

  hideVoiceView() {
    setState(() {
      textShow = "按住说话";
      voiceState = true;
    });

    stop();
    if (overlayEntry != null) {
      overlayEntry.remove();
      overlayEntry = null;
    }

    if (isUp) {
      print("取消发送");
    } else {
      print("进行发送");
    }
  }

  moveVoiceView() {
    // print(offset - start);
    setState(() {
      isUp = starty - offset > 100 ? true : false;
      if (isUp) {
        textShow = "松开手指,取消发送";
        toastShow = textShow;
      } else {
        textShow = "松开 结束";
        toastShow = "手指上滑,取消发送";
      }
    });
  }

  ///初始化语音录制的方法
  void _init() async {
    recordPlugin.init();
  }

  ///开始语音录制的方法
  void start() async {
    recordPlugin.start();
  }

  ///停止语音录制的方法
  void stop() {
    recordPlugin.stop();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      child: GestureDetector(
        onVerticalDragStart: (details) {
          starty = details.globalPosition.dy;
          showVoiceView();
        },
        onVerticalDragDown: (details) {
          starty = details.globalPosition.dy;
          showVoiceView();
        },
        onVerticalDragCancel: () => hideVoiceView(),
        onVerticalDragEnd: (details) => hideVoiceView(),
        onVerticalDragUpdate: (details) {
          offset = details.globalPosition.dy;
          moveVoiceView();
        },
        child: Container(
            margin: const EdgeInsets.only(top: 2, bottom: 2),
            child: FlatButton(
              color: Colors.white,
              child: Text(
                textShow,
                style: TextStyle(
                    fontWeight: FontWeight.bold, color: Colors.grey[600]),
              ),
              onPressed: () {},
            )),
      ),
    );
  }

  @override
  void dispose() {
    if (recordPlugin != null) {
      recordPlugin.dispose();
    }
    super.dispose();
  }
}
