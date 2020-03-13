import 'dart:io';

import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

class MessageVideoGalleryView extends StatefulWidget {
  MessageVideoGalleryView({Key key, this.url}) : super(key: key);
  final String url;

  @override
  _MessageVideoGalleryViewState createState() =>
      _MessageVideoGalleryViewState();
}

class _MessageVideoGalleryViewState extends State<MessageVideoGalleryView> {
  VideoPlayerController _controller;

  @override
  void initState() {
    super.initState();
    if (widget.url.contains("http")) {
      _controller = VideoPlayerController.network(widget.url)
        ..initialize().then((_) {
          setState(() {});
        });
    } else {
      _controller = VideoPlayerController.file(File(widget.url))
        ..initialize().then((_) {
          setState(() {});
        });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          elevation: 0,
          // iconTheme: IconThemeData(color: Colors.white), // 头部图标颜色
          // centerTitle: true,
          backgroundColor: Colors.black,
        ),
        body: Center(
          child: _controller.value.initialized
              ? AspectRatio(
                  aspectRatio: _controller.value.aspectRatio,
                  child: VideoPlayer(_controller),
                )
              : Container(),
        ));
  }
}
