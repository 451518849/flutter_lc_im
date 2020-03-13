import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/view/avatar.dart';
import 'package:bubble/bubble.dart';
import 'package:flutter_lc_im_example/view/gallery/photo.dart';

import '../message.dart';

class ImageMessage extends StatefulWidget {
  final ImMessage message;
  final int messageAlign;
  final String avatarUrl;
  final Color color;

  ImageMessage(
      {Key key, this.message, this.messageAlign, this.avatarUrl, this.color})
      : super(key: key);

  @override
  _ImageMessageState createState() => _ImageMessageState();
}

class _ImageMessageState extends State<ImageMessage> {
  @override
  Widget build(BuildContext context) {
    return _buildImageMessage(context);
  }

  Widget _buildImageMessage(BuildContext context) {
    if (widget.messageAlign == MessageLeftAlign) {
      return Container(
        margin: const EdgeInsets.only(left: 10, top: 10),
        child: Row(
          children: <Widget>[
            Container(
              child: ImAvatar(
                avatarUrl: widget.avatarUrl,
              ),
            ),
            GestureDetector(
              onTap: () => _pushToFullImage(context, widget.message.url),
              child: Container(
                height: 200,
                width: 200,
                margin: const EdgeInsets.only(bottom: 10, left: 4),
                child: Bubble(
                  stick: true,
                  nip: BubbleNip.leftBottom,
                  color: Colors.white,
                  child: Image(
                    image: widget.message.url.contains("http")
                        ? NetworkImage(widget.message.url + ImageSize)
                        : FileImage(File(widget.message.url)),
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
        onTap: () => _pushToFullImage(context, widget.message.url),
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
                  color: widget.color,
                  child: Image(
                    image: widget.message.url.contains("http")
                        ? NetworkImage(widget.message.url + ImageSize)
                        : FileImage(File(widget.message.url)),
                    fit: BoxFit.cover,
                  ),
                ),
              ),
              Container(
                child: ImAvatar(avatarUrl: widget.avatarUrl),
              ),
            ],
          ),
        ),
      );
    }
  }

  void _pushToFullImage(BuildContext context, String url) {
    Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => MessageGalleryView(
                backgroundDecoration:
                    const BoxDecoration(color: Colors.black87),
                url: url)));
  }
}
