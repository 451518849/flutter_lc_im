import 'dart:async';

import 'package:flutter/services.dart';

class FlutterLcIm {
  static const MethodChannel _channel =
      const MethodChannel('flutter_lc_im');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic>pushToChatView(String userId,String peerId,String appUrl) async {
    var result = await _channel.invokeMethod(
      'pushToChatView',
      {
        'userId': userId,
        'peerId': peerId,
        'appUrl': appUrl
      }
    );
    return result;
  }
}
