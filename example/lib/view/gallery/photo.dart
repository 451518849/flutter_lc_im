import 'dart:io';

import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

class MessageGalleryView extends StatefulWidget {
  MessageGalleryView({
    this.url,
    this.backgroundDecoration,
  }) : pageController = PageController(initialPage: 0);

  final String url;
  final Decoration backgroundDecoration;
  final PageController pageController;

  @override
  State<StatefulWidget> createState() {
    return _MessageGalleryViewState();
  }
}

class _MessageGalleryViewState extends State<MessageGalleryView> {
  int currentIndex;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          elevation: 0,
          iconTheme: IconThemeData(color: Colors.white), // 头部图标颜色
          centerTitle: true,
          backgroundColor: Colors.black,
        ),
        body: GestureDetector(
          onTap: () {
            Navigator.pop(context);
          },
          child: Container(
              constraints: BoxConstraints.expand(
                height: MediaQuery.of(context).size.height,
              ),
              child: Stack(
                alignment: Alignment.bottomRight,
                children: <Widget>[
                  PhotoViewGallery.builder(
                    scrollPhysics: const BouncingScrollPhysics(),
                    builder: (BuildContext context, int index) {
                      return PhotoViewGalleryPageOptions(
                        imageProvider: widget.url.contains("http")
                            ? NetworkImage(widget.url)
                            : FileImage(File(widget.url)),
                        initialScale: PhotoViewComputedScale.contained * 0.8,
                        heroAttributes:
                            PhotoViewHeroAttributes(tag: widget.url),
                      );
                    },
                    itemCount: 1,
                    loadingBuilder: (context, event) => Center(
                      child: Container(
                        width: 20.0,
                        height: 20.0,
                        child: CircularProgressIndicator(
                          value: event == null
                              ? 0
                              : event.cumulativeBytesLoaded /
                                  event.expectedTotalBytes,
                        ),
                      ),
                    ),
                    backgroundDecoration: widget.backgroundDecoration,
                    pageController: widget.pageController,
                  )
                ],
              )),
        ));
  }
}
