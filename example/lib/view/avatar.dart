import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ImAvatar extends StatelessWidget {
  final String avatarUrl;
  final double width;
  final double radius;
  ImAvatar({this.avatarUrl, this.width = 38.0, this.radius = 4});

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Column(
        children: <Widget>[
          Container(
              width: this.width,
              height: this.width,
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(this.radius),
                  image: DecorationImage(
                      image: NetworkImage(this.avatarUrl), fit: BoxFit.cover))),
        ],
      ),
    );
  }
}
