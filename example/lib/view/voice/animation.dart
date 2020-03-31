import 'package:flutter/material.dart';

import 'indictor.dart';

class PulseOutIndicator extends Indicator {
  var scaleYDoubles = [1.0, 1.0, 1.0, 1.0, 1.0,1.0, 1.0, 1.0, 1.0, 1.0,1.0, 1.0, 1.0, 1.0, 1.0,1.0, 1.0, 1.0, 1.0, 1.0];
  int pluseLength = 20;
  
  @override
  paint(Canvas canvas, Paint paint, Size size) {
    var translateX = size.width / 11;
    var translateY = size.height / 2;
    for (int i = 0; i < pluseLength; i++) {
      canvas.save();
      canvas.translate((2 + i * 2) * translateX - translateX / 2, translateY);
      canvas.scale(1.0, scaleYDoubles[i]);
      var rectF = RRect.fromLTRBR(-translateX / 2, -size.height / 2.5,
          translateX / 2, size.height / 2.5, Radius.circular(5));
      canvas.drawRRect(rectF, paint);
      canvas.restore();
    }
  }

  @override
  List<AnimationController> animation() {
    var controllers = List<AnimationController>();
    for (int i = 0; i < pluseLength; i++) {
      var sizeController = new AnimationController(
          duration: Duration(milliseconds: 200), vsync: context);
      var alphaTween = new Tween(begin: 1.0, end: 0.3).animate(sizeController);
      sizeController.addListener(() {
        scaleYDoubles[i] = alphaTween.value;
        postInvalidate();
      });
      controllers.add(sizeController);
    }
    return controllers;
  }

  @override
  startAnims(List<AnimationController> controllers) {
    var delays = [1000,850,700,550,400,250,100,50,0,0,0,0,0,50,100,250,400,550,700,850,1000];
    for (var i = 0; i < controllers.length; i++) {
      Future.delayed(Duration(milliseconds: delays[i]), () {
        if (context.mounted) controllers[i].repeat(reverse: true);
      });
    }
  }
}
