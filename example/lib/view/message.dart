import 'package:flutter/material.dart';
import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/view/avatar.dart';
import 'package:bubble/bubble.dart';

const int MessageLeftAlign = 1;
const int MessageRightAlign = 2;

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
                    style: TextStyle(fontSize: 14.0)),
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
                    style: TextStyle(fontSize: 14.0)),
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
