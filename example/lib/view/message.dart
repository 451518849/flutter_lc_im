import 'package:flutter/material.dart';
import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/view/avatar.dart';
import 'package:bubble/bubble.dart';

const int MessageLeftAlign = 1;
const int MessageRightAlign = 2;

//压缩图片，图片对flutter内存的影响很大，压缩防止崩溃
const String ImageSize = '?imageView2/2/w/200/h/200';

/*
 * 单条消息 
 */
class ImMessageItemView extends StatelessWidget {
  final String avatarUrl;
  final Color color;
  final ImMessage message;
  final int messageAlign;
  const ImMessageItemView(
      {Key key,
      this.avatarUrl,
      this.color = const Color(0xfffdd82c),
      this.message,
      this.messageAlign = MessageLeftAlign})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return _messageView();
  }

  Widget _messageView() {
    if (message.messageType == ImMessageType.text) {
      return _textMessage();
    } else if (message.messageType == ImMessageType.image) {
      return _imageMessage();
    } else if (message.messageType == ImMessageType.audio) {
    } else if (message.messageType == ImMessageType.video) {}
    return SizedBox();
  }

  Widget _textMessage() {
    if (this.messageAlign == MessageLeftAlign) {
      return Container(
        margin: const EdgeInsets.only(left: 10, top: 10),
        child: Row(
          children: <Widget>[
            Container(
              child: ImAvatar(
                avatarUrl: this.avatarUrl,
              ),
            ),
            Container(
              margin: const EdgeInsets.only(bottom: 10, left: 4),
              constraints: BoxConstraints(maxWidth: 250),
              child: Bubble(
                stick: true,
                nip: BubbleNip.leftBottom,
                color: Colors.white,
                child: Text(message.text,
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: 16.0)),
              ),
            ),
          ],
        ),
      );
    } else {
      return Container(
        margin: const EdgeInsets.only(right: 10, top: 10),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: <Widget>[
            Container(
              margin: const EdgeInsets.only(bottom: 10, right: 4),
              constraints: BoxConstraints(maxWidth: 250),
              child: Bubble(
                stick: true,
                nip: BubbleNip.rightBottom,
                color: color,
                child: Text(message.text,
                    textAlign: TextAlign.left,
                    style: TextStyle(fontSize: 16.0)),
              ),
            ),
            Container(
              child: ImAvatar(avatarUrl: this.avatarUrl),
            ),
          ],
        ),
      );
    }
  }

  Widget _imageMessage() {
    if (this.messageAlign == MessageLeftAlign) {
      return Container(
        margin: const EdgeInsets.only(left: 10, top: 10),
        child: Row(
          children: <Widget>[
            Container(
              child: ImAvatar(
                avatarUrl: this.avatarUrl,
              ),
            ),
            Container(
              height: 200,
              width: 200,
              margin: const EdgeInsets.only(bottom: 10, left: 4),
              child: Bubble(
                stick: true,
                nip: BubbleNip.leftBottom,
                color: Colors.white,
                child: Image(
                  image: message.image != null
                      ? FileImage(message.image)
                      : NetworkImage(message.url + ImageSize),
                  fit: BoxFit.cover,
                ),
              ),
            ),
          ],
        ),
      );
    } else {
      return Container(
        margin: const EdgeInsets.only(right: 10, top: 10),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: <Widget>[
            Container(
              height: 200,
              width: 200,
              margin: const EdgeInsets.only(bottom: 10, right: 4),
              child: Bubble(
                stick: true,
                nip: BubbleNip.rightBottom,
                color: color,
                child: Image(
                  image: message.image != null
                      ? FileImage(message.image)
                      : NetworkImage(message.url + ImageSize),
                  fit: BoxFit.cover,
                ),
              ),
            ),
            Container(
              child: ImAvatar(avatarUrl: this.avatarUrl),
            ),
          ],
        ),
      );
    }
  }
}
