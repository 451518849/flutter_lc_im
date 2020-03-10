import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/view/avatar.dart';
import 'package:bubble/bubble.dart';
import 'package:audioplayers/audioplayers.dart';

import 'message_gallery.dart';

const int MessageLeftAlign = 1;
const int MessageRightAlign = 2;

//压缩图片，图片对flutter内存的影响很大，压缩防止崩溃
const String ImageSize = '?imageView2/2/w/200/h/200';

AudioPlayer audioPlayer = AudioPlayer();

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
    return _messageView(context);
  }

  Widget _messageView(BuildContext context) {
    if (message.messageType == ImMessageType.text) {
      return _textMessage();
    } else if (message.messageType == ImMessageType.image) {
      return _imageMessage(context);
    } else if (message.messageType == ImMessageType.audio) {
      return _voiceMessage(context);
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

  Widget _imageMessage(BuildContext context) {
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
            GestureDetector(
              onTap: () =>
                  _pushToFullImage(context, message.url, message.image),
              child: Container(
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
            ),
          ],
        ),
      );
    } else {
      return GestureDetector(
        onTap: () => _pushToFullImage(context, message.url, message.image),
        child: Container(
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
        ),
      );
    }
  }

  void _pushToFullImage(BuildContext context, String url, File image) {
    Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => MessageGalleryView(
                image: image,
                backgroundDecoration:
                    const BoxDecoration(color: Colors.black87),
                url: url)));
  }

  Widget _voiceMessage(BuildContext context) {
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
            GestureDetector(
              onTap: () => _speakVoice(message.url),
              child: Container(
                margin: const EdgeInsets.only(bottom: 10, left: 4),
                child: Bubble(
                  stick: true,
                  nip: BubbleNip.leftBottom,
                  color: Colors.white,
                  child: Row(
                    children: <Widget>[
                      Image.asset(
                        'assets/images/speak_left.png',
                        width: 20,
                        height: 20,
                      ),
                      Container(child: Text('          ')),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      );
    } else {
      return GestureDetector(
        onTap: () => _speakVoice(message.url),
        child: Container(
          margin: const EdgeInsets.only(right: 10, top: 10),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: <Widget>[
              Container(
                margin: const EdgeInsets.only(bottom: 10, right: 4),
                child: Bubble(
                  stick: true,
                  nip: BubbleNip.rightBottom,
                  color: color,
                  child: Row(
                    children: <Widget>[
                      Container(child: Text('          ')),
                      Image.asset(
                        'assets/images/speak_right.png',
                        width: 20,
                        height: 20,
                      ),
                    ],
                  ),
                ),
              ),
              Container(
                child: ImAvatar(avatarUrl: this.avatarUrl),
              ),
            ],
          ),
        ),
      );
    }
  }

  void _speakVoice(String path) async {
    
    if (path.contains("http")) {
      await audioPlayer.play(path);
    } else {
      await audioPlayer.play(path, isLocal: true);
    }
  }
}
